package com.example.rest.utils

import android.util.Log

/**
 * Servicio para enviar emails de verificación
 * Por ahora solo registra en logs, en producción se integrará con un servicio real
 */
object EmailService {
    
    private const val TAG = "EmailService"
    
    /**
     * Envía un código de verificación por email
     * @param correo Email del destinatario
     * @param codigo Código de verificación de 6 dígitos
     * @param nombre Nombre del usuario (opcional)
     * @return true si se envió correctamente, false en caso contrario
     */
    fun enviarCodigoVerificacion(
        correo: String,
        codigo: String,
        nombre: String = "Usuario"
    ): Boolean {
        return try {
            // TODO: Implementar envío real de email usando Supabase Edge Functions + Resend
            // Por ahora solo registramos en logs para desarrollo
            
            Log.d(TAG, "========================================")
            Log.d(TAG, "📧 EMAIL DE VERIFICACIÓN")
            Log.d(TAG, "========================================")
            Log.d(TAG, "Para: $correo")
            Log.d(TAG, "Asunto: Código de verificación - Rest Cycle")
            Log.d(TAG, "")
            Log.d(TAG, "Hola $nombre,")
            Log.d(TAG, "")
            Log.d(TAG, "Tu código de verificación es:")
            Log.d(TAG, "")
            Log.d(TAG, "    🔐 $codigo")
            Log.d(TAG, "")
            Log.d(TAG, "Este código expirará en 15 minutos.")
            Log.d(TAG, "")
            Log.d(TAG, "Si no solicitaste este código, ignora este mensaje.")
            Log.d(TAG, "========================================")
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar email: ${e.message}", e)
            false
        }
    }
    
    /**
     * Envía un email de bienvenida después de verificar la cuenta
     * @param correo Email del destinatario
     * @param nombre Nombre del usuario
     * @return true si se envió correctamente, false en caso contrario
     */
    fun enviarEmailBienvenida(correo: String, nombre: String): Boolean {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📧 EMAIL DE BIENVENIDA")
            Log.d(TAG, "========================================")
            Log.d(TAG, "Para: $correo")
            Log.d(TAG, "Asunto: ¡Bienvenido a Rest Cycle!")
            Log.d(TAG, "")
            Log.d(TAG, "¡Hola $nombre!")
            Log.d(TAG, "")
            Log.d(TAG, "Tu cuenta ha sido verificada exitosamente.")
            Log.d(TAG, "Ya puedes disfrutar de todas las funciones de Rest Cycle.")
            Log.d(TAG, "========================================")
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar email de bienvenida: ${e.message}", e)
            false
        }
    }
}
