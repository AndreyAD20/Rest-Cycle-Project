package com.example.rest.features.tools

import android.os.Bundle
import com.example.rest.BaseComposeActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rest.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.rest.data.models.Evento
import com.example.rest.network.SupabaseClient
import com.example.rest.ui.components.DialogoEvento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import android.widget.Toast
import kotlinx.coroutines.launch

class CalendarioComposeActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                PantallaCalendario(onBackClick = { finish() })
            }
        }
    }
}

// ... (content removed)

// ... (Header existing code) ...

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCalendario(onBackClick: () -> Unit) {
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF80DEEA), Primario),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    
    // Estado de datos
    var eventos by remember { mutableStateOf<List<Evento>>(emptyList()) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Cargar eventos del usuario
    LaunchedEffect(Unit) {
        val sharedPref = context.getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE)
        val idUsuario = sharedPref.getInt("ID_USUARIO", -1)
        
        if (idUsuario != -1) {
            withContext(Dispatchers.IO) {
                try {
                    val response = SupabaseClient.api.obtenerEventosPorUsuario(idUsuario = "eq.$idUsuario")
                    if (response.isSuccessful) {
                        eventos = response.body() ?: emptyList()
                    }
                } catch (e: Exception) {
                    // Error silencioso o log
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calendario", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Regresar", tint = Negro)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogo = true },
                containerColor = Color(0xFF00BCD4),
                contentColor = Negro
            ) {
                Icon(Icons.Default.Add, "Nuevo Evento")
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Calendar Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.Default.ArrowBack, "Mes Anterior")
                    }
                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Negro
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.Default.ArrowForward, "Mes Siguiente")
                    }
                }

                // Days of Week Header
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("DOM", "LUN", "MAR", "MIE", "JUE", "VIE", "SAB").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar Grid
                CalendarGrid(
                    yearMonth = currentMonth,
                    selectedDate = selectedDate,
                    eventos = eventos,
                    onDateSelected = { selectedDate = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Event List Section
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Agenda - ${selectedDate.format(DateTimeFormatter.ofPattern("dd MMM"))}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Negro
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Filtrar eventos por fecha seleccionada
                        val eventosDia = eventos.filter { 
                            try {
                                // Parse naive ISO prefix yyyy-MM-dd
                                it.fechaInicio.take(10) == selectedDate.toString()
                            } catch (e: Exception) { false }
                        }
                        
                        if (eventosDia.isEmpty()) {
                            Text(
                                "No hay eventos para este día",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(eventosDia) { evento ->
                                    EventoItem(evento)
                                }
                            }
                        }
                    }
                }
            }
            
            // Diálogo de creación
            if (mostrarDialogo) {
                DialogoEvento(
                    onDismiss = { mostrarDialogo = false },
                    onConfirmar = { titulo, tipo, inicioIso, finIso, lat, long ->
                        mostrarDialogo = false
                        scope.launch(Dispatchers.IO) {
                            val sharedPref = context.getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE)
                            val idUsuario = sharedPref.getInt("ID_USUARIO", -1)
                            
                            if (idUsuario != -1) {
                                val nuevoEvento = Evento(
                                    idUsuario = idUsuario,
                                    titulo = titulo,
                                    tipo = tipo,
                                    fechaInicio = inicioIso,
                                    fechaFin = finIso,
                                    latitud = lat,
                                    longitud = long
                                )
                                try {
                                    val res = SupabaseClient.api.crearEvento(nuevoEvento)
                                    if (res.isSuccessful) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Evento creado", Toast.LENGTH_SHORT).show()
                                            // Recargar eventos
                                            val refresh = SupabaseClient.api.obtenerEventosPorUsuario(idUsuario = "eq.$idUsuario")
                                            if (refresh.isSuccessful) {
                                                eventos = refresh.body() ?: emptyList()
                                            }
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            val errorBody = res.errorBody()?.string() ?: "Error desconocido"
                                            Toast.makeText(context, "Error ${res.code()}: $errorBody", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                     withContext(Dispatchers.Main) {
                                         Toast.makeText(context, "Error al crear: ${e.message}", Toast.LENGTH_SHORT).show()
                                     }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    eventos: List<Evento>,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val startOffset = yearMonth.atDay(1).dayOfWeek.value % 7

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(280.dp)
    ) {
        items(startOffset) { Box(modifier = Modifier.aspectRatio(1f)) }

        items(daysInMonth) { index ->
            val day = index + 1
            val date = yearMonth.atDay(day)
            val isSelected = date == selectedDate
            val hasEvent = eventos.any { 
                 try { it.fechaInicio.take(10) == date.toString() } catch (e: Exception) { false }
            }

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Primario else Color.Transparent)
                    .clickable { onDateSelected(date) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = day.toString(),
                        color = if (isSelected) Blanco else Negro,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    if (hasEvent) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Blanco else Color(0xFFFF5252))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventoItem(evento: Evento) {
    // Formatear hora para mostrar (ej: 02:30 PM)
    val horaTexto = try {
        // Asumiendo formato ISO con Z: 2026-01-23T14:30:00Z -> Parsear y mostrar hora local
        // Simplificación: parsear solo la hora del string si viene en ISO estricto
        // O mejor, usar un formatter simple sobre el string truncado
        val isoPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'") // Naive zulu
        // Esta parte puede ser frágil con zonas horarias reales, ajustar según necesidad
        // Para visualización rápida:
        evento.fechaInicio.substring(11, 16) // Toma HH:mm crudo (UTC) - Idealmente convertir a local
    } catch (e: Exception) { "??" }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)), 
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .background(
                        when(evento.tipo) {
                           "Reunión" -> Color(0xFF5C6BC0)
                           "Trabajo" -> Color(0xFFEF5350)
                           "Salud" -> Color(0xFF66BB6A)
                           else -> Primario
                        }, 
                        RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Negro
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$horaTexto - ${evento.tipo}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    if (evento.latitud != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Place,
                            contentDescription = "Con ubicación",
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}





