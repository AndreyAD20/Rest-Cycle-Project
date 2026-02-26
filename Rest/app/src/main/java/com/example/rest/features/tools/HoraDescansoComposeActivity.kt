package com.example.rest.features.tools

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.rest.BaseComposeActivity
import com.example.rest.data.models.*
import com.example.rest.data.repository.HorarioRepository
import com.example.rest.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

class HoraDescansoComposeActivity : BaseComposeActivity() {
    
    private val horarioRepository = HorarioRepository()
    // TODO: Obtener el ID del usuario de la sesión actual
    private val idUsuarioActual = 1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                var horarios by remember { mutableStateOf<List<Horario>>(emptyList()) }
                var dias by remember { mutableStateOf<List<Dia>>(emptyList()) }
                var medidas by remember { mutableStateOf<List<Medida>>(emptyList()) }
                var dispositivos by remember { mutableStateOf<List<Dispositivo>>(emptyList()) }
                var diasPorHorario by remember { mutableStateOf<Map<Int, List<Int>>>(emptyMap()) }
                var cargando by remember { mutableStateOf(true) }
                var mostrarDialogoCrear by remember { mutableStateOf(false) }
                
                // Cargar datos al iniciar
                LaunchedEffect(Unit) {
                    cargarDatos { horariosData, diasData, medidasData, dispositivosData, diasHorarioMap ->
                        horarios = horariosData
                        dias = diasData
                        medidas = medidasData
                        dispositivos = dispositivosData
                        diasPorHorario = diasHorarioMap
                        cargando = false
                    }
                }
                
                PantallaHorasDescanso(
                    horarios = horarios,
                    dias = dias,
                    medidas = medidas,
                    diasPorHorario = diasPorHorario,
                    cargando = cargando,
                    onBackClick = { finish() },
                    onAgregarClick = { 
                        if (dispositivos.isEmpty()) {
                            Toast.makeText(this, "No tienes dispositivos registrados", Toast.LENGTH_SHORT).show()
                        } else {
                            mostrarDialogoCrear = true
                        }
                    },
                    onEliminarHorario = { horario ->
                        lifecycleScope.launch {
                            eliminarHorario(horario) {
                                cargarDatos { horariosData, diasData, medidasData, dispositivosData, diasHorarioMap ->
                                    horarios = horariosData
                                    dias = diasData
                                    medidas = medidasData
                                    dispositivos = dispositivosData
                                    diasPorHorario = diasHorarioMap
                                }
                            }
                        }
                    }
                )
                
