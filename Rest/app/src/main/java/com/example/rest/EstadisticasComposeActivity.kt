// ============================================================================
// DECLARACIÓN DEL PAQUETE
// ============================================================================
// Define a qué "carpeta lógica" pertenece este archivo dentro del proyecto
// Tipo: Declaración de paquete
package com.example.rest

// ============================================================================
// IMPORTS (IMPORTACIONES)
// ============================================================================
// Estas líneas traen funcionalidades de otras partes de Android y librerías
// Tipo: Declaraciones de importación

// --- Imports de Android para permisos y estadísticas ---
import android.app.AppOpsManager              // Sirve para: Verificar permisos especiales de la app
import android.app.usage.UsageStats           // Sirve para: Obtener datos de uso de apps (tiempo en pantalla)
import android.app.usage.UsageStatsManager    // Sirve para: Consultar estadísticas de uso del sistema
import android.content.Context                // Sirve para: Acceder a recursos y servicios del sistema Android
import android.content.Intent                 // Sirve para: Navegar entre pantallas o abrir configuraciones
import android.os.Bundle                      // Sirve para: Guardar/restaurar estado de la actividad
import android.provider.Settings              // Sirve para: Abrir pantallas de configuración del sistema
import android.util.Log                       // Sirve para: Imprimir mensajes de depuración en Logcat

// --- Imports de Jetpack Compose (UI moderna) ---
import androidx.activity.ComponentActivity    // Sirve para: Clase base para actividades con Compose
import androidx.activity.compose.setContent   // Sirve para: Definir la interfaz de usuario con Compose
import androidx.compose.animation.core.animateFloatAsState  // Sirve para: Crear animaciones suaves
import androidx.compose.foundation.background // Sirve para: Añadir color/gradiente de fondo
import androidx.compose.foundation.layout.*   // Sirve para: Organizar elementos (Column, Row, Box, etc.)
import androidx.compose.foundation.lazy.LazyColumn  // Sirve para: Crear listas eficientes (como RecyclerView)
import androidx.compose.foundation.lazy.items       // Sirve para: Iterar sobre listas en LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape  // Sirve para: Crear esquinas redondeadas
import androidx.compose.material.icons.Icons        // Sirve para: Acceder a iconos predefinidos
import androidx.compose.material.icons.filled.ArrowBack  // Sirve para: Icono de flecha atrás
import androidx.compose.material.icons.filled.Warning    // Sirve para: Icono de advertencia
import androidx.compose.material.icons.filled.Lock       // Sirve para: Icono de candado
import androidx.compose.material3.*           // Sirve para: Componentes Material Design 3 (botones, cards, etc.)
import androidx.compose.runtime.*             // Sirve para: Manejar estado reactivo (remember, mutableStateOf)
import androidx.compose.ui.Alignment          // Sirve para: Alinear elementos (centro, arriba, abajo)
import androidx.compose.ui.Modifier           // Sirve para: Modificar propiedades de componentes (tamaño, padding)
import androidx.compose.ui.draw.clip          // Sirve para: Recortar formas (aplicar bordes redondeados)
import androidx.compose.ui.geometry.Offset    // Sirve para: Definir posiciones en gradientes
import androidx.compose.ui.graphics.Brush     // Sirve para: Crear gradientes de color
import androidx.compose.ui.graphics.Color     // Sirve para: Definir colores
import androidx.compose.ui.platform.LocalContext  // Sirve para: Obtener el contexto de Android en Compose
import androidx.compose.ui.text.font.FontWeight   // Sirve para: Definir grosor de texto (Bold, Normal)
import androidx.compose.ui.text.style.TextAlign   // Sirve para: Alinear texto (centro, izquierda)
import androidx.compose.ui.unit.dp           // Sirve para: Definir medidas en density-independent pixels

// --- Imports del proyecto ---
import com.example.rest.ui.theme.*           // Sirve para: Importar colores y estilos personalizados

// --- Imports de Java para fechas y tiempo ---
import java.util.Calendar                    // Sirve para: Trabajar con fechas (hoy, hace 7 días, etc.)
import java.util.concurrent.TimeUnit         // Sirve para: Convertir milisegundos a horas/minutos

