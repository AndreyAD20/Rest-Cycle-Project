package com.example.rest.features.tools

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.rest.data.repository.LocalBlockingRepository
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.rest.BaseComposeActivity
import com.example.rest.ui.theme.*

class BloqueoAppsComposeActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                PantallaBloqueoApps(onBackClick = { finish() })
            }
        }
    }
}

data class AppBloqueo(
    val id: Int,
    val nombre: String,
    val iconColor: Color,
    val limitHours: Int = 0,
    val limitMinutes: Int = 0,
    val isBlocked: Boolean = false,
    val packageName: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBloqueoApps(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { LocalBlockingRepository(context) }

    // Gradiente de fondo
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF80DEEA), Primario),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    // Datos reales desde repositorio local
    val apps = remember { mutableStateListOf<AppBloqueo>() }
    
    // Cargar apps bloqueadas al inicio
    LaunchedEffect(Unit) {
        apps.addAll(repository.getBlockedApps())
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<AppBloqueo?>(null) }

    if (showAddDialog) {
        AppSelectionDialog(
            repository = repository,
            onDismiss = { showAddDialog = false },
            onAppsSelected = { newApps ->
                // Guardar cada app seleccionada y actualizar lista
                newApps.forEach { app ->
                    repository.saveBlockedApp(app)
                    if (!apps.any { it.packageName == app.packageName }) {
                        apps.add(app)
                    }
                }
                showAddDialog = false
            }
        )
    }

    if (showTimeDialog && selectedApp != null) {
        TimeLimitDialog(
            currentHours = selectedApp!!.limitHours,
            currentMinutes = selectedApp!!.limitMinutes,
            onDismiss = { showTimeDialog = false },
            onConfirm = { hours, minutes ->
                val index = apps.indexOfFirst { it.id == selectedApp!!.id }
                if (index != -1) {
                    val updatedApp = apps[index].copy(
                        limitHours = hours,
                        limitMinutes = minutes,
                        isBlocked = false 
                    )
                    apps[index] = updatedApp
                    repository.saveBlockedApp(updatedApp)
                }
                showTimeDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Bloqueo de Apps",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
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
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Encabezado descriptivo
                Card(
                    colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Configura límites diarios para tus aplicaciones y mejora tu productividad.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = Negro,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Icono Bloqueo",
                            modifier = Modifier.size(40.dp),
                            tint = Negro
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tus Aplicaciones",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Negro
                    )
                    
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = Color(0xFF00BCD4),
                        contentColor = Negro,
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, Negro, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, "Agregar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(apps) { app ->
                        AppBloqueoItem(
                            app = app,
                            onClick = {
                                selectedApp = app
                                showTimeDialog = true
                            },
                            onToggleBlock = { 
                                val index = apps.indexOfFirst { it.id == app.id }
                                if (index != -1) {
                                    apps[index] = apps[index].copy(
                                        isBlocked = !apps[index].isBlocked,
                                        limitHours = 0,
                                        limitMinutes = 0
                                    )
                                }
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

// ... (Existing AppBloqueoItem and TimeLimitDialog) ...

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionDialog(
    repository: LocalBlockingRepository,
    onDismiss: () -> Unit,
    onAppsSelected: (List<AppBloqueo>) -> Unit
) {
    // Cargar apps instaladas
    var installedApps by remember { mutableStateOf<List<AppBloqueo>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        // Ejecutar en background para no bloquear UI
        withContext(Dispatchers.IO) {
            val apps = repository.getInstalledApps()
            withContext(Dispatchers.Main) {
                installedApps = apps
            }
        }
    }
    
    var searchQuery by remember { mutableStateOf("") }
    val selected = remember { mutableStateListOf<AppBloqueo>() }

    // Filtrar apps
    val filteredApps = if (searchQuery.isBlank()) {
        installedApps
    } else {
        installedApps.filter { it.nombre.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Blanco,
            modifier = Modifier
                .fillMaxWidth()
                .height(650.dp) // Altura para buen espacio
                .clip(RoundedCornerShape(28.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                
                // Encabezado Premium
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Primario, Color(0xFF80DEEA))
                            )
                        )
                ) {
                    // Decoración de fondo (Círculos sutiles)
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .offset(x = (-40).dp, y = (-40).dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    )
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 30.dp, y = 30.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Agregar Apps",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Blanco
                            )
                        )
                        // Icono Búho o similar
                        Icon(
                            Icons.Default.Add, // Podríamos usar el drawable del buho si estuviera disponible como vector, o un icono representativo
                            contentDescription = null,
                            tint = Blanco.copy(alpha = 0.8f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Barra de Búsqueda Estilizada
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Primario) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Borrar", tint = Color.Gray)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Blanco,
                            unfocusedContainerColor = Blanco,
                            focusedBorderColor = Primario,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Lista de Apps como Tarjetas
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredApps) { app ->
                            val isSelected = selected.contains(app)
                            val borderColor = if (isSelected) Primario else Color.Transparent
                            val backgroundColor = if (isSelected) Primario.copy(alpha = 0.05f) else Color(0xFFF5F5F5)
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, borderColor, RoundedCornerShape(16.dp))
                                    .clickable {
                                        if (isSelected) selected.remove(app) else selected.add(app)
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = if(isSelected) 4.dp else 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AppIcon(packageName = app.packageName, modifier = Modifier.matchParentSize())
                                    }
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Text(
                                        text = app.nombre,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Negro,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    // Custom Check Indicator
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, if(isSelected) Primario else Color.Gray.copy(alpha=0.5f), CircleShape)
                                            .background(if(isSelected) Primario else Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Blanco,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (filteredApps.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No se encontraron apps", color = Color.Gray)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Botones de Acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = { onAppsSelected(selected) },
                            colors = ButtonDefaults.buttonColors(containerColor = Primario),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Text(
                                text = "Agregar (${selected.size})",
                                color = Blanco,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppBloqueoItem(
    app: AppBloqueo,
    onClick: () -> Unit,
    onToggleBlock: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp) // Compact padding
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icono Real
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(packageName = app.packageName, modifier = Modifier.matchParentSize())
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        app.nombre,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Negro
                    )
                    // Mostrar estado actual
                    if (app.isBlocked) {
                        Text(
                            "Bloqueado",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Red
                        )
                    } else if (app.limitHours > 0 || app.limitMinutes > 0) {
                        Text(
                            "${app.limitHours}h ${app.limitMinutes}m diarios",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF004D40)
                        )
                    } else {
                        Text(
                            "Sin límite",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Botón de Acción (Reloj/Editar)
            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .border(1.dp, Color.LightGray, CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar Límite",
                    tint = Negro,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun TimeLimitDialog(
    currentHours: Int,
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var hours by remember { mutableStateOf(currentHours.toString()) }
    var minutes by remember { mutableStateOf(currentMinutes.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Blanco,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Establecer Límite Diario",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Negro,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Horas
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) hours = it },
                        label = { Text("Horas") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Blanco,
                            unfocusedContainerColor = Blanco,
                            focusedBorderColor = Primario,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Negro,
                            unfocusedTextColor = Negro
                        )
                    )
                    
                    Text(
                        ":",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = Negro
                    )
                    
                    // Minutos
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { 
                            if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                                val valInt = it.toIntOrNull() ?: 0
                                if (valInt < 60) minutes = it 
                            }
                        },
                        label = { Text("Min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                         colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Blanco,
                            unfocusedContainerColor = Blanco,
                            focusedBorderColor = Primario,
                            unfocusedBorderColor = Color.Gray,
                             focusedTextColor = Negro,
                             unfocusedTextColor = Negro
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val h = hours.toIntOrNull() ?: 0
                            val m = minutes.toIntOrNull() ?: 0
                            onConfirm(h, m)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primario)
                    ) {
                        Text("Guardar", color = Blanco)
                    }
                }
            }
        }
    }
}

@Composable
fun AppIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val icon = remember(packageName) { mutableStateOf<android.graphics.drawable.Drawable?>(null) }
    
    LaunchedEffect(packageName) {
        withContext(Dispatchers.IO) {
            try {
                icon.value = context.packageManager.getApplicationIcon(packageName)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    if (icon.value != null) {
        AndroidView(
            factory = { ctx ->
                android.widget.ImageView(ctx).apply {
                    scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                }
            },
            update = { view ->
                view.setImageDrawable(icon.value)
            },
            modifier = modifier
        )
    } else {
        // Fallback
        Box(modifier = modifier.background(Color.Gray))
    }
}





