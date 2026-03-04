package com.example.rest.features.home

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.rest.BaseComposeActivity
import com.example.rest.ui.theme.*
import com.example.rest.utils.SecurityUtils
import kotlinx.coroutines.launch

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

class CambiarContrasenaComposeActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode = com.example.rest.utils.ThemeManager.isDarkMode(this)
            TemaRest(temaOscuro = isDarkMode) {
                PantallaCambiarContrasena(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCambiarContrasena(onBackClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var actualPwd by remember { mutableStateOf("") }
    var nuevaPwd by remember { mutableStateOf("") }
    var confirmarPwd by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    val usuarioRepository = remember { com.example.rest.data.repository.UsuarioRepository() }
    val prefs = com.example.rest.utils.PreferencesManager(context)
    val userId = prefs.getUserId()

    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Primario, Color(0xFF80DEEA)),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cambiar Contraseña", color = Negro, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Regresar", tint = Negro)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brochaGradiente)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = actualPwd,
                    onValueChange = { actualPwd = it },
                    label = { Text("Contraseña Actual") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Blanco.copy(alpha = 0.9f),
                        unfocusedContainerColor = Blanco.copy(alpha = 0.9f),
                        focusedBorderColor = Primario,
                        unfocusedBorderColor = Primario.copy(alpha = 0.5f)
                    )
                )
                OutlinedTextField(
                    value = nuevaPwd,
                    onValueChange = { nuevaPwd = it },
                    label = { Text("Nueva Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Blanco.copy(alpha = 0.9f),
                        unfocusedContainerColor = Blanco.copy(alpha = 0.9f),
                        focusedBorderColor = Primario,
                        unfocusedBorderColor = Primario.copy(alpha = 0.5f)
                    )
                )
                OutlinedTextField(
                    value = confirmarPwd,
                    onValueChange = { confirmarPwd = it },
                    label = { Text("Confirmar Nueva Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Blanco.copy(alpha = 0.9f),
                        unfocusedContainerColor = Blanco.copy(alpha = 0.9f),
                        focusedBorderColor = Primario,
                        unfocusedBorderColor = Primario.copy(alpha = 0.5f)
                    )
                )
                Button(
                    onClick = {
                        if (actualPwd.isBlank() || nuevaPwd.isBlank() || confirmarPwd.isBlank()) {
                            Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (nuevaPwd != confirmarPwd) {
                            Toast.makeText(context, "Las nuevas contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        scope.launch {
                            try {
                                // Verificar contraseña actual
                                val userRes = usuarioRepository.obtenerUsuarioPorId(userId)
                                if (userRes is com.example.rest.data.repository.UsuarioRepository.Result.Success) {
                                    val user = userRes.data
                                    val hashedActual = SecurityUtils.hashPassword(actualPwd)
                                    // Comprobar si la contraseña coincide (si es bcrypt hay que usar verify, depende de SecurityUtils)
                                    val isValid = SecurityUtils.verifyPassword(actualPwd, user.contraseña)
                                    
                                    if (!isValid) {
                                        Toast.makeText(context, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }
                                    
                                    // Validar complejidad de nueva contraseña
                                    val passwordRegex = "^(?=.*[A-Z])(?=.*\\d).{8,}\$".toRegex()
                                    if (!nuevaPwd.matches(passwordRegex)) {
                                        Toast.makeText(context, "La clave debe tener al menos 8 caracteres, 1 mayúscula y 1 número.", Toast.LENGTH_LONG).show()
                                        return@launch
                                    }

                                    // Actualizar con nueva contraseña
                                    val nuevaHash = SecurityUtils.hashPassword(nuevaPwd)
                                    val updated = user.copy(contraseña = nuevaHash)
                                    when (val updRes = usuarioRepository.actualizarUsuario(userId, updated)) {
                                        is com.example.rest.data.repository.UsuarioRepository.Result.Success -> {
                                            Toast.makeText(context, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                                            onBackClick() // finaliza
                                        }
                                        is com.example.rest.data.repository.UsuarioRepository.Result.Error -> {
                                            Toast.makeText(context, "Error: ${updRes.message}", Toast.LENGTH_SHORT).show()
                                        }
                                        else -> {}
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = Primario),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Blanco, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Cambiar", color = Blanco, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
