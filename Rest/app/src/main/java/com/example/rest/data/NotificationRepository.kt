package com.example.rest.data

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Modelo de datos para las notificaciones internalizadas en el Chat Head.
 * 
 * "data class" en Kotlin se usa para crear clases cuyo objetivo principal es
 * únicamente retener datos (como un POJO o un Model). El compilador genera
 * automáticamente métodos útiles como equals(), hashCode() y toString() detrás de escena.
 */
data class AppNotification(
    // UUID.randomUUID() genera un identificador único alfanumérico globalmente para cada notificación
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "General", // "Evento", "Descanso", "Tarea", "Estadísticas"
    val isRead: Boolean = false,
    val sourceClass: String? = null,   // fully-qualified Activity para navegación al hacer tap
    val systemNotificationId: Int? = null // ID usado al crear la notificación del SO
)

/**
 * Repositorio en Memoria (StateFlow) que actúa como el "Historial de Chat"
 * entre todos los emisores del proyecto y la burbuja universal de Rest Cycle.
 * 
 * La palabra clave "object" en Kotlin declara una clase que es también un Singleton real. 
 * Esto significa que solo existirá UNA ÚNICA INSTANCIA de NotificationRepository 
 * en toda la aplicación durante su tiempo de vida en memoria viva.
 */
object NotificationRepository {

    // ---------------------------------------------------------------------------------
    // PATRÓN BACKING PROPERTY (Propiedad de Respaldo)
    // ---------------------------------------------------------------------------------
    // MutableStateFlow (privado): Es la propiedad real que guarda los datos y permite modificarlos.
    // Solo puede ser mutado desde DENTRO de este archivo.
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    
    // StateFlow (público): Es una versión de solo-lectura expuesta hacia afuera.
    // Las Views (Activities, Services) se suscriben a esta variable para ser notificadas
    // cuando '_notifications' cambia, sin riesgo de que alguien cambie los datos accidentalmente de afuera.
    // "StateFlow" está diseñado para manejar estados reactivos en la interfaz y está inspirado en RxJava/LiveData.
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // ── Observadores ────────────────────────────────────────────────────────────

    /** Callback del Badge rojo (número de no leídos) */
    private var badgeObserver: ((Int) -> Unit)? = null
    fun setBadgeObserver(observer: ((Int) -> Unit)?) {
        badgeObserver = observer
        observer?.invoke(getUnreadCount())
    }

    /**
     * Callback que se dispara INMEDIATAMENTE cuando llega una notificación nueva.
     * Usado por [ChatHeadOverlayService] para auto-mostrar el tooltip al instante.
     */
    private var newNotificationObserver: ((AppNotification) -> Unit)? = null
    fun setNewNotificationObserver(observer: ((AppNotification) -> Unit)?) {
        newNotificationObserver = observer
    }

    // ── Operaciones ─────────────────────────────────────────────────────────────

    /**
     * Inserta un nuevo mensaje/alerta en la burbuja de chat.
     * Dispara automáticamente el tooltip en el overlay y actualiza el Badge.
     * @param sourceClass Fully-qualified class name de la Activity a la que navegar al tocar.
     * @param systemId ID de notificación si también se emitió una notificación de sistema común.
     */
    fun addNotification(
        title: String,
        message: String,
        category: String,
        sourceClass: String? = null,
        systemId: Int? = null
    ) {
        val newNotif = AppNotification(
            title = title,
            message = message,
            category = category,
            sourceClass = sourceClass,
            systemNotificationId = systemId
        )
        // Agregamos al principio (más reciente primero)
        // La función .update bloquea temporalmente el flujo para leer el estado anterior (current)
        // y calcular y asignar el estado nuevo en un solo paso atómico.
        // Esto previene que se pierdan datos si dos hilos (threads) intentan agregar notificaciones a la vez (Race condition).
        _notifications.update { current -> listOf(newNotif) + current }

        // Avisar al Badge (contador rojo)
        badgeObserver?.invoke(getUnreadCount())
        // Avisar al Overlay para auto-mostrar tooltip de inmediato
        newNotificationObserver?.invoke(newNotif)
    }

    /**
     * Marca una notificación en específico como leída usando su id único.
     * Opcionalmente cancela la notificación estándar del SO si cuenta con un systemNotificationId y se provee contexto.
     */
    fun markAsRead(id: String, context: Context? = null) {
        var notificationToClearFromOS: AppNotification? = null

        _notifications.update { current ->
            current.map { notif ->
                if (notif.id == id && !notif.isRead) {
                    notificationToClearFromOS = notif
                    notif.copy(isRead = true)
                } else {
                    notif
                }
            }
        }

        // Si realmente encontramos una y la marcamos como leída, intentamos quitarla del SO
        notificationToClearFromOS?.let { notif ->
            if (context != null && notif.systemNotificationId != null) {
                try {
                    NotificationManagerCompat.from(context).cancel(notif.systemNotificationId)
                } catch (e: Exception) {
                    android.util.Log.e("NotifRepo", "Error cancelando notif del SO: ${e.message}")
                }
            }
            // Siempre actualizar badge al cambiar read state
            badgeObserver?.invoke(getUnreadCount())
        }
    }

    /**
     * Marca todas las notificaciones del historial como leídas.
     * Limpia el Badge y oculta el contador rojo.
     */
    fun markAllAsRead() {
        _notifications.update { current -> current.map { it.copy(isRead = true) } }
        badgeObserver?.invoke(0)
    }

    private fun getUnreadCount(): Int = _notifications.value.count { !it.isRead }
}
