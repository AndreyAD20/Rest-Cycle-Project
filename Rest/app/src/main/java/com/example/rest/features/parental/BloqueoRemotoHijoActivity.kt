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
import com.example.rest.network.SupabaseClient
import com.example.rest.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class BloqueoRemotoHijoActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val idHijo = intent.getIntExtra("id_hijo", -1)
        val nombreHijo = intent.getStringExtra("nombre_hijo") ?: "Hijo"
        
        if (idHijo == -1) {
            Toast.makeText(this, "Error: No se encontró el ID del hijo", Toast.LENGTH_SHORT).show()
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
    val apps = remember { mutableStateListOf<AppVinculada>() }
    var isLoading by remember { mutableStateOf(true) }
    var idDispositivo by remember { mutableIntStateOf(-1) }

    // Cargar ID de dispositivo y luego las apps
    LaunchedEffect(idHijo) {
        isLoading = true
        try {
            val resDev = SupabaseClient.api.obtenerDispositivosPorUsuario("eq.$idHijo")
            if (resDev.isSuccessful && !resDev.body().isNullOrEmpty()) {
                idDispositivo = resDev.body()!![0].id ?: -1
                if (idDispositivo != -1) {
                    val resApps = SupabaseClient.api.obtenerAppsVinculadas("eq.$idDispositivo")
                    if (resApps.isSuccessful) {
                        apps.clear()
                        apps.addAll(resApps.body() ?: emptyList())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BloqueoRemoto", "Error cargando datos: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF0D47A1), Color(0xFF00838F), Color(0xFF00BFA5)),
        start = Offset(0f, 0f),
        end = Offset(1000f, 2000f)
    )

    var showTimeDialog by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<AppVinculada?>(null) }

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
                apps[apps.indexOf(appToUpdate)] = appToUpdate.copy(tiempoLimite = newLimit, bloqueada = false)
                
                scope.launch(Dispatchers.IO) {
                    try {
                        val updates = mapOf(
                            "tiempolimite" to newLimit,
                            "bloqueada" to false,
                            "fecha_actualizacion" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }.format(Date())
                        )
                        SupabaseClient.api.actualizarAppVinculada(appToUpdate.id.toString(), updates)
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
            CenterAlignedTopAppBar(
                title = { Text("Bloqueo - $nombreHijo", style = MaterialTheme.typography.titleLarge, color = Blanco) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Blanco)
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
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Blanco)
            } else if (apps.isEmpty()) {
                Text(
                    "El hijo aún no ha vinculado aplicaciones.",
                    color = Blanco,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                ) {
                    items(apps) { app ->
                        ItemAppRemoto(
                            app = app,
                            onToggleBlock = { isChecked ->
                                val updated = app.copy(bloqueada = isChecked)
                                apps[apps.indexOf(app)] = updated
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val updates = mapOf(
                                            "bloqueada" to isChecked,
                                            "fecha_actualizacion" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }.format(Date())
                                        )
                                        SupabaseClient.api.actualizarAppVinculada(app.id.toString(), updates)
                                    } catch (e: Exception) {
                                        Log.e("BloqueoRemoto", "Error al bloquear: ${e.message}")
                                    }
                                }
                            },
                            onEditLimit = {
                                selectedApp = app
                                showTimeDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemAppRemoto(
    app: AppVinculada,
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
                    .background(Color((app.nombrePaquete ?: "").hashCode().or(0xFF000000.toInt()))),
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
                    Text("Límite: ${h}h ${m}m", style = MaterialTheme.typography.labelSmall, color = Primario)
                } else {
                    Text("Sin restricciones", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                
                Text("Usado hoy: ${app.tiempoUsadoHoy} min", style = MaterialTheme.typography.labelSmall, color = Negro.copy(alpha = 0.6f))
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
                    text = "Límite de Tiempo",
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
