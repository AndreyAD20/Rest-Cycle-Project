package com.example.rest.features.habits

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rest.BaseComposeActivity
import com.example.rest.ui.theme.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class AppUsageInfo(
    val appName: String,
    val packageName: String,
    val totalTimeInMillis: Long
)

class EstadisticasComposeActivity : BaseComposeActivity() {
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
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(checkUsageStatsPermission(context)) }
    var periodoSeleccionado by remember { mutableStateOf(0) }
    val periodos = listOf("Diario", "Semanal", "Mensual")
    
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF80DEEA), Primario),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    LaunchedEffect(Unit) {
        hasPermission = checkUsageStatsPermission(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Estadísticas de Uso", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Blanco
                    ) 
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
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brochaGradiente)
                .padding(paddingValues)
        ) {
            if (!hasPermission) {
                PermissionRequestScreen(
                    onRequestPermission = {
                        requestUsageStatsPermission(context)
                    },
                    onCheckPermission = {
                        hasPermission = checkUsageStatsPermission(context)
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        TabRow(
                            selectedTabIndex = periodoSeleccionado,
                            containerColor = Blanco.copy(alpha = 0.3f),
                            contentColor = Blanco,
                            divider = {}
                        ) {
                            periodos.forEachIndexed { index, titulo ->
                                Tab(
                                    selected = periodoSeleccionado == index,
                                    onClick = { periodoSeleccionado = index },
                                    text = { 
                                        Text(
                                            titulo, 
                                            fontWeight = if (periodoSeleccionado == index) FontWeight.Bold else FontWeight.Normal,
                                            color = if (periodoSeleccionado == index) Primario else Blanco
                                        ) 
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        GraficoUso(periodoSeleccionado, context)
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        ListaUsoApps(periodoSeleccionado, context)
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE53935)),
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
                                    "El uso excesivo de aplicaciones puede ser perjudicial para tu salud.",
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
}

@Composable
fun PermissionRequestScreen(
    onRequestPermission: () -> Unit,
    onCheckPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Blanco
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Permiso Requerido",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Blanco
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Para mostrar estadísticas de uso de aplicaciones, necesitamos acceso a los datos de uso del sistema.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Blanco.copy(alpha = 0.9f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blanco)
        ) {
            Text(
                "Otorgar Permiso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primario
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onCheckPermission) {
            Text(
                "Ya otorgué el permiso",
                color = Blanco
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.9f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Instrucciones:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "1. Toca 'Otorgar Permiso'\n" +
                    "2. Busca esta aplicación en la lista\n" +
                    "3. Activa el interruptor de permiso\n" +
                    "4. Regresa a la app y toca 'Ya otorgué el permiso'",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

fun checkUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

fun requestUsageStatsPermission(context: Context) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    context.startActivity(intent)
}

fun getUsageStats(context: Context, period: Int): List<AppUsageInfo> {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val packageManager = context.packageManager
    
    val calendar = Calendar.getInstance()
    val endTime = calendar.timeInMillis
    
    calendar.apply {
        when (period) {
            0 -> { /* Diario */ }
            1 -> add(Calendar.DAY_OF_YEAR, -7)
            else -> add(Calendar.DAY_OF_YEAR, -30)
        }
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startTime = calendar.timeInMillis
    
    val usageStatsList = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_BEST,
        startTime,
        endTime
    )
    
    val usageMap = mutableMapOf<String, Long>()
    
    usageStatsList?.forEach { usageStats ->
        val packageName = usageStats.packageName
        val totalTime = usageStats.totalTimeInForeground
        val lastTimeUsed = usageStats.lastTimeUsed

        if (totalTime > 0 && lastTimeUsed >= startTime) {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                usageMap[packageName] = (usageMap[packageName] ?: 0) + totalTime
            }
        }
    }
    
    val appUsageList = usageMap.mapNotNull { (packageName, totalTime) ->
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            AppUsageInfo(appName, packageName, totalTime)
        } catch (e: Exception) {
            AppUsageInfo(packageName, packageName, totalTime)
        }
    }
    
    return appUsageList.sortedByDescending { it.totalTimeInMillis }
}

fun formatUsageTime(timeInMillis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
    
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "< 1m"
    }
}

@Composable
fun GraficoUso(periodo: Int, context: Context) {
    val usageStats = remember(periodo) { getUsageStats(context, periodo) }
    val topApps = usageStats.take(5)
    
    val maxTime = topApps.maxOfOrNull { it.totalTimeInMillis } ?: 1L
    
    val colores = listOf(
        Color(0xFF6750A4), 
        Color(0xFF4CAF50), 
        Color(0xFFFFEB3B), 
        Color(0xFFFF9800), 
        Color(0xFFF44336)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.9f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        if (topApps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay datos de uso disponibles",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Negro.copy(alpha = 0.5f)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    topApps.forEachIndexed { index, appInfo ->
                        val normalizedValue = (appInfo.totalTimeInMillis.toFloat() / maxTime.toFloat())
                        val alturaAnimada by animateFloatAsState(targetValue = normalizedValue, label = "")
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(30.dp)
                                    .fillMaxHeight(alturaAnimada.coerceAtLeast(0.05f))
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
                    topApps.forEach { appInfo ->
                        Text(
                            appInfo.appName.take(3).uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListaUsoApps(periodo: Int, context: Context) {
    val usageStats = remember(periodo) { getUsageStats(context, periodo) }
    val topApps = usageStats.take(10)
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.9f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (topApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay datos de uso disponibles",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Negro.copy(alpha = 0.5f)
                )
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                topApps.forEachIndexed { index, appInfo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "${index + 1}.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                appInfo.appName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Text(
                            formatUsageTime(appInfo.totalTimeInMillis),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (index < topApps.lastIndex) {
                        HorizontalDivider(color = Color.LightGray)
                    }
                }
            }
        }
    }
}
