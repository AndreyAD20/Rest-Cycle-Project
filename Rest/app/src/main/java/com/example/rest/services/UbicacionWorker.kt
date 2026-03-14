package com.example.rest.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
 * Worker que obtiene el GPS del dispositivo hijo y lo guarda en:
 * 1. Tabla "ubicaciones" - para consulta de ubicación actual en tiempo real
 * 2. Tabla "historial_ubicacion" - para mantener histórico de posiciones
 * Se ejecuta cada 15 minutos.
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

        // Verificar permiso de ubicación
        if (appContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Permiso de ubicación no concedido.")
            return Result.failure()
        }

        return try {
            val ubicacion = obtenerUbicacionActual() ?: run {
                Log.w(TAG, "No se pudo obtener la ubicación.")
                return Result.retry()
            }

            val sdfFecha = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val sdfHora = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            val ahora = java.util.Date()

            // 1. Guardar ubicación actual (para consulta en tiempo real desde control parental)
            try {
                val responseUbicacion = SupabaseClient.api.guardarUbicacion(
                    UbicacionInput(
                        idUsuario = idUsuario,
                        latitud = ubicacion.first,
                        longitud = ubicacion.second
                    )
                )
                if (responseUbicacion.isSuccessful) {
                    Log.i(TAG, "Ubicación actual actualizada: ${ubicacion.first}, ${ubicacion.second}")
                } else {
                    Log.w(TAG, "Error guardando ubicación actual: ${responseUbicacion.code()}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Excepción guardando ubicación actual: ${e.message}")
            }

            // 2. Guardar en historial (Instantánea de cada 15 min)
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
