package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para la tabla 'apps_vinculadas'
 * Apps con límites de tiempo configurados (control parental)
 */
data class AppVinculada(
    @SerializedName("id")
    val id: Int? = null,
    
    @SerializedName("iddispositivo")
    val idDispositivo: Int,
    
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("nombre_paquete")
    val nombrePaquete: String? = null,
    
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
    val activa: Boolean = true,
    
    @SerializedName("fecha_creacion")
    val fechaCreacion: String? = null,
    
    @SerializedName("fecha_actualizacion")
    val fechaActualizacion: String? = null,
    
    @SerializedName("bloqueada_por")
    val bloqueadaPor: String = "hijo",
    
    @SerializedName("requiere_password")
    val requierePassword: Boolean = false
)
