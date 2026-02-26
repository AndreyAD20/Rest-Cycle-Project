package com.example.rest.data.repository

import com.example.rest.data.models.Nota
import com.example.rest.network.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotaRepository {
    
    private val api = SupabaseClient.api
    
    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }
    
    /**
     * Obtener todas las notas de un usuario
     */
    suspend fun obtenerNotasPorUsuario(idUsuario: Int): Result<List<Nota>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.obtenerNotasPorUsuario(idUsuario = "eq.$idUsuario")
                if (response.isSuccessful) {
                    val notas = response.body() ?: emptyList()
                    Result.Success(notas)
                } else {
                    Result.Error("Error al obtener notas: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
            }
        }
    
    /**
     * Obtener la última nota modificada/creada
     */
    suspend fun obtenerUltimaNota(idUsuario: Int): Result<Nota?> {
        return withContext(Dispatchers.IO) {
            try {
                // Ordenar por fecha_actualizacion descendente, límite 1
                val response = api.obtenerNotasPorUsuario(
                    idUsuario = "eq.$idUsuario",
                    order = "fecha_actualizacion.desc",
                    limit = "1"
                )
                if (response.isSuccessful) {
                    val notas = response.body()
                    Result.Success(notas?.firstOrNull())
                } else {
                    Result.Error("Error al obtener última nota: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Crear una nueva nota
     */
    suspend fun crearNota(nota: Nota): Result<Nota> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.crearNota(nota)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    Result.Success(response.body()!![0])
                } else {
                    Result.Error("Error al crear nota: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Eliminar una nota
     */
    suspend fun eliminarNota(idNota: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.eliminarNota(id = "eq.$idNota")
                if (response.isSuccessful) {
                    Result.Success(true)
                } else {
                    Result.Error("Error al eliminar nota: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Actualizar una nota
     */
    suspend fun actualizarNota(idNota: Int, nota: Nota): Result<Nota> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.actualizarNota(id = "eq.$idNota", nota = nota)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    Result.Success(response.body()!![0])
                } else {
                    Result.Error("Error al actualizar nota: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Actualizar solo el estado de favorito de una nota
     */
    suspend fun actualizarFavorito(idNota: Int, favorito: Boolean): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Crear un objeto Nota con solo el campo favorito
                val notaParcial = Nota(
                    idUsuario = null,
                    titulo = null,
                    contenido = null,
                    color = null,
                    fecha_actualizacion = null,
                    favorito = favorito
                )
                val response = api.actualizarNota(id = "eq.$idNota", nota = notaParcial)
                if (response.isSuccessful) {
                    Result.Success("Favorito actualizado")
                } else {
                    Result.Error("Error al actualizar favorito: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.message}")
            }
        }
    }
}
