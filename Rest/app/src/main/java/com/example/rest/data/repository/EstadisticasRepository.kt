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
     * 1. Verifica/Crea el dispositivo.
     * 2. Recorre las apps y actualiza o crea los registros en apps_vinculadas.
     */
    suspend fun sincronizarEstadisticas(context: Context, stats: List<AppUsageInfo>, nombreDispositivo: String = "Android Device") {
        withContext(Dispatchers.IO) {
            try {
                // 1. Obtener ID del usuario actual (desde SharedPreferences o similar, aquí asumiremos que está logueado y guardado)
                val sharedPref = context.getSharedPreferences("RestCyclePrefs", Context.MODE_PRIVATE)
                val userId = sharedPref.getInt("ID_USUARIO", -1)
                
                if (userId == -1) {
                    Log.e(TAG, "No hay usuario logueado para sincronizar.")
                    return@withContext
                }

                // 2. Gestionar Dispositivo
                val dispositivoId = obtenerOregistrarDispositivo(userId, nombreDispositivo) ?: return@withContext

                // 3. Sincronizar cada App
                // Primero obtenemos las apps existentes para saber si hacer update o insert
                val appsExistentesResponse = api.obtenerAppsVinculadas(idDispositivo = "eq.$dispositivoId")
                val appsExistentes = if (appsExistentesResponse.isSuccessful) {
                    appsExistentesResponse.body() ?: emptyList()
                } else {
                    emptyList()
                }

                val appsMap = appsExistentes.associateBy { it.nombre.lowercase() }

                Log.d(TAG, "Sincronizando ${stats.size} apps. Apps existentes en BD: ${appsExistentes.size}")

                stats.forEach { appStat ->
                    // Convertir tiempo a minutos (schema es integer)
                    val tiempoMinutos = (appStat.totalTimeInMillis / 60000).toInt()
                    
                    val appExistente = appsMap[appStat.appName.lowercase()]
                    val categoria = inferirCategoria(context, appStat.packageName)
                    
                    if (appExistente != null) {
                        // UPDATE
                        Log.d(TAG, "Actualizando: ${appStat.appName} (${tiempoMinutos}min, cat: $categoria)")
                        // Actualizar si el tiempo cambió O si la categoría es diferente
                        val needsUpdate = appExistente.tiempoUso != tiempoMinutos || 
                                         appExistente.categoria != categoria
                        
                        if (needsUpdate) {
                             val updateData = mutableMapOf<String, Any>(
                                "tiempouso" to tiempoMinutos,
                                "categoria" to categoria // Siempre actualizar categoría
                            )
                            
                            val updateResponse = api.actualizarAppVinculada("eq.${appExistente.id}", updateData)
                            if (!updateResponse.isSuccessful) {
                                Log.e(TAG, "Error actualizando ${appStat.appName}: ${updateResponse.code()}")
                            } else {
                                Log.d(TAG, "✓ Actualizado correctamente")
                            }
                        } else {
                            Log.d(TAG, "Sin cambios, omitiendo actualización")
                        }
                    } else {
                        // INSERT
                        Log.d(TAG, "Insertando nueva app: ${appStat.appName} (${tiempoMinutos}min, cat: $categoria)")
                        val nuevaApp = AppVinculada(
                            idDispositivo = dispositivoId,
                            nombre = appStat.appName,
                            tiempoUso = tiempoMinutos,
                            categoria = categoria,
                            tiempoLimite = 0 // Default
                        )
                        val createResponse = api.crearAppVinculada(nuevaApp)
                        if (!createResponse.isSuccessful) {
                            Log.e(TAG, "Error insertando ${appStat.appName}: ${createResponse.code()}")
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
                    val nuevoDispositivo = Dispositivo(
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
