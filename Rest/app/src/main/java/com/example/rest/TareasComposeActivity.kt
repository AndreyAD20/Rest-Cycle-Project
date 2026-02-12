package com.example.rest

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.rest.ui.theme.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class TareasComposeActivity : ComponentActivity() {
    
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Crear canal de notificaciones
        TaskNotificationManager.createNotificationChannel(this)
        
        // Solicitar permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        setContent {
            TemaRest {
                PantallaTareas(
                    context = this,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

data class Tarea(
    val id: Int,
    val titulo: String,
    val nota: String = "",
    val hora: String,
    val tieneHora: Boolean = false,
    val vibracion: Boolean = false,
    val sonido: Boolean = false,
    val completada: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(context: Context, onBackClick: () -> Unit) {
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF80DEEA), Primario),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    // Cargar tareas guardadas
    val tareas = remember {
        mutableStateListOf<Tarea>().apply {
            addAll(loadTasks(context))
        }
    }

    var mostrarDialogoTarea by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Mis Tareas",
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
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { mostrarDialogoTarea = true },
                    containerColor = Color(0xFF00BCD4),
                    contentColor = Negro
                ) {
                    Icon(Icons.Default.Add, "Nueva Tarea")
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    
                    items(tareas) { tarea ->
                        TareaItem(
                            tarea = tarea,
                            onCheckedChange = { isChecked ->
                                val index = tareas.indexOf(tarea)
                                if (index != -1) {
                                    tareas[index] = tarea.copy(completada = isChecked)
                                    saveTasks(context, tareas)
                                    
                                    // Cancelar alarma si se completa la tarea
                                    if (isChecked) {
                                        TaskNotificationManager.cancelTaskAlarm(context, tarea.id)
                                    }
                                }
                            }
                        )
                    }
                    
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        if (mostrarDialogoTarea) {
            AgregarTareaDialog(
                context = context,
                onDismiss = { mostrarDialogoTarea = false },
                onSave = { nuevaTarea ->
                    val newId = (tareas.maxOfOrNull { it.id } ?: 0) + 1
                    val tareaConId = nuevaTarea.copy(id = newId)
                    tareas.add(tareaConId)
                    saveTasks(context, tareas)
                    
                    // Programar alarma si tiene hora asignada
                    if (tareaConId.tieneHora) {
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                            // Si no tiene permiso de alarmas exactas, avisar y redirigir
                            android.widget.Toast.makeText(context, "Se necesita permiso para alarmas exactas para que las notificaciones funcionen bien", android.widget.Toast.LENGTH_LONG).show()
                            val intent = android.content.Intent().apply {
                                action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                            }
                            context.startActivity(intent)
                        } else {
                            TaskNotificationManager.scheduleTaskAlarm(context, tareaConId)
                            android.widget.Toast.makeText(context, "Tarea guardada con recordatorio", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        android.widget.Toast.makeText(context, "Tarea guardada", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    
                    mostrarDialogoTarea = false
                }
            )
        }
    }
}

@Composable
fun TareaItem(tarea: Tarea, onCheckedChange: (Boolean) -> Unit) {
    val alpha by animateFloatAsState(targetValue = if (tarea.completada) 0.5f else 1f)

    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = tarea.completada,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Primario,
                    uncheckedColor = Color.Gray,
                    checkmarkColor = Blanco
                )
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = tarea.titulo,
                    style = TextStyle(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        textDecoration = if (tarea.completada) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = Negro
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tarea.hora,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarTareaDialog(
    context: Context,
    onDismiss: () -> Unit,
    onSave: (Tarea) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var asignarHora by remember { mutableStateOf(false) }
    var vibracion by remember { mutableStateOf(false) }
    var sonido by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)), // Light cyan background
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Text(
                    text = "Nueva Tarea",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Negro
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Título Field
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00BCD4),
                        unfocusedBorderColor = Color.Gray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Nota Field
                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Nota...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00BCD4),
                        unfocusedBorderColor = Color.Gray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Asignar Hora Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Asignar hora",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Negro
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = asignarHora,
                            onCheckedChange = { asignarHora = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF00BCD4),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                        
                        if (asignarHora) {
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = {
                                    // Mostrar Time Picker
                                    val calendar = Calendar.getInstance()
                                    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                                    val currentMinute = calendar.get(Calendar.MINUTE)
                                    
                                    TimePickerDialog(
                                        context,
                                        { _, selectedHour, selectedMinute ->
                                            // Convertir a formato 12 horas con AM/PM
                                            val amPm = if (selectedHour >= 12) "PM" else "AM"
                                            val hour12 = when {
                                                selectedHour == 0 -> 12
                                                selectedHour > 12 -> selectedHour - 12
                                                else -> selectedHour
                                            }
                                            hora = String.format("%d:%02d %s", hour12, selectedMinute, amPm)
                                        },
                                        currentHour,
                                        currentMinute,
                                        false // Usar formato 12 horas
                                    ).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0xFF00BCD4).copy(alpha = 0.1f)
                                )
                            ) {
                                Text(
                                    text = hora.ifEmpty { "Seleccionar" },
                                    color = Negro,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Seleccionar hora",
                                    tint = Negro,
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.Gray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                // Vibración
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Vibración",
                            tint = Negro,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Vibración",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = Negro
                        )
                    }
                    Switch(
                        checked = vibracion,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                // Solicitar permiso de notificaciones si es necesario (Android 13+)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                        // Aquí deberíamos idealmente usar el launcher registrado en la activity, 
                                        // pero como estamos en un dialog, podemos pedir al usuario que lo habilite.
                                        // Una opción rápida es mostrar un toast o intentar pedirlo si tenemos acceso al launcher.
                                        // En este caso, asumimos que se pidió en onCreate, pero si no, avisamos.
                                        android.widget.Toast.makeText(context, "Habilita las notificaciones para usar la vibración", android.widget.Toast.LENGTH_LONG).show()
                                        // (Opcional) Intentar abrir configuración si es crítico
                                    } else {
                                        vibracion = true
                                    }
                                } else {
                                    vibracion = true
                                }
                            } else {
                                vibracion = false
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF00BCD4),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.Gray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                // Sonido
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Sonido",
                            tint = Negro,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Sonido",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = Negro
                        )
                    }
                    Switch(
                        checked = sonido,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                        android.widget.Toast.makeText(context, "Habilita las notificaciones para usar el sonido", android.widget.Toast.LENGTH_LONG).show()
                                    } else {
                                        sonido = true
                                    }
                                } else {
                                    sonido = true
                                }
                            } else {
                                sonido = false
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF00BCD4),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            if (titulo.isNotEmpty()) {
                                onSave(
                                    Tarea(
                                        id = 0, // Will be set by parent
                                        titulo = titulo,
                                        nota = nota,
                                        hora = if (asignarHora) hora else "",
                                        tieneHora = asignarHora && hora.isNotEmpty(),
                                        vibracion = vibracion,
                                        sonido = sonido
                                    )
                                )
                            }
                        },
                        enabled = titulo.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00BCD4),
                            contentColor = Negro
                        )
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
