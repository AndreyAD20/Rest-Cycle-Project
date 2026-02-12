package com.example.rest.data.models

import android.graphics.drawable.Drawable

/**
 * Modelo para estadísticas de uso de una aplicación
 */
data class AppUsageStats(
    val packageName: String,
    val appName: String,
    val totalTimeInForeground: Long, // Tiempo en milisegundos
    val lastTimeUsed: Long, // Timestamp de último uso
    val icon: Drawable? = null
) {
    /**
     * Convierte el tiempo en milisegundos a formato legible (Xh Ym)
     */
    fun getFormattedTime(): String {
        val totalMinutes = totalTimeInForeground / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }
    
    /**
     * Obtiene el tiempo en horas como decimal
     */
    fun getTimeInHours(): Float {
        return totalTimeInForeground / 3600000f
    }
}
