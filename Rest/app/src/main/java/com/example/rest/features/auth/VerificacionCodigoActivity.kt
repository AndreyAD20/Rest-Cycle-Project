package com.example.rest.features.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.lifecycleScope
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import com.example.rest.data.repository.UsuarioRepository
import com.example.rest.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Actividad para verificar el código de verificación enviado por email
 */
class VerificacionCodigoActivity : BaseComposeActivity() {
    
    private val usuarioRepository = UsuarioRepository()
    private lateinit var correo: String
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Obtener correo del intent
        correo = intent.getStringExtra("correo") ?: run {
            Toast.makeText(this, "Error: No se proporcionó el correo", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        setContent {
            TemaRest {
                var cargando by remember { mutableStateOf(false) }
                
                PantallaVerificacionCodigo(
                    correo = correo,
                    alClickVerificar = { codigo ->
                        cargando = true
                        verificarCodigo(codigo) {
                            cargando = false
                        }
                    },
                    alClickReenviar = {
                        cargando = true
                        reenviarCodigo() {
                            cargando = false
                        }
                    },
                    cargando = cargando
                )
            }
        }
    }
    
    private fun verificarCodigo(codigo: String, onComplete: () -> Unit) {
        lifecycleScope.launch {
            try {
                when (val resultado = usuarioRepository.verificarCodigo(correo, codigo)) {
                    is UsuarioRepository.Result.Success -> {
                        runOnUiThread {
                            Toast.makeText(
                                this@VerificacionCodigoActivity,
                                "✅ ¡Email verificado exitosamente!",
                                Toast.LENGTH_LONG
                            ).show()
                            
                            // Navegar según el flujo
                            val retornarAPadre = intent.getBooleanExtra("retornarAPadre", false)
                            if (retornarAPadre) {
                                Toast.makeText(
                                    this@VerificacionCodigoActivity,
                                    "✅ ¡Cuenta de hijo verificada y vinculada!",
                                    Toast.LENGTH_LONG
                                ).show()
                                finish() // Vuelve a la actividad anterior (GestionHijosComposeActivity)
                            } else {
                                val intent = Intent(this@VerificacionCodigoActivity, LoginComposeActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                    is UsuarioRepository.Result.Error -> {
                        runOnUiThread {
                            Toast.makeText(
                                this@VerificacionCodigoActivity,
                                "❌ ${resultado.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    is UsuarioRepository.Result.Loading -> {
                        // Ya está manejado por el estado cargando
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@VerificacionCodigoActivity,
                        "❌ Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                onComplete()
            }
        }
    }
    
    private fun reenviarCodigo(onComplete: () -> Unit) {
        lifecycleScope.launch {
            try {
                when (val resultado = usuarioRepository.reenviarCodigo(correo)) {
                    is UsuarioRepository.Result.Success -> {
                        runOnUiThread {
                            Toast.makeText(
                                this@VerificacionCodigoActivity,
                                "✅ ${resultado.data}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    is UsuarioRepository.Result.Error -> {
                        runOnUiThread {
                            Toast.makeText(
                                this@VerificacionCodigoActivity,
                                "❌ ${resultado.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    is UsuarioRepository.Result.Loading -> {
                        // Ya está manejado por el estado cargando
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@VerificacionCodigoActivity,
                        "❌ Error: ${e.message}",
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
fun PantallaVerificacionCodigo(
    correo: String,
    alClickVerificar: (String) -> Unit,
    alClickReenviar: () -> Unit,
    cargando: Boolean = false
) {
    var codigo by remember { mutableStateOf("") }
    
    // Gradiente de fondo cyan/turquesa
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Búho con mensaje
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.buho_background),
                    contentDescription = "Búho",
                    modifier = Modifier.size(80.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Frase en un "bocadillo"
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Blanco,
                    modifier = Modifier
                        .border(
                            width = 2.dp,
                            color = Negro,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Text(
                        text = "Verifica tu\ncorreo electrónico",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Negro,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            // Título
            Text(
                text = "Verificación de Email",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF004D40),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Mensaje informativo
            Text(
                text = "Hemos enviado un código de 6 dígitos a:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF004D40),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = correo,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF6B4EFF),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Campo de código
            OutlinedTextField(
                value = codigo,
                onValueChange = { 
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        codigo = it
                    }
                },
                placeholder = { 
                    Text(
                        "Código de 6 dígitos",
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
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    textAlign = TextAlign.Center,
                    letterSpacing = 8.sp
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Botón Verificar
            Button(
                onClick = {
                    when {
                        codigo.isBlank() -> {
                            // El Toast se mostrará desde la Activity
                        }
                        codigo.length != 6 -> {
                            // El Toast se mostrará desde la Activity
                        }
                        else -> {
                            alClickVerificar(codigo)
                        }
                    }
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp)
                    .border(
                        width = 2.dp,
                        color = Negro,
                        shape = RoundedCornerShape(30.dp)
                    ),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primario
                ),
                enabled = !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Negro,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Verificar",
                        style = MaterialTheme.typography.labelLarge,
                        color = Negro
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón Reenviar código
            TextButton(
                onClick = alClickReenviar,
                enabled = !cargando
            ) {
                Text(
                    text = "¿No recibiste el código? Reenviar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B4EFF)
                )
            }
        }
    }
}
