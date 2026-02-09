package com.example.rest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.rest.ui.theme.*

class HoraDescansoComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceIntent = android.content.Intent(this, UsageMonitorService::class.java)
        startService(serviceIntent)

        setContent {
            TemaRest {
                PantallaHoraDescanso(onBackClick = { finish() })
            }
        }
    }
}

// Data class moved to HorarioDescanso.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHoraDescanso(onBackClick: () -> Unit) {
    // Gradiente de fondo
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF80DEEA), Primario),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    val context = androidx.compose.ui.platform.LocalContext.current
    val horarios = remember { mutableStateListOf<HorarioDescanso>() }

    LaunchedEffect(Unit) {
        val saved = DowntimeManager.getSchedules(context)
        horarios.clear()
        horarios.addAll(saved)
    }

    var mostrarDialogo by remember { mutableStateOf(false) }
    var horarioEnEdicion by remember { mutableStateOf<HorarioDescanso?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, "Regresar", tint = Negro)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { 
                        horarioEnEdicion = null // Nuevo horario
                        mostrarDialogo = true 
                    },
                    containerColor = Color(0xFF00BCD4),
                    contentColor = Negro,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Add, "Agregar Otro")
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brochaGradiente)
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        // Intro Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home, // Placeholder for Owl/Sleep
                                    contentDescription = "Icono Descanso",
                                    modifier = Modifier.size(48.dp),
                                    tint = Negro
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Aquí puedes establecer horas para bloquear el dispositivo de tu hijo.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = Negro
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Warning for DND Permission if needed
                    val notificationManager = androidx.core.content.ContextCompat.getSystemService(context, android.app.NotificationManager::class.java)
                    val isDndGranted = notificationManager?.isNotificationPolicyAccessGranted == true
                    
                    if (!isDndGranted) {
                         item {
                             Card(
                                 colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), // Reddish warning
                                 shape = RoundedCornerShape(16.dp),
                                 modifier = Modifier.fillMaxWidth().clickable {
                                     val intent = android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                     context.startActivity(intent)
                                 }
                             ) {
                                 Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                     Icon(Icons.Default.Settings, contentDescription = "Alerta", tint = Color.Red)
                                     Spacer(modifier = Modifier.width(16.dp))
                                     Text(
                                         "Falta permiso para 'Modo Hora de Dormir'. Toca aquí para activarlo.",
                                         style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                         color = Color.Red
                                     )
                                 }
                             }
                             Spacer(modifier = Modifier.height(24.dp))
                         }
                    }

                    // Warning for Secure Settings (Grayscale)
                    val hasSecureSettings = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_SECURE_SETTINGS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    
                    if (!hasSecureSettings) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), // Orange warning
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    // Copy command to clipboard
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("ADB Command", "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS")
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "Comando ADB copiado al portapapeles", android.widget.Toast.LENGTH_LONG).show()
                                }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Settings, contentDescription = "Alerta", tint = Color(0xFFFF9800))
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            "Falta permiso para Escala de Grises",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFFE65100)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Este permiso requiere PC. Toca para copiar comando ADB.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    items(horarios) { horario ->
                        HorarioCard(
                            horario = horario,
                            onEdit = {
                                horarioEnEdicion = horario
                                mostrarDialogo = true
                            }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    
                    item {
                         Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        }

        if (mostrarDialogo) {
            DialogoEditarHorario(
                horario = horarioEnEdicion,
                onDismiss = { mostrarDialogo = false },
                onSave = { nuevoHorario ->
                    if (horarioEnEdicion != null) {
                        // Editar existente
                        val index = horarios.indexOfFirst { it.id == nuevoHorario.id }
                        if (index != -1) {
                            horarios[index] = nuevoHorario
                            DowntimeManager.saveSchedule(context, nuevoHorario)
                        }
                    } else {
                        // Agregar nuevo
                        // Generar ID simple
                        val newId = (horarios.maxOfOrNull { it.id } ?: 0) + 1
                        val finalHorario = nuevoHorario.copy(id = newId)
                        horarios.add(finalHorario)
                        DowntimeManager.saveSchedule(context, finalHorario)
                    }
                    mostrarDialogo = false
                }
            )
        }
    }
}

@Composable
fun HorarioCard(horario: HorarioDescanso, onEdit: () -> Unit) {
    var activo by remember { mutableStateOf(horario.activo) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = horario.nombre,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Negro
                )
                Switch(
                    checked = activo,
                    onCheckedChange = { isChecked ->
                        activo = isChecked
                        // Persist change
                        val updated = horario.copy(activo = isChecked)
                        DowntimeManager.saveSchedule(context, updated)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Blanco,
                        checkedTrackColor = Color(0xFF00BCD4),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Blanco
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${horario.horaInicio} - ${horario.horaFin}",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Negro
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Días Activos",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Negro
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dias = listOf("L", "M", "M", "J", "V", "S", "D")
                dias.forEachIndexed { index, dia ->
                    // Usamos el estado del horario para mostrar los dias activos
                    DaySelector(dia, selected = horario.diasActivos.getOrElse(index) { false })
                }
            }
            
             Spacer(modifier = Modifier.height(8.dp))
             
             Row(
                 modifier = Modifier.fillMaxWidth(),
                 horizontalArrangement = Arrangement.End
             ) {
                 IconButton(onClick = onEdit) {
                     Icon(Icons.Default.Settings, contentDescription = "Configurar", tint = Negro)
                 }
             }
        }
    }
}

