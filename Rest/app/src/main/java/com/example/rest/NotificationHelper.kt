package com.example.rest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.rest.data.NotificationRepository
import com.example.rest.services.ChatHeadOverlayService
import com.example.rest.utils.NotificationPreferences
import com.example.rest.features.home.InicioComposeActivity

/**
 * Helper unificado para enviar notificaciones:
 * - Flotantes (ChatHead)
 * - Sistema (NotificationManager)
 * - A ambos (hijo y padre)
 */
object NotificationHelper {

    enum class NotifType {
        USO_EXCESIVO,
        TAREA,
        EVENTO,
        TIEMPO_PANTALLA,
        BLOQUEO
    }

    // ========== CANALES DE NOTIFICACIÓN ==========

    private const val CHANNEL_FLOTANTE = "notification_flotante"
    private const val CHANNEL_SISTEMA = "notification_sistema"

    private fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            // Canal flotante (alta importancia)
            val flotanteChannel = NotificationChannel(
                CHANNEL_FLOTANTE,
                "Notificaciones Flotantes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Burbujas flotantes de Rest Cycle"
                enableVibration(true)
            }

            // Canal sistema (importancia por defecto)
            val sistemaChannel = NotificationChannel(
                CHANNEL_SISTEMA,
                "Notificaciones del Sistema",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones push de Rest Cycle"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(listOf(flotanteChannel, sistemaChannel))
        }
    }

    // ========== VERIFICACIONES DE HABILITACIÓN ==========

    fun isFlotanteEnabled(context: Context, tipo: NotifType): Boolean {
        if (!NotificationPreferences.isFlotanteEnabled(context)) return false

        return when (tipo) {
            NotifType.USO_EXCESIVO -> NotificationPreferences.isFlotanteUsoExcesivo(context)
            NotifType.TAREA -> NotificationPreferences.isFlotanteTareas(context)
            NotifType.EVENTO -> NotificationPreferences.isFlotanteEventos(context)
            NotifType.TIEMPO_PANTALLA -> NotificationPreferences.isFlotanteTiempoPantalla(context)
            NotifType.BLOQUEO -> NotificationPreferences.isFlotanteBloqueo(context)
        }
    }

    fun isSistemaEnabled(context: Context, tipo: NotifType): Boolean {
        if (!NotificationPreferences.isSistemaEnabled(context)) return false

        return when (tipo) {
            NotifType.USO_EXCESIVO -> NotificationPreferences.isSistemaUsoExcesivo(context)
            NotifType.TAREA -> NotificationPreferences.isSistemaTareas(context)
            NotifType.EVENTO -> NotificationPreferences.isSistemaEventos(context)
            NotifType.TIEMPO_PANTALLA -> NotificationPreferences.isSistemaTiempoPantalla(context)
            NotifType.BLOQUEO -> NotificationPreferences.isSistemaBloqueo(context)
        }
    }

    // ========== NOTIFICACIÓN FLOTANTE ==========

    fun showFloatingNotification(
        context: Context,
        title: String,
        message: String,
        tipo: NotifType
    ) {
        if (!isFlotanteEnabled(context, tipo)) return

        // Agregar al repositorio de notificaciones
        NotificationRepository.addNotification(
            title = title,
            message = message,
            category = getCategoryName(tipo)
        )

        // Lanzar ChatHead si no está activo
        ChatHeadManager.launchOverlayService(context)
    }

    // ========== NOTIFICACIÓN DEL SISTEMA ==========

    fun showSystemNotification(
        context: Context,
        title: String,
        message: String,
        tipo: NotifType,
        pendingIntent: PendingIntent? = null
    ) {
        if (!isSistemaEnabled(context, tipo)) return

        createChannels(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_SISTEMA)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250))

        pendingIntent?.let {
            builder.setContentIntent(it)
        }

        try {
            val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            android.util.Log.e("NotificationHelper", "Permiso de notificaciones no concedido")
        }
    }

    // ========== NOTIFICAR A AMBOS (HIJO Y PADRE) ==========

    fun notifyBoth(
        context: Context,
        title: String,
        message: String,
        tipo: NotifType,
        idHijo: Int = -1
    ) {
        // Enviar al hijo (flotante + sistema)
        showFloatingNotification(context, title, message, tipo)
        showSystemNotification(context, title, message, tipo)

        // TODO: Aquí se puede agregar lógica para notificar al padre
        // Por ejemplo: guardar en tabla de notificaciones de Supabase
        // o usar Firebase Cloud Messaging
    }

    // ========== HELPERS ==========

    private fun getCategoryName(tipo: NotifType): String {
        return when (tipo) {
            NotifType.USO_EXCESIVO -> "UsoExcesivo"
            NotifType.TAREA -> "Tarea"
            NotifType.EVENTO -> "Evento"
            NotifType.TIEMPO_PANTALLA -> "TiempoPantalla"
            NotifType.BLOQUEO -> "Bloqueo"
        }
    }

    /**
     * Obtener intent para abrir la app al tocar notificación
     */
    fun getAppPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, InicioComposeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
