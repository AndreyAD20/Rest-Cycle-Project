package com.example.rest

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.rest.features.tools.TareasComposeActivity
import com.example.rest.utils.BubbleHelper
import java.util.Calendar

object TaskNotificationManager {
    private const val CHANNEL_ID = "task_notifications"
    private const val CHANNEL_NAME = "Recordatorios de Tareas"
    private const val CHANNEL_DESCRIPTION = "Notificaciones para recordatorios de tareas programadas"

    /**
     * Crear canal de notificaciones (necesario para Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                
                // Configurar sonido predeterminado
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("TaskNotificationMgr", "Notification channel created")
        }
    }

    /**
     * Mostrar notificación de tarea
     */
    fun showTaskNotification(
        context: Context,
        taskId: Int,
        title: String,
        note: String,
        vibrate: Boolean,
        sound: Boolean
    ) {
        // Verificar permiso de notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("TaskNotificationMgr", "POST_NOTIFICATIONS permission not granted")
                return
            }
        }

        // Intent para abrir la app al tocar la notificación
        val intent = Intent(context, TareasComposeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val textContent = note.ifEmpty { "Es hora de realizar esta tarea" }
        
        // ── Fase 4: Centralizar en el Historial del Asistente (Burbuja Rest Cycle) ──
        com.example.rest.data.NotificationRepository.addNotification(
            title = title,
            message = textContent,
            category = "Notificación de Tarea",
            sourceClass = "com.example.rest.features.tools.TareasComposeActivity",
            systemId = taskId // ← ID para sincronización con OS
        )
        // Forzamos levantar el globo flotante
        com.example.rest.ChatHeadManager.showChat(context, "rest_cycle_assistant", "Asistente Rest Cycle")

        // Construir notificación clásica (silenciosa y como respaldo)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(textContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            // Cambiado a CATEGORY_MESSAGE para tener mayor probabilidad de ser mostrado como Burbuja
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Configuración de Burbuja (Bubbles API)
        val bubbleData = BubbleHelper.createBubbleMetadata(context, pendingIntent)
        if (bubbleData != null) {
            builder.setBubbleMetadata(bubbleData)
            val person = BubbleHelper.createBotPerson()
            builder.addPerson(person)
            // MessagingStyle es requerido por Android 11+ para las notificaciones en formato Burbuja
            builder.setStyle(NotificationCompat.MessagingStyle(person)
                .addMessage(textContent, System.currentTimeMillis(), person))
        }

        // Configurar vibración
        if (vibrate) {
            builder.setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
        } else {
            builder.setVibrate(longArrayOf(0))
        }

        // Configurar sonido
        if (sound) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(soundUri)
        }

        // Mostrar notificación
        with(NotificationManagerCompat.from(context)) {
            notify(taskId, builder.build())
            Log.d("TaskNotificationMgr", "Notification shown for task: $title (ID: $taskId)")
        }
    }

    /**
     * Programar alarma para una tarea
     */
    fun scheduleTaskAlarm(
        context: Context,
        task: Tarea
    ) {
        if (!task.tieneHora || task.hora.isEmpty()) {
            Log.w("TaskNotificationMgr", "Task has no time set, skipping alarm")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Verificar permiso de alarmas exactas (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("TaskNotificationMgr", "Cannot schedule exact alarms, permission not granted")
                return
            }
        }

        // Parsear hora (formato: "HH:mm AM/PM")
        val calendar = parseTimeToCalendar(task.hora)
        
        // Si la hora ya pasó hoy, programar para mañana
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Crear intent para el BroadcastReceiver
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", task.titulo)
            putExtra("TASK_NOTE", task.nota)
            putExtra("VIBRATE", task.vibracion)
            putExtra("SOUND", task.sonido)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Programar alarma exacta
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d("TaskNotificationMgr", "Alarm scheduled for task: ${task.titulo} at ${task.hora}")
        } catch (e: SecurityException) {
            Log.e("TaskNotificationMgr", "Failed to schedule alarm: ${e.message}")
        }
    }

    /**
     * Cancelar alarma de una tarea
     */
    fun cancelTaskAlarm(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d("TaskNotificationMgr", "Alarm cancelled for task ID: $taskId")
    }

    /**
     * Parsear hora en formato "HH:mm AM/PM" a Calendar
     */
    private fun parseTimeToCalendar(timeString: String): Calendar {
        val calendar = Calendar.getInstance()
        
        try {
            // Formato esperado: "7:30 AM" o "07:30 PM"
            val parts = timeString.trim().split(" ")
            if (parts.size != 2) {
                Log.w("TaskNotificationMgr", "Invalid time format: $timeString")
                return calendar
            }
            
            val timeParts = parts[0].split(":")
            if (timeParts.size != 2) {
                Log.w("TaskNotificationMgr", "Invalid time format: $timeString")
                return calendar
            }
            
            var hour = timeParts[0].toIntOrNull() ?: 0
            val minute = timeParts[1].toIntOrNull() ?: 0
            val amPm = parts[1].uppercase()
            
            // Convertir a formato 24 horas
            if (amPm == "PM" && hour != 12) {
                hour += 12
            } else if (amPm == "AM" && hour == 12) {
                hour = 0
            }
            
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            Log.d("TaskNotificationMgr", "Parsed time: $timeString -> ${calendar.time}")
        } catch (e: Exception) {
            Log.e("TaskNotificationMgr", "Error parsing time: ${e.message}")
        }
        
        return calendar
    }
}
