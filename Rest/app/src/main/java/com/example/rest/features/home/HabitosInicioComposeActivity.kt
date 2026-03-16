package com.example.rest.features.home

import android.os.Bundle
import com.example.rest.BaseComposeActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.rest.data.models.Nota
import com.example.rest.data.models.Evento
import com.example.rest.data.repository.NotaRepository
import com.example.rest.network.SupabaseClient

class HabitosInicioComposeActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode = com.example.rest.utils.ThemeManager.isDarkMode(this)
            TemaRest(temaOscuro = isDarkMode) {
                PantallaInicioHub(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicioHub(onBackClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Estado para la imagen de perfil
    var profileImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    
    // Estados para Widgets
    var ultimaNota by remember { mutableStateOf<Nota?>(null) }
    var proximoEvento by remember { mutableStateOf<Evento?>(null) }
    var isWidgetsLoading by remember { mutableStateOf(true) }

    // Cargar imagen guardada al inicio, SIEMPRE verificar actualizaciones, y cargar Widgets
    LaunchedEffect(Unit) {
        val prefs = com.example.rest.utils.PreferencesManager(context)
        val userId = prefs.getUserId()
        
        if (userId != -1) {
            // 1. Cargar lo que tengamos localmente primero (para velocidad)
            val localBitmap = getProfileImageBitmap(context, userId)
            if (localBitmap != null) {
                profileImageBitmap = localBitmap
            }

            // 2. Intentar sincronizar foto con Supabase en segundo plano
            scope.launch {
                isCheckingUpdate = true
                val remoteBitmap = descargarFotoDeSupabase(context, userId)
                if (remoteBitmap != null) {
                    profileImageBitmap = remoteBitmap
                }
                isCheckingUpdate = false
            }
            
            // 3. Cargar datos de Widgets iniciales
            isWidgetsLoading = true
            try {
                // Fetch nota
                val notaRepo = NotaRepository()
                when (val notaResult = notaRepo.obtenerUltimaNota(userId)) {
                    is NotaRepository.Result.Success<*> -> {
                        ultimaNota = notaResult.data as? Nota
                    }
                    else -> { ultimaNota = null }
                }
                
                // Fetch evento
                val response = SupabaseClient.api.obtenerEventosPorUsuario("eq.$userId")
                if (response.isSuccessful) {
                    val todosEventos = response.body() ?: emptyList()
                    val ahora = LocalDateTime.now().minusHours(2)
                    
                    proximoEvento = todosEventos.filter { evento ->
                        try {
                            val fechaStr = evento.fechaInicio
                                .replace("Z", "")
                                .replace("+00:00", "")
                                .substringBefore("+")
                                .substringBefore(".")
                            val fechaEvento = LocalDateTime.parse(fechaStr)
                            fechaEvento.isAfter(ahora)
                        } catch (e: Exception) {
                            Log.e("HabitosInicio", "Error parsing fecha evento: ${evento.fechaInicio}", e)
                            false
                        }
                    }.minByOrNull { it.fechaInicio }
                } else {
                    proximoEvento = null
                }
            } catch (e: Exception) {
                Log.e("HabitosInicio", "Error cargando widgets iniciales", e)
                ultimaNota = null
                proximoEvento = null
            } finally {
                isWidgetsLoading = false
            }
        } else {
            isWidgetsLoading = false
        }
    }

    // Cerrar drawer cuando se vuelve a la actividad y recargar widgets
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    drawerState.close()
                }
                
                // Cargar Widgets (Última nota y Próximo Evento) cada vez que se resume la actividad
                val prefs = com.example.rest.utils.PreferencesManager(context)
                val userId = prefs.getUserId()
                if (userId != -1) {
                    scope.launch {
                        isWidgetsLoading = true
                        val notaRepo = NotaRepository()
                        
                        // Fetch nota
                        when (val notaResult = notaRepo.obtenerUltimaNota(userId)) {
                            is NotaRepository.Result.Success<*> -> {
                                ultimaNota = notaResult.data as? Nota
                            }
                            else -> { ultimaNota = null }
                        }
                        
                        // Fetch evento
                        try {
                            val response = SupabaseClient.api.obtenerEventosPorUsuario("eq.$userId")
                            if (response.isSuccessful) {
                                val todosEventos = response.body() ?: emptyList()
                                val ahora = LocalDateTime.now().minusHours(2)
                                
                                proximoEvento = todosEventos.filter { evento ->
                                    try {
                                        // Manejar diferentes formatos de fecha Supabase (igual que CalendarioComposeActivity)
                                        val fechaStr = evento.fechaInicio
                                            .replace("Z", "")
                                            .replace("+00:00", "")
                                            .substringBefore("+")
                                            .substringBefore(".")
                                        val fechaEvento = LocalDateTime.parse(fechaStr)
                                        fechaEvento.isAfter(ahora)
                                    } catch (e: Exception) {
                                        Log.e("HabitosInicio", "Error parsing fecha evento: ${evento.fechaInicio}", e)
                                        false
                                    }
                                }.minByOrNull { it.fechaInicio }
                            } else {
                                proximoEvento = null
                            }
                        } catch (e: Exception) {
                            Log.e("HabitosInicio", "Error cargando eventos", e)
                            proximoEvento = null
                        }
                        
                        isWidgetsLoading = false
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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

    // Gradiente idéntico al de Selección de Modos
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0D47A1),   // Azul profundo
            Color(0xFF00838F),   // Teal
            Color(0xFF00BFA5)    // Verde menta
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 2000f)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.Transparent,
                drawerContentColor = Negro
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFECE9E6), Color(0xFFFFFFFF)),
                                start = Offset(0f, 0f),
                                end = Offset(0f, 2000f)
                            )
                        )
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        Spacer(Modifier.height(12.dp))
                        
                        Text(
                            stringResource(R.string.menu_title),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Negro
                        )
                        
                        Divider(color = Negro.copy(alpha = 0.3f))
                
                        // 1. Estadísticas
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.menu_statistics), style = MaterialTheme.typography.bodyLarge.copy(fontSize = TextUnit(18f, TextUnitType.Sp)), color = Negro) },
                            selected = false,
                            onClick = {
                                context.startActivity(android.content.Intent(context, com.example.rest.features.habits.EstadisticasComposeActivity::class.java))
                            },
                            icon = { Icon(Icons.Default.Star, null, tint = Negro, modifier = Modifier.size(28.dp)) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = Negro.copy(alpha = 0.2f)
                            )
                        )
                        
                        // 2. Notas
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.menu_notes), style = MaterialTheme.typography.bodyLarge.copy(fontSize = TextUnit(18f, TextUnitType.Sp)), color = Negro) },
                            selected = false,
                            onClick = {
                                context.startActivity(android.content.Intent(context, com.example.rest.features.tools.NotasComposeActivity::class.java))
                            },
                            icon = { Icon(Icons.Default.Edit, null, tint = Negro, modifier = Modifier.size(28.dp)) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = Negro.copy(alpha = 0.2f)
                            )
                        )
                        
                        // 3. Bloqueo de Aplicaciones
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.menu_app_block), style = MaterialTheme.typography.bodyLarge.copy(fontSize = TextUnit(18f, TextUnitType.Sp)), color = Negro) },
                            selected = false,
                            onClick = {
                                context.startActivity(android.content.Intent(context, com.example.rest.features.tools.BloqueoAppsComposeActivity::class.java))
                            },
                            icon = { Icon(Icons.Default.Lock, null, tint = Negro, modifier = Modifier.size(28.dp)) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = Negro.copy(alpha = 0.2f)
                            )
                        )
                        
                        // 4. Horas de Descanso
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.menu_rest_hours), style = MaterialTheme.typography.bodyLarge.copy(fontSize = TextUnit(18f, TextUnitType.Sp)), color = Negro) },
                            selected = false,
                            onClick = {
                                context.startActivity(android.content.Intent(context, com.example.rest.features.tools.HoraDescansoComposeActivity::class.java))
                            },
                            icon = { Icon(Icons.Default.Face, null, tint = Negro, modifier = Modifier.size(28.dp)) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = Negro.copy(alpha = 0.2f)
                            )
                        )
                        
                        // 5. Calendario
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.menu_calendar), style = MaterialTheme.typography.bodyLarge.copy(fontSize = TextUnit(18f, TextUnitType.Sp)), color = Negro) },
                            selected = false,
                            onClick = {
                                context.startActivity(android.content.Intent(context, com.example.rest.features.tools.CalendarioComposeActivity::class.java))
                            },
                            icon = { Icon(Icons.Default.DateRange, null, tint = Negro, modifier = Modifier.size(28.dp)) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = Negro.copy(alpha = 0.2f)
                            )
                        )
                        
                        // 6. Control Parental
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.menu_parental_control), style = MaterialTheme.typography.bodyLarge.copy(fontSize = TextUnit(18f, TextUnitType.Sp)), color = Negro) },
                            selected = false,
                            onClick = {
                            },
                            icon = { Icon(Icons.Default.Person, null, tint = Negro, modifier = Modifier.size(28.dp)) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = Negro.copy(alpha = 0.2f)
                            )
                        )

                        Spacer(modifier = Modifier.weight(1f)) // Empujar hacia abajo

                        Divider(color = Negro.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                        // 7. Cerrar Sesión
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.menu_logout), style = MaterialTheme.typography.bodyLarge.copy(fontSize = TextUnit(18f, TextUnitType.Sp)), color = Negro) },
                            selected = false,
                            onClick = {
                                // Limpiar fotos de perfil locales
                                try {
                                    context.filesDir.listFiles()?.forEach { file ->
                                        if (file.name.startsWith("profile_image_")) {
                                            file.delete()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("PerfilDebug", "Error al limpiar fotos: ${e.message}")
                                }
                                
                                // Borrar sesión y volver al login
                                val preferencesManager = com.example.rest.utils.PreferencesManager(context)
                                preferencesManager.clearPreferences()
                                val intent = android.content.Intent(context, com.example.rest.features.auth.LoginComposeActivity::class.java)
                                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                                (context as? android.app.Activity)?.finish()
                            },
                            icon = { Icon(Icons.Default.ExitToApp, null, tint = Negro, modifier = Modifier.size(28.dp)) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).padding(bottom = 16.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = Negro.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
               CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.home_healthy_habits), color = Color.White, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, "Regresar", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
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
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    
                    // Obtener nombre de usuario
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val nombreUsuario = remember {
                        val prefs = com.example.rest.utils.PreferencesManager(context)
                        val rawName = prefs.getUserName() ?: context.getString(R.string.fallback_user_name)
                        rawName.substringBefore(" ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    }

                    // Glassmorphic Profile Header
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.5f))
                                    .border(2.dp, Color.White, CircleShape)
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
                                        modifier = Modifier.size(40.dp),
                                        tint = Negro
                                    )
                                }
                                
                                if (isCheckingUpdate) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color(0xFF00BFA5), 
                                        strokeWidth = 2.5.dp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(20.dp))
                            
                            Column {
                                Text(
                                    text = stringResource(R.string.greeting_hello),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Negro.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "$nombreUsuario!",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = Negro
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- INICIO DE WIDGETS ---
                    if (isWidgetsLoading) {
                        CircularProgressIndicator(color = Negro)
                    } else {
                        // Widget: Próximo Evento
                        if (proximoEvento != null) {
                            val evento = proximoEvento!!
                            val (fechaTexto, horaTexto) = try {
                                val fechaStr = evento.fechaInicio
                                    .replace("Z", "")
                                    .replace("+00:00", "")
                                    .substringBefore("+")
                                    .substringBefore(".")
                                val fecha = LocalDateTime.parse(fechaStr)
                                val fmtFecha = DateTimeFormatter.ofPattern("dd MMM")
                                val fmtHora = DateTimeFormatter.ofPattern("hh:mm a")
                                Pair(fecha.format(fmtFecha), fecha.format(fmtHora))
                            } catch (e: Exception) {
                                Pair(evento.fechaInicio.take(10), "")
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.home_next_event_label),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                                    color = Negro.copy(alpha = 0.7f),
                                    modifier = Modifier.align(Alignment.CenterStart)
                                )
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        context.startActivity(android.content.Intent(context, com.example.rest.features.tools.CalendarioComposeActivity::class.java))
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .background(Color(0xFF00BFA5).copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Event, contentDescription = null, tint = Color(0xFF00BFA5), modifier = Modifier.size(32.dp))
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(text = evento.titulo, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color(0xFF263238), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "$fechaTexto • $horaTexto", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = Color(0xFF78909C))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }

                        // Widget: Última Nota
                        if (ultimaNota != null) {
                            val nota = ultimaNota!!
                            val colorFondoNota = try {
                                Color(android.graphics.Color.parseColor(nota.color ?: "#FFFFFF"))
                            } catch (e: Exception) {
                                Blanco.copy(alpha = 0.9f)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.home_last_note_label),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                                    color = Negro.copy(alpha = 0.7f),
                                    modifier = Modifier.align(Alignment.CenterStart)
                                )
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = colorFondoNota),
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        context.startActivity(android.content.Intent(context, com.example.rest.features.tools.NotasComposeActivity::class.java))
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(Color.White.copy(alpha = 0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.StickyNote2, contentDescription = null, tint = Color(0xFF263238).copy(alpha = 0.8f))
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        nota.titulo?.let {
                                            Text(text = it, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF263238), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                        nota.contenido?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF455A64),
                                                maxLines = 3,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}
