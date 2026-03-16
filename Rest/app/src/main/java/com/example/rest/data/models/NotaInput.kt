package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de entrada para crear una nota (sin ID)
 */
data class NotaInput(
    @SerializedName("idusuario")
    val idUsuario: Int,
    
    @SerializedName("titulo")
    val titulo: String,
    
    @SerializedName("contenido")
    val contenido: String,

    @SerializedName("color")
    val color: String = "#FFFFFF",

    @SerializedName("fecha_actualizacion")
    val fecha_actualizacion: String,

    @SerializedName("favorito")
    val favorito: Boolean = false
)
