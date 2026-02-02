package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

data class Dia(
    @SerializedName("id")
    val id: Int? = null,
    
    @SerializedName("nombre")
    val nombre: String?
)
