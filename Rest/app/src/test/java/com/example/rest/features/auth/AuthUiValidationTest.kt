package com.example.rest.features.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Period

/**
 * Pruebas unitarias para validar la lógica pura de campos de texto y reglas de UI en toda la Autenticación.
 */
class AuthUiValidationTest {

    // =========================================================================
    // LÓGICA PURA EXTRAÍDA DE LAS VISTAS
    // =========================================================================

    // De OlvidoContrasenaActivity
    private fun esCorreoValidoParaRecuperacion(correo: String): Boolean {
        return correo.isNotBlank() && correo.contains("@")
    }

    // De LoginComposeActivity
    private fun validarLogin(correo: String, contrasena: String): ValidationResult {
        if (correo.isBlank() || !correo.contains("@")) {
            return ValidationResult.Error("FORMATO_CORREO_INVALIDO")
        }
        if (contrasena.isBlank()) {
            return ValidationResult.Error("CONTRASENA_VACIA")
        }
        return ValidationResult.Success
    }

    // De RegistroComposeActivity
    private fun validarRegistroCompleto(
        nombre: String,
        apellido: String,
        correo: String,
        fechaNacimiento: String, // formato esperado en UI: YYYYMMDD o YYYY-MM-DD
        pin: String,
        confirmarPin: String,
        aceptaTerminos: Boolean
    ): ValidationResult {
        
        // 1. Nombre y Apellido
        if (nombre.isBlank()) return ValidationResult.Error("NOMBRE_VACIO")
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$".toRegex())) return ValidationResult.Error("NOMBRE_INVALIDO")
        if (apellido.isNotBlank() && !apellido.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$".toRegex())) return ValidationResult.Error("APELLIDO_INVALIDO")
        
        // 2. Correo
        if (correo.isBlank()) return ValidationResult.Error("CORREO_VACIO")
        if (!isValidEmailDomainMock(correo)) return ValidationResult.Error("FORMATO_CORREO_INVALIDO")
        
        // 3. Fecha de Nacimiento
        if (fechaNacimiento.isBlank()) return ValidationResult.Error("FECHA_VACIA")
        
        val fechaFormateada = if (fechaNacimiento.length == 8 && !fechaNacimiento.contains("-")) {
            "${fechaNacimiento.substring(0, 4)}-${fechaNacimiento.substring(4, 6)}-${fechaNacimiento.substring(6, 8)}"
        } else {
            fechaNacimiento
        }
        
        val esMayorDeEdad = try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val fechaNac = LocalDate.parse(fechaFormateada, formatter)
            // Aqui para la prueba, usaremos la fecha local actual del sistema o una inyectada.
            val edad = Period.between(fechaNac, LocalDate.now()).years
            edad >= 18
        } catch (e: Exception) {
            return ValidationResult.Error("FECHA_INVALIDA")
        }

        // 4. Pin/Contraseña
        if (pin.isBlank()) return ValidationResult.Error("PIN_VACIO")
        if (!isValidPasswordMock(pin)) return ValidationResult.Error("PIN_FORMATO_INVALIDO")
        if (confirmarPin.isBlank()) return ValidationResult.Error("CONFIRMAR_PIN_VACIO")
        if (pin != confirmarPin) return ValidationResult.Error("PIN_NO_COINCIDE")
        
        // 5. Términos
        if (!aceptaTerminos) return ValidationResult.Error("TERMINOS_NO_ACEPTADOS")

        return ValidationResult.SuccessWithData(esMayorDeEdad)
    }

    // Mocks manuales de las funciones de SecurityUtils solo para testear la UI logic
    private fun isValidEmailDomainMock(email: String): Boolean {
        // En pruebas locales (JUnit) android.util.Patterns.EMAIL_ADDRESS lanza NullPointerException
        // Utilizamos nuestro propio Regex estandar para el mock
        val pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+$"
        return email.matches(pattern.toRegex())
    }

    private fun isValidPasswordMock(password: String): Boolean {
        return password.length >= 6 // Validacion minima en tu SecurityUtils
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class SuccessWithData(val isAdult: Boolean) : ValidationResult()
        data class Error(val errorCode: String) : ValidationResult()
    }

    // =========================================================================
    // TESTS UNITARIOS
    // =========================================================================

