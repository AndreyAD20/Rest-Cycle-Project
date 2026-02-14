package com.example.rest.data.repository

import com.example.rest.data.models.HistorialApp
import com.example.rest.network.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repositorio para gestionar el historial de uso de apps
 */
class HistorialAppsRepository {
    
    private val api = SupabaseClient.api
    
    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }
    
    /**
     * Obtener historial de apps por dispositivo
     */
    suspend fun obtenerHistorialPorDispositivo(idDispositivo: Int): Result<List<HistorialApp>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.obtenerHistorialApps(
                    idDispositivo = "eq.$idDispositivo"
                )
                
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    Result.Error("Error al obtener historial: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Excepción: ${e.message}")
            }
        }
    }
    
    /**
     * Obtener historial de apps por dispositivo y fecha
     */
    suspend fun obtenerHistorialPorFecha(
        idDispositivo: Int,
        fecha: String
    ): Result<List<HistorialApp>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.obtenerHistorialAppsPorFecha(
                    idDispositivo = "eq.$idDispositivo",
                    fecha = "eq.$fecha"
                )
                
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    Result.Error("Error al obtener historial: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Excepción: ${e.message}")
            }
        }
    }
    
    /**
     * Registrar uso de app en historial
     */
    suspend fun registrarUsoApp(historial: HistorialApp): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.registrarUsoApp(historial)
                
                if (response.isSuccessful) {
                    Result.Success(true)
                } else {
                    Result.Error("Error al registrar uso: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Excepción: ${e.message}")
            }
        }
    }
    
    /**
     * Obtener top 5 apps más usadas en los últimos N días
     */
    suspend fun obtenerTop5Apps(
        idDispositivo: Int,
        dias: Int = 7
    ): Result<List<Pair<String, Int>>> {
        return withContext(Dispatchers.IO) {
            try {
                // Calcular fecha de inicio
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -dias)
                val fechaInicio = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(calendar.time)
                
                val response = api.obtenerHistorialApps(
                    idDispositivo = "eq.$idDispositivo"
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val historial = response.body()!!
                    
                    // Filtrar por fecha y agrupar por nombre
                    val appsAgrupadas = historial
                        .filter { it.fecha >= fechaInicio }
                        .groupBy { it.nombre }
                        .mapValues { entry -> entry.value.sumOf { it.tiempoUso } }
                        .toList()
                        .sortedByDescending { it.second }
                        .take(5)
                    
                    Result.Success(appsAgrupadas)
                } else {
                    Result.Error("Error al obtener top apps: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Excepción: ${e.message}")
            }
        }
    }
    
    /**
     * Obtener estadísticas semanales
     */
    suspend fun obtenerEstadisticasSemanales(
        idDispositivo: Int
    ): Result<Map<String, Int>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.obtenerHistorialApps(
                    idDispositivo = "eq.$idDispositivo"
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val historial = response.body()!!
                    
                    // Obtener últimos 7 días
                    val calendar = Calendar.getInstance()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val estadisticas = mutableMapOf<String, Int>()
                    
                    for (i in 6 downTo 0) {
                        calendar.time = Date()
                        calendar.add(Calendar.DAY_OF_YEAR, -i)
                        val fecha = dateFormat.format(calendar.time)
                        
                        val tiempoTotal = historial
                            .filter { it.fecha == fecha }
                            .sumOf { it.tiempoUso }
                        
                        estadisticas[fecha] = tiempoTotal
                    }
                    
                    Result.Success(estadisticas)
                } else {
                    Result.Error("Error al obtener estadísticas: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Excepción: ${e.message}")
            }
        }
    }
    
    /**
     * Obtener estadísticas de las últimas 3 semanas del calendario (lunes a domingo)
     * Retorna un mapa con el formato "Semana 1", "Semana 2", "Semana 3" -> tiempo total en minutos
     */
    suspend fun obtenerEstadisticasPorSemanas(
        idDispositivo: Int,
        numeroSemanas: Int = 3
    ): Result<Map<String, Pair<String, Int>>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.obtenerHistorialApps(
                    idDispositivo = "eq.$idDispositivo"
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val historial = response.body()!!
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val displayFormat = SimpleDateFormat("dd MMM", Locale("es", "ES"))
                    val estadisticas = mutableMapOf<String, Pair<String, Int>>()
                    
                    val calendar = Calendar.getInstance()
                    
                    // Ir al domingo de esta semana
                    val diaSemana = calendar.get(Calendar.DAY_OF_WEEK)
                    val diasHastaDomingo = if (diaSemana == Calendar.SUNDAY) 0 else (Calendar.SUNDAY - diaSemana + 7) % 7
                    calendar.add(Calendar.DAY_OF_YEAR, diasHastaDomingo)
                    
                    // Procesar las últimas N semanas
                    for (semana in 1..numeroSemanas) {
                        // Domingo de la semana (fin)
                        val finSemana = calendar.clone() as Calendar
                        
                        // Lunes de la semana (inicio) - 6 días atrás
                        val inicioSemana = calendar.clone() as Calendar
                        inicioSemana.add(Calendar.DAY_OF_YEAR, -6)
                        
                        val fechaInicio = dateFormat.format(inicioSemana.time)
                        val fechaFin = dateFormat.format(finSemana.time)
                        
                        // Calcular tiempo total de la semana
                        val tiempoTotal = historial
                            .filter { it.fecha >= fechaInicio && it.fecha <= fechaFin }
                            .sumOf { it.tiempoUso }
                        
                        // Formato de etiqueta: "Lun 10 - Dom 16"
                        val etiqueta = "${displayFormat.format(inicioSemana.time)} - ${displayFormat.format(finSemana.time)}"
                        
                        estadisticas["Semana $semana"] = Pair(etiqueta, tiempoTotal)
                        
                        // Retroceder a la semana anterior (ir al domingo anterior)
                        calendar.add(Calendar.DAY_OF_YEAR, -7)
                    }
                    
                    Result.Success(estadisticas)
                } else {
                    Result.Error("Error al obtener estadísticas: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Excepción: ${e.message}")
            }
        }
    }
}
