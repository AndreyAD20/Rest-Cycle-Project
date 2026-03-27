package com.example.rest.services

import android.app.*
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkRequest
import com.example.rest.R
import com.example.rest.data.models.HistorialApp
import com.example.rest.data.models.UbicacionInput
import com.example.rest.data.models.AppInstaladaInput
import com.google.android.gms.location.*

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
        private const val CHANNEL_ID = "app_monitoring_channel_silent"
        private const val ALERT_CHANNEL_ID = "app_monitoring_alerts_channel"
        private const val CHECK_INTERVAL = 3000L // 3 segundos
        private const val SYNC_INTERVAL = 300000L // 5 minutos
        
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
    // Cooldown: evita relanzar BloqueoActivity para la misma app dentro de 60 segundos
    private val lastBlockedTimeMap: MutableMap<String, Long> = mutableMapOf()
    private val BLOCK_COOLDOWN_MS = 3_000L // 3 segundos - igual al intervalo de check
    // Paquete que actualmente tiene el BloqueoActivity activo
    private var currentlyBlockedPackage: String? = null

    // Rastreo de historial cada 15 min
    private var lastHistorySyncTime: Long = 0
    private val HISTORY_SYNC_INTERVAL = 15 * 60 * 1000L // 15 minutos

    // Rastreo de comandos cada 30 seg
    private var lastCommandCheckTime: Long = 0

// Actualización periódica del tiempo de uso en BD
private var lastUsageTimeUpdate: Long = 0
private val USAGE_TIME_UPDATE_INTERVAL = 300000L // 5 minutos

