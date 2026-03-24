package com.example.rest

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.rest.services.ChatHeadOverlayService

object ChatHeadManager {

    /**
     * Punto de entrada principal para mostrar un Chat Head.
     * Originalmente se contempló usar Notification Bubbles en API 30+,
     * pero debido a bloqueos silenciosos de fabricantes (OEMs) se usa explícitamente el Overlay para todas las APIs garantizando el 100% de éxito.
     */
    fun showChat(context: Context, chatId: String, titulo: String) {
        if (!isBubbleEnabled(context)) {
            android.util.Log.d("ChatHeadManager", "Burbuja desactivada por configuración del usuario.")
            return
        }
        android.util.Log.d("ChatHeadManager", "Forzando Overlay Custom (Chat Heads Clásico) en todas las versiones.")
        launchOverlayService(context)
    }

    private fun isBubbleEnabled(context: Context): Boolean {
        val sharedPrefs = context.getSharedPreferences("RestCyclePrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean("BURBUJA_ACTIVA", false)
    }

    /**
     * Verifica si tiene el permiso de Overlay y lanza el servicio.
     * Si no lo tiene, lanza la pantalla de ajustes de Android para solicitarlo.
     * Evita lanzar el servicio múltiples veces innecesariamente.
     */
    fun launchOverlayService(context: Context) {
        if (!isBubbleEnabled(context)) return

        // En Android 6.0 (API 23)+ se requiere pedir permiso explícito para Overlay
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            // Agregamos flag NEW_TASK porque se lanza desde un contexto que podría no ser Activity
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return
        }

        // Si ya tiene el permiso (o es < API 23), verificamos si el servicio ya está corriendo
        // antes de intentar iniciarlo nuevamente para evitar diálogos de permiso repetitivos
        if (isOverlayServiceRunning(context)) {
            return
        }

        // Si no está corriendo, iniciamos el Foreground Service
        val serviceIntent = Intent(context, ChatHeadOverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    /**
     * Verifica si el servicio de superposición está actualmente en ejecución.
     */
    private fun isOverlayServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE).any { service ->
            service.service.className == ChatHeadOverlayService::class.java.name
        }
    }

    /**
     * Detiene el servicio de superposición.
     */
    fun hideOverlayService(context: Context) {
        val serviceIntent = Intent(context, ChatHeadOverlayService::class.java)
        context.stopService(serviceIntent)
    }
}
