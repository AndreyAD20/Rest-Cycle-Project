package com.example.rest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Actividad base para todas las actividades Compose del proyecto.
 * Configura automáticamente el modo edge-to-edge y oculta las barras del sistema.
 */
abstract class BaseComposeActivity : AppCompatActivity() {
    
    private var escaladoFuenteActual: Float = 1.0f
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        aplicarIdiomaGuardado()
        escaladoFuenteActual = com.example.rest.utils.ThemeManager.getFontSizeScale(this)
        configurarPantallaCompleta()
    }
    
    private fun aplicarIdiomaGuardado() {
        val sharedPrefs = getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE)
        val idiomaSeleccionado = sharedPrefs.getString("IDIOMA", "Español") ?: "Español"
        val code = when (idiomaSeleccionado) {
            "English" -> "en"
            "Português" -> "pt"
            else -> "es"
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
    }
    
    override fun onResume() {
        super.onResume()
        // Si el tamaño de la fuente fue modificado en configuraciones, recargar la pantalla
        val nuevaEscala = com.example.rest.utils.ThemeManager.getFontSizeScale(this)
        if (nuevaEscala != escaladoFuenteActual) {
            recreate()
        }
    }
    
    /**
     * Configura el modo de pantalla completa ocultando las barras del sistema
     */
    private fun configurarPantallaCompleta() {
        // Habilitar modo edge-to-edge
        enableEdgeToEdge()
        
        // Ocultar las barras del sistema
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
