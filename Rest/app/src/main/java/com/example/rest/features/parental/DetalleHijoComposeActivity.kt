package com.example.rest.features.parental

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rest.BaseComposeActivity
import com.example.rest.ui.theme.Negro
import com.example.rest.ui.theme.Primario
import com.example.rest.ui.theme.TemaRest

class DetalleHijoComposeActivity : BaseComposeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val idHijo = intent.getIntExtra("ID_HIJO", -1)
        val nombreHijo = intent.getStringExtra("NOMBRE_HIJO") ?: "Hijo"

        if (idHijo == -1) {
            Toast.makeText(this, "Error al cargar datos del hijo", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            val isDarkMode = com.example.rest.utils.ThemeManager.isDarkMode(this)
            TemaRest(temaOscuro = isDarkMode) {
                PantallaDetalleHijo(
                    nombreHijo = nombreHijo,
                    onBackClick = { finish() },
                    onFeatureClick = { featureName ->
                        when (featureName) {
                            "Ubicación" -> {
                                val intent = Intent(this, UbicacionComposeActivity::class.java)
                                intent.putExtra("id_hijo", idHijo)
                                intent.putExtra("nombre_hijo", nombreHijo)
                                startActivity(intent)
                            }
                            "Control de Apps" -> {
                                val intent = Intent(this, BloqueoRemotoHijoActivity::class.java)
                                intent.putExtra("id_hijo", idHijo)
                                intent.putExtra("nombre_hijo", nombreHijo)
                                startActivity(intent)
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleHijo(
    nombreHijo: String,
    onBackClick: () -> Unit,
    onFeatureClick: (String) -> Unit
) {
    // Gradiente idéntico al de Selección de Modos
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0D47A1),   // Azul profundo
            Color(0xFF00838F),   // Teal
            Color(0xFF00BFA5)    // Verde menta
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 2500f)
    )

    // Layout
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Panel de Control",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
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
                Spacer(modifier = Modifier.height(20.dp))

                // Profile Header Card (Glassmorphism style)
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Flat glass
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        val inicial = nombreHijo.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00BCD4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = inicial,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column {
                            Text(
                                text = nombreHijo,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Supervisión Activa",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Herramientas",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Grid of Features
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FeatureCard(
                            title = "Tareas",
                            icon = Icons.Default.TaskAlt,
                            color = Color(0xFF4CAF50), // Verde
                            onClick = { onFeatureClick("Tareas") }
                        )
                    }
                    item {
                        FeatureCard(
                            title = "Eventos",
                            icon = Icons.Default.CalendarMonth,
                            color = Color(0xFF2196F3), // Azul
                            onClick = { onFeatureClick("Eventos") }
                        )
                    }
                    item {
                        FeatureCard(
                            title = "Control Apps",
                            icon = Icons.Default.AppBlocking,
                            color = Color(0xFFE91E63), // Rosa/Rojo
                            onClick = { onFeatureClick("Control de Apps") }
                        )
                    }
                    item {
                        FeatureCard(
                            title = "Ubicación",
                            icon = Icons.Default.LocationOn,
                            color = Color(0xFFFF9800), // Naranja
                            onClick = { onFeatureClick("Ubicación") }
                        )
                    }
                    item {
                        FeatureCard(
                            title = "Estadística",
                            icon = Icons.Default.BarChart,
                            color = Color(0xFF9C27B0), // Morado
                            onClick = { onFeatureClick("Estadísticas") }
                        )
                    }
                    item {
                        FeatureCard(
                            title = "Historial Web",
                            icon = Icons.Default.History,
                            color = Color(0xFF607D8B), // Azul Gris
                            onClick = { onFeatureClick("Historial de Navegación") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Cuadrado perfecto
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Negro,
                textAlign = TextAlign.Center
            )
        }
    }
}
