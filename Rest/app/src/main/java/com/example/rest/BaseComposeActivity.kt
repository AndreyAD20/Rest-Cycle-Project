package com.example.rest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Actividad base para todas las actividades Compose del proyecto.
 * Configura automáticamente el modo edge-to-edge y oculta las barras del sistema.
 */
abstract class BaseComposeActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configurarPantallaCompleta()
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
