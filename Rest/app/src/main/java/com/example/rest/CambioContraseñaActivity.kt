package com.example.rest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.rest.data.repository.RecuperacionRepository
import com.example.rest.ui.theme.*
import kotlinx.coroutines.launch

class CambioContraseñaActivity : BaseComposeActivity() {
    
    private val recuperacionRepository = RecuperacionRepository()
    private var correo: String = ""
    private var codigoId: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Obtener datos del intent
        correo = intent.getStringExtra("correo") ?: ""
        codigoId = intent.getIntExtra("codigoId", 0)
        
        setContent {
            TemaRest {
                var cargando by remember { mutableStateOf(false) }
                
                PantallaCambioContraseña(
                    alClickRegresar = {
                        finish()
                    },
                    alClickConfirmar = { nuevaContraseña, confirmarContraseña ->
                        when {
                            nuevaContraseña.isBlank() || nuevaContraseña.length < 6 -> {
                                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                            }
                            nuevaContraseña != confirmarContraseña -> {
                                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                cargando = true
                                cambiarContraseña(nuevaContraseña) {
                                    cargando = false
                                }
                            }
                        }
                    },
                    cargando = cargando
                )
            }
        }
    }
    
    private fun cambiarContraseña(nuevaContraseña: String, onComplete: () -> Unit) {
        lifecycleScope.launch {
            try {
                when (val resultado = recuperacionRepository.cambiarContraseña(correo, codigoId, nuevaContraseña)) {
                    is RecuperacionRepository.Result.Success -> {
                        runOnUiThread {
                            Toast.makeText(
                                this@CambioContraseñaActivity,
                                "¡Contraseña cambiada exitosamente!",
                                Toast.LENGTH_LONG
                            ).show()
                            
                            // Navegar a LoginActivity
                            val intent = Intent(this@CambioContraseñaActivity, LoginComposeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                    is RecuperacionRepository.Result.Error -> {
                        runOnUiThread {
                            Toast.makeText(
                                this@CambioContraseñaActivity,
                                resultado.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    is RecuperacionRepository.Result.Loading -> {
                        // Ya manejado
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@CambioContraseñaActivity,
                        "Error inesperado: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                onComplete()
            }
        }
    }
}

@Composable
fun PantallaCambioContraseña(
    alClickRegresar: () -> Unit,
    alClickConfirmar: (String, String) -> Unit,
    cargando: Boolean = false
) {
    var nuevaContraseña by remember { mutableStateOf("") }
    var confirmarContraseña by remember { mutableStateOf("") }

    val brochaGradiente = Brush.linearGradient(
        colors = listOf(
            Primario,
            Color(0xFF80DEEA)
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brochaGradiente)
    ) {
        // Botón de regresar
        IconButton(
            onClick = alClickRegresar,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Regresar",
                tint = Color(0xFF004D40),
                modifier = Modifier.size(32.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            // Logo del búho con bocadillo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.buho_background),
                    contentDescription = "Logo Búho",
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Blanco,
                    modifier = Modifier
                        .width(200.dp)
                        .height(80.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Cambia a tu nuevo Pin\nque no se te olvide",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Negro,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Campo Nuevo Pin
            OutlinedTextField(
                value = nuevaContraseña,
                onValueChange = { nuevaContraseña = it },
                placeholder = {
                    Text(
                        "Nuevo Pin",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF757575)
                    )
                },
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco,
                    focusedBorderColor = Color(0xFF6B4EFF),
                    unfocusedBorderColor = Color(0xFFB0BEC5),
                    focusedTextColor = Negro,
                    unfocusedTextColor = Negro
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Confirmar Pin
            OutlinedTextField(
                value = confirmarContraseña,
                onValueChange = { confirmarContraseña = it },
                placeholder = {
                    Text(
                        "Confirmar Pin",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF757575)
                    )
                },
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco,
                    focusedBorderColor = Color(0xFF6B4EFF),
                    unfocusedBorderColor = Color(0xFFB0BEC5),
                    focusedTextColor = Negro,
                    unfocusedTextColor = Negro
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Confirmar
            Button(
                onClick = { alClickConfirmar(nuevaContraseña, confirmarContraseña) },
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF004D40)
                ),
                enabled = !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Blanco,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Confirmar",
                        style = MaterialTheme.typography.labelLarge,
                        color = Blanco
                    )
                }
            }
        }
    }
}
