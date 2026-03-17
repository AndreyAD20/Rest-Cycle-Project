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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import androidx.compose.ui.res.stringResource
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
                                Toast.makeText(this, getString(R.string.toast_invalid_email), Toast.LENGTH_SHORT).show()
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
                                getString(R.string.toast_code_sent, correo),
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
fun PantallaOlvidoContrasena(
    alClickRegresar: () -> Unit,
    alClickEnviar: (String) -> Unit,
    cargando: Boolean = false
) {
    var correo by remember { mutableStateOf("") }
    var menuIdiomaExpandido by remember { mutableStateOf(false) }

    val brochaGradiente = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0D47A1),   // Azul profundo
            Color(0xFF00838F),   // Teal
            Color(0xFF00BFA5)    // Verde menta
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 2000f)
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
            // Bot\u00f3n de regresar
            IconButton(onClick = alClickRegresar) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.content_desc_back),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Selector de Idioma
            val context = androidx.compose.ui.platform.LocalContext.current
            val sharedPrefs = remember { context.getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE) }
            var idiomaSeleccionado by remember { mutableStateOf(sharedPrefs.getString("IDIOMA", "Espa\u00f1ol") ?: "Espa\u00f1ol") }

            Box {
                IconButton(onClick = { menuIdiomaExpandido = true }) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Cambiar Idioma",
                        tint = Color.White,
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
                                    langPt, "Portugu\u00eas" -> "pt"
                                    else -> "es"
                                }
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
                                android.widget.Toast.makeText(context, context.getString(R.string.toast_language_saved, opcion), android.widget.Toast.LENGTH_SHORT).show()
                                
                                val activity = context as? android.app.Activity
                                if (activity != null) {
                                    val intent = android.content.Intent(activity, OlvidoContrasenaActivity::class.java)
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
            // Logo del b\u00facho con bocadillo
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
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .width(200.dp)
                        .height(80.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.recovery_email_title_speech),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Campo de Correo
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it.trim() },
                placeholder = {
                    Text(
                        stringResource(R.string.login_email_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Bot\u00f3n Enviar C\u00f3digo
            Button(
                onClick = { alClickEnviar(correo) },
                modifier = Modifier
                    .width(220.dp)
                    .height(64.dp)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                ),
                enabled = !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.recovery_send_button),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
