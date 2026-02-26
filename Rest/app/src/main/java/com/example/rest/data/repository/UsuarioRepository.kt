package com.example.rest.data.repository

import com.example.rest.data.models.LoginRequest
import com.example.rest.data.models.RegistroRequest
import com.example.rest.data.models.Usuario
import com.example.rest.network.SupabaseClient
import com.example.rest.utils.CodeGenerator
import com.example.rest.utils.EmailService

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
     * Registrar nuevo usuario con verificación de email
     * Genera un código de verificación y lo envía por email
     * @param request Datos del usuario a registrar
     * @return Result con mensaje de éxito y el código (para desarrollo)
     */
    suspend fun registrarConVerificacion(request: RegistroRequest): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Verificar si el correo ya existe en la tabla usuario
                val verificacion = api.verificarCorreo(correo = "eq.${request.correo}")
                if (verificacion.isSuccessful && !verificacion.body().isNullOrEmpty()) {
                    return@withContext Result.Error("El correo ya está registrado")
                }
                
                // 2. Generar código de verificación
                val codigoVerificacion = CodeGenerator.generateVerificationCode()
                val codigoExpiracion = CodeGenerator.getExpirationTime()
                
                // 3. Crear usuario en la base de datos (sin verificar)
                val response = api.crearUsuario(request)
                
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("UsuarioCtx", "Error crearUsuario: $errorBody")
                    return@withContext Result.Error("Error al crear el usuario: ${response.code()} - $errorBody")
                }
                
                // 4. Actualizar con código de verificación
                val updateData: Map<String, @JvmSuppressWildcards Any> = mapOf(
                    "codigo_verificacion" to codigoVerificacion,
                    "codigo_expiracion" to codigoExpiracion,
                    "email_verificado" to false
                )
                
                val updateResponse = api.actualizarCodigoVerificacion(
                    correo = "eq.${request.correo}",
                    update = updateData
                )
                
                if (!updateResponse.isSuccessful) {
                    return@withContext Result.Error("Error al generar código de verificación")
                }
                
                // 5. Enviar email con código
                val fecha = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                android.util.Log.d("UsuarioRepository", "[$fecha] Intentando enviar email a ${request.correo}")
                
                val envioExitoso = EmailService.enviarCodigoVerificacion(
                    correo = request.correo,
                    codigo = codigoVerificacion,
                    nombre = request.nombre
                )
                
                if (!envioExitoso) {
                    android.util.Log.e("UsuarioRepository", "[$fecha] Falló el envío del email")
                    return@withContext Result.Error("No se pudo enviar el correo de verificación. Por favor verifica que el correo sea válido o intenta más tarde.")
                }
                
                android.util.Log.d("UsuarioRepository", "[$fecha] Email enviado correctamente")
                
                // Mensaje sin mostrar el código
                Result.Success("Registro exitoso. Hemos enviado un código de verificación a su correo.")
            } catch (e: Exception) {
                Result.Error("Error al registrar: ${e.message}")
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
    
    /**
     * Verificar código de verificación
     * @param correo Correo del usuario
     * @param codigo Código ingresado por el usuario
     * @return Result con el usuario verificado
     */
    suspend fun verificarCodigo(correo: String, codigo: String): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Obtener datos de verificación del usuario
                val response = api.obtenerDatosVerificacion(correo = "eq.$correo", select = "*")
                
                if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                    return@withContext Result.Error("Usuario no encontrado")
                }
                
                val usuario = response.body()!![0]
                
                // 2. Verificar que el usuario no esté ya verificado
                if (usuario.emailVerificado) {
                    return@withContext Result.Error("El usuario ya está verificado")
                }
                
                // 3. Verificar que exista un código
                if (usuario.codigoVerificacion.isNullOrBlank()) {
                    return@withContext Result.Error("No hay código de verificación pendiente")
                }
                
                // 4. Verificar que el código coincida
                if (usuario.codigoVerificacion != codigo) {
                    return@withContext Result.Error("Código incorrecto")
                }
                
                // 5. Verificar que no haya expirado
                if (usuario.codigoExpiracion != null && CodeGenerator.isExpired(usuario.codigoExpiracion)) {
                    return@withContext Result.Error("El código ha expirado. Solicita uno nuevo.")
                }
                
                // 6. Marcar usuario como verificado y limpiar código
                val updateData: Map<String, @JvmSuppressWildcards Any?> = mapOf(
                    "email_verificado" to true,
                    "codigo_verificacion" to null,
                    "codigo_expiracion" to null
                )
                
                val updateResponse = api.marcarUsuarioVerificado(
                    correo = "eq.$correo",
                    update = updateData
                )
                
                if (updateResponse.isSuccessful && !updateResponse.body().isNullOrEmpty()) {
                    // Enviar email de bienvenida
                    EmailService.enviarEmailBienvenida(correo, usuario.nombre)
                    Result.Success(updateResponse.body()!![0])
                } else {
                    Result.Error("Error al verificar usuario")
                }
            } catch (e: Exception) {
                Result.Error("Error al verificar código: ${e.message}")
            }
        }
    }
    
    /**
     * Reenviar código de verificación
     * @param correo Correo del usuario
     * @return Result con mensaje de éxito
     */
    suspend fun reenviarCodigo(correo: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Verificar que el usuario existe
                val response = api.verificarCorreo(correo = "eq.$correo", select = "*")
                
                if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                    return@withContext Result.Error("Usuario no encontrado")
                }
                
                val usuario = response.body()!![0]
                
                // 2. Verificar que no esté ya verificado
                if (usuario.emailVerificado) {
                    return@withContext Result.Error("El usuario ya está verificado")
                }
                
                // 3. Generar nuevo código
                val nuevoCodigoVerificacion = CodeGenerator.generateVerificationCode()
                val nuevaExpiracion = CodeGenerator.getExpirationTime()
                
                // 4. Actualizar código en la base de datos
                val updateData: Map<String, @JvmSuppressWildcards Any> = mapOf(
                    "codigo_verificacion" to nuevoCodigoVerificacion,
                    "codigo_expiracion" to nuevaExpiracion
                )
                
                val updateResponse = api.actualizarCodigoVerificacion(
                    correo = "eq.$correo",
                    update = updateData
                )
                
                if (!updateResponse.isSuccessful) {
                    return@withContext Result.Error("Error al generar nuevo código")
                }
                
                // 5. Enviar email con nuevo código
                val envioExitoso = EmailService.enviarCodigoVerificacion(
                    correo = correo,
                    codigo = nuevoCodigoVerificacion,
                    nombre = usuario.nombre
                )
                
                if (!envioExitoso) {
                    return@withContext Result.Error("Error al enviar el email. Intenta nuevamente.")
                }
                
                Result.Success("Nuevo código enviado a tu correo.")
            } catch (e: Exception) {
                Result.Error("Error al reenviar código: ${e.message}")
            }
        }
    }
    
    /**
     * Actualizar usuario después de verificar email
     * Sincroniza el auth_user_id y marca email_verificado como true
     * @param authUserId UUID del usuario en Supabase Auth
     * @param correo Correo del usuario
     * @return Result con el usuario actualizado
     */
    suspend fun actualizarUsuarioVerificado(authUserId: String, correo: String): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                // Buscar el usuario por correo
                val response = api.verificarCorreo(correo = "eq.$correo", select = "*")
                
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val usuario = response.body()!![0]
                    
                    // Actualizar con auth_user_id y email_verificado
                    val usuarioActualizado = usuario.copy(
                        authUserId = authUserId,
                        emailVerificado = true
                    )
                    
                    val updateResponse = api.actualizarUsuario(
                        id = "eq.${usuario.id}",
                        usuario = usuarioActualizado
                    )
                    
                    if (updateResponse.isSuccessful && !updateResponse.body().isNullOrEmpty()) {
                        Result.Success(updateResponse.body()!![0])
                    } else {
                        Result.Error("Error al actualizar usuario verificado")
                    }
                } else {
                    Result.Error("Usuario no encontrado")
                }
            } catch (e: Exception) {
                Result.Error("Error al actualizar: ${e.message}")
            }
        }
    }
    
    /**
     * Crear un usuario hijo y enlazarlo con el padre
     * @param idPadre ID del usuario padre
     * @param datosHijo Datos del hijo para registro
     * @param contrasenaSegura Contraseña de seguridad para la conexión
     */
    /**
     * Crear un usuario hijo, generar código de verificación, enviar email y enlazarlo con el padre.
     * @param idPadre ID del usuario padre
     * @param datosHijo Datos del hijo para registro
     * @param contrasenaSegura Contraseña de seguridad para la conexión
     */
    suspend fun crearHijo(idPadre: Int, datosHijo: RegistroRequest, contrasenaSegura: String): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            var idHijoCreado: Int? = null
            try {
                // 1. Verificar correo primero
                val verificar = api.verificarCorreo(correo = "eq.${datosHijo.correo}")
                if (verificar.isSuccessful && !verificar.body().isNullOrEmpty()) {
                    return@withContext Result.Error("El correo del hijo ya está registrado")
                }
                
                // 2. Generar código de verificación
                val codigoVerificacion = CodeGenerator.generateVerificationCode()
                val codigoExpiracion = CodeGenerator.getExpirationTime()
                
                // 3. Crear el usuario hijo
                val responseHijo = api.crearUsuario(datosHijo)
                if (!responseHijo.isSuccessful || responseHijo.body().isNullOrEmpty()) {
                    return@withContext Result.Error("Error al crear la cuenta del hijo: ${responseHijo.code()}")
                }
                
                val hijo = responseHijo.body()!![0]
                idHijoCreado = hijo.id
                
                // 4. Actualizar con código de verificación
                 val updateData: Map<String, @JvmSuppressWildcards Any> = mapOf(
                    "codigo_verificacion" to codigoVerificacion,
                    "codigo_expiracion" to codigoExpiracion,
                    "email_verificado" to false
                )
                
                val updateResponse = api.actualizarCodigoVerificacion(
                    correo = "eq.${datosHijo.correo}",
                    update = updateData
                )
                
                if (!updateResponse.isSuccessful) {
                    throw Exception("Error al guardar código de verificación")
                }

                // 5. Enviar email con código
                val envioExitoso = EmailService.enviarCodigoVerificacion(
                    correo = datosHijo.correo,
                    codigo = codigoVerificacion,
                    nombre = datosHijo.nombre
                )
                
                if (!envioExitoso) {
                    throw Exception("No se pudo enviar el correo de verificación.")
                }
                
                // 6. Crear la conexión parental
                 val conexion = com.example.rest.data.models.ConexionParental(
                    idPadre = idPadre,
                    idHijo = hijo.id!!,
                    contrasenaSegura = contrasenaSegura
                )
                
                val responseConexion = api.crearConexionParental(conexion)
                
                if (responseConexion.isSuccessful) {
                    // Retornamos el hijo creado, el UI deberá llevar a la pantalla de verificación
                    Result.Success(hijo)
                } else {
                    throw Exception("Error al enlazar la cuenta: ${responseConexion.code()}")
                }
                
            } catch (e: Exception) {
                // Rollback en caso de excepción: borrar usuario creado
                 if (idHijoCreado != null) {
                     try { api.eliminarUsuario("eq.$idHijoCreado") } catch (ex: Exception) { }
                }
                Result.Error("Excepción al crear hijo: ${e.message}")
            }
        }
    }
}
