package com.example.rest.features.home

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import com.example.rest.ui.theme.*
import com.example.rest.utils.SecurityUtils
import kotlinx.coroutines.launch

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

class CambiarContrasenaComposeActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode = com.example.rest.utils.ThemeManager.isDarkMode(this)
            TemaRest(temaOscuro = isDarkMode) {
                PantallaCambiarContrasena(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCambiarContrasena(onBackClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var actualPwd by remember { mutableStateOf("") }
    var nuevaPwd by remember { mutableStateOf("") }
    var confirmarPwd by remember { mutableStateOf("") }
    var actualPwdVisible by remember { mutableStateOf(false) }
    var nuevaPwdVisible by remember { mutableStateOf(false) }
    var confirmarPwdVisible by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val usuarioRepository = remember { com.example.rest.data.repository.UsuarioRepository() }
    val prefs = com.example.rest.utils.PreferencesManager(context)
    val userId = prefs.getUserId()

    val brochaGradiente = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0D47A1),   // Azul profundo
            Color(0xFF00838F),   // Teal
            Color(0xFF00BFA5)    // Verde menta
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 2000f)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.change_password_screen_title), color = Negro, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.content_desc_back), tint = Negro)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brochaGradiente)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
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
                    
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier
                    ) {
                        Text(
                            text = stringResource(R.string.change_password_title_speech),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = actualPwd,
                    onValueChange = { actualPwd = it },
                    label = { Text(stringResource(R.string.change_password_current_label)) },
                    visualTransformation = if (actualPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (actualPwdVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (actualPwdVisible) stringResource(R.string.content_desc_hide_password) else stringResource(R.string.content_desc_show_password)
                        IconButton(onClick = { actualPwdVisible = !actualPwdVisible }) {
                            Icon(imageVector = image, contentDescription = description, tint = Primario)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.2f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedTrailingIconColor = Color.White,
                        unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f)
                    )
                )
                OutlinedTextField(
                    value = nuevaPwd,
                    onValueChange = { nuevaPwd = it },
                    label = { Text(stringResource(R.string.change_password_new_label)) },
                    visualTransformation = if (nuevaPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (nuevaPwdVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (nuevaPwdVisible) stringResource(R.string.content_desc_hide_password) else stringResource(R.string.content_desc_show_password)
                        IconButton(onClick = { nuevaPwdVisible = !nuevaPwdVisible }) {
                            Icon(imageVector = image, contentDescription = description, tint = Primario)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.2f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedTrailingIconColor = Color.White,
                        unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f)
                    )
                )
                OutlinedTextField(
                    value = confirmarPwd,
                    onValueChange = { confirmarPwd = it },
                    label = { Text(stringResource(R.string.change_password_confirm_new_label)) },
                    visualTransformation = if (confirmarPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (confirmarPwdVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (confirmarPwdVisible) stringResource(R.string.content_desc_hide_password) else stringResource(R.string.content_desc_show_password)
                        IconButton(onClick = { confirmarPwdVisible = !confirmarPwdVisible }) {
                            Icon(imageVector = image, contentDescription = description, tint = Primario)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.2f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedTrailingIconColor = Color.White,
                        unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f)
                    )
                )
                Button(
                    onClick = {
                        if (actualPwd.isBlank() || nuevaPwd.isBlank() || confirmarPwd.isBlank()) {
                            Toast.makeText(context, context.getString(R.string.toast_fill_all_fields), Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (nuevaPwd != confirmarPwd) {
                            Toast.makeText(context, context.getString(R.string.toast_passwords_no_match), Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        scope.launch {
                            try {
                                // Verificar contraseña actual
                                val userRes = usuarioRepository.obtenerUsuarioPorId(userId)
                                if (userRes is com.example.rest.data.repository.UsuarioRepository.Result.Success) {
                                    val user = userRes.data
                                    val hashedActual = SecurityUtils.hashPassword(actualPwd)
                                    // Comprobar si la contraseña coincide (si es bcrypt hay que usar verify, depende de SecurityUtils)
                                    val isValid = SecurityUtils.verifyPassword(actualPwd, user.contraseña)
                                    
                                    if (!isValid) {
                                        Toast.makeText(context, context.getString(R.string.toast_password_incorrect), Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }
                                    
                                    // Validar complejidad de nueva contraseña
                                    if (!SecurityUtils.isValidPassword(nuevaPwd)) {
                                        Toast.makeText(context, context.getString(R.string.toast_password_complexity), Toast.LENGTH_LONG).show()
                                        return@launch
                                    }

                                    // Actualizar con nueva contraseña
                                    val nuevaHash = SecurityUtils.hashPassword(nuevaPwd)
                                    val updated = user.copy(contraseña = nuevaHash)
                                    when (val updRes = usuarioRepository.actualizarUsuario(userId, updated)) {
                                        is com.example.rest.data.repository.UsuarioRepository.Result.Success -> {
                                            Toast.makeText(context, context.getString(R.string.toast_password_updated), Toast.LENGTH_SHORT).show()
                                            onBackClick() // finaliza
                                        }
                                        is com.example.rest.data.repository.UsuarioRepository.Result.Error -> {
                                            Toast.makeText(context, context.getString(R.string.toast_password_update_error, updRes.message), Toast.LENGTH_SHORT).show()
                                        }
                                        else -> {}
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, context.getString(R.string.toast_exception_saving_profile, e.message), Toast.LENGTH_SHORT).show()
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.change_password_button), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
