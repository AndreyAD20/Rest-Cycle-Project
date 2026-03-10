package com.example.rest.features.home

import android.os.Bundle
import com.example.rest.BaseComposeActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.FileProvider
import com.example.rest.features.habits.*
import com.example.rest.ui.theme.*
import com.example.rest.ui.components.dialogs.DialogoNota
import androidx.compose.ui.res.stringResource
import com.example.rest.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.rest.utils.*

class PerfilComposeActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode = com.example.rest.utils.ThemeManager.isDarkMode(this)
            TemaRest(temaOscuro = isDarkMode) {
                PantallaPerfil(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(onBackClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Estado para la imagen de perfil
    var profileImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }

    // Cargar imagen guardada al inicio y SIEMPRE verificar actualizaciones
    LaunchedEffect(Unit) {
        val prefs = com.example.rest.utils.PreferencesManager(context)
        val userId = prefs.getUserId()
        
        if (userId != -1) {
            // 1. Cargar lo que tengamos localmente primero (para velocidad)
            val localBitmap = getProfileImageBitmap(context, userId)
            if (localBitmap != null) {
                profileImageBitmap = localBitmap
            }

            // 2. SIEMPRE intentar sincronizar con Supabase en segundo plano
            scope.launch {
                isCheckingUpdate = true
                val remoteBitmap = descargarFotoDeSupabase(context, userId)
                if (remoteBitmap != null) {
                    // Si hay foto remota, actualizamos la UI y el caché local
                    profileImageBitmap = remoteBitmap
                }
                isCheckingUpdate = false
            }
        }
    }



    // Launchers para cámara y galería
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUri != null) {
            val prefs = com.example.rest.utils.PreferencesManager(context)
            val userId = prefs.getUserId()
            
            if (userId != -1) {
                saveImageToInternalStorage(context, tempCameraUri!!, userId)
                profileImageBitmap = getProfileImageBitmap(context, userId)
                
                // Subir a Supabase
                scope.launch {
                    profileImageBitmap?.let { bitmap ->
                        subirFotoASupabase(context, bitmap)
                    }
                }
            }
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val prefs = com.example.rest.utils.PreferencesManager(context)
            val userId = prefs.getUserId()
            
            if (userId != -1) {
                saveImageToInternalStorage(context, uri, userId)
                profileImageBitmap = getProfileImageBitmap(context, userId)
                
                // Subir a Supabase
                scope.launch {
                    profileImageBitmap?.let { bitmap ->
                        subirFotoASupabase(context, bitmap)
                    }
                }
            }
        }
    }

    // Función para manejar la selección de fuente
    fun launchCamera() {
        Log.d("PerfilDebug", "Intentando lanzar cámara...")
        try {
            val file = createImageFile(context)
            Log.d("PerfilDebug", "Archivo creado: ${file.absolutePath}")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            Log.d("PerfilDebug", "URI generada: $uri")
            tempCameraUri = uri
            cameraLauncher.launch(uri)
            Log.d("PerfilDebug", "Launcher de cámara iniciado")
        } catch (e: Exception) {
            Log.e("PerfilDebug", "Error al lanzar cámara: ${e.message}")
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.toast_camera_error, e.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, context.getString(R.string.toast_permission_granted), Toast.LENGTH_SHORT).show()
            launchCamera()
        } else {
            Toast.makeText(context, context.getString(R.string.toast_permission_required_photo), Toast.LENGTH_SHORT).show()
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text(stringResource(R.string.profile_photo_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.profile_photo_choose_option))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Botón Cámara
                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            val permissionCheckResult = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permissionCheckResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                launchCamera()
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primario)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.profile_photo_take))
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Botón Galería
                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primario)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.profile_photo_choose_gallery))
                    }
                    
                    // Mostrar botón de eliminar solo si hay foto
                    if (profileImageBitmap != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = {
                                showImageSourceDialog = false
                                // Eliminar foto
                                val prefs = com.example.rest.utils.PreferencesManager(context)
                                val userId = prefs.getUserId()
                                
                                if (userId != -1) {
                                    // Eliminar localmente
                                    deleteProfileImage(context, userId)
                                    profileImageBitmap = null
                                    
                                    // Eliminar de Supabase
                                    scope.launch {
                                        eliminarFotoDeSupabase(context)
                                    }
                                    
                                    Toast.makeText(context, context.getString(R.string.toast_photo_deleted), Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.profile_photo_delete))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Primario, Color(0xFF80DEEA)),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    // Variables de estado para edición de perfil
    var userId by remember { mutableStateOf(-1) }
    var nombreText by remember { mutableStateOf("") }
    var apellidoText by remember { mutableStateOf("") }
    var correoText by remember { mutableStateOf("") }
    var correoOriginal by remember { mutableStateOf("") } // Para detectar si el correo cambió
    var fechaText by remember { mutableStateOf("") }
    var nuevaContraseña by remember { mutableStateOf("") }
    var confirmarContraseña by remember { mutableStateOf("") }
    var isLoadingUserData by remember { mutableStateOf(true) }
    var isSavingUser by remember { mutableStateOf(false) }

    // Estado para verificación de correo
    var mostrarDialogoVerificacion by remember { mutableStateOf(false) }
    var codigoIngresado by remember { mutableStateOf("") }
    var codigoGenerado by remember { mutableStateOf("") }
    var codigoVerificado by remember { mutableStateOf(false) }
    var errorCodigo by remember { mutableStateOf(false) }
    var enviandoCodigo by remember { mutableStateOf(false) }
    var pendienteGuardar by remember { mutableStateOf(false) } // Bandera: guardar después de verificar
    var mostrarContraseña by remember { mutableStateOf(false) } // Toggle visibilidad contraseña

    val recuperacionRepository = remember { com.example.rest.data.repository.RecuperacionRepository() }
    val usuarioRepository = remember { com.example.rest.data.repository.UsuarioRepository() }

    // Función de validación de formato de correo
    fun esCorreoValido(correo: String): Boolean {
        if (!correo.contains("@")) return false
        val partes = correo.split("@")
        if (partes.size != 2) return false
        val dominio = partes[1]
        val sufijosValidos = listOf(".com", ".net", ".org", ".co", ".edu", ".io", ".gov", ".mil", ".int", ".info", ".biz")
        return sufijosValidos.any { dominio.endsWith(it) }
    }

    // Función reutilizable para guardar cambios del perfil
    fun guardarCambiosPerfil() {
        isSavingUser = true
        scope.launch {
            try {
                val getRes = usuarioRepository.obtenerUsuarioPorId(userId)
                if (getRes is com.example.rest.data.repository.UsuarioRepository.Result.Success) {
                    val currentUsuario = getRes.data

                    if (!com.example.rest.utils.SecurityUtils.verifyPassword(confirmarContraseña, currentUsuario.contraseña)) {
                        Toast.makeText(context, context.getString(R.string.err_password_mismatch), Toast.LENGTH_SHORT).show()
                        isSavingUser = false
                        return@launch
                    }

                    val fechaFormateada = if (fechaText.length == 8) {
                        "${fechaText.substring(0, 4)}-${fechaText.substring(4, 6)}-${fechaText.substring(6, 8)}"
                    } else {
                        fechaText
                    }

                    val updatedUsuario = currentUsuario.copy(
                        nombre = nombreText,
                        apellido = apellidoText.ifBlank { null },
                        correo = correoText,
                        fechaNacimiento = fechaFormateada
                    )

                    when (val updateRes = usuarioRepository.actualizarUsuario(userId, updatedUsuario)) {
                        is com.example.rest.data.repository.UsuarioRepository.Result.Success -> {
                            Toast.makeText(context, context.getString(R.string.toast_profile_updated), Toast.LENGTH_SHORT).show()
                            val prefs = com.example.rest.utils.PreferencesManager(context)
                            prefs.saveUserName(nombreText)
                            prefs.saveUserEmail(correoText)
                            correoOriginal = correoText // Actualizar el correo original
                        }
                        is com.example.rest.data.repository.UsuarioRepository.Result.Error -> {
                            Toast.makeText(context, context.getString(R.string.toast_error_saving_changes, updateRes.message), Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.toast_exception_saving_profile, e.message), Toast.LENGTH_SHORT).show()
            } finally {
                isSavingUser = false
                confirmarContraseña = ""
                pendienteGuardar = false
            }
        }
    }

    // Cargar datos del usuario desde Supabase al iniciar
    LaunchedEffect(Unit) {
        val prefs = com.example.rest.utils.PreferencesManager(context)
        userId = prefs.getUserId()
        if (userId != -1) {
            when (val res = usuarioRepository.obtenerUsuarioPorId(userId)) {
                is com.example.rest.data.repository.UsuarioRepository.Result.Success -> {
                    val usuario = res.data
                    nombreText = usuario.nombre
                    apellidoText = usuario.apellido ?: ""
                    correoText = usuario.correo
                    correoOriginal = usuario.correo // Guardar correo original
                    fechaText = usuario.fechaNacimiento.replace("-", "")
                }
                else -> {
                    Toast.makeText(context, context.getString(R.string.toast_error_loading_profile), Toast.LENGTH_SHORT).show()
                }
            }
        }
        isLoadingUserData = false
    }

    // Diálogo de verificación de correo
    if (mostrarDialogoVerificacion) {
        AlertDialog(
            onDismissRequest = {
                if (!enviandoCodigo) {
                    mostrarDialogoVerificacion = false
                    codigoIngresado = ""
                    codigoVerificado = false
                    errorCodigo = false
                }
            },
            title = { Text(stringResource(R.string.profile_verify_email_title), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.profile_verify_email_body, correoText),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = codigoIngresado,
                        onValueChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                codigoIngresado = it
                                errorCodigo = false
                            }
                        },
                        label = { Text(stringResource(R.string.recovery_code_placeholder)) },
                        singleLine = true,
                        enabled = !codigoVerificado && !enviandoCodigo,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primario,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    if (codigoVerificado) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.profile_verify_email_ok),
                                color = Color(0xFF4CAF50),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (errorCodigo) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.profile_verify_email_error),
                            color = Color(0xFFF44336),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                if (codigoVerificado) {
                    Button(
                        onClick = {
                            mostrarDialogoVerificacion = false
                            guardarCambiosPerfil()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text(stringResource(R.string.btn_confirm), color = Blanco)
                    }
                } else {
                    Button(
                        onClick = {
                            if (codigoIngresado == codigoGenerado) {
                                codigoVerificado = true
                                errorCodigo = false
                            } else {
                                errorCodigo = true
                            }
                        },
                        enabled = codigoIngresado.length == 6 && !enviandoCodigo,
                        colors = ButtonDefaults.buttonColors(containerColor = Primario)
                    ) {
                        Text(stringResource(R.string.profile_verify_email_btn))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!enviandoCodigo) {
                            mostrarDialogoVerificacion = false
                            codigoIngresado = ""
                            codigoVerificado = false
                            errorCodigo = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(stringResource(R.string.settings_edit_profile), color = Negro, fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.content_desc_back), tint = Negro)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Transparente)
                )
            },
            containerColor = FondoSecundario // Usar fondo claro secundario
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Fondo decorativo en la parte superior
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Primario, Color(0xFF80DEEA))
                            ),
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(60.dp)) // Espacio para que la foto superponga el header

                    // Foto de Perfil superpuesta
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(Blanco)
                            .border(4.dp, Blanco, CircleShape)
                            .clickable { showImageSourceDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImageBitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = profileImageBitmap!!.asImageBitmap(),
                                contentDescription = stringResource(R.string.profile_photo_title),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.settings_profile),
                                modifier = Modifier.size(80.dp),
                                tint = Color.Gray
                            )
                        }
                        
                        // Indicador de carga (Overlay)
                        if (isCheckingUpdate || isLoadingUserData) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = Primario, 
                                strokeWidth = 3.dp
                            )
                        }
                        
                        // Icono de pequeña cámara superpuesto
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-4).dp, y = (-4).dp)
                                .size(36.dp)
                                .background(Primario, CircleShape)
                                .border(2.dp, Blanco, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Blanco, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!isLoadingUserData) {
                        // Tarjeta Principal de Información
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Blanco),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.profile_info_title),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = TextUnit(18f, TextUnitType.Sp),
                                    color = TextoSecundario,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                // Campo Nombre
                                OutlinedTextField(
                                    value = nombreText,
                                    onValueChange = { nombreText = it },
                                    label = { Text(stringResource(R.string.register_name_placeholder)) },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Primario) },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Primario,
                                        unfocusedBorderColor = Color.LightGray
                                    ),
                                    singleLine = true
                                )

                                // Campo Apellido
                                OutlinedTextField(
                                    value = apellidoText,
                                    onValueChange = { apellidoText = it },
                                    label = { Text(stringResource(R.string.register_lastname_placeholder)) },
                                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = Primario) },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Primario,
                                        unfocusedBorderColor = Color.LightGray
                                    ),
                                    singleLine = true
                                )

                                // Campo Fecha
                                com.example.rest.ui.components.inputs.CampoFechaAutoFormato(
                                    value = fechaText,
                                    onValueChange = { fechaText = it },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                    // Nota: Para que el icono y forma entren aquí, CampoFechaAutoFormato tendría que exponer esas propiedades o tendríamos que modificar dicho componente. 
                                    // De lo contrario aplicará su diseño original.
                                )

                                // Campo Correo
                                OutlinedTextField(
                                    value = correoText,
                                    onValueChange = { correoText = it },
                                    label = { Text(stringResource(R.string.register_email_placeholder)) },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Primario) },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Primario,
                                        unfocusedBorderColor = Color.LightGray
                                    ),
                                    singleLine = true
                                )
                                
                                Divider(color = FondoSecundario, modifier = Modifier.padding(vertical = 8.dp))

                                // Campo Confirmar Contraseña (para validación)
                                Text(
                                    text = stringResource(R.string.profile_confirm_identity),
                                    fontSize = TextUnit(14f, TextUnitType.Sp),
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = confirmarContraseña,
                                    onValueChange = { confirmarContraseña = it },
                                    label = { Text(stringResource(R.string.register_confirm_password_placeholder)) },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Primario) },
                                    trailingIcon = {
                                        IconButton(onClick = { mostrarContraseña = !mostrarContraseña }) {
                                            Icon(
                                                imageVector = if (mostrarContraseña) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (mostrarContraseña) "Ocultar contraseña" else "Mostrar contraseña",
                                                tint = Color.Gray
                                            )
                                        }
                                    },
                                    visualTransformation = if (mostrarContraseña) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Primario,
                                        unfocusedBorderColor = Color.LightGray
                                    ),
                                    singleLine = true
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botón de Guardar Cambios
                        Button(
                            onClick = {
                                if (nombreText.isBlank() || correoText.isBlank() || fechaText.length != 8) {
                                    Toast.makeText(context, context.getString(R.string.err_empty_fields), Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                
                                // Validar formato de correo
                                if (!esCorreoValido(correoText)) {
                                    Toast.makeText(context, context.getString(R.string.toast_invalid_email_format), Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                
                                if (confirmarContraseña.isBlank()) {
                                    Toast.makeText(context, context.getString(R.string.err_empty_confirm_password), Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                // Si el correo cambió, solicitar verificación
                                if (correoText.trim().lowercase() != correoOriginal.trim().lowercase()) {
                                    enviandoCodigo = true
                                    codigoVerificado = false
                                    codigoIngresado = ""
                                    errorCodigo = false
                                    scope.launch {
                                        val resultado = recuperacionRepository.enviarCodigoVerificacionCorreo(correoText.trim())
                                        enviandoCodigo = false
                                        when (resultado) {
                                            is com.example.rest.data.repository.RecuperacionRepository.Result.Success -> {
                                                codigoGenerado = resultado.data
                                                mostrarDialogoVerificacion = true
                                                Toast.makeText(context, context.getString(R.string.toast_code_sent_to, correoText), Toast.LENGTH_SHORT).show()
                                            }
                                            is com.example.rest.data.repository.RecuperacionRepository.Result.Error -> {
                                                Toast.makeText(context, context.getString(R.string.toast_code_send_error, resultado.message), Toast.LENGTH_LONG).show()
                                            }
                                            else -> {}
                                        }
                                    }
                                } else {
                                    // Correo no cambió, guardar directamente
                                    guardarCambiosPerfil()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primario),
                            enabled = !isSavingUser,
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            if (isSavingUser) {
                                CircularProgressIndicator(color = Blanco, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Blanco)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.settings_edit_profile), color = Blanco, fontWeight = FontWeight.Bold, fontSize = TextUnit(18f, TextUnitType.Sp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botón para cambiar contraseña (Secundario/Tonal)
                        OutlinedButton(
                            onClick = {
                                context.startActivity(android.content.Intent(context, com.example.rest.features.home.CambiarContrasenaComposeActivity::class.java))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primario),
                            border = BorderStroke(1.dp, Primario)
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null, tint = Primario)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.change_password_title), color = Primario, fontWeight = FontWeight.Bold, fontSize = TextUnit(18f, TextUnitType.Sp))
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
        }
    }
}

fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}

fun saveImageToInternalStorage(context: Context, uri: Uri, userId: Int) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val outputStream = context.openFileOutput("profile_image_$userId.jpg", Context.MODE_PRIVATE)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun getProfileImageBitmap(context: Context, userId: Int): Bitmap? {
    val file = File(context.filesDir, "profile_image_$userId.jpg")
    return if (file.exists()) {
        try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            
            // Corregir rotación usando ExifInterface (Nativo de Android)
            val exifInterface = android.media.ExifInterface(file.absolutePath)
            val orientation = exifInterface.getAttributeInt(
                android.media.ExifInterface.TAG_ORIENTATION,
                android.media.ExifInterface.ORIENTATION_UNDEFINED
            )

            val matrix = android.graphics.Matrix()
            when (orientation) {
                android.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                android.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                android.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            if (orientation != android.media.ExifInterface.ORIENTATION_UNDEFINED && orientation != android.media.ExifInterface.ORIENTATION_NORMAL) {
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    } else {
        null
    }
}

/**
 * Subir foto de perfil a Supabase como Base64
 */
suspend fun subirFotoASupabase(context: Context, bitmap: Bitmap) {
    withContext(Dispatchers.IO) {
        try {
            // Obtener ID del usuario desde SharedPreferences
            val prefs = com.example.rest.utils.PreferencesManager(context)
            val userId = prefs.getUserId()
            
            if (userId == -1) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.toast_error_unidentified_user), Toast.LENGTH_SHORT).show()
                }
                return@withContext
            }
            
            // Redimensionar imagen si es muy grande (Max 800x800)
            val resizedBitmap = resizeBitmap(bitmap, 800, 800)

            // Convertir bitmap a Base64
            val byteArrayOutputStream = java.io.ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream) // Calidad 80 es suficiente
            val byteArray = byteArrayOutputStream.toByteArray()
            val base64String = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP) // NO_WRAP para evitar saltos de línea
            
            // Subir a Supabase
            val repository = com.example.rest.data.repository.UsuarioRepository()
            val result = repository.actualizarFotoPerfil(userId, "data:image/jpeg;base64,$base64String")
            
            withContext(Dispatchers.Main) {
                when (result) {
                    is com.example.rest.data.repository.UsuarioRepository.Result.Success -> {
                        Toast.makeText(context, context.getString(R.string.toast_photo_updated), Toast.LENGTH_SHORT).show()
                    }
                    is com.example.rest.data.repository.UsuarioRepository.Result.Error -> {
                        Toast.makeText(context, context.getString(R.string.toast_error_uploading_photo, result.message), Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, context.getString(R.string.toast_error_uploading_photo, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
        }
    }
}

/**
 * Descargar foto de perfil desde Supabase
 */
suspend fun descargarFotoDeSupabase(context: Context, userId: Int): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val repository = com.example.rest.data.repository.UsuarioRepository()
            val result = repository.obtenerUsuarioPorId(userId)
            
            when (result) {
                is com.example.rest.data.repository.UsuarioRepository.Result.Success -> {
                    val usuario = result.data
                    val fotoBase64 = usuario.fotoPerfil
                    
                    if (!fotoBase64.isNullOrBlank()) {
                        // Remover el prefijo "data:image/jpeg;base64," si existe
                        val base64Data = if (fotoBase64.startsWith("data:image")) {
                            fotoBase64.substring(fotoBase64.indexOf(",") + 1)
                        } else {
                            fotoBase64
                        }
                        
                        // Decodificar Base64 a Bitmap
                        val decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        
                        // Guardar localmente para uso futuro
                        if (bitmap != null) {
                            val file = File(context.filesDir, "profile_image_$userId.jpg")
                            FileOutputStream(file).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                            }
                        }
                        
                        bitmap
                    } else {
                        null
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e("PerfilDebug", "Error al descargar foto: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}

/**
 * Eliminar foto de perfil del almacenamiento local
 */
fun deleteProfileImage(context: Context, userId: Int) {
    try {
        val file = File(context.filesDir, "profile_image_$userId.jpg")
        if (file.exists()) {
            file.delete()
            Log.d("PerfilDebug", "Foto local eliminada correctamente")
        }
    } catch (e: Exception) {
        Log.e("PerfilDebug", "Error al eliminar foto local: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Eliminar foto de perfil de Supabase (establecer como null)
 */
suspend fun eliminarFotoDeSupabase(context: Context) {
    withContext(Dispatchers.IO) {
        try {
            val prefs = com.example.rest.utils.PreferencesManager(context)
            val userId = prefs.getUserId()
            
            if (userId == -1) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.toast_error_unidentified_user), Toast.LENGTH_SHORT).show()
                }
                return@withContext
            }
            
            // Actualizar con null en Supabase
            val repository = com.example.rest.data.repository.UsuarioRepository()
            val result = repository.actualizarFotoPerfil(userId, null)
            
            withContext(Dispatchers.Main) {
                when (result) {
                    is com.example.rest.data.repository.UsuarioRepository.Result.Success -> {
                        Log.d("PerfilDebug", "Foto eliminada de Supabase")
                    }
                    is com.example.rest.data.repository.UsuarioRepository.Result.Error -> {
                        Toast.makeText(context, context.getString(R.string.toast_error_server_delete, result.message), Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, context.getString(R.string.toast_error_photo_delete, e.message), Toast.LENGTH_LONG).show()
            }
        }
    }
}

/**
 * Redimensionar bitmap manteniendo la proporción
 */
fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    
    if (width <= maxWidth && height <= maxHeight) return bitmap

    val ratioBitmap = width.toFloat() / height.toFloat()
    val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

    var finalWidth = maxWidth
    var finalHeight = maxHeight

    if (ratioMax > ratioBitmap) {
        finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
    } else {
        finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
    }

    return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
}
