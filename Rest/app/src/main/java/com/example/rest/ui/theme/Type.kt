package com.example.rest.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.rest.R

// Configuración de Google Fonts
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Fuente Istok Web (para cuerpo de texto y campos de entrada)
val istokWebFontName = GoogleFont("Istok Web")
val istokWebFontFamily = FontFamily(
    Font(googleFont = istokWebFontName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = istokWebFontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = istokWebFontName, fontProvider = provider, weight = FontWeight.Bold)
)

// Fuente Itim (para títulos y encabezados)
val itimFontName = GoogleFont("Itim")
val itimFontFamily = FontFamily(
    Font(googleFont = itimFontName, fontProvider = provider, weight = FontWeight.Normal)
)

// Tipografía personalizada usando las dimensiones del archivo dimenciones.xml
val Typography = Typography(
    // Títulos grandes - Itim
    displayLarge = TextStyle(
        fontFamily = itimFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp, // tamano_texto_titulo_grande
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = itimFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp, // tamano_texto_titulo
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    
    // Títulos - Itim
    headlineLarge = TextStyle(
        fontFamily = itimFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp, // tamano_texto_titulo
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = itimFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp, // tamano_texto_grande
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    
    // Cuerpo de texto - Istok Web
    bodyLarge = TextStyle(
        fontFamily = istokWebFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp, // tamano_texto_medio
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = istokWebFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp, // tamano_texto_pequeno
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = istokWebFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp, // tamano_texto_muy_pequeno
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    
    // Labels - Istok Web
    labelLarge = TextStyle(
        fontFamily = istokWebFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp, // tamano_texto_medio
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = istokWebFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp, // tamano_texto_pequeno
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = istokWebFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp, // tamano_texto_muy_pequeno
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)
