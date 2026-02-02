package com.example.rest.features.tools

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.rest.BaseComposeActivity
import com.example.rest.data.models.Nota
import com.example.rest.data.repository.NotaRepository
import com.example.rest.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.example.rest.ui.components.DialogoNota

class NotasComposeActivity : BaseComposeActivity() {
    
    private val notaRepository = NotaRepository()
    
    // Obtener ID real del usuario desde SharedPreferences
    private val idUsuarioActual: Int by lazy {
        val sharedPref = getSharedPreferences("RestCyclePrefs", android.content.Context.MODE_PRIVATE)
        sharedPref.getInt("ID_USUARIO", -1)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (idUsuarioActual == -1) {
            Toast.makeText(this, "Error de sesión. Por favor inicia sesión nuevamente.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            TemaRest {
                var notas by remember { mutableStateOf<List<Nota>>(emptyList()) }
                var cargando by remember { mutableStateOf(true) }
                var mostrarDialogoCrear by remember { mutableStateOf(false) }
                
                // Cargar notas al iniciar
                LaunchedEffect(Unit) {
                    cargarNotas { notasCargadas ->
                        notas = notasCargadas
                        cargando = false
                    }
                }
                
                PantallaNotas(
                    notas = notas,
                    cargando = cargando,
                    onBackClick = { finish() },
                    onCrearNota = { titulo, contenido, color ->
                        lifecycleScope.launch {
                            crearNota(titulo, contenido, color) {
                                // Recargar notas
                                cargarNotas { notasCargadas ->
                                    notas = notasCargadas
                                }
                            }
                        }
                    },
                    onEliminarNota = { nota ->
                        lifecycleScope.launch {
                            eliminarNota(nota) {
                                // Recargar notas
                                cargarNotas { notasCargadas ->
                                    notas = notasCargadas
                                }
                            }
                        }
                    },
                    onEditarNota = { nota, titulo, contenido, color ->
                        lifecycleScope.launch {
                            actualizarNota(nota, titulo, contenido, color) {
                                cargarNotas { notasCargadas ->
                                    notas = notasCargadas
                                }
                            }
                        }
                    }
                )
            }
        }
    }
    
