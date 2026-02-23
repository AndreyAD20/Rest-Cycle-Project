package com.example.rest.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.rest.features.tools.AppBloqueo
import com.example.rest.data.models.AppVinculada
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocalBlockingRepository(private val context: Context) {

    private val sharedPref = context.getSharedPreferences("RestCycleBloqueo", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Obtener apps instaladas (filtrando apps de sistema y las ya enlazadas)
    fun getInstalledApps(excludePackages: Set<String> = emptySet()): List<AppBloqueo> {
        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val appBloqueoList = mutableListOf<AppBloqueo>()

        for (appInfo in installedApps) {
            // Solo apps con icono en el launcher (descarta servicios/procesos de fondo)
            if (pm.getLaunchIntentForPackage(appInfo.packageName) == null) continue

            // Ignorar nuestra propia app
            if (appInfo.packageName == context.packageName) continue

            // Ignorar apps ya enlazadas
            if (appInfo.packageName in excludePackages) continue

            // Filtrar apps puramente de sistema (sin actualización del usuario)
            // FLAG_UPDATED_SYSTEM_APP = usuario instaló una actualización → mostrar
            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystemApp = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            if (isSystemApp && !isUpdatedSystemApp) continue

            val name = pm.getApplicationLabel(appInfo).toString()
            val icon = pm.getApplicationIcon(appInfo)
            val color = extractColorFromIcon(icon)

            appBloqueoList.add(
                AppBloqueo(
                    id = appInfo.packageName.hashCode(),
                    nombre = name,
                    iconColor = Color(color),
                    packageName = appInfo.packageName
                )
            )
        }
        return appBloqueoList.sortedBy { it.nombre }
    }

    // Guardar regla de bloqueo
    fun saveBlockedApp(app: AppBloqueo) {
        val currentList = getBlockedApps().toMutableList()
        val index = currentList.indexOfFirst { it.packageName == app.packageName }
        
        if (index != -1) {
            currentList[index] = app
        } else {
            currentList.add(app)
        }
        
        saveList(currentList)
    }

    // Obtener todas las apps bloqueadas/trackeadas
    fun getBlockedApps(): List<AppBloqueo> {
        val json = sharedPref.getString("blocked_apps_list", null) ?: return emptyList()
        val type = object : TypeToken<List<AppBloqueo>>() {}.type
        return gson.fromJson(json, type)
    }

    // Eliminar o dejar de trackear una app
    fun removeBlockedApp(packageName: String) {
        val currentList = getBlockedApps().toMutableList()
        currentList.removeAll { it.packageName == packageName }
        saveList(currentList)
    }

    private fun saveList(list: List<AppBloqueo>) {
        val json = gson.toJson(list)
        sharedPref.edit().putString("blocked_apps_list", json).apply()
    }

    // Actualizar lista completa desde sincronización
    fun updateBlockedApps(newList: List<AppBloqueo>) {
        saveList(newList)
    }

    // Helper para extraer color prominente (simplificado)
    private fun extractColorFromIcon(drawable: Drawable): Int {
        // En una implementación real usaríamos Palette API.
        // Aquí devolvemos un color hash o fijo por simplicidad y rendimiento
        return try {
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                // Tomar pixel central
                bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
            } else {
                android.graphics.Color.GRAY
            }
        } catch (e: Exception) {
            android.graphics.Color.GRAY
        }
    }

    // Actualizar lista con estadísticas de uso de HOY
    fun updateAppsWithUsage(apps: List<AppBloqueo>): List<AppBloqueo> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? android.app.usage.UsageStatsManager
        
        if (usageStatsManager == null) return apps

        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        // Query usage stats with compatibility check
        val usageMap = mutableMapOf<String, Long>()
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            // API 28+
            val statsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
            for ((pkg, usage) in statsMap) {
                usageMap[pkg] = usage.totalTimeInForeground
            }
        } else {
            // API < 28 (Manual aggregation)
            val statsList = usageStatsManager.queryUsageStats(
                android.app.usage.UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
            if (statsList != null) {
                for (usage in statsList) {
                    val current = usageMap[usage.packageName] ?: 0L
                    usageMap[usage.packageName] = current + usage.totalTimeInForeground
                }
            }
        }
        
        return apps.map { app ->
            val totalTime = usageMap[app.packageName] ?: 0L
            var minutesUsed = 0
            if (totalTime > 0) {
                minutesUsed = (totalTime / 60000).toInt()
            }
            app.copy(usageMinutes = minutesUsed)
        }
    }
}
