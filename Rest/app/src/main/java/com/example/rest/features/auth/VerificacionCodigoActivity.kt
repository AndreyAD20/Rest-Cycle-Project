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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import com.example.rest.data.repository.UsuarioRepository
import com.example.rest.features.home.InicioComposeActivity
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
            Toast.makeText(this, getString(R.string.error_no_email_provided), Toast.LENGTH_LONG).show()
            finish()
            return
        }
        // Contraseña para auto-login tras verificación (puede ser null si no viene del registro)
        val contraseñaAutoLogin = intent.getStringExtra("contraseña")
        
        setContent {
            TemaRest {
                var cargando by remember { mutableStateOf(false) }
                
                PantallaVerificacionCodigo(
                    correo = correo,
                    alClickVerificar = { codigo ->
                        cargando = true
                        verificarCodigo(codigo, contraseñaAutoLogin) {
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
    
    private fun verificarCodigo(codigo: String, contraseña: String?, onComplete: () -> Unit) {
        lifecycleScope.launch {
            try {
                when (val resultado = usuarioRepository.verificarCodigo(correo, codigo)) {
                    is UsuarioRepository.Result.Success -> {
                        runOnUiThread {
                            Toast.makeText(
                                this@VerificacionCodigoActivity,
                                getString(R.string.toast_email_verified),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        
                        val retornarAPadre = intent.getBooleanExtra("retornarAPadre", false)
                        if (retornarAPadre) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@VerificacionCodigoActivity,
                                    getString(R.string.toast_child_account_verified),
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
                            }
                        } else if (!contraseña.isNullOrBlank()) {
                            // Auto-login con las credenciales del registro
                            when (val loginResult = usuarioRepository.login(this@VerificacionCodigoActivity, correo, contraseña)) {
                                is UsuarioRepository.Result.Success -> {
                                    val usuario = loginResult.data
                                    runOnUiThread {
                                        val preferencesManager = com.example.rest.utils.PreferencesManager(this@VerificacionCodigoActivity)
                                        preferencesManager.saveUserName(usuario.nombre)
                                        preferencesManager.saveUserId(usuario.id ?: -1)
                                        preferencesManager.saveUserEmail(correo)
                                        preferencesManager.saveMayorEdad(usuario.mayorEdad)
                                        val intent = Intent(this@VerificacionCodigoActivity, InicioComposeActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                                else -> {
                                    // Fallback: ir al login si el auto-login falla
                                    runOnUiThread {
                                        val intent = Intent(this@VerificacionCodigoActivity, LoginComposeActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                            }
                        } else {
                            runOnUiThread {
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
                    is UsuarioRepository.Result.NotVerified -> {
                        // Not expecting this from verificarCodigo, but required to be exhaustive
                    }
                    is UsuarioRepository.Result.Requires2FA -> {
                        // Not expecting this from verificarCodigo, but required to be exhaustive
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
                                "✅ Código reenviado exitosamente. Revisa tu correo.",
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
                    is UsuarioRepository.Result.NotVerified -> {
                        // Not expecting this from reenviarCodigo, but required to be exhaustive
                    }
                    is UsuarioRepository.Result.Requires2FA -> {
                        // Not expecting this from reenviarCodigo, but required to be exhaustive
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
    
    // Gradiente id\u00e9ntico al de Selecci\u00f3n de Modos
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
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                ) {
                    Text(
                        text = stringResource(R.string.verify_email_bubble_speech),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            // Título
            Text(
                text = stringResource(R.string.verify_email_title),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Mensaje informativo
            Text(
                text = stringResource(R.string.verify_email_sent_to),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = correo,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Campo de código OTP - 6 cajas individuales
            OtpInputField(
                otpValue = codigo,
                onOtpChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) codigo = it }
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
                    .width(220.dp)
                    .height(56.dp)
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
                        text = stringResource(R.string.btn_verify),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = alClickReenviar,
                enabled = !cargando
            ) {
                Text(
                    text = stringResource(R.string.btn_resend_code),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun OtpInputField(
    otpValue: String,
    onOtpChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
    ) {
        // Invisible field that captures keyboard input
        BasicTextField(
            value = otpValue,
            onValueChange = onOtpChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester),
            decorationBox = {}
        )

        // 6 visible boxes
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(6) { index ->
                val char = otpValue.getOrNull(index)?.toString() ?: ""
                val isCurrent = otpValue.length == index

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Blanco)
                        .border(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = if (isCurrent) Color(0xFF6B4EFF) else Color(0xFFB0BEC5),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                ) {
                    Text(
                        text = char,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Negro,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}
