package com.example.rest.utils

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.rest.data.models.AppUsageStats
import java.util.*

/**
 * Helper class para obtener estadísticas de uso de aplicaciones
 */
object UsageStatsHelper {
    
    private const val TAG = "UsageStatsHelper"
    
    /**
     * Verifica si la app tiene permiso para acceder a estadísticas de uso
     */
    fun checkUsageStatsPermission(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            }
            val hasPermission = mode == AppOpsManager.MODE_ALLOWED
            Log.d(TAG, "checkUsageStatsPermission: $hasPermission")
            hasPermission
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permission", e)
            false
        }
    }
    
    /**
     * Abre la configuración para que el usuario otorgue el permiso
     */
    fun requestUsageStatsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
    
    /**
     * Obtiene estadísticas de uso para un período específico
     * @param context Contexto de la aplicación
     * @param period 0 = Hoy, 1 = Última semana, 2 = Último mes
     * @return Lista de estadísticas de apps ordenadas por tiempo de uso
     */
    fun getAppUsageStats(context: Context, period: Int): List<AppUsageStats> {
        Log.d(TAG, "getAppUsageStats called for period: $period")
        
        if (!checkUsageStatsPermission(context)) {
            Log.w(TAG, "No permission to access usage stats")
            return emptyList()
        }
        
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        
        // Calcular tiempo de inicio según período
        val startTime = when (period) {
            0 -> { // Hoy - desde medianoche
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            1 -> { // Última semana - últimos 7 días completos
                calendar.apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            2 -> { // Último mes - últimos 30 días completos
                calendar.apply {
                    add(Calendar.DAY_OF_YEAR, -30)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            else -> {
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
        }
        
        Log.d(TAG, "Querying stats from $startTime to $endTime")
        Log.d(TAG, "Time range: ${(endTime - startTime) / (1000 * 60 * 60)} hours")
        
        // Usar INTERVAL_BEST para obtener la mejor granularidad disponible
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            startTime,
            endTime
        )
        
        Log.d(TAG, "Raw usage stats count: ${usageStatsList?.size ?: 0}")
        
        if (usageStatsList.isNullOrEmpty()) {
            Log.w(TAG, "No usage stats available")
            return emptyList()
        }
        
        val packageManager = context.packageManager
        val appUsageList = mutableListOf<AppUsageStats>()
        
        // Agrupar por paquete y sumar tiempos
        val statsMap = mutableMapOf<String, Pair<Long, Long>>()
        
        for (stats in usageStatsList) {
            // Solo contar apps con tiempo de uso real
            if (stats.totalTimeInForeground > 0) {
                val existing = statsMap[stats.packageName]
                if (existing == null) {
                    statsMap[stats.packageName] = Pair(stats.totalTimeInForeground, stats.lastTimeUsed)
                } else {
                    val newTotalTime = existing.first + stats.totalTimeInForeground
                    val newLastUsed = maxOf(existing.second, stats.lastTimeUsed)
                    statsMap[stats.packageName] = Pair(newTotalTime, newLastUsed)
                }
            }
        }
        
        Log.d(TAG, "Apps with usage > 0: ${statsMap.size}")
        
        // Convertir a AppUsageStats y filtrar apps del sistema
        for ((packageName, timePair) in statsMap) {
            val (totalTime, lastUsed) = timePair
            
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                
                // Filtrar algunas apps del sistema que no son relevantes
                if (packageName.startsWith("com.android.systemui") ||
                    packageName.startsWith("com.google.android.inputmethod") ||
                    packageName == "android") {
                    continue
                }
                
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                val icon = try {
                    packageManager.getApplicationIcon(packageName)
                } catch (e: Exception) {
                    null
                }
                
                appUsageList.add(
                    AppUsageStats(
                        packageName = packageName,
                        appName = appName,
                        totalTimeInForeground = totalTime,
                        lastTimeUsed = lastUsed,
                        icon = icon
                    )
                )
                
                Log.d(TAG, "Added: $appName - ${formatTotalTime(totalTime)}")
            } catch (e: PackageManager.NameNotFoundException) {
                Log.d(TAG, "App not found: $packageName")
            } catch (e: Exception) {
                Log.e(TAG, "Error processing $packageName", e)
            }
        }
        
        Log.d(TAG, "Final app list count: ${appUsageList.size}")
        
        // Ordenar por tiempo de uso descendente
        val sorted = appUsageList.sortedByDescending { it.totalTimeInForeground }
        
        // Log top 5 para debug
        sorted.take(5).forEachIndexed { index, app ->
            Log.d(TAG, "Top ${index + 1}: ${app.appName} - ${app.getFormattedTime()}")
        }
        
        return sorted
    }
    
    /**
     * Obtiene el tiempo total de pantalla para un período
     */
    fun getTotalScreenTime(context: Context, period: Int): Long {
        val apps = getAppUsageStats(context, period)
        val total = apps.sumOf { it.totalTimeInForeground }
        Log.d(TAG, "Total screen time: $total ms (${formatTotalTime(total)})")
        return total
    }
    
    /**
     * Formatea el tiempo total en formato legible
     */
    fun formatTotalTime(timeInMillis: Long): String {
        val totalMinutes = timeInMillis / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }
}
