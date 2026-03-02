package com.example.rest.utils

import org.mindrot.jbcrypt.BCrypt

object SecurityUtils {

    /**
     * Aplica hashing a una contraseña de texto plano usando BCrypt.
     * @param plainTextPassword La contraseña sin encriptar.
     * @return El hash BCrypt resultante.
     */
    fun hashPassword(plainTextPassword: String): String {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt())
    }

    /**
     * Verifica si una contraseña de texto plano coincide con un hash BCrypt.
     * @param plainTextPassword La contraseña ingresada por el usuario al hacer login.
     * @param hashedPassword El hash guardado en la base de datos.
     * @return true si coinciden, false en caso contrario.
     */
    fun verifyPassword(plainTextPassword: String, hashedPassword: String): Boolean {
        return try {
            BCrypt.checkpw(plainTextPassword, hashedPassword)
        } catch (e: Exception) {
            false // En caso de que hashedPassword no tenga formato de bcrypt válido
        }
    }

    /**
     * Verifica si una contraseña cumple con las reglas de seguridad:
     * - Mínimo 8 caracteres
     * - Al menos 1 letra mayúscula
     * - Al menos 1 número
     */
    fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        
        var hasUppercase = false
        var hasNumber = false
        
        for (char in password) {
            if (char.isUpperCase()) hasUppercase = true
            if (char.isDigit()) hasNumber = true
        }
        
        return hasUppercase && hasNumber
    }

    /**
     * Verifica si el dominio del correo es uno de los proveedores comunes permitidos.
     * Esto evita errores de digitación como "@gmail.comhila".
     */
    fun isValidEmailDomain(email: String): Boolean {
        // Expresión regular estructurada básica primero
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex())) {
            return false
        }
        
        val dominio = email.substringAfterLast("@").lowercase()
        val dominiosPermitidos = listOf(
            "gmail.com",
            "hotmail.com", "hotmail.es", "hotmail.co",
            "outlook.com", "outlook.es", "outlook.co",
            "yahoo.com", "yahoo.es", "yahoo.co",
            "icloud.com",
            "live.com", "live.com.mx",
            "msn.com"
        )
        
        // Verifica si está en la lista exacta, O si es un subdominio institucional/educativo (ej: uptc.edu.co)
        return dominiosPermitidos.contains(dominio) || 
               dominio.endsWith(".edu.co") || 
               dominio.endsWith(".gov.co") || 
               dominio.endsWith(".edu") ||
               dominio.endsWith(".org.co") || 
               dominio.endsWith(".com.co")
    }
}
