package com.example.rest.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo para solicitud de verificación de código
 */
data class VerificationRequest(
    @SerializedName("correo")
    val correo: String,
    
    @SerializedName("codigo")
    val codigo: String
)

/**
 * Modelo para solicitud de reenvío de código
 */
data class ResendCodeRequest(
    @SerializedName("correo")
    val correo: String
)

/**
 * Modelo para actualización de verificación de usuario
 */
data class VerificationUpdate(
    @SerializedName("email_verificado")
    val emailVerificado: Boolean = true,
    
    @SerializedName("codigo_verificacion")
    val codigoVerificacion: String? = null,
    
    @SerializedName("codigo_expiracion")
    val codigoExpiracion: String? = null
)
