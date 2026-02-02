package com.example.rest.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
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
    onConfirmar: (String, String, String, String, Double?, Double?) -> Unit // Titulo, Tipo, Inicio, Fin, Lat, Long
) {
    var titulo by remember { mutableStateOf("") }
    var tipoSeleccionado by remember { mutableStateOf("Reunión") }
    
    // Ubicación
    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var mostrarMapa by remember { mutableStateOf(false) }

    val tipos = listOf("Reunión", "Trabajo", "Cita", "Salud", "Ocio", "Otro")
    
    // Fechas y Horas (usamos Calendar para manejar selección)
    val calendario = Calendar.getInstance()
    var fechaInicio by remember { mutableStateOf(calendario) }
    var fechaFin by remember { mutableStateOf(calendario.clone() as Calendar) }
    
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
        title = { Text("Nuevo Evento") },
        text = {
            Column {
                // TITULO
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título del evento") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // TIPO (Chips deslizables)
                Text("Tipo de evento:", style = MaterialTheme.typography.bodyMedium)
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
                            } else null
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // FECHA Y HORA INICIO
                Text("Inicio:", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(context, { _, y, m, d ->
                                val newCal = fechaInicio.clone() as Calendar
                                newCal.set(y, m, d)
                                fechaInicio = newCal
                            }, fechaInicio.get(Calendar.YEAR), fechaInicio.get(Calendar.MONTH), fechaInicio.get(Calendar.DAY_OF_MONTH)).show()
                        },
                        modifier = Modifier.weight(1f)
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(formatHora(fechaInicio))
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // FECHA Y HORA FIN
                Text("Fin:", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(context, { _, y, m, d ->
                                val newCal = fechaFin.clone() as Calendar
                                newCal.set(y, m, d)
                                fechaFin = newCal
                            }, fechaFin.get(Calendar.YEAR), fechaFin.get(Calendar.MONTH), fechaFin.get(Calendar.DAY_OF_MONTH)).show()
                        },
                        modifier = Modifier.weight(1f)
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
                        modifier = Modifier.weight(1f)
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
                        containerColor = if (latitud != null) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    )
                ) {
                    Icon(Icons.Default.Place, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (latitud != null) "Ubicación Seleccionada" else "Agregar Ubicación (Mapa)")
                }
            }
        },
        confirmButton = {
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
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
