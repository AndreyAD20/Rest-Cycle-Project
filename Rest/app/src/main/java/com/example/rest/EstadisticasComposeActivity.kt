package com.example.rest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
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

class EstadisticasComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                PantallaEstadisticas(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEstadisticas(onBackClick: () -> Unit) {
    // Gradiente de fondo
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF80DEEA), Primario),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    var periodoSeleccionado by remember { mutableStateOf(0) }
    val periodos = listOf("Diario", "Semanal", "Mensual")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Estadísticas de Uso", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Regresar", tint = Negro)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brochaGradiente)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Selector de Periodo
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    TabRow(
                        selectedTabIndex = periodoSeleccionado,
                        containerColor = Color.Transparent,
                        contentColor = Negro,
                        divider = {}
                    ) {
                        periodos.forEachIndexed { index, titulo ->
                            Tab(
                                selected = periodoSeleccionado == index,
                                onClick = { periodoSeleccionado = index },
                                text = { Text(titulo, fontWeight = if (periodoSeleccionado == index) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Gráfico
                item {
                    GraficoUso(periodoSeleccionado)
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Lista de Apps
                item {
                   ListaUsoApps()
                   Spacer(modifier = Modifier.height(32.dp))
                }
                
                // Advertencia
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5252)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning, 
                                contentDescription = null, 
                                tint = Blanco,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                "El uso excesivo de redes sociales puede ser perjudicial para el desarrollo cognitivo.",
                                color = Blanco,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun GraficoUso(periodo: Int) {
    // Datos simulados según periodo
    val datos = when (periodo) {
        0 -> listOf(0.3f, 0.5f, 0.2f, 0.8f, 0.4f) // Diario
        1 -> listOf(0.5f, 0.7f, 0.4f, 0.9f, 0.6f) // Semanal
        else -> listOf(0.6f, 0.8f, 0.5f, 0.7f, 0.9f) // Mensual
    }
    val colores = listOf(Color(0xFF6750A4), Color(0xFF4CAF50), Color(0xFFFFEB3B), Color(0xFFFF9800), Color(0xFFF44336))
    val etiquetas = listOf("FB", "WA", "SC", "GL", "YT")

    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().height(250.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                datos.forEachIndexed { index, valor ->
                    val alturaAnimada by animateFloatAsState(targetValue = valor)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .fillMaxHeight(alturaAnimada)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(colores[index % colores.size])
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                etiquetas.forEach { 
                    Text(it, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ListaUsoApps() {
    val apps = listOf(
        "Facebook" to "6 h 30 m",
        "WhatsApp" to "4 h 30 m",
        "Snapchat" to "2 h",
        "Google" to "1 h",
        "YouTube" to "45 m"
    )
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            apps.forEach { (nombre, tiempo) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(nombre, style = MaterialTheme.typography.bodyLarge)
                    Text(tiempo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                if (apps.last() != (nombre to tiempo)) {
                   Divider(color = Color.LightGray)
                }
            }
        }
    }
}
