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
        @Query("select") select: String = "*"
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
     * Obtener datos de verificación de usuario por correo
     */
    @GET("usuario")
    suspend fun obtenerDatosVerificacion(
        @Query("correo") correo: String,
        @Query("select") select: String = "codigo_verificacion,codigo_expiracion,email_verificado"
    ): Response<List<Usuario>>
    
    /**
     * Actualizar código de verificación de usuario
     */
    @PATCH("usuario")
    suspend fun actualizarCodigoVerificacion(
        @Query("correo") correo: String,
        @Body update: Map<String, @JvmSuppressWildcards Any?>
    ): Response<List<Usuario>>
    
    /**
     * Actualizar foto de perfil de usuario
     */
    @PATCH("usuario")
    suspend fun actualizarFotoPerfil(
        @Query("id") id: String,
        @Body update: Map<String, @JvmSuppressWildcards Any?>
    ): Response<List<Usuario>>
    
    /**
     * Marcar usuario como verificado
     */
    @PATCH("usuario")
    suspend fun marcarUsuarioVerificado(
        @Query("correo") correo: String,
        @Body update: Map<String, @JvmSuppressWildcards Any?>
    ): Response<List<Usuario>>
    
    /**
     * Eliminar usuario
     */
    @DELETE("usuario")
    suspend fun eliminarUsuario(
        @Query("id") id: String
    ): Response<Void>

    /**
     * Buscar usuario por código de vinculación (para enlace parental)
     */
    @GET("usuario")
    suspend fun buscarPorCodigoVinculacion(
        @Query("codigo_vinculacion") codigoVinculacion: String,
        @Query("select") select: String = "*"
    ): Response<List<Usuario>>
    
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
        @Body dispositivo: DispositivoInput
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
        @Query("select") select: String = "*",
        @Query("order") order: String? = null,
        @Query("limit") limit: String? = null
    ): Response<List<Nota>>
    
    /**
     * Crear nueva nota
     */
    @POST("notas")
    suspend fun crearNota(
        @Body nota: NotaInput
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
     * Actualizar nota parcialmente (Map)
     */
    @PATCH("notas")
    suspend fun actualizarNotaParcial(
        @Query("id") id: String,
        @Body update: Map<String, @JvmSuppressWildcards Any?>
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
    
    // ==================== DÍAS ====================
    
    /**
     * Obtener todos los días
     */
    @GET("dias")
    suspend fun obtenerDias(): Response<List<Dia>>
    
    // ==================== MEDIDAS ====================
    
    /**
     * Obtener todas las medidas
     */
    @GET("medida")
    suspend fun obtenerMedidas(): Response<List<Medida>>
    
    /**
     * Crear nueva medida
     */
    @POST("medida")
    suspend fun crearMedida(
        @Body medida: Medida
    ): Response<List<Medida>>
    
    // ==================== HORARIOS ====================
    
    /**
     * Obtener horarios de un dispositivo
     */
    @GET("horarios")
    suspend fun obtenerHorariosPorDispositivo(
        @Query("iddispositivo") idDispositivo: String,
        @Query("select") select: String = "*"
    ): Response<List<Horario>>
    
    /**
     * Crear nuevo horario
     */
    @POST("horarios")
    suspend fun crearHorario(
        @Body horario: Horario
    ): Response<List<Horario>>
    
    /**
     * Actualizar horario
     */
    @PATCH("horarios")
    suspend fun actualizarHorario(
        @Query("id") id: String,
        @Body horario: Horario
    ): Response<List<Horario>>
    
    /**
     * Eliminar horario
     */
    @DELETE("horarios")
    suspend fun eliminarHorario(
        @Query("id") id: String
    ): Response<Void>
    
    // ==================== DÍAS HORARIO ====================
    
    /**
     * Obtener días de un horario
     */
    @GET("dias_horarios")
    suspend fun obtenerDiasDeHorario(
        @Query("idhorario") idHorario: String,
        @Query("select") select: String = "*"
    ): Response<List<DiasHorario>>
    
    /**
     * Crear relación día-horario
     */
    @POST("dias_horarios")
    suspend fun crearDiaHorario(
        @Body diaHorario: DiasHorario
    ): Response<List<DiasHorario>>
    
    /**
     * Eliminar relación día-horario por horario y día
     */
    /**
     * Eliminar relación día-horario por horario y día
     */
    @DELETE("dias_horarios")
    suspend fun eliminarDiaHorario(
        @Query("idhorario") idHorario: String,
        @Query("iddia") idDia: String
    ): Response<Void>

    // ==================== EVENTOS (CALENDARIO) ====================
    
    /**
     * Obtener eventos de un usuario
     */
    @GET("eventos")
    suspend fun obtenerEventosPorUsuario(
        @Query("id_usuario") idUsuario: String,
        @Query("select") select: String = "*"
    ): Response<List<Evento>>
    
    /**
     * Crear nuevo evento
     */
    @POST("eventos")
    suspend fun crearEvento(
        @Body evento: EventoInput
    ): Response<List<Evento>>
    
    /**
     * Actualizar evento
     */
    @PATCH("eventos")
    suspend fun actualizarEvento(
        @Query("id") id: String,
        @Body evento: EventoInput
    ): Response<List<Evento>>
    
    /**
     * Eliminar evento
     */
    @DELETE("eventos")
    suspend fun eliminarEvento(
        @Query("id") id: String
    ): Response<Void>

    // ==================== CONEXIÓN PARENTALES ====================

    /**
     * Crear conexión padre-hijo
     */
    @POST("conexion_parentales")
    suspend fun crearConexionParental(
        @Body conexion: ConexionParental
    ): Response<Void>

    /**
     * Obtener conexiones por padre
     */
    @GET("conexion_parentales")
    suspend fun obtenerConexionesPorPadre(
        @Query("idpadre") idPadre: String,
        @Query("select") select: String = "*"
    ): Response<List<ConexionParental>>

    /**
     * Obtener conexiones por hijo
     */
    @GET("conexion_parentales")
    suspend fun obtenerConexionesPorHijo(
        @Query("idhijo") idHijo: String,
        @Query("select") select: String = "*"
    ): Response<List<ConexionParental>>
    // ==================== APPS VINCULADAS (ESTADÍSTICAS) ====================

    /**
     * Obtener apps vinculadas de un dispositivo
     */
    @GET("apps_vinculadas")
    suspend fun obtenerAppsVinculadas(
        @Query("iddispositivo") idDispositivo: String,
        @Query("select") select: String = "*"
    ): Response<List<AppVinculada>>

    /**
     * Crear app vinculada
     */
    @POST("apps_vinculadas")
    suspend fun crearAppVinculada(
        @Body app: AppVinculadaInput
    ): Response<List<AppVinculada>>

    /**
     * Actualizar app vinculada (tiempo uso, limite)
     */
    @PATCH("apps_vinculadas")
    suspend fun actualizarAppVinculada(
        @Query("id") id: String,
        @Body app: Map<String, @JvmSuppressWildcards Any>
    ): Response<List<AppVinculada>>

    /**
     * Actualizar app vinculada por paquete (para sincronizar desde UI local)
     */
    @PATCH("apps_vinculadas")
    suspend fun actualizarAppVinculadaPorPaquete(
        @Query("iddispositivo") idDispositivo: String,
        @Query("nombre_paquete") nombrePaquete: String,
        @Body app: Map<String, @JvmSuppressWildcards Any>
    ): Response<List<AppVinculada>>

    /**
     * Eliminar app vinculada
     */
    @DELETE("apps_vinculadas")
    suspend fun eliminarAppVinculada(
        @Query("iddispositivo") idDispositivo: String,
        @Query("nombre_paquete") nombrePaquete: String
    ): Response<Void>

    // ==================== APPS INSTALADAS ====================

    /**
     * Obtener apps instaladas de un dispositivo
     */
    @GET("apps_instaladas")
    suspend fun obtenerAppsInstaladas(
        @Query("iddispositivo") idDispositivo: String,
        @Query("select") select: String = "*"
    ): Response<List<AppInstalada>>

    /**
     * Obtener apps instaladas por estado de enlazada
     */
    @GET("apps_instaladas")
    suspend fun obtenerAppsInstaladasPorEstado(
        @Query("iddispositivo") idDispositivo: String,
        @Query("enlazada") enlazada: String,
        @Query("select") select: String = "*"
    ): Response<List<AppInstalada>>

    /**
     * Crear app instalada
     */
    @POST("apps_instaladas")
    suspend fun crearAppInstalada(
        @Body app: AppInstaladaInput
    ): Response<List<AppInstalada>>

    /**
     * Actualizar app instalada (para marcar enlazada)
     */
    @PATCH("apps_instaladas")
    suspend fun actualizarAppInstalada(
        @Query("id") id: String,
        @Body update: Map<String, @JvmSuppressWildcards Any>
    ): Response<List<AppInstalada>>

    /**
     * Actualizar app instalada por paquete
     */
    @PATCH("apps_instaladas")
    suspend fun actualizarAppInstaladaPorPaquete(
        @Query("iddispositivo") idDispositivo: String,
        @Query("nombre_paquete") nombrePaquete: String,
        @Body update: Map<String, @JvmSuppressWildcards Any>
    ): Response<List<AppInstalada>>

    /**
     * Upsert apps instaladas (bulk insert/update)
     */
    @POST("apps_instaladas")
    suspend fun upsertAppsInstaladas(
        @Body apps: List<AppInstaladaInput>
    ): Response<List<AppInstalada>>

    // ==================== APPS BLOQUEO ====================

    /**
     * Obtener bloqueos de un dispositivo
     */
    @GET("apps_vinculadas")
    suspend fun obtenerAppsBloqueo(
        @Query("iddispositivo") idDispositivo: String,
        @Query("select") select: String = "*"
    ): Response<List<AppVinculada>>

    /**
     * Crear app en bloqueo
     */
    @POST("apps_vinculadas")
    suspend fun crearAppBloqueo(
        @Body app: AppBloqueoInput
    ): Response<List<AppVinculada>>

    /**
     * Obtener app bloqueo por paquete
     */
    @GET("apps_vinculadas")
    suspend fun obtenerAppBloqueoPorPaquete(
        @Query("iddispositivo") idDispositivo: String,
        @Query("nombre_paquete") nombrePaquete: String,
        @Query("select") select: String = "*"
    ): Response<List<AppVinculada>>
    
    // ==================== HISTORIAL APPS (ESTADÍSTICAS) ====================
    
    /**
     * Obtener historial de apps por dispositivo
     */
    @GET("historial_apps")
    suspend fun obtenerHistorialApps(
        @Query("iddispositivo") idDispositivo: String,
        @Query("select") select: String = "*"
    ): Response<List<HistorialApp>>
    
    /**
     * Obtener historial de apps por dispositivo y fecha
     */
    @GET("historial_apps")
    suspend fun obtenerHistorialAppsPorFecha(
        @Query("iddispositivo") idDispositivo: String,
        @Query("fecha") fecha: String,
        @Query("select") select: String = "*"
    ): Response<List<HistorialApp>>
    
    /**
     * Terminar sesión de app
     */
     // Eliminar sesiones_app endpoints

    /**
     * Obtener historial de una app específica hoy
     */
    @GET("historial_apps")
    suspend fun obtenerHistorialApp(
        @Query("iddispositivo") idDispositivo: String,
        @Query("nombre_paquete") nombrePaquete: String,
        @Query("fecha") fecha: String,
        @Query("select") select: String = "*"
    ): Response<List<HistorialApp>>
    
    /**
     * Registrar uso de app en historial
     */
    @POST("historial_apps")
    suspend fun crearHistorialApp(
        @Body historial: HistorialAppInput
    ): Response<List<HistorialApp>>
    

    
    /**
     * Actualizar registro de historial
     */
    @PATCH("historial_apps")
    suspend fun actualizarHistorialApp(
        @Query("id") id: String,
        @Body update: Map<String, @JvmSuppressWildcards Any>
    ): Response<List<HistorialApp>>

    // ==================== UBICACIONES ====================

    /**
     * Guardar nueva ubicación del hijo
     */
    @POST("ubicaciones")
    suspend fun guardarUbicacion(
        @Body ubicacion: UbicacionInput,
        @Query("on_conflict") onConflict: String = "id_usuario"
    ): Response<List<Ubicacion>>

    /**
     * Guardar una entrada en el historial de ubicaciones (cada hora)
     */
    @POST("historial_ubicacion")
    suspend fun guardarHistorialUbicacion(
        @Body historial: HistorialUbicacionInput
    ): Response<List<HistorialUbicacion>>

    /**
     * Obtener la última ubicación registrada de un usuario (hijo)
     */
    @GET("ubicaciones")
    suspend fun obtenerUltimaUbicacion(
        @Query("id_usuario") idUsuario: String,
        @Query("order") order: String = "timestamp.desc.nullsfirst",
        @Query("limit") limit: String = "1",
        @Query("select") select: String = "*"
    ): Response<List<Ubicacion>>

    /**
     * Obtener el historial completo de ubicaciones de un usuario
     */
    @GET("historial_ubicacion")
    suspend fun obtenerHistorialUbicacion(
        @Query("id_usuario") idUsuario: String,
        @Query("order") order: String = "fecha.desc,hora.desc",
        @Query("select") select: String = "*"
    ): Response<List<HistorialUbicacion>>

    // ==================== COMANDOS CONTROL PARENTAL ====================

    /**
     * Crear un comando de control (ej: solicitar ubicación)
     */
    @POST("comandos_control")
    suspend fun crearComandoControl(
        @Body comando: Map<String, Any>
    ): Response<Any>

    /**
     * Obtener comandos pendientes para el hijo
     */
    @GET("comandos_control")
    suspend fun obtenerComandosPendientes(
        @Query("id_hijo") idHijo: String,
        @Query("estado") estado: String = "eq.PENDIENTE",
        @Query("order") order: String = "fecha_creacion.desc",
        @Query("limit") limit: String = "10"
    ): Response<Any>

    /**
     * Actualizar estado de un comando
     */
    @PATCH("comandos_control")
    suspend fun actualizarComando(
        @Query("id") id: String,
        @Body updates: Map<String, Any>
    ): Response<Any>
}
