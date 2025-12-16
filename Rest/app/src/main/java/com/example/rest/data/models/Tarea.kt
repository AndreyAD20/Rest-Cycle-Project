package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para la tabla 'tarea'
 */
data class Tarea(
    @SerializedName("id")
    val id: Int? = null,
    
    @SerializedName("idusuario")
    val idUsuario: Int?,
    
    @SerializedName("titulo")
    val titulo: String,
    
    @SerializedName("descripcion")
    val descripcion: String,
    
    @SerializedName("fechainicio")
    val fechaInicio: String, // Formato: YYYY-MM-DD
    
    @SerializedName("fechafin")
    val fechaFin: String?, // Formato: YYYY-MM-DD
    
    @SerializedName("completada")
    val completada: Boolean,
    
    @SerializedName("tipo")
    val tipo: String // "personal", "escolar", etc.
)
