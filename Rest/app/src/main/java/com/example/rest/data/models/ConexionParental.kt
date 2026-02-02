package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo para la tabla 'conexion_parentales'
 * Enlaza un usuario Padre con un usuario Hijo y guarda una contraseña de seguridad.
 */
data class ConexionParental(
    @SerializedName("idpadre")
    val idPadre: Int,

    @SerializedName("idhijo")
    val idHijo: Int,

    @SerializedName("contrasenasegura")
    val contrasenaSegura: String
)
