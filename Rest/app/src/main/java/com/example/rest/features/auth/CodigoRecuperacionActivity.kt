package com.example.rest.features.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

class CodigoRecuperacionActivity : BaseComposeActivity() {
    
    private val recuperacionRepository = RecuperacionRepository()
    private var correo: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Obtener correo del intent
        correo = intent.getStringExtra("correo") ?: ""
        
        setContent {
            TemaRest {
                var cargando by remember { mutableStateOf(false) }
                
                PantallaCodigoRecuperacion(
                    alClickRegresar = {
                        finish()
                    },
                    alClickConfirmar = { codigo ->
                        when {
                            codigo.isBlank() || codigo.length != 6 -> {
                                Toast.makeText(this, "Por favor ingresa un cÃ³digo de 6 dÃ­gitos", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                cargando = true
                                verificarCodigo(codigo) {
                                    cargando = false
                                }
                            }
                        }
                    },
                    alClickReenviar = {
                        // Volver a la pantalla anterior para reenviar cÃ³digo
                        finish()
                    },
                    cargando = cargando
                )
            }
        }
    }
    
    private fun verificarCodigo(codigo: String, onComplete: () -> Unit) {
        lifecycleScope.launch {
            try {
                when (val resultado = recuperacionRepository.verificarCodigo(correo, codigo)) {
                    is RecuperacionRepository.Result.Success -> {
                        val codigoId = resultado.data
                        runOnUiThread {
                            Toast.makeText(
                                this@CodigoRecuperacionActivity,
                                "CÃ³digo verificado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            
                            // Navegar a pantalla de cambio de contraseÃ±a
                            val intent = Intent(this@CodigoRecuperacionActivity, CambioContrasenaActivity::class.java)
                            intent.putExtra("correo", correo)
                            intent.putExtra("codigoId", codigoId)
                            startActivity(intent)
                            finish()
                        }
                    }
                    is RecuperacionRepository.Result.Error -> {
                        runOnUiThread {
                            Toast.makeText(
                                this@CodigoRecuperacionActivity,
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
                        this@CodigoRecuperacionActivity,
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
fun PantallaCodigoRecuperacion(
    alClickRegresar: () -> Unit,
    alClickConfirmar: (String) -> Unit,
    alClickReenviar: () -> Unit,
    cargando: Boolean = false
) {
    var codigo by remember { mutableStateOf("") }

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
        // BotÃ³n de regresar
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
            // Logo del bÃºho con bocadillo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.buho_background),
                    contentDescription = "Logo BÃºho",
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
                            text = "Pon tu codigo\nde recuperacion",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Negro,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Campo de CÃ³digo de VerificaciÃ³n
            OutlinedTextField(
                value = codigo,
                onValueChange = { if (it.length <= 6) codigo = it },
                placeholder = {
                    Text(
                        "Codigo de VerificaciÃ³n",
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            // BotÃ³n Confirmar
            Button(
                onClick = { alClickConfirmar(codigo) },
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

            Spacer(modifier = Modifier.height(24.dp))

            // Link para reenviar cÃ³digo
            Text(
                text = "Volver a Enviar Codigo",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF004D40),
                modifier = Modifier.clickable { alClickReenviar() }
            )
        }
    }
}




