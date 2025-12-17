package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para la tabla 'codigos_recuperacion'
 */
data class CodigoRecuperacion(
    @SerializedName("id")
    val id: Int? = null,
    
    @SerializedName("correo")
    val correo: String,
    
    @SerializedName("codigo")
    val codigo: String,
    
    @SerializedName("fecha_creacion")
    val fechaCreacion: String? = null,
    
    @SerializedName("usado")
    val usado: Boolean = false
)

/**
 * Request para crear código de recuperación
 */
data class SolicitarCodigoRequest(
    @SerializedName("correo")
    val correo: String,
    
    @SerializedName("codigo")
    val codigo: String
)

/**
 * Request para cambiar contraseña
 */
data class CambiarContraseñaRequest(
    @SerializedName("contraseña")
    val contraseña: String
)
