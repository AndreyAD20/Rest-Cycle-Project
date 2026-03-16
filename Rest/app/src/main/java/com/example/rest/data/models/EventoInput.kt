package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 cristian-alvarado
 * Modelo para CREAR o ACTUALIZAR un evento (POST/PATCH a Supabase).
 * NO incluye campos opcionales nulos (latitud, longitud, ubicacion_nombre)
 * cuando no tienen valor, para evitar el error 23502 (NOT NULL violation)
 * causado por serializeNulls() en el cliente Gson.
=======
 * Modelo de entrada para crear un evento (sin ID)
main
 */
data class EventoInput(
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("titulo") val titulo: String,
 cristian-alvarado
    @SerializedName("tipo") val tipo: String,
    @SerializedName("fecha_inicio") val fechaInicio: String,
    @SerializedName("fecha_fin") val fechaFin: String,
=======
    @SerializedName("tipo") val tipo: String, // 'Reunión', 'Trabajo', 'Salud', etc.
    @SerializedName("fecha_inicio") val fechaInicio: String, // ISO 8601 Timestamp
    @SerializedName("fecha_fin") val fechaFin: String, // ISO 8601 Timestamp
 main
    @SerializedName("latitud") val latitud: Double? = null,
    @SerializedName("longitud") val longitud: Double? = null,
    @SerializedName("ubicacion_nombre") val ubicacionNombre: String? = null
)
