package com.example.rest.features.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
 cristian-alvarado
import androidx.compose.material.icons.filled.Lightbulb
=======
import androidx.compose.material.icons.automirrored.filled.Logout
 main
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import androidx.compose.ui.platform.LocalContext
import com.example.rest.data.GeneradorContenidoMock
import com.example.rest.data.PreferenciasInteresManager
import com.example.rest.ui.theme.*

class InicioComposeActivity : BaseComposeActivity() {
    
    // Launcher para pedir permiso de notificaciones
    private val notificationPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, iniciar servicio
            com.example.rest.services.AppMonitorService.startService(this)
        } else {
            android.widget.Toast.makeText(
                this,
                getString(R.string.toast_notif_permission_needed),
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Iniciar el servicio de notificaciones periódicas si ya se han elegido temas
        if (com.example.rest.data.PreferenciasInteresManager.obtenerTemas(this).isNotEmpty()) {
            com.example.rest.services.TopicNotificationService.startService(this)
        }
        
        setContent {
            val isDarkMode = com.example.rest.utils.ThemeManager.isDarkMode(this)
            TemaRest(temaOscuro = isDarkMode) {
                var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }
                
                PantallaModosDeUso(
                    alClickRegresar = {
                        // Mostrar diálogo de confirmación
                        mostrarDialogoCerrarSesion = true
                    },
                    alClickConfiguracion = {
                        val intent = Intent(this, com.example.rest.features.settings.ConfiguracionComposeActivity::class.java)
                        startActivity(intent)
                    },
                    alClickControlParental = {
                        // Redirigir segun si es mayor de edad
                        val prefs = com.example.rest.utils.PreferencesManager(this)
                        val esMayor = prefs.getMayorEdad()
                        val intent = if (!esMayor) {
                            Intent(this, com.example.rest.features.hijo.EnlaceHijoComposeActivity::class.java)
                        } else {
                            Intent(this, com.example.rest.features.parental.GestionHijosComposeActivity::class.java)
                        }
                        startActivity(intent)
                    },
                    alClickHabitosSaludables = {
                        // Navegar a Perfil
                        val intent = Intent(this, com.example.rest.features.home.HabitosInicioComposeActivity::class.java)
                        startActivity(intent)
                    },
                    alClickTemasInteres = {
                        val intent = Intent(this, com.example.rest.features.tools.TemasInteresComposeActivity::class.java)
                        startActivity(intent)
                    },
                    onRequestNotificationPermission = {
                        // Pedir permiso de notificaciones
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            // Android < 13 no necesita permiso explícito
                            com.example.rest.services.AppMonitorService.startService(this)
                        }
                    }
                )
                
                // Diálogo de confirmación para cerrar sesión
                if (mostrarDialogoCerrarSesion) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogoCerrarSesion = false },
                        title = { Text(stringResource(R.string.dialog_logout_title)) },
                        text = { Text(stringResource(R.string.dialog_logout_text)) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    mostrarDialogoCerrarSesion = false
                                    
                                    // Detener servicio de monitoreo
                                    com.example.rest.services.AppMonitorService.stopService(this@InicioComposeActivity)
                                    
                                    // Borrar sesión
                                    val preferencesManager = com.example.rest.utils.PreferencesManager(this@InicioComposeActivity)
                                    preferencesManager.clearPreferences()
                                    
                                    // Ir a Login
                                    val intent = Intent(this@InicioComposeActivity, com.example.rest.features.auth.LoginComposeActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                            ) {
                                Text(stringResource(R.string.btn_logout))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarDialogoCerrarSesion = false }) {
                                Text(stringResource(R.string.btn_cancel))
                            }
                        }
                    )
                }

                // Check for Usage Stats Permission
                var showUsageStatsDialog by remember { mutableStateOf(false) }
                val context = LocalContext.current
                
                // Check on resume/start
                DisposableEffect(Unit) {
                    val hasPermission = checkUsageStatsPermission(context)
                    if (!hasPermission) {
                        showUsageStatsDialog = true
                    }
                    onDispose {}
                }

                if (showUsageStatsDialog) {
                    AlertDialog(
                        onDismissRequest = { /* No dismiss allowed optionally */ },
                        title = { Text(stringResource(R.string.dialog_permission_required)) },
                        text = { Text(stringResource(R.string.dialog_usage_permission_block_text)) },
                        confirmButton = {
                            TextButton(onClick = {
                                showUsageStatsDialog = false
                                startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            }) {
                                Text(stringResource(R.string.btn_grant_permission))
                            }
                        }
                    )
                }
            }
        }
    }
}


