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
import com.example.rest.data.models.SesionApp
import com.example.rest.network.SupabaseClient
import com.example.rest.features.blocking.BloqueoActivity
import com.example.rest.data.models.AppVinculada
import com.example.rest.data.repository.LocalBlockingRepository
import com.example.rest.features.tools.AppBloqueo
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

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
    private val timestampFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    
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
                        val nuevoDispositivo = com.example.rest.data.models.Dispositivo(
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
        startForeground(NOTIFICATION_ID, createNotification())
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
                // y queremos asegurar que se intente enviar la petición
                try {
                    runBlocking {
                        withTimeout(2000) { // Timeout de 2 segundos para no bloquear la UI
                             registrarSesionCompleta(packageName, sessionStartTime, now, durationSeconds)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "No se pudo guardar la última sesión al destruir: ${e.message}")
                }
            }
        }
        
        serviceScope.cancel()
    }

    /**
     * Inicia el monitoreo periódico
     */
    private fun startMonitoring() {
        handler.post(object : Runnable {
            override fun run() {
                if (isMonitoring) {
                    actualizarListaRestricciones()
                    checkForegroundApp()
                    handler.postDelayed(this, CHECK_INTERVAL)
                }
            }
        })
    }

    /**
     * Verifica qué app está en primer plano
     */
    private fun checkForegroundApp() {
        try {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val currentTime = System.currentTimeMillis()
            
            // Obtener estadísticas de los últimos 5 segundos
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                currentTime - 5000,
                currentTime
            )
            
            if (stats.isNullOrEmpty()) {
                return
            }
            
            // Obtener la app más reciente
            val sortedStats = stats.sortedByDescending { it.lastTimeUsed }
            val foregroundApp = sortedStats.firstOrNull()
            
            foregroundApp?.let { app ->
                val packageName = app.packageName
                
                // Ignorar nuestra propia app y el launcher
                if (packageName == this.packageName || 
                    packageName.contains("launcher", ignoreCase = true)) {
                    return
                }
                
                // Si cambió la app
                if (packageName != currentPackageName) {
                    Log.d(TAG, "App cambió: $currentPackageName -> $packageName")
                    
                    // Procesar sesión anterior (si existe)
                    currentPackageName?.let { previousPackage ->
                        val now = System.currentTimeMillis()
                        val durationSeconds = ((now - sessionStartTime) / 1000).toInt()
                        
                        // Solo registrar si duró más de 5 segundos (filtro de ruido)
                        if (durationSeconds >= 2) {
                            val inicio = sessionStartTime
                            val fin = now
                            
                            serviceScope.launch {
                                registrarSesionCompleta(previousPackage, inicio, fin, durationSeconds)
                            }
                        } else {
                            Log.d(TAG, "Sesión de $previousPackage ignorada (muy corta: ${durationSeconds}s)")
                        }
                    }
                    
                    // Iniciar seguimiento de nueva app en memoria
                    currentPackageName = packageName
                    sessionStartTime = currentTime
                    updateNotification("Usando: ${getAppName(packageName)}")
                    
                    // Verificar si debe bloquearse
                    verificarBloqueo(packageName, getAppName(packageName))
                } else {
                    // Si sigue siendo la misma app, verificar si ya se pasó del tiempo
                    verificarBloqueo(packageName, getAppName(packageName))
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error verificando app en primer plano: ${e.message}")
            e.printStackTrace()
        }
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
            val nuevoDispositivo = com.example.rest.data.models.Dispositivo(
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
    private suspend fun registrarSesionCompleta(packageName: String, inicio: Long, fin: Long, duracion: Int) {
        try {
            val validDeviceId = ensureDeviceId()
            
            if (validDeviceId == -1) {
                Log.e(TAG, "No se puede guardar sesión: ID dispositivo inválido")
                return
            }
            
            // Usamos SesionAppInput que NO tiene el campo ID para evitar problemas con Gson
            val sesion = com.example.rest.data.models.SesionAppInput(
                idDispositivo = validDeviceId,
                nombrePaquete = packageName,
                inicio = timestampFormat.format(Date(inicio)),
                fin = timestampFormat.format(Date(fin)),
                duracion = duracion,
                activa = false // Ya terminó
            )
            
            val response = api.iniciarSesion(sesion)
            
            if (response.isSuccessful) {
                Log.d(TAG, "✓ Sesión guardada: $packageName ($duracion s)")
                
                // Actualizar mapa local de uso
                val currentUsage = dailyUsageMap[packageName] ?: 0
                dailyUsageMap[packageName] = currentUsage + (duracion / 60)
            } else {
                // Incluso si falla el envío, actualizar localmente para el bloqueo
                val currentUsage = dailyUsageMap[packageName] ?: 0
                dailyUsageMap[packageName] = currentUsage + (duracion / 60)
                
                Log.e(TAG, "✗ Error guardando sesión: ${response.code()} Body: ${response.errorBody()?.string()}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Excepción guardando sesión: ${e.message}")
            e.printStackTrace()
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
        val appRestricted = restrictedApps.find { it.packageName == packageName } ?: return

        var reason = ""
        var shouldBlock = false

        if (appRestricted.isBlocked) {
            shouldBlock = true
            reason = "Esta aplicación ha sido bloqueada manualmente."
        } else {
            val limitMinutes = (appRestricted.limitHours * 60) + appRestricted.limitMinutes
            
            if (limitMinutes > 0) {
                // Verificar límite de tiempo
                val usoAcumuladoMin = dailyUsageMap[packageName] ?: 0
                
                // Sumar sesión actual (en minutos)
                val currentSessionMs = System.currentTimeMillis() - sessionStartTime
                val currentSessionMin = (currentSessionMs / 60000).toInt()
                
                val totalMin = usoAcumuladoMin + currentSessionMin
                
                if (totalMin >= limitMinutes) {
                    shouldBlock = true
                    reason = "Has excedido el límite de tiempo diario ($limitMinutes min)."
                }
            }
        }

        if (shouldBlock) {
             val intent = BloqueoActivity.newIntent(this, appName, reason)
             intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
             startActivity(intent)
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
