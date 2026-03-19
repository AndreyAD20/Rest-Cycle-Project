package com.example.rest.data.models

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Modelo de entrada para crear apps vinculadas (sin ID)
 */
data class AppVinculadaInput(
    @SerializedName("iddispositivo")
    val idDispositivo: Int,
    
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("nombre_paquete")
    val nombrePaquete: String,
    
    @SerializedName("categoria")
    val categoria: String? = null,
    
    @SerializedName("icono_url")
    val iconoUrl: String? = null,
    
    @SerializedName("tiempo_limite")
    val tiempoLimite: Int, // Obligatorio en minutos
    
    @SerializedName("tiempo_usado_hoy")
    val tiempoUsadoHoy: Int = 0,
    
    @SerializedName("bloqueada")
    val bloqueada: Boolean = false,
    
    @SerializedName("activa")
    val activa: Boolean = true,

    @SerializedName("fecha_creacion")
    val fechaCreacion: String? = null,

    @SerializedName("fecha_actualizacion")
    val fechaActualizacion: String? = null
)

/** Helper para obtener el timestamp local actual */
fun localTimestamp(): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        .apply { timeZone = TimeZone.getDefault() }
        .format(Date())
