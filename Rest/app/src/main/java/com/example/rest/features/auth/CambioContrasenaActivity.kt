package com.example.rest.features.auth

import android.content.Intent
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.activity.compose.BackHandler
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.Language
import androidx.compose.ui.res.stringResource
import com.example.rest.data.repository.RecuperacionRepository
import com.example.rest.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CambioContrasenaActivity : BaseComposeActivity() {
    
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
                var mostrarDialogoSalir by remember { mutableStateOf(false) }
                
                if (mostrarDialogoSalir) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogoSalir = false },
                        title = { Text(stringResource(R.string.dialog_cancel_password_change_title)) },
                        text = { Text(stringResource(R.string.dialog_cancel_password_change_text)) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    mostrarDialogoSalir = false
                                    finish()
                                }
                            ) {
                                Text(stringResource(R.string.btn_yes_exit))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarDialogoSalir = false }) {
                                Text(stringResource(R.string.btn_cancel))
                            }
                        }
                    )
                }
                
                PantallaCambioContrasena(
                    alClickRegresar = {
                        mostrarDialogoSalir = true
                    },
                    alClickConfirmar = { nuevaContrasena, confirmarContrasena ->
                        when {
                            !com.example.rest.utils.SecurityUtils.isValidPassword(nuevaContrasena) -> {
                                Toast.makeText(this, getString(R.string.err_invalid_password_format), Toast.LENGTH_SHORT).show()
                            }
                            nuevaContrasena != confirmarContrasena -> {
                                Toast.makeText(this, getString(R.string.err_password_mismatch), Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                cargando = true
                                cambiarContrasena(nuevaContrasena) {
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
    
    private fun cambiarContrasena(nuevaContrasena: String, onComplete: () -> Unit) {
        lifecycleScope.launch {
            try {
                when (val resultado = recuperacionRepository.cambiarContraseña(correo, codigoId, nuevaContrasena)) {
                    is RecuperacionRepository.Result.Success<*> -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@CambioContrasenaActivity,
                                getString(R.string.toast_password_changed),
                                Toast.LENGTH_LONG
                            ).show()
                            
                            // Navegar a LoginActivity
                            val intent = Intent(this@CambioContrasenaActivity, LoginComposeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                    is RecuperacionRepository.Result.Error -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@CambioContrasenaActivity,
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
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@CambioContrasenaActivity,
                        getString(R.string.toast_unexpected_error, e.message),
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
fun PantallaCambioContrasena(
    alClickRegresar: () -> Unit,
    alClickConfirmar: (String, String) -> Unit,
    cargando: Boolean = false
) {
    var nuevaContrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var mostrarNueva by remember { mutableStateOf(false) }
    var mostrarConfirmar by remember { mutableStateOf(false) }
    var menuIdiomaExpandido by remember { mutableStateOf(false) }
    
    // Interceptar botón atrás del sistema
    BackHandler {
        alClickRegresar()
    }

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
        // Barra Superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Botón de regresar
            IconButton(onClick = alClickRegresar) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.content_desc_back),
                    tint = Negro,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Selector de Idioma
            Box {
                IconButton(onClick = { menuIdiomaExpandido = true }) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Cambiar Idioma",
                        tint = Negro,
                        modifier = Modifier.size(32.dp)
                    )
                }
                DropdownMenu(
                    expanded = menuIdiomaExpandido,
                    onDismissRequest = { menuIdiomaExpandido = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Español") },
                        onClick = {
                            menuIdiomaExpandido = false
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("English") },
                        onClick = {
                            menuIdiomaExpandido = false
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Português") },
                        onClick = {
                            menuIdiomaExpandido = false
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("pt"))
                        }
                    )
                }
            }
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
                    contentDescription = stringResource(R.string.content_desc_owl_logo),
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
                            text = stringResource(R.string.change_password_title_speech),
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
                value = nuevaContrasena,
                onValueChange = { nuevaContrasena = it },
                placeholder = {
                    Text(
                        stringResource(R.string.change_password_new_placeholder),
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
                visualTransformation = if (mostrarNueva) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (mostrarNueva) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { mostrarNueva = !mostrarNueva }) {
                        Icon(imageVector = image, contentDescription = if (mostrarNueva) stringResource(R.string.content_desc_hide_password) else stringResource(R.string.content_desc_show_password))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Confirmar Pin
            OutlinedTextField(
                value = confirmarContrasena,
                onValueChange = { confirmarContrasena = it },
                placeholder = {
                    Text(
                        stringResource(R.string.register_confirm_password_placeholder),
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
                visualTransformation = if (mostrarConfirmar) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (mostrarConfirmar) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { mostrarConfirmar = !mostrarConfirmar }) {
                        Icon(imageVector = image, contentDescription = if (mostrarConfirmar) stringResource(R.string.content_desc_hide_password) else stringResource(R.string.content_desc_show_password))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Confirmar
            Button(
                onClick = { alClickConfirmar(nuevaContrasena, confirmarContrasena) },
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
                        color = Blanco,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.btn_confirm),
                        style = MaterialTheme.typography.labelLarge,
                        color = Negro
                    )
                }
            }
        }
    }
}
