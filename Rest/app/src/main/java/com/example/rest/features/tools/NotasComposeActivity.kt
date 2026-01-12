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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.rest.BaseComposeActivity
import com.example.rest.data.models.Nota
import com.example.rest.data.repository.NotaRepository
import com.example.rest.ui.theme.*
import kotlinx.coroutines.launch

class NotasComposeActivity : BaseComposeActivity() {
    
    private val notaRepository = NotaRepository()
    // TODO: Obtener el ID del usuario de la sesión actual
    private val idUsuarioActual = 1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    onAgregarClick = { mostrarDialogoCrear = true },
                    onEliminarNota = { nota ->
                        lifecycleScope.launch {
                            eliminarNota(nota) {
                                // Recargar notas
                                cargarNotas { notasCargadas ->
                                    notas = notasCargadas
                                }
                            }
                        }
                    }
                )
                
                // Diálogo para crear nota
                if (mostrarDialogoCrear) {
                    DialogoCrearNota(
                        onDismiss = { mostrarDialogoCrear = false },
                        onConfirmar = { titulo, contenido ->
                            lifecycleScope.launch {
                                crearNota(titulo, contenido) {
                                    mostrarDialogoCrear = false
                                    // Recargar notas
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
    }
    
    private fun cargarNotas(onComplete: (List<Nota>) -> Unit) {
        lifecycleScope.launch {
            when (val resultado = notaRepository.obtenerNotasPorUsuario(idUsuarioActual)) {
                is NotaRepository.Result.Success -> {
                    onComplete(resultado.data)
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
    
    private fun crearNota(titulo: String, contenido: String, onComplete: () -> Unit) {
        lifecycleScope.launch {
            val nuevaNota = Nota(
                idUsuario = idUsuarioActual,
                titulo = titulo,
                contenido = contenido
            )
            
            when (val resultado = notaRepository.crearNota(nuevaNota)) {
                is NotaRepository.Result.Success -> {
                    Toast.makeText(this@NotasComposeActivity, "Nota creada exitosamente", Toast.LENGTH_SHORT).show()
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
    
    private fun eliminarNota(nota: Nota, onComplete: () -> Unit) {
        lifecycleScope.launch {
            nota.id?.let { idNota ->
                when (val resultado = notaRepository.eliminarNota(idNota)) {
                    is NotaRepository.Result.Success -> {
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
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PantallaNotas(
    notas: List<Nota>,
    cargando: Boolean,
    onBackClick: () -> Unit,
    onAgregarClick: () -> Unit,
    onEliminarNota: (Nota) -> Unit
) {
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF80DEEA), Primario),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Todas las notas",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarClick,
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
                    items(notas) { nota ->
                        NotaCard(
                            nota = nota,
                            onEliminar = { onEliminarNota(nota) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotaCard(
    nota: Nota,
    onEliminar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
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
                    color = Negro
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botón eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
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

@Composable
fun DialogoCrearNota(
    onDismiss: () -> Unit,
    onConfirmar: (String, String) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Nota") },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    label = { Text("Contenido") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (titulo.isNotBlank() && contenido.isNotBlank()) {
                        onConfirmar(titulo, contenido)
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
}
