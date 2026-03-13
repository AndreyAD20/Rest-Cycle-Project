package com.example.rest

import androidx.compose.ui.graphics.Color

data class AppBloqueo(
    val nombre: String,
    val packageName: String,
    val iconColorValue: Int = 0xFF000000.toInt(),
    var limitMinutes: Int = 0, // Changed from limitHours to limitMinutes
    var usageTime: Long = 0
) {
    val iconColor: Color
        get() = Color(iconColorValue)
}
