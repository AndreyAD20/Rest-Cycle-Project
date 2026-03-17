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
import androidx.compose.animation.core.*
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

import androidx.compose.ui.res.stringResource
import com.example.rest.R

// Constantes para notificaciones
const val CHANNEL_ID = "REPORTE_CHANNEL"
const val CHANNEL_NAME = "Informes"
const val NOTIFICATION_ID = 1001

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, context.getString(R.string.notif_report_channel_desc), importance).apply {
            description = context.getString(R.string.notif_report_channel_desc)
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
        .setContentTitle(context.getString(R.string.notif_report_downloaded_title))
        .setContentText(context.getString(R.string.notif_report_downloaded_text))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    val textContent = "Toca para abrir el reporte PDF"
    
    // ── Fase 4: Centralizar en el Historial del Asistente (Burbuja Rest Cycle) ──
    com.example.rest.data.NotificationRepository.addNotification(
        title = "Informe Descargado",
        message = textContent,
        category = "Notificación de Estadísticas",
        sourceClass = "com.example.rest.features.habits.EstadisticasComposeActivity"
    )
    // Forzamos levantar el globo flotante
    com.example.rest.ChatHeadManager.showChat(context, "rest_cycle_assistant", "Asistente Rest Cycle")

    // Añadimos configuración de Burbuja
    val bubbleData = com.example.rest.utils.BubbleHelper.createBubbleMetadata(context, pendingIntent)
    if (bubbleData != null) {
        builder.setBubbleMetadata(bubbleData)
        val person = com.example.rest.utils.BubbleHelper.createBotPerson()
        builder.addPerson(person)
        builder.setStyle(NotificationCompat.MessagingStyle(person)
            .addMessage(textContent, System.currentTimeMillis(), person))
    }

    try {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < 33
        ) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        } else {
             Toast.makeText(context, context.getString(R.string.toast_report_saved_no_notif), Toast.LENGTH_SHORT).show()
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
    val periodos = listOf(
        stringResource(R.string.stats_tab_daily), 
        stringResource(R.string.stats_tab_weekly), 
        stringResource(R.string.stats_tab_monthly)
    )
    
    // Launcher para notificación (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, context.getString(R.string.toast_no_notification_permission), Toast.LENGTH_SHORT).show()
        }
    }
    
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0D47A1),   // Azul profundo
            Color(0xFF00838F),   // Teal
            Color(0xFF00BFA5)    // Verde menta
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 2000f)
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
        val prefs = com.example.rest.utils.PreferencesManager(context)
        prefs.getUserName() ?: context.getString(R.string.fallback_user_name)
    }

    if (showDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            title = { Text(stringResource(R.string.dialog_download_report_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.dialog_download_report_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDownloadDialog = false
                    val stats = getUsageStats(context, periodoSeleccionado, semanaOffset)
                    generarReportePDF(context, stats, getPeriodoFecha(context, periodoSeleccionado, semanaOffset), nombreUsuario)
                }) {
                    Text(stringResource(R.string.btn_download))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.stats_title), 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Blanco
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.content_desc_back), tint = Blanco)
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
                // Estado para datos diarios y carga
                var datosDiarios by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
                var cargandoDiario by remember { mutableStateOf(false) }

                // Trigger para refrescar al volver (ON_RESUME)
                var refreshTrigger by remember { mutableStateOf(0) }
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                        if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                            refreshTrigger++
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                LaunchedEffect(semanaOffset, periodoSeleccionado, refreshTrigger) {
                    if (periodoSeleccionado == 1) { // Solo si es semanal
                        cargandoDiario = true
                        datosDiarios = withContext(Dispatchers.IO) { getUsageStatsDiario(context, semanaOffset) }
                        cargandoDiario = false
                    }
                }

                // Cargar lista general (Diario/Mensual/Semanal)
                var usageStats by remember { mutableStateOf<List<AppUsageInfo>>(emptyList()) }
                var cargandoGeneral by remember { mutableStateOf(false) }
                
                LaunchedEffect(periodoSeleccionado, semanaOffset, refreshTrigger) {
                     cargandoGeneral = true
                     usageStats = withContext(Dispatchers.IO) { getUsageStats(context, periodoSeleccionado, semanaOffset) }
                     cargandoGeneral = false
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
                                text = getPeriodoFecha(context, periodoSeleccionado, semanaOffset),
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
                            icon = { Icon(Icons.Default.Download, stringResource(R.string.btn_download)) },
                            text = { Text(stringResource(R.string.stats_download_report), fontWeight = FontWeight.Bold) },
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
                             val fechaTexto = getPeriodoFecha(context, 1, semanaOffset)
                             val splitted = fechaTexto.split(" - ")
                             val inicio = splitted.getOrElse(0) { "" }
                             val fin = splitted.getOrElse(1) { "" }
                            
                            GraficoBarrasSemanal(datosDiarios, inicio, fin, cargando = cargandoDiario)
                        } else {
                            // Vista diaria y mensual: Gráfico top apps
                            GraficoUso(usageStats, cargando = cargandoGeneral)
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        // Reutilizamos la lista ya cargada arriba
                        ListaUsoApps(usageStats, cargando = cargandoGeneral)
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
                                    stringResource(R.string.stats_health_warning),
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
            stringResource(R.string.dialog_permission_required),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Blanco
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            stringResource(R.string.permission_stats_desc),
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
                stringResource(R.string.btn_grant_permission),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primario
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onCheckPermission) {
            Text(
                stringResource(R.string.btn_already_granted),
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
                    stringResource(R.string.instructions_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.instructions_stats_steps),
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
    
    // Asegurar que startTime sea 00:00:00.000
    val startCalendar = Calendar.getInstance()
    startCalendar.add(Calendar.DAY_OF_YEAR, offset)
    
    when (period) {
        0 -> { /* Diario */ }
        1 -> { // Semanal (Lunes a Domingo)
            startCalendar.firstDayOfWeek = Calendar.MONDAY
            startCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        else -> { // Mensual (1 al último)
            startCalendar.set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    
    startCalendar.set(Calendar.HOUR_OF_DAY, 0)
    startCalendar.set(Calendar.MINUTE, 0)
    startCalendar.set(Calendar.SECOND, 0)
    startCalendar.set(Calendar.MILLISECOND, 0)
    val startTime = startCalendar.timeInMillis

    // Asegurar que endTime sea 23:59:59.999
    val endCalendar = Calendar.getInstance()
    endCalendar.timeInMillis = startTime
    when (period) {
        0 -> { /* Mismo día */ }
        1 -> endCalendar.add(Calendar.DAY_OF_YEAR, 6)
        else -> {
            endCalendar.add(Calendar.MONTH, 1)
            endCalendar.add(Calendar.DAY_OF_YEAR, -1)
        }
    }
    endCalendar.set(Calendar.HOUR_OF_DAY, 23)
    endCalendar.set(Calendar.MINUTE, 59)
    endCalendar.set(Calendar.SECOND, 59)
    endCalendar.set(Calendar.MILLISECOND, 999)
    val endTime = endCalendar.timeInMillis
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    Log.d("EstadisticasDebug", "=== INICIO CONSULTA ===")
    Log.d("EstadisticasDebug", "Periodo: $period, Offset: $offset")
    Log.d("EstadisticasDebug", "Desde: ${dateFormat.format(startTime)}")
    Log.d("EstadisticasDebug", "Hasta: ${dateFormat.format(endTime)}")
    
    try {
        val usageMap = mutableMapOf<String, Long>()

        // FASE 1: Unificar a queryUsageStats con INTERVAL_DAILY para máxima precisión
        // Esto evita el desfase de queryAndAggregateUsageStats y la limitación de queryEvents
        val statsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        
        if (statsList != null) {
            for (stats in statsList) {
                val packageName = stats.packageName
                val timeInForeground = stats.totalTimeInForeground
                if (timeInForeground > 0) {
                    usageMap[packageName] = (usageMap[packageName] ?: 0L) + timeInForeground
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
                                   packageName.contains("com.twitter") ||
                                   packageName.contains("com.zhiliaoapp.musically") || // TikTok
                                   packageName.contains("com.snapchat.android") ||    // Snapchat
                                   packageName.contains("com.netflix.mediaclient") || // Netflix
                                   packageName.contains("com.disney.disneyplus") ||   // Disney+
                                   packageName.contains("com.spotify.music")         // Spotify
                                   
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

fun formatUsageTime(context: Context, timeInMillis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
    
    return when {
        hours > 0 && minutes > 0 -> context.getString(R.string.time_hrs_min, hours, minutes)
        hours > 0 -> context.getString(R.string.time_hrs, hours)
        minutes > 0 -> context.getString(R.string.time_min, minutes)
        else -> context.getString(R.string.time_less_than_min)
    }
}

// Helper para obtener el texto de la fecha según el periodo (CON AÑO)
fun getPeriodoFecha(context: Context, periodo: Int, offset: Int = 0): String {
    val calendar = Calendar.getInstance()
    // Identificar locale actual para el SimpleDateFormat
    val localeActual = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales.get(0)
    } else {
        context.resources.configuration.locale
    }
    val formatoDia = SimpleDateFormat("dd MMM", localeActual)
    val formatoDiaFull = SimpleDateFormat("dd MMM yyyy", localeActual)

    return when (periodo) {
        0 -> {
            calendar.add(Calendar.DAY_OF_YEAR, offset)
            if (offset == 0) context.getString(R.string.date_today_prefix, formatoDiaFull.format(calendar.time))
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
    calendar.add(Calendar.WEEK_OF_YEAR, offset)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    
    val diasSemana = listOf(
        context.getString(R.string.day_mon), 
        context.getString(R.string.day_tue), 
        context.getString(R.string.day_wed), 
        context.getString(R.string.day_thu), 
        context.getString(R.string.day_fri), 
        context.getString(R.string.day_sat), 
        context.getString(R.string.day_sun)
    )
    val resultado = mutableMapOf<String, Long>()
    
    // Iterar por los 7 días
    val packageManager = context.packageManager
    for (i in 0 until 7) {
        val startDay = calendar.timeInMillis
        
        // El fin del día es exactamente 23:59:59.999 del día que acabamos de marcar
        val endDayCalendar = (calendar.clone() as Calendar)
        endDayCalendar.set(Calendar.HOUR_OF_DAY, 23)
        endDayCalendar.set(Calendar.MINUTE, 59)
        endDayCalendar.set(Calendar.SECOND, 59)
        endDayCalendar.set(Calendar.MILLISECOND, 999)
        val endDay = endDayCalendar.timeInMillis
        
        // Avanzar el calendario al inicio del siguiente día para la próxima iteración
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        
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
                                       packageName.contains("com.twitter") ||
                                       packageName.contains("com.zhiliaoapp.musically") || // TikTok
                                       packageName.contains("com.snapchat.android") ||    // Snapchat
                                       packageName.contains("com.netflix.mediaclient") || // Netflix
                                       packageName.contains("com.disney.disneyplus") ||   // Disney+
                                       packageName.contains("com.spotify.music")         // Spotify
                                       
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
    canvas.drawText(context.getString(R.string.pdf_report_title), textStartX, y + 25f, titlePaint)
    y += 80f
    
    // Información del Usuario y Fecha
    paint.isFakeBoldText = true
    canvas.drawText(context.getString(R.string.pdf_user_prefix, nombreUsuario), startX, y, paint)
    y += 20f
    canvas.drawText(context.getString(R.string.pdf_period_prefix, periodoStr), startX, y, paint)
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
        val tiempo = formatUsageTime(context, app.totalTimeInMillis)
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
                Toast.makeText(context, context.getString(R.string.toast_pdf_saved_downloads), Toast.LENGTH_LONG).show()
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
                Toast.makeText(context, context.getString(R.string.toast_error_saving_pdf, e.message ?: ""), Toast.LENGTH_SHORT).show()
            }
        }
    } else {
        val file = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), fileName)
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, context.getString(R.string.toast_pdf_saved_path, file.absolutePath), Toast.LENGTH_LONG).show()
            
            // Notificación para legacy (cuidado con FileUriExposedException en >= N)
            // Por seguridad en este entorno rápido, solo mostramos Toast en legacy
            // o podríamos usar FileProvider si estuviera configurado.
        } catch (e: Exception) {
             e.printStackTrace()
             Toast.makeText(context, context.getString(R.string.toast_legacy_error, e.message ?: ""), Toast.LENGTH_SHORT).show()
        }
    }
    pdfDocument.close()
}

@Composable
fun GraficoUso(stats: List<AppUsageInfo>, cargando: Boolean = false) {
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
        if (cargando) {
            // SHIMMER
            val transition = rememberInfiniteTransition(label = "shimmer_uso")
            val translateAnim by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1000f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(
                        durationMillis = 1200,
                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                    ),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Restart
                ),
                label = "shimmerTranslate"
            )
            
            val shimmerBrush = Brush.linearGradient(
                colors = listOf(
                    Color.LightGray.copy(alpha = 0.3f),
                    Color.LightGray.copy(alpha = 0.6f),
                    Color.LightGray.copy(alpha = 0.3f)
                ),
                start = Offset.Zero,
                end = Offset(x = translateAnim, y = translateAnim)
            )

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
                    repeat(5) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(30.dp)
                                    .fillMaxHeight(0.2f + (it % 3) * 0.2f)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(shimmerBrush)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(5) {
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(shimmerBrush)
                        )
                    }
                }
            }
        } else if (topApps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.stats_no_data),
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
fun ListaUsoApps(stats: List<AppUsageInfo>, cargando: Boolean = false) {
    val topApps = stats.take(10)
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.9f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (cargando) {
             // SHIMMER LISTA
            val transition = rememberInfiniteTransition(label = "shimmer_lista")
            val translateAnim by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1000f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(
                        durationMillis = 1200,
                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                    ),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Restart
                ),
                label = "shimmerTranslate"
            )
            
            val shimmerBrush = Brush.linearGradient(
                colors = listOf(
                    Color.LightGray.copy(alpha = 0.3f),
                    Color.LightGray.copy(alpha = 0.6f),
                    Color.LightGray.copy(alpha = 0.3f)
                ),
                start = Offset.Zero,
                end = Offset(x = translateAnim, y = translateAnim)
            )
            
            Column(modifier = Modifier.padding(16.dp)) {
                repeat(5) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(shimmerBrush))
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.width(100.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
                        }
                        Box(modifier = Modifier.width(60.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
                    }
                    if (it < 4) HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }

        } else if (topApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.stats_no_data),
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
                            formatUsageTime(LocalContext.current, appInfo.totalTimeInMillis),
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
