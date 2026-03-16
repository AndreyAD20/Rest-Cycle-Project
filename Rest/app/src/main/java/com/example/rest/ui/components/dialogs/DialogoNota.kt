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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.rest.R
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
        title = {
            Text(
                if (nota == null) stringResource(R.string.dialog_note_title_new) else stringResource(R.string.dialog_note_title_edit),
                color = Negro,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            )
        },
        containerColor = Color(0xFFFAFAFA), // Fondo claro
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 8.dp,
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text(stringResource(R.string.dialog_note_label_title)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Negro,
                        unfocusedTextColor = Negro,
                        focusedBorderColor = Color(0xFF00BCD4),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    label = { Text(stringResource(R.string.dialog_note_label_content)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    maxLines = 10,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Negro,
                        unfocusedTextColor = Negro,
                        focusedBorderColor = Color(0xFF00BCD4),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    stringResource(R.string.dialog_note_label_color),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                    color = Negro.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
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
                                    contentDescription = stringResource(R.string.dialog_note_desc_selected),
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
                    if (titulo.isNotBlank() || contenido.isNotBlank()) {
                        val finalTitulo = if (titulo.isBlank() && contenido.isNotBlank()) "Sin Título" else titulo
                        onConfirmar(finalTitulo, contenido, colorSeleccionado)
                    }
                },
                modifier = Modifier.padding(bottom = 8.dp, end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BCD4),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (nota == null) stringResource(R.string.btn_create) else stringResource(R.string.btn_save),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_cancel),
                    color = Color(0xFF757575),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            }
        }
    )
}