                // Diálogo para crear horario
                if (mostrarDialogoCrear && dispositivos.isNotEmpty()) {
                    DialogoCrearHorario(
                        dias = dias,
                        medidas = medidas,
                        dispositivos = dispositivos,
                        onDismiss = { mostrarDialogoCrear = false },
                        onConfirmar = { horaInicio, horaFin, idMedida, idDispositivo, diasSeleccionados ->
                            lifecycleScope.launch {
                                crearHorario(horaInicio, horaFin, idMedida, idDispositivo, diasSeleccionados) {
                                    mostrarDialogoCrear = false
                                    cargarDatos { horariosData, diasData, medidasData, dispositivosData, diasHorarioMap ->
                                        horarios = horariosData
                                        dias = diasData
                                        medidas = medidasData
                                        dispositivos = dispositivosData
                                        diasPorHorario = diasHorarioMap
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
    
    private fun cargarDatos(onComplete: (List<Horario>, List<Dia>, List<Medida>, List<Dispositivo>, Map<Int, List<Int>>) -> Unit) {
        lifecycleScope.launch {
            // Cargar días
            val diasResult = horarioRepository.obtenerDias()
            val dias = when (diasResult) {
                is HorarioRepository.Result.Success -> diasResult.data
                else -> emptyList()
            }
            
            // Cargar medidas
            val medidasResult = horarioRepository.obtenerMedidas()
            val medidas = when (medidasResult) {
                is HorarioRepository.Result.Success -> medidasResult.data
                else -> emptyList()
            }
            
            // Cargar dispositivos del usuario
            val dispositivosResult = horarioRepository.obtenerDispositivosPorUsuario(idUsuarioActual)
            val dispositivos = when (dispositivosResult) {
                is HorarioRepository.Result.Success -> dispositivosResult.data
                else -> emptyList()
            }
            
            // Cargar horarios de todos los dispositivos del usuario
            val todosLosHorarios = mutableListOf<Horario>()
            dispositivos.forEach { dispositivo ->
                dispositivo.id?.let { idDispositivo ->
                    val horariosResult = horarioRepository.obtenerHorariosPorDispositivo(idDispositivo)
                    when (horariosResult) {
                        is HorarioRepository.Result.Success -> {
                            todosLosHorarios.addAll(horariosResult.data)
                        }
                        else -> {}
                    }
                }
            }
            
            // Cargar días por horario
            val diasPorHorario = mutableMapOf<Int, List<Int>>()
            todosLosHorarios.forEach { horario ->
                horario.id?.let { idHorario ->
                    val diasHorarioResult = horarioRepository.obtenerDiasDeHorario(idHorario)
                    when (diasHorarioResult) {
                        is HorarioRepository.Result.Success -> {
                            diasPorHorario[idHorario] = diasHorarioResult.data.mapNotNull { it.idDia }
                        }
                        else -> {}
                    }
                }
            }
            
            onComplete(todosLosHorarios, dias, medidas, dispositivos, diasPorHorario)
        }
    }
    
    private fun crearHorario(horaInicio: String, horaFin: String, idMedida: Int, idDispositivo: Int, diasSeleccionados: List<Int>, onComplete: () -> Unit) {
        lifecycleScope.launch {
            val nuevoHorario = Horario(
                idDispositivo = idDispositivo,
                idMedida = idMedida,
                horaInicio = horaInicio,
                horaFin = horaFin
            )
            
            when (val resultado = horarioRepository.crearHorario(nuevoHorario)) {
                is HorarioRepository.Result.Success -> {
                    val horarioCreado = resultado.data
                    // Crear relaciones con días
                    horarioCreado.id?.let { idHorario ->
                        diasSeleccionados.forEach { idDia ->
                            val diaHorario = DiasHorario(
                                idHorario = idHorario,
                                idDia = idDia
                            )
                            horarioRepository.crearDiaHorario(diaHorario)
                        }
                    }
                    Toast.makeText(this@HoraDescansoComposeActivity, "Horario creado exitosamente", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
                is HorarioRepository.Result.Error -> {
                    Toast.makeText(this@HoraDescansoComposeActivity, resultado.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
    
    private fun eliminarHorario(horario: Horario, onComplete: () -> Unit) {
        lifecycleScope.launch {
            horario.id?.let { idHorario ->
                when (val resultado = horarioRepository.eliminarHorario(idHorario)) {
                    is HorarioRepository.Result.Success -> {
                        Toast.makeText(this@HoraDescansoComposeActivity, "Horario eliminado", Toast.LENGTH_SHORT).show()
                        onComplete()
                    }
                    is HorarioRepository.Result.Error -> {
                        Toast.makeText(this@HoraDescansoComposeActivity, resultado.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHorasDescanso(
    horarios: List<Horario>,
    dias: List<Dia>,
    medidas: List<Medida>,
    diasPorHorario: Map<Int, List<Int>>,
    cargando: Boolean,
    onBackClick: () -> Unit,
    onAgregarClick: () -> Unit,
    onEliminarHorario: (Horario) -> Unit
) {
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Primario, Color(0xFF80DEEA)),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Horas de Descanso",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Blanco
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Regresar", tint = Blanco)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Primario)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarClick,
                containerColor = Color(0xFF00BCD4),
                contentColor = Negro
            ) {
                Icon(Icons.Default.Add, "Agregar Horario")
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
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Blanco
                )
            } else if (horarios.isEmpty()) {
                Text(
                    text = "No tienes horarios de descanso\nPresiona + para crear uno",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Blanco,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(horarios) { horario ->
                        HorarioCard(
                            horario = horario,
                            dias = dias,
                            medidas = medidas,
                            diasSeleccionados = diasPorHorario[horario.id] ?: emptyList(),
                            onEliminar = { onEliminarHorario(horario) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HorarioCard(
    horario: Horario,
    dias: List<Dia>,
    medidas: List<Medida>,
    diasSeleccionados: List<Int>,
    onEliminar: () -> Unit
) {
    val medida = medidas.find { it.id == horario.idMedida }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${horario.horaInicio} - ${horario.horaFin}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Negro
                    )
                    medida?.let {
                        Text(
                            text = it.nombre ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
                
                // Botón eliminar
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onEliminar() }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Mostrar días seleccionados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dias.forEach { dia ->
                    val isSelected = diasSeleccionados.contains(dia.id)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFF00BCD4)
                                else Color.LightGray.copy(alpha = 0.3f)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF00BCD4) else Color.Gray,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dia.nombre?.take(1) ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Blanco else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoCrearHorario(
    dias: List<Dia>,
    medidas: List<Medida>,
    dispositivos: List<Dispositivo>,
    onDismiss: () -> Unit,
    onConfirmar: (String, String, Int, Int, List<Int>) -> Unit
) {
    var horaInicio by remember { mutableStateOf("08:00") }
    var horaFin by remember { mutableStateOf("22:00") }
    var medidaSeleccionada by remember { mutableStateOf<Medida?>(medidas.firstOrNull()) }
    var dispositivoSeleccionado by remember { mutableStateOf<Dispositivo?>(dispositivos.firstOrNull()) }
    var diasSeleccionados by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var expandedMedidas by remember { mutableStateOf(false) }
    var expandedDispositivos by remember { mutableStateOf(false) }
    
    val timePickerStateInicio = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = true
    )
    
    val timePickerStateFin = rememberTimePickerState(
        initialHour = 22,
        initialMinute = 0,
        is24Hour = true
    )
    
    var mostrarPickerInicio by remember { mutableStateOf(false) }
    var mostrarPickerFin by remember { mutableStateOf(false) }
    
    // Actualizar hora inicio cuando cambia el picker
    LaunchedEffect(timePickerStateInicio.hour, timePickerStateInicio.minute) {
        horaInicio = String.format("%02d:%02d:00", timePickerStateInicio.hour, timePickerStateInicio.minute)
    }
    
    // Actualizar hora fin cuando cambia el picker
    LaunchedEffect(timePickerStateFin.hour, timePickerStateFin.minute) {
        horaFin = String.format("%02d:%02d:00", timePickerStateFin.hour, timePickerStateFin.minute)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Horario de Descanso") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Selector de dispositivo
                ExposedDropdownMenuBox(
                    expanded = expandedDispositivos,
                    onExpandedChange = { expandedDispositivos = !expandedDispositivos }
                ) {
                    OutlinedTextField(
                        value = dispositivoSeleccionado?.nombre ?: "Seleccionar dispositivo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dispositivo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDispositivos) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDispositivos,
                        onDismissRequest = { expandedDispositivos = false }
                    ) {
                        dispositivos.forEach { dispositivo ->
                            DropdownMenuItem(
                                text = { Text(dispositivo.nombre ?: "Sin nombre") },
                                onClick = {
                                    dispositivoSeleccionado = dispositivo
                                    expandedDispositivos = false
                                }
                            )
                        }
                    }
                }
                
                // Selector de medida
                ExposedDropdownMenuBox(
                    expanded = expandedMedidas,
                    onExpandedChange = { expandedMedidas = !expandedMedidas }
                ) {
                    OutlinedTextField(
                        value = medidaSeleccionada?.nombre ?: "Seleccionar medida",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Acción") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMedidas) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMedidas,
                        onDismissRequest = { expandedMedidas = false }
                    ) {
                        medidas.forEach { medida ->
                            DropdownMenuItem(
                                text = { Text(medida.nombre ?: "") },
                                onClick = {
                                    medidaSeleccionada = medida
                                    expandedMedidas = false
                                }
                            )
                        }
                    }
                }
                
                // Hora inicio
                OutlinedTextField(
                    value = horaInicio.substring(0, 5),
                    onValueChange = {},
                    label = { Text("Hora Inicio") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostrarPickerInicio = true }
                )
                
                // Hora fin
                OutlinedTextField(
                    value = horaFin.substring(0, 5),
                    onValueChange = {},
                    label = { Text("Hora Fin") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostrarPickerFin = true }
                )
                
                Text("Selecciona los días:", style = MaterialTheme.typography.bodyMedium)
                
                // Días seleccionables
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dias.forEach { dia ->
                        dia.id?.let { idDia ->
                            val isSelected = diasSeleccionados.contains(idDia)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color(0xFF00BCD4)
                                        else Color.LightGray.copy(alpha = 0.3f)
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF00BCD4) else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        diasSeleccionados = if (isSelected) {
                                            diasSeleccionados - idDia
                                        } else {
                                            diasSeleccionados + idDia
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dia.nombre?.take(1) ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Blanco else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    medidaSeleccionada?.id?.let { idMedida ->
                        dispositivoSeleccionado?.id?.let { idDispositivo ->
                            if (diasSeleccionados.isNotEmpty()) {
                                onConfirmar(horaInicio, horaFin, idMedida, idDispositivo, diasSeleccionados.toList())
                            }
                        }
                    }
                }
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
    
    // Diálogo Time Picker para hora inicio
    if (mostrarPickerInicio) {
        AlertDialog(
            onDismissRequest = { mostrarPickerInicio = false },
            confirmButton = {
                TextButton(onClick = { mostrarPickerInicio = false }) {
                    Text("OK")
                }
            },
            text = {
                TimePicker(state = timePickerStateInicio)
            }
        )
    }
    
    // Diálogo Time Picker para hora fin
    if (mostrarPickerFin) {
        AlertDialog(
            onDismissRequest = { mostrarPickerFin = false },
            confirmButton = {
                TextButton(onClick = { mostrarPickerFin = false }) {
                    Text("OK")
                }
            },
            text = {
                TimePicker(state = timePickerStateFin)
            }
        )
    }
}