// ============================================================================
// CLASE PRINCIPAL: EstadisticasComposeActivity
// ============================================================================
// Esta función sirve para: Crear la pantalla de estadísticas de uso de apps
// Esta función llama a: PantallaEstadisticas (función composable)
// Tipo: Clase (Activity de Android)
class EstadisticasComposeActivity : ComponentActivity() {
    // Esta función sirve para: Inicializar la actividad cuando se abre
    // Esta función cambia: El contenido visual de la pantalla
    // Esta función llama a: setContent, TemaRest, PantallaEstadisticas
    // Tipo: Función (método override de ciclo de vida)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                PantallaEstadisticas(onBackClick = { finish() })
            }
        }
    }
}

// ============================================================================
// MODELO DE DATOS: AppUsageInfo
// ============================================================================
// Esta clase sirve para: Almacenar información de uso de una aplicación
// Contiene: Nombre de la app, nombre del paquete, tiempo de uso en milisegundos
// Tipo: Data class (clase de datos inmutable)
data class AppUsageInfo(
    val appName: String,           // Nombre visible de la app (ej: "WhatsApp")
    val packageName: String,       // Identificador único (ej: "com.whatsapp")
    val totalTimeInMillis: Long    // Tiempo de uso en milisegundos
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEstadisticas(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(checkUsageStatsPermission(context)) }
    var periodoSeleccionado by remember { mutableStateOf(0) }
    val periodos = listOf("Diario", "Semanal", "Mensual")
    
    // Gradiente de fondo
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF80DEEA), Primario),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    // Recargar el estado del permiso cuando la actividad vuelve a primer plano
    DisposableEffect(Unit) {
        onDispose { }
    }
    
    LaunchedEffect(Unit) {
        hasPermission = checkUsageStatsPermission(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Estadísticas de Uso", 
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
            if (!hasPermission) {
                // Mostrar pantalla de solicitud de permiso
                PermissionRequestScreen(
                    onRequestPermission = {
                        requestUsageStatsPermission(context)
                    },
                    onCheckPermission = {
                        hasPermission = checkUsageStatsPermission(context)
                    }
                )
            } else {
                // Mostrar estadísticas reales
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Selector de Periodo
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        TabRow(
                            selectedTabIndex = periodoSeleccionado,
                            containerColor = Color.Transparent,
                            contentColor = Negro,
                            divider = {}
                        ) {
                            periodos.forEachIndexed { index, titulo ->
                                Tab(
                                    selected = periodoSeleccionado == index,
                                    onClick = { periodoSeleccionado = index },
                                    text = { 
                                        Text(
                                            titulo, 
                                            fontWeight = if (periodoSeleccionado == index) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Gráfico
                    item {
                        GraficoUso(periodoSeleccionado, context)
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Lista de Apps
                    item {
                        ListaUsoApps(periodoSeleccionado, context)
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    
                    // Advertencia
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5252)),
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
                                    "El uso excesivo de aplicaciones puede ser perjudicial para el desarrollo cognitivo.",
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
            tint = Primario
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Permiso Requerido",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Negro
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Para mostrar estadísticas de uso de aplicaciones, necesitamos acceso a los datos de uso del sistema.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Negro.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primario)
        ) {
            Text(
                "Otorgar Permiso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onCheckPermission) {
            Text(
                "Ya otorgué el permiso",
                color = Primario
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
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

// ============================================================================
// FUNCIÓN: checkUsageStatsPermission
// ============================================================================
// Esta función sirve para: Verificar si la app tiene permiso para ver estadísticas de uso
// Esta función cambia: Nada (solo consulta)
// Esta función llama a: AppOpsManager.checkOpNoThrow
// Retorna: true si tiene permiso, false si no
// Tipo: Función (retorna Boolean)
fun checkUsageStatsPermission(context: Context): Boolean {
    // Obtener el servicio que maneja permisos especiales
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    
    // Verificar el estado del permiso PACKAGE_USAGE_STATS
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,  // Tipo de permiso a verificar
        android.os.Process.myUid(),            // ID de nuestra app
        context.packageName                    // Nombre del paquete de nuestra app
    )
    
    // Retornar true solo si el permiso está permitido
    return mode == AppOpsManager.MODE_ALLOWED
}

// ============================================================================
// FUNCIÓN: requestUsageStatsPermission
// ============================================================================
// Esta función sirve para: Abrir la pantalla de configuración donde el usuario otorga el permiso
// Esta función cambia: Navega a otra pantalla (Configuración del sistema)
// Esta función llama a: context.startActivity
// Tipo: Función (no retorna nada - Unit)
fun requestUsageStatsPermission(context: Context) {
    // Crear un Intent (intención) para abrir la configuración de acceso a uso
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    
    // Abrir la pantalla de configuración
    context.startActivity(intent)
}

// Obtener apps instaladas por el usuario que tienen ícono en el launcher
fun getUserInstalledLaunchableApps(context: Context): Set<String> {
    val packageManager = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    
    // Obtener todas las apps con ícono en el launcher
    val launchableApps = packageManager.queryIntentActivities(mainIntent, 0)
    
    Log.d("EstadisticasApp", "Total apps con launcher intent: ${launchableApps.size}")
    
    // Filtrar para obtener solo apps instaladas por el usuario
    val userApps = launchableApps.mapNotNull { resolveInfo ->
        try {
            val packageName = resolveInfo.activityInfo.packageName
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            
            // Verificar si es app del sistema
            val isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            
            Log.d("EstadisticasApp", "Procesando $packageName: isSystem=$isSystemApp, isUpdated=$isUpdatedSystemApp")

            // CAMBIO: Aceptamos TODAS las apps que tengan launcher, sean de sistema o no.
            // Esto arregla el problema de apps preinstaladas (como WhatsApp en algunos dispositivos) que no se mostraban.
            Log.d("EstadisticasApp", "App de usuario ACEPTADA (tiene Launcher): $packageName")
            packageName
        } catch (e: Exception) {
            Log.e("EstadisticasApp", "Error procesando app: ${e.message}")
            null
        }
    }.toSet()
    
    Log.d("EstadisticasApp", "Apps de usuario filtradas: ${userApps.size}")
    return userApps
}

// ============================================================================
// FUNCIÓN: getUsageStats (LA MÁS IMPORTANTE)
// ============================================================================
// Esta función sirve para: Obtener la lista de apps con su tiempo de uso en un período
// Esta función cambia: Nada en el sistema, solo consulta y procesa datos
// Esta función llama a: UsageStatsManager.queryUsageStats, packageManager.getLaunchIntentForPackage
// Retorna: Lista de AppUsageInfo ordenada por tiempo de uso (mayor a menor)
// Tipo: Función (retorna List<AppUsageInfo>)
fun getUsageStats(context: Context, period: Int): List<AppUsageInfo> {
    // --- PASO 1: Obtener servicios del sistema ---
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val packageManager = context.packageManager
    
    // --- PASO 2: Calcular el rango de fechas ---
    val calendar = Calendar.getInstance()  // Obtener fecha/hora actual
    val endTime = calendar.timeInMillis    // Tiempo final = ahora
    
    // Calcular tiempo de inicio según el período seleccionado
    // Tipo: Expresión when (similar a switch en otros lenguajes)
    calendar.apply {
        when (period) {
            0 -> add(Calendar.DAY_OF_YEAR, -1)   // Diario: restar 1 día
            1 -> add(Calendar.DAY_OF_YEAR, -7)   // Semanal: restar 7 días
            else -> add(Calendar.DAY_OF_YEAR, -30) // Mensual: restar 30 días
        }
    }
    val startTime = calendar.timeInMillis  // Tiempo inicial = hace X días
    
    Log.d("EstadisticasApp", "Consultando stats desde: $startTime hasta: $endTime")
    
    // --- PASO 3: Consultar estadísticas al sistema ---
    // Esta llamada le pide a Android todas las apps que se usaron en el período
    val usageStatsList = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,  // Agrupar por día
        startTime,                          // Desde cuándo
        endTime                             // Hasta cuándo
    )
    
    Log.d("EstadisticasApp", "UsageStats obtenidos: ${usageStatsList?.size ?: 0}")
    
    // --- PASO 4: Filtrar y agrupar apps ---
    // Crear un mapa (diccionario) para acumular tiempos por app
    // Tipo: Mutable Map (colección clave-valor modificable)
    val usageMap = mutableMapOf<String, Long>()
    
    // Iterar sobre cada estadística recibida
    // Tipo: forEach (bucle/loop)
    usageStatsList?.forEach { usageStats ->
        val packageName = usageStats.packageName      // Ej: "com.whatsapp"
        val totalTime = usageStats.totalTimeInForeground  // Tiempo en milisegundos
        
        // Solo procesar apps que se usaron (tiempo > 0)
        // Tipo: Condicional if
        if (totalTime > 0) {
            // Verificar si la app es "lanzable" (tiene interfaz de usuario)
            // ESTO ES LA CLAVE: getLaunchIntentForPackage retorna null si la app no se puede abrir
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            
            // Tipo: Condicional if-else
            if (launchIntent != null) {
                // La app SÍ se puede abrir (WhatsApp, YouTube, etc.)
                // Acumular el tiempo (si ya existía, sumar; si no, crear nueva entrada)
                usageMap[packageName] = (usageMap[packageName] ?: 0) + totalTime
                Log.d("EstadisticasApp", "-> App AGREGADA (tiene LaunchIntent): $packageName - Tiempo: ${totalTime}ms")
            } else {
                // La app NO se puede abrir (servicios del sistema, etc.)
                Log.d("EstadisticasApp", "-> App DESCARTADA (Sin LaunchIntent): $packageName")
            }
        }
    }
    
    Log.d("EstadisticasApp", "Apps con uso después de filtrar: ${usageMap.size}")
    
    // --- PASO 5: Convertir el mapa a lista de objetos AppUsageInfo ---
    // mapNotNull = mapear (transformar) cada elemento, ignorando los que retornen null
    // Tipo: Función de transformación de colecciones
    val appUsageList = usageMap.mapNotNull { (packageName, totalTime) ->
        // Tipo: Bloque try-catch (manejo de errores)
        try {
            // Obtener información de la app (nombre visible, icono, etc.)
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            
            // Crear objeto AppUsageInfo con los datos
            AppUsageInfo(appName, packageName, totalTime)
        } catch (e: Exception) {
            // Si hay error (app desinstalada mientras se ejecutaba, etc.)
            Log.e("EstadisticasApp", "Error obteniendo info de $packageName: ${e.message}")
            // Usar el packageName como nombre de respaldo
            AppUsageInfo(packageName, packageName, totalTime)
        }
    }
    
    // Imprimir logs de depuración
    Log.d("EstadisticasApp", "Apps finales en lista: ${appUsageList.size}")
    appUsageList.take(10).forEach { 
        Log.d("EstadisticasApp", "App: ${it.appName} - ${formatUsageTime(it.totalTimeInMillis)}")
    }
    
    // --- PASO 6: Ordenar y retornar ---
    // Ordenar de mayor a menor tiempo de uso
    // Tipo: Función de ordenamiento + return implícito
    return appUsageList.sortedByDescending { it.totalTimeInMillis }
}

// ============================================================================
// FUNCIÓN: formatUsageTime
// ============================================================================
// Esta función sirve para: Convertir milisegundos a formato legible (ej: "2h 30m")
// Esta función cambia: Nada, solo transforma un número
// Esta función llama a: TimeUnit.MILLISECONDS.toHours, TimeUnit.MILLISECONDS.toMinutes
// Retorna: String con el tiempo formateado
// Tipo: Función (retorna String)
fun formatUsageTime(timeInMillis: Long): String {
    // Convertir milisegundos a horas y minutos
    val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60  // % 60 = resto de dividir entre 60
    
    // Decidir qué formato mostrar según los valores
    // Tipo: Expresión when (retorna directamente el String)
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"  // Ej: "2h 30m"
        hours > 0 -> "${hours}h"                              // Ej: "5h"
        minutes > 0 -> "${minutes}m"                          // Ej: "45m"
        else -> "< 1m"                                        // Menos de 1 minuto
    }
}

@Composable
fun GraficoUso(periodo: Int, context: Context) {
    val usageStats = remember(periodo) { getUsageStats(context, periodo) }
    val topApps = usageStats.take(5)
    
    // Calcular el máximo para normalizar las barras
    val maxTime = topApps.maxOfOrNull { it.totalTimeInMillis } ?: 1L
    
    val colores = listOf(
        Color(0xFF6750A4), 
        Color(0xFF4CAF50), 
        Color(0xFFFFEB3B), 
        Color(0xFFFF9800), 
        Color(0xFFF44336)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.5f)),
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
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.7f)),
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
