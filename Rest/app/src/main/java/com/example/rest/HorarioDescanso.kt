package com.example.rest

/**
 * 📦 MODELO DE DATOS: HorarioDescanso
 *
 * En Kotlin, una "data class" es una clase especial pensada para guardar datos.
 * Android la usa aquí para representar UN horario de descanso creado por el usuario.
 *
 * Piensa en esto como una "ficha" con toda la información de un horario:
 * nombre, hora inicio, hora fin, qué días aplica, si está activo, etc.
 *
 * Cada vez que el usuario crea un horario, se crea UN objeto de este tipo.
 */
data class HorarioDescanso(

    /**
     * Identificador único del horario.
     * Se genera automáticamente con el timestamp actual en milisegundos.
     * Así dos horarios creados al mismo tiempo nunca tendrán el mismo ID.
     */
    val id: Long = System.currentTimeMillis(),

    /**
     * Nombre que el usuario le pone al horario.
     * Ejemplo: "Noche", "Siesta de tarde", "Fin de semana"
     */
    val nombre: String,

    /**
     * Hora a la que empieza el modo descanso.
     * Formato de 24 horas: "HH:mm" → Ejemplo: "22:00" para las 10 PM.
     */
    val horaInicio: String,

    /**
     * Hora a la que termina el modo descanso.
     * Formato de 24 horas: "HH:mm" → Ejemplo: "07:00" para las 7 AM.
     */
    val horaFin: String,

    /**
     * Lista de 7 booleanos que indica qué días de la semana aplica el horario.
     *
     *  Índice:  0      1       2        3        4       5        6
     *  Día:    Lun    Mar      Mie      Jue      Vie     Sab      Dom
     *
     * Si diasActivos[0] == true → el horario aplica los Lunes.
     * Si diasActivos[5] == false → el horario NO aplica los Sábados.
     *
     * Por defecto, todos los días están activos (List(7) { true }).
     */
    val diasActivos: List<Boolean> = List(7) { true },

    /**
     * Indica si el horario está habilitado manualmente por el usuario.
     * El usuario puede desactivarlo con el switch en la tarjeta de la UI.
     * Si activo == false, el servicio ignora este horario aunque la hora coincida.
     */
    val activo: Boolean = true,

    /**
     * Indica si este horario debe activar el "modo sueño" completo:
     * - No Molestar (DND): silencia notificaciones
     * - Escala de grises: pone la pantalla en blanco y negro
     *
     * Por defecto está en true para todos los horarios nuevos.
     */
    val bedtimeMode: Boolean = true
)
