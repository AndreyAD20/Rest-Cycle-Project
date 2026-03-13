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
 * Worker que obtiene el GPS del dispositivo hijo y lo envía a Supabase.
 * Se ejecuta cada 60 minutos a través de UbicacionScheduler.
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

        val tipoTarea = inputData.getString("tipo_tarea") ?: "actualizar"

        return try {
            // Obtener ubicación actual con FusedLocationProviderClient
            val ubicacion = obtenerUbicacionActual() ?: run {
                Log.w(TAG, "No se pudo obtener la ubicación.")
                return Result.retry()
            }

            if (tipoTarea == "historial") {
                // Enviar a historial_ubicacion (Cada hora)
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
                    Log.i(TAG, "Historial guardado: ${ubicacion.first}, ${ubicacion.second}")
                    Result.success()
                } else {
                    Log.e(TAG, "Error historial: ${response.code()}")
                    Result.retry()
                }
            } else {
                // Enviar a ubicaciones (Actualizar cada 10-15 min)
                val response = SupabaseClient.api.guardarUbicacion(
                    UbicacionInput(
                        idUsuario = idUsuario,
                        latitud = ubicacion.first,
                        longitud = ubicacion.second
                    )
                )
                if (response.isSuccessful) {
                    Log.i(TAG, "Ubicación actualizada: ${ubicacion.first}, ${ubicacion.second}")
                    Result.success()
                } else {
                    Log.e(TAG, "Error actualizar: ${response.code()}")
                    Result.retry()
                }
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
