package com.example.rest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted, rescheduling tasks...")
            
            // Cargar tareas guardadas
            
            // Corrección: loadTasks es una función de nivel superior, no un objeto
            val tasks = loadTasks(context)
            
            // Programar alarmas para tareas no completadas que tienen hora
            tasks.filter { !it.completada && it.tieneHora }.forEach { task ->
                TaskNotificationManager.scheduleTaskAlarm(context, task)
                Log.d("BootReceiver", "Rescheduled alarm for task: ${task.titulo}")
            }
        }
    }
}
