package com.example.rest.features.habits

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import com.example.rest.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun GraficoBarrasSemanal(
    datosDiarios: Map<String, Long>, // Map<DiaSemana, Millis>
    fechaInicio: String,
    fechaFin: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize()
        ) {
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Uso Diario",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Negro
                    )
                    Text(
                        "$fechaInicio - $fechaFin",
                        style = MaterialTheme.typography.bodySmall,
                        color = Negro.copy(alpha = 0.6f)
                    )
                }
                
                // Total Semanal (cálculo rápido)
                val totalSemanal = datosDiarios.values.sum()
                val horasTotal = TimeUnit.MILLISECONDS.toHours(totalSemanal)
                val minsTotal = TimeUnit.MILLISECONDS.toMinutes(totalSemanal) % 60
                
                Surface(
                    color = Primario.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Total: ${horasTotal}h ${minsTotal}m",
                        style = MaterialTheme.typography.labelMedium,
                        color = Primario,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (datosDiarios.isEmpty() || datosDiarios.values.all { it == 0L }) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Sin actividad esta semana",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                // Encontrar el valor máximo para escalar (con un mínimo de 1 hora para evitar división por 0 o gráficos feos)
                val maxMillis = (datosDiarios.values.maxOrNull() ?: 1L).coerceAtLeast(3600000L)
                val diasOrdenados = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // 1. LÍNEAS GUÍA DE FONDO (Grid Lines)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 3 líneas divisorias (100%, 50%, 0%)
                        repeat(3) { i ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.LightGray.copy(alpha = 0.3f))
                            )
                        }
                    }

                    // 2. BARRAS
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        diasOrdenados.forEachIndexed { index, dia ->
                            val millis = datosDiarios[dia] ?: 0L
                            val hours = TimeUnit.MILLISECONDS.toHours(millis).toFloat()
                            val minutesPart = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
                            
                            val fraction = millis.toFloat() / maxMillis.toFloat()
                            val animatedFraction by animateFloatAsState(
                                targetValue = fraction, 
                                label = "barHeight$index",
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                )
                            )
                            
                            val isMax = millis == datosDiarios.values.maxOrNull() && millis > 0
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                // Área de la barra (ocupa el espacio sobrante)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom
                                    ) {
                                        // Etiqueta flotante
                                        if (fraction > 0.1f) {
                                            val textoTiempo = if (hours >= 1) String.format("%.0fh", hours) else "${minutesPart}m"
                                            Text(
                                                textoTiempo,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isMax) Primario else Color.Gray,
                                                fontWeight = if (isMax) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = if (isMax) 11.sp else 10.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }

                                        // Barra
                                        Box(
                                            modifier = Modifier
                                                .width(if (isMax) 18.dp else 14.dp)
                                                .fillMaxHeight(animatedFraction.coerceAtLeast(0.02f))
                                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                                .background(
                                                    if (isMax) {
                                                        Brush.verticalGradient(
                                                            colors = listOf(
                                                                Color(0xFF29B6F6),
                                                                Primario
                                                            )
                                                        )
                                                    } else {
                                                        Brush.verticalGradient(
                                                            colors = listOf(
                                                                Color(0xFFB3E5FC),
                                                                Color(0xFF4FC3F7)
                                                            )
                                                        )
                                                    }
                                                )
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Día
                                Text(
                                    text = dia.take(1),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Negro,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
