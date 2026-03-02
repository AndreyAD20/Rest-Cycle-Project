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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.rest.R
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
    val sharedPrefs = remember { context.getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE) }
    
    var monitoreoActivo by remember { mutableStateOf(sharedPrefs.getBoolean("MONITOREO_ACTIVO", true)) }
    var burbujaActiva by remember { mutableStateOf(sharedPrefs.getBoolean("BURBUJA_ACTIVA", false)) }
    
    var idiomaExpandido by remember { mutableStateOf(false) }
    var idiomaSeleccionado by remember { mutableStateOf(sharedPrefs.getString("IDIOMA", "Español") ?: "Español") }
    
    var tamañoFuenteExpandido by remember { mutableStateOf(false) }
    var tamañoFuenteKey by remember { 
        mutableStateOf(sharedPrefs.getString("TAMANO_FUENTE_KEY", null) ?: run {
             // Migrar de formato antiguo si existe
             val oldStr = sharedPrefs.getString("TAMANO_FUENTE", "Mediano")
             val key = when(oldStr) {
                 "Pequeño", "Small", "Pequeno" -> "small"
                 "Grande", "Large" -> "large"
                 else -> "medium"
             }
             sharedPrefs.edit().putString("TAMANO_FUENTE_KEY", key).apply()
             key
        })
    }
    
    val tamañoFuenteDisplay = when (tamañoFuenteKey) {
        "small" -> stringResource(R.string.font_small)
        "large" -> stringResource(R.string.font_large)
        else -> stringResource(R.string.font_medium)
    }
    

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.White
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
                .background(MaterialTheme.colorScheme.primary)
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
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = stringResource(R.string.settings_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Monitoreo de APPS (con toggle)
                item {
                    OpcionConfiguracionToggle(
                        icono = Icons.Default.Visibility,
                        titulo = stringResource(R.string.settings_monitoring),
                        activado = monitoreoActivo,
                        onToggle = { isEnabled ->
                            monitoreoActivo = isEnabled
                            // Guardar preferencia
                            sharedPrefs.edit().putBoolean("MONITOREO_ACTIVO", isEnabled).apply()
                            
                            // Activar/Desactivar Servicio
                            if (isEnabled) {
                                com.example.rest.services.AppMonitorService.startService(context)
                                android.widget.Toast.makeText(context, context.getString(R.string.toast_monitoring_enabled), android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                com.example.rest.services.AppMonitorService.stopService(context)
                                android.widget.Toast.makeText(context, context.getString(R.string.toast_monitoring_paused), android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }



                // Burbuja
                item {
                    OpcionConfiguracionToggle(
                        icono = Icons.Default.MoreHoriz,
                        titulo = stringResource(R.string.settings_bubble),
                        activado = burbujaActiva,
                        onToggle = { isEnabled ->
                            burbujaActiva = isEnabled
                            sharedPrefs.edit().putBoolean("BURBUJA_ACTIVA", isEnabled).apply()
                            android.widget.Toast.makeText(context, context.getString(R.string.toast_setting_saved), android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Cambiar Idioma
                item {
                    OpcionConfiguracionDropdown(
                        icono = Icons.Default.Language,
                        titulo = stringResource(R.string.settings_language),
                        valorSeleccionado = idiomaSeleccionado,
                        expandido = idiomaExpandido,
                        onExpandirCambio = { idiomaExpandido = it },
                        opciones = listOf(
                            stringResource(R.string.lang_spanish),
                            stringResource(R.string.lang_english),
                            stringResource(R.string.lang_portuguese)
                        ),
                        onSeleccion = { nuevoIdioma ->
                            idiomaSeleccionado = nuevoIdioma
                            sharedPrefs.edit().putString("IDIOMA", nuevoIdioma).apply()
                            
                            val code = when (nuevoIdioma) {
                                "English" -> "en"
                                "Português" -> "pt"
                                else -> "es"
                            }
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
                            
                            android.widget.Toast.makeText(context, context.getString(R.string.toast_language_saved, nuevoIdioma), android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Tamaño de la Fuente (con dropdown)
                item {
                    val opSmall = stringResource(R.string.font_small)
                    val opMed = stringResource(R.string.font_medium)
                    val opLarge = stringResource(R.string.font_large)

                    OpcionConfiguracionDropdown(
                        icono = Icons.Default.TextFields,
                        titulo = stringResource(R.string.settings_font_size),
                        valorSeleccionado = tamañoFuenteDisplay,
                        expandido = tamañoFuenteExpandido,
                        onExpandirCambio = { tamañoFuenteExpandido = it },
                        opciones = listOf(opSmall, opMed, opLarge),
                        onSeleccion = { nuevaFuenteDisplay -> 
                            val newKey = when(nuevaFuenteDisplay) {
                                opSmall -> "small"
                                opLarge -> "large"
                                else -> "medium"
                            }
                            if (newKey != tamañoFuenteKey) {
                                tamañoFuenteKey = newKey 
                                sharedPrefs.edit().putString("TAMANO_FUENTE_KEY", newKey).apply()
                                android.widget.Toast.makeText(context, context.getString(R.string.toast_font_saved, nuevaFuenteDisplay), android.widget.Toast.LENGTH_SHORT).show()
                                
                                // Forzar reinicio de la actividad para aplicar la nueva escala de fuente en CompositionLocalProvider
                                (context as? android.app.Activity)?.recreate()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Perfil
                item {
                    OpcionConfiguracion(
                        icono = Icons.Default.People,
                        titulo = stringResource(R.string.settings_profile),
                        onClick = {
                            context.startActivity(android.content.Intent(context, com.example.rest.features.home.PerfilComposeActivity::class.java))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Seguridad
                item {
                    OpcionConfiguracion(
                        icono = Icons.Default.Security,
                        titulo = stringResource(R.string.settings_security),
                        onClick = {
                            android.widget.Toast.makeText(context, context.getString(R.string.toast_security_warning), android.widget.Toast.LENGTH_LONG).show()
                        }
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
            .height(64.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
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
            .height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            Switch(
                checked = activado,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.scale(0.9f)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = valorSeleccionado,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
                
                Icon(
                    imageVector = if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (opcion == valorSeleccionado) FontWeight.Bold else FontWeight.Normal
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
