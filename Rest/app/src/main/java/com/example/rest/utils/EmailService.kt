package com.example.rest.utils

import android.util.Log
import com.example.rest.network.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Servicio para enviar emails de verificación
 * Usa Supabase Edge Functions + Resend API
 */
object EmailService {
    
    private const val TAG = "EmailService"
    private val EDGE_FUNCTION_URL = "${SupabaseConfig.SUPABASE_URL}/functions/v1/enviar-codigo-verificacion"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Envía un código de verificación por email usando Edge Function
     * @param correo Email del destinatario
     * @param codigo Código de verificación de 6 dígitos
     * @param nombre Nombre del usuario
     * @return true si se envió correctamente, false en caso contrario
     */
    suspend fun enviarCodigoVerificacion(
        correo: String,
        codigo: String,
        nombre: String = "Usuario"
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🚀 Iniciando envío de código a: $correo")
                Log.d(TAG, "URL: $EDGE_FUNCTION_URL")
                
                // Crear JSON body
                val json = JSONObject().apply {
                    put("email", correo)
                    put("code", codigo)
                    put("nombre", nombre)
                }
                
                val body = json.toString().toRequestBody("application/json".toMediaType())
                
                // Crear request
                val request = Request.Builder()
                    .url(EDGE_FUNCTION_URL)
                    .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()
                
                // Ejecutar request
                Log.d(TAG, "⏳ Ejecutando petición HTTP...")
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: "Sin respuesta"
                
                Log.d(TAG, "📩 Response Code: ${response.code}")
                Log.d(TAG, "📩 Response Body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Email enviado exitosamente")
                    true
                } else {
                    Log.e(TAG, "❌ Falló el envío del email. Código: ${response.code}")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción al enviar email: ${e.message}", e)
                false
            }
        }
    }
    
    /**
     * Envía un email de bienvenida después de verificar la cuenta
     * @param correo Email del destinatario
     * @param nombre Nombre del usuario
     * @return true si se envió correctamente, false en caso contrario
     */
    suspend fun enviarEmailBienvenida(correo: String, nombre: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📧 (TODO) Email de bienvenida para $nombre -> $correo")
                // TODO: Implementar llamada a Edge Function si es necesario
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
                false
            }
        }
    }
    
    /**
     * Envía un código de recuperación de contraseña por email usando Edge Function
     * @param correo Email del destinatario
     * @param codigo Código de recuperación de 6 dígitos
     * @return true si se envió correctamente, false en caso contrario
     */
    suspend fun enviarCodigoRecuperacion(
        correo: String,
        codigo: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔐 Iniciando envío de código de recuperación a: $correo")
                
                // Usar la misma Edge Function pero con un parámetro diferente
                val edgeFunctionUrl = "${SupabaseConfig.SUPABASE_URL}/functions/v1/enviar-codigo-verificacion"
                
                // Crear JSON body
                val json = JSONObject().apply {
                    put("email", correo)
                    put("code", codigo)
                    put("tipo", "recuperacion") // Indicar que es recuperación
                }
                
                val body = json.toString().toRequestBody("application/json".toMediaType())
                
                // Crear request
                val request = Request.Builder()
                    .url(edgeFunctionUrl)
                    .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()
                
                // Ejecutar request
                Log.d(TAG, "⏳ Ejecutando petición HTTP...")
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: "Sin respuesta"
                
                Log.d(TAG, "📩 Response Code: ${response.code}")
                Log.d(TAG, "📩 Response Body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Email de recuperación enviado exitosamente")
                    true
                } else {
                    Log.e(TAG, "❌ Falló el envío del email. Código: ${response.code}")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción al enviar email de recuperación: ${e.message}", e)
                false
            }
        }
    }
}
