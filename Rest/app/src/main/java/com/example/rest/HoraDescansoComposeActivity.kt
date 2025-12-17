package com.example.rest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rest.ui.theme.*

class HoraDescansoComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                PantallaHoraDescanso(onBackClick = { finish() })
            }
        }
    }
}

data class HorarioDescanso(
    val id: Int,
    val nombre: String,
    val horaInicio: String,
    val horaFin: String,
    val diasActivos: List<Boolean> = List(7) { true }, // L M M J V S D
    val activo: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHoraDescanso(onBackClick: () -> Unit) {
    // Gradiente de fondo
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF80DEEA), Primario),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    val horarios = remember {
        mutableStateListOf(
            HorarioDescanso(1, "Horario 1", "10:00 PM", "6:00 AM"),
            HorarioDescanso(2, "Horario 2", "3:00 PM", "5:00 AM", activo = false)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Regresar", tint = Negro)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Add new schedule */ },
                containerColor = Color(0xFF00BCD4),
                contentColor = Negro,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, "Agregar Otro")
            }
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
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Intro Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home, // Placeholder for Owl/Sleep
                                contentDescription = "Icono Descanso",
                                modifier = Modifier.size(48.dp),
                                tint = Negro
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Aquí puedes establecer horas para bloquear el dispositivo de tu hijo.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = Negro
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                items(horarios) { horario ->
                    HorarioCard(horario)
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                item {
                     Spacer(modifier = Modifier.height(64.dp))
                }
            }
        }
    }
}

@Composable
fun HorarioCard(horario: HorarioDescanso) {
    var activo by remember { mutableStateOf(horario.activo) }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = horario.nombre,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Negro
                )
                Switch(
                    checked = activo,
                    onCheckedChange = { activo = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Blanco,
                        checkedTrackColor = Color(0xFF00BCD4),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Blanco
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${horario.horaInicio} - ${horario.horaFin}",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Negro
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Días Activos",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Negro
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dias = listOf("L", "M", "M", "J", "V", "S", "D")
                dias.forEachIndexed { index, dia ->
                    DaySelector(dia, selected = index < 5) // Mock selection
                }
            }
            
             Spacer(modifier = Modifier.height(8.dp))
             
             Row(
                 modifier = Modifier.fillMaxWidth(),
                 horizontalArrangement = Arrangement.End
             ) {
                 IconButton(onClick = { /* TODO: Settings */ }) {
                     Icon(Icons.Default.Settings, contentDescription = "Configurar", tint = Negro)
                 }
             }
        }
    }
}

@Composable
fun DaySelector(text: String, selected: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (selected) Blanco else Color.Transparent)
            .border(1.dp, if (selected) Color.Transparent else Negro, CircleShape)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Negro
        )
    }
}
