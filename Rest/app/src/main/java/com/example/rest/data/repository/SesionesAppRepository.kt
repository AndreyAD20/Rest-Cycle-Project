package com.example.rest.data.repository

import com.example.rest.data.models.SesionApp
import com.example.rest.network.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repositorio para gestionar sesiones de uso de apps
 */
class SesionesAppRepository {
    
    private val api = SupabaseClient.api
    
    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }
    
    /**
     * Iniciar nueva sesión de app
     */
    suspend fun iniciarSesion(
        idDispositivo: Int,
        nombrePaquete: String
    ): Result<SesionApp> {
        return withContext(Dispatchers.IO) {
            try {
                val ahora = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .format(Date())
                
                val sesion = com.example.rest.data.models.SesionAppInput(
                    idDispositivo = idDispositivo,
                    nombrePaquete = nombrePaquete,
                    inicio = ahora,
                    activa = true
                )
                
                val response = api.iniciarSesion(sesion)
                
                if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                    Result.Success(response.body()!![0])
                } else {
                    Result.Error("Error al iniciar sesión: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Excepción: ${e.message}")
            }
        }
    }
    
    /**
     * Finalizar sesión de app
     */
    suspend fun finalizarSesion(idSesion: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val ahora = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .format(Date())
                
                // Primero obtener la sesión para calcular duración
                val sesiones = api.obtenerSesiones(
                    idDispositivo = "*",
                    select = "*"
                )
                
                val sesion = sesiones.body()?.find { it.id == idSesion }
                
                if (sesion != null) {
                    // Calcular duración en segundos
                    val formatoFecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val inicio = formatoFecha.parse(sesion.inicio)
                    val fin = formatoFecha.parse(ahora)
                    val duracionSegundos = ((fin.time - inicio.time) / 1000).toInt()
                    
                    val update = mapOf(
                        "fin" to ahora,
                        "duracion" to duracionSegundos,
                        "activa" to false
                    )
                    
                    val response = api.finalizarSesion(
                        id = "eq.$idSesion",
                        update = update
                    )
                    
                    if (response.isSuccessful) {
                        Result.Success(true)
                    } else {
                        Result.Error("Error al finalizar sesión: ${response.code()}")
                    }
                } else {
                    Result.Error("Sesión no encontrada")
                }
            } catch (e: Exception) {
                Result.Error("Excepción: ${e.message}")
            }
        }
    }
    
    /**
     * Obtener sesiones activas de un dispositivo
     */
    suspend fun obtenerSesionesActivas(idDispositivo: Int): Result<List<SesionApp>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.obtenerSesionesActivas(
                    idDispositivo = "eq.$idDispositivo"
                )
                
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    Result.Error("Error al obtener sesiones activas: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Excepción: ${e.message}")
            }
        }
    }
    
    /**
     * Obtener todas las sesiones de un dispositivo
     */
    suspend fun obtenerSesiones(idDispositivo: Int): Result<List<SesionApp>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.obtenerSesiones(
                    idDispositivo = "eq.$idDispositivo"
                )
                
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    Result.Error("Error al obtener sesiones: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Excepción: ${e.message}")
            }
        }
    }
    
    /**
     * Obtener sesiones por fecha
     */
    suspend fun obtenerSesionesPorFecha(
        idDispositivo: Int,
        fecha: String
    ): Result<List<SesionApp>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.obtenerSesionesPorFecha(
                    idDispositivo = "eq.$idDispositivo",
                    inicio = "gte.$fecha"
                )
                
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    Result.Error("Error al obtener sesiones: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Excepción: ${e.message}")
            }
        }
    }
    
    /**
     * Finalizar todas las sesiones activas de un dispositivo
     * Útil cuando el dispositivo se apaga o la app se cierra
     */
    suspend fun finalizarTodasSesionesActivas(idDispositivo: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val sesionesResult = obtenerSesionesActivas(idDispositivo)
                
                if (sesionesResult is Result.Success) {
                    sesionesResult.data.forEach { sesion ->
                        sesion.id?.let { finalizarSesion(it) }
                    }
                    Result.Success(true)
                } else {
                    Result.Error("Error al obtener sesiones activas")
                }
            } catch (e: Exception) {
                Result.Error("Excepción: ${e.message}")
            }
        }
    }
}
