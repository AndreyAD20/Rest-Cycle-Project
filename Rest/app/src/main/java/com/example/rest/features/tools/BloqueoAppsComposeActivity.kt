package com.example.rest.features.tools

import android.os.Bundle
import com.example.rest.BaseComposeActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
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

class BloqueoAppsComposeActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                PantallaBloqueoApps(onBackClick = { finish() })
            }
        }
    }
}

data class AppBloqueo(
    val nombre: String,
    val iconColor: Color, // Placeholder for real icon
    val limitHours: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBloqueoApps(onBackClick: () -> Unit) {
    // Gradiente de fondo
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF80DEEA), Primario),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    val apps = remember {
        mutableStateListOf(
            AppBloqueo("YouTube", Color(0xFFFF0000)),
            AppBloqueo("TikTok", Color(0xFF000000)),
            AppBloqueo("Facebook", Color(0xFF1877F2)),
            AppBloqueo("Instagram", Color(0xFFE1306C)),
            AppBloqueo("Juegos", Color(0xFF4CAF50)),
            AppBloqueo("X / Twitter", Color(0xFF000000))
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Bloqueo de Apps",
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Encabezado tipo "Bocadillo" o Tarjeta
                Card(
                    colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExpandedText(
                            text = "Aquí puedes seleccionar las apps que creas que te distraen, esto te ayudará a mejorar tu enfoque y concentración."
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.Lock, // Placeholder for Owl
                            contentDescription = "Icono Bloqueo",
                            modifier = Modifier.size(48.dp),
                            tint = Negro
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tiempo uso diario:",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Negro
                    )
                    
                    FloatingActionButton(
                        onClick = { /* TODO: Add specific rule */ },
                        containerColor = Color(0xFF00BCD4),
                        contentColor = Negro,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Add, "Agregar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de Apps
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(apps) { app ->
                        AppBloqueoItem(app)
                    }
                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandedText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = Negro,
        modifier = Modifier.widthIn(max = 200.dp)
    )
}

@Composable
fun AppBloqueoItem(app: AppBloqueo) {
    var limit by remember { mutableStateOf(app.limitHours) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Placeholder App Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(app.iconColor),
                    contentAlignment = Alignment.Center
                ) {
                   Text(
                       app.nombre.take(1),
                       color = Blanco,
                       fontWeight = FontWeight.Bold
                   )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    app.nombre,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Negro
                )
            }

            // Simple Limit Selector
            Surface(
                color = if(limit > 0) Color(0xFF00BCD4) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable { 
                    // Simple cycle for demo purposes
                    limit = (limit + 1) % 5 
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if(limit > 0) "${limit}h" else "Off",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if(limit > 0) Blanco else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if(limit > 0) Blanco else Color.Gray
                    )
                }
            }
        }
    }
}





