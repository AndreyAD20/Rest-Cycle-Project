package com.example.rest.features.parental

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import com.example.rest.data.models.AppVinculada
import com.example.rest.data.models.AppInstalada
import com.example.rest.data.models.AppInstaladaInput
import com.example.rest.data.models.AppBloqueoInput
import com.example.rest.network.SupabaseClient
import com.example.rest.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// Data class para combinar app instalada con su bloqueo
data class AppDelHijo(
    val id: Int,
    val nombre: String,
    val nombrePaquete: String,
    val tiempoLimite: Int,
    val bloqueada: Boolean,
    val bloqueadaPor: String,
    val tiempoUsado: Int = 0
)

class BloqueoRemotoHijoActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val idHijo = intent.getIntExtra("id_hijo", -1)
        val nombreHijo = intent.getStringExtra("nombre_hijo") ?: "Hijo"
        
        if (idHijo == -1) {
            Toast.makeText(this, "Error: No se encontrÃ³ el ID del hijo", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            TemaRest {
                PantallaBloqueoRemoto(
                    idHijo = idHijo,
                    nombreHijo = nombreHijo,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBloqueoRemoto(
    idHijo: Int,
    nombreHijo: String,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val appsEnlazadas = remember { mutableStateListOf<Pair<AppInstalada, AppVinculada?>>() }
    var isLoading by remember { mutableStateOf(true) }
    var idDispositivo by remember { mutableIntStateOf(-1) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // Cargar ID de dispositivo y luego las apps
    LaunchedEffect(idHijo, refreshTrigger) {
        isLoading = true
        try {
            val resDev = SupabaseClient.api.obtenerDispositivosPorUsuario("eq.$idHijo")
            if (resDev.isSuccessful && !resDev.body().isNullOrEmpty()) {
                idDispositivo = resDev.body()!![0].id ?: -1
                if (idDispositivo != -1) {
                    // 1. Obtener apps instaladas donde enlazada = true
                    val resEnlazadas = SupabaseClient.api.obtenerAppsInstaladasPorEstado("eq.$idDispositivo", "eq.true")
                    // 2. Obtener bloqueos de apps_vinculadas
                    val resBloqueos = SupabaseClient.api.obtenerAppsBloqueo("eq.$idDispositivo")
                    
                    val enlazadas = resEnlazadas.body() ?: emptyList()
                    val bloqueos = resBloqueos.body() ?: emptyList()
                    
                    appsEnlazadas.clear()
                    for (app in enlazadas) {
                        val bloqueo = bloqueos.find { it.nombrePaquete == app.nombrePaquete }
                        appsEnlazadas.add(Pair(app, bloqueo))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BloqueoRemoto", "Error cargando datos: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    // Convertir a lista plana para filtrar
    val appsList = appsEnlazadas.map { (app, bloqueo) ->
        AppDelHijo(
            id = app.id ?: 0,
            nombre = app.nombre,
            nombrePaquete = app.nombrePaquete,
            tiempoLimite = bloqueo?.tiempoLimite ?: 0,
            bloqueada = bloqueo?.bloqueada ?: false,
            bloqueadaPor = bloqueo?.bloqueadaPor ?: "hijo"
        )
    }

    var searchText by remember { mutableStateOf("") }
    val filteredApps = remember(appsList, searchText) {
        if (searchText.isEmpty()) appsList
        else appsList.filter { it.nombre.contains(searchText, ignoreCase = true) || it.nombrePaquete.contains(searchText, ignoreCase = true) }
    }

    val appsRestringidas = filteredApps.filter { it.bloqueada || it.tiempoLimite > 0 }.sortedBy { it.nombre }
    val appsOtras = filteredApps.filter { !it.bloqueada && it.tiempoLimite == 0 }.sortedBy { it.nombre }

    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF0D47A1), Color(0xFF00838F), Color(0xFF00BFA5)),
        start = Offset(0f, 0f),
        end = Offset(1000f, 2000f)
    )

    var showTimeDialog by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<AppDelHijo?>(null) }
    var showEnlazarDialog by remember { mutableStateOf(false) }
    var appsNoEnlazadas = remember { mutableStateListOf<AppInstalada>() }


    if (showTimeDialog && selectedApp != null) {
        val h = selectedApp!!.tiempoLimite / 60
        val m = selectedApp!!.tiempoLimite % 60
        TimeLimitDialogRemoto(
            currentHours = h,
            currentMinutes = m,
            onDismiss = { showTimeDialog = false },
            onConfirm = { newH, newM ->
                val newLimit = (newH * 60) + newM
                val appToUpdate = selectedApp!!
                
                scope.launch(Dispatchers.IO) {
                    try {
                        // Buscar el bloqueo existente
                        val resBloqueos = SupabaseClient.api.obtenerAppsBloqueo("eq.$idDispositivo")
                        val bloqueo = resBloqueos.body()?.find { it.nombrePaquete == appToUpdate.nombrePaquete }
                        
                        if (bloqueo != null) {
                            // Actualizar existente
                            val updates = mapOf(
                                "tiempolimite" to newLimit,
                                "bloqueada_por" to "padre",
                                "requiere_password" to (newLimit > 0),
                                "fecha_actualizacion" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }.format(Date())
                            )
                            SupabaseClient.api.actualizarAppVinculada(bloqueo.id.toString(), updates)
                        } else {
                            // Crear nuevo registro de bloqueo
                            val nuevoBloqueo = AppBloqueoInput(
                                idDispositivo = idDispositivo,
                                nombre = appToUpdate.nombre,
                                nombrePaquete = appToUpdate.nombrePaquete,
                                tiempoLimite = newLimit,
                                bloqueada = false,
                                bloqueadaPor = "padre",
                                requierePassword = newLimit > 0
                            )
                            SupabaseClient.api.crearAppBloqueo(nuevoBloqueo)
                        }
                        
                        // Actualizar UI
                        withContext(Dispatchers.Main) {
                            refreshTrigger++
                        }
                    } catch (e: Exception) {
                        Log.e("BloqueoRemoto", "Error actualizando limite: ${e.message}")
                    }
                }
                showTimeDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.Transparent)) {
                CenterAlignedTopAppBar(
                    title = { Text("Bloqueo - $nombreHijo", style = MaterialTheme.typography.titleLarge, color = Blanco) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, "Volver", tint = Blanco)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
                
                // Buscador
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    placeholder = { Text("Buscar aplicaciÃ³n...", color = Blanco.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Blanco) },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = Blanco)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blanco,
                        unfocusedBorderColor = Blanco.copy(alpha = 0.5f),
                        focusedTextColor = Blanco,
                        unfocusedTextColor = Blanco
                    )
                )
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brochaGradiente)
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Blanco)
            } else if (appsList.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "El hijo aÃºn no ha vinculado aplicaciones.",
                        color = Blanco,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Las apps aparecerÃ¡n cuando el hijo las comparta desde su dispositivo.",
                        color = Blanco.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                ) {
                    if (appsRestringidas.isNotEmpty()) {
                        item {
                            Text(
                                "RESTRINGIDAS (${appsRestringidas.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = Blanco,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(appsRestringidas) { app ->
                            ItemAppRemoto(
                                app = app,
                                onToggleBlock = { isChecked ->
                                    actualizarBloqueoEnBD(scope, app, isChecked, idDispositivo)
                                },
                                onEditLimit = {
                                    selectedApp = app
                                    showTimeDialog = true
                                }
                            )
                        }
                    }

                    if (appsOtras.isNotEmpty()) {
                        item {
                            Text(
                                "OTRAS APLICACIONES (${appsOtras.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = Blanco.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(appsOtras) { app ->
                            ItemAppRemoto(
                                app = app,
                                onToggleBlock = { isChecked ->
                                    actualizarBloqueoEnBD(scope, app, isChecked, idDispositivo)
                                },
                                onEditLimit = {
                                    selectedApp = app
                                    showTimeDialog = true
                                }
                            )
                        }

                        // BotÃ³n para enlazar mÃ¡s apps
                        item {
                            Button(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            val res = SupabaseClient.api.obtenerAppsInstaladasPorEstado("eq.$idDispositivo", "eq.false")
                                            val lista = res.body() ?: emptyList()
                                            appsNoEnlazadas.clear()
                                            appsNoEnlazadas.addAll(lista)
                                        } catch (e: Exception) {
                                            Log.e("BloqueoRemoto", "Error cargando apps: ${e.message}")
                                        }
                                    }
                                    showEnlazarDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Primario,
                                    contentColor = Blanco
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Enlazar mÃ¡s apps",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialog para enlazar apps
        if (showEnlazarDialog) {
            DialogEnlazarApps(
                apps = appsNoEnlazadas.toList(),
                onEnlazar = { app ->
                    // Enlazar app directamente
                    scope.launch(Dispatchers.IO) {
                        try {
                            // 1. Marcar como enlazada en apps_instaladas (o crear si no existe)
                            val updateRes = SupabaseClient.api.actualizarAppInstaladaPorPaquete(
                                "eq.$idDispositivo",
                                "eq.${app.nombrePaquete}",
                                mapOf("enlazada" to true)
                            )
                            
                            if (!updateRes.isSuccessful || updateRes.body().isNullOrEmpty()) {
                                // Si no existe en la tabla de instaladas, la creamos
                                SupabaseClient.api.crearAppInstalada(
                                    AppInstaladaInput(
                                        idDispositivo = idDispositivo,
                                        nombre = app.nombre,
                                        nombrePaquete = app.nombrePaquete,
                                        enlazada = true
                                    )
                                )
                            }

                            // 2. Crear registro en apps_bloqueo
                            SupabaseClient.api.crearAppBloqueo(
                                AppBloqueoInput(
                                    idDispositivo = idDispositivo,
                                    nombre = app.nombre,
                                    nombrePaquete = app.nombrePaquete,
                                    tiempoLimite = 0,
                                    bloqueada = false,
                                    bloqueadaPor = "hijo",
                                    requierePassword = false
                                )
                            )
                            // Recargar lista
                            withContext(Dispatchers.Main) {
                                try {
                                    val resEnlazadas = SupabaseClient.api.obtenerAppsInstaladasPorEstado("eq.$idDispositivo", "eq.true")
                                    val resBloqueos = SupabaseClient.api.obtenerAppsBloqueo("eq.$idDispositivo")
                                    val enlazadas = resEnlazadas.body() ?: emptyList()
                                    val bloqueos = resBloqueos.body() ?: emptyList()
                                    appsEnlazadas.clear()
                                    for (a in enlazadas) {
                                        val bloqueo = bloqueos.find { it.nombrePaquete == a.nombrePaquete }
                                        appsEnlazadas.add(Pair(a, bloqueo))
                                    }
                                } catch (e: Exception) {
                                    Log.e("BloqueoRemoto", "Error recargando: ${e.message}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("BloqueoRemoto", "Error enlazando app: ${e.message}")
                        }
                    }
                    showEnlazarDialog = false
                },
                onDismiss = { showEnlazarDialog = false }
            )
        }
    }
}

@Composable
fun ItemAppRemoto(
    app: AppDelHijo,
    onToggleBlock: (Boolean) -> Unit,
    onEditLimit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono (Color hash como fallback)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(app.nombrePaquete.hashCode().or(0xFF000000.toInt()))),
                contentAlignment = Alignment.Center
            ) {
                Text(app.nombre.firstOrNull()?.toString() ?: "?", color = Blanco, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(app.nombre, style = MaterialTheme.typography.titleMedium, color = Negro)
                
                if (app.bloqueada) {
                    Text("Totalmente Bloqueada", style = MaterialTheme.typography.labelSmall, color = Color.Red)
                } else if (app.tiempoLimite > 0) {
                    val h = app.tiempoLimite / 60
                    val m = app.tiempoLimite % 60
                    Text("LÃ­mite: ${h}h ${m}m", style = MaterialTheme.typography.labelSmall, color = Primario)
                } else {
                    Text("Sin restricciones", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                
                // Mostrar quiÃ©n puso el bloqueo
                if (app.bloqueadaPor == "padre") {
                    Text("Bloqueado por padre", style = MaterialTheme.typography.labelSmall, color = Color.Red.copy(alpha = 0.7f))
                }
                
                Text("Usado hoy: ${app.tiempoUsado} min", style = MaterialTheme.typography.labelSmall, color = Negro.copy(alpha = 0.6f))
            }

            // Interruptor de bloqueo total
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = app.bloqueada,
                    onCheckedChange = onToggleBlock,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Red,
                        checkedTrackColor = Color.Red.copy(alpha = 0.5f)
                    )
                )
                
                IconButton(onClick = onEditLimit) {
                    Icon(Icons.Default.Settings, "Configurar Tiempo", tint = Primario, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun TimeLimitDialogRemoto(
    currentHours: Int,
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var hours by remember { mutableIntStateOf(currentHours) }
    var minutes by remember { mutableIntStateOf(currentMinutes) }

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
                    text = "LÃ­mite de Tiempo",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Negro
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    WheelPickerRemoto((0..23).toList(), hours) { hours = it }
                    Text(" h ", fontWeight = FontWeight.Bold)
                    WheelPickerRemoto((0..59).toList(), minutes) { minutes = it }
                    Text(" min ", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
                    Button(onClick = { onConfirm(hours, minutes) }, colors = ButtonDefaults.buttonColors(containerColor = Primario)) {
                        Text("Guardar", color = Blanco)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPickerRemoto(items: List<Int>, initialValue: Int, onValueChange: (Int) -> Unit) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialValue)
    val flingBehavior = rememberSnapFlingBehavior(listState)
    
    val selectedIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    
    LaunchedEffect(selectedIndex) {
        if (selectedIndex < items.size) onValueChange(items[selectedIndex])
    }

    Box(modifier = Modifier.size(60.dp, 120.dp), contentAlignment = Alignment.Center) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 40.dp)
        ) {
            items(items) { valItem ->
                Box(modifier = Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = valItem.toString().padStart(2, '0'),
                        style = if (valItem == items.getOrNull(selectedIndex)) MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyLarge,
                        color = if (valItem == items.getOrNull(selectedIndex)) Negro else Color.LightGray
                    )
                }
            }
        }
        HorizontalDivider(modifier = Modifier.align(Alignment.Center).offset(y = (-20).dp), color = Primario, thickness = 1.dp)
        HorizontalDivider(modifier = Modifier.align(Alignment.Center).offset(y = (20).dp), color = Primario, thickness = 1.dp)
    }
}

private fun actualizarBloqueoEnBD(scope: kotlinx.coroutines.CoroutineScope, app: AppDelHijo, bloquear: Boolean, dispositivoId: Int) {
    scope.launch(Dispatchers.IO) {
        try {
            // Buscar el bloqueo existente
            val resBloqueos = SupabaseClient.api.obtenerAppsBloqueo("eq.$dispositivoId")
            val bloqueoExistente = resBloqueos.body()?.find { it.nombrePaquete == app.nombrePaquete }
            
            val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }.format(Date())
            
            if (bloqueoExistente != null) {
                // Actualizar existente
                val updates = mapOf(
                    "bloqueada" to bloquear,
                    "bloqueada_por" to "padre",
                    "requiere_password" to bloquear,
                    "fecha_actualizacion" to timestamp
                )
                SupabaseClient.api.actualizarAppVinculada(bloqueoExistente.id.toString(), updates)
                Log.d("BloqueoRemoto", "BD actualizada para ${app.nombre}: bloqueada=$bloquear")
            } else {
                // Crear nuevo registro de bloqueo
                val nuevoBloqueo = AppBloqueoInput(
                    idDispositivo = dispositivoId,
                    nombre = app.nombre,
                    nombrePaquete = app.nombrePaquete,
                    tiempoLimite = app.tiempoLimite,
                    bloqueada = bloquear,
                    bloqueadaPor = "padre",
                    requierePassword = bloquear
                )
                SupabaseClient.api.crearAppBloqueo(nuevoBloqueo)
                Log.d("BloqueoRemoto", "BD creada para ${app.nombre}: bloqueada=$bloquear")
            }
        } catch (e: Exception) {
            Log.e("BloqueoRemoto", "Error actualizando BD: ${e.message}")
        }
    }
}

@Composable
fun DialogEnlazarApps(
    apps: List<AppInstalada>,
    onEnlazar: (AppInstalada) -> Unit,
    onDismiss: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    
    val filteredApps = remember(apps, searchText) {
        if (searchText.isEmpty()) apps
        else apps.filter { it.nombre.contains(searchText, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Blanco,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Enlazar mÃ¡s apps",
                    style = MaterialTheme.typography.titleLarge,
                    color = Negro,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Selecciona las apps del hijo que deseas monitorear:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Negro.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Buscador
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar app...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay mÃ¡s apps para enlazar",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredApps) { app ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEnlazar(app) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Primario.copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Icono
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(app.nombrePaquete.hashCode().or(0xFF000000.toInt()))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            app.nombre.firstOrNull()?.toString() ?: "?",
                                            color = Blanco,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Text(
                                        text = app.nombre,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Negro,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Enlazar",
                                        tint = Primario
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cerrar", color = Color.Gray)
                    }
                }
            }
        }
    }
}
