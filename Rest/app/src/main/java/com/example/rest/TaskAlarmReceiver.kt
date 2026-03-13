package com.example.rest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val isEvento = intent.getBooleanExtra("IS_EVENTO", false)

        if (isEvento) {
            // ── Evento del Calendario ──────────────────────────────────────
            val eventoId  = intent.getIntExtra("EVENTO_ID", -1)
            val titulo    = intent.getStringExtra("EVENTO_TITULO") ?: "Evento"
            val tipo      = intent.getStringExtra("EVENTO_TIPO")   ?: ""
            val fechaIso  = intent.getStringExtra("EVENTO_FECHA")  ?: ""

            Log.d("TaskAlarmReceiver", "Alarma de EVENTO recibida: $titulo (ID: $eventoId)")

            EventoNotificationManager.showEventoNotification(
                context  = context,
                notifId  = eventoId,
                titulo   = titulo,
                tipo     = tipo,
                fechaIso = fechaIso
            )
        } else {
            // ── Tarea normal ───────────────────────────────────────────────
            val taskId    = intent.getIntExtra("TASK_ID", -1)
            val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Tarea"
            val taskNote  = intent.getStringExtra("TASK_NOTE")  ?: ""
            val vibrate   = intent.getBooleanExtra("VIBRATE", false)
            val sound     = intent.getBooleanExtra("SOUND", false)

            Log.d("TaskAlarmReceiver", "Alarma de TAREA recibida: $taskTitle (ID: $taskId)")

            TaskNotificationManager.showTaskNotification(
                context = context,
                taskId  = taskId,
                title   = taskTitle,
                note    = taskNote,
                vibrate = vibrate,
                sound   = sound
            )
        }
    }
}
