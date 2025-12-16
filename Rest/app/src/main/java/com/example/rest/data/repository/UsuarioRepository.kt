package com.example.rest.data.repository

import com.example.rest.data.models.LoginRequest
import com.example.rest.data.models.RegistroRequest
import com.example.rest.data.models.Usuario
import com.example.rest.network.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para operaciones relacionadas con usuarios
 * Maneja la lógica de negocio y las llamadas a la API
 */
class UsuarioRepository {
    
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
     * Realizar login
     * @param correo Correo electrónico del usuario
     * @param contraseña Contraseña del usuario
     * @return Result con el usuario si las credenciales son correctas
     */
    suspend fun login(correo: String, contraseña: String): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.login(
                    correo = "eq.$correo",
                    contraseña = "eq.$contraseña"
                )
                
                if (response.isSuccessful) {
                    val usuarios = response.body()
                    if (!usuarios.isNullOrEmpty()) {
                        Result.Success(usuarios[0])
                    } else {
                        Result.Error("Correo o contraseña incorrectos")
                    }
                } else {
                    Result.Error("Error en el servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Registrar nuevo usuario
     * @param request Datos del usuario a registrar
     * @return Result con el usuario creado
     */
    suspend fun registrar(request: RegistroRequest): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                // Primero verificar si el correo ya existe
                val verificacion = api.verificarCorreo(correo = "eq.${request.correo}")
                if (verificacion.isSuccessful && !verificacion.body().isNullOrEmpty()) {
                    return@withContext Result.Error("El correo ya está registrado")
                }
                
                // Crear el usuario
                val response = api.crearUsuario(request)
                
                if (response.isSuccessful) {
                    val usuarios = response.body()
                    if (!usuarios.isNullOrEmpty()) {
                        Result.Success(usuarios[0])
                    } else {
                        Result.Error("Error al crear el usuario")
                    }
                } else {
                    Result.Error("Error en el servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Obtener usuario por ID
     * @param id ID del usuario
     * @return Result con el usuario encontrado
     */
    suspend fun obtenerUsuarioPorId(id: Int): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.obtenerUsuarioPorId(id = "eq.$id")
                
                if (response.isSuccessful) {
                    val usuarios = response.body()
                    if (!usuarios.isNullOrEmpty()) {
                        Result.Success(usuarios[0])
                    } else {
                        Result.Error("Usuario no encontrado")
                    }
                } else {
                    Result.Error("Error en el servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Verificar si un correo ya está registrado
     * @param correo Correo a verificar
     * @return true si el correo existe, false si no
     */
    suspend fun verificarCorreoExiste(correo: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.verificarCorreo(correo = "eq.$correo")
                response.isSuccessful && !response.body().isNullOrEmpty()
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Actualizar datos de usuario
     * @param id ID del usuario
     * @param usuario Datos actualizados del usuario
     * @return Result con el usuario actualizado
     */
    suspend fun actualizarUsuario(id: Int, usuario: Usuario): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.actualizarUsuario(id = "eq.$id", usuario = usuario)
                
                if (response.isSuccessful) {
                    val usuarios = response.body()
                    if (!usuarios.isNullOrEmpty()) {
                        Result.Success(usuarios[0])
                    } else {
                        Result.Error("Error al actualizar el usuario")
                    }
                } else {
                    Result.Error("Error en el servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
}
