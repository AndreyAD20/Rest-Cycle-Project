package com.example.rest.features.tools

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para la lógica de visualización y formateo de Datos en la UI de Notas.
 */
class NotasUiLogicTest {

    @Test
    fun `formatearFecha deberia retornar string vacio para fecha nula`() {
        val resultado = formatearFecha(null)
        assertEquals("", resultado)
    }

    @Test
    fun `formatearFecha deberia retornar string vacio para fecha en blanco`() {
        val resultado = formatearFecha("")
        assertEquals("", resultado)
        
        val resultadoEspacios = formatearFecha("   ")
        assertEquals("", resultadoEspacios)
    }

    @Test
    fun `formatearFecha deberia fallar graciosamente retornando vacio en formato invalido`() {
        val resultado = formatearFecha("fecha-invalida-123")
        // Segun la implementacion, si falla el parseo, retorna el string vacio
        assertEquals("", resultado)
    }

    @Test
    fun `formatearFecha formatea correctamente fecha ISO con milisegundos y timezone Z`() {
        val fechaIso = "2023-10-15T14:30:00.000Z"
        val resultado = formatearFecha(fechaIso)
        
        // El formato de salida depende del Locale por defecto, pero podemos verificar
        // que ha procesado la fecha y no devuelve vacio, y que contiene el dia
        assertTrue("El resultado no debe estar vacio", resultado.isNotEmpty())
        assertTrue("Deberia contener el dia 15", resultado.contains("15"))
        assertTrue("Deberia contener el mes Oct", resultado.contains("oct", ignoreCase = true) || resultado.contains("oct.", ignoreCase = true))
    }

    @Test
    fun `formatearFecha formatea correctamente fecha ISO postgres`() {
        // Formato tipico que devuelve Supabase: "2024-03-12T05:15:30.123456+00:00"
        val fechaIso = "2024-03-12T05:15:30.123456+00:00"
        val resultado = formatearFecha(fechaIso)
        
        assertTrue("El resultado no debe estar vacio", resultado.isNotEmpty())
        assertTrue("Deberia contener el dia 12", resultado.contains("12"))
    }
}
