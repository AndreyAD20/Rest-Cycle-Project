package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

data class DiasHorario(
    @SerializedName("idhorario")
    val idHorario: Int?,
    
    @SerializedName("iddia")
    val idDia: Int?
)
