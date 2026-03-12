package com.example.rest.features.tools

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rest.BaseComposeActivity
import com.example.rest.data.PreferenciasInteresManager
import com.example.rest.ui.theme.Acento
import com.example.rest.ui.theme.Blanco
import com.example.rest.ui.theme.Primario
import com.example.rest.ui.theme.TemaRest

/**
 * TemasInteresComposeActivity:
 * Activity principal que maneja la pantalla de "Temas de Interés".
 * Hereda de BaseComposeActivity para mantener la coherencia en la arquitectura base del proyecto.
 * 
 * En Jetpack Compose, las Activities son los contenedores, y la interfaz gráfica
 * se define usando funciones @Composable (como TemasInteresScreen).
 */
class TemasInteresComposeActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode = com.example.rest.utils.ThemeManager.isDarkMode(this)
            TemaRest(temaOscuro = isDarkMode) {
                TemasInteresScreen(
                    onBackClick = { finish() },
                    onSaveComplete = {
                        Toast.makeText(this, "Preferencias guardadas", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TemasInteresScreen(onBackClick: () -> Unit, onSaveComplete: () -> Unit) {
    val context = LocalContext.current
    
    // ---------------------------------------------------------------------------------
    // ESTRUCTURA DE DATOS ESTATICA
    // ---------------------------------------------------------------------------------
    // Un "mapOf" es un diccionario inmutable que asocia una "Clave" (tema principal)
    // con un "Valor" (una lista de subtemas). 
    // Esto agiliza la lectura de los datos para rellenar la interfaz.
    val temasConSubtemas = mapOf(
        "Deporte" to listOf("fútbol", "baloncesto", "tenis", "running", "ciclismo", "deportes extremos", "gimnasio/fitness", "artes marciales"),
        "Música" to listOf("pop", "rock", "electrónica", "reggaetón", "clásica", "jazz", "indie", "conciertos en vivo", "producción musical"),
        "Arte" to listOf("pintura", "escultura", "ilustración digital", "street art", "museos", "arte contemporáneo", "dibujo a mano"),
        "Tecnología" to listOf("gadgets", "inteligencia artificial", "programación", "ciberseguridad", "móviles", "videojuegos (como industria)", "hardware"),
        "Negocios" to listOf("emprendimiento", "finanzas personales", "marketing", "startups", "inversiones", "economía", "e-commerce"),
        "Cine" to listOf("estrenos", "cine clásico", "cine independiente", "series", "animación", "documentales", "cine de terror", "cine de acción"),
        "Salud" to listOf("ejercicio", "nutrición", "salud mental", "meditación", "hábitos saludables", "yoga", "bienestar general"),
        "Videojuegos" to listOf("juegos de acción", "RPG", "deportes", "estrategia", "móviles", "eSports", "consolas específicas (PlayStation, Xbox, Nintendo, PC)"),
        "Ciencia" to listOf("astronomía", "física", "biología", "psicología", "medio ambiente", "divulgación científica"),
        "Literatura" to listOf("novelas", "poesía", "fantasía", "ciencia ficción", "no ficción", "desarrollo personal", "cómics/manga"),
        "Viajes" to listOf("playa", "montaña", "ciudades", "viajes de aventura", "viajes gastronómicos", "viajes low-cost", "cultura local"),
        "Gastronomía" to listOf("comida rápida", "comida saludable", "cocina internacional", "postres", "recetas caseras", "restaurantes", "café"),
        "Moda" to listOf("ropa urbana", "moda elegante", "sneakers", "moda sostenible", "accesorios", "tendencias", "streetwear"),
        "Fotografía" to listOf("retrato", "paisaje", "fotografía urbana", "fotografía de viajes", "edición/filtros", "equipo fotográfico"),
        "Historia" to listOf("historia antigua", "historia moderna", "guerras mundiales", "historia de tu país", "biografías", "documentales históricos")
    )

    // ---------------------------------------------------------------------------------
    // GESTIÓN DE ESTADO (STATE) EN JETPACK COMPOSE
    // ---------------------------------------------------------------------------------
    // En Compose, para que la UI reaccione a cambios, las variables deben estar envueltas
    // en un "State". El bloque "remember" le dice a Compose que recuerde el valor
    // durante las "recomposiciones" (redibujados de pantalla).
    
    // Recuperar selecciones guardadas originalmente desde SharedPreferences
    val preSeleccionados = remember { PreferenciasInteresManager.obtenerTemas(context) }
    
    // Paso actual del flujo: controla qué pantalla se renderiza (1 = temas, 2 = subtemas).
    // Usamos mutableStateOf; cuando esta variable cambia, la UI se actualiza automáticamente.
    var pasoActual by remember { mutableStateOf(1) }

    // Set (conjunto) mutable que guarda los temas principales seleccionados (Paso 1).
    // Usamos el set para evitar duplicados. Extraemos solo el tema ("Tema:subtema" -> "Tema").
    var temasPrincipales by remember { 
        mutableStateOf(
            preSeleccionados.map { it.substringBefore(":") }.toSet()
        )
    }

    // Set que guarda la cadena completa "Tema:subtema" elegida en el Paso 2.
    var subtemasSeleccionados by remember { 
        mutableStateOf(preSeleccionados.toSet()) 
    }

    // Scaffold es la estructura visual base de Material Design (contiene topBar, bottomBar, etc.)
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = if (pasoActual == 1) "Temas Generales" else "Subtemas", 
                        fontWeight = FontWeight.Bold, 
                        color = Blanco
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (pasoActual == 2) {
                            pasoActual = 1 // Regresar al paso 1
                        } else {
                            onBackClick() // Finalizar Activity
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, "Regresar", tint = Blanco)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        // BOTÓN DE SIMULACIÓN PARA TESTEO MOCK (DESARROLLO)
                        // 1. Asegurarse de que el Búho (ChatHead Overlay) está vivo para recibir notificaciones
                        com.example.rest.ChatHeadManager.launchOverlayService(context)
                        
                        // 2. Extraer los temas que el usuario está pre-seleccionando
                        val temasActuales = subtemasSeleccionados.toList()
                        
                        // 3. Crear un "Intent" explícito para comunicar con el servicio "TopicNotificationService".
                        // Le pasamos la acción personalizada ACTION_SIMULATE_TOPIC_POPUP
                        val btnIntent = android.content.Intent(context, com.example.rest.services.TopicNotificationService::class.java)
                        btnIntent.action = com.example.rest.services.TopicNotificationService.ACTION_SIMULATE_TOPIC_POPUP
                        
                        // Enviamos los temas actuales como "extras" dentro del Intent
                        btnIntent.putStringArrayListExtra("TEMAS_SIMULADOS", ArrayList(temasActuales))
                        
                        // Lanzar el servicio. Desde Android Oreo (O) es necesario usar startForegroundService.
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            context.startForegroundService(btnIntent)
                        } else {
                            context.startService(btnIntent)
                        }
                    }) {
                        Text("Simular", color = Blanco, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Primario
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (pasoActual == 1) {
                    Button(
                        onClick = { pasoActual = 2 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Acento),
                        shape = RoundedCornerShape(12.dp),
                        enabled = temasPrincipales.isNotEmpty()
                    ) {
                        Text("Siguiente", color = Blanco, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    Button(
                        onClick = {
                            PreferenciasInteresManager.guardarTemas(context, subtemasSeleccionados.toList())
                            com.example.rest.services.TopicNotificationService.startService(context)
                            onSaveComplete()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Acento),
                        shape = RoundedCornerShape(12.dp),
                        enabled = subtemasSeleccionados.isNotEmpty()
                    ) {
                        Text("Guardar Preferencias", color = Blanco, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        // Crossfade es una animación nativa de Compose que difumina suavemente los cambios entre estados (ej. paso 1 al paso 2)
        Crossfade(targetState = pasoActual, modifier = Modifier.padding(paddingValues)) { paso ->
            if (paso == 1) {
                // ---------------------------------------------------------------------------------
                // PASO 1: SELECCIONAR TEMAS PRINCIPALES
                // ---------------------------------------------------------------------------------
                // LazyColumn es equivalente al antiguo RecyclerView. Renderiza los elementos
                // solo a medida que el usuario hace scroll, ahorrando memoria.
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
                ) {
                    item {
                        Text(
                            text = "¿Qué temas te interesan?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Elige tus categorías favoritas para personalizar tu experiencia.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        // FlowRow es un "layout" muy útil para tags o etiquetas. 
                        // Coloca los elementos uno al lado del otro horizontalmente,
                        // y cuando no caben en la pantalla, automáticamente hace un salto de línea (wrap).
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            temasConSubtemas.keys.forEach { tema ->
                                val isSelected = temasPrincipales.contains(tema)
                                // animateColorAsState es una de las APIs de animación más sencillas.
                                // Interpola el color (de gris a verde/primario) cuando el valor "isSelected" cambia.
                                val backgroundColor by animateColorAsState(
                                    targetValue = if (isSelected) Primario else MaterialTheme.colorScheme.surfaceVariant,
                                    animationSpec = tween(durationMillis = 300)
                                )
                                val contentColor = if (isSelected) Blanco else MaterialTheme.colorScheme.onSurfaceVariant

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(backgroundColor)
                                        .clickable {
                                            // Al hacer clic, actualizamos la variable de estado "temasPrincipales".
                                            // Al mutar este estado, Jetpack Compose sabe que debe "re-componer"
                                            // TODO lo que dependa de esa variable para refrescar la vista.
                                            temasPrincipales = if (isSelected) {
                                                temasPrincipales - tema
                                            } else {
                                                temasPrincipales + tema
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = contentColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = tema,
                                            color = contentColor,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // PASO 2: SELECCIONAR SUBTEMAS
                val temasMostrados = temasConSubtemas.filterKeys { it in temasPrincipales }
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
                ) {
                    item {
                        Text(
                            text = "¿Qué subcategorías prefieres?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ajusta exactamente lo que deseas ver de cada tema que seleccionaste.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // .items() es el Iterador de la LazyColumn que crea las Views en masa
                    // Es equivalente a la lógica OnBindViewHolder del antiguo RecyclerView
                    items(temasMostrados.keys.toList()) { tema ->
                        val subtemas = temasMostrados[tema] ?: emptyList()
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = tema,
                                    fontWeight = FontWeight.Bold,
                                    color = Primario,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    subtemas.forEach { subtema ->
                                        val preferenciaStr = "$tema:$subtema"
                                        val isSubSelected = subtemasSeleccionados.contains(preferenciaStr)
                                        
                                        val backColor by animateColorAsState(
                                            targetValue = if (isSubSelected) Primario else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            animationSpec = tween(durationMillis = 200)
                                        )
                                        val textColor = if (isSubSelected) Blanco else MaterialTheme.colorScheme.onSurface
                                        
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(backColor)
                                                .clickable {
                                                    subtemasSeleccionados = if (isSubSelected) {
                                                        subtemasSeleccionados - preferenciaStr
                                                    } else {
                                                        subtemasSeleccionados + preferenciaStr
                                                    }
                                                }
                                                .padding(horizontal = 14.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (isSubSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = textColor,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(
                                                    text = subtema,
                                                    color = textColor,
                                                    fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
