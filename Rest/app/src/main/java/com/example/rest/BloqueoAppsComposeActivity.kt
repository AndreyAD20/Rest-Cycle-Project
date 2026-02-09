package com.example.rest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rest.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class BloqueoAppsComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // EDUCATIVO: Verificar permiso de Acceso a Datos de Uso.
        // Necesitamos esto para leer las estadísticas de uso diaras de otras apps.
        // Sin esto, la app no puede saber si excediste tu límite de tiempo.
        if (!hasUsageStatsPermission()) {
            startActivity(android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
            android.widget.Toast.makeText(this, "Por favor habilita el acceso a datos de uso", android.widget.Toast.LENGTH_LONG).show()
        }

        // EDUCATIVO: Verificar permiso de Superposición (Mostrar sobre otras apps).
        // Esto es crucial para la función de "Bloqueo". La seguridad de Android evita que las apps
        // aparezcan encima de otras por defecto. Necesitamos este permiso especial para mostrar
        // la pantalla de "Tiempo Agotado" (BloqueoOverlayActivity) encima de la app bloqueada.
        if (!android.provider.Settings.canDrawOverlays(this)) {
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:$packageName")
            )
            startActivity(intent)
            android.widget.Toast.makeText(this, "Por favor permite mostrar sobre otras apps", android.widget.Toast.LENGTH_LONG).show()
        }


        val serviceIntent = android.content.Intent(this, UsageMonitorService::class.java)
        startService(serviceIntent)

        setContent {
            TemaRest {
                PantallaBloqueoApps(onBackClick = { finish() })
            }
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(android.content.Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBloqueoApps(onBackClick: () -> Unit) {
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF80DEEA), Primario),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    val context = androidx.compose.ui.platform.LocalContext.current
    var blockedApps by remember { mutableStateOf(AppBlockingManager.getBlockedApps(context)) }
    var mostrandoSeleccionApps by remember { mutableStateOf(false) }

    // Función para actualizar la lista
    fun refreshApps() {
        blockedApps = AppBlockingManager.getBlockedApps(context)
    }

    val pm = context.packageManager
    val allInstalledApps = remember { mutableStateListOf<AppBloqueo>() }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val packages = pm.getInstalledPackages(0)
            val appsList = packages.mapNotNull { packageInfo ->
                val appInfo = packageInfo.applicationInfo
                if (appInfo != null && (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                     val label = appInfo.loadLabel(pm).toString()
                     val pkg = packageInfo.packageName
                     // Use Long for bitwise operations then convert to Int
                     val color = (0xFF000000 or (pkg.hashCode().toLong() and 0x00FFFFFF)).toInt()
                     AppBloqueo(label, pkg, color)
                } else null
            }
            withContext(Dispatchers.Main) {
                allInstalledApps.clear()
                allInstalledApps.addAll(appsList)
            }
        }
    }




    Box(modifier = Modifier.fillMaxSize()) {
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
                                imageVector = Icons.Default.Lock,
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
                            onClick = { mostrandoSeleccionApps = true },
                            containerColor = Color(0xFF00BCD4),
                            contentColor = Negro,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Add, "Agregar")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(blockedApps) { app ->
                            AppBloqueoItem(app, onUpdate = { updatedApp ->
                                AppBlockingManager.saveBlockedApp(context, updatedApp)
                                refreshApps()
                            })
                        }
                        item {
                            Spacer(modifier = Modifier.height(64.dp))
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = mostrandoSeleccionApps,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            PantallaSeleccionApps(
                allApps = allInstalledApps,
                onBack = { mostrandoSeleccionApps = false },
                onAppSelected = { app ->
                    val newApp = app.copy() // Create copy
                    AppBlockingManager.saveBlockedApp(context, newApp)
                    refreshApps()
                    mostrandoSeleccionApps = false
                }
            )
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

// Helper to get next limit
fun getNextLimit(current: Int): Int {
    return when (current) {
        0 -> 1
        1 -> 10
        10 -> 30
        30 -> 60
        else -> 0
    }
}

// Helper to get display text
fun getLimitText(current: Int): String {
    return when (current) {
        0 -> "Off"
        1 -> "1m"
        10 -> "10m"
        30 -> "30m"
        60 -> "1h"
        else -> "${current}m"
    }
}

@Composable
fun AppBloqueoItem(app: AppBloqueo, onUpdate: (AppBloqueo) -> Unit) {
    var limit by remember { mutableStateOf(app.limitMinutes) }

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

            Surface(
                color = if(limit > 0) Color(0xFF00BCD4) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable { 
                    limit = getNextLimit(limit)
                    app.limitMinutes = limit
                    onUpdate(app)
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getLimitText(limit),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSeleccionApps(
    allApps: List<AppBloqueo>,
    onBack: () -> Unit,
    onAppSelected: (AppBloqueo) -> Unit
) {
    val fondoColor = Color(0xFFB3E5FC)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(fondoColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp),
                tint = Color.Black
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(50), 
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Aplicaciones Del Dispositivo",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = Color.DarkGray
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) 
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.Transparent, 
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF64B5F6))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFEEEEEE).copy(alpha = 0.9f))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allApps) { app ->
                        ItemSeleccionApp(app = app, onAdd = { onAppSelected(app) })
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ItemSeleccionApp(app: AppBloqueo, onAdd: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF90CAF9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)) 
                        .background(app.iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        app.nombre.take(1),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = app.nombre,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.Black
                )
            }
            
            IconButton(
                onClick = onAdd,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF00BCD4), CircleShape)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
