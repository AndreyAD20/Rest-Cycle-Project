package com.example.rest.features.home

import android.os.Bundle
import com.example.rest.BaseComposeActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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




    // Cerrar drawer cuando se vuelve a la actividad
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    drawerState.close()
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

    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Primario, Color(0xFF80DEEA)),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
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
                                colors = listOf(Primario, Color(0xFF80DEEA)),
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
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu", tint = Negro)
                        }
                    },
                    actions = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, "Regresar", tint = Negro)
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
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(CircleShape)
                            .background(Blanco)
                            .border(4.dp, Color(0xFF004D40), CircleShape)
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
                                modifier = Modifier.size(120.dp),
                                tint = Negro
                            )
                        }
                        
                        // Indicador de carga (Overlay)
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = Primario, 
                                strokeWidth = 3.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Obtener nombre de usuario
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val nombreUsuario = remember {
                        val prefs = com.example.rest.utils.PreferencesManager(context)
                        prefs.getUserName() ?: context.getString(R.string.fallback_user_name)
                    }

                    Text(
                        text = nombreUsuario,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Negro
                    )



                    // ... (existing header code)
                    

                }
            }
        }
    }
}
