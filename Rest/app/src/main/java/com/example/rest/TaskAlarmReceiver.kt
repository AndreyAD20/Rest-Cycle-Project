package com.example.rest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("TASK_ID", -1)
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Tarea"
        val taskNote = intent.getStringExtra("TASK_NOTE") ?: ""
        val vibrate = intent.getBooleanExtra("VIBRATE", false)
        val sound = intent.getBooleanExtra("SOUND", false)

        Log.d("TaskAlarmReceiver", "Alarm received for task: $taskTitle (ID: $taskId)")

        // Mostrar notificación
        TaskNotificationManager.showTaskNotification(
            context = context,
            taskId = taskId,
            title = taskTitle,
            note = taskNote,
            vibrate = vibrate,
            sound = sound
        )
    }
}
