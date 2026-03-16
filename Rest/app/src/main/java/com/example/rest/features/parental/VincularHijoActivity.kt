package com.example.rest.features.parental

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rest.R
import androidx.lifecycle.lifecycleScope
import com.example.rest.BaseComposeActivity
import com.example.rest.data.repository.UsuarioRepository
import com.example.rest.ui.theme.*
import com.example.rest.utils.PreferencesManager
import kotlinx.coroutines.launch

class VincularHijoActivity : BaseComposeActivity() {

    private val repository = UsuarioRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PreferencesManager(this)
        val idPadre = prefs.getUserId()

        setContent {
            TemaRest {
                PantallaVincularHijo(
                    idPadre = idPadre,
                    onVinculacionExitosa = {
                        Toast.makeText(
                            this,
                            getString(R.string.toast_child_linked_success),
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    },
                    onVolver = { finish() },
                    repository = repository,
                    onError = { mensaje ->
                        Toast.makeText(this, "❌ $mensaje", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaVincularHijo(
    idPadre: Int,
    onVinculacionExitosa: () -> Unit,
    onVolver: () -> Unit,
    onError: (String) -> Unit,
    repository: UsuarioRepository
) {
    var codigoVinculacion by remember { mutableStateOf("") }
    var contrasenaParental by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var verContrasena by remember { mutableStateOf(false) }
    var verConfirmar by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Validaciones en tiempo real
    val codigoValido = codigoVinculacion.length == 5
    val contrasenasCoinciden = contrasenaParental == confirmarContrasena
    val contrasenaFuerte = com.example.rest.utils.SecurityUtils.isValidPassword(contrasenaParental)
    val formularioValido = codigoValido && contrasenasCoinciden && contrasenaFuerte && !cargando

    val fondoGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF1A237E), Color(0xFF00695C)),
        start = Offset(0f, 0f),
        end = Offset(800f, 1800f)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.link_child_title),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.content_desc_back), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fondoGradiente)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Icono decorativo
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FamilyRestroom,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.link_child_code_instruction),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                )

                // ---- TARJETA DEL FORMULARIO ----
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Código de vinculación del hijo
                        Text(
                            text = stringResource(R.string.link_child_code_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF37474F)
                        )

                        OutlinedTextField(
                            value = codigoVinculacion,
                            onValueChange = {
                                if (it.length <= 5) codigoVinculacion = it.uppercase()
                            },
                            placeholder = { Text(stringResource(R.string.link_child_code_placeholder)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.Numbers, contentDescription = null, tint = Color(0xFF00695C))
                            },
                            trailingIcon = {
                                if (codigoVinculacion.isNotEmpty()) {
                                    if (codigoValido) {
                                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32))
                                    } else {
                                        Text(
                                            "${codigoVinculacion.length}/5",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF90A4AE)
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00695C),
                                unfocusedBorderColor = Color(0xFFB0BEC5)
                            )
                        )

                        HorizontalDivider(color = Color(0xFFECEFF1))

                        // Contraseña parental
                        Text(
                            text = stringResource(R.string.parental_password_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF37474F)
                        )

                        OutlinedTextField(
                            value = contrasenaParental,
                            onValueChange = { contrasenaParental = it },
                            label = { Text(stringResource(R.string.parental_password_placeholder)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = if (verContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00695C))
                            },
                            trailingIcon = {
                                IconButton(onClick = { verContrasena = !verContrasena }) {
                                    Icon(
                                        if (verContrasena) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (verContrasena) stringResource(R.string.content_desc_hide_password) else stringResource(R.string.content_desc_show_password)
                                    )
                                }
                            },
                            isError = contrasenaParental.isNotBlank() && !contrasenaFuerte,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00695C),
                                unfocusedBorderColor = Color(0xFFB0BEC5)
                            )
                        )

                        // Info banner de requisitos
                        AnimatedVisibility(visible = contrasenaParental.isNotEmpty() && !contrasenaFuerte) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Info, tint = Color(0xFFF57F17), contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text(
                                        stringResource(R.string.err_parental_password_format),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF5D4037)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = confirmarContrasena,
                            onValueChange = { confirmarContrasena = it },
                            label = { Text(stringResource(R.string.parental_password_confirm_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = if (verConfirmar) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color(0xFF00695C))
                            },
                            trailingIcon = {
                                IconButton(onClick = { verConfirmar = !verConfirmar }) {
                                    Icon(
                                        if (verConfirmar) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (verConfirmar) stringResource(R.string.content_desc_hide_password) else stringResource(R.string.content_desc_show_password)
                                    )
                                }
                            },
                            isError = confirmarContrasena.isNotBlank() && !contrasenasCoinciden,
                            supportingText = {
                                if (confirmarContrasena.isNotBlank() && !contrasenasCoinciden) {
                                    Text(stringResource(R.string.err_password_mismatch), color = MaterialTheme.colorScheme.error)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00695C),
                                unfocusedBorderColor = Color(0xFFB0BEC5)
                            )
                        )
                    }
                }

                // Información adicional
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Info, tint = Color(0xFF80DEEA), contentDescription = null, modifier = Modifier.size(20.dp))
                        Text(
                            text = stringResource(R.string.link_child_info_banner),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // Botón principal
                Button(
                    onClick = {
                        when {
                            !codigoValido ->
                                onError(context.getString(R.string.err_code_length))
                            !contrasenaFuerte ->
                                onError(context.getString(R.string.err_parental_password_format))
                            !contrasenasCoinciden ->
                                onError(context.getString(R.string.err_password_mismatch))
                            else -> {
                                cargando = true
                                scope.launch {
                                    val result = repository.vincularHijo(
                                        context = context,
                                        idPadre = idPadre,
                                        codigoVinculacion = codigoVinculacion,
                                        contrasenaParental = contrasenaParental
                                    )
                                    cargando = false
                                    when (result) {
                                        is UsuarioRepository.Result.Success -> onVinculacionExitosa()
                                        is UsuarioRepository.Result.Error -> onError(result.message)
                                        else -> {}
                                    }
                                }
                            }
                        }
                    },
                    enabled = formularioValido,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00BFA5),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.3f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(Icons.Default.Link, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.link_child_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
