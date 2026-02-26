package com.example.rest.ui.components.dialogs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoEvento(
    onDismiss: () -> Unit,
    onConfirmar: (String, String, String, String, Double?, Double?) -> Unit, // Titulo, Tipo, Inicio, Fin, Lat, Long
    initialDate: java.time.LocalDate? = null, // Fecha seleccionada del calendario
    evento: com.example.rest.data.models.Evento? = null, // Evento a editar (null = crear nuevo)
    onEliminar: (() -> Unit)? = null // Callback para eliminar evento
) {
    val esEdicion = evento != null
    
    var titulo by remember { mutableStateOf(evento?.titulo ?: "") }
    var tipoSeleccionado by remember { mutableStateOf(evento?.tipo ?: "Reunión") }
    
    // Ubicación
    var latitud by remember { mutableStateOf<Double?>(evento?.latitud) }
    var longitud by remember { mutableStateOf<Double?>(evento?.longitud) }
    var mostrarMapa by remember { mutableStateOf(false) }

    val tipos = listOf("Reunión", "Trabajo", "Cita", "Salud", "Ocio", "Otro")
    
    // Fechas y Horas - Inicializar con la fecha seleccionada del calendario o del evento
    val calendario = Calendar.getInstance()
    
    if (evento != null) {
        // Modo edición: usar fechas del evento
        try {
            val formatoIso = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            formatoIso.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val fechaInicioEvento = formatoIso.parse(evento.fechaInicio.replace("Z", ""))
            val fechaFinEvento = formatoIso.parse(evento.fechaFin.replace("Z", ""))
            
            val calInicio = Calendar.getInstance()
            calInicio.time = fechaInicioEvento ?: Calendar.getInstance().time
            
            val calFin = Calendar.getInstance()
            calFin.time = fechaFinEvento ?: Calendar.getInstance().time
            
            calendario.time = calInicio.time
        } catch (e: Exception) {
            // Si hay error, usar fecha actual
        }
    } else if (initialDate != null) {
        // Modo creación: usar fecha seleccionada
        calendario.set(initialDate.year, initialDate.monthValue - 1, initialDate.dayOfMonth)
    }
    
    var fechaInicio by remember { mutableStateOf(calendario.clone() as Calendar) }
    var fechaFin by remember { 
        mutableStateOf(
            if (evento != null) {
                try {
                    val formatoIso = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                    formatoIso.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    val fechaFinEvento = formatoIso.parse(evento.fechaFin.replace("Z", ""))
                    val calFin = Calendar.getInstance()
                    calFin.time = fechaFinEvento ?: Calendar.getInstance().time
                    calFin
                } catch (e: Exception) {
                    calendario.clone() as Calendar
                }
            } else {
                calendario.clone() as Calendar
            }
        )
    }
    
    // Validar que fechaFin no sea menor que fechaInicio
    LaunchedEffect(fechaInicio, fechaFin) {
        if (fechaFin.before(fechaInicio)) {
            fechaFin = fechaInicio.clone() as Calendar
        }
    }
    
    val context = LocalContext.current
    
    // Helpers para mostrar texto
    fun formatFecha(cal: Calendar): String = 
        android.text.format.DateFormat.format("dd/MM/yyyy", cal).toString()
    
    fun formatHora(cal: Calendar): String = 
        android.text.format.DateFormat.format("hh:mm a", cal).toString()

    if (mostrarMapa) {
        DialogoMapa(
            onDismiss = { mostrarMapa = false },
            onConfirmar = { lat, long ->
                latitud = lat
                longitud = long
                mostrarMapa = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (esEdicion) "Editar Evento" else "Nuevo Evento", color = Color.White) },
        containerColor = Color(0xFF0097A7), // Azul oscuro que combina con el tema
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 8.dp,
        modifier = Modifier.border(
            width = 2.dp,
            color = Color(0xFF00BCD4), // Borde cian que combina con el tema
            shape = RoundedCornerShape(16.dp)
        ),
        text = {
            Column {
                // TITULO
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título del evento") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF00BCD4),
                        unfocusedBorderColor = Color(0xFFB0BEC5)
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // TIPO (Chips deslizables)
                Text("Tipo de evento:", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(tipos.size) { index ->
                        val tipo = tipos[index]
                        FilterChip(
                            selected = tipo == tipoSeleccionado,
                            onClick = { tipoSeleccionado = tipo },
                            label = { Text(tipo) },
                            leadingIcon = if (tipo == tipoSeleccionado) {
                                { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                selectedContainerColor = Color(0xFF00BCD4),
                                labelColor = Color.Black,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // FECHA Y HORA INICIO
                Text("Inicio:", style = MaterialTheme.typography.labelLarge, color = Color.White)
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(context, { _, y, m, d ->
                                val newCal = fechaInicio.clone() as Calendar
                                newCal.set(y, m, d)
                                fechaInicio = newCal
                            }, fechaInicio.get(Calendar.YEAR), fechaInicio.get(Calendar.MONTH), fechaInicio.get(Calendar.DAY_OF_MONTH)).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(formatFecha(fechaInicio))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(context, { _, h, min ->
                                val newCal = fechaInicio.clone() as Calendar
                                newCal.set(Calendar.HOUR_OF_DAY, h)
                                newCal.set(Calendar.MINUTE, min)
                                fechaInicio = newCal
                            }, fechaInicio.get(Calendar.HOUR_OF_DAY), fechaInicio.get(Calendar.MINUTE), false).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(formatHora(fechaInicio))
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // FECHA Y HORA FIN
                Text("Fin:", style = MaterialTheme.typography.labelLarge, color = Color.White)
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(context, { _, y, m, d ->
                                val newCal = fechaFin.clone() as Calendar
                                newCal.set(y, m, d)
                                fechaFin = newCal
                            }, fechaFin.get(Calendar.YEAR), fechaFin.get(Calendar.MONTH), fechaFin.get(Calendar.DAY_OF_MONTH)).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(formatFecha(fechaFin))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(context, { _, h, min ->
                                val newCal = fechaFin.clone() as Calendar
                                newCal.set(Calendar.HOUR_OF_DAY, h)
                                newCal.set(Calendar.MINUTE, min)
                                fechaFin = newCal
                            }, fechaFin.get(Calendar.HOUR_OF_DAY), fechaFin.get(Calendar.MINUTE), false).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(formatHora(fechaFin))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // UBICACIÓN (Botón Mapa)
                OutlinedButton(
                    onClick = { mostrarMapa = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (latitud != null) Color(0xFF00BCD4) else Color.White,
                        contentColor = if (latitud != null) Color.White else Color.Black
                    )
                ) {
                    Icon(Icons.Default.Place, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (latitud != null) "Ubicación Seleccionada" else "Agregar Ubicación (Mapa)")
                }
            }
        },
        confirmButton = {
            Row {
                // Botón Eliminar (solo en modo edición)
                if (esEdicion && onEliminar != null) {
                    TextButton(
                        onClick = onEliminar,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFFF5252)
                        )
                    ) {
                        Text("Eliminar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                // Botón Guardar/Crear
                Button(
                    onClick = {
                        if (titulo.isNotBlank()) {
                            // Convertir a ISO 8601
                            val formatoIso = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                            formatoIso.timeZone = java.util.TimeZone.getTimeZone("UTC")
                            
                            onConfirmar(
                                titulo,
                                tipoSeleccionado,
                                formatoIso.format(fechaInicio.time),
                                formatoIso.format(fechaFin.time),
                                latitud,
                                longitud
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00BCD4),
                        contentColor = Color.White
                    )
                ) {
                    Text(if (esEdicion) "Guardar" else "Crear")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Cancelar")
            }
        }
    )
}
