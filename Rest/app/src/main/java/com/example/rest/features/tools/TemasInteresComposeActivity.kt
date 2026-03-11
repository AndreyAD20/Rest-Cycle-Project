package com.example.rest.features.tools

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rest.BaseComposeActivity
import com.example.rest.data.PreferenciasInteresManager
import com.example.rest.ui.theme.Acento
import com.example.rest.ui.theme.Blanco
import com.example.rest.ui.theme.Primario
import com.example.rest.ui.theme.TemaRest

class TemasInteresComposeActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode = com.example.rest.utils.ThemeManager.isDarkMode(this)
            TemaRest(temaOscuro = isDarkMode) {
                TemasInteresScreen(
                    onBackClick = { finish() },
                    onSaveComplete = {
                        Toast.makeText(this, "Preferencias guardadas", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TemasInteresScreen(onBackClick: () -> Unit, onSaveComplete: () -> Unit) {
    val context = LocalContext.current
    
    // Lista maquetada de temas principales
    val temasDisponibles = listOf(
        "Deporte", "Música", "Arte", "Tecnología", "Negocios", "Cine", "Salud", "Videojuegos",
        "Ciencia", "Literatura", "Viajes", "Gastronomía", "Moda", "Fotografía", "Historia"
    )

    // Estado con los temas seleccionados actualmente (recuperados de la memoria si ya existen)
    var temasSeleccionados by remember { 
        mutableStateOf(PreferenciasInteresManager.obtenerTemas(context).toSet()) 
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text("Temas de Interés", fontWeight = FontWeight.Bold, color = Blanco) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Regresar", tint = Blanco)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Primario
                )
            )
        },
        bottomBar = {
            // Botón fijo abajo para guardar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        PreferenciasInteresManager.guardarTemas(context, temasSeleccionados.toList())
                        onSaveComplete()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Acento),
                    shape = RoundedCornerShape(12.dp),
                    enabled = temasSeleccionados.isNotEmpty()
                ) {
                    Text(
                        "Guardar Preferencias", 
                        color = Blanco, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
        ) {
            item {
                Text(
                    text = "Selecciona tus Temas Favoritos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Personalizaremos tu experiencia y te mostraremos noticias, consejos y reflexiones basadas en lo que a ti te importa.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    temasDisponibles.forEach { tema ->
                        val isSelected = temasSeleccionados.contains(tema)
                        
                        // Animación suave del color de fondo
                        val backgroundColor by animateColorAsState(
                            targetValue = if (isSelected) Primario else MaterialTheme.colorScheme.surfaceVariant,
                            animationSpec = tween(durationMillis = 300)
                        )
                        
                        val contentColor = if (isSelected) Blanco else MaterialTheme.colorScheme.onSurfaceVariant

                        // Custom Chip interactivo
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(backgroundColor)
                                .clickable {
                                    temasSeleccionados = if (isSelected) {
                                        temasSeleccionados - tema
                                    } else {
                                        temasSeleccionados + tema
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = contentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = tema,
                                    color = contentColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
