package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para la tabla 'sesiones_app'
 * Sesiones individuales de uso de apps
 */
data class SesionApp(
    @SerializedName("id")
    val id: Int? = null,
    
    @SerializedName("iddispositivo")
    val idDispositivo: Int,
    
    @SerializedName("nombre_paquete")
    val nombrePaquete: String,
    
    @SerializedName("inicio")
    val inicio: String, // Timestamp ISO 8601
    
    @SerializedName("fin")
    val fin: String? = null, // Timestamp ISO 8601
    
    @SerializedName("duracion")
    val duracion: Int? = null, // En segundos
    
    @SerializedName("activa")
    val activa: Boolean = true
)

/**
 * DTO para crear sesión sin enviar ID nulo
 */
data class SesionAppInput(
    @SerializedName("iddispositivo")
    val idDispositivo: Int,
    
    @SerializedName("nombre_paquete")
    val nombrePaquete: String,
    
    @SerializedName("inicio")
    val inicio: String,
    
    @SerializedName("fin")
    val fin: String? = null,
    
    @SerializedName("duracion")
    val duracion: Int? = null,
    
    @SerializedName("activa")
    val activa: Boolean = true
)
