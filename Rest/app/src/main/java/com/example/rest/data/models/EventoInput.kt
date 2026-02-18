package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de entrada para crear un evento (sin ID)
 */
data class EventoInput(
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("tipo") val tipo: String, // 'Reunión', 'Trabajo', 'Salud', etc.
    @SerializedName("fecha_inicio") val fechaInicio: String, // ISO 8601 Timestamp
    @SerializedName("fecha_fin") val fechaFin: String, // ISO 8601 Timestamp
    @SerializedName("latitud") val latitud: Double? = null,
    @SerializedName("longitud") val longitud: Double? = null,
    @SerializedName("ubicacion_nombre") val ubicacionNombre: String? = null
)
