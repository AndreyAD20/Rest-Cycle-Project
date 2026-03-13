package com.example.rest.services

import android.content.Context
import androidx.work.*
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import java.util.Calendar
/**
 * Programa UbicacionWorker para que se ejecute cada 60 minutos.
 * Debe llamarse una sola vez al iniciar sesión en modo hijo.
 */
object UbicacionScheduler {

    private const val WORK_NAME_ACTUALIZAR = "ubicacion_actualizar"
    private const val WORK_NAME_HISTORIAL = "ubicacion_historial"

    fun iniciar(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        // 1. Calcular minutos hasta la siguiente hora "en punto" para el historial
        val calendar = Calendar.getInstance()
        val minutosActuales = calendar.get(Calendar.MINUTE)
        val minutosParaLaHora = 60L - minutosActuales

        val workManager = WorkManager.getInstance(context)

        // --- TAREA A: Actualización en tiempo real (Upsert) ---
        // Android tiene un mínimo de 15 minutos para tareas periódicas
        val requestActualizar = PeriodicWorkRequestBuilder<UbicacionWorker>(
            repeatInterval = 15, // Mínimo permitido por Android
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInputData(workDataOf("tipo_tarea" to "actualizar"))
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME_ACTUALIZAR,
            ExistingPeriodicWorkPolicy.KEEP,
            requestActualizar
        )

        // --- TAREA B: Historial Horario (Hora en punto) ---
        val requestHistorial = PeriodicWorkRequestBuilder<UbicacionWorker>(
            repeatInterval = 60,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setInitialDelay(minutosParaLaHora, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInputData(workDataOf("tipo_tarea" to "historial"))
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME_HISTORIAL,
            ExistingPeriodicWorkPolicy.KEEP,
            requestHistorial
        )

        // Ejecutar inmediatamente una vez para ambas para tener datos iniciales
        val inmediataActualizar = OneTimeWorkRequestBuilder<UbicacionWorker>()
            .setInputData(workDataOf("tipo_tarea" to "actualizar"))
            .build()
        
        val inmediataHistorial = OneTimeWorkRequestBuilder<UbicacionWorker>()
            .setInputData(workDataOf("tipo_tarea" to "historial"))
            .build()

        workManager.enqueueUniqueWork("${WORK_NAME_ACTUALIZAR}_now", ExistingWorkPolicy.REPLACE, inmediataActualizar)
        workManager.enqueueUniqueWork("${WORK_NAME_HISTORIAL}_now", ExistingWorkPolicy.REPLACE, inmediataHistorial)
    }

    /**
     * Cancelar el envío de ubicación (ej. al cerrar sesión)
     */
    fun cancelar(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(WORK_NAME_ACTUALIZAR)
        workManager.cancelUniqueWork(WORK_NAME_HISTORIAL)
    }
}
