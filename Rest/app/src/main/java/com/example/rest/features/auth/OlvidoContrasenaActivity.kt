package com.example.rest.features.auth

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import androidx.lifecycle.lifecycleScope
import com.example.rest.data.repository.RecuperacionRepository
import com.example.rest.ui.theme.*
import kotlinx.coroutines.launch

class OlvidoContrasenaActivity : BaseComposeActivity() {
    
    private val recuperacionRepository = RecuperacionRepository()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            TemaRest {
                var cargando by remember { mutableStateOf(false) }
                
                PantallaOlvidoContrasena(
                    alClickRegresar = {
                        finish()
                    },
                    alClickEnviar = { correo ->
                        when {
                            correo.isBlank() || !correo.contains("@") -> {
                                Toast.makeText(this, "Por favor ingresa un correo válido", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                cargando = true
                                enviarCodigo(correo) {
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
    
    private fun enviarCodigo(correo: String, onComplete: () -> Unit) {
        lifecycleScope.launch {
            try {
                when (val resultado = recuperacionRepository.solicitarCodigo(correo)) {
                    is RecuperacionRepository.Result.Success<*> -> {
                        runOnUiThread {
                            Toast.makeText(
                                this@OlvidoContrasenaActivity,
                                "Código enviado a $correo",
                                Toast.LENGTH_LONG
                            ).show()
                            
                            // Navegar a pantalla de código
                            val intent = Intent(this@OlvidoContrasenaActivity, CodigoRecuperacionActivity::class.java)
                            intent.putExtra("correo", correo)
                            startActivity(intent)
                            finish()
                        }
                    }
                    is RecuperacionRepository.Result.Error -> {
                        runOnUiThread {
                            Toast.makeText(
                                this@OlvidoContrasenaActivity,
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
                        this@OlvidoContrasenaActivity,
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
fun PantallaOlvidoContrasena(
    alClickRegresar: () -> Unit,
    alClickEnviar: (String) -> Unit,
    cargando: Boolean = false
) {
    var correo by remember { mutableStateOf("") }

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
                            text = "Pon tu correo y te\nenviaremos un codigo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Negro,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Campo de Correo
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                placeholder = {
                    Text(
                        "Correo Electrónico",
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Enviar Código
            Button(
                onClick = { alClickEnviar(correo) },
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primario
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.buho_background),
                            contentDescription = "Enviar",
                            tint = Blanco,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enviar Codigo de Recuperacion",
                            style = MaterialTheme.typography.labelLarge,
                            color = Blanco,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
