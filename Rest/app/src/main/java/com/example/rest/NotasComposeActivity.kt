package com.example.rest

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rest.ui.theme.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class NotasComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                PantallaNotasPrincipal(onBackClick = { finish() })
            }
        }
    }
}

data class Nota(
    val id: String = UUID.randomUUID().toString(),
    val titulo: String,
    val contenido: String,
    val fecha: String,
    val esDestacada: Boolean = false
)

// ============================================================================
// FUNCIONES DE PERSISTENCIA LOCAL
// ============================================================================
// Estas funciones sirven para: Guardar y cargar notas en el almacenamiento local del dispositivo
// Usan: SharedPreferences (almacenamiento clave-valor) y Gson (convertir objetos a JSON)
// Tipo: Funciones de utilidad

// Esta función sirve para: Guardar la lista de notas en SharedPreferences
// Esta función cambia: El archivo de preferencias del dispositivo (escribe datos)
// Esta función llama a: Gson.toJson, SharedPreferences.edit
// Tipo: Función (no retorna nada - Unit)
private fun guardarNotas(context: Context, notas: List<Nota>) {
    // Obtener el archivo de preferencias "notas_prefs" en modo privado (solo esta app puede leerlo)
    val sharedPreferences = context.getSharedPreferences("notas_prefs", Context.MODE_PRIVATE)
    
    // Crear instancia de Gson para convertir objetos a texto
    val gson = Gson()
    
    // Convertir la lista de objetos Nota a un String en formato JSON
    val notasJson = gson.toJson(notas)
    
    // Guardar el String JSON en las preferencias
    // .edit() abre el modo edición
    // .putString() escribe el valor
    // .apply() guarda los cambios de forma asíncrona (segura)
    sharedPreferences.edit().putString("notas_lista", notasJson).apply()
}

