package com.example.rest.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestor local para guardar y recuperar los Temas de Interés del usuario.
 * Utiliza SharedPreferences para almacenar rápidamente una lista de Strings.
 */
object PreferenciasInteresManager {
    private const val PREFS_NAME = "TemasInteresPrefs"
    private const val KEY_TEMAS = "temas_guardados"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** Guarda la lista de temas seleccionados */
    fun guardarTemas(context: Context, temas: List<String>) {
        // SharedPreferences soporta guardar un HashSet de Strings nativamente
        val setTemas = HashSet<String>(temas)
        getPrefs(context).edit().putStringSet(KEY_TEMAS, setTemas).apply()
    }

    /** Recupera la lista de temas guardados. Retorna una lista vacía si no hay nada guardado. */
    fun obtenerTemas(context: Context): List<String> {
        val setTemas = getPrefs(context).getStringSet(KEY_TEMAS, null)
        return setTemas?.toList() ?: emptyList()
    }
    
    /** Verifica si el usuario ya ha configurado sus temas al menos una vez */
    fun hasConfiguradoTemas(context: Context): Boolean {
        return getPrefs(context).contains(KEY_TEMAS)
    }

    /**
     * Construye un texto (prompt) basado en las preferencias exactas ('Tema:subtema') del usuario,
     * para usarse en la generación de contenido o filtros con IA.
     */
    fun construirPromptPreferencias(context: Context): String {
        val temasGuardados = obtenerTemas(context)
        if (temasGuardados.isEmpty()) {
            return "El usuario no ha definido preferencias específicas aún."
        }
        
        return "Las preferencias de interés del usuario son las siguientes (formato 'Tema:subtema'):\n" +
                temasGuardados.joinToString(", ") +
                "\nUtiliza estas combinaciones exactas para filtrar y personalizar noticias, datos y contenido motivacional acorde a estos gustos."
    }
}
