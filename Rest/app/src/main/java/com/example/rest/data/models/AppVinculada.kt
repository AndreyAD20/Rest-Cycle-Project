package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para la tabla 'apps_vinculadas'
 */
data class AppVinculada(
    @SerializedName("id")
    val id: Int? = null,
    
    @SerializedName("iddispositivo")
    val idDispositivo: Int,
    
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("categoria")
    val categoria: String? = null,
    
    @SerializedName("tiempolimite")
    val tiempoLimite: Int? = null, // En minutos
    
    @SerializedName("tiempouso")
    val tiempoUso: Int? = null // En minutos
)
