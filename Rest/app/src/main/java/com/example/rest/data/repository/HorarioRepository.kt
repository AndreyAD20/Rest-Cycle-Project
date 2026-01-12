package com.example.rest.data.repository

import com.example.rest.data.models.*
import com.example.rest.network.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HorarioRepository {
    
    private val api = SupabaseClient.api
    
    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }
    
    /**
     * Obtener todos los días
     */
    suspend fun obtenerDias(): Result<List<Dia>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.obtenerDias()
                if (response.isSuccessful) {
                    Result.Success(response.body() ?: emptyList())
                } else {
                    Result.Error("Error al obtener días: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Obtener todas las medidas
     */
    suspend fun obtenerMedidas(): Result<List<Medida>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.obtenerMedidas()
                if (response.isSuccessful) {
                    Result.Success(response.body() ?: emptyList())
                } else {
                    Result.Error("Error al obtener medidas: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Obtener dispositivos de un usuario
     */
    suspend fun obtenerDispositivosPorUsuario(idUsuario: Int): Result<List<Dispositivo>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.obtenerDispositivosPorUsuario(idUsuario = "eq.$idUsuario")
                if (response.isSuccessful) {
                    Result.Success(response.body() ?: emptyList())
                } else {
                    Result.Error("Error al obtener dispositivos: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Obtener horarios de un dispositivo
     */
    suspend fun obtenerHorariosPorDispositivo(idDispositivo: Int): Result<List<Horario>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.obtenerHorariosPorDispositivo(idDispositivo = "eq.$idDispositivo")
                if (response.isSuccessful) {
                    Result.Success(response.body() ?: emptyList())
                } else {
                    Result.Error("Error al obtener horarios: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Crear nuevo horario
     */
    suspend fun crearHorario(horario: Horario): Result<Horario> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.crearHorario(horario)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    Result.Success(response.body()!![0])
                } else {
                    Result.Error("Error al crear horario: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Eliminar horario
     */
    suspend fun eliminarHorario(idHorario: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.eliminarHorario(id = "eq.$idHorario")
                if (response.isSuccessful) {
                    Result.Success(true)
                } else {
                    Result.Error("Error al eliminar horario: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Obtener días de un horario
     */
    suspend fun obtenerDiasDeHorario(idHorario: Int): Result<List<DiasHorario>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.obtenerDiasDeHorario(idHorario = "eq.$idHorario")
                if (response.isSuccessful) {
                    Result.Success(response.body() ?: emptyList())
                } else {
                    Result.Error("Error al obtener días del horario: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Crear relación día-horario
     */
    suspend fun crearDiaHorario(diaHorario: DiasHorario): Result<DiasHorario> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.crearDiaHorario(diaHorario)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    Result.Success(response.body()!![0])
                } else {
                    Result.Error("Error al crear día-horario: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
}
