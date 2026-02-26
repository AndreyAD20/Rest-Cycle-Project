package com.example.rest.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
        title = { Text(if (nota == null) "Nueva Nota" else "Editar Nota", color = Color.White) },
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
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF00BCD4),
                        unfocusedBorderColor = Color(0xFFB0BEC5)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    label = { Text("Contenido") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF00BCD4),
                        unfocusedBorderColor = Color(0xFFB0BEC5)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Elige un color:", style = MaterialTheme.typography.bodyMedium, color = Color.White)
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
                                    width = if (colorSeleccionado == colorHex) 3.dp else 1.dp,
                                    color = if (colorSeleccionado == colorHex) Color(0xFF00BCD4) else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { colorSeleccionado = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (colorSeleccionado == colorHex) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = if (colorHex == "#FFFFFF") Color(0xFF00BCD4) else Negro,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isNotBlank() && contenido.isNotBlank()) {
                        onConfirmar(titulo, contenido, colorSeleccionado)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BCD4),
                    contentColor = Color.White
                )
            ) {
                Text(if (nota == null) "Crear" else "Guardar")
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
