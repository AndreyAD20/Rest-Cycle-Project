package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de entrada para crear un dispositivo (sin ID)
 */
data class DispositivoInput(
    @SerializedName("idusuario")
    val idUsuario: Int,
    
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("ip")
    val ip: String,
    
    @SerializedName("estado")
    val estado: String
)
