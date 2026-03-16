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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Density
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

// Esquema de colores oscuro con tonos NEGROS y muy oscuros
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00E5FF), // Cyan muy brillante para contraste
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF004D56), // Cyan muy oscuro
    onPrimaryContainer = Color(0xFFE0F7FA),
    
    secondary = Color(0xFFFF4081), // Rosa brillante
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF4A0E2F),
    onSecondaryContainer = Color(0xFFFCE4EC),
    
    tertiary = Color(0xFF26C6DA),
    onTertiary = Color(0xFF000000),
    
    background = Color(0xFF000000), // NEGRO PURO
    onBackground = Color(0xFFFFFFFF), // Texto blanco puro
    
    surface = Color(0xFF0A0A0A), // Casi negro
    onSurface = Color(0xFFF5F5F5), // Texto casi blanco
    surfaceVariant = Color(0xFF1A1A1A), // Gris muy oscuro
    onSurfaceVariant = Color(0xFFE0E0E0),
    
    error = Color(0xFFFF5252),
    onError = Color(0xFF000000),
    
    outline = Color(0xFF424242),
    outlineVariant = Color(0xFF212121)
)

@Composable
fun TemaRest(
    temaOscuro: Boolean = isSystemInDarkTheme(),
    fontScale: Float? = null,
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
            val context = vista.context
            if (context is Activity) {
                val ventana = context.window
                WindowCompat.setDecorFitsSystemWindows(ventana, false)
                WindowCompat.getInsetsController(ventana, vista).isAppearanceLightStatusBars = !temaOscuro
            }
        }
    }

    val context = LocalContext.current
    val currentFontScale = fontScale ?: com.example.rest.utils.ThemeManager.getFontSizeScale(context)
    val density = LocalDensity.current

    CompositionLocalProvider(
        LocalDensity provides Density(density.density, fontScale = currentFontScale * density.fontScale)
    ) {
        MaterialTheme(
            colorScheme = esquemaColor,
            typography = Typography,
            content = contenido
        )
    }
}
