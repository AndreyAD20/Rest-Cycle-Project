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

    // Obtener apps instaladas (filtrando algunas de sistema si es necesario)
    fun getInstalledApps(): List<AppBloqueo> {
        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val appBloqueoList = mutableListOf<AppBloqueo>()

        for (appInfo in installedApps) {
            // Filtrar apps del sistema que no sean actualizables (básicas traseras)
            // Filtrar apps que no sean lanzables (sin icono en el launcher)
            // Esto elimina servicios de sistema y procesos de fondo irrelevantes para bloqueo
            if (pm.getLaunchIntentForPackage(appInfo.packageName) == null) {
                continue
            }
            
            // Ignorar nuestra propia app
            if (appInfo.packageName == context.packageName) continue

            val name = pm.getApplicationLabel(appInfo).toString()
            val icon = pm.getApplicationIcon(appInfo)
            
            // Generar un color basado en el icono (simplificado) o aleatorio
            val color = extractColorFromIcon(icon)

            // Usamos hashcode del paquete como ID temporal para la UI
            appBloqueoList.add(
                AppBloqueo(
                    id = appInfo.packageName.hashCode(),
                    nombre = name,
                    iconColor = Color(color),
                    packageName = appInfo.packageName // Necesitamos agregar este campo a AppBloqueo
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
}
