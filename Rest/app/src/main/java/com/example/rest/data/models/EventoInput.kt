package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo para CREAR o ACTUALIZAR un evento (POST/PATCH a Supabase).
 * NO incluye campos opcionales nulos (latitud, longitud, ubicacion_nombre)
 * cuando no tienen valor, para evitar el error 23502 (NOT NULL violation)
 * causado por serializeNulls() en el cliente Gson.
 */
data class EventoInput(
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("fecha_inicio") val fechaInicio: String,
    @SerializedName("fecha_fin") val fechaFin: String,
    @SerializedName("latitud") val latitud: Double? = null,
    @SerializedName("longitud") val longitud: Double? = null,
    @SerializedName("ubicacion_nombre") val ubicacionNombre: String? = null
)
