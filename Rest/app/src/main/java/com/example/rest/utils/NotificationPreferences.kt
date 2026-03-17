package com.example.rest.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestor de preferencias para notificaciones.
 * Usa SharedPreferences normal (no encriptado) para configuraciones simples.
 */
object NotificationPreferences {

    private const val PREFS_NAME = "NotificationPrefs"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ========== NOTIFICACIONES FLOTANTES ==========

    fun setFlotanteEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("FLOTANTE_ENABLED", enabled).apply()
    }

    fun isFlotanteEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean("FLOTANTE_ENABLED", true)
    }

    // Uso excesivo
    fun setFlotanteUsoExcesivo(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("FLOTANTE_USO_EXCESIVO", enabled).apply()
    }

    fun isFlotanteUsoExcesivo(context: Context): Boolean {
        return getPrefs(context).getBoolean("FLOTANTE_USO_EXCESIVO", true)
    }

    // Tareas
    fun setFlotanteTareas(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("FLOTANTE_TAREAS", enabled).apply()
    }

    fun isFlotanteTareas(context: Context): Boolean {
        return getPrefs(context).getBoolean("FLOTANTE_TAREAS", true)
    }

    // Eventos
    fun setFlotanteEventos(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("FLOTANTE_EVENTOS", enabled).apply()
    }

    fun isFlotanteEventos(context: Context): Boolean {
        return getPrefs(context).getBoolean("FLOTANTE_EVENTOS", true)
    }

    // Tiempo de pantalla
    fun setFlotanteTiempoPantalla(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("FLOTANTE_TIEMPO_PANTALLA", enabled).apply()
    }

    fun isFlotanteTiempoPantalla(context: Context): Boolean {
        return getPrefs(context).getBoolean("FLOTANTE_TIEMPO_PANTALLA", true)
    }

    // Bloqueo
    fun setFlotanteBloqueo(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("FLOTANTE_BLOQUEO", enabled).apply()
    }

    fun isFlotanteBloqueo(context: Context): Boolean {
        return getPrefs(context).getBoolean("FLOTANTE_BLOQUEO", true)
    }

    // ========== NOTIFICACIONES DEL SISTEMA ==========

    fun setSistemaEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("SISTEMA_ENABLED", enabled).apply()
    }

    fun isSistemaEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean("SISTEMA_ENABLED", true)
    }

    // Uso excesivo
    fun setSistemaUsoExcesivo(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("SISTEMA_USO_EXCESIVO", enabled).apply()
    }

    fun isSistemaUsoExcesivo(context: Context): Boolean {
        return getPrefs(context).getBoolean("SISTEMA_USO_EXCESIVO", true)
    }

    // Tareas
    fun setSistemaTareas(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("SISTEMA_TAREAS", enabled).apply()
    }

    fun isSistemaTareas(context: Context): Boolean {
        return getPrefs(context).getBoolean("SISTEMA_TAREAS", true)
    }

    // Eventos
    fun setSistemaEventos(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("SISTEMA_EVENTOS", enabled).apply()
    }

    fun isSistemaEventos(context: Context): Boolean {
        return getPrefs(context).getBoolean("SISTEMA_EVENTOS", true)
    }

    // Tiempo de pantalla
    fun setSistemaTiempoPantalla(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("SISTEMA_TIEMPO_PANTALLA", enabled).apply()
    }

    fun isSistemaTiempoPantalla(context: Context): Boolean {
        return getPrefs(context).getBoolean("SISTEMA_TIEMPO_PANTALLA", true)
    }

    // Bloqueo
    fun setSistemaBloqueo(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("SISTEMA_BLOQUEO", enabled).apply()
    }

    fun isSistemaBloqueo(context: Context): Boolean {
        return getPrefs(context).getBoolean("SISTEMA_BLOQUEO", true)
    }
}
