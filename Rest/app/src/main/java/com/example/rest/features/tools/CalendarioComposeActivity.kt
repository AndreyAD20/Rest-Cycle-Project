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
import androidx.compose.material.icons.filled.List
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
import com.example.rest.ui.components.dialogs.DialogoEvento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
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
    var vistaActual by remember { mutableStateOf("dia") } // "dia" o "lista"
    
    // Estado de datos
    var eventos by remember { mutableStateOf<List<Evento>>(emptyList()) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var eventoAEditar by remember { mutableStateOf<Evento?>(null) } // Evento seleccionado para editar
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
                actions = {
                    // Botón para alternar vista
                    IconButton(onClick = { 
                        vistaActual = if (vistaActual == "dia") "lista" else "dia"
                    }) {
                        Icon(
                            imageVector = if (vistaActual == "dia") Icons.Default.List else Icons.Default.DateRange,
                            contentDescription = if (vistaActual == "dia") "Ver Lista" else "Ver Calendario",
                            tint = Negro
                        )
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
            if (vistaActual == "dia") {
                // Vista de Calendario (actual)
                VistaCalendarioDia(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    eventos = eventos,
                    onMonthChange = { newMonth ->
                        currentMonth = newMonth
                        // Mantener el mismo día del mes al cambiar de mes
                        try {
                            val dayOfMonth = selectedDate.dayOfMonth
                            val maxDayInNewMonth = newMonth.lengthOfMonth()
                            val newDay = if (dayOfMonth <= maxDayInNewMonth) dayOfMonth else maxDayInNewMonth
                            selectedDate = newMonth.atDay(newDay)
                        } catch (e: Exception) {
                            // Si hay algún error, usar el primer día del mes
                            selectedDate = newMonth.atDay(1)
                        }
                    },
                    onDateSelected = { selectedDate = it },
                    onEventoClick = { evento ->
                        eventoAEditar = evento
                        mostrarDialogo = true
                    }
                )
            } else {
                // Vista de Lista de Eventos Futuros
                VistaListaEventosFuturos(
                    eventos = eventos,
                    onEventoClick = { evento ->
                        eventoAEditar = evento
                        mostrarDialogo = true
                    }
                )
            }
            
            // Diálogo de creación/edición
            if (mostrarDialogo) {
                DialogoEvento(
                    initialDate = selectedDate,
                    evento = eventoAEditar, // Pasar evento si es edición
                    onDismiss = { 
                        mostrarDialogo = false
                        eventoAEditar = null
                    },
                    onConfirmar = { titulo, tipo, inicioIso, finIso, lat, long ->
                        mostrarDialogo = false
                        scope.launch(Dispatchers.IO) {
                            val sharedPref = context.getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE)
                            val idUsuario = sharedPref.getInt("ID_USUARIO", -1)
                            
                            if (idUsuario != -1) {
                                try {
                                    val res = if (eventoAEditar != null) {
                                        // Actualizar evento existente
                                        val eventoActualizado = Evento(
                                            id = eventoAEditar!!.id,
                                            idUsuario = idUsuario,
                                            titulo = titulo,
                                            tipo = tipo,
                                            fechaInicio = inicioIso,
                                            fechaFin = finIso,
                                            latitud = lat,
                                            longitud = long
                                        )
                                        SupabaseClient.api.actualizarEvento("eq.${eventoAEditar!!.id}", eventoActualizado)
                                    } else {
                                        // Validar fecha futura o actual
                                        val fechaInicio = try {
                                            LocalDateTime.parse(inicioIso.replace("Z", ""))
                                        } catch (e: Exception) {
                                            LocalDateTime.now()
                                        }
                                        
                                        val hoy = LocalDate.now()
                                        if (fechaInicio.toLocalDate().isBefore(hoy)) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "No puedes crear eventos en fechas pasadas", Toast.LENGTH_SHORT).show()
                                            }
                                            return@launch
                                        }

                                        // Crear nuevo evento
                                        val nuevoEvento = com.example.rest.data.models.EventoInput(
                                            idUsuario = idUsuario,
                                            titulo = titulo,
                                            tipo = tipo,
                                            fechaInicio = inicioIso,
                                            fechaFin = finIso,
                                            latitud = lat,
                                            longitud = long
                                        )
                                        SupabaseClient.api.crearEvento(nuevoEvento)
                                    }
                                    if (res.isSuccessful) {
                                        withContext(Dispatchers.Main) {
                                            val mensaje = if (eventoAEditar != null) "Evento actualizado" else "Evento creado"
                                            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                                            eventoAEditar = null
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
                                } catch (e: Exception) {
                                     withContext(Dispatchers.Main) {
                                         Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                     }
                                }
                            }
                        }
                    },
                    onEliminar = if (eventoAEditar != null) {
                        {
                            mostrarDialogo = false
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val res = SupabaseClient.api.eliminarEvento("eq.${eventoAEditar!!.id}")
                                    withContext(Dispatchers.Main) {
                                        if (res.isSuccessful) {
                                            Toast.makeText(context, "Evento eliminado", Toast.LENGTH_SHORT).show()
                                            eventoAEditar = null
                                            // Recargar eventos
                                            val sharedPref = context.getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE)
                                            val idUsuario = sharedPref.getInt("ID_USUARIO", -1)
                                            if (idUsuario != -1) {
                                                val refresh = SupabaseClient.api.obtenerEventosPorUsuario(idUsuario = "eq.$idUsuario")
                                                if (refresh.isSuccessful) {
                                                    eventos = refresh.body() ?: emptyList()
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
fun VistaCalendarioDia(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    eventos: List<Evento>,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onEventoClick: (Evento) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Calendar Header con Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onMonthChange(currentMonth.minusMonths(1)) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Primario.copy(alpha = 0.1f))
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        "Mes Anterior",
                        tint = Primario
                    )
                }
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))).uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Negro
                )
                IconButton(
                    onClick = { onMonthChange(currentMonth.plusMonths(1)) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Primario.copy(alpha = 0.1f))
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        "Mes Siguiente",
                        tint = Primario
                    )
                }
            }
        }

        // Days of Week Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF004D40).copy(alpha = 0.1f) // Fondo más sutil
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                listOf("DOM", "LUN", "MAR", "MIE", "JUE", "VIE", "SAB").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF004D40) // Color oscuro
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Grid
        CalendarGrid(
            yearMonth = currentMonth,
            selectedDate = selectedDate,
            eventos = eventos,
            onDateSelected = onDateSelected
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Event List Section for Selected Day
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
                            EventoItem(
                                evento = evento,
                                onClick = { onEventoClick(evento) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VistaListaEventosFuturos(
    eventos: List<Evento>,
    onEventoClick: (Evento) -> Unit = {}
) {
    // Filtrar y ordenar eventos futuros
    val eventosFuturos = remember(eventos) {
        val ahora = LocalDateTime.now()
        eventos.filter { evento ->
            try {
                // Manejar diferentes formatos de fecha ISO
                val fechaStr = evento.fechaInicio
                    .replace("Z", "")
                    .replace("+00:00", "")
                    .substringBefore("+")
                    .substringBefore(".")
                
                val fechaEvento = LocalDateTime.parse(fechaStr)
                fechaEvento.isAfter(ahora)
            } catch (e: Exception) {
                // Log para debugging (opcional)
                android.util.Log.e("CalendarioDebug", "Error parsing fecha: ${evento.fechaInicio}", e)
                false
            }
        }.sortedBy { it.fechaInicio }
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Próximos Eventos",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Negro
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (eventosFuturos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No tienes eventos pendientes",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(eventosFuturos) { evento ->
                        EventoItemConFecha(
                            evento = evento,
                            onClick = { onEventoClick(evento) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventoItem(evento: Evento, onClick: () -> Unit = {}) {
    // Formatear fecha y hora
    val (fechaTexto, horaTexto) = try {
        val fecha = LocalDateTime.parse(evento.fechaInicio.replace("Z", ""))
        val fmtFecha = DateTimeFormatter.ofPattern("dd MMM")
        val fmtHora = DateTimeFormatter.ofPattern("hh:mm a")
        Pair(fecha.format(fmtFecha), fecha.format(fmtHora))
    } catch (e: Exception) {
        Pair(evento.fechaInicio.take(10), "")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)), 
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
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
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$fechaTexto, $horaTexto",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
                if (!evento.tipo.isNullOrBlank()) {
                    Text(
                        text = evento.tipo,
                        style = MaterialTheme.typography.labelSmall,
                        color = Primario,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (evento.latitud != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Con ubicación",
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Con ubicación",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventoItemConFecha(evento: Evento, onClick: () -> Unit = {}) {
    // Formatear fecha y hora completa
    val (fechaTexto, horaTexto) = try {
        val fecha = LocalDateTime.parse(evento.fechaInicio.replace("Z", ""))
        val fmtFecha = DateTimeFormatter.ofPattern("dd MMM yyyy")
        val fmtHora = DateTimeFormatter.ofPattern("hh:mm a")
        Pair(fecha.format(fmtFecha), fecha.format(fmtHora))
    } catch (e: Exception) {
        Pair(evento.fechaInicio.take(10), "")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)), 
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
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
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$fechaTexto, $horaTexto",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
                if (!evento.tipo.isNullOrBlank()) {
                    Text(
                        text = evento.tipo,
                        style = MaterialTheme.typography.labelSmall,
                        color = Primario,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (evento.latitud != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Con ubicación",
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Con ubicación",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
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
    val today = LocalDate.now()

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(300.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(startOffset) { Box(modifier = Modifier.aspectRatio(1f)) }

        items(daysInMonth) { index ->
            val day = index + 1
            val date = yearMonth.atDay(day)
            val isSelected = date == selectedDate
            val isToday = date == today
            val hasEvent = eventos.any { 
                 try { it.fechaInicio.take(10) == date.toString() } catch (e: Exception) { false }
            }

            Card(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onDateSelected(date) },
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isSelected -> Primario
                        isToday -> Color(0xFFFDD835) // Amarillo más sólido
                        else -> Color.White.copy(alpha = 0.9f)
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 8.dp else if (isToday) 4.dp else 2.dp
                ),
                border = if (isToday && !isSelected) {
                    androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF57F17)) // Borde amarillo oscuro
                } else null
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = day.toString(),
                            color = when {
                                isSelected -> Blanco
                                isToday -> Color(0xFF6A1B00) // Marrón oscuro para contraste con amarillo
                                else -> Negro
                            },
                            fontWeight = when {
                                isSelected || isToday -> FontWeight.Bold
                                else -> FontWeight.Normal
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (hasEvent) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Blanco 
                                        else Color(0xFFFF5252)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventoItem(evento: Evento) {
    val horaTexto = try {
        evento.fechaInicio.substring(11, 16)
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
                            imageVector = Icons.Default.Place,
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
