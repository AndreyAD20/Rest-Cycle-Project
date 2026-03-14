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
 * Servicio para enviar emails transaccionales.
 * Cada método apunta a su propia Supabase Edge Function.
 */
object EmailService {

    private const val TAG = "EmailService"

    // ── URLs de las 3 Edge Functions ──────────────────────────────────────────
    private val URL_VERIFICACION_EMAIL     = "${SupabaseConfig.SUPABASE_URL}/functions/v1/enviar-verificacion-email"
    private val URL_VERIFICACION_IDENTIDAD = "${SupabaseConfig.SUPABASE_URL}/functions/v1/enviar-verificacion-identidad"
    private val URL_RECUPERACION           = "${SupabaseConfig.SUPABASE_URL}/functions/v1/enviar-recuperacion-contrasena"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ── Utilidad interna ──────────────────────────────────────────────────────
    private suspend fun postJson(url: String, json: JSONObject): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()
                val response = client.newCall(request).execute()
                Log.d(TAG, "POST $url → ${response.code} | ${response.body?.string()}")
                response.isSuccessful
            } catch (e: Exception) {
                Log.e(TAG, "Error en $url: ${e.message}", e)
                false
            }
        }
    }

    // ── 1. Verificación de cuenta (registro) ──────────────────────────────────
    /**
     * Envía el código de verificación de cuenta tras el registro.
     * Edge Function: enviar-verificacion-email
     */
    suspend fun enviarCodigoVerificacion(
        correo: String,
        codigo: String,
        nombre: String = "Usuario"
    ): Boolean {
        Log.d(TAG, "📧 Verificación de email → $correo")
        return postJson(URL_VERIFICACION_EMAIL, JSONObject().apply {
            put("email", correo)
            put("code", codigo)
            put("nombre", nombre)
        })
    }

    // ── 2. Verificación de identidad (2FA al iniciar sesión) ──────────────────
    /**
     * Envía el código de segundo factor cuando el usuario inicia sesión.
     * Edge Function: enviar-verificacion-identidad
     */
    suspend fun enviarCodigo2FA(
        correo: String,
        codigo: String,
        nombre: String = "Usuario"
    ): Boolean {
        Log.d(TAG, "🔐 Verificación de identidad → $correo")
        return postJson(URL_VERIFICACION_IDENTIDAD, JSONObject().apply {
            put("email", correo)
            put("code", codigo)
            put("nombre", nombre)
        })
    }

    // ── 3. Recuperación de contraseña ─────────────────────────────────────────
    /**
     * Envía el código para restablecer la contraseña.
     * Edge Function: enviar-recuperacion-contrasena
     */
    suspend fun enviarCodigoRecuperacion(
        correo: String,
        codigo: String
    ): Boolean {
        Log.d(TAG, "🔑 Recuperación de contraseña → $correo")
        return postJson(URL_RECUPERACION, JSONObject().apply {
            put("email", correo)
            put("code", codigo)
        })
    }

    // ── Bienvenida (pendiente de Edge Function dedicada) ──────────────────────
    suspend fun enviarEmailBienvenida(correo: String, nombre: String): Boolean {
        Log.d(TAG, "📧 (TODO) Bienvenida para $nombre → $correo")
        return true
    }
}
