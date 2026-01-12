package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

data class Horario(
    @SerializedName("id")
    val id: Int? = null,
    
    @SerializedName("iddispositivo")
    val idDispositivo: Int?,
    
    @SerializedName("idmedida")
    val idMedida: Int?,
    
    @SerializedName("horainicio")
    val horaInicio: String?,
    
    @SerializedName("horafin")
    val horaFin: String?
)
