package com.example.rest.data.repository

import android.content.Context
import android.util.Log
import com.example.rest.data.models.AppUsageInfo
import com.example.rest.data.models.AppVinculada
import com.example.rest.data.models.Dispositivo
import com.example.rest.network.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para sincronizar estadísticas con Supabase
 */
class EstadisticasRepository {

    private val api = SupabaseClient.api
    private val TAG = "EstadisticasRepo"

    /**
     * Sincronizar estadísticas de uso con la base de datos
     * Ahora usa historial_apps para estadísticas (no apps_vinculadas)
     * 1. Verifica/Crea el dispositivo.
     * 2. Registra el uso de apps en historial_apps para el día actual.
     */
    suspend fun sincronizarEstadisticas(context: Context, stats: List<AppUsageInfo>, nombreDispositivo: String = "Android Device") {
        withContext(Dispatchers.IO) {
            try {
                // 1. Obtener ID del usuario actual
                val sharedPref = context.getSharedPreferences("RestCyclePrefs", Context.MODE_PRIVATE)
                val userId = sharedPref.getInt("ID_USUARIO", -1)
                
                if (userId == -1) {
                    Log.e(TAG, "No hay usuario logueado para sincronizar.")
                    return@withContext
                }

                // 2. Gestionar Dispositivo
                val dispositivoId = obtenerOregistrarDispositivo(userId, nombreDispositivo) ?: return@withContext
                
                // Guardar ID de dispositivo en SharedPreferences para el servicio
                with(sharedPref.edit()) {
                    putInt("ID_DISPOSITIVO", dispositivoId)
                    apply()
                }

                // 3. Obtener fecha actual
                val fechaHoy = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(java.util.Date())
                
                // 4. Obtener historial existente de hoy
                val historialHoyResponse = api.obtenerHistorialAppsPorFecha(
                    idDispositivo = "eq.$dispositivoId",
                    fecha = "eq.$fechaHoy"
                )
                
                val historialHoy = if (historialHoyResponse.isSuccessful) {
                    historialHoyResponse.body() ?: emptyList()
                } else {
                    emptyList()
                }

                val historialMap = historialHoy.associateBy { it.nombrePaquete }

                Log.d(TAG, "Sincronizando stats (${stats.size}) en historial_apps")
                
                stats.forEach { appStat ->
                    val packageName = appStat.packageName
                    val appName = appStat.appName
                    
                    // Calcular tiempo y nombre
                    val tiempoMinutos = ((appStat.totalTimeInMillis) / 60000).toInt()
                    
                    // Solo registrar si tiene tiempo de uso > 0
                    if (tiempoMinutos <= 0) {
                        return@forEach
                    }
                    
                    Log.d(TAG, "Procesando: $appName - ${tiempoMinutos}min")
                    
                    val categoria = inferirCategoria(context, packageName)
                    val historialExistente = historialMap[packageName]
                    
                    // Guardar en historial
                    if (historialExistente != null) {
                        // UPDATE - Actualizar si cambiaron los datos
                        // Preservamos el número de aperturas que ya tenía
                        if (historialExistente.tiempoUso != tiempoMinutos) {
                             
                             val updateData = mapOf<String, Any>(
                                 "tiempo_uso" to tiempoMinutos,
                                 "fecha" to fechaHoy
                             )
                             
                             val updateResponse = api.actualizarHistorialApp("eq.${historialExistente.id}", updateData)
                             if (updateResponse.isSuccessful) {
                                 Log.d(TAG, "Actualizado: $appName ($tiempoMinutos min)")
                             } else {
                                 Log.e(TAG, "Error actualizando $appName: ${updateResponse.code()}")
                             }
                        }
                    } else {
                        // INSERT - Crear nuevo registro
                        Log.d(TAG, "Insertando: $appName ($tiempoMinutos min)")
                        
                        val nuevoHistorial = com.example.rest.data.models.HistorialAppInput(
                            idDispositivo = dispositivoId,
                            nombre = appName,
                            nombrePaquete = packageName,
                            categoria = categoria,
                            tiempoUso = tiempoMinutos,
                            numeroAperturas = 1, // Valor inicial estimado
                            fecha = fechaHoy
                        )
                        
                        val createResponse = api.crearHistorialApp(nuevoHistorial)
                        if (!createResponse.isSuccessful) {
                            Log.e(TAG, "Error insertando $appName: ${createResponse.code()}")
                        }
                    }
                }
                
                Log.d(TAG, "Sincronización completada con éxito.")

            } catch (e: Exception) {
                Log.e(TAG, "Error en sincronización: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun inferirCategoria(context: Context, packageName: String): String {
        val pm = context.packageManager
        val pkgLower = packageName.lowercase()

        // 1. Intentar usar categoría del sistema (API 26+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                when (appInfo.category) {
                    android.content.pm.ApplicationInfo.CATEGORY_GAME -> return "Juegos"
                    android.content.pm.ApplicationInfo.CATEGORY_SOCIAL -> return "Redes Sociales"
                    android.content.pm.ApplicationInfo.CATEGORY_VIDEO -> return "Entretenimiento"
                    android.content.pm.ApplicationInfo.CATEGORY_AUDIO -> return "Música y Audio"
                    android.content.pm.ApplicationInfo.CATEGORY_PRODUCTIVITY -> return "Productividad"
                    android.content.pm.ApplicationInfo.CATEGORY_MAPS -> return "Mapas y Navegación"
                    android.content.pm.ApplicationInfo.CATEGORY_NEWS -> return "Noticias"
                }
            } catch (e: Exception) {
                // Fallback si falla
            }
        }

        // 2. Inferencia por nombre de paquete (Keywords comunes)
        return when {
            pkgLower.contains("facebook") || pkgLower.contains("instagram") || 
            pkgLower.contains("twitter") || pkgLower.contains("tiktok") || 
            pkgLower.contains("snapchat") || pkgLower.contains("whatsapp") || 
            pkgLower.contains("telegram") || pkgLower.contains("discord") ||
            pkgLower.contains("messenger") || pkgLower.contains("threads") -> "Redes Sociales"

            pkgLower.contains("youtube") || pkgLower.contains("netflix") || 
            pkgLower.contains("disney") || pkgLower.contains("prime") || 
            pkgLower.contains("hbo") || pkgLower.contains("twitch") || 
            pkgLower.contains("spotify") || pkgLower.contains("music") ||
            pkgLower.contains("player") -> "Entretenimiento"

            pkgLower.contains("game") || pkgLower.contains("play") || 
            pkgLower.contains("rovio") || pkgLower.contains("supercell") || 
            pkgLower.contains("activision") || pkgLower.contains("unity") ||
            pkgLower.contains("pubg") || pkgLower.contains("callofduty") ||
            pkgLower.contains("freefire") || pkgLower.contains("pvz") ||
            pkgLower.contains("zombie") || pkgLower.contains("plant") -> "Juegos"

            pkgLower.contains("kindle") || pkgLower.contains("audible") || 
            pkgLower.contains("book") || pkgLower.contains("reader") ||
            pkgLower.contains("wattpad") || pkgLower.contains("comic") ||
            pkgLower.contains("manga") -> "Lectura"

            pkgLower.contains("maps") || pkgLower.contains("waze") || 
            pkgLower.contains("uber") || pkgLower.contains("didiglobal") ||
            pkgLower.contains("moovit") -> "Mapas y Navegación"

            pkgLower.contains("chrome") || pkgLower.contains("firefox") || 
            pkgLower.contains("browser") || pkgLower.contains("edge") ||
            pkgLower.contains("office") || pkgLower.contains("docs") ||
            pkgLower.contains("sheet") || pkgLower.contains("slides") -> "Productividad"

            pkgLower.contains("health") || pkgLower.contains("fitness") || 
            pkgLower.contains("sport") || pkgLower.contains("meditation") ||
            pkgLower.contains("rest") || pkgLower.contains("diet") ||
            pkgLower.contains("workout") -> "Salud"

            pkgLower.contains("android") || pkgLower.contains("google.android") || 
            pkgLower.contains("samsung") || pkgLower.contains("xiaomi") ||
            pkgLower.contains("huawei") || pkgLower.contains("system") -> "Sistema"

            else -> "Otros"
        }
    }

    private suspend fun obtenerOregistrarDispositivo(userId: Int, nombre: String): Int? {
        return try {
            // Buscar dispositivos de este usuario
            val response = api.obtenerDispositivosPorUsuario("eq.$userId")
            if (response.isSuccessful) {
                val dispositivos = response.body() ?: emptyList()
                
                // Intentar encontrar uno con el mismo nombre (simplificación)
                // En producción usaríamos un Android ID único o UUID guardado localmente
                val dispositivoExistente = dispositivos.find { it.nombre == nombre }
                
                if (dispositivoExistente != null) {
                    return dispositivoExistente.id
                } else {
                    // Crear nuevo
                    val nuevoDispositivo = com.example.rest.data.models.DispositivoInput(
                        idUsuario = userId,
                        nombre = nombre,
                        ip = "127.0.0.1", // Placeholder, o obtener real
                        estado = "activo"
                    )
                    val createResponse = api.crearDispositivo(nuevoDispositivo)
                    if (createResponse.isSuccessful && !createResponse.body().isNullOrEmpty()) {
                        return createResponse.body()!![0].id
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error gestionando dispositivo: ${e.message}")
            null
        }
    }
}
