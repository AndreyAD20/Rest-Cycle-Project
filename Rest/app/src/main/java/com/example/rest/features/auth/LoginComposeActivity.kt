package com.example.rest.features.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import com.example.rest.data.repository.UsuarioRepository
import com.example.rest.features.home.InicioComposeActivity
import com.example.rest.ui.theme.*
import kotlinx.coroutines.launch

class LoginComposeActivity : BaseComposeActivity() {
    
    private val usuarioRepository = UsuarioRepository()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar si ya hay una sesión activa
        val sharedPref = getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE)
        val idUsuario = sharedPref.getInt("ID_USUARIO", -1)

        if (idUsuario != -1) {
            // Ya existe una sesión, ir directo al inicio
            val intent = Intent(this, InicioComposeActivity::class.java)
            startActivity(intent)
            finish()
            return // Importante para no cargar la UI de login
        }

        setContent {
            TemaRest {
                var cargando by remember { mutableStateOf(false) }
                
                PantallaLogin(
                    alClickIniciarSesion = { correo, contraseña ->
                        // Validaciones
                        when {
                            correo.isBlank() || !correo.contains("@") -> {
                                Toast.makeText(this, "Por favor ingresa un correo válido", Toast.LENGTH_SHORT).show()
                            }
                            contraseña.isBlank() -> {
                                Toast.makeText(this, "Por favor ingresa tu contraseña", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                // Realizar login
                                cargando = true
                                realizarLogin(correo, contraseña) {
                                    cargando = false
                                }
                            }
                        }
                    },
                    alClickRegistro = {
                        // Navegar a RegistroComposeActivity
                        val intencion = Intent(this, RegistroComposeActivity::class.java)
                        startActivity(intencion)
                    },
                    alClickOlvidoContraseña = {
                        // Navegar a OlvidoContrasenaActivity
                        val intencion = Intent(this, OlvidoContrasenaActivity::class.java)
                        startActivity(intencion)
                    },
                    cargando = cargando
                )
            }
        }
    }
    
    private fun realizarLogin(
        correo: String,
        contraseña: String,
        onComplete: () -> Unit
    ) {
        // Verificar conectividad antes de intentar el login
        if (!isNetworkAvailable()) {
            Toast.makeText(
                this@LoginComposeActivity,
                "❌ No hay conexión a Internet. Por favor, verifica tu conexión e intenta nuevamente.",
                Toast.LENGTH_LONG
            ).show()
            onComplete()
            return
        }
        
        lifecycleScope.launch {
            try {
                when (val resultado = usuarioRepository.login(correo, contraseña)) {
                    is UsuarioRepository.Result.Success -> {
                        val usuario = resultado.data
                        runOnUiThread {
                            Toast.makeText(
                                this@LoginComposeActivity,
                                "✅ ¡Bienvenido ${usuario.nombre}!",
                                Toast.LENGTH_LONG
                            ).show()
                            
                            // Guardar nombre de usuario e ID en SharedPreferences
                            val sharedPref = getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE)
                            with (sharedPref.edit()) {
                                putString("NOMBRE_USUARIO", usuario.nombre)
                                putInt("ID_USUARIO", usuario.id ?: -1)
                                apply()
                            }

                            // Navegar a InicioComposeActivity
                            val intencion = Intent(this@LoginComposeActivity, InicioComposeActivity::class.java)
                            startActivity(intencion)
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
                                this@LoginComposeActivity,
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
                        this@LoginComposeActivity,
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
}

@Composable
fun PantallaLogin(
    alClickIniciarSesion: (String, String) -> Unit,
    alClickRegistro: () -> Unit,
    alClickOlvidoContraseña: () -> Unit,
    cargando: Boolean = false
) {
    var correo by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Gradiente de fondo cyan/turquesa como en la imagen
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
            Image(
                painter = painterResource(id = R.drawable.buho_background),
                contentDescription = "Logo Búho",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 40.dp)
            )

            // Campo de Correo Electrónico
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                placeholder = { 
                    Text(
                        "Correo Electronico",
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

            // Campo de Contraseña
            OutlinedTextField(
                value = contraseña,
                onValueChange = { contraseña = it },
                placeholder = { 
                    Text(
                        "Ingrese su Contraseña",
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
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else
                        Icons.Filled.VisibilityOff

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ¿Olvido la Contraseña?
            Text(
                text = "¿Olvido la Contraseña?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF004D40),
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    alClickOlvidoContraseña()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Iniciar Sesión
            Button(
                onClick = {
                    alClickIniciarSesion(correo, contraseña)
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
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.labelLarge,
                        color = Negro
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ¿No tienes cuenta?
            Text(
                text = "¿No tienes cuenta?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF004D40),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Registrate
            Button(
                onClick = alClickRegistro,
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
                )
            ) {
                Text(
                    text = "Registrate",
                    style = MaterialTheme.typography.labelLarge,
                    color = Negro
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // O ingresa por
            Text(
                text = "O ingresa por",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF004D40),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Iconos de redes sociales
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.face_background),
                    contentDescription = "Facebook",
                    modifier = Modifier.size(50.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.gmail_background),
                    contentDescription = "Gmail",
                    modifier = Modifier.size(50.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.icloud_background),
                    contentDescription = "iCloud",
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}



