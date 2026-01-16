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
import androidx.lifecycle.lifecycleScope
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import com.example.rest.data.models.RegistroRequest
import com.example.rest.data.repository.UsuarioRepository
import com.example.rest.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class RegistroComposeActivity : BaseComposeActivity() {
    
    private val usuarioRepository = UsuarioRepository()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                var cargando by remember { mutableStateOf(false) }
                
                PantallaRegistro(
                    alClickRegistrar = { request ->
                        cargando = true
                        realizarRegistro(request) {
                            cargando = false
                        }
                    },
                    alClickYaTienesCuenta = {
                        finish() // Volver a LoginComposeActivity
                    },
                    alMostrarError = { mensaje ->
                        mostrarMensaje(mensaje)
                    },
                    cargando = cargando
                )
            }
        }
    }
    
    private fun realizarRegistro(
        request: RegistroRequest,
        onComplete: () -> Unit
    ) {
        // Verificar conectividad antes de intentar el registro
        if (!isNetworkAvailable()) {
            Toast.makeText(
                this@RegistroComposeActivity,
                "❌ No hay conexión a Internet. Por favor, verifica tu conexión e intenta nuevamente.",
                Toast.LENGTH_LONG
            ).show()
            onComplete()
            return
        }
        
        lifecycleScope.launch {
            try {
                when (val resultado = usuarioRepository.registrarConVerificacion(request)) {
                    is UsuarioRepository.Result.Success -> {
                        val mensaje = resultado.data
                        runOnUiThread {
                            Toast.makeText(
                                this@RegistroComposeActivity,
                                "✅ $mensaje",
                                Toast.LENGTH_LONG
                            ).show()
                            
                            // Navegar a pantalla de verificación
                            val intent = Intent(this@RegistroComposeActivity, VerificacionCodigoActivity::class.java)
                            intent.putExtra("correo", request.correo)
                            startActivity(intent)
                            finish()
                        }
                    }
                    is UsuarioRepository.Result.Error -> {
                        runOnUiThread {
                            val mensajeError = when {
                                resultado.message.contains("Unable to resolve host") ||
                                resultado.message.contains("UnknownHostException") -> 
                                    "❌ No se puede conectar al servidor. Verifica tu conexión a Internet."
                                resultado.message.contains("timeout") -> 
                                    "❌ La conexión tardó demasiado. Intenta nuevamente."
                                else -> "❌ ${resultado.message}"
                            }
                            Toast.makeText(
                                this@RegistroComposeActivity,
                                mensajeError,
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
                    val mensajeError = when {
                        e.message?.contains("Unable to resolve host") == true ||
                        e.message?.contains("UnknownHostException") == true -> 
                            "❌ No se puede conectar al servidor. Verifica tu conexión a Internet."
                        e.message?.contains("timeout") == true -> 
                            "❌ La conexión tardó demasiado. Intenta nuevamente."
                        else -> "❌ Error inesperado: ${e.message}"
                    }
                    Toast.makeText(
                        this@RegistroComposeActivity,
                        mensajeError,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                onComplete()
            }
        }
    }
    
    /**
     * Verifica si hay conexión a Internet disponible
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) 
            as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    /**
     * Muestra un mensaje Toast
     */
    fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}

@Composable
fun PantallaRegistro(
    alClickRegistrar: (RegistroRequest) -> Unit,
    alClickYaTienesCuenta: () -> Unit,
    alMostrarError: (String) -> Unit,
    cargando: Boolean = false
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    // Telefono eliminado
    var fechaNacimiento by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirmarPin by remember { mutableStateOf("") }
    var aceptaTerminos by remember { mutableStateOf(false) }
    var rol by remember { mutableStateOf("hijo") } // Por defecto "hijo"

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
        // Botón de volver (flecha) en la esquina superior izquierda
        IconButton(
            onClick = alClickYaTienesCuenta,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color(0xFF004D40),
                modifier = Modifier.size(32.dp)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(top = 60.dp, bottom = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Búho con frase
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
                        text = "Aquí podrás\nRegistrarte en\nnuestra APP",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Negro,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Campo de Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = { 
                    Text(
                        "Nombre",
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
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Apellido
            OutlinedTextField(
                value = apellido,
                onValueChange = { apellido = it },
                placeholder = { 
                    Text(
                        "Apellido",
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
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Correo Electrónico
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

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Fecha de Nacimiento
            OutlinedTextField(
                value = fechaNacimiento,
                onValueChange = { fechaNacimiento = it },
                placeholder = { 
                    Text(
                        "Fecha de Nacimiento (YYYY-MM-DD)",
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
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Pin
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                placeholder = { 
                    Text(
                        "Contraseña",
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

            // Campo de Confirmar Pin
            OutlinedTextField(
                value = confirmarPin,
                onValueChange = { confirmarPin = it },
                placeholder = { 
                    Text(
                        "Confirmar Contraseña",
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
            
            // Checkbox de Términos y Condiciones
            Row(
                modifier = Modifier
                    .width(330.dp)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = aceptaTerminos,
                    onCheckedChange = { aceptaTerminos = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF6B4EFF),
                        uncheckedColor = Color(0xFFB0BEC5)
                    )
                )
                Text(
                    text = "Acepto los Términos y Condiciones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF004D40),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón Registrar
            Button(
                onClick = {
                    // Validaciones con mensajes
                    when {
                        nombre.isBlank() -> {
                            alMostrarError("❌ Por favor, ingresa tu nombre")
                        }
                        correo.isBlank() -> {
                            alMostrarError("❌ Por favor, ingresa tu correo electrónico")
                        }
                        !correo.contains("@") -> {
                            alMostrarError("❌ Por favor, ingresa un correo válido")
                        }
                        // Telefono validacion eliminada
                        fechaNacimiento.isBlank() -> {
                            alMostrarError("❌ Por favor, ingresa tu fecha de nacimiento")
                        }
                        pin.isBlank() -> {
                            alMostrarError("❌ Por favor, ingresa tu contraseña")
                        }
                        pin.length < 4 -> {
                            alMostrarError("❌ La contraseña debe tener al menos 4 caracteres")
                        }
                        confirmarPin.isBlank() -> {
                            alMostrarError("❌ Por favor, confirma tu contraseña")
                        }
                        pin != confirmarPin -> {
                            alMostrarError("❌ Las contraseñas no coinciden")
                        }
                        !aceptaTerminos -> {
                            alMostrarError("❌ Debes aceptar los Términos y Condiciones")
                        }
                        else -> {
                            // Validar formato de fecha
                            val mayorEdad = try {
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                val fechaNac = LocalDate.parse(fechaNacimiento, formatter)
                                val edad = Period.between(fechaNac, LocalDate.now()).years
                                edad >= 18
                            } catch (e: Exception) {
                                alMostrarError("❌ Formato de fecha inválido. Usa YYYY-MM-DD (ej: 2000-01-15)")
                                return@Button
                            }

                            val request = RegistroRequest(
                                nombre = nombre,
                                apellido = apellido.ifBlank { null },
                                correo = correo,
                                telefono = null,
                                fechaNacimiento = fechaNacimiento,
                                contraseña = pin,
                                rol = rol,
                                mayorEdad = mayorEdad
                            )
                            alClickRegistrar(request)
                        }
                    }
                },
                modifier = Modifier
                    .width(158.dp)
                    .height(48.dp)
                    .border(
                        width = 2.dp,
                        color = Negro,
                        shape = RoundedCornerShape(8.dp)
                    ),
                shape = RoundedCornerShape(8.dp),
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
                        text = "Registrar",
                        style = MaterialTheme.typography.labelLarge,
                        color = Negro
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
