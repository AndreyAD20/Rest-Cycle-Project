package com.example.rest.features.parental

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import com.example.rest.ui.theme.*

// Modelo temporal de datos mock (sin BD)
data class HijoMock(
    val nombre: String,
    val apellido: String,
    val bloqueado: Boolean = false
)

class GestionHijosComposeActivity : BaseComposeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode = com.example.rest.utils.ThemeManager.isDarkMode(this)
            TemaRest(temaOscuro = isDarkMode) {
                PantallaGestionHijos(
                    onBack = { finish() },
                    onEnlazarNuevo = {
                        startActivity(Intent(this, VincularHijoActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionHijos(
    onBack: () -> Unit,
    onEnlazarNuevo: () -> Unit
) {
    val context = LocalContext.current

    // --- DATOS MOCK (sin BD por ahora) ---
    // Cuando se conecte la BD, reemplazar esta lista con la carga real de la API
    val hijosMock = remember {
        listOf(
            HijoMock("Cristian Felipe", "Alvarado", bloqueado = false),
            HijoMock("Diego Alejandro", "Sandoval", bloqueado = true)
        )
    }
    // Para alternar bloqueo de cada hijo (solo visual por ahora)
    val estadoBloqueo = remember { hijosMock.map { mutableStateOf(it.bloqueado) } }

    val gradiente = Brush.radialGradient(
        colors = listOf(Color(0xFF80DEEA), Color(0xFF00BCD4), Color(0xFF00838F)),
        center = Offset(400f, 300f),
        radius = 1200f
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Gestión Parental",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Negro
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Negro)
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
                .background(gradiente)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

            // — TÍTULO PRINCIPAL —
            Text(
                text = "Gestiona los Dispositivos\nde tus Hijos",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                ),
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // — ILUSTRACIÓN: Siluetas de Niños —
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.ic_hijos_silueta),
                contentDescription = "Siluetas de hijos",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 32.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(32.dp))

            // — LISTA DE HIJOS VINCULADOS —
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                hijosMock.forEachIndexed { index, hijo ->
                    TarjetaHijo(
                        nombre = "${hijo.nombre} ${hijo.apellido}",
                        bloqueado = estadoBloqueo[index].value,
                        onToggleBloqueo = {
                            estadoBloqueo[index].value = !estadoBloqueo[index].value
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // — BOTÓN: ENLAZAR NUEVO USUARIO —
            OutlinedButton(
                onClick = onEnlazarNuevo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Enlazar Nuevo Usuario",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
}

@Composable
fun TarjetaHijo(
    nombre: String,
    bloqueado: Boolean,
    onToggleBloqueo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF006064).copy(alpha = 0.85f))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nombre
        Text(
            text = nombre,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            ),
            modifier = Modifier.weight(1f)
        )

        // Candado (toggle bloqueo)
        IconButton(onClick = onToggleBloqueo) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = if (bloqueado) "Desbloquear" else "Bloquear",
                tint = if (bloqueado) Color(0xFFFFB300) else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }

        // Sync/Conectar
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF00BCD4).copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Sincronizar",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


