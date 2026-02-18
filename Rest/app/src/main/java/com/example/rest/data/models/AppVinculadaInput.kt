package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

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
    
    @SerializedName("tiempolimite")
    val tiempoLimite: Int, // Obligatorio en minutos
    
    @SerializedName("tiempo_usado_hoy")
    val tiempoUsadoHoy: Int = 0,
    
    @SerializedName("bloqueada")
    val bloqueada: Boolean = false,
    
    @SerializedName("activa")
    val activa: Boolean = true
)