// Funciones auxiliares para permisos
fun checkUsageStatsPermission(context: android.content.Context): Boolean {
    val appOps = context.getSystemService(android.content.Context.APP_OPS_SERVICE) as android.app.AppOpsManager
    val mode = appOps.checkOpNoThrow(
        android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == android.app.AppOpsManager.MODE_ALLOWED
}

fun requestUsageStatsPermission(context: android.content.Context) {
    val intent = Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}

@Composable
fun PantallaModosDeUso(
    alClickRegresar: () -> Unit,
    alClickConfiguracion: () -> Unit,
    alClickControlParental: () -> Unit,
    alClickHabitosSaludables: () -> Unit,
    alClickTemasInteres: () -> Unit,
    onRequestNotificationPermission: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }
    var showBubblePermissionDialog by remember { mutableStateOf(false) }
    
    // Variable para controlar la animación del Tooltip de 7 segundos
    var showTooltip by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000) // 1 segundo antes de aparecer
        showTooltip = true
        kotlinx.coroutines.delay(7000) // Se queda por 7 segundos
        showTooltip = false
    }

    // Función auxiliar para verificar si las burbujas están activas
    fun areBubblesEnabled(): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val notificationManager = context.getSystemService(android.app.NotificationManager::class.java)
            return notificationManager.areBubblesAllowed()
        }
        return true // Para versiones anteriores a Android 11, devolvemos true porque no requieren permiso
    }

    // Acción pendiente para ejecutar después de otorgar permisos
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Función para verificar y pedir permisos antes de navegar
    val checkAndRequestPermissions = { action: () -> Unit ->
        val hasUsage = checkUsageStatsPermission(context)
        if (!hasUsage) {
            pendingAction = action
            showPermissionDialog = true
        } else {
            // Verificar Notificaciones (Android 13+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                val hasNotif = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                
                if (!hasNotif) {
                    pendingAction = action
                    showNotificationPermissionDialog = true
                } else {
                    // Todo OK, ejecutar acción e iniciar servicio si no está corriendo
                    com.example.rest.services.AppMonitorService.startService(context)
                    action()
                }
            } else {
                // Android < 13
                com.example.rest.services.AppMonitorService.startService(context)
                action()
            }
            
            // Verificamos si las burbujas están habilitadas
            if (!areBubblesEnabled()) {
                showBubblePermissionDialog = true
            }
        }
    }
    
    // Verificar si regresamos de configuración (onResume) y hay acción pendiente
    // Esto maneja el caso donde el usuario fue a Settings a dar permiso de uso y volvió
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                 if (pendingAction != null && checkUsageStatsPermission(context)) {
                     // Si ya tiene uso, verificar notif o ejecutar
                     if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                         val hasNotif = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        
                        if (hasNotif) {
                             val action = pendingAction
                             pendingAction = null
                             com.example.rest.services.AppMonitorService.startService(context)
                             action?.invoke()
                        }
                     } else {
                         val action = pendingAction
                         pendingAction = null
                         com.example.rest.services.AppMonitorService.startService(context)
                         action?.invoke()
                     }
                 }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Diálogo de permiso de uso de estadísticas
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { /* No permitir cerrar sin decidir */ },
            title = { Text(stringResource(R.string.dialog_permission_required)) },
            text = { Text(stringResource(R.string.dialog_usage_permission_stats_text)) },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        requestUsageStatsPermission(context)
                    }
                ) {
                    Text(stringResource(R.string.btn_activate_permission))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showPermissionDialog = false 
                        android.widget.Toast.makeText(context, context.getString(R.string.toast_monitoring_unavailable), android.widget.Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text(stringResource(R.string.btn_not_now))
                }
            }
        )
    }
    
    // Diálogo de permiso de notificaciones
    if (showNotificationPermissionDialog) {
        AlertDialog(
            onDismissRequest = { /* No permitir cerrar sin decidir */ },
            title = { Text(stringResource(R.string.dialog_notif_permission_title)) },
            text = { Text(stringResource(R.string.dialog_notif_permission_text)) },
            confirmButton = {
                Button(
                    onClick = {
                        showNotificationPermissionDialog = false
                        onRequestNotificationPermission()
                    }
                ) {
                    Text(stringResource(R.string.btn_allow))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showNotificationPermissionDialog = false 
                        android.widget.Toast.makeText(context, context.getString(R.string.toast_monitoring_unavailable), android.widget.Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text(stringResource(R.string.btn_not_now))
                }
            }
        )
    }

 cristian-alvarado
    // Diálogo persuasivo para las Burbujas (Android 11+)
    if (showBubblePermissionDialog) {
        AlertDialog(
            onDismissRequest = { showBubblePermissionDialog = false },
            title = { Text("Habilitar Burbujas Flotantes") },
            text = { Text("Rest Cycle usa burbujas interactivas para mostrar los reportes y recordatorios de tareas de manera amigable.\n\nPor favor, dirígete a las configuraciones de la aplicación, busca 'Burbujas' o 'Bubbles' y selecciona 'Todas las conversaciones pueden mostrar burbujas'.") },
            confirmButton = {
                Button(
                    onClick = {
                        showBubblePermissionDialog = false
                        val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Abrir Ajustes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBubblePermissionDialog = false }
                ) {
                    Text("Más Tarde")
                }
            }
        )
    }

    // Gradiente de fondo cyan/turquesa
=======
    // Gradiente de fondo estilo Hijo (Azul profundo -> Teal -> Verde menta)
 main
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0D47A1),   // Azul profundo
            Color(0xFF00838F),   // Teal
            Color(0xFF00BFA5)    // Verde menta
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 2000f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brochaGradiente)
    ) {
        // Botón de regresar en la esquina superior izquierda
        IconButton(
            onClick = alClickRegresar,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = stringResource(R.string.btn_logout),
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // Botón de configuración en la esquina superior derecha
        IconButton(
            onClick = alClickConfiguracion,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.content_desc_settings),
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 80.dp) // Padding top para no solapar los botones superiores
        ) {
            // Sección Dinámica "Para Ti" basada en los Temas de Interés
            SeccionParaTi(alClickTemasInteres = alClickTemasInteres)
            
            Spacer(modifier = Modifier.height(20.dp))

            // Título "Modos de Uso"
            Text(
                text = stringResource(R.string.home_usage_modes),
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // Botón Control Parental
            Button(
                onClick = { checkAndRequestPermissions(alClickControlParental) },
                modifier = Modifier
                    .width(260.dp)
                    .height(56.dp)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.15f)
                )
            ) {
                Text(
                    text = stringResource(R.string.home_parental_control),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Logo del búho
            Image(
                painter = painterResource(id = R.drawable.buho_background),
                contentDescription = stringResource(R.string.content_desc_owl_logo),
                modifier = Modifier
                    .size(180.dp)
                    .padding(vertical = 20.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botón Hábitos Saludables
            Button(
                onClick = { checkAndRequestPermissions(alClickHabitosSaludables) },
                modifier = Modifier
                    .width(260.dp)
                    .height(56.dp)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.15f)
                )
            ) {
                Text(
                    text = stringResource(R.string.home_healthy_habits),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Botón Temas de Interés (Bombillo) Flotante Arriba a la Derecha con Tooltip
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Tooltip animado que dura visible 7 segundos
                androidx.compose.animation.AnimatedVisibility(
                    visible = showTooltip,
                    enter = androidx.compose.animation.fadeIn(
                        animationSpec = androidx.compose.animation.core.tween(500)
                    ) + androidx.compose.animation.expandHorizontally(
                        expandFrom = Alignment.End, 
                        animationSpec = androidx.compose.animation.core.tween(500)
                    ),
                    exit = androidx.compose.animation.fadeOut(
                        animationSpec = androidx.compose.animation.core.tween(500)
                    ) + androidx.compose.animation.shrinkHorizontally(
                        shrinkTowards = Alignment.End, 
                        animationSpec = androidx.compose.animation.core.tween(500)
                    )
                ) {
                    Card(
                        modifier = Modifier
                            .widthIn(max = 200.dp)
                            .padding(end = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp, topEnd = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Personaliza tu experiencia",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Toca aquí para elegir tus Temas de Interés y ver contenido adaptado a ti.",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Bombillo ajustado de tamaño
                IconButton(
                    onClick = {
                        showTooltip = false // Lo ocultamos al tocar
                        alClickTemasInteres()
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFF59D).copy(alpha = 0.9f), Color.Transparent),
                                radius = 120f
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Temas de Interés",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SeccionParaTi(alClickTemasInteres: () -> Unit) {
    val context = LocalContext.current
    var temasElegidos by remember { mutableStateOf(PreferenciasInteresManager.obtenerTemas(context)) }
    var fraseDelDia by remember { mutableStateOf("") }
    
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                // Actualizar temas elegidos y generar nueva frase cada vez que la pantalla vuelve al frente
                temasElegidos = PreferenciasInteresManager.obtenerTemas(context)
                if (temasElegidos.isNotEmpty()) {
                    fraseDelDia = GeneradorContenidoMock.generarFraseMotivacional(temasElegidos)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (temasElegidos.isNotEmpty() && fraseDelDia.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Para ti",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Negro
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Frase motivacional
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"$fraseDelDia\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        // Si la lista de temas está vacía, no mostramos nada en esta sección ya que el bombillo flotante 
        // superior se encarga de recordarle al usuario configurar sus temas.
    }
}
