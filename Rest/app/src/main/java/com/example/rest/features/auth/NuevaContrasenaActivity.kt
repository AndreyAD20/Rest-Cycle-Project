package com.example.rest.features.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import com.example.rest.network.SupabaseAuthClient
import com.example.rest.ui.theme.TemaRest
import io.github.jan.supabase.auth.OtpType
import kotlinx.coroutines.launch

class NuevaContrasenaActivity : BaseComposeActivity() {
    
    private var tokenHash: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Obtener el token_hash del intent
        tokenHash = intent.getStringExtra("token_hash")
        android.util.Log.d("NuevaContrasenaActivity", "Token recibido: $tokenHash")
        
        setContent {
            TemaRest {
                PantallaNuevaContrasena(
                    onPasswordChanged = { nuevaContrasena ->
                        lifecycleScope.launch {
                            try {
                                // Si el SDK de Supabase ya procesó el deep link vía handleDeeplinks,
                                // la sesión ya está establecida y solo necesitamos actualizar la contraseña.
                                // Si se recibió token_hash explícito, verificar el OTP primero.
                                if (!tokenHash.isNullOrBlank()) {
                                    SupabaseAuthClient.auth.verifyEmailOtp(
                                        type = OtpType.Email.RECOVERY,
                                        tokenHash = tokenHash!!
                                    )
                                }
                                // En ambos casos, actualizar la contraseña (la sesión ya existe)
                                SupabaseAuthClient.auth.updateUser {
                                    password = nuevaContrasena
                                }
                                
                                Toast.makeText(
                                    this@NuevaContrasenaActivity,
                                    getString(R.string.toast_password_changed),
                                    Toast.LENGTH_LONG
                                ).show()
                                
                                // Volver al login
                                val intent = android.content.Intent(
                                    this@NuevaContrasenaActivity,
                                    LoginComposeActivity::class.java
                                )
                                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or 
                                              android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                                
                            } catch (e: Exception) {
                                android.util.Log.e("NuevaContrasenaActivity", "Error al cambiar contraseña: ${e.message}", e)
                                Toast.makeText(
                                    this@NuevaContrasenaActivity,
                                    "Error al cambiar contraseña: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun PantallaNuevaContrasena(
    onPasswordChanged: (String) -> Unit,
    onCancel: () -> Unit
) {
    var nuevaContrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var showPassword1 by remember { mutableStateOf(false) }
    var showPassword2 by remember { mutableStateOf(false) }

    val brochaGradiente = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = listOf(
            androidx.compose.ui.graphics.Color(0xFF0D47A1),
            androidx.compose.ui.graphics.Color(0xFF00838F),
            androidx.compose.ui.graphics.Color(0xFF00BFA5)
        ),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(1000f, 2000f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brochaGradiente),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // Logo del búho
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.buho_background),
                contentDescription = null,
                modifier = Modifier
                    .size(110.dp)
                    .padding(bottom = 24.dp)
            )

            // Título
            Text(
                text = "Nueva Contraseña",
                style = MaterialTheme.typography.headlineSmall,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Ingresa tu nueva contraseña",
                style = MaterialTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.75f),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Campo Nueva Contraseña
            OutlinedTextField(
                value = nuevaContrasena,
                onValueChange = { nuevaContrasena = it },
                placeholder = {
                    Text(
                        "Nueva contraseña",
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Lock,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword1 = !showPassword1 }) {
                        Icon(
                            imageVector = if (showPassword1)
                                androidx.compose.material.icons.Icons.Filled.Visibility
                            else
                                androidx.compose.material.icons.Icons.Filled.VisibilityOff,
                            contentDescription = if (showPassword1) "Ocultar" else "Mostrar",
                            tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                visualTransformation = if (showPassword1) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f),
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.15f),
                    focusedBorderColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                    focusedTextColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedTextColor = androidx.compose.ui.graphics.Color.White,
                    focusedLeadingIconColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedLeadingIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                    focusedTrailingIconColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedTrailingIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Confirmar Contraseña
            OutlinedTextField(
                value = confirmarContrasena,
                onValueChange = { confirmarContrasena = it },
                placeholder = {
                    Text(
                        "Confirmar contraseña",
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Lock,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword2 = !showPassword2 }) {
                        Icon(
                            imageVector = if (showPassword2)
                                androidx.compose.material.icons.Icons.Filled.Visibility
                            else
                                androidx.compose.material.icons.Icons.Filled.VisibilityOff,
                            contentDescription = if (showPassword2) "Ocultar" else "Mostrar",
                            tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                visualTransformation = if (showPassword2) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f),
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.15f),
                    focusedBorderColor = if (confirmarContrasena.isNotEmpty() && confirmarContrasena != nuevaContrasena)
                        androidx.compose.ui.graphics.Color(0xFFFF6B6B)
                    else
                        androidx.compose.ui.graphics.Color.White,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                    focusedTextColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedTextColor = androidx.compose.ui.graphics.Color.White,
                    focusedLeadingIconColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedLeadingIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                    focusedTrailingIconColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedTrailingIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                ),
                singleLine = true
            )

            // Error si no coinciden
            if (confirmarContrasena.isNotEmpty() && confirmarContrasena != nuevaContrasena) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Las contraseñas no coinciden",
                    color = androidx.compose.ui.graphics.Color(0xFFFF6B6B),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Guardar
            Button(
                onClick = {
                    if (nuevaContrasena == confirmarContrasena && nuevaContrasena.isNotBlank()) {
                        onPasswordChanged(nuevaContrasena)
                    }
                },
                modifier = Modifier
                    .width(220.dp)
                    .height(52.dp)
                    .border(
                        width = 1.dp,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f),
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    disabledContainerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f),
                    disabledContentColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.4f)
                ),
                enabled = nuevaContrasena.isNotBlank() && nuevaContrasena == confirmarContrasena
            ) {
                Text(
                    text = "Guardar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón Cancelar
            TextButton(onClick = onCancel) {
                Text(
                    text = "Cancelar",
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