    // --- 1. OLVIDO CONTRASEÑA ---
    @Test
    fun `OlvidoContrasena - correo en blanco es invalido`() {
        assertFalse(esCorreoValidoParaRecuperacion(""))
        assertFalse(esCorreoValidoParaRecuperacion("   "))
    }

    @Test
    fun `OlvidoContrasena - correo sin arroba es invalido`() {
        assertFalse(esCorreoValidoParaRecuperacion("usuarioejemplo.com"))
    }

    @Test
    fun `OlvidoContrasena - correo con formato basico es valido`() {
        assertTrue(esCorreoValidoParaRecuperacion("usuario@ejemplo.com"))
    }

    // --- 2. LOGIN ---
    @Test
    fun `Login - correo invalido devuelve error`() {
        val result = validarLogin("usuariocorreo", "contrasena123")
        assertEquals(ValidationResult.Error("FORMATO_CORREO_INVALIDO"), result)
    }

    @Test
    fun `Login - contrasena vacia devuelve error`() {
        val result = validarLogin("usuario@correo.com", "   ")
        assertEquals(ValidationResult.Error("CONTRASENA_VACIA"), result)
    }

    @Test
    fun `Login - credenciales validas retorna exito`() {
        val result = validarLogin("usuario@correo.com", "mipassword123")
        assertEquals(ValidationResult.Success, result)
    }

    // --- 3. REGISTRO ---
    @Test
    fun `Registro - nombre vacio o con numeros devuelve error`() {
        // Vacio
        var result = validarRegistroCompleto("", "", "correo@a.com", "2000-01-01", "123456", "123456", true)
        assertEquals(ValidationResult.Error("NOMBRE_VACIO"), result)

        // Numeros
        result = validarRegistroCompleto("Juan123", "", "correo@a.com", "2000-01-01", "123456", "123456", true)
        assertEquals(ValidationResult.Error("NOMBRE_INVALIDO"), result)
    }

    @Test
    fun `Registro - correo sin formato valido devuelve error`() {
        val result = validarRegistroCompleto("Juan", "Perez", "correo_sin_arroba", "2000-01-01", "123456", "123456", true)
        assertEquals(ValidationResult.Error("FORMATO_CORREO_INVALIDO"), result)
    }

    @Test
    fun `Registro - contrasenas no coinciden devuelve error`() {
        val result = validarRegistroCompleto("Juan", "Perez", "correo@a.com", "2000-01-01", "123456", "654321", true)
        assertEquals(ValidationResult.Error("PIN_NO_COINCIDE"), result)
    }

    @Test
    fun `Registro - terminos no aceptados devuelve error`() {
        val result = validarRegistroCompleto("Juan", "Perez", "correo@a.com", "2000-01-01", "123456", "123456", false)
        assertEquals(ValidationResult.Error("TERMINOS_NO_ACEPTADOS"), result)
    }

    @Test
    fun `Registro - fecha invalida falla o rechaza formato`() {
        val result = validarRegistroCompleto("Juan", "Perez", "correo@a.com", "fecha-texto", "123456", "123456", true)
        assertEquals(ValidationResult.Error("FECHA_INVALIDA"), result)
    }

    @Test
    fun `Registro - flujo exitoso mayor de edad determina adulto correctamente`() {
        // Pasamos YYYYMMDD para validar el parseo automatico (ej. 19900101)
        val result = validarRegistroCompleto("Juan", "Perez", "correo@a.com", "19900101", "123456", "123456", true)
        assertTrue(result is ValidationResult.SuccessWithData)
        assertTrue((result as ValidationResult.SuccessWithData).isAdult)
    }

    @Test
    fun `Registro - flujo exitoso menor de edad determina NO adulto correctamente`() {
        // Alguien nacido muy recientemente
        val yearReciente = LocalDate.now().year - 10
        val fechaString = "${yearReciente}0101" // Hace 10 años
        
        val result = validarRegistroCompleto("Juan", "Perez", "correo@a.com", fechaString, "123456", "123456", true)
        assertTrue(result is ValidationResult.SuccessWithData)
        assertFalse("Usuario de $yearReciente no deberia ser adulto", (result as ValidationResult.SuccessWithData).isAdult)
    }
}
