package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

data class AppInstaladaInput(
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
    
    @SerializedName("enlazada")
    val enlazada: Boolean = false
)
