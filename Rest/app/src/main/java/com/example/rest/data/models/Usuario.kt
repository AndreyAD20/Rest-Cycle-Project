package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para la tabla 'usuario'
 */
data class Usuario(
    @SerializedName("id")
    val id: Int? = null,
    
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("apellido")
    val apellido: String? = null,
    
    @SerializedName("correo")
    val correo: String,
    
    @SerializedName("telefono")
    val telefono: String,
    
    @SerializedName("fechanacimiento")
    val fechaNacimiento: String, // Formato: YYYY-MM-DD
    
    @SerializedName("contraseña")
    val contraseña: String,
    
    @SerializedName("rol")
    val rol: String, // "padre" o "hijo"
    
    @SerializedName("Mayoredad")
    val mayorEdad: Boolean
)

/**
 * Modelo para login (solo campos necesarios)
 */
data class LoginRequest(
    @SerializedName("correo")
    val correo: String,
    
    @SerializedName("contraseña")
    val contraseña: String
)

/**
 * Modelo para registro (sin ID)
 */
data class RegistroRequest(
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("apellido")
    val apellido: String?,
    
    @SerializedName("correo")
    val correo: String,
    
    @SerializedName("telefono")
    val telefono: String,
    
    @SerializedName("fechanacimiento")
    val fechaNacimiento: String,
    
    @SerializedName("contraseña")
    val contraseña: String,
    
    @SerializedName("rol")
    val rol: String,
    
    @SerializedName("Mayoredad")
    val mayorEdad: Boolean
)
