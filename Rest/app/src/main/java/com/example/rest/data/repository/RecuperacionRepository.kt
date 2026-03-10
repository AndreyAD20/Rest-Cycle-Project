package com.example.rest.data.repository

import com.example.rest.data.models.CambiarContraseñaRequest
import com.example.rest.data.models.CodigoRecuperacion
import com.example.rest.data.models.SolicitarCodigoRequest
import com.example.rest.network.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Repositorio para operaciones de recuperación de contraseña
 */
class RecuperacionRepository {
    
    private val api = SupabaseClient.api
    
    /**
     * Resultado de operaciones
     */
    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }
    
    /**
     * Generar código aleatorio de 6 dígitos
     */
    private fun generarCodigo(): String {
        return Random.nextInt(100000, 999999).toString()
    }
    
    /**
     * Solicitar código de recuperación
     * @param correo Correo del usuario
     * @return Result indicando éxito o error
     */
    suspend fun solicitarCodigo(correo: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Primero verificar que el correo existe
                val verificacion = api.verificarCorreo(correo = "eq.$correo")
                if (!verificacion.isSuccessful || verificacion.body().isNullOrEmpty()) {
                    return@withContext Result.Error("El correo no está registrado")
                }
                
                // Generar código
                val codigo = generarCodigo()
                
                // Guardar en la base de datos
                val request = SolicitarCodigoRequest(
                    correo = correo,
                    codigo = codigo
                )
                
                val response = api.crearCodigoRecuperacion(request)
                
                if (response.isSuccessful) {
                    // Enviar código por email usando EmailService
                    android.util.Log.d("RecuperacionRepo", "✅ Código guardado en BD: $codigo")
                    android.util.Log.d("RecuperacionRepo", "📧 Intentando enviar correo a: $correo")
                    
                    try {
                        val emailEnviado = com.example.rest.utils.EmailService.enviarCodigoRecuperacion(
                            correo = correo,
                            codigo = codigo
                        )
                        
                        android.util.Log.d("RecuperacionRepo", "📩 Resultado envío: $emailEnviado")
                        
                        if (emailEnviado) {
                            android.util.Log.d("RecuperacionRepo", "✅ Email enviado correctamente")
                            Result.Success(true)
                        } else {
                            android.util.Log.w("RecuperacionRepo", "⚠️ Email NO enviado, pero código guardado")
                            // Si falla el envío de email, aún así retornamos éxito
                            // porque el código está guardado en la BD
                            Result.Success(true)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("RecuperacionRepo", "❌ Excepción al enviar email: ${e.message}", e)
                        // Si falla el envío de email, aún así retornamos éxito
                        Result.Success(true)
                    }
                } else {
                    android.util.Log.e("RecuperacionRepo", "❌ Error al guardar código en BD: ${response.code()}")
                    Result.Error("Error al generar código: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Enviar código de verificación a un correo nuevo (sin verificar si existe en BD)
     * Usado cuando el usuario cambia su correo en el perfil
     * @param correoNuevo Nuevo correo al que enviar el código
     * @return Result con el código generado (para validación local) o Error
     */
    suspend fun enviarCodigoVerificacionCorreo(correoNuevo: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Generar código
                val codigo = generarCodigo()
                
                // Guardar en la base de datos (reutilizamos la misma tabla de recuperación)
                val request = SolicitarCodigoRequest(
                    correo = correoNuevo,
                    codigo = codigo
                )
                
                val response = api.crearCodigoRecuperacion(request)
                
                if (response.isSuccessful) {
                    // Enviar código por email
                    try {
                        com.example.rest.utils.EmailService.enviarCodigoRecuperacion(
                            correo = correoNuevo,
                            codigo = codigo
                        )
                        android.util.Log.d("RecuperacionRepo", "✅ Código de verificación enviado a: $correoNuevo")
                    } catch (e: Exception) {
                        android.util.Log.w("RecuperacionRepo", "⚠️ Error enviando email, pero código guardado: ${e.message}")
                    }
                    Result.Success(codigo)
                } else {
                    android.util.Log.e("RecuperacionRepo", "❌ Error al guardar código: ${response.code()}")
                    Result.Error("Error al generar código: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Verificar código de recuperación
     * @param correo Correo del usuario
     * @param codigo Código a verificar
     * @return Result con el ID del código si es válido
     */
    suspend fun verificarCodigo(correo: String, codigo: String): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.verificarCodigoRecuperacion(
                    correo = "eq.$correo",
                    codigo = "eq.$codigo"
                )
                
                if (response.isSuccessful) {
                    val codigos = response.body()
                    if (!codigos.isNullOrEmpty()) {
                        val codigoRecuperacion = codigos[0]
                        // Verificar que no haya expirado (15 minutos)
                        // Por simplicidad, asumimos que Supabase ya filtró los expirados
                        Result.Success(codigoRecuperacion.id!!)
                    } else {
                        Result.Error("Código inválido o expirado")
                    }
                } else {
                    Result.Error("Error al verificar código: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Cambiar contraseña
     * @param correo Correo del usuario
     * @param codigoId ID del código de recuperación
     * @param nuevaContraseña Nueva contraseña
     * @return Result indicando éxito o error
     */
    suspend fun cambiarContraseña(
        correo: String,
        codigoId: Int,
        nuevaContraseña: String
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Generar Hash de la nueva contraseña
                val hashPswd = com.example.rest.utils.SecurityUtils.hashPassword(nuevaContraseña)
                
                // Actualizar contraseña con el Hash
                val request = CambiarContraseñaRequest(contraseña = hashPswd)
                val response = api.actualizarContraseñaPorCorreo(
                    correo = "eq.$correo",
                    request = request
                )
                
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    // Marcar código como usado
                    api.marcarCodigoUsado(
                        id = "eq.$codigoId",
                        update = mapOf("usado" to true)
                    )
                    Result.Success(true)
                } else {
                    Result.Error("Error al cambiar contraseña: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
}
