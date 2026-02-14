package com.example.rest.features.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rest.BaseComposeActivity
import com.example.rest.ui.theme.*

class ConfiguracionComposeActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode = com.example.rest.utils.ThemeManager.isDarkMode(this)
            TemaRest(temaOscuro = isDarkMode) {
                PantallaConfiguracion(
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracion(onBackClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Cargar preferencia guardada
    var modoOscuro by remember { 
        mutableStateOf(com.example.rest.utils.ThemeManager.isDarkMode(context)) 
    }
    var tamañoFuenteExpandido by remember { mutableStateOf(false) }
    var tamañoFuenteSeleccionado by remember { mutableStateOf("Mediano") }
    
    val gradienteBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        ),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradienteBrush)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header con icono y título
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "Configura La App y\nTus Preferencias",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 28.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Modo Oscuro (con toggle)
                item {
                    OpcionConfiguracionToggle(
                        icono = Icons.Default.DarkMode,
                        titulo = "Modo Oscuro",
                        activado = modoOscuro,
                        onToggle = { 
                            modoOscuro = it
                            // Guardar preferencia
                            com.example.rest.utils.ThemeManager.setDarkMode(context, it)
                            // Recrear actividad para aplicar cambios
                            (context as? android.app.Activity)?.recreate()
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Notificaciones
                item {
                    OpcionConfiguracion(
                        icono = Icons.Default.Notifications,
                        titulo = "Notificaciones",
                        onClick = { /* TODO */ }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Burbuja
                item {
                    OpcionConfiguracion(
                        icono = Icons.Default.MoreHoriz,
                        titulo = "Burbuja",
                        onClick = { /* TODO */ }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Cambiar Idioma
                item {
                    OpcionConfiguracion(
                        icono = Icons.Default.Language,
                        titulo = "Cambiar Idioma",
                        onClick = { /* TODO */ }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Tamaño de la Fuente (con dropdown)
                item {
                    OpcionConfiguracionDropdown(
                        icono = Icons.Default.TextFields,
                        titulo = "Tamaño de la Fuente",
                        valorSeleccionado = tamañoFuenteSeleccionado,
                        expandido = tamañoFuenteExpandido,
                        onExpandirCambio = { tamañoFuenteExpandido = it },
                        opciones = listOf("Pequeño", "Mediano", "Grande"),
                        onSeleccion = { tamañoFuenteSeleccionado = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Perfil
                item {
                    OpcionConfiguracion(
                        icono = Icons.Default.People,
                        titulo = "Perfil",
                        onClick = { /* TODO */ }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Seguridad
                item {
                    OpcionConfiguracion(
                        icono = Icons.Default.Security,
                        titulo = "Seguridad",
                        onClick = { /* TODO */ }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun OpcionConfiguracion(
    icono: ImageVector,
    titulo: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun OpcionConfiguracionToggle(
    icono: ImageVector,
    titulo: String,
    activado: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            
            Switch(
                checked = activado,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun OpcionConfiguracionDropdown(
    icono: ImageVector,
    titulo: String,
    valorSeleccionado: String,
    expandido: Boolean,
    onExpandirCambio: (Boolean) -> Unit,
    opciones: List<String>,
    onSeleccion: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandirCambio(!expandido) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            DropdownMenu(
                expanded = expandido,
                onDismissRequest = { onExpandirCambio(false) },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = opcion,
                                color = if (opcion == valorSeleccionado) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onSeleccion(opcion)
                            onExpandirCambio(false)
                        }
                    )
                }
            }
        }
    }
}
