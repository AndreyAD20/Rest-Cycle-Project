package com.example.rest.features.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import androidx.compose.ui.platform.LocalContext
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
                "El permiso de notificaciones es necesario para el monitoreo en tiempo real",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        // Navegar a Control Parental
                        val intent = Intent(this, com.example.rest.features.parental.GestionHijosComposeActivity::class.java)
                        startActivity(intent)
                    },
                    alClickHabitosSaludables = {
                        // Navegar a Perfil
                        val intent = Intent(this, com.example.rest.features.home.PerfilComposeActivity::class.java)
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
                        title = { Text("Cerrar sesión") },
                        text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    mostrarDialogoCerrarSesion = false
                                    
                                    // Detener servicio de monitoreo
                                    com.example.rest.services.AppMonitorService.stopService(this@InicioComposeActivity)
                                    
                                    // Borrar sesión
                                    val sharedPref = getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE)
                                    with(sharedPref.edit()) {
                                        clear()
                                        apply()
                                    }
                                    
                                    // Ir a Login
                                    val intent = Intent(this@InicioComposeActivity, com.example.rest.features.auth.LoginComposeActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                            ) {
                                Text("Cerrar Sesión")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarDialogoCerrarSesion = false }) {
                                Text("Cancelar")
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
                        title = { Text("Permiso Requerido") },
                        text = { Text("Para que Rest Cycle funcione correctamente y bloquee aplicaciones, necesita acceso a las estadísticas de uso.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showUsageStatsDialog = false
                                startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            }) {
                                Text("Conceder Permiso")
                            }
                        }
                    )
                }

                // Check for Overlay Permission (Android 10+)
                var showOverlayDialog by remember { mutableStateOf(false) }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
                    // Only show if usage stats is already granted or dismissed to avoid stacking
                    if (!showUsageStatsDialog) {
                         showOverlayDialog = true
                    }
                }

                if (showOverlayDialog) {
                    AlertDialog(
                        onDismissRequest = { /* No dismiss */ },
                        title = { Text("Permiso de Superposición") },
                        text = { Text("Para mostrar la pantalla de bloqueo sobre otras apps, Rest Cycle necesita permiso para mostrarse encima.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showOverlayDialog = false
                                val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                // No pasamos el paquete para que abra la lista general (fix para algunos dispositivos)
                                // intent.data = android.net.Uri.parse("package:$packageName")
                                startActivity(intent)
                            }) {
                                Text("Conceder Permiso")
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
    onRequestNotificationPermission: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }

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
            title = { Text("Permiso Requerido") },
            text = { Text("Para que la aplicación funcione correctamente y registre tus estadísticas en tiempo real, necesitamos acceso a los datos de uso. Por favor activa el permiso para 'Rest Cycle'.") },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        requestUsageStatsPermission(context)
                    }
                ) {
                    Text("Activar Permiso")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showPermissionDialog = false 
                        android.widget.Toast.makeText(context, "El monitoreo en tiempo real no estará disponible", android.widget.Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text("Ahora no")
                }
            }
        )
    }
    
    // Diálogo de permiso de notificaciones
    if (showNotificationPermissionDialog) {
        AlertDialog(
            onDismissRequest = { /* No permitir cerrar sin decidir */ },
            title = { Text("Permiso de Notificaciones") },
            text = { Text("Para monitorear tus aplicaciones en tiempo real, necesitamos mostrar una notificación persistente. Esto es requerido por Android para servicios en segundo plano.") },
            confirmButton = {
                Button(
                    onClick = {
                        showNotificationPermissionDialog = false
                        onRequestNotificationPermission()
                    }
                ) {
                    Text("Permitir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showNotificationPermissionDialog = false 
                        android.widget.Toast.makeText(context, "El monitoreo en tiempo real no estará disponible", android.widget.Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text("Ahora no")
                }
            }
        )
    }

    // Gradiente de fondo cyan/turquesa
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
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
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Regresar",
                tint = MaterialTheme.colorScheme.onBackground,
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
                contentDescription = "Configuración",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            // Título "Modos de Uso"
            Text(
                text = "Modos de Uso",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Negro,
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
                        width = 2.dp,
                        color = Negro,
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00ACC1)
                )
            ) {
                Text(
                    text = "Control Parental",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Negro
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Logo del búho
            Image(
                painter = painterResource(id = R.drawable.buho_background),
                contentDescription = "Logo Búho",
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
                        width = 2.dp,
                        color = Negro,
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00ACC1)
                )
            ) {
                Text(
                    text = "Habitos Saludables",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Negro
                )
            }
        }
    }
}
