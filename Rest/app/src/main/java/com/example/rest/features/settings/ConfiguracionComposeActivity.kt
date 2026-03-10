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
    
    // Estado para el diálogo de Burbuja
    var mostrarDialogoBurbuja by remember { mutableStateOf(false) }
    
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
                
                // Monitoreo de APPS (con toggle)
                item {
                    val sharedPrefs = remember { context.getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE) }
                    var monitoreoActivo by remember { 
                        mutableStateOf(sharedPrefs.getBoolean("MONITOREO_ACTIVO", true)) 
                    }
                    
                    OpcionConfiguracionToggle(
                        icono = Icons.Default.Visibility,
                        titulo = "Monitoreo de Uso",
                        activado = monitoreoActivo,
                        onToggle = { isEnabled ->
                            monitoreoActivo = isEnabled
                            // Guardar preferencia
                            sharedPrefs.edit().putBoolean("MONITOREO_ACTIVO", isEnabled).apply()
                            
                            // Activar/Desactivar Servicio
                            if (isEnabled) {
                                com.example.rest.services.AppMonitorService.startService(context)
                                android.widget.Toast.makeText(context, "Monitoreo activado", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                com.example.rest.services.AppMonitorService.stopService(context)
                                android.widget.Toast.makeText(context, "Monitoreo pausado", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
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
                        onClick = { mostrarDialogoBurbuja = true }
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
    
    if (mostrarDialogoBurbuja) {
        DialogoConfiguracionBurbuja(
            onDismiss = { mostrarDialogoBurbuja = false }
        )
    }
}

@Composable
fun DialogoConfiguracionBurbuja(onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE) }
    
    // Estados del sonido
    var sonidoExpandido by remember { mutableStateOf(false) }
    var sonidoSeleccionado by remember { mutableStateOf(sharedPrefs.getString("BURBUJA_SONIDO", "Predeterminado") ?: "Predeterminado") }
    val opcionesSonido = listOf("Predeterminado", "Burbuja (Pop)", "Campana Suave", "Silencioso")
    
    // Estados de temas de interés
    val temasDisponibles = listOf("Educación", "Salud y Bienestar", "Tecnología", "Deportes", "Productividad")
    val temasGuardados = sharedPrefs.getStringSet("BURBUJA_TEMAS", setOf("Salud y Bienestar", "Productividad")) ?: setOf()
    
    var temasSeleccionados by remember { mutableStateOf(temasGuardados.toMutableSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Configuración de Burbuja",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // 1. Selector de Sonido
                Text(
                    text = "Sonido de la Burbuja",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                ExposedDropdownMenuBoxContainer(
                    valorSeleccionado = sonidoSeleccionado,
                    opciones = opcionesSonido,
                    onSeleccion = { sonidoSeleccionado = it }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 2. Multiselección de Temas de Interés
                Text(
                    text = "Temas de Interés",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Selecciona qué tipo de contenido te gustaría ver en los reportes rápidos de las burbujas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                temasDisponibles.forEach { tema ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val nuevoSet = temasSeleccionados.toMutableSet()
                                if (nuevoSet.contains(tema)) nuevoSet.remove(tema) else nuevoSet.add(tema)
                                temasSeleccionados = nuevoSet
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = temasSeleccionados.contains(tema),
                            onCheckedChange = { isChecked ->
                                val nuevoSet = temasSeleccionados.toMutableSet()
                                if (isChecked) nuevoSet.add(tema) else nuevoSet.remove(tema)
                                temasSeleccionados = nuevoSet
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = tema, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Guardar preferencias
                    sharedPrefs.edit()
                        .putString("BURBUJA_SONIDO", sonidoSeleccionado)
                        .putStringSet("BURBUJA_TEMAS", temasSeleccionados)
                        .apply()
                    
                    android.widget.Toast.makeText(context, "Configuración de burbuja guardada", android.widget.Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ExposedDropdownMenuBoxContainer(
    valorSeleccionado: String,
    opciones: List<String>,
    onSeleccion: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expandido = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = valorSeleccionado,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
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
