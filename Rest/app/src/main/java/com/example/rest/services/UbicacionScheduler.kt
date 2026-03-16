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

    private const val WORK_NAME_HISTORY_SNAPSHOT = "ubicacion_snapshot_historial"

    fun iniciar(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val workManager = WorkManager.getInstance(context)

        // --- TAREA PERIÓDICA: Capturar snapshot para el HISTORIAL (Cada 15 min) ---
        val requestHistory = PeriodicWorkRequestBuilder<UbicacionWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(15, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME_HISTORY_SNAPSHOT,
            ExistingPeriodicWorkPolicy.UPDATE,
            requestHistory
        )

        // Ejecutar inmediatamente una vez para tener la primera marca
        val inmediata = OneTimeWorkRequestBuilder<UbicacionWorker>().build()
        workManager.enqueueUniqueWork("${WORK_NAME_HISTORY_SNAPSHOT}_now", ExistingWorkPolicy.REPLACE, inmediata)
    }

    /**
     * Cancelar el envío de ubicación (ej. al cerrar sesión)
     */
    fun cancelar(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(WORK_NAME_HISTORY_SNAPSHOT)
        workManager.cancelAllWorkByTag(WORK_NAME_HISTORY_SNAPSHOT)
    }
}
