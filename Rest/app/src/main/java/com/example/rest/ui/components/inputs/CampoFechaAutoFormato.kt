package com.example.rest.ui.components.inputs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Campo de texto para ingresar fechas con formato automático YYYY-MM-DD
 * - Solo acepta números
 * - Inserta guiones automáticamente en las posiciones correctas
 * - Máximo 8 dígitos (YYYYMMDD)
 */
@Composable
fun CampoFechaAutoFormato(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Fecha (YYYY-MM-DD)",
    modifier: Modifier = Modifier.fillMaxWidth(),
    shape: Shape = RoundedCornerShape(30.dp),
    focusedContainerColor: Color = Color.White,
    unfocusedContainerColor: Color = Color.White,
    focusedBorderColor: Color = Color(0xFF6B4EFF),
    unfocusedBorderColor: Color = Color(0xFFB0BEC5),
    focusedTextColor: Color = Color.Black,
    unfocusedTextColor: Color = Color.Black,
    placeholderColor: Color = Color(0xFF757575),
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    // Estado de validación
    val errorMessage = remember(value) {
        validarFecha(value)
    }
    val isError = errorMessage != null
    
    // Estado de foco
    var isFocused by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Solo permitir dígitos y limitar a 8
            val digitsOnly = newValue.filter { it.isDigit() }.take(8)
            
            // Validar mes y día en tiempo real
            val validatedValue = validarYLimitarFecha(digitsOnly)
            onValueChange(validatedValue)
        },
        label = null,
        placeholder = { 
            Text(
                text = if (isFocused) "AAAA-MM-DD" else label,
                style = textStyle,
                color = placeholderColor
            ) 
        },
        supportingText = if (isError) {
            { Text(errorMessage!!, color = Color.Red) }
        } else null,
        isError = isError,
        visualTransformation = DateVisualTransformation(),
        modifier = modifier.onFocusChanged { focusState ->
            isFocused = focusState.isFocused
        },
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = focusedContainerColor,
            unfocusedContainerColor = unfocusedContainerColor,
            focusedBorderColor = if (isError) Color.Red else focusedBorderColor,
            unfocusedBorderColor = if (isError) Color.Red else unfocusedBorderColor,
            focusedTextColor = focusedTextColor,
            unfocusedTextColor = unfocusedTextColor,
            errorBorderColor = Color.Red
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        keyboardActions = keyboardActions,
        singleLine = true,
        textStyle = textStyle
    )
}

/**
 * Valida y limita la entrada de fecha en tiempo real
 */
private fun validarYLimitarFecha(digits: String): String {
    if (digits.length <= 4) return digits
    
    // Validar mes (posiciones 4-5)
    if (digits.length >= 5) {
        val mes = digits.substring(4, 5).toIntOrNull() ?: 0
        // Si el primer dígito del mes es > 1, limitar
        if (mes > 1) {
            return digits.substring(0, 4) + "1"
        }
    }
    
    if (digits.length >= 6) {
        val mes = digits.substring(4, 6).toIntOrNull() ?: 0
        // Si el mes es > 12, limitar a 12
        if (mes > 12) {
            return digits.substring(0, 4) + "12" + digits.substring(6).take(2)
        }
        if (mes == 0) {
            return digits.substring(0, 4) + "01" + digits.substring(6).take(2)
        }
    }
    
    // Validar día (posiciones 6-7)
    if (digits.length >= 7) {
        val dia = digits.substring(6, 7).toIntOrNull() ?: 0
        // Si el primer dígito del día es > 3, limitar
        if (dia > 3) {
            return digits.substring(0, 6) + "3"
        }
    }
    
    if (digits.length == 8) {
        val dia = digits.substring(6, 8).toIntOrNull() ?: 0
        // Si el día es > 31, limitar a 31
        if (dia > 31) {
            return digits.substring(0, 6) + "31"
        }
        if (dia == 0) {
            return digits.substring(0, 6) + "01"
        }
    }
    
    return digits
}

/**
 * Valida la fecha completa y retorna mensaje de error si es inválida
 */
private fun validarFecha(digits: String): String? {
    if (digits.length < 8) return null
    
    val year = digits.substring(0, 4).toIntOrNull() ?: return "Año inválido"
    val month = digits.substring(4, 6).toIntOrNull() ?: return "Mes inválido"
    val day = digits.substring(6, 8).toIntOrNull() ?: return "Día inválido"
    
    // Validar rangos básicos
    if (month < 1 || month > 12) return "Mes debe estar entre 01 y 12"
    if (day < 1 || day > 31) return "Día debe estar entre 01 y 31"
    
    // Validar días según el mes
    val diasPorMes = when (month) {
        2 -> if (esBisiesto(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    
    if (day > diasPorMes) {
        return "El mes $month solo tiene $diasPorMes días"
    }
    
    return null
}

/**
 * Verifica si un año es bisiesto
 */
private fun esBisiesto(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}

/**
 * VisualTransformation que formatea dígitos como YYYY-MM-DD
 */
class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitsOnly = text.text
        
        // Formatear según la cantidad de dígitos
        val formatted = when {
            digitsOnly.length <= 4 -> digitsOnly
            digitsOnly.length <= 6 -> "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4)}"
            else -> "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4, 6)}-${digitsOnly.substring(6)}"
        }
        
        return TransformedText(
            AnnotatedString(formatted),
            DateOffsetMapping(digitsOnly.length)
        )
    }
}

/**
 * Mapeo de offset para el cursor
 */
class DateOffsetMapping(private val digitsLength: Int) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        return when {
            offset <= 4 -> offset
            offset <= 6 -> offset + 1  // +1 por el primer guion
            else -> offset + 2  // +2 por ambos guiones
        }.coerceAtMost(getTransformedLength())
    }
    
    override fun transformedToOriginal(offset: Int): Int {
        return when {
            offset <= 4 -> offset
            offset <= 7 -> offset - 1  // -1 por el primer guion
            else -> offset - 2  // -2 por ambos guiones
        }.coerceAtMost(digitsLength)
    }
    
    private fun getTransformedLength(): Int {
        return when {
            digitsLength <= 4 -> digitsLength
            digitsLength <= 6 -> digitsLength + 1
            else -> digitsLength + 2
        }
    }
}
