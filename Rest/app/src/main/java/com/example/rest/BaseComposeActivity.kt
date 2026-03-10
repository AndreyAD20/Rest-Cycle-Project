package com.example.rest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Actividad base para todas las actividades Compose del proyecto.
 * - Configura automáticamente el modo edge-to-edge y oculta las barras del sistema.
 * - Crea todos los canales de notificación necesarios (idempotente — se puede llamar N veces).
 */
abstract class BaseComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configurarPantallaCompleta()
        inicializarCanalesNotificacion()
    }

    /**
     * Crea todos los canales de notificación de la app.
     * Es idempotente: si el canal ya existe, no hace nada.
     * IMPORTANTE: debe ejecutarse desde primer plano (Activity), antes de cualquier alarma.
     */
    private fun inicializarCanalesNotificacion() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // ── Canal 1: tareas (ya existía, no lo tocamos) ───────────────────────
        if (nm.getNotificationChannel("task_notifications") == null) {
            val canal = NotificationChannel(
                "task_notifications",
                "Recordatorios de Tareas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para tareas programadas"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttr = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(soundUri, audioAttr)
            }
            nm.createNotificationChannel(canal)
            Log.d("BaseActivity", "✅ Canal 'task_notifications' creado")
        }

        // ── Canal 2: eventos del calendario con burbujas ──────────────────────
        if (nm.getNotificationChannel("bubble_eventos") == null) {
            val canal = NotificationChannel(
                "bubble_eventos",
                "Eventos del Calendario",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de eventos del calendario (con burbujas)"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                // Android 10+ (Q=29): habilitar burbujas en este canal
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setAllowBubbles(true)
                }
            }
            nm.createNotificationChannel(canal)
            Log.d("BaseActivity", "✅ Canal 'bubble_eventos' creado con setAllowBubbles(true)")
        }
    }

    /**
     * Configura el modo de pantalla completa ocultando las barras del sistema.
     */
    private fun configurarPantallaCompleta() {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
