package com.example.rest.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.rest.R
import com.example.rest.features.tools.CalendarioComposeActivity

object BubbleHelper {

    const val SHORTCUT_CALENDARIO = "shortcut_calendario"
    const val SHORTCUT_CHAT = "shortcut_chat" // 💬 Nuevo

    /**
     * Publica un ShortcutInfo dinámico para el Calendario.
     * DEBE llamarse desde primer plano (Activity), nunca desde BroadcastReceiver.
     * Android 12 requiere este shortcut ANTES de mostrar la notificación como burbuja.
     */
    fun publishCalendarioShortcut(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return

        try {
            val icon = IconCompat.createWithResource(context, R.mipmap.logo_buho)

            val intent = Intent(context, CalendarioComposeActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_CALENDARIO)
                .setShortLabel("Calendario")
                .setLongLabel("Eventos del Calendario")
                .setIcon(icon)
                .setIntent(intent)
                .setLongLived(true)                                    // Requerido para burbujas
                .setCategories(setOf("android.shortcut.conversation")) // Requerido para burbujas
                .setPerson(createBotPerson(context))
                .build()

            ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
            android.util.Log.d("BubbleHelper", "✅ Shortcut '$SHORTCUT_CALENDARIO' publicado correctamente")
        } catch (e: Exception) {
            android.util.Log.w("BubbleHelper", "No se pudo publicar el shortcut: ${e.message}")
        }
    }

    /**
     * Publica el Shortcut dedicado a los chats (Burbujas Flotantes Nativas Fase 3).
     */
    fun publishChatShortcut(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return

        try {
            val icon = IconCompat.createWithResource(context, R.mipmap.logo_buho) // Ideal: Icono de persona

            val intent = Intent(context, com.example.rest.features.tools.BubbleChatActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_CHAT)
                .setShortLabel("Chats")
                .setLongLabel("Conversaciones y Soporte")
                .setIcon(icon)
                .setIntent(intent)
                .setLongLived(true)
                .setCategories(setOf("android.shortcut.conversation"))
                .setPerson(createBotPerson(context))
                .build()

            ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
            android.util.Log.d("BubbleHelper", "✅ Shortcut '$SHORTCUT_CHAT' publicado correctamente")
        } catch (e: Exception) {
            android.util.Log.w("BubbleHelper", "No se pudo publicar el shortcut de chat: ${e.message}")
        }
    }

    /**
     * Crea los metadatos de burbuja.
     * Usamos Builder(PendingIntent, Icon) en TODAS las versiones.
     * En Android 12+, el sistema requiere un ShortcutId.
     */
    fun createBubbleMetadata(
        context: Context,
        pendingIntent: PendingIntent,
        shortcutId: String = SHORTCUT_CALENDARIO, // Parametrizamos
        iconResId: Int = R.mipmap.logo_buho
    ): NotificationCompat.BubbleMetadata? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // ── Android 12+ (API 31+): usar shortcutId (OBLIGATORIO) ────
                NotificationCompat.BubbleMetadata.Builder(shortcutId)
                    .setIntent(pendingIntent) // ← CRÍTICO: lanza nuestro diseño en lugar del calendario/chat base
                    .setDesiredHeight(480)
                    .setAutoExpandBubble(true)
                    .setSuppressNotification(true)
                    .build()
            } else {
                // ── Android 11 (API 30): usar PendingIntent + Icon ──────────
                val bubbleIcon = IconCompat.createWithResource(context, iconResId)
                NotificationCompat.BubbleMetadata.Builder(pendingIntent, bubbleIcon)
                    .setDesiredHeight(480)
                    .setAutoExpandBubble(true)
                    .setSuppressNotification(false)
                    .build()
            }
        } catch (e: Exception) {
            android.util.Log.w("BubbleHelper", "Error creando BubbleMetadata: ${e.message}")
            null
        }
    }

    /**
     * Crea un objeto Person identificando a la app (bot).
     */
    fun createBotPerson(context: Context? = null): Person {
        return Person.Builder()
            .setName("Rest Cycle")
            .setKey("rest_cycle_bot")
            .setBot(true)
            .setImportant(true)
            .apply {
                if (context != null) {
                    setIcon(IconCompat.createWithResource(context, R.mipmap.logo_buho))
                }
            }
            .build()
    }
}