// Mapa para evitar duplicar avisos de "10 minutos restantes", valor es el timestamp del último aviso
private val warningNotifiedMap: MutableMap<String, Long> = mutableMapOf()

    // ===== AUDÍFONOS (cable + Bluetooth) =====
    /** Timestamp en ms de cuando se conectaron audífonos (-1 = no conectados) */
    private var headphoneConnectedSince: Long = -1L
    /** Evita spam: true una vez disparada la notificación en la sesión actual */
    private var headphoneWarningFired: Boolean = false
    /** Tiempo mínimo de uso continuo de audífonos antes de notificar (60 min) */
    private val HEADPHONE_ALERT_MS = 60 * 60 * 1000L

    private val headphoneReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_HEADSET_PLUG -> {
                    val state = intent.getIntExtra("state", -1)
                    if (state == 1) onHeadphonesConnected() else if (state == 0) onHeadphonesDisconnected()
                }
                BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED -> {
                    val newState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1)
                    if (newState == BluetoothProfile.STATE_CONNECTED) onHeadphonesConnected()
                    else if (newState == BluetoothProfile.STATE_DISCONNECTED) onHeadphonesDisconnected()
                }
            }
        }
    }

    // Rastreo de ubicación
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private val LOCATION_UPDATE_INTERVAL = 120_000L // 2 minutos
    private val LOCATION_FASTEST_INTERVAL = 60_000L // 1 minuto

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Servicio creado")
        
        // Obtener ID del usuario
        val prefs = com.example.rest.utils.PreferencesManager(this)
        val userId = prefs.getUserId()
        
        if (userId == -1) {
            Log.e(TAG, "No hay usuario logueado, deteniendo servicio")
            stopSelf()
            return
        }
        
        // Intentar obtener ID de dispositivo de SharedPreferences
        val sharedPref = getSharedPreferences("RestCyclePrefs", Context.MODE_PRIVATE)
        dispositivoId = sharedPref.getInt("ID_DISPOSITIVO", -1)
        
        // Siempre intentar obtener/actualizar el dispositivo correcto desde Supabase
        serviceScope.launch {
            try {
                val response = api.obtenerDispositivosPorUsuario("eq.$userId")
                if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                    val dispositivos = response.body() ?: emptyList()
                    
                    // SELECCIONAR EL DISPOSITIVO MÁS APROPIADO
                    val dispositivoAUsar = dispositivos
                        // Primero buscar uno marcado como activo
                        .firstOrNull { it.estado == "activo" }
                        // Si no hay activo, tomar el más recientemente actualizado/creado (mayor ID)
                        ?: dispositivos.maxByOrNull { it.id ?: Int.MIN_VALUE } 
                        
if (dispositivoAUsar != null) {
    dispositivoId = dispositivoAUsar.id ?: -1
                         // Asegurar que esté marcado como activo
                         if (dispositivoAUsar.estado != "activo") {
                             val dispositivoActualizado = dispositivoAUsar.copy(estado = "activo")
                             api.actualizarDispositivo("eq.${dispositivoAUsar.id}", dispositivoActualizado)
                         }
                        // Actualizar el ID local para próximas veces
                        with(sharedPref.edit()) {
                            putInt("ID_DISPOSITIVO", dispositivoId)
                            apply()
                        }
                        Log.d(TAG, "ID de dispositivo obtenido/actualizado: $dispositivoId (estado: activo)")
                    } else {
                        // No hay dispositivos en absoluto, crear uno nuevo
                        val dispositivoNombre = "Android Device"  // O obtener nombre real
                        val nuevoDispositivo = com.example.rest.data.models.DispositivoInput(
                            idUsuario = userId,
                            nombre = dispositivoNombre,
                            ip = "",  // IP vacía (ya corregida anteriormente)
                            estado = "activo"
                        )
                        
                        val crearRespuesta = api.crearDispositivo(nuevoDispositivo)
                        if (crearRespuesta.isSuccessful && !crearRespuesta.body().isNullOrEmpty()) {
                            dispositivoId = crearRespuesta.body()!![0].id ?: -1
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
                } else {
                    // Error en consulta o sin dispositivos, crear uno nuevo
                    val dispositivoNombre = "Android Device"
                    val nuevoDispositivo = com.example.rest.data.models.DispositivoInput(
                        idUsuario = userId,
                        nombre = dispositivoNombre,
                        ip = "",
                        estado = "activo"
                    )
                    
                    val crearRespuesta = api.crearDispositivo(nuevoDispositivo)
                    if (crearRespuesta.isSuccessful && !crearRespuesta.body().isNullOrEmpty()) {
                        dispositivoId = crearRespuesta.body()!![0].id ?: -1
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
        
        createNotificationChannel()

        // Registrar receiver de audífonos (cable + Bluetooth)
        val headphoneFilter = IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
        }
        registerReceiver(headphoneReceiver, headphoneFilter)

        // Verificar si ya hay audífonos conectados al arrancar
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioManager.isWiredHeadsetOn || audioManager.isBluetoothA2dpOn) {
            onHeadphonesConnected()
        }
        
        val hasLocationPermission = android.content.pm.PackageManager.PERMISSION_GRANTED ==
            androidx.core.content.ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) || android.content.pm.PackageManager.PERMISSION_GRANTED ==
            androidx.core.content.ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            )

        if (Build.VERSION.SDK_INT >= 34) { // Android 14+
            val foregroundType = if (hasLocationPermission) {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC or
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            } else {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            }
            startForeground(NOTIFICATION_ID, createNotification(), foregroundType)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }

        // Iniciar rastreo de ubicación de alta frecuencia (solo si hay permiso)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (hasLocationPermission) {
            startLocationUpdates()
        } else {
            Log.w(TAG, "Permisos de ubicación no concedidos - rastreo desactivado en este inicio")
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
        stopLocationUpdates()
        try { unregisterReceiver(headphoneReceiver) } catch (e: Exception) { }
        
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
        
        // Verificar comandos pendientes al iniciar
        serviceScope.launch {
            procesarComandosPendientes()
        }
        
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

                    // Verificar uso de audífonos cada tick
                    checkHeadphoneUsage()
                    
                    // Verificar si toca enviar al historial (cada 15 min)
                    if (now - lastHistorySyncTime > HISTORY_SYNC_INTERVAL) {
                        sincronizarUbicacionHistorial()
                        lastHistorySyncTime = now
                    }
                    
                    // Actualizar tiempo de uso en BD cada 30 segundos
                    if (now - lastUsageTimeUpdate > USAGE_TIME_UPDATE_INTERVAL) {
                        actualizarTiempoUsadoHoyEnBD()
                        lastUsageTimeUpdate = now
                    }
                    
                    // Verificar comandos cada 30 segundos
                    if (now - lastCommandCheckTime > 30_000) {
                        serviceScope.launch {
                            procesarComandosPendientes()
                        }
                        lastCommandCheckTime = now
                    }

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
                // Subir inventario completo de apps instaladas para que el padre pueda verlas
                subirAppsInstaladas()
                
                  val response = api.obtenerAppsBloqueo("eq.$dispositivoId")
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
                                 // Preservar bloqueo local si ya estaba activo
                                 // (evita que la sincronización revierte bloqueos por límite de tiempo)
                                 isBlocked = vinculada.bloqueada || (localRepository.getBlockedApps()
                                     .find { it.packageName == pkg }?.isBlocked == true),
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
     * Sube las apps instaladas a apps_instaladas en Supabase
     * Sincroniza TODAS las apps (no solo las nuevas) para mantener la lista actualizada
     */
    private suspend fun subirAppsInstaladas() {
        if (dispositivoId == -1) return
        
        try {
            // 1. Obtener apps que ya existen en apps_instaladas
            val response = api.obtenerAppsInstaladas("eq.$dispositivoId")
            val appsEnNube = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
            val paquetesEnNube = appsEnNube.mapNotNull { it.nombrePaquete }.toSet()
            
            // 2. Obtener TODAS las apps instaladas (con launcher)
            val pm = packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val appsInstaladas = pm.queryIntentActivities(mainIntent, 0)
            
            val appsASincronizar = mutableListOf<AppInstaladaInput>()
            
            for (resolveInfo in appsInstaladas) {
                val packageName = resolveInfo.activityInfo.packageName
                
                // Ignorar si es nuestra propia app
                if (packageName == this.packageName) {
                    continue
                }
                
                val appName = resolveInfo.loadLabel(pm).toString()
                
                // Si NO existe en la nube, preparar para subir
                if (!paquetesEnNube.contains(packageName)) {
                    val nuevaApp = AppInstaladaInput(
                        idDispositivo = dispositivoId,
                        nombre = appName,
                        nombrePaquete = packageName,
                        enlazada = false  // Por defecto NO enlazada
                    )
                    appsASincronizar.add(nuevaApp)
                }
            }
            
            // 3. Subir las apps nuevas usando upsert
            if (appsASincronizar.isNotEmpty()) {
                Log.d(TAG, "Sincronizando ${appsASincronizar.size} nuevas apps a la nube...")
                try {
                    val upsertResponse = api.upsertAppsInstaladas(appsASincronizar)
                    if (upsertResponse.isSuccessful) {
                        Log.d(TAG, "Sincronización de apps completada")
                    } else {
                        Log.e(TAG, "Error en upsert: ${upsertResponse.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sincronizando apps: ${e.message}")
                }
            } else {
                Log.d(TAG, "No hay nuevas apps para sincronizar")
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

            // Usar cálculo exacto por eventos para evitar el bug de buckets UTC a las 7PM/8PM
            val statsMap = com.example.rest.utils.UsageStatsHelper.getExactDailyUsageMap(
                usageStatsManager,
                startTime,
                endTime
            )

            dailyUsageMap.clear()
            for ((packageName, usageStat) in statsMap) {
                if (usageStat > 0) {
                    val totalTimeInMinutes = (usageStat / 60000).toInt()
                    if (totalTimeInMinutes > 0) {
                        dailyUsageMap[packageName] = totalTimeInMinutes
                    }
                }
            }
            Log.d(TAG, "Uso diario inicializado (Exacto): ${dailyUsageMap.size} apps con actividad")
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
            // Pero si es nuestra app, limpiar cooldowns (el usuario reconoció el bloqueo)
            if (isSystemOrLauncher(foregroundPackage)) {
                if (foregroundPackage == this.packageName) {
                    lastBlockedTimeMap.clear()
                }
                return
            }

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
                // Solo excluir launchers y systemui — NO nuestra propia app
                // Así cuando BloqueoActivity está en pantalla, com.example.rest aparece
                // como más reciente y la app bloqueada deja de ser detectada como activa
                .filter { it.packageName != packageName &&
                        it.packageName != this.packageName &&
                        !it.packageName.contains("launcher", ignoreCase = true) &&
                        !it.packageName.contains("nexuslauncher", ignoreCase = true) &&
                        it.packageName != "com.android.systemui" &&
                        it.packageName != "com.google.android.packageinstaller"
                }
                .maxByOrNull { it.lastTimeUsed }

            // Si otra app fue usada más recientemente, la nuestra ya no está en frente
            if (moreRecentApp != null && moreRecentApp.lastTimeUsed > targetStat.lastTimeUsed + 90000) {
                return false
            }
            true
        } catch (e: Exception) { false }
    }

    private fun getForegroundByEvents(usm: UsageStatsManager, currentTime: Long): String? {
        return try {
            // Aumentar ventana a 30s para no perder eventos en Android 13 (que puede retrasarlos)
            val events = usm.queryEvents(currentTime - 30_000, currentTime)
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
                currentTime - it.lastTimeUsed < 30_000 // Aumentado a 30s para Android 13
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
     * Asegura que tenemos un ID de dispositivo válido, actualizando desde Supabase si es necesario
     */
    private suspend fun ensureDeviceId(): Int {
        // Si ya tenemos un ID válido, verificamos que siga siendo correcto
        if (dispositivoId != -1) {
            return try {
                val response = api.obtenerDispositivoPorId("$dispositivoId")
                if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                    val dispositivo = response.body()!![0]
                     // Asegurar que esté marcado como activo
                     if (dispositivo.estado != "activo") {
                         val dispositivoActualizado = dispositivo.copy(estado = "activo")
                         api.actualizarDispositivo("eq.$dispositivoId", dispositivoActualizado)
                     }
                    dispositivoId
                } else {
                    -1  // Dispositivo ya no existe
                }
            } catch (e: Exception) {
                -1  // Error en consulta
            }
        }
        
        // No tenemos ID válido, intentar obtenerlo de Supabase
        val prefs = com.example.rest.utils.PreferencesManager(this)
        val userId = prefs.getUserId()
        if (userId == -1) return -1
        
        return try {
            val response = api.obtenerDispositivosPorUsuario("eq.$userId")
            if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                val dispositivos = response.body() ?: emptyList()
                
                // SELECCIONAR EL DISPOSITIVO MÁS APROPIADO
                val dispositivoAUsar = dispositivos
                    // Primero buscar uno marcado como activo
                    .firstOrNull { it.estado == "activo" }
                    // Si no hay activo, tomar el más recientemente actualizado/creado (mayor ID)
                    ?: dispositivos.maxByOrNull { it.id ?: Int.MIN_VALUE }
                    
                if (dispositivoAUsar != null) {
                    // Actualizar ID local y asegurar estado activo
                    dispositivoId = dispositivoAUsar.id ?: -1
                     if (dispositivoAUsar.estado != "activo") {
                         val dispositivoActualizado = dispositivoAUsar.copy(estado = "activo")
                         api.actualizarDispositivo("eq.$dispositivoId", dispositivoActualizado)
                     }
                    // Guardar en SharedPreferences
                    val sharedPref = getSharedPreferences("RestCyclePrefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putInt("ID_DISPOSITIVO", dispositivoId)
                        apply()
                    }
                    dispositivoId
                } else {
                    -1  // No hay dispositivos
                }
            } else {
                -1  // Error en consulta
            }
        } catch (e: Exception) {
            -1
        }
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

    // ===== AUDÍFONOS =====

    private fun onHeadphonesConnected() {
        if (headphoneConnectedSince == -1L) {
            headphoneConnectedSince = System.currentTimeMillis()
            headphoneWarningFired = false
            Log.d(TAG, "Audífonos conectados — iniciando contador")
        }
    }

    private fun onHeadphonesDisconnected() {
        headphoneConnectedSince = -1L
        headphoneWarningFired = false
        Log.d(TAG, "Audífonos desconectados — contador reiniciado")
    }

    /**
     * Llamado en cada tick del loop; si se alcanza 60 min continuos con audífonos,
     * dispara la notificación (solo una vez por sesión conectada).
     */
    private fun checkHeadphoneUsage() {
        if (headphoneConnectedSince == -1L || headphoneWarningFired) return
        val elapsed = System.currentTimeMillis() - headphoneConnectedSince
        if (elapsed >= HEADPHONE_ALERT_MS) {
            headphoneWarningFired = true
            enviarNotificacionAudifonos()
        }
    }

    /**
     * Envía la notificación de descanso de oídos.
     */
    private fun enviarNotificacionAudifonos() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val intent = Intent(this, com.example.rest.features.home.InicioComposeActivity::class.java)
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle(getString(R.string.notif_headphone_title))
            .setContentText(getString(R.string.notif_headphone_body))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify("HEADPHONE_REST".hashCode(), notification)
        Log.d(TAG, "Notificación de audífonos enviada (60 min alcanzados)")
    }

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
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Notificación silenciosa para el servicio de monitoreo de apps"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            
            // Canal para Alertas
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Alertas de Tiempo de Pantalla",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones cuando una aplicación está por alcanzar su límite de tiempo"
            }
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    /**
     * Crea la notificación foreground
     */
    private fun createNotification(contentText: String = "Registrando sesiones de apps"): Notification {
        val intent = Intent(this, com.example.rest.features.home.InicioComposeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RestCycle - Monitoreando uso")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setOngoing(true)
            .setContentIntent(pendingIntent)

        // Integración de API de Burbujas
        val bubbleData = com.example.rest.utils.BubbleHelper.createBubbleMetadata(this, pendingIntent)
        if (bubbleData != null) {
            builder.setBubbleMetadata(bubbleData)
            val person = com.example.rest.utils.BubbleHelper.createBotPerson()
            builder.addPerson(person)
            builder.setStyle(NotificationCompat.MessagingStyle(person)
                .addMessage(contentText, System.currentTimeMillis(), person))
        }

        return builder.build()
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
                
                val tiempoRestante = limitMinutes - realUsageMin
                if (tiempoRestante in 1..10) {
                    val ahora = System.currentTimeMillis()
                    val ultimoAviso = warningNotifiedMap[packageName] ?: 0L
                    
                    // Si ha pasado más de 12 horas (43,200,000 ms) desde el último aviso
                    // Asumimos que es un día nuevo o se reseteó el uso
                    if (ahora - ultimoAviso > 12 * 60 * 60 * 1000L) {
                        warningNotifiedMap[packageName] = ahora
                        enviarNotificacionPreBloqueo(appName, tiempoRestante)
                    }
                }
                
                if (realUsageMin >= limitMinutes) {
                    shouldBlock = true
                    reason = "Has excedido el límite de tiempo diario ($limitMinutes min)."

                    // Enviar notificación de límite excedido
                    try {
                        val prefsNotif = com.example.rest.utils.PreferencesManager(this)
                        com.example.rest.NotificationHelper.notifyBoth(
                            context = this,
                            title = "Límite excedido",
                            message = "Has usado $appName por $realUsageMin min (límite: $limitMinutes min)",
                            tipo = com.example.rest.NotificationHelper.NotifType.USO_EXCESIVO,
                            idHijo = prefsNotif.getUserId()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error enviando notificación de uso excesivo: ${e.message}")
                    }
                    
                    // Si no estaba marcada como bloqueada, actualizar en la nube y localmente
                    if (!appRestricted.isBlocked) {
                        Log.d(TAG, "Límite excedido para $packageName. Bloqueando en nube...")
                        updateAppBlockedStatusInCloud(packageName, true)
                        // Actualizar tiempo de uso en BD al bloquear por límite de tiempo
                        serviceScope.launch {
                            try {
                                val currentUsageMin = getRealDailyUsageMinutes(packageName)
                                val usageUpdates = mapOf(
                                    "tiempo_usado_hoy" to currentUsageMin,
                                    "fecha_actualizacion" to timestampFormat.format(Date())
                                )
                                api.actualizarAppVinculadaPorPaquete(
                                    idDispositivo = "eq.$dispositivoId",
                                    nombrePaquete = "eq.$packageName",
                                    app = usageUpdates
                                )
                                Log.d(TAG, "tiempo_usado_hoy actualizado en BD al bloquear: $packageName = $currentUsageMin min")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error actualizando tiempo_usado_hoy en BD al bloquear: ${e.message}")
                            }
                        }
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
            val now = System.currentTimeMillis()
            val lastBlocked = lastBlockedTimeMap[packageName] ?: 0L

            // Si ya tenemos el bloqueo activo para ESTA app, no relanzar
            if (currentlyBlockedPackage == packageName && now - lastBlocked < BLOCK_COOLDOWN_MS) {
                Log.d(TAG, "Bloqueo ya activo para $packageName, no se relanza")
                return
            }

            // Si hay otra app bloqueada diferente, limpiar registro anterior
            if (currentlyBlockedPackage != null && currentlyBlockedPackage != packageName) {
                lastBlockedTimeMap.remove(currentlyBlockedPackage!!)
            }

            lastBlockedTimeMap[packageName] = now
            currentlyBlockedPackage = packageName
            Log.d(TAG, "BLOQUEANDO app en tiempo real: $appName")

            // Enviar notificación de bloqueo
            try {
                val prefsNotif = com.example.rest.utils.PreferencesManager(this)
                com.example.rest.NotificationHelper.notifyBoth(
                    context = this,
                    title = "App Bloqueada",
                    message = "$appName ha sido bloqueada. $reason",
                    tipo = com.example.rest.NotificationHelper.NotifType.BLOQUEO,
                    idHijo = prefsNotif.getUserId()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error enviando notificación de bloqueo: ${e.message}")
            }

            currentPackageName = null
            val intent = BloqueoActivity.newIntent(this, appName, reason)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            // Si la app ya no necesita ser bloqueada (ej. reset de día), limpiar su estado
            if (currentlyBlockedPackage == packageName) {
                currentlyBlockedPackage = null
            }
        }
    }

    /**
     * Actualiza el tiempo de uso diario en la tabla apps_bloqueo para todas las apps restringidas
     */
    private fun actualizarTiempoUsadoHoyEnBD() {
        if (dispositivoId == -1 || restrictedApps.isEmpty()) return

        serviceScope.launch {
            try {
                val validDeviceId = ensureDeviceId()
                if (validDeviceId == -1) return@launch

                for (app in restrictedApps) {
                    val packageName = app.packageName
                    // Obtener el tiempo real de uso del día (incluyendo sesión actual)
                    val tiempoUsadoMin = getRealDailyUsageMinutes(packageName)
                    
                    // Actualizar en la tabla apps_bloqueo
                    val updates = mapOf(
                        "tiempo_usado_hoy" to tiempoUsadoMin,
                        "fecha_actualizacion" to timestampFormat.format(Date())
                    )
                    api.actualizarAppVinculadaPorPaquete(
                        idDispositivo = "eq.$validDeviceId",
                        nombrePaquete = "eq.$packageName",
                        app = updates
                    )
                }
                Log.d(TAG, "Actualizado tiempo_usado_hoy en BD para ${restrictedApps.size} apps")
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando tiempo_usado_hoy en BD: ${e.message}")
            }
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

            val statsMap = com.example.rest.utils.UsageStatsHelper.getExactDailyUsageMap(usageStatsManager, startTime, endTime)
            var baseMs = statsMap[packageName] ?: 0L
            
            // Añadir el tiempo de la sesión actual en tiempo real
            // UsageStats no se actualiza hasta que la app se cierra o el sistema hace un flush
            if (packageName == currentPackageName && sessionStartTime > 0) {
                val currentSessionMs = System.currentTimeMillis() - sessionStartTime
                // Comparamos el baseMs del sistema vs nuestro mapa local + sesión actual
                // Tomamos el mayor para asegurar que bloquea exacto en el tiempo real
                val localMapMs = (dailyUsageMap[packageName]?.toLong() ?: 0L) * 60000L
                val calculatedMs = localMapMs + currentSessionMs
                if (calculatedMs > baseMs) {
                    baseMs = calculatedMs
                }
            }
            
            val minutos = (baseMs / 60000).toInt()
            if (minutos == 0 && baseMs > 5000) 1 else minutos
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
     * Envía la notificación de aviso de que se acaba el tiempo (10 minutos o menos)
     */
    private fun enviarNotificacionPreBloqueo(appName: String, minutosRestantes: Int) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        
        val intent = Intent(this, com.example.rest.features.home.InicioComposeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = getString(R.string.notif_time_warning_title)
        val body = getString(R.string.notif_time_warning_body, minutosRestantes, appName)

        val builder = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Usamos un ID único por app basado en su hashcode
        val notifyId = "PREBLOCK_${appName}".hashCode()
        notificationManager.notify(notifyId, builder.build())
        Log.d(TAG, "Notificación de pre-bloqueo enviada para $appName ($minutosRestantes min restantes)")
    }

    /**
     * Actualiza la notificación
     */
    private fun updateNotification(contentText: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification(contentText))
    }

    /**
     * Inicia las actualizaciones de ubicación
     */
    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, LOCATION_UPDATE_INTERVAL)
            .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                
                serviceScope.launch {
                    val userId = com.example.rest.utils.PreferencesManager(this@AppMonitorService).getUserId()
                    if (userId != -1) {
                        try {
                            api.guardarUbicacion(
                                UbicacionInput(
                                    idUsuario = userId,
                                    latitud = location.latitude,
                                    longitud = location.longitude,
                                    timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.format(java.util.Date())
                                )
                            )
                            Log.d(TAG, "Ubicación en tiempo real enviada desde el servicio: ${location.latitude}, ${location.longitude}")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error enviando ubicación desde servicio: ${e.message}")
                        }
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            Log.i(TAG, "Actualizaciones de ubicación iniciadas (cada 2 min)")
        } catch (e: Exception) {
            Log.e(TAG, "No se pudieron iniciar actualizaciones de ubicación: ${e.message}")
        }
    }

    /**
     * Procesa comandos de control parental recibidos del padre
     */
    private suspend fun procesarComandosPendientes() {
        val userId = com.example.rest.utils.PreferencesManager(this).getUserId()
        if (userId == -1) return

        try {
            val response = api.obtenerComandosPendientes("eq.$userId", "eq.PENDIENTE")
            if (response.isSuccessful) {
                @Suppress("UNCHECKED_CAST")
                val comandos = response.body() as? List<Map<String, Any>>
                comandos?.forEach { comando ->
                    val id = comando["id"]
                    val tipo = comando["tipo_comando"] as? String
                    
                    when (tipo) {
                        "SOLICITAR_UBICACION" -> {
                            Log.i(TAG, "Recibido comando: SOLICITAR_UBICACION")
                            // Ejecutar worker de ubicación inmediatamente
                            val workRequest = androidx.work.OneTimeWorkRequestBuilder<UbicacionWorker>().build()
                            androidx.work.WorkManager.getInstance(this).enqueue(workRequest)
                            
                            // Marcar comando como procesado
                            if (id != null) {
                                api.actualizarComando(
                                    id = "eq.$id",
                                    updates = mapOf("estado" to "PROCESADO")
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error procesando comandos: ${e.message}")
        }
    }

    /**
     * Envía la ubicación actual al historial (cada 15 min)
     */
    @Suppress("MissingPermission")
    private fun sincronizarUbicacionHistorial() {
        val userId = com.example.rest.utils.PreferencesManager(this).getUserId()
        if (userId == -1) return

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    serviceScope.launch {
                        try {
                            val sdfFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val sdfHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            val ahora = Date()
                            
                            api.guardarHistorialUbicacion(
                                com.example.rest.data.models.HistorialUbicacionInput(
                                    idUsuario = userId,
                                    latitud = location.latitude,
                                    longitud = location.longitude,
                                    fecha = sdfFecha.format(ahora),
                                    hora = sdfHora.format(ahora)
                                )
                            )
                            Log.i(TAG, "Ubicación histórica enviada desde servicio (15 min)")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error enviando historial desde servicio: ${e.message}")
                        }
                    }
                }
            }
    }

    /**
     * Detiene las actualizaciones de ubicación
     */
    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            Log.d(TAG, "Actualizaciones de ubicación detenidas")
        }
    }
}
