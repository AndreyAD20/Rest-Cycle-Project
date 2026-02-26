package com.example.rest.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * Generador de códigos de verificación
 */
object CodeGenerator {
    
    private const val TAG = "CodeGenerator"
    
    /**
     * Genera un código de verificación aleatorio de 6 dígitos
     * @return String con 6 dígitos
     */
    fun generateVerificationCode(): String {
        return (100000..999999).random().toString()
    }
    
    /**
     * Calcula el tiempo de expiración (15 minutos desde ahora)
     * @return String en formato ISO 8601 compatible con PostgreSQL
     */
    fun getExpirationTime(): String {
        val now = System.currentTimeMillis()
        val expiration = now + (15 * 60 * 1000) // 15 minutos en milisegundos
        
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        
        val expirationDate = Date(expiration)
        val formatted = sdf.format(expirationDate)
        
        Log.d(TAG, "Generando expiración:")
        Log.d(TAG, "  Ahora (ms): $now")
        Log.d(TAG, "  Expira (ms): $expiration")
        Log.d(TAG, "  Formato: $formatted")
        
        return formatted
    }
    
    /**
     * Verifica si un código ha expirado
     * @param expiracion String con la fecha de expiración
     * @return true si ha expirado, false si aún es válido
     */
    fun isExpired(expiracion: String): Boolean {
        return try {
            // PostgreSQL devuelve formato ISO 8601 con T: "2026-01-12T16:20:51"
            // Necesitamos reemplazar la T por espacio para parsear
            val normalizedExpiracion = expiracion.replace("T", " ")
            
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            
            val expirationDate = sdf.parse(normalizedExpiracion)
            val now = System.currentTimeMillis()
            
            Log.d(TAG, "Verificando expiración:")
            Log.d(TAG, "  String recibido: $expiracion")
            Log.d(TAG, "  String normalizado: $normalizedExpiracion")
            Log.d(TAG, "  Fecha parseada (ms): ${expirationDate?.time}")
            Log.d(TAG, "  Ahora (ms): $now")
            Log.d(TAG, "  Diferencia (ms): ${(expirationDate?.time ?: 0) - now}")
            Log.d(TAG, "  Diferencia (min): ${((expirationDate?.time ?: 0) - now) / 60000}")
            Log.d(TAG, "  ¿Expirado?: ${(expirationDate?.time ?: 0) < now}")
            
            if (expirationDate == null) {
                Log.e(TAG, "Error: No se pudo parsear la fecha")
                return true
            }
            
            expirationDate.time < now
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar expiración: ${e.message}", e)
            true // Si hay error al parsear, consideramos expirado
        }
    }
}
