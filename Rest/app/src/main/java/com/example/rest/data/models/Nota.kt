package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para la tabla 'notas'
 */
data class Nota(
    @SerializedName("id")
    val id: Int? = null,
    
    @SerializedName("idusuario")
    val idUsuario: Int?,
    
    @SerializedName("titulo")
    val titulo: String?,
    
    @SerializedName("contenido")
    val contenido: String?
)
