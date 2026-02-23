package com.example.rest.services

import android.app.*
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.rest.R
import com.example.rest.data.models.HistorialApp

import com.example.rest.network.SupabaseClient
import com.example.rest.features.blocking.BloqueoActivity
import com.example.rest.data.models.AppVinculada
import com.example.rest.data.repository.LocalBlockingRepository
import com.example.rest.features.tools.AppBloqueo
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.TimeZone

/**
 * Servicio foreground que monitorea qué app está en primer plano
 * y registra sesiones automáticamente en tiempo real
 */
class AppMonitorService : Service() {

    companion object {
        private const val TAG = "AppMonitorService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "app_monitoring_channel"
        private const val CHECK_INTERVAL = 3000L // 3 segundos
        private const val SYNC_INTERVAL = 60000L // 1 minuto
        
        // Acción para iniciar el servicio
        fun startService(context: Context) {
            val intent = Intent(context, AppMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        // Acción para detener el servicio
        fun stopService(context: Context) {
            val intent = Intent(context, AppMonitorService::class.java)
            context.stopService(intent)
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val api = SupabaseClient.api
    private val timestampFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }
    
    private var currentPackageName: String? = null
    // private var currentSessionId: Int? = null // Eliminado
    private var sessionStartTime: Long = 0
    private var dispositivoId: Int = -1
    private var isMonitoring = false
    
    // Repositorio local
    private lateinit var localRepository: LocalBlockingRepository
    
    // Lista de apps restringidas (cache local)
    private var restrictedApps: List<AppBloqueo> = emptyList()
    
    // Mapa de uso diario por paquete (minutos)
    private var dailyUsageMap: MutableMap<String, Int> = mutableMapOf()
    
    private var lastRestrictionCheckTime: Long = 0

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Servicio creado")
        
        // Obtener ID del usuario
        val sharedPref = getSharedPreferences("RestCyclePrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("ID_USUARIO", -1)
        
        if (userId == -1) {
            Log.e(TAG, "No hay usuario logueado, deteniendo servicio")
            stopSelf()
            return
        }
        
        // Intentar obtener ID de dispositivo de SharedPreferences
        dispositivoId = sharedPref.getInt("ID_DISPOSITIVO", -1)
        
        // Si no está guardado, obtenerlo/crearlo desde la API
        if (dispositivoId == -1) {
            serviceScope.launch {
                try {
                    val response = api.obtenerDispositivosPorUsuario("eq.$userId")
                    if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                        dispositivoId = response.body()!![0].id ?: -1
                        
                        // Guardar en SharedPreferences
                        with(sharedPref.edit()) {
                            putInt("ID_DISPOSITIVO", dispositivoId)
                            apply()
                        }
                        
                        Log.d(TAG, "ID de dispositivo obtenido: $dispositivoId")
                    } else {
                        // Crear nuevo dispositivo
                        val nuevoDispositivo = com.example.rest.data.models.DispositivoInput(
                            idUsuario = userId,
                            nombre = "Android Device",
                            ip = "0.0.0.0", // IP por defecto
                            estado = "activo"
                        )
                        
                        val createResponse = api.crearDispositivo(nuevoDispositivo)
                        if (createResponse.isSuccessful && createResponse.body() != null && createResponse.body()!!.isNotEmpty()) {
                            dispositivoId = createResponse.body()!![0].id ?: -1
                            
                            // Guardar en SharedPreferences
                            with(sharedPref.edit()) {
                                putInt("ID_DISPOSITIVO", dispositivoId)
                                apply()
                            }
                            
                            Log.d(TAG, "Dispositivo creado con ID: $dispositivoId")
                        } else {
                            Log.e(TAG, "Error creando dispositivo, deteniendo servicio")
                            stopSelf()
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error obteniendo dispositivo: ${e.message}")
                    stopSelf()
                    return@launch
                }
            }
        }
        
        createNotificationChannel()
        
        if (Build.VERSION.SDK_INT >= 34) { // Android 14+
            startForeground(
                NOTIFICATION_ID, 
                createNotification(), 
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Servicio iniciado")
        
        if (!isMonitoring) {
            isMonitoring = true
        
            // Inicializar repositorio local
            localRepository = LocalBlockingRepository(this)
            
            startMonitoring()
        }
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Servicio destruido")
        
        isMonitoring = false
        handler.removeCallbacksAndMessages(null)
        
        // Guardar última sesión pendiente si es válida
        currentPackageName?.let { packageName ->
            val now = System.currentTimeMillis()
            val durationSeconds = ((now - sessionStartTime) / 1000).toInt()
            
            if (durationSeconds >= 2) {
                // Usamos runBlocking aquí porque el servicio se está destruyendo
                val duracion = durationSeconds
                try {
                    runBlocking {
                        withTimeout(2000) { 
                             registrarUsoApp(packageName, duracion)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "No se pudo guardar la última sesión al destruir: ${e.message}")
                }
            }
        }
        
        serviceScope.cancel()
    }

    private var lastSyncTime: Long = 0

    /**
     * Inicia el monitoreo periódico
     */
    private fun startMonitoring() {
        // Inicializar uso diario con estadísticas del sistema
        inicializarUsoDiario()
        
        handler.post(object : Runnable {
            override fun run() {
                if (isMonitoring) {
                    val now = System.currentTimeMillis()
                    if (now - lastSyncTime > SYNC_INTERVAL) {
                        sincronizarReglasConBaseDeDatos()
                        lastSyncTime = now
                    }
                    
                    actualizarListaRestricciones()
                    checkForegroundApp()
                    handler.postDelayed(this, CHECK_INTERVAL)
                }
            }
        })
    }
    
    /**
     * Sincroniza las reglas de bloqueo con la base de datos (Supabase)
     */
    private fun sincronizarReglasConBaseDeDatos() {
        if (dispositivoId == -1) return
        
        serviceScope.launch {
            try {
                // Primero, subir apps locales que no estén en la nube
                // DESACTIVADO: El usuario quiere seleccionar manualmente qué apps vincular
                // subirAppsInstaladas()
                
                val response = api.obtenerAppsVinculadas("eq.$dispositivoId")
                if (response.isSuccessful) {
                    val appsVinculadas = response.body() ?: emptyList()
                    
                    if (appsVinculadas.isNotEmpty()) {
                        // Obtener lista actual para preservar colores/iconos si es posible
                        // o simplemente recrear. Para simplificar, recreamos mapeando.
                        // Nota: AppBloqueo requiere iconColor, que no tenemos en BD.
                        // Podríamos intentar mantener los de la lista instalada actual.
                        
                        val pm = packageManager
                        val listaMapeada = appsVinculadas.mapNotNull { vinculada ->
                             val pkg = vinculada.nombrePaquete ?: return@mapNotNull null
                             
                             // Intentar obtener info local para color/nombre real
                             var iconColor = androidx.compose.ui.graphics.Color.Gray
                             var nombreApp = vinculada.nombre
                             
                             try {
                                 val appInfo = pm.getApplicationInfo(pkg, 0)
                                 // nombreApp = pm.getApplicationLabel(appInfo).toString() // Usar nombre de BD o local? BD tiene prioridad si se editó
                                 // Generar color (reutilizando lógica simplificada o random)
                                 iconColor = androidx.compose.ui.graphics.Color(pkg.hashCode())
                             } catch (e: Exception) {
                                 // App no instalada, pero la regla existe.
                             }
                             
                             // Convertir tiempo limite (minutos) a horas/minutos
                             val horas = vinculada.tiempoLimite / 60
                             val minutos = vinculada.tiempoLimite % 60
                             
                             AppBloqueo(
                                 id = pkg.hashCode(),
                                 nombre = nombreApp,
                                 packageName = pkg,
                                 iconColor = iconColor,
                                 isBlocked = vinculada.bloqueada,
                                 limitHours = horas,
                                 limitMinutes = minutos
                             )
                        }
                        
                        // Actualizar repositorio local
                        localRepository.updateBlockedApps(listaMapeada)
                        Log.d(TAG, "Sincronización completada: ${listaMapeada.size} reglas actualizadas")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sincronizando reglas: ${e.message}")
            }
        }
    }

    /**
     * Sube las apps instaladas a la base de datos si no existen
     */
    private suspend fun subirAppsInstaladas() {
        if (dispositivoId == -1) return
        
        try {
            // 1. Obtener apps en BD
            val response = api.obtenerAppsVinculadas("eq.$dispositivoId")
            val appsEnNube = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
            val paquetesEnNube = appsEnNube.mapNotNull { it.nombrePaquete }.toSet()
            
            // 2. Obtener apps instaladas (con launcher)
            val pm = packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val appsInstaladas = pm.queryIntentActivities(mainIntent, 0)
            
            val appsASubir = mutableListOf<com.example.rest.data.models.AppVinculadaInput>()
            
            for (resolveInfo in appsInstaladas) {
                val packageName = resolveInfo.activityInfo.packageName
                
                // Ignorar si ya está en la nube o si es nuestra propia app
                if (paquetesEnNube.contains(packageName) || packageName == this.packageName) {
                    continue
                }
                
                val appName = resolveInfo.loadLabel(pm).toString()
                
                val nuevaApp = com.example.rest.data.models.AppVinculadaInput(
                    idDispositivo = dispositivoId,
                    nombre = appName,
                    nombrePaquete = packageName,
                    tiempoLimite = 0,
                    bloqueada = false,
                    activa = true,
                    fechaCreacion = timestampFormat.format(Date()),
                    fechaActualizacion = timestampFormat.format(Date())
                )
                appsASubir.add(nuevaApp)
            }
            
            // 3. Subir las nuevas (dada la API actual, una por una o en lote si agregamos endpoint)
            // Como create toma un objeto, lo hacemos iterativo. No es lo más eficiente pero funciona para < 100 apps iniciales.
            // Para optimizar, se podría agregar un endpoint batch en el backend.
            if (appsASubir.isNotEmpty()) {
                Log.d(TAG, "Subiendo ${appsASubir.size} nuevas apps a la nube...")
                for (app in appsASubir) {
                   try {
                       api.crearAppVinculada(app)
                   } catch (e: Exception) {
                       Log.e(TAG, "Error subiendo app ${app.nombre}: ${e.message}")
                   }
                }
                Log.d(TAG, "Carga de apps completada")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en subirAppsInstaladas: ${e.message}")
        }
    }

    /**
     * Inicializa el mapa de uso diario consultando UsageStats del sistema
     * para tener el acumulado real desde las 00:00
     */
    /**
     * Inicializa el mapa de uso diario consultando UsageStats del sistema
     * para tener el acumulado real desde las 00:00
     */
    private fun inicializarUsoDiario() {
        try {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            // Usar queryAndAggregateUsageStats para evitar duplicados y tener el total correcto por paquete
            val statsMap = usageStatsManager.queryAndAggregateUsageStats(
                startTime,
                endTime
            )

            if (statsMap != null) {
                dailyUsageMap.clear()
                for ((packageName, usageStat) in statsMap) {
                    if (usageStat.totalTimeInForeground > 0) {
                       val totalTimeInMinutes = (usageStat.totalTimeInForeground / 60000).toInt()
                       if (totalTimeInMinutes > 0) {
                           dailyUsageMap[packageName] = totalTimeInMinutes
                       }
                    }
                }
                Log.d(TAG, "Uso diario inicializado (Agregado): ${dailyUsageMap.size} apps con actividad")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando uso diario: ${e.message}")
        }
    }

    /**
     * Verifica qué app está en primer plano.
     * Estrategia:
     *  1. queryEvents (10s) - detecta cambios de app
     *  2. Si no hay evento nuevo, mantener la última app conocida (currentPackageName)
     *     siempre que siga apareciendo en UsageStats reciente
     *  3. getForegroundByUsageStats como fallback inicial (primer arranque)
     */
    private fun checkForegroundApp() {
        try {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val currentTime = System.currentTimeMillis()

            // Estrategia 1: buscar evento reciente de cambio de app
            val eventApp = getForegroundByEvents(usageStatsManager, currentTime)

            val foregroundPackage: String? = when {
                // Hay un evento reciente - usarlo directamente
                eventApp != null -> eventApp

                // No hay evento, pero tenemos una app conocida - verificar si sigue activa
                currentPackageName != null -> {
                    val pkg = currentPackageName!!
                    if (isAppStillActive(usageStatsManager, pkg, currentTime)) pkg else null
                }

                // Primer arranque sin historial - usar UsageStats
                else -> getForegroundByUsageStats(usageStatsManager, currentTime)
            }

            if (foregroundPackage == null) {
                // La app anterior terminó o no hay nada en primer plano
                if (currentPackageName != null) {
                    Log.d(TAG, "App salió del primer plano: $currentPackageName")
                    currentPackageName = null
                }
                return
            }

            // Ignorar nuestra propia app, launchers y systemui
            if (isSystemOrLauncher(foregroundPackage)) return

            if (foregroundPackage != currentPackageName) {
                // App cambió - registrar sesión anterior
                Log.d(TAG, "App cambió: $currentPackageName -> $foregroundPackage")
                currentPackageName?.let { previousPackage ->
                    val durationSeconds = ((currentTime - sessionStartTime) / 1000).toInt()
                    if (durationSeconds >= 2) {
                        serviceScope.launch { registrarUsoApp(previousPackage, durationSeconds) }
                    }
                }
                currentPackageName = foregroundPackage
                sessionStartTime = currentTime
                updateNotification("Usando: ${getAppName(foregroundPackage)}")
            }

            // Siempre verificar bloqueo
            verificarBloqueo(foregroundPackage, getAppName(foregroundPackage))

        } catch (e: Exception) {
            Log.e(TAG, "Error verificando app en primer plano: ${e.message}")
        }
    }

    /**
     * Verifica si una app sigue en primer plano comprobando que su lastTimeUsed
     * sea reciente (en los últimos 5 minutos) y más reciente que cualquier otra app.
     */
    private fun isAppStillActive(usm: UsageStatsManager, packageName: String, currentTime: Long): Boolean {
        return try {
            val stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                currentTime - 86_400_000,
                currentTime
            ) ?: return false

            val targetStat = stats.find { it.packageName == packageName } ?: return false
            val timeSinceLastUse = currentTime - targetStat.lastTimeUsed

            // La app sigue activa si fue usada hace menos de 5 minutos
            // Y no hay otra app no-sistema con lastTimeUsed más reciente
            if (timeSinceLastUse > 5 * 60 * 1000) return false

            val moreRecentApp = stats
                .filter { it.packageName != packageName && !isSystemOrLauncher(it.packageName) }
                .maxByOrNull { it.lastTimeUsed }

            // Si otra app fue usada más recientemente, la nuestra ya no está en frente
            if (moreRecentApp != null && moreRecentApp.lastTimeUsed > targetStat.lastTimeUsed + 3000) {
                return false
            }
            true
        } catch (e: Exception) { false }
    }

    private fun getForegroundByEvents(usm: UsageStatsManager, currentTime: Long): String? {
        return try {
            val events = usm.queryEvents(currentTime - 10_000, currentTime)
            val event = android.app.usage.UsageEvents.Event()
            var latestPkg: String? = null
            var latestTime: Long = 0

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if ((event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED ||
                            event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) &&
                    event.timeStamp > latestTime) {
                    latestTime = event.timeStamp
                    latestPkg = event.packageName
                }
            }
            latestPkg
        } catch (e: Exception) { null }
    }

    private fun getForegroundByUsageStats(usm: UsageStatsManager, currentTime: Long): String? {
        return try {
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 86_400_000, currentTime)
            stats?.maxByOrNull { it.lastTimeUsed }?.takeIf {
                currentTime - it.lastTimeUsed < 10_000 // Solo si fue usada en los últimos 10s
            }?.packageName
        } catch (e: Exception) { null }
    }

    private fun isSystemOrLauncher(packageName: String): Boolean {
        return packageName == this.packageName ||
               packageName.contains("launcher", ignoreCase = true) ||
               packageName.contains("nexuslauncher", ignoreCase = true) ||
               packageName == "com.android.systemui" ||
               packageName == "com.google.android.packageinstaller"
    }

    /**
     * Registra una sesión completa en la base de datos
     */
    /**
     * Asegura que tenemos un ID de dispositivo válido
     */
    private suspend fun ensureDeviceId(): Int {
        if (dispositivoId != -1) return dispositivoId
        
        val sharedPref = getSharedPreferences("RestCyclePrefs", Context.MODE_PRIVATE)
        val storedId = sharedPref.getInt("ID_DISPOSITIVO", -1)
        if (storedId != -1) {
            dispositivoId = storedId
            return storedId
        }
        
        val userId = sharedPref.getInt("ID_USUARIO", -1)
        if (userId == -1) return -1
        
        try {
            // Intentar obtener de API
            val response = api.obtenerDispositivosPorUsuario("eq.$userId")
            if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                val id = response.body()!![0].id ?: -1
                if (id != -1) {
                    dispositivoId = id
                    with(sharedPref.edit()) {
                        putInt("ID_DISPOSITIVO", id)
                        apply()
                    }
                    return id
                }
            }
            
            // Crear si no existe
            val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
            val nuevoDispositivo = com.example.rest.data.models.DispositivoInput(
                idUsuario = userId,
                nombre = deviceName,
                ip = "0.0.0.0",
                estado = "activo"
            )
             
            val createResponse = api.crearDispositivo(nuevoDispositivo)
            if (createResponse.isSuccessful && createResponse.body() != null && createResponse.body()!!.isNotEmpty()) {
                val id = createResponse.body()!![0].id ?: -1
                if (id != -1) {
                    dispositivoId = id
                    with(sharedPref.edit()) {
                        putInt("ID_DISPOSITIVO", id)
                        apply()
                    }
                    return id
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error ensuring device ID: ${e.message}")
        }
        return -1
    }

    /**
     * Registra una sesión completa en la base de datos
     */
    /**
     * Registra una sesión completa en la base de datos
     */
    /**
     * Registra el uso en el mapa local y actualiza HistorialApps
     */
    private suspend fun registrarUsoApp(packageName: String, duracion: Int) {
        if (duracion < 1) return 
        
        // 1. Actualizar mapa local (para bloqueo inmediato)
        val currentUsage = dailyUsageMap[packageName] ?: 0
        dailyUsageMap[packageName] = currentUsage + (duracion / 60)
        
        // 2. Actualizar o crear registro en historial_apps (BD)
        try {
            val validDeviceId = ensureDeviceId()
            if (validDeviceId == -1) return

            val fechaHoy = dateFormat.format(Date())
            
            // Buscar si ya existe registro para esta app hoy
            val response = api.obtenerHistorialApp(
                idDispositivo = "eq.$validDeviceId",
                nombrePaquete = "eq.$packageName",
                fecha = "eq.$fechaHoy"
            )
            
            val appName = getAppName(packageName)
            val minutos = duracion / 60
            
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                // Actualizar existente
                val historialExistente = response.body()!![0]
                val nuevoTiempo = historialExistente.tiempoUso + minutos
                val nuevasAperturas = historialExistente.numeroAperturas + 1
                
                val updateData = mapOf<String, Any>(
                    "tiempo_uso" to nuevoTiempo,
                    "numero_aperturas" to nuevasAperturas,
                    "nombre" to appName,
                    "fecha_registro" to timestampFormat.format(Date())
                )
                
                api.actualizarHistorialApp(
                    id = "eq.${historialExistente.id}",
                    update = updateData
                )
            } else {
                // Crear nuevo
                val nuevoHistorial = com.example.rest.data.models.HistorialAppInput(
                    idDispositivo = validDeviceId,
                    nombre = appName,
                    nombrePaquete = packageName,
                    tiempoUso = minutos,
                    numeroAperturas = 1,
                    fecha = fechaHoy,
                    fechaRegistro = timestampFormat.format(Date())
                )
                
                api.crearHistorialApp(nuevoHistorial)
            }
            Log.d(TAG, "Historial actualizado para $packageName (+${minutos}min)")
            
            // 3. Actualizar tiempo_usado_hoy en apps_vinculadas (si la app está vinculada)
            if (restrictedApps.any { it.packageName == packageName }) {
                try {
                    // Leer uso real del sistema (no el mapa en memoria que puede estar desactualizado)
                    val totalUsageMin = getRealDailyUsageMinutes(packageName)
                    val updates = mapOf(
                        "tiempo_usado_hoy" to totalUsageMin,
                        "fecha_actualizacion" to timestampFormat.format(Date())
                    )
                    api.actualizarAppVinculadaPorPaquete(
                        idDispositivo = "eq.$validDeviceId",
                        nombrePaquete = "eq.$packageName",
                        app = updates
                    )
                    Log.d(TAG, "tiempo_usado_hoy actualizado en BD: $packageName = ${totalUsageMin}min")
                } catch (e: Exception) {
                    Log.e(TAG, "Error actualizando apps_vinculadas (tiempo): ${e.message}")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando historial: ${e.message}")
        }
    }
    
    // Métodos iniciarSesion y finalizarSesion eliminados al simplificar la lógica

    /**
     * Obtiene el nombre de la app desde el package name
     */
    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName.split(".").lastOrNull() ?: packageName
        }
    }

    /**
     * Crea el canal de notificación
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Monitoreo de Apps",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificación para el servicio de monitoreo de apps"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Crea la notificación foreground
     */
    private fun createNotification(contentText: String = "Registrando sesiones de apps"): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RestCycle - Monitoreando uso")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    
    /**
     * Actualiza la lista de apps restringidas y el uso diario
     */
    /**
     * Actualiza la lista de apps restringidas desde repositorio local
     */
    private fun actualizarListaRestricciones() {
        val now = System.currentTimeMillis()
        // Actualizar cada n segundos si es necesario, pero local es rápido
        if (now - lastRestrictionCheckTime < 5000) return
        lastRestrictionCheckTime = now
        
        restrictedApps = localRepository.getBlockedApps()
        // Log.d(TAG, "Apps restringidas actualizadas: ${restrictedApps.size}")
    }
    
    /**
     * Verifica si la app actual debe ser bloqueada
     */
    /**
     * Verifica si la app actual debe ser bloqueada
     */
    private fun verificarBloqueo(packageName: String, appName: String) {
        // Buscar si la app está en la lista de restringidas
        val appRestricted = restrictedApps.find { it.packageName == packageName }
        if (appRestricted == null) {
            Log.d(TAG, "verificarBloqueo: $packageName no está en lista de restricciones (${restrictedApps.size} apps)")
            return
        }

        var reason = ""
        var shouldBlock = false

        if (appRestricted.isBlocked) {
            shouldBlock = true
            reason = "Esta aplicación ha sido bloqueada manualmente."
            Log.d(TAG, "verificarBloqueo: $packageName BLOQUEADA manualmente")
        } else {
            val limitMinutes = (appRestricted.limitHours * 60) + appRestricted.limitMinutes
            
            if (limitMinutes > 0) {
                // Consultar uso real del sistema (más preciso que dailyUsageMap)
                val realUsageMin = getRealDailyUsageMinutes(packageName)
                
                Log.d(TAG, "verificarBloqueo: $packageName uso=$realUsageMin min / límite=$limitMinutes min")
                
                if (realUsageMin >= limitMinutes) {
                    shouldBlock = true
                    reason = "Has excedido el límite de tiempo diario ($limitMinutes min)."
                    
                    // Si no estaba marcada como bloqueada, actualizar en la nube y localmente
                    if (!appRestricted.isBlocked) {
                        Log.d(TAG, "Límite excedido para $packageName. Bloqueando en nube...")
                        updateAppBlockedStatusInCloud(packageName, true)
                        val updatedApp = appRestricted.copy(isBlocked = true)
                        localRepository.saveBlockedApp(updatedApp)
                        restrictedApps = restrictedApps.map { 
                            if (it.packageName == packageName) updatedApp else it 
                        }
                    }
                }
            } else {
                Log.d(TAG, "verificarBloqueo: $packageName sin límite configurado, omitiendo")
            }
        }

        if (shouldBlock) {
             Log.d(TAG, "BLOQUEANDO app: $appName")
             val intent = BloqueoActivity.newIntent(this, appName, reason)
             intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
             startActivity(intent)
        }
    }

    /**
     * Obtiene los minutos reales de uso del dia de hoy usando UsageStats
     */
    private fun getRealDailyUsageMinutes(packageName: String): Int {
        return try {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val statsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
                val ms = statsMap[packageName]?.totalTimeInForeground ?: 0L
                (ms / 60000).toInt()
            } else {
                val statsList = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, startTime, endTime
                )
                val ms = statsList?.filter { it.packageName == packageName }
                    ?.sumOf { it.totalTimeInForeground } ?: 0L
                (ms / 60000).toInt()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error leyendo UsageStats para $packageName: ${e.message}")
            // Fallback al mapa en memoria
            dailyUsageMap[packageName] ?: 0
        }
    }

    private fun updateAppBlockedStatusInCloud(packageName: String, isBlocked: Boolean) {
        if (dispositivoId == -1) return
        
        serviceScope.launch {
            try {
                // Obtener uso real en tiempo real del sistema
                val tiempoUsadoHoy = getRealDailyUsageMinutes(packageName)
                
                val updates = mapOf(
                    "bloqueada" to isBlocked,
                    "activa" to !isBlocked,
                    "tiempo_usado_hoy" to tiempoUsadoHoy,
                    "fecha_actualizacion" to timestampFormat.format(Date())
                )
                api.actualizarAppVinculadaPorPaquete(
                    idDispositivo = "eq.$dispositivoId",
                    nombrePaquete = "eq.$packageName",
                    app = updates
                )
                Log.d(TAG, "BD actualizada para $packageName: bloqueada=$isBlocked, activa=${!isBlocked}, tiempo_usado_hoy=${tiempoUsadoHoy}min")
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando estado de bloqueo en nube: ${e.message}")
            }
        }
    }

    /**
     * Actualiza la notificación
     */
    private fun updateNotification(contentText: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification(contentText))
    }
}
