package com.example.rest.services

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rest.data.models.UbicacionInput
import com.example.rest.network.SupabaseClient
import com.example.rest.utils.PreferencesManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Worker que obtiene el GPS del dispositivo hijo y lo guarda en el HISTORIAL.
 * Se ejecuta cada 15 minutos solo para dejar una marca en la línea de tiempo.
 */
class UbicacionWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "UbicacionWorker"
    }

    override suspend fun doWork(): Result {
        val prefs = PreferencesManager(appContext)
        val idUsuario = prefs.getUserId()

        if (idUsuario == -1) {
            Log.w(TAG, "No hay usuario autenticado. Cancelando envío de ubicación.")
            return Result.failure()
        }

        return try {
            val ubicacion = obtenerUbicacionActual() ?: run {
                Log.w(TAG, "No se pudo obtener la ubicación para el historial.")
                return Result.retry()
            }

            // Registro en historial_ubicacion (Instantánea de cada 15 min)
            val sdfFecha = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val sdfHora = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            val ahora = java.util.Date()

            val response = SupabaseClient.api.guardarHistorialUbicacion(
                com.example.rest.data.models.HistorialUbicacionInput(
                    idUsuario = idUsuario,
                    latitud = ubicacion.first,
                    longitud = ubicacion.second,
                    fecha = sdfFecha.format(ahora),
                    hora = sdfHora.format(ahora)
                )
            )
            
            if (response.isSuccessful) {
                Log.i(TAG, "Instantánea de historial guardada (15 min).")
                Result.success()
            } else {
                Log.e(TAG, "Error al guardar instantánea: ${response.code()}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en UbicacionWorker: ${e.message}")
            Result.retry()
        }
    }

    /**
     * Obtiene la ubicación actual usando FusedLocationProviderClient.
     * Retorna un Pair<latitud, longitud> o null si falla.
     */
    @Suppress("MissingPermission")
    private suspend fun obtenerUbicacionActual(): Pair<Double, Double>? {
        val fusedClient = LocationServices.getFusedLocationProviderClient(appContext)
        val cancellationTokenSource = CancellationTokenSource()

        return withTimeoutOrNull(15_000L) { // 15 segundos máximo
            suspendCancellableCoroutine { cont ->
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        cont.resume(Pair(location.latitude, location.longitude))
                    } else {
                        cont.resume(null)
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error obteniendo GPS: ${e.message}")
                    cont.resume(null)
                }

                cont.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }
        }
    }
}
