package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para la tabla 'usuario'
 */
data class Usuario(
    @SerializedName("id")
    val id: Int? = null,
    
    @SerializedName("auth_user_id")
    val authUserId: String? = null,
    
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("apellido")
    val apellido: String? = null,
    
    @SerializedName("correo")
    val correo: String,
    
    @SerializedName("fechanacimiento")
    val fechaNacimiento: String, // Formato: YYYY-MM-DD
    
    @SerializedName("contraseña")
    val contraseña: String,
    
    @SerializedName("mayoredad")
    val mayorEdad: Boolean = false,
    
    @SerializedName("email_verificado")
    val emailVerificado: Boolean = false,
    
    @SerializedName("codigo_verificacion")
    val codigoVerificacion: String? = null,
    
    @SerializedName("codigo_expiracion")
    val codigoExpiracion: String? = null,

    @SerializedName("foto_perfil")
    val fotoPerfil: String? = null,
    
    @SerializedName("ultimo_token_sesion")
    val ultimoTokenSesion: String? = null,
    
    @SerializedName("codigo_vinculacion")
    val codigoVinculacion: String? = null,
    
    @SerializedName("codigo_vinculacion_expiracion")
    val codigoVinculacionExpiracion: String? = null
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
    
    @SerializedName("fechanacimiento")
    val fechaNacimiento: String,
    
    @SerializedName("contraseña")
    val contraseña: String,
    
    @SerializedName("mayoredad")
    val mayorEdad: Boolean
)
