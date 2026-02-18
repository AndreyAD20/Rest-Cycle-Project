package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de entrada para crear historial de apps (sin ID)
 */
data class HistorialAppInput(
    @SerializedName("iddispositivo")
    val idDispositivo: Int,
    
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("nombre_paquete")
    val nombrePaquete: String,
    
    @SerializedName("categoria")
    val categoria: String? = null,
    
    @SerializedName("tiempo_uso")
    val tiempoUso: Int, // En minutos
    
    @SerializedName("numero_aperturas")
    val numeroAperturas: Int = 1,
    
    @SerializedName("fecha")
    val fecha: String, // Formato: YYYY-MM-DD
    
    @SerializedName("hora_inicio")
    val horaInicio: String? = null,
    
    @SerializedName("hora_fin")
    val horaFin: String? = null,
    
    @SerializedName("fecha_registro")
    val fechaRegistro: String? = null
)
