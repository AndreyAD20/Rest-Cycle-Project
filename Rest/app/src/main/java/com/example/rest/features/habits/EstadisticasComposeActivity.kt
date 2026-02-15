package com.example.rest.features.habits

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import android.graphics.drawable.Drawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.shape.CircleShape
import java.text.SimpleDateFormat
import java.util.Locale
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import android.os.Build
import android.provider.MediaStore
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.rest.data.models.AppUsageInfo
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Constantes para notificaciones
const val CHANNEL_ID = "REPORTE_CHANNEL"
const val CHANNEL_NAME = "Informes"
const val NOTIFICATION_ID = 1001

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            description = "Notificaciones de descarga de reportes"
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun showDownloadNotification(context: Context, uri: android.net.Uri) {
    createNotificationChannel(context)
    
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    }
    
    val pendingIntent: PendingIntent = PendingIntent.getActivity(
        context, 
        0, 
        intent, 
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_sys_download_done) // Icono genérico por ahora
        .setContentTitle("Informe Descargado")
        .setContentText("Toca para abrir el reporte PDF")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    try {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < 33
        ) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        } else {
             Toast.makeText(context, "Informe guardado (Permiso de notificación no otorgado)", Toast.LENGTH_SHORT).show()
        }
    } catch (e: SecurityException) {
        Log.e("Notification", "Error mostrando notificación: ${e.message}")
    }
}



class EstadisticasComposeActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode = com.example.rest.utils.ThemeManager.isDarkMode(this)
            TemaRest(temaOscuro = isDarkMode) {
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
    var semanaOffset by remember { mutableStateOf(0) } // 0 = Actual, -1 = Anterior
    var showDownloadDialog by remember { mutableStateOf(false) }
    val periodos = listOf("Diario", "Semanal", "Mensual")
    
    // Launcher para notificación (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Sin permiso, no recibirás notificaciones de descarga.", Toast.LENGTH_SHORT).show()
        }
    }
    
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF80DEEA), Primario),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    // Repositorio para sincronizar
    val estadisticasRepo = remember { com.example.rest.data.repository.EstadisticasRepository() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        hasPermission = checkUsageStatsPermission(context)
        // Solicitar POST_NOTIFICATIONS en Android 13+
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // SINCRONIZACIÓN AUTOMÁTICA CON SUPABASE (Solo hoy)
        if (hasPermission) {
            scope.launch {
                try {
                    val statsHoy = getUsageStats(context, 0) // 0 = Hoy
                    val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
                    estadisticasRepo.sincronizarEstadisticas(context, statsHoy, deviceName)
                } catch (e: Exception) {
                    Log.e("Sync", "Error sync auto: ${e.message}")
                }
            }
        }
    }

    // Obtener nombre de usuario
    val nombreUsuario = remember {
        val sharedPref = context.getSharedPreferences("RestCyclePrefs", Context.MODE_PRIVATE)
        sharedPref.getString("NOMBRE_USUARIO", "Usuario") ?: "Usuario"
    }

    if (showDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            title = { Text("Descargar Informe", fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas descargar el reporte de uso en PDF?") },
            confirmButton = {
                TextButton(onClick = {
                    showDownloadDialog = false
                    val stats = getUsageStats(context, periodoSeleccionado, semanaOffset)
                    generarReportePDF(context, stats, getPeriodoFecha(periodoSeleccionado, semanaOffset), nombreUsuario)
                }) {
                    Text("Descargar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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
                // Cargar datos en hilo secundario para evitar ANR
                val datosDiarios = produceState<Map<String, Long>>(initialValue = emptyMap(), key1 = semanaOffset) {
                    if (periodoSeleccionado == 1) { // Solo si es semanal
                        value = withContext(Dispatchers.IO) { getUsageStatsDiario(context, semanaOffset) }
                    }
                }

                // Cargar lista general (Diario/Mensual/Semanal)
                val usageStats = produceState<List<AppUsageInfo>>(initialValue = emptyList(), key1 = periodoSeleccionado, key2 = semanaOffset) {
                     value = withContext(Dispatchers.IO) { getUsageStats(context, periodoSeleccionado, semanaOffset) }
                }

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
                            divider = {}
                        ) {
                            periodos.forEachIndexed { index, titulo ->
                                Tab(
                                    selected = periodoSeleccionado == index,
                                    onClick = { 
                                        periodoSeleccionado = index 
                                        semanaOffset = 0 // Resetear offset al cambiar tabs
                                    },
                                    text = {
                                        Text(
                                            titulo,
                                            fontWeight = if (periodoSeleccionado == index) FontWeight.Bold else FontWeight.Normal,
                                            color = if (periodoSeleccionado == index) Primario else Color.DarkGray
                                        )
                                    }
                                )
                            }
                        }
                        
                        // FECHA CON NAVEGACIÓN (Solo si es semanal)
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (periodoSeleccionado == 1) { // Semanal
                                IconButton(
                                    onClick = { 
                                        if (semanaOffset > -1) semanaOffset-- 
                                    },
                                    enabled = semanaOffset > -1
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowLeft, 
                                        "Anterior",
                                        tint = if (semanaOffset > -1) Negro else Color.Gray.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            
                            Text(
                                text = getPeriodoFecha(periodoSeleccionado, semanaOffset),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Negro,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            
                            if (periodoSeleccionado == 1) { // Semanal
                                IconButton(
                                    onClick = { 
                                        if (semanaOffset < 0) semanaOffset++ 
                                    },
                                    enabled = semanaOffset < 0
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowRight, 
                                        "Siguiente",
                                        tint = if (semanaOffset < 0) Negro else Color.Gray.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                        
                        // BOTÓN DE DESCARGA EXTENDIDO
                        Spacer(modifier = Modifier.height(16.dp))
                        ExtendedFloatingActionButton(
                            onClick = { showDownloadDialog = true },
                            icon = { Icon(Icons.Default.Download, "Descargar") },
                            text = { Text("DESCARGAR REPORTE", fontWeight = FontWeight.Bold) },
                            containerColor = Primario,
                            contentColor = Blanco,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        // Mostrar gráfico diferente según el periodo
                        if (periodoSeleccionado == 1) {
                            // Vista semanal: Mostrar gráfico de barras diario
                            // Calcular fechas para el gráfico
                             val fechaTexto = getPeriodoFecha(1, semanaOffset)
                             val splitted = fechaTexto.split(" - ")
                             val inicio = splitted.getOrElse(0) { "" }
                             val fin = splitted.getOrElse(1) { "" }
                            
                            GraficoBarrasSemanal(datosDiarios.value, inicio, fin)
                        } else {
                            // Vista diaria y mensual: Gráfico top apps
                            GraficoUso(usageStats.value)
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        // Reutilizamos la lista ya cargada arriba
                        ListaUsoApps(usageStats.value)
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

fun getUsageStats(context: Context, period: Int, offset: Int = 0): List<AppUsageInfo> {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val packageManager = context.packageManager
    
    val calendar = Calendar.getInstance()
    var endTime = calendar.timeInMillis // Default to now
    
    val startCalendar = Calendar.getInstance()
    startCalendar.set(Calendar.HOUR_OF_DAY, 0)
    startCalendar.set(Calendar.MINUTE, 0)
    startCalendar.set(Calendar.SECOND, 0)
    startCalendar.set(Calendar.MILLISECOND, 0)
    
    when (period) {
        0 -> { /* Diario - desde las 00:00 de HOY */ 
            startCalendar.add(Calendar.DAY_OF_YEAR, offset)
            endTime = startCalendar.timeInMillis + 86400000L - 1 // Final del día (aprox)
            
            // Re-ajustar startCalendar para el inicio del día específico
            val specificStart = Calendar.getInstance()
            specificStart.timeInMillis = startCalendar.timeInMillis
            // Ya está en 00:00:00 del día con offset
            
            // Ajustar endTime al final de ese día
            val specificEnd = Calendar.getInstance()
            specificEnd.timeInMillis = startCalendar.timeInMillis
            specificEnd.set(Calendar.HOUR_OF_DAY, 23)
            specificEnd.set(Calendar.MINUTE, 59)
            specificEnd.set(Calendar.SECOND, 59)
            endTime = specificEnd.timeInMillis
        }
        1 -> { // Semanal
            startCalendar.firstDayOfWeek = Calendar.MONDAY
            startCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            startCalendar.add(Calendar.WEEK_OF_YEAR, offset)
            
            val endCalendar = Calendar.getInstance()
            endCalendar.timeInMillis = startCalendar.timeInMillis
            endCalendar.add(Calendar.DAY_OF_YEAR, 6)
            endCalendar.set(Calendar.HOUR_OF_DAY, 23)
            endCalendar.set(Calendar.MINUTE, 59)
            endCalendar.set(Calendar.SECOND, 59)
            
            endTime = endCalendar.timeInMillis
        }
        else -> { // Mensual
            startCalendar.set(Calendar.DAY_OF_MONTH, 1)
            startCalendar.add(Calendar.MONTH, offset)
            
            val endCalendar = Calendar.getInstance()
            endCalendar.timeInMillis = startCalendar.timeInMillis
            endCalendar.add(Calendar.MONTH, 1)
            endCalendar.add(Calendar.DAY_OF_YEAR, -1) // Último día del mes
            endCalendar.set(Calendar.HOUR_OF_DAY, 23)
            endCalendar.set(Calendar.MINUTE, 59)
            endCalendar.set(Calendar.SECOND, 59)
            
            endTime = endCalendar.timeInMillis
        }
    }
    
    val startTime = startCalendar.timeInMillis
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    Log.d("EstadisticasDebug", "=== INICIO CONSULTA ===")
    Log.d("EstadisticasDebug", "Periodo: $period")
    Log.d("EstadisticasDebug", "Desde: ${dateFormat.format(startTime)}")
    Log.d("EstadisticasDebug", "Hasta: ${dateFormat.format(endTime)}")
    
    try {
        val usageMap = mutableMapOf<String, Long>()

        Log.d("EstadisticasDebug", "Usando queryAndAggregateUsageStats para periodo: $period")
        // Lógica UNIFICADA para TODOS los periodos (Diario/Semanal/Mensual)
        // Esto simplifica y es más robusto en diferentes dispositivos
        val aggregatedStats = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
        
        if (aggregatedStats != null) {
            aggregatedStats.forEach { (packageName, usageStats) ->
                val totalTime = usageStats.totalTimeInForeground
                if (totalTime > 0) {
                     // Solo sumar si este periodo tiene tiempo relevante
                     // (Para diario, startTime filtra correctamente)
                     usageMap[packageName] = totalTime
                }
            }
        }
        
        Log.d("EstadisticasDebug", "Total apps con uso: ${usageMap.size}")
    
        val appUsageList = usageMap.mapNotNull { (packageName, totalTime) ->
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                
                val isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                val isUpdatedSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                
                val isWhitelisted = packageName.contains("com.google.android.youtube") || 
                                   packageName.contains("com.google.android.apps.maps") || 
                                   packageName.contains("com.google.android.gm") || 
                                   packageName.contains("com.android.chrome") || 
                                   packageName.contains("com.google.android.apps.photos") || 
                                   packageName.contains("com.whatsapp") || 
                                   packageName.contains("com.facebook") ||
                                   packageName.contains("com.instagram") ||
                                   packageName.contains("com.twitter")
                                   
                val isBlacklisted = packageName.contains("launcher") || 
                                   packageName.contains("systemui") || 
                                   packageName.contains("settings") || 
                                   packageName.contains("wallpaper") ||
                                   packageName.contains("inputmethod") || 
                                   packageName.contains("provider") ||
                                   packageName.contains("service") ||
                                   packageName == "android" ||
                                   packageName.startsWith("com.android.internal") ||
                                   packageName.contains("com.motorola.ccc") || 
                                   packageName.contains("com.motorola.android")
                
                
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                
                // FILTRO DE BLACKLIST (Aplicaciones ocultas por solicitud del usuario)
                val blacklist = listOf(
                    "clima local", 
                    "moto secure", 
                    "family space", 
                    "widgets moto", 
                    "moto widget",
                    "cámara", // A veces son procesos de sistema
                    "teclado"
                )
                
                // Normalizar nombre para comparación
                val normalizedName = appName.lowercase()
                val isBlocked = blacklist.any { normalizedName.contains(it) }

                // FILTRO ESTRICTO: Si es de sistema (original o actualizada), SOLO permitir whitelist
                if ((isSystemApp || isUpdatedSystemApp) && !isWhitelisted) {
                    return@mapNotNull null
                }

                // Además, aplicar blacklist por si acaso alguna se escapa o es de usuario pero no deseada
                if (isBlacklisted || isBlocked) return@mapNotNull null
                
                val appIcon = packageManager.getApplicationIcon(packageName)
                
                AppUsageInfo(appName, packageName, totalTime, appIcon)
            } catch (e: Exception) {
                Log.w("EstadisticasDebug", "No se pudo obtener info de $packageName: ${e.message}")
                null
            }
        }
        
        Log.d("EstadisticasDebug", "Apps procesadas: ${appUsageList.size}")
        Log.d("EstadisticasDebug", "=== FIN CONSULTA ===")
        
        return appUsageList.sortedByDescending { it.totalTimeInMillis }

    } catch (e: Exception) {
        Log.e("EstadisticasDebug", "Error fatal en getUsageStats: ${e.message}")
        e.printStackTrace()
        return emptyList()
    }
}

fun formatUsageTime(timeInMillis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
    
    return when {
        hours > 0 && minutes > 0 -> "${hours} hrs ${minutes} min"
        hours > 0 -> "${hours} hrs"
        minutes > 0 -> "${minutes} min"
        else -> "< 1 min"
    }
}

// Helper para obtener el texto de la fecha según el periodo (CON AÑO)
fun getPeriodoFecha(periodo: Int, offset: Int = 0): String {
    val calendar = Calendar.getInstance()
    // Locale español para nombres de meses
    val formatoDia = SimpleDateFormat("dd MMM", Locale("es", "ES"))
    val formatoDiaFull = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))

    return when (periodo) {
        0 -> {
            calendar.add(Calendar.DAY_OF_YEAR, offset)
            if (offset == 0) "Hoy, ${formatoDiaFull.format(calendar.time)}"
            else formatoDiaFull.format(calendar.time)
        }
        1 -> {
            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            calendar.add(Calendar.WEEK_OF_YEAR, offset)
            
            val inicio = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, 6)
            val fin = calendar.time
            "${formatoDia.format(inicio)} - ${formatoDia.format(fin)}"
        }
        else -> {
            calendar.set(Calendar.DAY_OF_MONTH, 1) // Inicio de mes
            calendar.add(Calendar.MONTH, offset)
            val inicio = calendar.time
            
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val fin = calendar.time
            "${formatoDia.format(inicio)} - ${formatoDia.format(fin)}"
        }
    }
}

/**
 * Obtiene el uso diario para la gráfica semanal
 * Retorna un mapa con los días de la semana y el tiempo en milisegundos
 */
fun getUsageStatsDiario(context: Context, offset: Int = 0): Map<String, Long> {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val calendar = Calendar.getInstance()
    
    // Configurar al Lunes de la semana seleccionada
    calendar.firstDayOfWeek = Calendar.MONDAY
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    calendar.add(Calendar.WEEK_OF_YEAR, offset)
    
    val diasSemana = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    val resultado = mutableMapOf<String, Long>()
    
    // Iterar por los 7 días
    val packageManager = context.packageManager
    for (i in 0 until 7) {
        val startDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endDay = calendar.timeInMillis - 1
        
        // Usar queryAndAggregateUsageStats para mayor precisión
        val statsMap = usageStatsManager.queryAndAggregateUsageStats(startDay, endDay)
        
        var totalDia = 0L
        if (statsMap != null) {
             statsMap.forEach { (packageName, stats) ->
                 try {
                     // Obtener información de la app
                     val appInfo = packageManager.getApplicationInfo(packageName, 0)
                     
                     // Definiciones idénticas a getUsageStats para consistencia
                     val isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                     val isUpdatedSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                     
                     val isWhitelisted = packageName.contains("com.google.android.youtube") || 
                                       packageName.contains("com.google.android.apps.maps") || 
                                       packageName.contains("com.google.android.gm") || 
                                       packageName.contains("com.android.chrome") || 
                                       packageName.contains("com.google.android.apps.photos") || 
                                       packageName.contains("com.whatsapp") || 
                                       packageName.contains("com.facebook") ||
                                       packageName.contains("com.instagram") ||
                                       packageName.contains("com.twitter")
                                       
                     val isBlacklisted = packageName.contains("launcher") || 
                                       packageName.contains("systemui") || 
                                       packageName.contains("settings") || 
                                       packageName.contains("wallpaper") ||
                                       packageName.contains("inputmethod") || 
                                       packageName.contains("provider") ||
                                       packageName.contains("service") ||
                                       packageName == "android" ||
                                       packageName.startsWith("com.android.internal") ||
                                       packageName.contains("com.motorola.ccc") || 
                                       packageName.contains("com.motorola.android")
                     
                     val appName = packageManager.getApplicationLabel(appInfo).toString()
                     val blacklist = listOf(
                         "clima local", "moto secure", "family space", "widgets moto", 
                         "moto widget", "cámara", "teclado"
                     )
                     val normalizedName = appName.lowercase()
                     val isBlocked = blacklist.any { normalizedName.contains(it) }

                     // Lógica de filtrado
                     var shouldInclude = true
                     
                     // 1. Si es sistema y NO está en whitelist -> Excluir
                     if ((isSystemApp || isUpdatedSystemApp) && !isWhitelisted) {
                         shouldInclude = false
                     }
                     
                     // 2. Si está en blacklist -> Excluir
                     if (isBlacklisted || isBlocked) {
                         shouldInclude = false
                     }
                     
                     // NOTA: NO excluimos context.packageName para ser consistentes con la lista
                     
                     if (shouldInclude) {
                         totalDia += stats.totalTimeInForeground
                     }
                     
                 } catch (e: Exception) {
                     // Si falla obtener info (ej. app desinstalada), ignorar para no inflar stats con basura
                 }
             }
        }
        
        resultado[diasSemana[i]] = totalDia
    }
    
    return resultado
}

fun generarReportePDF(context: Context, stats: List<AppUsageInfo>, periodoStr: String, nombreUsuario: String) {
    val pdfDocument = android.graphics.pdf.PdfDocument()
    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = android.graphics.Paint()
    val titlePaint = android.graphics.Paint()
    val watermarkPaint = android.graphics.Paint()

    // 1. MARCA DE AGUA (FONDO)
    watermarkPaint.color = android.graphics.Color.BLUE
    watermarkPaint.alpha = 40 // Muy transparente (0-255)
    watermarkPaint.textSize = 80f
    watermarkPaint.textAlign = android.graphics.Paint.Align.CENTER
    watermarkPaint.isFakeBoldText = true
    
    canvas.save()
    canvas.translate(pageInfo.pageWidth / 2f, pageInfo.pageHeight / 2f)
    canvas.rotate(-45f)
    canvas.drawText("REST CYCLE", 0f, 0f, watermarkPaint)
    canvas.restore()

    // Configuración de pinceles de texto
    titlePaint.textSize = 24f
    titlePaint.isFakeBoldText = true
    titlePaint.color = android.graphics.Color.BLACK
    
    paint.textSize = 14f
    paint.color = android.graphics.Color.BLACK

    var y = 50f
    val startX = 50f
    
    // 2. LOGO DE LA APP (Izquierda)
    try {
        val appIcon = context.packageManager.getApplicationIcon(context.packageName)
        val bitmap = if (appIcon is BitmapDrawable) {
            appIcon.bitmap
        } else {
            val bmp = Bitmap.createBitmap(appIcon.intrinsicWidth, appIcon.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val iconCanvas = Canvas(bmp)
            appIcon.setBounds(0, 0, iconCanvas.width, iconCanvas.height)
            appIcon.draw(iconCanvas)
            bmp
        }
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 60, 60, false)
        canvas.drawBitmap(scaledBitmap, startX, y, null)
    } catch (e: Exception) {
        Log.e("PDF", "Error dibujando icono: ${e.message}")
    }

    // 3. ENCABEZADO (Derecha del logo)
    val textStartX = 130f
    canvas.drawText("Reporte de Uso - Rest Cycle", textStartX, y + 25f, titlePaint)
    y += 80f
    
    // Información del Usuario y Fecha
    paint.isFakeBoldText = true
    canvas.drawText("Usuario: $nombreUsuario", startX, y, paint)
    y += 20f
    canvas.drawText("Periodo: $periodoStr", startX, y, paint)
    y += 30f
    paint.isFakeBoldText = false
    
    // Línea separadora azul
    paint.color = android.graphics.Color.BLUE
    paint.strokeWidth = 2f
    canvas.drawLine(startX, y, pageInfo.pageWidth - 50f, y, paint)
    paint.color = android.graphics.Color.BLACK // Reset color
    paint.strokeWidth = 1f
    y += 30f
    
    // 4. LISTA DE APPS
    stats.forEachIndexed { index, app ->
        if (y > pageInfo.pageHeight - 50f) { // Paginación simple (corte)
            return@forEachIndexed
        }
        val nombre = if (app.appName.length > 30) app.appName.take(30) + "..." else app.appName
        val tiempo = formatUsageTime(app.totalTimeInMillis)
        val texto = "${index + 1}. $nombre"
        
        // Dibujar nombre a la izquierda
        canvas.drawText(texto, startX, y, paint)
        
        // Dibujar tiempo a la derecha (alineado)
        val timeWidth = paint.measureText(tiempo)
        canvas.drawText(tiempo, pageInfo.pageWidth - 50f - timeWidth, y, paint)
        
        y += 25f
    }
    
    pdfDocument.finishPage(page)

    // Guardar archivo
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "Reporte_RestCycle_$timeStamp.pdf"
    
    // Guardar en Downloads
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = android.content.ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                Toast.makeText(context, "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
                // Intentar abrir el archivo
                // val openIntent = Intent(Intent.ACTION_VIEW).apply {
                //     setDataAndType(it, "application/pdf")
                //     flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                // }
                // context.startActivity(openIntent) 
                
                // MOSTRAR NOTIFICACIÓN
                showDownloadNotification(context, it)
                
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al guardar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    } else {
        val file = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), fileName)
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "PDF guardado: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            
            // Notificación para legacy (cuidado con FileUriExposedException en >= N)
            // Por seguridad en este entorno rápido, solo mostramos Toast en legacy
            // o podríamos usar FileProvider si estuviera configurado.
        } catch (e: Exception) {
             e.printStackTrace()
             Toast.makeText(context, "Error legacy: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    pdfDocument.close()
}

@Composable
fun GraficoUso(stats: List<AppUsageInfo>) {
    val topApps = stats.take(5)
    
    val maxTime = topApps.maxOfOrNull { it.totalTimeInMillis } ?: 1L
    
    // Paleta AZUL (Oscuro a Claro)
    val colores = listOf(
        Color(0xFF0D47A1), // Azul muy oscuro 
        Color(0xFF1565C0), 
        Color(0xFF1976D2), 
        Color(0xFF2196F3), 
        Color(0xFF64B5F6)  // Azul claro
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
fun ListaUsoApps(stats: List<AppUsageInfo>) {
    val topApps = stats.take(10)
    
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
                            
                            // App Icon
                            appInfo.icon?.let { drawable ->
                                val bitmap = if (drawable is BitmapDrawable) {
                                    drawable.bitmap
                                } else {
                                    val bmp = Bitmap.createBitmap(
                                        drawable.intrinsicWidth.coerceAtLeast(1),
                                        drawable.intrinsicHeight.coerceAtLeast(1),
                                        Bitmap.Config.ARGB_8888
                                    )
                                    val canvas = Canvas(bmp)
                                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                                    drawable.draw(canvas)
                                    bmp
                                }
                                
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = appInfo.appName,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            
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
