package com.example.rest

import android.os.Bundle
import androidx.activity.ComponentActivity
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

class CalendarioComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                PantallaCalendario(onBackClick = { finish() })
            }
        }
    }
}

data class EventoCalendario(
    val id: Int,
    val titulo: String,
    val fecha: LocalDate,
    val hora: String
)

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

    // Mock Data
    val eventos = remember {
        listOf(
            EventoCalendario(1, "Reunión avances", LocalDate.now().plusDays(2), "09:00 AM"),
            EventoCalendario(2, "Entrega de proyecto", LocalDate.now().plusDays(5), "02:00 PM"),
            EventoCalendario(3, "Partido de fútbol", LocalDate.now().plusDays(10), "05:00 PM"),
            EventoCalendario(4, "Cena familiar", LocalDate.now(), "08:00 PM")
        )
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
                onClick = { /* TODO */ },
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
                            text = "Recordatorios - ${selectedDate.format(DateTimeFormatter.ofPattern("dd MMM"))}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Negro
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val eventosDia = eventos.filter { it.fecha == selectedDate }
                        
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
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    eventos: List<EventoCalendario>,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1).dayOfWeek.value % 7 // adjust for Sunday start if needed logic matches standard
    // In Java Time, Monday = 1, Sunday = 7. 
    // If our grid starts Sunday, we need to shift.
    // DOM LUN MAR ...
    // If first day is Monday (1), we skip 1 slot? No, Sunday is index 0. Monday is index 1.
    // standard: 1=Mon, ..., 7=Sun.
    // We want 7=Sun to be index 0? Or 7=Sun -> 0, 1=Mon -> 1?
    // Let's assume Grid DOM=0.
    // If first day is Tues(2), we need 2 empty slots (Sun, Mon).
    // offset = dayOfWeek.value % 7 gives: 7(Sun)->0, 1(Mon)->1, ... perfect.
    
    val startOffset = yearMonth.atDay(1).dayOfWeek.value % 7

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(280.dp) // Fixed height to avoid scroll conflict
    ) {
        // Empty slots
        items(startOffset) {
            Box(modifier = Modifier.aspectRatio(1f))
        }

        // Days
        items(daysInMonth) { index ->
            val day = index + 1
            val date = yearMonth.atDay(day)
            val isSelected = date == selectedDate
            val hasEvent = eventos.any { it.fecha == date }

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
fun EventoItem(evento: EventoCalendario) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)), // Light Cyan
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
                    .background(Primario, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Negro
                )
                Text(
                    text = evento.hora,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
        }
    }
}
