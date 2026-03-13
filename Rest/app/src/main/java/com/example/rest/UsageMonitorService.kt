package com.example.rest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import java.util.Calendar

/**
 * 🔧 SERVICIO EN SEGUNDO PLANO: UsageMonitorService
 *
 * Un "Service" en Android es un componente que corre en segundo plano,
 * incluso cuando el usuario no está mirando la pantalla ni usando la app.
 *
 * Este servicio es el "cerebro operativo" del sistema de descanso:
 * cada 3 segundos revisa si hay un horario activo y aplica o quita
 * el modo No Molestar (DND) y la escala de grises.
 *
 * Es un "Foreground Service" porque muestra una notificación persistente.
 * Android exige esto para servicios de larga duración, ya que el sistema
 * sabe que el usuario está al tanto de que la app está corriendo.
 */
class UsageMonitorService : Service() {

    // ---------------------------------------------------------------------------
    // Mecanismo de repetición: Handler + Runnable
    // ---------------------------------------------------------------------------

    /**
     * Handler permite programar tareas para ejecutarse en el hilo principal de UI.
     * Aquí lo usamos para repetir la verificación cada 3 segundos.
     */
    private val handler = Handler(Looper.getMainLooper())

    /**
     * Runnable es un bloque de código que se puede ejecutar repetidamente.
     *
     * Funciona así:
     *   1. Se ejecuta checkDowntime() (verifica horarios)
     *   2. Se reprograma a sí mismo para ejecutarse de nuevo en 3000ms (3 segundos)
     *   3. Vuelve al paso 1... indefinidamente mientras el servicio esté activo
     */
    private val checkRunnable = object : Runnable {
        override fun run() {
            checkDowntime()                          // Verificar horarios ahora
            handler.postDelayed(this, 3000)          // Repetir en 3 segundos
        }
    }

    // ---------------------------------------------------------------------------
    // Constantes del canal de notificación
    // ---------------------------------------------------------------------------

    companion object {
        /** ID numérico de la notificación (cualquier número único sirve). */
        private const val NOTIFICATION_ID = 1001

        /**
         * ID del canal de notificación (requerido desde Android 8.0 / Oreo).
         * Android agrupa las notificaciones en "canales" para que el usuario
         * pueda silenciar categorías específicas.
         */
        private const val CHANNEL_ID = "usage_monitor_channel"
    }

    // ---------------------------------------------------------------------------
    // Ciclo de vida del Service
    // ---------------------------------------------------------------------------

