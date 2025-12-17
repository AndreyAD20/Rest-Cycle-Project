package com.example.rest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.rest.ui.theme.*

class PerfilComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                PantallaPerfil(
                    onBackClick = { finish() },
                    onSettingsClick = { /* TODO: Navigate to settings */ },
                    onNotificationsClick = { /* TODO: Navigate to notifications */ },
                    onMenuClick = { /* TODO: Open drawer/menu */ }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    // Gradiente de fondo consistente con el tema
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(
            Color(0xFF80DEEA), // Cyan claro / Turquesa
            Primario // Cyan más oscuro
        ),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f) // Gradiente vertical para variar un poco
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Blanco,
                drawerContentColor = Negro
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                Spacer(Modifier.height(12.dp))
                
                Text(
                    "Menú",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                
                Divider()
                
                NavigationDrawerItem(
                    label = { Text("Estadísticas", style = MaterialTheme.typography.bodyLarge) },
                    selected = false,
                    onClick = {
                        val intent = android.content.Intent(context, EstadisticasComposeActivity::class.java)
                        androidx.core.content.ContextCompat.startActivity(context, intent, null)
                    },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Notas", style = MaterialTheme.typography.bodyLarge) },
                    selected = false,
                    onClick = {
                        val intent = android.content.Intent(context, NotasComposeActivity::class.java)
                        androidx.core.content.ContextCompat.startActivity(context, intent, null)
                    },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Bloqueo de aplicaciones", style = MaterialTheme.typography.bodyLarge) },
                    selected = false,
                    onClick = {
                        val intent = android.content.Intent(context, BloqueoAppsComposeActivity::class.java)
                        androidx.core.content.ContextCompat.startActivity(context, intent, null)
                    },
                    icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Hora de descanso", style = MaterialTheme.typography.bodyLarge) },
                    selected = false,
                    onClick = {
                        val intent = android.content.Intent(context, HoraDescansoComposeActivity::class.java)
                        androidx.core.content.ContextCompat.startActivity(context, intent, null)
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) }, // Requires extended icons, fallback to AccessTime if needed or check imports. Default icons set usually has basic ones.
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Tareas y actividades", style = MaterialTheme.typography.bodyLarge) },
                    selected = false,
                    onClick = {
                        val intent = android.content.Intent(context, TareasComposeActivity::class.java)
                        androidx.core.content.ContextCompat.startActivity(context, intent, null)
                    },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Calendario", style = MaterialTheme.typography.bodyLarge) },
                    selected = false,
                    onClick = {
                        val intent = android.content.Intent(context, CalendarioComposeActivity::class.java)
                        androidx.core.content.ContextCompat.startActivity(context, intent, null)
                    },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Regresar",
                                tint = Negro
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configuración",
                                tint = Negro
                            )
                        }
                        IconButton(onClick = onNotificationsClick) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = Negro
                            )
                        }
                        IconButton(onClick = {
                             scope.launch {
                                 drawerState.open()
                             }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú",
                                tint = Negro
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Perfil
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(160.dp)
                            .border(4.dp, Negro, CircleShape)
                            .clip(CircleShape)
                            .background(Blanco.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Foto de Perfil",
                            modifier = Modifier.size(100.dp),
                            tint = Negro
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "None", // Placeholder nombre
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Negro
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Opciones del Dashboard
                    // Usamos Cards para dar un look más moderno y "premium"
                    
                    DashboardCard(
                        title = "Futuros eventos",
                        subtitle = "Revisa tu agenda",
                        onClick = { /* TODO */ }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DashboardCard(
                        title = "Tiempo utilizando el celular hoy",
                        subtitle = "2h 15m", // Placeholder data
                        onClick = { /* TODO */ }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DashboardCard(
                        title = "Bloquea tus aplicaciones",
                        subtitle = "Modo concentración",
                        onClick = { /* TODO */ }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Blanco.copy(alpha = 0.5f) // Glassmorphism-ish
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Negro
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
            // Optional: Add arrow icon or status icon here
        }
    }
}