@Composable
fun DaySelector(text: String, selected: Boolean, onClick: (() -> Unit)? = null) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (selected) Blanco else Color.Transparent)
            .border(1.dp, if (selected) Color.Transparent else Negro, CircleShape)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Negro
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
    initialHour: Int = 12,
    initialMinute: Int = 0
) {
    val timeState = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = false)

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.wrapContentSize()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seleccionar Hora",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                TimePicker(state = timeState)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancelar")
                    }
                    TextButton(onClick = { onConfirm(timeState.hour, timeState.minute) }) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoEditarHorario(
    horario: HorarioDescanso?,
    onDismiss: () -> Unit,
    onSave: (HorarioDescanso) -> Unit
) {
    var nombre by remember { mutableStateOf(horario?.nombre ?: "") }
    var horaInicio by remember { mutableStateOf(horario?.horaInicio ?: "") }
    var horaFin by remember { mutableStateOf(horario?.horaFin ?: "") }
    var bedtimeMode by remember { mutableStateOf(horario?.bedtimeMode ?: false) }
    // Estado para los 7 días
    val diasActivos = remember { mutableStateListOf(*(horario?.diasActivos?.toTypedArray() ?: Array(7) { false })) }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    var mostrarTimePickerInicio by remember { mutableStateOf(false) }
    var mostrarTimePickerFin by remember { mutableStateOf(false) }

    // Helper to parse existing time string "HH:MM AM/PM"
    fun parseTime(timeStr: String): Pair<Int, Int> {
        return try {
            if (timeStr.isBlank()) return 12 to 0
            val parts = timeStr.split(" ", ":")
            var h = parts[0].toInt()
            val m = parts[1].toInt()
            val ampm = parts[2]
            if (ampm == "PM" && h < 12) h += 12
            if (ampm == "AM" && h == 12) h = 0
            h to m
        } catch (e: Exception) {
            12 to 0
        }
    }

    // Helper to format time back
    fun formatTime(hour: Int, minute: Int): String {
        val ampm = if (hour >= 12) "PM" else "AM"
        val h = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
        return String.format("%d:%02d %s", h, minute, ampm)
    }


    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (horario == null) "Nuevo Horario" else "Editar Horario",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Negro
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Horario") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = horaInicio,
                        onValueChange = {},
                        label = { Text("Inicio") },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { mostrarTimePickerInicio = true },
                        enabled = false, // Disable typing, enable clicking via modifier
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Negro,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    OutlinedTextField(
                        value = horaFin,
                        onValueChange = {},
                        label = { Text("Fin") },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { mostrarTimePickerFin = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Negro,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                
                if (mostrarTimePickerInicio) {
                     val (initH, initM) = parseTime(horaInicio)
                     TimePickerDialog(
                         onDismissRequest = { mostrarTimePickerInicio = false },
                         onConfirm = { h, m ->
                             horaInicio = formatTime(h, m)
                             mostrarTimePickerInicio = false
                         },
                         initialHour = initH,
                         initialMinute = initM
                     )
                }

                if (mostrarTimePickerFin) {
                     val (initH, initM) = parseTime(horaFin)
                     TimePickerDialog(
                         onDismissRequest = { mostrarTimePickerFin = false },
                         onConfirm = { h, m ->
                             horaFin = formatTime(h, m)
                             mostrarTimePickerFin = false
                         },
                         initialHour = initH,
                         initialMinute = initM
                     )
                }


                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Días Activos", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val dias = listOf("L", "M", "M", "J", "V", "S", "D")
                    dias.forEachIndexed { index, dia ->
                        DaySelector(
                            text = dia, 
                            selected = diasActivos[index],
                            onClick = { diasActivos[index] = !diasActivos[index] }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                         Text(
                             "Activar Modo Hora de Dormir",
                             style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                             color = Negro
                         )
                         Text(
                             "Silencia notificaciones y llamadas.",
                             style = MaterialTheme.typography.bodySmall,
                             color = Color.Gray
                         )
                    }
                    Switch(
                        checked = bedtimeMode,
                        onCheckedChange = { 
                            bedtimeMode = it
                            // Check permission
                            val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                            if (it && !notificationManager.isNotificationPolicyAccessGranted) {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                context.startActivity(intent)
                                android.widget.Toast.makeText(context, "Concede permiso de 'No molestar' a Rest Cycle", android.widget.Toast.LENGTH_LONG).show()
                                bedtimeMode = false // Don't enable until granted
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Blanco,
                            checkedTrackColor = Color(0xFF00BCD4),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Blanco
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                HorarioDescanso(
                                    id = horario?.id ?: 0, // ID manejado en la lista
                                    nombre = nombre,
                                    horaInicio = horaInicio,
                                    horaFin = horaFin,
                                    diasActivos = diasActivos.toList(),
                                    activo = horario?.activo ?: true,
                                    bedtimeMode = bedtimeMode
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4))
                    ) {
                        Text("Guardar", color = Negro)
                    }
                }
            }
        }
    }
}
