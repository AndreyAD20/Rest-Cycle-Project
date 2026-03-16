package com.example.rest.features.tools

import android.os.Bundle
import com.example.rest.BaseComposeActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rest.ui.theme.*
import com.example.rest.data.models.Evento
import com.example.rest.network.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TareasComposeActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                PantallaTareas(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(onBackClick: () -> Unit) {
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0D47A1),   // Azul profundo
            Color(0xFF00838F),   // Teal
            Color(0xFF00BFA5)    // Verde menta
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 2000f)
    )

    var eventosPendientes by remember { mutableStateOf<List<Evento>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        val prefs = com.example.rest.utils.PreferencesManager(context)
        val idUsuario = prefs.getUserId()

        if (idUsuario != -1) {
            withContext(Dispatchers.IO) {
                try {
                    val response = SupabaseClient.api.obtenerEventosPorUsuario("eq.$idUsuario")
                    if (response.isSuccessful) {
                        val todosEventos = response.body() ?: emptyList()
                        
                        // Filtrar eventos futuros o de hoy
                        val ahora = LocalDateTime.now().minusHours(2) // Margen de 2 horas para no borrar inmediato
                        
                        eventosPendientes = todosEventos.filter { evento ->
                            try {
                                // Formato esperado: 2026-01-01T10:00:00Z (Naive)
                                val fechaEvento = LocalDateTime.parse(evento.fechaInicio.replace("Z", ""))
                                fechaEvento.isAfter(ahora)
                            } catch (e: Exception) {
                                false // Si falla parseo, ignorar
                            }
                        }.sortedBy { it.fechaInicio } // Ordenar por fecha más próxima
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    cargando = false
                }
            }
        } else {
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Actividades Pendientes",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                },
                navigationIcon = {
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
            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Negro)
                }
            } else if (eventosPendientes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DateRange, 
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Blanco.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No tienes actividades pendientes",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Negro
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    
                    items(eventosPendientes) { evento ->
                        ActividadItem(evento)
                    }
                    
                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
            }
        }
    }
}

@Composable
fun ActividadItem(evento: Evento) {
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
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de color por tipo
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(40.dp)
                    .background(
                        when(evento.tipo) {
                           "Reunión" -> Color(0xFF5C6BC0)
                           "Trabajo" -> Color(0xFFEF5350)
                           "Salud" -> Color(0xFF66BB6A)
                           else -> Primario
                        }, 
                        RoundedCornerShape(3.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Negro
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange, // Reloj o Calendario
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$fechaTexto, $horaTexto",
                        style = MaterialTheme.typography.bodyMedium,
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
            }
        }
    }
}
