package com.example.rest.data.models

import android.graphics.drawable.Drawable

/**
 * Modelo para información de uso de aplicación (UI)
 */
data class AppUsageInfo(
    val appName: String,
    val packageName: String,
    val totalTimeInMillis: Long,
    val icon: Drawable? = null
)
