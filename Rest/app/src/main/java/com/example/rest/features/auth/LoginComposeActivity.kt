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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
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
        val preferencesManager = com.example.rest.utils.PreferencesManager(this)
        val idUsuario = preferencesManager.getUserId()

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
                                Toast.makeText(this, getString(R.string.toast_invalid_email), Toast.LENGTH_SHORT).show()
                            }
                            contraseña.isBlank() -> {
                                Toast.makeText(this, getString(R.string.toast_empty_password), Toast.LENGTH_SHORT).show()
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
                getString(R.string.toast_no_internet),
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
                                getString(R.string.toast_welcome, usuario.nombre),
                                Toast.LENGTH_LONG
                            ).show()
                            
                            // Guardar nombre de usuario e ID usando PreferencesManager (Encriptado)
                            val preferencesManager = com.example.rest.utils.PreferencesManager(this@LoginComposeActivity)
                            preferencesManager.saveUserName(usuario.nombre)
                            preferencesManager.saveUserId(usuario.id ?: -1)

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
                                    getString(R.string.toast_server_error)
                                resultado.message.contains("timeout") -> 
                                    getString(R.string.toast_timeout_error)
                                else -> getString(R.string.toast_unexpected_error, resultado.message)
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
                            getString(R.string.toast_server_error)
                        e.message?.contains("timeout") == true -> 
                            getString(R.string.toast_timeout_error)
                        else -> getString(R.string.toast_unexpected_error, e.message)
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
        
        // --- Selector de Idioma (Superior Derecha) ---
        val context = androidx.compose.ui.platform.LocalContext.current
        val sharedPrefs = remember { context.getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE) }
        var idiomaExpandido by remember { mutableStateOf(false) }
        var idiomaSeleccionado by remember { mutableStateOf(sharedPrefs.getString("IDIOMA", "Español") ?: "Español") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Box {
                IconButton(onClick = { idiomaExpandido = true }) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Cambiar Idioma",
                        tint = Negro,
                        modifier = Modifier.size(32.dp)
                    )
                }

                DropdownMenu(
                    expanded = idiomaExpandido,
                    onDismissRequest = { idiomaExpandido = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    val opciones = listOf(
                        stringResource(R.string.lang_spanish),
                        stringResource(R.string.lang_english),
                        stringResource(R.string.lang_portuguese)
                    )
                    
                    val langEn = stringResource(R.string.lang_english)
                    val langPt = stringResource(R.string.lang_portuguese)
                    
                    opciones.forEach { opcion ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = opcion,
                                    color = if (opcion == idiomaSeleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                ) 
                            },
                            onClick = {
                                idiomaSeleccionado = opcion
                                sharedPrefs.edit().putString("IDIOMA", opcion).apply()
                                
                                val code = when (opcion) {
                                    langEn, "English" -> "en"
                                    langPt, "Português" -> "pt"
                                    else -> "es"
                                }
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
                                android.widget.Toast.makeText(context, context.getString(R.string.toast_language_saved, opcion), android.widget.Toast.LENGTH_SHORT).show()
                                (context as? android.app.Activity)?.recreate()
                                idiomaExpandido = false
                            }
                        )
                    }
                }
            }
        }
        // ----------------------------------------------

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
                onValueChange = { correo = it.trim() },
                placeholder = { 
                    Text(
                        stringResource(R.string.login_email_placeholder),
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
                textStyle = MaterialTheme.typography.bodyLarge,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = Color(0xFF757575)
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Contraseña
            OutlinedTextField(
                value = contraseña,
                onValueChange = { contraseña = it },
                placeholder = { 
                    Text(
                        stringResource(R.string.login_password_placeholder),
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

                    val description = if (passwordVisible) 
                        stringResource(R.string.content_desc_hide_password) 
                    else 
                        stringResource(R.string.content_desc_show_password)

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = Color(0xFF757575)
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ¿Olvido la Contraseña?
            Text(
                text = stringResource(R.string.login_forgot_password),
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
                        text = stringResource(R.string.login_sign_in_button),
                        style = MaterialTheme.typography.labelLarge,
                        color = Negro
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ¿No tienes cuenta?
            Text(
                text = stringResource(R.string.login_no_account_prompt),
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
                    text = stringResource(R.string.login_register_button),
                    style = MaterialTheme.typography.labelLarge,
                    color = Negro
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // O ingresa por
            Text(
                text = stringResource(R.string.login_or_enter_via),
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



