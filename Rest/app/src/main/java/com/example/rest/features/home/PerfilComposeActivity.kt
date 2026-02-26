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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rest.features.habits.*
import com.example.rest.ui.theme.*
import com.example.rest.ui.components.dialogs.DialogoNota

class PerfilComposeActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                PantallaPerfil(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(onBackClick: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
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
                            "Menu",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Blanco
                        )
                        
                        Divider(color = Blanco.copy(alpha = 0.3f))
                
                        // 1. Estadísticas
                        NavigationDrawerItem(
                            label = { Text("Estadísticas", style = MaterialTheme.typography.bodyLarge, color = Blanco) },
                            selected = false,
                            onClick = {
                                context.startActivity(android.content.Intent(context, com.example.rest.features.habits.EstadisticasComposeActivity::class.java))
                            },
                            icon = { Icon(Icons.Default.Star, null, tint = Blanco) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = Blanco.copy(alpha = 0.2f)
                            )
                        )
                        
                        // 2. Notas
                        NavigationDrawerItem(
                            label = { Text("Notas", style = MaterialTheme.typography.bodyLarge, color = Blanco) },
                            selected = false,
                            onClick = {
                                context.startActivity(android.content.Intent(context, com.example.rest.features.tools.NotasComposeActivity::class.java))
                            },
                            icon = { Icon(Icons.Default.Edit, null, tint = Blanco) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = Blanco.copy(alpha = 0.2f)
                            )
                        )
                        
                        // 3. Bloqueo de Aplicaciones
                        NavigationDrawerItem(
                            label = { Text("Bloqueo de Apps", style = MaterialTheme.typography.bodyLarge, color = Blanco) },
                            selected = false,
                            onClick = {
                                context.startActivity(android.content.Intent(context, com.example.rest.features.tools.BloqueoAppsComposeActivity::class.java))
                            },
                            icon = { Icon(Icons.Default.Lock, null, tint = Blanco) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = Blanco.copy(alpha = 0.2f)
                            )
                        )
                        
                        // 4. Horas de Descanso
                        NavigationDrawerItem(
                            label = { Text("Horas de Descanso", style = MaterialTheme.typography.bodyLarge, color = Blanco) },
                            selected = false,
                            onClick = {
                                context.startActivity(android.content.Intent(context, com.example.rest.features.tools.HoraDescansoComposeActivity::class.java))
                            },
                            icon = { Icon(Icons.Default.Face, null, tint = Blanco) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = Blanco.copy(alpha = 0.2f)
                            )
                        )
                        
                        // 5. Calendario
                        NavigationDrawerItem(
                            label = { Text("Calendario", style = MaterialTheme.typography.bodyLarge, color = Blanco) },
                            selected = false,
                            onClick = {
                                context.startActivity(android.content.Intent(context, com.example.rest.features.tools.CalendarioComposeActivity::class.java))
                            },
                            icon = { Icon(Icons.Default.DateRange, null, tint = Blanco) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = Blanco.copy(alpha = 0.2f)
                            )
                        )
                        
                        // 6. Control Parental
                        NavigationDrawerItem(
                            label = { Text("Control Parental", style = MaterialTheme.typography.bodyLarge, color = Blanco) },
                            selected = false,
                            onClick = {
                            },
                            icon = { Icon(Icons.Default.Person, null, tint = Blanco) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = Blanco.copy(alpha = 0.2f)
                            )
                        )

                        Spacer(modifier = Modifier.weight(1f)) // Empujar hacia abajo

                        Divider(color = Blanco.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                        // 7. Cerrar Sesión
                        NavigationDrawerItem(
                            label = { Text("Cerrar Sesión", style = MaterialTheme.typography.bodyLarge, color = Blanco) },
                            selected = false,
                            onClick = {
                                // Borrar sesión y volver al login
                                val sharedAction = context.getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE)
                                with(sharedAction.edit()) {
                                    clear()
                                    apply()
                                }
                                val intent = android.content.Intent(context, com.example.rest.features.auth.LoginComposeActivity::class.java)
                                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                                (context as? android.app.Activity)?.finish()
                            },
                            icon = { Icon(Icons.Default.ExitToApp, null, tint = Blanco) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).padding(bottom = 16.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = Blanco.copy(alpha = 0.2f)
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
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Blanco)
                            .border(4.dp, Color(0xFF004D40), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            modifier = Modifier.size(64.dp),
                            tint = Negro
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Obtener nombre de usuario
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val nombreUsuario = remember {
                        val sharedPref = context.getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE)
                        sharedPref.getString("NOMBRE_USUARIO", "Usuario") ?: "Usuario"
                    }

                    Text(
                        text = nombreUsuario,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Negro
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // ... (existing header code)
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    // SECCIÓN NOTA RECIENTE (Dinámica)
                    var ultimaNota by remember { mutableStateOf<com.example.rest.data.models.Nota?>(null) }
                    var mostrarDialogoNota by remember { mutableStateOf(false) }
                    val notaRepository = remember { com.example.rest.data.repository.NotaRepository() }
                    
                    // Función para cargar la última nota
                    fun cargarUltimaNota() {
                         scope.launch {
                             // Obtener ID real del usuario
                             val sharedPref = context.getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE)
                             val idUsuario = sharedPref.getInt("ID_USUARIO", -1)
                             
                             if (idUsuario != -1) {
                                 when (val result = notaRepository.obtenerUltimaNota(idUsuario)) {
                                     is com.example.rest.data.repository.NotaRepository.Result.Success<*> -> {
                                         @Suppress("UNCHECKED_CAST")
                                         ultimaNota = result.data as? com.example.rest.data.models.Nota
                                     }
                                     else -> {} // Manejar error si es necesario
                                 }
                             }
                         }
                    }

                    // Cargar al inicio
                    LaunchedEffect(Unit) {
                        cargarUltimaNota()
                    }
                    
                    if (ultimaNota != null) {
                        Text(
                            "Última Nota Modificada",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Negro,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Start
                        )
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(android.graphics.Color.parseColor(ultimaNota?.color ?: "#FFFFFF"))
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { mostrarDialogoNota = true }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = ultimaNota?.titulo ?: "Sin título",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Negro
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ultimaNota?.contenido ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Negro,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        // Placeholder si no hay notas
                         Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.9f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Hábitos Saludables",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Negro
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Gestiona tus hábitos diarios",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    
                    // Diálogo para editar la nota reciente sin salir
                    if (mostrarDialogoNota && ultimaNota != null) {
                        DialogoNota(
                            nota = ultimaNota,
                            onDismiss = { mostrarDialogoNota = false },
                            onConfirmar = { titulo, contenido, color ->
                                scope.launch {
                                    val fechaActual = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                                    }.format(java.util.Date())
                                    
                                    val notaActualizada = ultimaNota!!.copy(
                                        titulo = titulo,
                                        contenido = contenido,
                                        color = color,
                                        fecha_actualizacion = fechaActual
                                    )
                                    
                                    ultimaNota?.id?.let { id ->
                                        when (notaRepository.actualizarNota(id, notaActualizada)) {
                                            is com.example.rest.data.repository.NotaRepository.Result.Success<*> -> {
                                                android.widget.Toast.makeText(context, "Nota actualizada", android.widget.Toast.LENGTH_SHORT).show()
                                                cargarUltimaNota() // Recargar para ver cambios
                                            }
                                            is com.example.rest.data.repository.NotaRepository.Result.Error -> {
                                                android.widget.Toast.makeText(context, "Error al actualizar", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                                mostrarDialogoNota = false
                            }
                        )
                    }
                }
            }
        }
    }
}