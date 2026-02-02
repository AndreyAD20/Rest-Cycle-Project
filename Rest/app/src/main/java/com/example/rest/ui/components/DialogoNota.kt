package com.example.rest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.rest.data.models.Nota
import com.example.rest.ui.theme.Negro

@Composable
fun DialogoNota(
    nota: Nota? = null,
    onDismiss: () -> Unit,
    onConfirmar: (String, String, String) -> Unit
) {
    var titulo by remember { mutableStateOf(nota?.titulo ?: "") }
    var contenido by remember { mutableStateOf(nota?.contenido ?: "") }
    var colorSeleccionado by remember { mutableStateOf(nota?.color ?: "#FFFFFF") }
    
    val colores = listOf(
        "#FFFFFF", // Blanco
        "#FFCDD2", // Rojo Pastel
        "#C8E6C9", // Verde Pastel
        "#BBDEFB", // Azul Pastel
        "#FFF9C4", // Amarillo Pastel
        "#E1BEE7"  // Violeta Pastel
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (nota == null) "Nueva Nota" else "Editar Nota") },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    label = { Text("Contenido") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 10
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Elige un color:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Color Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colores.forEach { colorHex ->
                        val color = Color(android.graphics.Color.parseColor(colorHex))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (colorSeleccionado == colorHex) 2.dp else 1.dp,
                                    color = if (colorSeleccionado == colorHex) Negro else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { colorSeleccionado = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (colorSeleccionado == colorHex) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = Negro,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (titulo.isNotBlank() && contenido.isNotBlank()) {
                        onConfirmar(titulo, contenido, colorSeleccionado)
                    }
                }
            ) {
                Text(if (nota == null) "Crear" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
