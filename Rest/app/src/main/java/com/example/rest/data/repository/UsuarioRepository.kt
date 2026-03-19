package com.example.rest.data.repository

import com.example.rest.R
import com.example.rest.data.models.Usuario
import com.example.rest.data.models.RegistroRequest
import com.example.rest.network.SupabaseAuthClient
import com.example.rest.network.SupabaseClient
import com.example.rest.utils.CodeGenerator
import com.example.rest.utils.EmailService
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
        data class NotVerified(val correo: String) : Result<Nothing>()
        data class Requires2FA(val correo: String, val contrasena: String) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }
    
    /**
     * Realizar login usando Supabase Auth
     * @param context Contexto de la aplicación para guardar preferencias
     * @param correo Correo electrónico del usuario
     * @param contraseña Contraseña del usuario en texto plano
     * @return Result con el usuario si las credenciales son correctas
     */
    suspend fun login(context: android.content.Context, correo: String, contraseña: String): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                // Paso 1: Login con Supabase Auth
                val session = SupabaseAuthClient.auth.signInWith(Email) {
                    email = correo
                    password = contraseña
                }
                
                // Paso 2: Buscar al usuario en nuestra tabla personalizada para obtener sus datos (id, nombre, etc)
                val response = api.verificarCorreo(
                    correo = "eq.$correo",
                    select = "*"
                )
                
                if (response.isSuccessful) {
                    val usuarios = response.body()
                    if (!usuarios.isNullOrEmpty()) {
                        val usuario = usuarios[0]
                        
                        // Guardar datos en preferencias
                        val prefs = com.example.rest.utils.PreferencesManager(context)
                        prefs.saveUserId(usuario.id ?: -1)
                        prefs.saveUserEmail(usuario.correo)
                        prefs.saveUserName(usuario.nombre)
                        prefs.saveMayorEdad(usuario.mayorEdad)
                        
                        Result.Success(usuario)
                    } else {
                        // Si no existe en la tabla, algo falló en la sincronización del registro
                        Result.Error(context.getString(R.string.err_user_not_found))
                    }
                } else {
                    Result.Error(context.getString(R.string.err_server_error_code, response.code()))
                }
            } catch (e: Exception) {
                android.util.Log.e("UsuarioRepository", "Error en login para $correo", e)
                Result.Error(e.message ?: context.getString(R.string.err_invalid_credentials))
            }
        }
    }
    
    /**
     * Enviar código de autenticación de dos pasos (2FA) al correo del usuario
     */
    suspend fun enviarCodigo2FA(context: android.content.Context, correo: String, nombre: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val nuevoCodigo = CodeGenerator.generateVerificationCode()
                val nuevaExpiracion = CodeGenerator.getExpirationTime()

                val updateData: Map<String, @JvmSuppressWildcards Any> = mapOf(
                    "codigo_verificacion" to nuevoCodigo,
                    "codigo_expiracion" to nuevaExpiracion
                )

                val updateResponse = api.actualizarCodigoVerificacion(correo = "eq.$correo", update = updateData)

                if (!updateResponse.isSuccessful) {
                    return@withContext Result.Error(context.getString(R.string.err_code_generation_failure))
                }

                val envioExitoso = EmailService.enviarCodigo2FA(correo = correo, codigo = nuevoCodigo, nombre = nombre)

                if (envioExitoso) {
                    Result.Success(context.getString(R.string.toast_2fa_sent))
                } else {
                    Result.Error(context.getString(R.string.err_email_send_failure))
                }
            } catch (e: Exception) {
                Result.Error(context.getString(R.string.err_network_error_msg, e.message ?: ""))
            }
        }
    }

    /**
     * Reenviar código 2FA si el usuario no lo recibió.
     */
    suspend fun reenviarCodigo2FA(context: android.content.Context, correo: String, nombre: String = "Usuario"): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Generar nuevo código 2FA en lugar del de registro normal
                val nuevoCodigo = CodeGenerator.generateVerificationCode()
                val nuevaExpiracion = CodeGenerator.getExpirationTime()

                val updateData: Map<String, @JvmSuppressWildcards Any> = mapOf(
                    "codigo_verificacion" to nuevoCodigo,
                    "codigo_expiracion" to nuevaExpiracion
                )

                val updateResponse = api.actualizarCodigoVerificacion(correo = "eq.$correo", update = updateData)

                if (!updateResponse.isSuccessful) {
                    return@withContext Result.Error(context.getString(R.string.err_code_generation_failure))
                }

                val envioExitoso = EmailService.enviarCodigo2FA(correo = correo, codigo = nuevoCodigo, nombre = nombre)

                if (envioExitoso) {
                    Result.Success(context.getString(R.string.toast_2fa_resent))
                } else {
                    Result.Error(context.getString(R.string.err_email_send_failure))
                }
            } catch (e: Exception) {
                Result.Error(context.getString(R.string.err_network_error_msg, e.message ?: ""))
            }
        }
    }

    /**
     * Verificar código 2FA y completar login
     */
    suspend fun verificarCodigo2FA(context: android.content.Context, correo: String, codigo: String): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.verificarCorreo(correo = "eq.$correo", select = "*")
                if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                    return@withContext Result.Error(context.getString(R.string.err_user_not_found))
                }

                val usuario = response.body()!![0]
                
                if (usuario.codigoVerificacion != codigo) {
                    return@withContext Result.Error(context.getString(R.string.err_code_incorrect))
                }

                if (usuario.codigoExpiracion != null && com.example.rest.utils.CodeGenerator.isExpired(usuario.codigoExpiracion)) {
                    return@withContext Result.Error(context.getString(R.string.err_code_expired))
                }

                val nuevoToken = java.util.UUID.randomUUID().toString()
                
                val updateResponse = api.actualizarUsuario(id = "eq.${usuario.id}", usuario = usuario.copy(
                    ultimoTokenSesion = nuevoToken,
                    codigoVerificacion = null,
                    codigoExpiracion = null
                ))

                if (updateResponse.isSuccessful) {
                    val prefs = com.example.rest.utils.PreferencesManager(context)
                    prefs.saveSessionToken(nuevoToken)
                    Result.Success(usuario.copy(ultimoTokenSesion = nuevoToken))
                } else {
                    Result.Error(context.getString(R.string.err_server_error_code, updateResponse.code()))
                }
            } catch (e: Exception) {
                Result.Error(context.getString(R.string.err_network_error_msg, e.message ?: ""))
            }
        }
    }

    /**
     * Obtiene o genera el código de vinculación para un hijo.
     * Si no tiene código, genera uno nuevo de 6 caracteres alfanuméricos y lo guarda en Supabase.
     * @param idHijo ID entero del hijo en la tabla `usuario`
     * @return Result con el código de vinculación
     */
    suspend fun obtenerYGenerarCodigoVinculacion(context: android.content.Context, idHijo: Int): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Primero, obtener el usuario actual para ver si ya tiene código
                val response = api.obtenerUsuarioPorId(id = "eq.$idHijo")
                if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                    return@withContext Result.Error(context.getString(R.string.err_user_not_found))
                }
                val usuario = response.body()!![0]
                
                // Si ya tiene código de vinculación vigente, retornarlo
                if (!usuario.codigoVinculacion.isNullOrBlank()) {
                    return@withContext Result.Success(usuario.codigoVinculacion)
                }
                
                // Si no tiene código, generar uno nuevo
                val nuevoCodigoBase = java.util.UUID.randomUUID().toString()
                    .replace("-", "")
                    .uppercase()
                    .take(5)
                
                // Guardarlo en Supabase
                val updateResponse = api.actualizarFotoPerfil(id = "eq.$idHijo", update = mapOf("codigo_vinculacion" to nuevoCodigoBase))
                
                if (updateResponse.isSuccessful) {
                    Result.Success(nuevoCodigoBase)
                } else {
                    Result.Error(context.getString(R.string.err_update_user_failure))
                }
            } catch (e: Exception) {
                Result.Error(context.getString(R.string.err_network_error_msg, e.message ?: ""))
            }
        }
    }

    suspend fun estaVinculado(context: android.content.Context, idHijo: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Verificar directamente si existe un registro en conexion_parentales
                // donde el idhijo corresponda al proporcionado
                val response = api.obtenerConexionesPorHijo(idHijo = "eq.$idHijo")
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    Result.Success(true)
                } else {
                    Result.Success(false)
                }
            } catch (e: Exception) {
                Result.Error(context.getString(R.string.err_network_error_msg, e.message ?: ""))
            }
        }
    }

    /**
     * Obtiene la lista de hijos vinculados a un padre desde `conexion_parentales`.
     * Para cada conexión, busca el usuario hijo y retorna sus datos.
     * @param idPadre ID del padre
     * @return Result con lista de Usuario (los hijos)
     */
    suspend fun obtenerHijosVinculados(context: android.content.Context, idPadre: Int): Result<List<Usuario>> {
        return withContext(Dispatchers.IO) {
            try {
                val conexionesResponse = api.obtenerConexionesPorPadre(idPadre = "eq.$idPadre")
                if (!conexionesResponse.isSuccessful) {
                    return@withContext Result.Error(context.getString(R.string.err_server_error_code, conexionesResponse.code()))
                }
                val conexiones = conexionesResponse.body() ?: emptyList()

                // Para cada conexión, buscar los datos del hijo
                val hijos = mutableListOf<Usuario>()
                for (conexion in conexiones) {
                    val hijoResponse = api.obtenerUsuarioPorId(id = "eq.${conexion.idHijo}")
                    if (hijoResponse.isSuccessful && !hijoResponse.body().isNullOrEmpty()) {
                        hijos.add(hijoResponse.body()!![0])
                    }
                }
                Result.Success(hijos)
            } catch (e: Exception) {
                Result.Error(context.getString(R.string.err_network_error_msg, e.message ?: ""))
            }
        }
    }

    /**
     * Vincula un hijo con el padre usando el código de vinculación del hijo.
     * (1) Busca al hijo por su código de vinculación.
     * (2) Hashea la contraseña parental.
     * (3) Graba en `conexion_parentales`.
     * (4) Borra el código de vinculación del hijo (lo marca como usado).
     * @param idPadre ID del usuario padre.
     * @param codigoVinculacion Código de 5 caracteres del hijo.
     * @param contrasenaParental Contraseña libre que elige el padre (se guarda hasheada).
     * @return Result<Unit>
     */
    suspend fun vincularHijo(context: android.content.Context, idPadre: Int, codigoVinculacion: String, contrasenaParental: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // 1 — Buscar al hijo por su código de vinculación
                // Realizamos la búsqueda directamente con el GET de código vinculado
                val busquedaResponse = api.buscarPorCodigoVinculacion(codigoVinculacion = "eq.${codigoVinculacion.uppercase()}")

                if (!busquedaResponse.isSuccessful || busquedaResponse.body().isNullOrEmpty()) {
                    return@withContext Result.Error(context.getString(R.string.err_vinculacion_invalid))
                }

                val hijo = busquedaResponse.body()!![0]
                val idHijo = hijo.id ?: return@withContext Result.Error(context.getString(R.string.err_server_error_msg, "ID not found"))

                // 2 — No vincular si ya está vinculado con alguien
                val yaVinculado = api.obtenerConexionesPorHijo(idHijo = "eq.$idHijo")
                if (yaVinculado.isSuccessful && !yaVinculado.body().isNullOrEmpty()) {
                    return@withContext Result.Error(context.getString(R.string.err_already_linked))
                }

                // 3 — Hashear la contraseña parental
                val contrasenaHash = com.example.rest.utils.SecurityUtils.hashPassword(contrasenaParental)

                // 4 — Guardar la conexión
                val conexion = com.example.rest.data.models.ConexionParental(
                    idPadre = idPadre,
                    idHijo = idHijo,
                    contrasenaSegura = contrasenaHash
                )
                val saveResponse = api.crearConexionParental(conexion)
                if (!saveResponse.isSuccessful) {
                    return@withContext Result.Error(context.getString(R.string.err_save_conexion_failure))
                }

                // 5 — Borrar el código de vinculación del hijo (marcar como usado)
                api.actualizarFotoPerfil(
                    id = "eq.$idHijo",
                    update = mapOf("codigo_vinculacion" to null, "codigo_vinculacion_expiracion" to null)
                )

                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(context.getString(R.string.err_network_error_msg, e.message ?: ""))
            }
        }
    }

    /**
     * Registrar nuevo usuario usando Supabase Auth
     * El trigger crea automáticamente el usuario en la tabla public.usuario
     * @param request Datos del usuario a registrar
     * @return Result con el usuario creado
     */
    suspend fun registrar(context: android.content.Context, request: RegistroRequest): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Verificar si el correo ya existe en Supabase Auth
                val verificacion = api.verificarCorreo(correo = "eq.${request.correo}")
                if (verificacion.isSuccessful && !verificacion.body().isNullOrEmpty()) {
                    return@withContext Result.Error(context.getString(R.string.err_email_already_registered))
                }
                
                // 2. Crear usuario en Supabase Auth
                // Si no lanza excepción, el correo fue enviado correctamente.
                // El trigger de BD crea la fila en tabla `usuario` DESPUÉS de que
                // el usuario confirme el enlace del correo — no intentar leer la
                // tabla aquí porque siempre estará vacía en este punto.
                SupabaseAuthClient.auth.signUpWith(Email) {
                    email = request.correo
                    password = request.contraseña
                    data = buildJsonObject {
                        put("nombre", JsonPrimitive(request.nombre))
                        put("apellido", JsonPrimitive(request.apellido ?: ""))
                        put("fechanacimiento", JsonPrimitive(request.fechaNacimiento))
                        put("mayor_edad", JsonPrimitive(request.mayorEdad))
                    }
                }

                // 3. signUpWith sin excepción = registro exitoso (pendiente de confirmación).
                //    Devolvemos un objeto Usuario mínimo con los datos del formulario.
                //    RegistroComposeActivity solo necesita saber que fue exitoso para
                //    mostrar el mensaje y volver al login — no usa los campos del objeto.
                Result.Success(
                    Usuario(
                        nombre = request.nombre,
                        apellido = request.apellido,
                        correo = request.correo,
                        fechaNacimiento = request.fechaNacimiento,
                        contraseña = "",          // nunca se persiste desde aquí
                        mayorEdad = request.mayorEdad,
                        emailVerificado = false   // pendiente de confirmación
                    )
                )
            } catch (e: Exception) {
                android.util.Log.e("UsuarioRepository", "Error en registro para ${request.correo}", e)
                Result.Error(e.message ?: context.getString(R.string.err_network_error_msg, ""))
            }
        }
    }
    
    /**
     * Obtener usuario por ID
     * @param id ID del usuario
     * @return Result con el usuario encontrado
     */
    suspend fun obtenerUsuarioPorId(context: android.content.Context, id: Int): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.obtenerUsuarioPorId(id = "eq.$id")
                
                if (response.isSuccessful) {
                    val usuarios = response.body()
                    if (!usuarios.isNullOrEmpty()) {
                        Result.Success(usuarios[0])
                    } else {
                        Result.Error(context.getString(R.string.err_user_not_found))
                    }
                } else {
                    Result.Error(context.getString(R.string.err_server_error_code, response.code()))
                }
            } catch (e: Exception) {
                Result.Error(context.getString(R.string.err_network_error_msg, e.message ?: ""))
            }
        }
    }
    
    /**
     * Registrar nuevo usuario con verificación de email
     * Genera un código de verificación y lo envía por email
     * @param request Datos del usuario a registrar
     * @return Result con mensaje de éxito y el código (para desarrollo)
     */
    suspend fun registrarConVerificacion(context: android.content.Context, request: RegistroRequest): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Verificar si el correo ya existe en la tabla usuario
                val verificacion = api.verificarCorreo(correo = "eq.${request.correo}")
                if (verificacion.isSuccessful && !verificacion.body().isNullOrEmpty()) {
                    return@withContext Result.Error(context.getString(R.string.err_email_already_registered))
                }
                
                // 2. Generar código de verificación
                val codigoVerificacion = CodeGenerator.generateVerificationCode()
                val codigoExpiracion = CodeGenerator.getExpirationTime()
                
                // 3. Crear usuario en la base de datos (sin verificar)
                val response = api.crearUsuario(request)
                
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("UsuarioCtx", "Error crearUsuario: $errorBody")
                    return@withContext Result.Error(context.getString(R.string.err_server_error_code, response.code()))
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
                    return@withContext Result.Error(context.getString(R.string.err_code_generation_failure))
                }
                
                // 5. Enviar email con código
                val envioExitoso = EmailService.enviarCodigoVerificacion(
                    correo = request.correo,
                    codigo = codigoVerificacion,
                    nombre = request.nombre
                )
                
                if (!envioExitoso) {
                    return@withContext Result.Error(context.getString(R.string.err_email_send_detailed))
                }
                
                // Mensaje sin mostrar el código
                Result.Success(context.getString(R.string.toast_register_success))
            } catch (e: Exception) {
                Result.Error(context.getString(R.string.err_network_error_msg, e.message ?: ""))
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
    suspend fun actualizarUsuario(context: android.content.Context, id: Int, usuario: Usuario): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.actualizarUsuario(id = "eq.$id", usuario = usuario)
                
                if (response.isSuccessful) {
                    val usuarios = response.body()
                    if (!usuarios.isNullOrEmpty()) {
                        Result.Success(usuarios[0])
                    } else {
                        Result.Error(context.getString(R.string.err_update_user_failure))
                    }
                } else {
                    Result.Error(context.getString(R.string.err_server_error_code, response.code()))
                }
            } catch (e: Exception) {
                Result.Error(context.getString(R.string.err_network_error_msg, e.message ?: ""))
            }
        }
    }
    
    /**
     * Verificar código de verificación
     * @param correo Correo del usuario
     * @param codigo Código ingresado por el usuario
     * @return Result con el usuario verificado
     */
    suspend fun verificarCodigo(context: android.content.Context, correo: String, codigo: String): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Obtener datos de verificación del usuario
                val response = api.obtenerDatosVerificacion(correo = "eq.$correo", select = "*")
                
                if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                    return@withContext Result.Error(context.getString(R.string.err_user_not_found))
                }
                
                val usuario = response.body()!![0]
                
                // 2. Verificar que el usuario no esté ya verificado
                if (usuario.emailVerificado) {
                    return@withContext Result.Error(context.getString(R.string.err_user_already_verified))
                }
                
                // 3. Verificar que exista un código
                if (usuario.codigoVerificacion.isNullOrBlank()) {
                    return@withContext Result.Error(context.getString(R.string.err_no_pending_code))
                }
                
                // 4. Verificar que el código coincida
                if (usuario.codigoVerificacion != codigo) {
                    return@withContext Result.Error(context.getString(R.string.err_code_incorrect))
                }
                
                // 5. Verificar que no haya expirado
                if (usuario.codigoExpiracion != null && CodeGenerator.isExpired(usuario.codigoExpiracion)) {
                    return@withContext Result.Error(context.getString(R.string.err_code_expired))
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
                    Result.Error(context.getString(R.string.err_update_user_failure))
                }
            } catch (e: Exception) {
                Result.Error(context.getString(R.string.err_network_error_msg, e.message ?: ""))
            }
        }
    }
    
    /**
     * Reenviar código de verificación de email
     */
    suspend fun reenviarCodigo(context: android.content.Context, correo: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Verificar que el usuario existe
                val response = api.verificarCorreo(correo = "eq.$correo", select = "*")
                if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                    return@withContext Result.Error(context.getString(R.string.err_user_not_found))
                }
                val usuario = response.body()!![0]
                
                // 2. Generar nuevo código
                val nuevoCodigo = CodeGenerator.generateVerificationCode()
                val nuevaExpiracion = CodeGenerator.getExpirationTime()
                
                // 3. Actualizar en DB
                val updateData: Map<String, @JvmSuppressWildcards Any> = mapOf(
                    "codigo_verificacion" to nuevoCodigo,
                    "codigo_expiracion" to nuevaExpiracion
                )
                
                val updateResponse = api.actualizarCodigoVerificacion(
                    correo = "eq.$correo",
                    update = updateData
                )
                
                if (!updateResponse.isSuccessful) {
                    return@withContext Result.Error(context.getString(R.string.err_code_generation_failure))
                }
                
                // 4. Enviar email
                val envioExitoso = EmailService.enviarCodigoVerificacion(
                    correo = correo,
                    codigo = nuevoCodigo,
                    nombre = usuario.nombre
                )
                
                if (envioExitoso) {
                    Result.Success(context.getString(R.string.toast_code_resent_success))
                } else {
                    Result.Error(context.getString(R.string.err_email_send_detailed))
                }
            } catch (e: Exception) {
                Result.Error(context.getString(R.string.err_network_error_msg, e.message ?: ""))
            }
        }
    }
    
    /**
     * Enviar email de recuperación de contraseña de Supabase
     */
    suspend fun recuperarContrasena(context: android.content.Context, correo: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseAuthClient.auth.resetPasswordForEmail(
                    email = correo,
                    redirectUrl = "com.example.rest://login"
                )
                Result.Success(context.getString(R.string.recovery_check_email_msg))
            } catch (e: Exception) {
                Result.Error(e.message ?: context.getString(R.string.err_network_error_msg, ""))
            }
        }
    }
    
    /**
     * Cerrar sesión en Supabase y limpiar preferencias locales
     */
    suspend fun cerrarSesion(context: android.content.Context): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseAuthClient.logout()
                val prefs = com.example.rest.utils.PreferencesManager(context)
                prefs.clearPreferences()
                Result.Success(Unit)
            } catch (e: Exception) {
                // Limpiar preferencias aunque falle la red
                com.example.rest.utils.PreferencesManager(context).clearPreferences()
                Result.Success(Unit)
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
    suspend fun actualizarUsuarioVerificado(context: android.content.Context, authUserId: String, correo: String): Result<Usuario> {
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
                        Result.Error(context.getString(R.string.err_update_user_failure))
                    }
                } else {
                    Result.Error(context.getString(R.string.err_user_not_found))
                }
            } catch (e: Exception) {
                Result.Error(context.getString(R.string.err_network_error_msg, e.message ?: ""))
            }
        }
    }
    
    /**
     * Crear un usuario hijo, generar código de verificación, enviar email y enlazarlo con el padre.
     * @param context Contexto para localización
     * @param idPadre ID del usuario padre
     * @param datosHijo Datos del hijo para registro
     * @param contrasenaSegura Contraseña de seguridad para la conexión
     */
    suspend fun crearHijo(context: android.content.Context, idPadre: Int, datosHijo: RegistroRequest, contrasenaSegura: String): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            var idHijoCreado: Int? = null
            try {
                // 1. Verificar correo primero
                val verificar = api.verificarCorreo(correo = "eq.${datosHijo.correo}")
                if (verificar.isSuccessful && !verificar.body().isNullOrEmpty()) {
                    return@withContext Result.Error(context.getString(R.string.err_email_already_registered))
                }
                
                // 2. Generar código de verificación
                val codigoVerificacion = CodeGenerator.generateVerificationCode()
                val codigoExpiracion = CodeGenerator.getExpirationTime()
                
                // 3. Crear el usuario hijo
                val responseHijo = api.crearUsuario(datosHijo)
                if (!responseHijo.isSuccessful || responseHijo.body().isNullOrEmpty()) {
                    return@withContext Result.Error(context.getString(R.string.err_create_user_failure))
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
                    throw Exception("Error saving verification code")
                }

                // 5. Enviar email con código
                val envioExitoso = EmailService.enviarCodigoVerificacion(
                    correo = datosHijo.correo,
                    codigo = codigoVerificacion,
                    nombre = datosHijo.nombre
                )
                
                if (!envioExitoso) {
                    throw Exception("Could not send email")
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
                    throw Exception("Error linking account")
                }
                
            } catch (e: Exception) {
                // Rollback en caso de excepción: borrar usuario creado
                 if (idHijoCreado != null) {
                      try { api.eliminarUsuario("eq.$idHijoCreado") } catch (ex: Exception) { }
                }
                Result.Error(context.getString(R.string.err_network_error_msg, e.message ?: ""))
            }
        }
    }
    /**
     * Actualizar foto de perfil del usuario
     * @param context Contexto para localización
     * @param userId ID del usuario
     * @param fotoBase64 Imagen en formato Base64 (null para eliminar)
     * @return Result indicando éxito o error
     */
    suspend fun actualizarFotoPerfil(context: android.content.Context, userId: Int, fotoBase64: String?): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val updateData = mapOf("foto_perfil" to fotoBase64)
                val response = api.actualizarFotoPerfil(
                    id = "eq.$userId",
                    update = updateData
                )
                
                if (response.isSuccessful) {
                    Result.Success(true)
                } else {
                    Result.Error(context.getString(R.string.err_update_user_failure))
                }
            } catch (e: Exception) {
                Result.Error(context.getString(R.string.err_network_error_msg, e.message ?: ""))
            }
        }
    }
    /**
     * Obtener usuario por correo electrónico (usado en Deep Links)
     */
    suspend fun obtenerUsuarioPorCorreo(correo: String): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.verificarCorreo(correo = "eq.$correo", select = "*")
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    Result.Success(response.body()!![0])
                } else {
                    Result.Error("Usuario no encontrado")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error de conexión")
            }
        }
    }
}
