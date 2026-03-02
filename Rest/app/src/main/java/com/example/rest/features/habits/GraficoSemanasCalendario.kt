package com.example.rest.features.habits

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rest.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun GraficoSemanasCalendario(context: Context) {
    val historialRepo = remember { com.example.rest.data.repository.HistorialAppsRepository() }
    var estadisticas by remember { mutableStateOf<Map<String, Pair<String, Int>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    // Obtener ID del dispositivo desde SharedPreferences
    val prefs = com.example.rest.utils.PreferencesManager(context)
    val userId = prefs.getUserId()
    
    LaunchedEffect(Unit) {
        if (userId != -1) {
            scope.launch {
                // Obtener dispositivos del usuario
                try {
                    val api = com.example.rest.network.SupabaseClient.api
                    val response = api.obtenerDispositivosPorUsuario("eq.$userId")
                    
                    if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                        val dispositivoId = response.body()!![0].id ?: return@launch
                        
                        when (val statsResult = historialRepo.obtenerEstadisticasPorSemanas(dispositivoId, 3)) {
                            is com.example.rest.data.repository.HistorialAppsRepository.Result.Success -> {
                                estadisticas = statsResult.data
                                isLoading = false
                            }
                            else -> {
                                isLoading = false
                            }
                        }
                    } else {
                        isLoading = false
                    }
                } catch (e: Exception) {
                    Log.e("GraficoSemanal", "Error: ${e.message}")
                    isLoading = false
                }
            }
        } else {
            isLoading = false
        }
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.9f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primario)
            }
        } else if (estadisticas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay datos de semanas anteriores",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Negro.copy(alpha = 0.5f)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(
                    "Últimas 3 Semanas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Negro
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Gráfico de barras horizontales
                val maxTiempo = estadisticas.values.maxOfOrNull { it.second } ?: 1
                
                estadisticas.entries.sortedBy { it.key }.forEach { (semana, data) ->
                    val (etiqueta, tiempoMinutos) = data
                    val porcentaje = if (maxTiempo > 0) tiempoMinutos.toFloat() / maxTiempo.toFloat() else 0f
                    val horas = tiempoMinutos / 60
                    val minutos = tiempoMinutos % 60
                    
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                semana,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(80.dp)
                            )
                            Text(
                                etiqueta,
                                style = MaterialTheme.typography.bodySmall,
                                color = Negro.copy(alpha = 0.6f),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                if (horas > 0) "${horas}h ${minutos}m" else "${minutos}m",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primario
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE0E0E0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(porcentaje.coerceAtLeast(0.05f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF1976D2),
                                                Color(0xFF64B5F6)
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