// Esta función sirve para: Cargar la lista de notas desde SharedPreferences
// Esta función cambia: Nada (solo lee datos)
// Esta función llama a: Gson.fromJson, SharedPreferences.getString
// Retorna: Lista de notas guardadas (o lista vacía si no hay nada guardado)
// Tipo: Función (retorna List<Nota>)
private fun cargarNotas(context: Context): List<Nota> {
    val sharedPreferences = context.getSharedPreferences("notas_prefs", Context.MODE_PRIVATE)
    
    // Leer el String JSON. Si no existe, devuelve null
    val notasJson = sharedPreferences.getString("notas_lista", null)
    
    // Verificar si encontramos datos
    // Tipo: Expresión if-else
    return if (notasJson != null) {
        val gson = Gson()
        // Definir el tipo específico de dato que queremos recuperar (List<Nota>)
        // Esto es necesario porque Gson necesita saber a qué convertir el JSON
        val tipo = object : TypeToken<List<Nota>>() {}.type
        
        // Convertir el texto JSON de vuelta a objetos Nota
        gson.fromJson(notasJson, tipo)
    } else {
        // Si no había nada guardado, retornar una lista vacía
        emptyList()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PantallaNotasPrincipal(onBackClick: () -> Unit) {
    // Contexto necesario para acceder a SharedPreferences
    val context = LocalContext.current
    
    // --- ESTADOS DE LA UI ---
    // Controlan qué se muestra en pantalla y cómo se comporta
    
    // Controla si mostramos el formulario de agregar/editar
    var mostrandoAgregarNota by remember { mutableStateOf(false) }
    // Guarda la nota que estamos editando (null si es una nueva nota)
    var notaAEditar by remember { mutableStateOf<Nota?>(null) }
    
    // --- ESTADOS PARA SELECCIÓN MÚLTIPLE ---
    // Sirve para: Saber si el usuario está en modo "borrar varias notas"
    var modoSeleccion by remember { mutableStateOf(false) }
    // Sirve para: Guardar los IDs de las notas marcadas
    var notasSeleccionadas by remember { mutableStateOf(setOf<String>()) }
    
    // Lista de notas que se muestra en pantalla
    // Usamos mutableStateListOf para que la UI se actualice automáticamente al cambiar la lista
    val notas = remember { mutableStateListOf<Nota>() }
    
    // --- EFECTO DE INICIO ---
    // LaunchedEffect(Unit) se ejecuta una sola vez cuando se crea la pantalla
    // Sirve para: Cargar las notas guardadas al abrir la app
    LaunchedEffect(Unit) {
        val notasGuardadas = cargarNotas(context)
        notas.clear() // Limpiar lista inicial
        notas.addAll(notasGuardadas) // Añadir las recuperadas del almacenamiento
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PantallaListaNotas(
            notas = notas,
            modoSeleccion = modoSeleccion,
            notasSeleccionadas = notasSeleccionadas,
            onBackClick = {
                // Lógica del botón atrás:
                // Si estamos seleccionando -> Salir del modo selección
                // Si no -> Regresar a la pantalla anterior (comportamiento normal)
                if (modoSeleccion) {
                    modoSeleccion = false
                    notasSeleccionadas = emptySet()
                } else {
                    onBackClick()
                }
            },
            onAddClick = { 
                notaAEditar = null
                mostrandoAgregarNota = true 
            },
            onNotaClick = { nota ->
                // Comportamiento al hacer clic en una nota:
                if (modoSeleccion) {
                    // MODO SELECCIÓN: Marcar o desmarcar la nota
                    val nuevasSeleccionadas = notasSeleccionadas.toMutableSet()
                    if (nuevasSeleccionadas.contains(nota.id)) {
                        nuevasSeleccionadas.remove(nota.id) // Si ya estaba, quitar
                    } else {
                        nuevasSeleccionadas.add(nota.id)    // Si no estaba, añadir
                    }
                    notasSeleccionadas = nuevasSeleccionadas
                    
                    // Si desmarcamos la última nota, salir del modo selección automáticamente
                    if (notasSeleccionadas.isEmpty()) {
                        modoSeleccion = false
                    }
                } else {
                    // MODO NORMAL: Abrir la nota para editar
                    notaAEditar = nota
                    mostrandoAgregarNota = true
                }
            },
            onNotaLongClick = { nota ->
                // Comportamiento al mantener presionado (Long Press):
                // Entrar en modo selección y marcar la nota presionada
                if (!modoSeleccion) {
                    modoSeleccion = true
                    notasSeleccionadas = setOf(nota.id)
                }
            },
            onDeleteSelected = {
                // Eliminar todas las notas seleccionadas
                // removeAll: Quita de la lista si cumple la condición (su ID está en notasSeleccionadas)
                notas.removeAll { it.id in notasSeleccionadas }
                
                guardarNotas(context, notas) // IMPORTANTE: Guardar cambios en almacenamiento
                
                // Resetear estado
                modoSeleccion = false
                notasSeleccionadas = emptySet()
            }
        )

        AnimatedVisibility(
            visible = mostrandoAgregarNota,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            PantallaAgregarNota(
                notaAEditar = notaAEditar,
                onBack = { 
                    mostrandoAgregarNota = false 
                    notaAEditar = null
                },
                onSave = { notaGuardada ->
                    if (notaAEditar != null) {
                        val index = notas.indexOfFirst { it.id == notaGuardada.id }
                        if (index != -1) {
                            notas[index] = notaGuardada
                        }
                    } else {
                        notas.add(0, notaGuardada) 
                    }
                    guardarNotas(context, notas) // Guardar cambios
                    mostrandoAgregarNota = false
                    notaAEditar = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PantallaListaNotas(
    notas: List<Nota>,
    modoSeleccion: Boolean,
    notasSeleccionadas: Set<String>,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onNotaClick: (Nota) -> Unit,
    onNotaLongClick: (Nota) -> Unit, // Nuevo callback
    onDeleteSelected: () -> Unit // Nuevo callback
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
                        if (modoSeleccion) "${notasSeleccionadas.size} seleccionadas" else "Todas las notas",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (modoSeleccion) Negro else Negro
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        // Cambiar icono según el modo
                        Icon(
                             if (modoSeleccion) Icons.Default.Close else Icons.Default.ArrowBack, 
                             if (modoSeleccion) "Cancelar" else "Regresar", 
                             tint = Negro
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            // Cambiar acción del botón flotante según el modo
            FloatingActionButton(
                onClick = if (modoSeleccion) onDeleteSelected else onAddClick,
                containerColor = if (modoSeleccion) Color(0xFFFF5252) else Color(0xFF00BCD4),
                contentColor = if (modoSeleccion) Blanco else Negro
            ) {
                Icon(
                    if (modoSeleccion) Icons.Default.Delete else Icons.Default.Add, 
                    if (modoSeleccion) "Eliminar" else "Agregar Nota"
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
                        modoSeleccion = modoSeleccion,
                        estaSeleccionada = nota.id in notasSeleccionadas,
                        onClick = { onNotaClick(nota) },
                        onLongClick = { onNotaLongClick(nota) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregarNota(
    notaAEditar: Nota? = null,
    onBack: () -> Unit,
    onSave: (Nota) -> Unit
) {
    var titulo by remember { mutableStateOf(notaAEditar?.titulo ?: "") }
    var contenido by remember { mutableStateOf(notaAEditar?.contenido ?: "") }
    var esDestacada by remember { mutableStateOf(notaAEditar?.esDestacada ?: false) }

    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF80DEEA), Primario),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brochaGradiente)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Regresar", tint = Negro)
                }
                
                IconButton(onClick = { esDestacada = !esDestacada }) {
                    Icon(
                        imageVector = if (esDestacada) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = "Destacar",
                        tint = if (esDestacada) Color(0xFFFFD700) else Negro
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = titulo,
                onValueChange = { titulo = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Negro
                ),
                decorationBox = { innerTextField ->
                    if (titulo.isEmpty()) {
                        Text(
                            "Título",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            BasicTextField(
                value = contenido,
                onValueChange = { contenido = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Negro),
                decorationBox = { innerTextField ->
                    if (contenido.isEmpty()) {
                        Text(
                            "Nota",
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val fechaActual = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())
                    onSave(
                        Nota(
                            id = notaAEditar?.id ?: UUID.randomUUID().toString(),
                            titulo = titulo,
                            contenido = contenido,
                            fecha = notaAEditar?.fecha ?: fechaActual,
                            esDestacada = esDestacada
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4))
            ) {
                Text(
                    if (notaAEditar != null) "Actualizar" else "Guardar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Negro
                )
            }
        }
    }
}

// Componente que dibuja una tarjeta de nota individual
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotaCard(
    nota: Nota,
    modoSeleccion: Boolean,
    estaSeleccionada: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    // Haptics permite hacer vibrar el teléfono suavemente
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current

    Card(
        // Cambiar color de fondo: Si está seleccionada, usar un azul muy claro, si no, blanco
        colors = CardDefaults.cardColors(
            containerColor = if (estaSeleccionada) Color(0xFFE0F7FA) else Blanco.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // Añadir borde azul si está seleccionada
        border = if (estaSeleccionada) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00BCD4)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    // Vibrar al hacer long-press
                    haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // ... (contenido de la nota: texto y fecha)
                Text(
                    text = nota.contenido,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Negro,
                    maxLines = 10
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Nota agregada ${nota.fecha}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        if (nota.titulo.isNotBlank()) {
                            Text(
                                text = nota.titulo,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Negro
                            )
                        }
                    }
                    if (nota.esDestacada) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Destacada",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // --- UI CONDICIONAL: Checkbox de selección ---
            // Solo dibujamos esta parte si estamos en modoSeleccion
            if (modoSeleccion) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.TopEnd // Ubicar en la esquina superior derecha
                ) {
                    Checkbox(
                        checked = estaSeleccionada,
                        onCheckedChange = { onClick() }, // El click en el checkbox hace lo mismo que en la card
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF00BCD4),
                            uncheckedColor = Color.Gray
                        )
                    )
                }
            }
        }
    }
}
