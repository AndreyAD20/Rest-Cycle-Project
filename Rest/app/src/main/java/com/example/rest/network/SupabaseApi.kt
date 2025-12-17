package com.example.rest.network

import com.example.rest.data.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Interface de API para Supabase
 * Define todos los endpoints REST disponibles
 */
interface SupabaseApi {
    
    // ==================== USUARIO ====================
    
    /**
     * Obtener todos los usuarios
     */
    @GET("usuario")
    suspend fun obtenerUsuarios(): Response<List<Usuario>>
    
    /**
     * Obtener usuario por ID
     */
    @GET("usuario")
    suspend fun obtenerUsuarioPorId(
        @Query("id") id: String,
        @Query("select") select: String = "*"
    ): Response<List<Usuario>>
    
    /**
     * Login - Buscar usuario por correo y contraseña
     */
    @GET("usuario")
    suspend fun login(
        @Query("correo") correo: String,
        @Query("contraseña") contraseña: String,
        @Query("select") select: String = "*"
    ): Response<List<Usuario>>
    
    /**
     * Verificar si existe un correo
     */
    @GET("usuario")
    suspend fun verificarCorreo(
        @Query("correo") correo: String,
        @Query("select") select: String = "id"
    ): Response<List<Usuario>>
    
    /**
     * Crear nuevo usuario (Registro)
     */
    @POST("usuario")
    suspend fun crearUsuario(
        @Body usuario: RegistroRequest
    ): Response<List<Usuario>>
    
    /**
     * Actualizar usuario
     */
    @PATCH("usuario")
    suspend fun actualizarUsuario(
        @Query("id") id: String,
        @Body usuario: Usuario
    ): Response<List<Usuario>>
    
    /**
     * Eliminar usuario
     */
    @DELETE("usuario")
    suspend fun eliminarUsuario(
        @Query("id") id: String
    ): Response<Void>
    
    // ==================== DISPOSITIVOS ====================
    
    /**
     * Obtener dispositivos de un usuario
     */
    @GET("dispositivos")
    suspend fun obtenerDispositivosPorUsuario(
        @Query("idusuario") idUsuario: String,
        @Query("select") select: String = "*"
    ): Response<List<Dispositivo>>
    
    /**
     * Crear nuevo dispositivo
     */
    @POST("dispositivos")
    suspend fun crearDispositivo(
        @Body dispositivo: Dispositivo
    ): Response<List<Dispositivo>>
    
    /**
     * Actualizar dispositivo
     */
    @PATCH("dispositivos")
    suspend fun actualizarDispositivo(
        @Query("id") id: String,
        @Body dispositivo: Dispositivo
    ): Response<List<Dispositivo>>
    
    // ==================== TAREAS ====================
    
    /**
     * Obtener tareas de un usuario
     */
    @GET("tarea")
    suspend fun obtenerTareasPorUsuario(
        @Query("idusuario") idUsuario: String,
        @Query("select") select: String = "*"
    ): Response<List<Tarea>>
    
    /**
     * Crear nueva tarea
     */
    @POST("tarea")
    suspend fun crearTarea(
        @Body tarea: Tarea
    ): Response<List<Tarea>>
    
    /**
     * Actualizar tarea
     */
    @PATCH("tarea")
    suspend fun actualizarTarea(
        @Query("id") id: String,
        @Body tarea: Tarea
    ): Response<List<Tarea>>
    
    /**
     * Eliminar tarea
     */
    @DELETE("tarea")
    suspend fun eliminarTarea(
        @Query("id") id: String
    ): Response<Void>
    
    // ==================== NOTAS ====================
    
    /**
     * Obtener notas de un usuario
     */
    @GET("notas")
    suspend fun obtenerNotasPorUsuario(
        @Query("idusuario") idUsuario: String,
        @Query("select") select: String = "*"
    ): Response<List<Nota>>
    
    /**
     * Crear nueva nota
     */
    @POST("notas")
    suspend fun crearNota(
        @Body nota: Nota
    ): Response<List<Nota>>
    
    /**
     * Actualizar nota
     */
    @PATCH("notas")
    suspend fun actualizarNota(
        @Query("id") id: String,
        @Body nota: Nota
    ): Response<List<Nota>>
    
    /**
     * Eliminar nota
     */
    @DELETE("notas")
    suspend fun eliminarNota(
        @Query("id") id: String
    ): Response<Void>
    
    // ==================== RECUPERACIÓN DE CONTRASEÑA ====================
    
    /**
     * Crear código de recuperación
     */
    @POST("codigos_recuperacion")
    suspend fun crearCodigoRecuperacion(
        @Body request: SolicitarCodigoRequest
    ): Response<List<CodigoRecuperacion>>
    
    /**
     * Verificar código de recuperación
     */
    @GET("codigos_recuperacion")
    suspend fun verificarCodigoRecuperacion(
        @Query("correo") correo: String,
        @Query("codigo") codigo: String,
        @Query("usado") usado: String = "eq.false",
        @Query("select") select: String = "*"
    ): Response<List<CodigoRecuperacion>>
    
    /**
     * Marcar código como usado
     */
    @PATCH("codigos_recuperacion")
    suspend fun marcarCodigoUsado(
        @Query("id") id: String,
        @Body update: Map<String, Boolean>
    ): Response<List<CodigoRecuperacion>>
    
    /**
     * Actualizar contraseña por correo
     */
    @PATCH("usuario")
    suspend fun actualizarContraseñaPorCorreo(
        @Query("correo") correo: String,
        @Body request: CambiarContraseñaRequest
    ): Response<List<Usuario>>
    
    /**
     * Enviar código de recuperación por email (Edge Function)
     */
    @POST("functions/v1/enviar-codigo-recuperacion")
    suspend fun enviarCodigoPorEmail(
        @Body request: Map<String, String>
    ): Response<Map<String, Any>>
}
