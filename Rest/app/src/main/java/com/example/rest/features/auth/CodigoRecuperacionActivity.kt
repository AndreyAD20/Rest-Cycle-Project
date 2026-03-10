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
import androidx.activity.compose.BackHandler
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.Language
import androidx.compose.ui.res.stringResource
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
                var mostrarDialogoSalir by remember { mutableStateOf(false) }
                
                if (mostrarDialogoSalir) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogoSalir = false },
                        title = { Text(stringResource(R.string.dialog_cancel_recovery_title)) },
                        text = { Text(stringResource(R.string.dialog_cancel_recovery_text)) },
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
                
                PantallaCodigoRecuperacion(
                    alClickRegresar = {
                        mostrarDialogoSalir = true
                    },
                    alClickConfirmar = { codigo ->
                        when {
                            codigo.isBlank() || codigo.length != 6 -> {
                                Toast.makeText(this, getString(R.string.toast_invalid_code_length), Toast.LENGTH_SHORT).show()
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
                        // Reenviar código
                        cargando = true
                        reenviarCodigo {
                            cargando = false
                        }
                    },
                    cargando = cargando
                )
            }
        }
    }
    
    private fun reenviarCodigo(onComplete: () -> Unit) {
        lifecycleScope.launch {
            try {
                when (val resultado = recuperacionRepository.solicitarCodigo(correo)) {
                    is RecuperacionRepository.Result.Success -> {
                        runOnUiThread {
                            Toast.makeText(
                                this@CodigoRecuperacionActivity,
                                getString(R.string.toast_new_code_sent),
                                Toast.LENGTH_SHORT
                            ).show()
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
                        getString(R.string.toast_resend_code_error, e.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                onComplete()
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
                                getString(R.string.toast_code_verified),
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
fun PantallaCodigoRecuperacion(
    alClickRegresar: () -> Unit,
    alClickConfirmar: (String) -> Unit,
    alClickReenviar: () -> Unit,
    cargando: Boolean = false
) {
    var codigo by remember { mutableStateOf("") }
    var tiempoRestante by remember { mutableStateOf(60) } // Iniciar con 60 segundos
    var puedeReenviar by remember { mutableStateOf(false) } // Iniciar deshabilitado
    var menuIdiomaExpandido by remember { mutableStateOf(false) }
    
    // Interceptar botón atrás del sistema
    BackHandler {
        alClickRegresar()
    }
    
    // Cooldown timer
    LaunchedEffect(tiempoRestante) {
        if (tiempoRestante > 0) {
            kotlinx.coroutines.delay(1000)
            tiempoRestante--
        } else {
            puedeReenviar = true
        }
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
            val context = androidx.compose.ui.platform.LocalContext.current
            val sharedPrefs = remember { context.getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE) }
            var idiomaSeleccionado by remember { mutableStateOf(sharedPrefs.getString("IDIOMA", "Español") ?: "Español") }

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
                    onDismissRequest = { menuIdiomaExpandido = false },
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
                                
                                val activity = context as? android.app.Activity
                                if (activity is CodigoRecuperacionActivity) {
                                    val intent = android.content.Intent(activity, CodigoRecuperacionActivity::class.java)
                                    intent.putExtra("correo", activity.intent.getStringExtra("correo") ?: "")
                                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    activity.startActivity(intent)
                                    activity.finish()
                                }
                                menuIdiomaExpandido = false
                            }
                        )
                    }
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
                            text = stringResource(R.string.recovery_code_title_speech),
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
                        stringResource(R.string.recovery_code_placeholder),
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

            Spacer(modifier = Modifier.height(24.dp))

            // Link para reenviar código
            Text(
                text = if (puedeReenviar) {
                    stringResource(R.string.recovery_resend_code)
                } else {
                    stringResource(R.string.recovery_resend_in_seconds, tiempoRestante)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (puedeReenviar) Color(0xFF004D40) else Color(0xFF999999),
                modifier = Modifier.clickable(enabled = puedeReenviar) {
                    if (puedeReenviar) {
                        puedeReenviar = false
                        tiempoRestante = 60
                        alClickReenviar()
                    }
                }
            )
        }
    }
}



