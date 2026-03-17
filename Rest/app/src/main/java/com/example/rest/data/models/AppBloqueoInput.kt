package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

data class AppBloqueoInput(
    @SerializedName("iddispositivo")
    val idDispositivo: Int,
    
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("nombre_paquete")
    val nombrePaquete: String,
    
    @SerializedName("tiempo_limite")
    val tiempoLimite: Int = 0,
    
    @SerializedName("tiempo_usado_hoy")
    val tiempoUsadoHoy: Int = 0,
    
    @SerializedName("bloqueada")
    val bloqueada: Boolean = false,
    
    @SerializedName("bloqueada_por")
    val bloqueadaPor: String = "hijo",
    
    @SerializedName("requiere_password")
    val requierePassword: Boolean = false,
    
    @SerializedName("activa")
    val activa: Boolean = true
)
