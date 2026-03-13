package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de Ubicación del hijo
 * Almacena la posición GPS enviada cada hora
 */
data class Ubicacion(
    val id: Int = 0,
    @SerializedName("id_usuario") val idUsuario: Int,
    val latitud: Double,
    val longitud: Double,
    val timestamp: String = ""
)

/**
 * Payload para insertar una nueva ubicación
 */
data class UbicacionInput(
    @SerializedName("id_usuario") val idUsuario: Int,
    val latitud: Double,
    val longitud: Double
)

/**
 * Payload para insertar en el historial de ubicaciones (historial_ubicacion)
 */
data class HistorialUbicacionInput(
    @SerializedName("id_usuario") val idUsuario: Int,
    val latitud: Double,
    val longitud: Double,
    val fecha: String, // YYYY-MM-DD
    val hora: String   // HH:MM:SS
)

/**
 * Modelo para representar una entrada en el historial de ubicaciones
 */
data class HistorialUbicacion(
    val id: Int = 0,
    @SerializedName("id_usuario") val idUsuario: Int,
    val latitud: Double,
    val longitud: Double,
    val fecha: String,
    val hora: String,
    @SerializedName("id_ubicacion") val idUbicacion: Int? = null
)
