package com.example.rest

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.rest.data.models.Evento
import com.example.rest.features.tools.CalendarioComposeActivity
import com.example.rest.utils.BubbleHelper
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object EventoNotificationManager {

    // Canal COMPARTIDO con TaskNotificationManager (NO tocar — solo para tareas)
    // private const val TASK_CHANNEL_ID = "task_notifications"

    // Canal DEDICADO para eventos del calendario con soporte de burbujas
    private const val CHANNEL_ID = "bubble_eventos"

    // Offset para evitar colisión de IDs con tareas
    private const val EVENTO_ID_OFFSET = 50000

    /**
     * Programa una alarma exacta para el evento, a la hora de fechaInicio.
     * Si la fecha ya pasó, no programa nada.
     */
    fun scheduleEventoAlarm(context: Context, evento: Evento) {
        val eventoId = evento.id ?: return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Verificar permiso de alarmas exactas (Android 12+)
        val usarAlarmaExacta: Boolean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            usarAlarmaExacta = alarmManager.canScheduleExactAlarms()
            if (!usarAlarmaExacta) {
                Log.w("EventoNotifMgr", "Sin permiso de alarma exacta, usando alarma aproximada")
                // No cancelamos — usamos setAndAllowWhileIdle como fallback
            }
        } else {
            usarAlarmaExacta = true
        }

        // Parsear fechaInicio ISO 8601
        val triggerAtMillis = parseIsoToMillis(evento.fechaInicio) ?: return

        // No programar si la hora ya pasó
        if (triggerAtMillis <= System.currentTimeMillis()) {
            Log.w("EventoNotifMgr", "Evento '${evento.titulo}' ya pasó, no se programa alarma")
            return
        }

        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra("IS_EVENTO",    true)
            putExtra("EVENTO_ID",    eventoId + EVENTO_ID_OFFSET)
            putExtra("EVENTO_TITULO", evento.titulo)
            putExtra("EVENTO_TIPO",  evento.tipo)
            putExtra("EVENTO_FECHA", evento.fechaInicio)  // ← fecha para mostrar en la burbuja
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventoId + EVENTO_ID_OFFSET,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (usarAlarmaExacta) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d("EventoNotifMgr", "✅ Alarma EXACTA programada: '${evento.titulo}' a las ${evento.fechaInicio}")
            } else {
                // Fallback para Android 12 sin permiso de alarma exacta
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.w("EventoNotifMgr", "⚠️ Alarma APROXIMADA programada: '${evento.titulo}' (puede llegar hasta 1min tarde)")
            }
        } catch (e: SecurityException) {
            Log.e("EventoNotifMgr", "Error programando alarma: ${e.message}")
        }
    }

    /**
     * Cancela la alarma del evento dado su ID.
     */
    fun cancelEventoAlarm(context: Context, eventoId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventoId + EVENTO_ID_OFFSET,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d("EventoNotifMgr", "🗑 Alarma cancelada para evento ID: $eventoId")
    }

    /**
     * Muestra la notificación para un evento del calendario.
     * Notificación normal siempre funciona; Bubbles se agrega si el sistema lo permite.
     */
    fun showEventoNotification(
        context: Context,
        notifId: Int,
        titulo: String,
        tipo: String,
        fechaIso: String = "",   // Fecha ISO del evento para mostrarse en la burbuja
        intentOverride: Intent? = null,
        bubbleIntentOverride: Intent? = null
    ) {
        try {
            // ── Los canales ya se crean en BaseComposeActivity.oncreate ─────────
            // Solo verificamos que el canal existe (puede haberse instalado fresco)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                    // Fallback: si la Activity base todavía no lo creó (ej: boot receiver)
                    val channel = android.app.NotificationChannel(
                        CHANNEL_ID, "Eventos del Calendario",
                        android.app.NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 500, 250, 500)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) setAllowBubbles(true)
                    }
                    nm.createNotificationChannel(channel)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val pref = when (nm.bubblePreference) {
                        android.app.NotificationManager.BUBBLE_PREFERENCE_ALL      -> "ALL ✅"
                        android.app.NotificationManager.BUBBLE_PREFERENCE_SELECTED -> "SELECTED ✅"
                        android.app.NotificationManager.BUBBLE_PREFERENCE_NONE     -> "NONE ❌ — usuario debe activar en Ajustes"
                        else -> "?"
                    }
                    Log.d("EventoNotifMgr", "BubblePreference: $pref")
                }
            }

            // Verificar permiso de notificaciones (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = androidx.core.app.ActivityCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (!granted) {
                    Log.w("EventoNotifMgr", "Sin permiso POST_NOTIFICATIONS")
                    return
                }
            }
            
            // ── Fase 4: Centralizar en el Historial del Asistente (Burbuja Rest Cycle) ──
            com.example.rest.data.NotificationRepository.addNotification(
                title = titulo,
                message = tipo,
                category = "Notificación de Sistema",
                sourceClass = "com.example.rest.features.tools.CalendarioComposeActivity", // Corregido path completo a la act
                systemId = notifId // ← ID para sincronizar con OS
            )
            // Lanza o actualiza la burbuja flotante asegurando que sea visible
            com.example.rest.ChatHeadManager.showChat(context, "rest_cycle_assistant", "Asistente Rest Cycle")

            val intent = intentOverride ?: Intent(context, CalendarioComposeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            // IMMUTABLE para contentIntent (tap en la notificación)
            val tapIntent = PendingIntent.getActivity(
                context, notifId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val contenido = "Evento: $tipo"
            // NOTA: publishCalendarioShortcut() se llama desde CalendarioComposeActivity (primer plano).
            // NO llamar aquí desde BroadcastReceiver — Android 12 lo rechaza silenciosamente.

            // ── Notificación básica (siempre funciona) ────────────────────────
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("📅 $titulo")
                .setContentText(contenido)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(tapIntent)

            // ── Bubbles API (opcional, Android 11+) ───────────────────────────
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    // Intent específico para la burbuja → BubbleEventoActivity (liviana) o override
                    val bubbleActivityIntent = bubbleIntentOverride ?: Intent(context, BubbleEventoActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("EVENTO_TITULO", titulo)
                        putExtra("EVENTO_TIPO",   tipo)
                        putExtra("EVENTO_FECHA",  fechaIso)   // ← NUEVO: hora para mostrar en la burbuja
                    }
                    val bubbleFlags = PendingIntent.FLAG_UPDATE_CURRENT or
                        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
                    val bubbleIntent = PendingIntent.getActivity(
                        context, notifId + 1, bubbleActivityIntent, bubbleFlags
                    )
                    
                    // Si estamos enviando un intent customizado de CHAT, usamos su respectivo ID en el OS
                    val currentShortcutId = if (bubbleIntentOverride != null) {
                        BubbleHelper.SHORTCUT_CHAT 
                    } else {
                        BubbleHelper.SHORTCUT_CALENDARIO
                    }

                    val bubbleData = BubbleHelper.createBubbleMetadata(context, bubbleIntent, currentShortcutId)
                    if (bubbleData != null) {
                        val person = BubbleHelper.createBotPerson(context)

                        builder
                            .setShortcutId(currentShortcutId) // shortcut publicado dinámicamente
                            .setBubbleMetadata(bubbleData)
                            .addPerson(person)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .setStyle(
                                NotificationCompat.MessagingStyle(person)
                                    .setConversationTitle(titulo)
                                    .addMessage("$titulo · $tipo", System.currentTimeMillis(), person)
                            )
                        Log.d("EventoNotifMgr", "✅ BubbleMetadata adjuntada (API ${android.os.Build.VERSION.SDK_INT})")
                    }
                } catch (e: Exception) {
                    Log.w("EventoNotifMgr", "Bubbles no disponible: ${e.message}")
                }
            }

            NotificationManagerCompat.from(context).notify(notifId, builder.build())
            Log.d("EventoNotifMgr", "✅ Notificación mostrada: $titulo")

        } catch (e: Exception) {
            Log.e("EventoNotifMgr", "Error en showEventoNotification: ${e.javaClass.simpleName}: ${e.message}", e)
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Convierte una fecha ISO 8601 (ej: "2026-02-28T20:30:00Z") a milisegundos epoch.
     * Retorna null si el formato no es válido.
     */
    private fun parseIsoToMillis(isoDate: String): Long? {
        val formatos = listOf(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ssXXX"
        )
        for (formato in formatos) {
            try {
                val sdf = SimpleDateFormat(formato, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = sdf.parse(isoDate.replace("+00:00", "Z")) ?: continue
                return date.time
            } catch (_: Exception) {}
        }
        Log.e("EventoNotifMgr", "No se pudo parsear la fecha: $isoDate")
        return null
    }
}
