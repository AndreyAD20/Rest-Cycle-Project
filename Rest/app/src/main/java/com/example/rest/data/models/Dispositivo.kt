package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para la tabla 'dispositivos'
 */
data class Dispositivo(
    @SerializedName("id")
    val id: Int? = null,
    
    @SerializedName("idusuario")
    val idUsuario: Int?,
    
    @SerializedName("nombre")
    val nombre: String?,
    
    @SerializedName("ip")
    val ip: String,
    
    @SerializedName("estado")
    val estado: String? // "activo", "inactivo", etc.
)
