package com.example.rest.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Esquema de colores claro usando los colores definidos
private val LightColorScheme = lightColorScheme(
    primary = Primario,
    onPrimary = Blanco,
    primaryContainer = Primario,
    onPrimaryContainer = TextoPrimario,
    
    secondary = Acento,
    onSecondary = Blanco,
    secondaryContainer = AcentoClaro,
    onSecondaryContainer = TextoPrimario,
    
    tertiary = CyanRest,
    onTertiary = Blanco,
    
    background = FondoPrimario,
    onBackground = TextoPrimario,
    
    surface = FondoPrimario,
    onSurface = TextoPrimario,
    surfaceVariant = FondoSecundario,
    onSurfaceVariant = TextoSecundario,
    
    error = EstadoError,
    onError = Blanco,
    
    outline = Borde,
    outlineVariant = Borde
)

// Esquema de colores oscuro (opcional, usando los mismos colores por ahora)
private val DarkColorScheme = darkColorScheme(
    primary = Primario,
    onPrimary = Negro,
    primaryContainer = Primario,
    onPrimaryContainer = Blanco,
    
    secondary = Acento,
    onSecondary = Negro,
    secondaryContainer = AcentoClaro,
    onSecondaryContainer = Blanco,
    
    tertiary = CyanRest,
    onTertiary = Negro,
    
    background = TextoSecundario,
    onBackground = Blanco,
    
    surface = TextoSecundario,
    onSurface = Blanco,
    surfaceVariant = TextoPrimario,
    onSurfaceVariant = FondoSecundario,
    
    error = EstadoError,
    onError = Blanco,
    
    outline = Borde,
    outlineVariant = Borde
)

@Composable
fun TemaRest(
    temaOscuro: Boolean = isSystemInDarkTheme(),
    contenido: @Composable () -> Unit
) {
    val esquemaColor = if (temaOscuro) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
    
    val vista = LocalView.current
    if (!vista.isInEditMode) {
        SideEffect {
            val ventana = (vista.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(ventana, false)
            WindowCompat.getInsetsController(ventana, vista).isAppearanceLightStatusBars = !temaOscuro
        }
    }

    MaterialTheme(
        colorScheme = esquemaColor,
        typography = Typography,
        content = contenido
    )
}