    private fun cargarNotas(onComplete: (List<Nota>) -> Unit) {
        lifecycleScope.launch {
            when (val resultado = notaRepository.obtenerNotasPorUsuario(idUsuarioActual)) {
                is NotaRepository.Result.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    onComplete(resultado.data as List<Nota>)
                }
                is NotaRepository.Result.Error -> {
                    Toast.makeText(this@NotasComposeActivity, resultado.message, Toast.LENGTH_SHORT).show()
                    onComplete(emptyList())
                }
                is NotaRepository.Result.Loading -> {
                    // Ya manejado
                }
            }
        }
    }
    
    // ... (rest of imports)
    
    // ... (existing code)

    // ... (existing code)



    // ... inside class
    
    private fun crearNota(titulo: String, contenido: String, color: String, onComplete: () -> Unit) {
        lifecycleScope.launch {
            // Fecha ISO 8601 actual
            val fechaActual = obtenerFechaActualIso()
            
            val nuevaNota = Nota(
                idUsuario = idUsuarioActual,
                titulo = titulo,
                contenido = contenido,
                color = color,
                fecha_actualizacion = fechaActual
            )
            
            when (val resultado = notaRepository.crearNota(nuevaNota)) {
                // ... same success/error
                is NotaRepository.Result.Success<*> -> {
                    Toast.makeText(this@NotasComposeActivity, "Nota creada exitosamente", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
                is NotaRepository.Result.Error -> {
                    Toast.makeText(this@NotasComposeActivity, resultado.message, Toast.LENGTH_SHORT).show()
                }
                is NotaRepository.Result.Loading -> {}
            }
        }
    }
    
    private fun eliminarNota(nota: Nota, onComplete: () -> Unit) {
        lifecycleScope.launch {
            nota.id?.let { idNota ->
                when (val resultado = notaRepository.eliminarNota(idNota)) {
                    is NotaRepository.Result.Success<*> -> {
                        Toast.makeText(this@NotasComposeActivity, "Nota eliminada", Toast.LENGTH_SHORT).show()
                        onComplete()
                    }
                    is NotaRepository.Result.Error -> {
                        Toast.makeText(this@NotasComposeActivity, resultado.message, Toast.LENGTH_SHORT).show()
                    }
                    is NotaRepository.Result.Loading -> {
                        // Ya manejado
                    }
                }
            }
        }
    }

    private fun actualizarNota(notaOriginal: Nota, nuevoTitulo: String, nuevoContenido: String, nuevoColor: String, onComplete: () -> Unit) {
        lifecycleScope.launch {
            val fechaActual = obtenerFechaActualIso()
            
            val notaActualizada = notaOriginal.copy(
                titulo = nuevoTitulo,
                contenido = nuevoContenido,
                color = nuevoColor,
                fecha_actualizacion = fechaActual
            )
            
            notaOriginal.id?.let { id ->
                when (val resultado = notaRepository.actualizarNota(id, notaActualizada)) {
                    is NotaRepository.Result.Success<*> -> {
                        Toast.makeText(this@NotasComposeActivity, "Nota actualizada", Toast.LENGTH_SHORT).show()
                        onComplete()
                    }
                    is NotaRepository.Result.Error -> {
                        Toast.makeText(this@NotasComposeActivity, resultado.message, Toast.LENGTH_SHORT).show()
                    }
                    is NotaRepository.Result.Loading -> {}
                }
            }
        }
    }
    
    // Helper para fecha ISO
    private fun obtenerFechaActualIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PantallaNotas(
    notas: List<Nota>,
    cargando: Boolean,
    onBackClick: () -> Unit,
    onCrearNota: (String, String, String) -> Unit,
    onEliminarNota: (Nota) -> Unit,
    onEditarNota: (Nota, String, String, String) -> Unit
) {
    // Estado para búsqueda
    var textoBusqueda by remember { mutableStateOf("") }
    
    // Estado para edición
    var notaEnEdicion by remember { mutableStateOf<Nota?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    // Filtrar notas
    val notasFiltradas = if (textoBusqueda.isBlank()) {
        notas
    } else {
        notas.filter { 
            (it.titulo?.contains(textoBusqueda, ignoreCase = true) == true) || 
            (it.contenido?.contains(textoBusqueda, ignoreCase = true) == true)
        }
    }

    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF80DEEA), Primario),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Mis Notas",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, "Regresar", tint = Negro)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
                
                // Barra de Búsqueda
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    placeholder = { Text("Buscar notas...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Blanco.copy(alpha = 0.9f),
                        unfocusedContainerColor = Blanco.copy(alpha = 0.7f),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    notaEnEdicion = null // Nueva nota
                    mostrarDialogo = true
                },
                containerColor = Color(0xFF00BCD4),
                contentColor = Negro
            ) {
                Icon(Icons.Default.Add, "Agregar Nota")
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
            } else if (notas.isEmpty()) {
                Text(
                    text = "No tienes notas aún\nPresiona + para crear una",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Blanco,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (notasFiltradas.isEmpty()) {
                 Text(
                    text = "No se encontraron resultados",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Blanco,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalItemSpacing = 16.dp,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notasFiltradas) { nota ->
                        NotaCard(
                            nota = nota,
                            onClick = { 
                                notaEnEdicion = nota
                                mostrarDialogo = true
                            },
                            onEliminar = { onEliminarNota(nota) }
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo compartido para Crear/Editar
    if (mostrarDialogo) {
        DialogoNota(
            nota = notaEnEdicion,
            onDismiss = { mostrarDialogo = false },
            onConfirmar = { titulo, contenido, color ->
                if (notaEnEdicion == null) {
                    // Crear nueva
                    onCrearNota(titulo, contenido, color)
                } else {
                    // Editar existente
                    onEditarNota(notaEnEdicion!!, titulo, contenido, color)
                }
                mostrarDialogo = false
            }
        )
    }
}

@Composable
fun NotaCard(
    nota: Nota,
    onClick: () -> Unit,
    onEliminar: () -> Unit
) {
    val colorFondo = try {
        Color(android.graphics.Color.parseColor(nota.color ?: "#FFFFFF"))
    } catch (e: Exception) {
        Blanco.copy(alpha = 0.9f)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Título
            nota.titulo?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Negro
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Contenido
            nota.contenido?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Negro,
                    maxLines = 5,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider(color = Color.Black.copy(alpha = 0.1f))
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fecha
                val fechaFormateada = formatearFecha(nota.fecha_actualizacion)
                Text(
                    text = fechaFormateada,
                    style = MaterialTheme.typography.labelSmall,
                    color = Negro.copy(alpha = 0.6f)
                )

                // Botón eliminar
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onEliminar() }
                )
            }
        }
    }
}

// Helper para mostrar fecha
fun formatearFecha(fechaIso: String?): String {
    if (fechaIso.isNullOrBlank()) return ""
    
    val formatosEntrada = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX", // Supabase/Postgres default (micros + offset)
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",    // ISO standard (millis + offset)
        "yyyy-MM-dd'T'HH:mm:ssX",        // No millis
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",  // Legacy text literal Z
        "yyyy-MM-dd'T'HH:mm:ss'Z'"       // Legacy text literal Z no millis
    )
    
    val outputFormat = SimpleDateFormat("d MMM, hh:mm a", Locale.getDefault())

    for (patron in formatosEntrada) {
        try {
            val inputFormat = SimpleDateFormat(patron, Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(fechaIso)
            if (date != null) return outputFormat.format(date)
        } catch (e: Exception) {
            continue
        }
    }
    
    // Si falla el parseo, devolver string original para depuración (o vacío si prefiere)
    // Devolvemos vacío para no ensuciar la UI, pero si quieres debuguear cambia a: return fechaIso
    return "" 
}


