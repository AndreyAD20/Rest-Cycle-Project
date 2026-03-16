package com.example.rest

/**
 * Modelo local de una Tarea para las notificaciones y alarmas.
 *
 * Este modelo es independiente del modelo Supabase (data.models.Tarea).
 * Guarda los datos que necesita TaskNotificationManager y TaskPersistence
 * en SharedPreferences mediante Gson.
 *
 * Campos:
 *   - id        : identificador único (usado como ID de alarma/notificación)
 *   - titulo    : nombre de la tarea
 *   - nota      : descripción o nota extra
 *   - tieneHora : si tiene hora de recordatorio programada
 *   - hora      : hora en formato "HH:mm AM/PM" (ej: "7:30 AM")
 *   - vibracion : si la notificación debe vibrar
 *   - sonido    : si la notificación debe sonar
 *   - completada: si la tarea ya fue marcada como hecha
 */
data class Tarea(
    val id: Int = 0,
    val titulo: String = "",
    val nota: String = "",
    val tieneHora: Boolean = false,
    val hora: String = "",
    val vibracion: Boolean = true,
    val sonido: Boolean = true,
    val completada: Boolean = false
)