    /**
     * onBind: se llama cuando otra parte de la app intenta "conectarse" al servicio.
     * Retornamos null porque este servicio NO necesita comunicación bidireccional
     * (es un "Started Service", no un "Bound Service").
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * onStartCommand: se llama CADA VEZ que alguien arranca el servicio con startService().
     *
     * Aquí:
     *   1. Creamos el canal de notificación (si no existe aún)
     *   2. Ponemos el servicio en "foreground" con su notificación visible
     *   3. Iniciamos el ciclo de verificación cada 3 segundos
     *
     * START_STICKY: si Android mata el servicio por falta de memoria,
     * lo reiniciará automáticamente cuando haya recursos disponibles.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()                      // Paso 1: crear canal
        startForeground(NOTIFICATION_ID, createNotification()) // Paso 2: notificación
        handler.post(checkRunnable)                      // Paso 3: iniciar ciclo
        return START_STICKY                              // Android lo reiniciará si muere
    }

    /**
     * onDestroy: se llama cuando el servicio está por terminarse.
     * Cancelamos todas las tareas pendientes del Handler para no desperdiciar
     * recursos después de que el servicio muera.
     */
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkRunnable)  // Detener el ciclo de verificación
    }

    // ---------------------------------------------------------------------------
    // Configuración de la notificación persistente
    // ---------------------------------------------------------------------------

    /**
     * Crea el canal de notificación (solo necesario en Android 8.0+).
     *
     * Los canales permiten al usuario controlar el comportamiento de las
     * notificaciones de la app. IMPORTANCE_LOW no hace sonido ni vibración.
     */
    private fun createNotificationChannel() {
        // SDK_INT es la versión de Android. O = Oreo = versión 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hora de Descanso",          // Nombre visible en ajustes de notificaciones
                NotificationManager.IMPORTANCE_LOW  // Silencioso, sin vibración
            ).apply {
                description = "Monitorea los horarios de descanso para aplicar DND y escala de grises"
                setShowBadge(false)          // No mostrar punto rojo en el ícono de la app
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    /**
     * Crea y retorna el objeto Notification que se muestra mientras el servicio corre.
     *
     * NotificationCompat.Builder usa el patrón "Builder" de diseño:
     * se configura paso a paso con métodos encadenados hasta llamar .build().
     */
    private fun createNotification(): android.app.Notification {
        val intent = Intent(this, com.example.rest.features.tools.HoraDescansoComposeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val textContent = "Monitoreando horarios de descanso"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hora de Descanso activa")
            .setContentText(textContent)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE) // Ayuda a que aparezca como Burbuja
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        // Configurar metadatos de Burbuja
        val bubbleData = com.example.rest.utils.BubbleHelper.createBubbleMetadata(this, pendingIntent)
        if (bubbleData != null) {
            builder.setBubbleMetadata(bubbleData)
            val person = com.example.rest.utils.BubbleHelper.createBotPerson()
            builder.addPerson(person)
            builder.setStyle(NotificationCompat.MessagingStyle(person)
                .addMessage(textContent, System.currentTimeMillis(), person))
        }

        return builder.build()
    }

    // ---------------------------------------------------------------------------
    // Lógica principal: verificar horarios y aplicar restricciones
    // ---------------------------------------------------------------------------

    /**
     * ⏰ VERIFICACIÓN PRINCIPAL — se ejecuta cada 3 segundos.
     *
     * Recorre TODOS los horarios guardados en DowntimeManager y pregunta:
     * "¿Está activo alguno de estos horarios AHORA MISMO?"
     *
     * Si al menos uno está activo → activa DND y escala de grises.
     * Si ninguno está activo → desactiva ambos.
     */
    private fun checkDowntime() {
        // Obtenemos la lista completa de horarios guardados localmente
        val schedules = DowntimeManager.getSchedules(this)

        var shouldEnableDND = false   // Asumimos que no hay que activar DND

        // Recorremos cada horario para ver si alguno aplica ahora
        for (schedule in schedules) {
            if (DowntimeManager.isScheduleActive(schedule)) {
                // ¡Este horario está activo ahora! ¿También activa bedtimeMode?
                if (schedule.bedtimeMode) shouldEnableDND = true
                break   // Con que uno esté activo es suficiente, salimos del bucle
            }
        }

        // Aplicamos el resultado a DND y escala de grises
        manageDND(shouldEnableDND)
        manageGrayscale(shouldEnableDND)
    }

    /**
     * 🔕 GESTIONAR EL MODO NO MOLESTAR (DND = Do Not Disturb)
     *
     * DND silencia las notificaciones del teléfono.
     * Requiere que el usuario haya concedido el permiso especial de "No Molestar".
     *
     * INTERRUPTION_FILTER_NONE  → Silencio total (bloquea todo)
     * INTERRUPTION_FILTER_ALL   → Normal (permite todo)
     *
     * @param enable true = activar DND, false = desactivar DND
     */
    private fun manageDND(enable: Boolean) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Si no tenemos permiso, salimos sin hacer nada
        if (!nm.isNotificationPolicyAccessGranted) return

        val current = nm.currentInterruptionFilter   // Estado actual del DND

        if (enable) {
            // Solo cambiamos si aún no está en DND (evitamos llamadas innecesarias)
            if (current == NotificationManager.INTERRUPTION_FILTER_ALL ||
                current == NotificationManager.INTERRUPTION_FILTER_UNKNOWN) {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            }
        } else {
            // Solo restauramos si DND estaba activo
            if (current != NotificationManager.INTERRUPTION_FILTER_ALL) {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        }
    }

    /**
     * ⬛ GESTIONAR LA ESCALA DE GRISES (pantalla en blanco y negro)
     *
     * La escala de grises reduce el atractivo visual del teléfono,
     * haciendo que usarlo sea menos atractivo antes de dormir.
     *
     * Requiere el permiso WRITE_SECURE_SETTINGS, que solo se puede conceder
     * por ADB (línea de comandos). Es un permiso muy poderoso.
     *
     * Se activa modificando Settings.Secure del sistema:
     *   - accessibility_display_daltonizer_enabled = 1 → activar filtro de color
     *   - accessibility_display_daltonizer = 0 → modo escala de grises
     *
     * @param enable true = pantalla en gris, false = restaurar color
     */
    private fun manageGrayscale(enable: Boolean) {
        // Verificamos si tenemos el permiso de escritura en ajustes seguros
        val hasPermission = checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val cr = contentResolver   // ContentResolver da acceso a los Settings del sistema

        if (enable) {
            // Activar escala de grises
            android.provider.Settings.Secure.putInt(cr, "accessibility_display_daltonizer_enabled", 1)
            android.provider.Settings.Secure.putInt(cr, "accessibility_display_daltonizer", 0)
        } else {
            try {
                // Solo desactivamos si estaba activo (para no interferir si el usuario
                // activó la escala de grises manualmente por otra razón)
                val current = android.provider.Settings.Secure.getInt(
                    cr, "accessibility_display_daltonizer_enabled"
                )
                if (current == 1) {
                    android.provider.Settings.Secure.putInt(
                        cr, "accessibility_display_daltonizer_enabled", 0
                    )
                }
            } catch (_: Exception) {
                // Si el setting no existe, ignoramos el error silenciosamente
            }
        }
    }
}
