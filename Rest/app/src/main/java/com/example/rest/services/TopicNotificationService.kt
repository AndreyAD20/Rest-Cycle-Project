package com.example.rest.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.rest.R
import com.example.rest.data.GeneradorContenidoMock
import com.example.rest.data.NotificationRepository
import com.example.rest.data.PreferenciasInteresManager
import kotlin.random.Random

/**
 * TopicNotificationService:
 * Foreground Service que simula la llegada de notificaciones cada 5 minutos
 * con temas de interés del usuario, inyectándolas en el NotificationRepository
 * para que la burbuja principal centralizada las muestre en un toast flotante estilizado.
 * 
 * Un "Service" en Android es un componente que se ejecuta en segundo plano, sin
 * interfaz gráfica directa. Al ser un "Foreground Service" (Servicio en Primer Plano),
 * requiere mostrar una notificación persistente en la barra de estado para que
 * el sistema operativo no lo mate para recuperar memoria.
 */
class TopicNotificationService : Service() {

    // Handler asociado al Hilo Principal (UI Thread).
    // Se usa para programar tareas que se ejecutarán en el futuro o periódicamente.
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isServiceRunning = false
    
    // El popup saldrá cada 5 minutos (300,000 ms)
    private val INTERVALO_MOSTRAR = 5 * 60 * 1000L

    // Un "Runnable" es un bloque de código ejecutable. Aquí creamos uno que se 
    // llama a sí mismo cíclicamente usando postDelayed, creando un bucle infinito
    // pausado en el tiempo especificado.
    private val periodicRunnable = object : Runnable {
        override fun run() {
            if (isServiceRunning) {
                mostrarNotificacionAleatoria()
                // Una vez terminado, se programa nuevamente para dentro del intervalo establecido
                mainHandler.postDelayed(this, INTERVALO_MOSTRAR)
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "topic_notification_service_channel"
        const val NOTIFICATION_ID = 4002
        const val ACTION_SIMULATE_TOPIC_POPUP = "com.example.rest.action.SIMULATE_TOPIC_POPUP"
        
        fun startService(context: Context) {
            val intent = Intent(context, TopicNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, TopicNotificationService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        isServiceRunning = true
        
        // Iniciar el temporizador
        mainHandler.postDelayed(periodicRunnable, INTERVALO_MOSTRAR)
    }

    /**
     * onStartCommand se llama cada vez que otro componente (como una Activity o BroadcastReceiver) 
     * llama a startService() o startForegroundService() con un Intent dirigido a este servicio.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_SIMULATE_TOPIC_POPUP) {
            // Leer desde los "extras" del Intent la lista de temas. 
            // Si el Intent no trae este dato, getStringArrayListExtra devolverá null,
            // y con el operador '?.' evitamos un crasheo del app.
            val temasSimulados = intent.getStringArrayListExtra("TEMAS_SIMULADOS")?.toList()
            Log.d("TopicNotification", "Recibido intent para simular popup con temas: $temasSimulados")
            mostrarNotificacionAleatoria(temasSimulados) // Fuerza la inyección inmediata
        }
        // START_STICKY indica al sistema Android que, si el sistema operativo mata este servicio 
        // por falta de memoria RAM, debe intentar recrearlo y reiniciarlo cuando vuelva a haber memoria, 
        // pasándole un Intent nulo.
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun mostrarNotificacionAleatoria(temasOverride: List<String>? = null) {
        // Verificar que la pantalla esté encendida
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!powerManager.isInteractive) {
            Log.d("TopicNotification", "Pantalla apagada, se omite el popup")
            return
        }
        
        val temasElegidos = temasOverride ?: PreferenciasInteresManager.obtenerTemas(this)
        if (temasElegidos.isEmpty()) {
            Log.d("TopicNotification", "No hay temas elegidos, no se muestra nada")
            return
        }

        // Seleccionar aleatoriamente el tipo de contenido (50% historia/noticia, 50% frase)
        // Usamos var porque estas variables cambiarán sus valores en el bloque if-else.
        var titulo = ""
        var mensaje = ""

        if (Random.nextBoolean()) {
            // Noticia o dato curioso (Mock, es decir, datos simulados de mentira)
            val noticia = GeneradorContenidoMock.obtenerNoticiasParaTemas(temasElegidos).randomOrNull()
            if (noticia != null) {
                titulo = noticia.tema
                mensaje = noticia.titulo
            }
        } else {
            // Frase motivacional
            titulo = "Para tu Inspiración"
            mensaje = GeneradorContenidoMock.generarFraseMotivacional(temasElegidos)
        }
        
        if (titulo.isNotEmpty() && mensaje.isNotEmpty()) {
            // ---------------------------------------------------------------------------------
            // SEPARACIÓN DE RESPONSABILIDADES (Separation of Concerns)
            // ---------------------------------------------------------------------------------
            // En lugar de que este servicio intente dibujar en pantalla directamente (lo cual
            // sería complejo y violaría el principio de única responsabilidad), simplemente 
            // le entregamos los datos al repositorio central (NotificationRepository).
            // Cualquier oyente que observe a ese repositorio (como la Burbuja ChatHead)
            // reaccionará de manera reactiva y se encargará de mostrarla en la UI de inmediato.
            NotificationRepository.addNotification(
                title = titulo,
                message = mensaje,
                category = "Notificación de Sistema"  // Usamos esta por ahora para que muestre el icono de info
            )
            Log.d("TopicNotification", "Inyectada notificación en el Repositorio: $titulo")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        mainHandler.removeCallbacks(periodicRunnable)
    }

    private fun buildForegroundNotification(): android.app.Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(CHANNEL_ID, "Explorador de Temas", NotificationManager.IMPORTANCE_LOW)
                .also { getSystemService(NotificationManager::class.java).createNotificationChannel(it) }
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Explorador de Temas activo")
            .setContentText("Generando recomendaciones personalizadas")
            .setSmallIcon(R.mipmap.logo_buho)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
