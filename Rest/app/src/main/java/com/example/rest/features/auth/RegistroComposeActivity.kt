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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import com.example.rest.data.models.RegistroRequest
import com.example.rest.data.repository.UsuarioRepository
import com.example.rest.ui.theme.*
import com.example.rest.ui.components.inputs.CampoFechaAutoFormato
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
                    alClickRegistrar = { request, pin ->
                        cargando = true
                        realizarRegistro(request, pin) {
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
        pin: String,
        onComplete: () -> Unit
    ) {
        // Verificar conectividad antes de intentar el registro
        if (!isNetworkAvailable()) {
            Toast.makeText(
                this@RegistroComposeActivity,
                getString(R.string.toast_no_internet),
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
                            intent.putExtra("contraseña", pin)
                            startActivity(intent)
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
                            // Show persistent dialog instead of Toast so user can read it
                            val builder = android.app.AlertDialog.Builder(this@RegistroComposeActivity)
                            builder.setTitle(getString(R.string.dialog_detailed_error))
                            builder.setMessage(mensajeError)
                            builder.setPositiveButton(getString(R.string.dialog_ok)) { dialog, _ -> dialog.dismiss() }
                            builder.show()
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
                    val builder = android.app.AlertDialog.Builder(this@RegistroComposeActivity)
                    builder.setTitle(getString(R.string.dialog_exception))
                    builder.setMessage(mensajeError)
                    builder.setPositiveButton(getString(R.string.dialog_ok)) { dialog, _ -> dialog.dismiss() }
                    builder.show()
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
    alClickRegistrar: (RegistroRequest, String) -> Unit,
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
    var mostrarPin by remember { mutableStateOf(false) }
    var mostrarConfirmarPin by remember { mutableStateOf(false) }
    var aceptaTerminos by remember { mutableStateOf(false) }
    var rol by remember { mutableStateOf("hijo") } // Por defecto "hijo"

    // FocusRequesters para navegación entre campos
    val apellidoFocus = remember { FocusRequester() }
    val correoFocus = remember { FocusRequester() }
    val fechaFocus = remember { FocusRequester() }
    val pinFocus = remember { FocusRequester() }
    val confirmarPinFocus = remember { FocusRequester() }

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
        val context = androidx.compose.ui.platform.LocalContext.current
        
        // --- Selector de Idioma (Superior Derecha) ---
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

        // Botón de volver (flecha) en la esquina superior izquierda
        IconButton(
            onClick = alClickYaTienesCuenta,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.content_desc_back),
                tint = Negro,
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
                        text = stringResource(R.string.register_title_speech),
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
                        stringResource(R.string.register_name_placeholder),
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { 
                        apellidoFocus.requestFocus()
                    }
                ),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color(0xFF757575)
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Apellido
            OutlinedTextField(
                value = apellido,
                onValueChange = { apellido = it },
                placeholder = { 
                    Text(
                        stringResource(R.string.register_lastname_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF757575)
                    ) 
                },
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp)
                    .focusRequester(apellidoFocus),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco,
                    focusedBorderColor = Color(0xFF6B4EFF),
                    unfocusedBorderColor = Color(0xFFB0BEC5),
                    focusedTextColor = Negro,
                    unfocusedTextColor = Negro
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { correoFocus.requestFocus() }),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color(0xFF757575)
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Correo Electrónico
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it.trim() },
                placeholder = { 
                    Text(
                        stringResource(R.string.register_email_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF757575)
                    ) 
                },
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp)
                    .focusRequester(correoFocus),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco,
                    focusedBorderColor = Color(0xFF6B4EFF),
                    unfocusedBorderColor = Color(0xFFB0BEC5),
                    focusedTextColor = Negro,
                    unfocusedTextColor = Negro
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { fechaFocus.requestFocus() }),
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

            // Campo de Fecha de Nacimiento con formato automático
            CampoFechaAutoFormato(
                value = fechaNacimiento,
                onValueChange = { fechaNacimiento = it },
                label = stringResource(R.string.register_birthdate_placeholder),
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp)
                    .focusRequester(fechaFocus),
                shape = RoundedCornerShape(30.dp),
                focusedContainerColor = Blanco,
                unfocusedContainerColor = Blanco,
                focusedBorderColor = Color(0xFF6B4EFF),
                unfocusedBorderColor = Color(0xFFB0BEC5),
                focusedTextColor = Negro,
                unfocusedTextColor = Negro,
                placeholderColor = Color(0xFF757575),
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardActions = KeyboardActions(onNext = { pinFocus.requestFocus() })
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Pin
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                placeholder = { 
                    Text(
                        stringResource(R.string.register_password_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF757575)
                    ) 
                },
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp)
                    .focusRequester(pinFocus),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco,
                    focusedBorderColor = Color(0xFF6B4EFF),
                    unfocusedBorderColor = Color(0xFFB0BEC5),
                    focusedTextColor = Negro,
                    unfocusedTextColor = Negro
                ),
                visualTransformation = if (mostrarPin) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { confirmarPinFocus.requestFocus() }),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = Color(0xFF757575)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { mostrarPin = !mostrarPin }) {
                        Icon(
                            imageVector = if (mostrarPin) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (mostrarPin) stringResource(R.string.content_desc_hide_password) else stringResource(R.string.content_desc_show_password),
                            tint = Color(0xFF757575)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Confirmar Pin
            OutlinedTextField(
                value = confirmarPin,
                onValueChange = { confirmarPin = it },
                placeholder = { 
                    Text(
                        stringResource(R.string.register_confirm_password_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF757575)
                    ) 
                },
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp)
                    .focusRequester(confirmarPinFocus),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco,
                    focusedBorderColor = Color(0xFF6B4EFF),
                    unfocusedBorderColor = Color(0xFFB0BEC5),
                    focusedTextColor = Negro,
                    unfocusedTextColor = Negro
                ),
                visualTransformation = if (mostrarConfirmarPin) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.LockOpen,
                        contentDescription = null,
                        tint = Color(0xFF757575)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { mostrarConfirmarPin = !mostrarConfirmarPin }) {
                        Icon(
                            imageVector = if (mostrarConfirmarPin) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (mostrarConfirmarPin) stringResource(R.string.content_desc_hide_password) else stringResource(R.string.content_desc_show_password),
                            tint = Color(0xFF757575)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // Checkbox de Términos y Condiciones
            Row(
                modifier = Modifier
                    .width(330.dp)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Checkbox(
                    checked = aceptaTerminos,
                    onCheckedChange = { aceptaTerminos = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF6B4EFF),
                        uncheckedColor = Color(0xFF424242),
                        checkmarkColor = Blanco
                    ),
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = stringResource(R.string.register_accept_terms),
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
                            alMostrarError(context.getString(R.string.err_empty_name))
                        }
                        !nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$".toRegex()) -> {
                            alMostrarError(context.getString(R.string.err_invalid_name))
                        }
                        apellido.isNotBlank() && !apellido.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$".toRegex()) -> {
                            alMostrarError(context.getString(R.string.err_invalid_lastname))
                        }
                        correo.isBlank() -> {
                            alMostrarError(context.getString(R.string.err_empty_email))
                        }
                        !com.example.rest.utils.SecurityUtils.isValidEmailDomain(correo) -> {
                            alMostrarError(context.getString(R.string.err_invalid_email_format))
                        }
                        // Telefono validacion eliminada
                        fechaNacimiento.isBlank() -> {
                            alMostrarError(context.getString(R.string.err_empty_birthdate))
                        }
                        pin.isBlank() -> {
                            alMostrarError(context.getString(R.string.err_empty_confirm_password))
                        }
                        !com.example.rest.utils.SecurityUtils.isValidPassword(pin) -> {
                            alMostrarError(context.getString(R.string.err_invalid_password_format))
                        }
                        confirmarPin.isBlank() -> {
                            alMostrarError(context.getString(R.string.err_empty_confirm_password))
                        }
                        pin != confirmarPin -> {
                            alMostrarError(context.getString(R.string.err_password_mismatch))
                        }
                        !aceptaTerminos -> {
                            alMostrarError(context.getString(R.string.err_accept_terms))
                        }
                        else -> {
                            // Convertir fecha de dígitos (YYYYMMDD) a formato YYYY-MM-DD
                            val fechaFormateada = if (fechaNacimiento.length == 8) {
                                "${fechaNacimiento.substring(0, 4)}-${fechaNacimiento.substring(4, 6)}-${fechaNacimiento.substring(6, 8)}"
                            } else {
                                fechaNacimiento
                            }
                            
                            // Validar formato de fecha
                            val mayorEdad = try {
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                val fechaNac = LocalDate.parse(fechaFormateada, formatter)
                                val edad = Period.between(fechaNac, LocalDate.now()).years
                                edad >= 18
                            } catch (e: Exception) {
                                alMostrarError(context.getString(R.string.err_invalid_birthdate_format))
                                return@Button
                            }

                            // Asignación automática de rol según edad
                            val rolAutomatico = if (mayorEdad) "padre" else "hijo"

                            val request = RegistroRequest(
                                nombre = nombre,
                                apellido = apellido.ifBlank { null },
                                correo = correo,
                                fechaNacimiento = fechaFormateada,
                                contraseña = com.example.rest.utils.SecurityUtils.hashPassword(pin),
                                rol = rolAutomatico
                            )
                            alClickRegistrar(request, pin)
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
                        text = stringResource(R.string.register_button_text),
                        style = MaterialTheme.typography.labelLarge,
                        color = Negro
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
