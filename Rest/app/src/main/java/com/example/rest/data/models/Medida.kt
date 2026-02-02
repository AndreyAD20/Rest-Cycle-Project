package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

data class Medida(
    @SerializedName("id")
    val id: Int? = null,
    
    @SerializedName("nombre")
    val nombre: String?,
    
    @SerializedName("descripcion")
    val descripcion: String?,
    
    @SerializedName("estado")
    val estado: String?
)
