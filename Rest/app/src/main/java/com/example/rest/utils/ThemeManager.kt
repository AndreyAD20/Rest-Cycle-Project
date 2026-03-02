package com.example.rest.utils

import android.content.Context
import android.content.SharedPreferences

object ThemeManager {
    private const val PREFS_NAME = "RestCyclePrefs"
    private const val KEY_DARK_MODE = "DARK_MODE"
    
    /**
     * Guardar preferencia de modo oscuro
     */
    fun setDarkMode(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }
    
    /**
     * Obtener preferencia de modo oscuro
     */
    fun isDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false) // Por defecto: modo claro
    }

    /**
     * Obtener el factor de escala de fuente actual
     */
    fun getFontSizeScale(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return when (prefs.getString("TAMANO_FUENTE_KEY", "medium")) {
            "small" -> 0.92f // Antes 0.85f, ahora un poco menos pequeño
            "large" -> 1.30f // Antes 1.15f, ahora más grande
            else -> 1.0f
        }
    }
}
