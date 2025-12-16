package com.example.rest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rest.ui.theme.*

class NotasComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                PantallaNotas(onBackClick = { finish() })
            }
        }
    }
}

data class Nota(
    val titulo: String,
    val contenido: String,
    val fecha: String,
    val esDestacada: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PantallaNotas(onBackClick: () -> Unit) {
    // Gradiente de fondo
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF80DEEA), Primario),
        start = Offset(0f, 0f),
        end = Offset(0f, 2000f)
    )

    val notas = listOf(
        Nota(
            "Frase",
            "No existen las coincidencias, vibramos en la misma frecuencia de aquello que estamos destinados a encontrar.",
            "16/09",
            true
        ),
        Nota(
            "Lista de canciones",
            "Hasta el techo\nArrollito\nSi tu me besas\nElla es mi fiesta\nTe mando flores\nPa olvidarte",
            "10/09"
        ),
        Nota(
            "Clientes para mañana",
            "Carolina Suesca\nSofia Franco\nDaniel Peña\nCamilo Ribas\nPepito Perez",
            "05/09"
        ),
        Nota(
            "Lista de aseo",
            "Soflan\nJabón de loza\nJabón de baño\nPapel higienico\nEsponjilla\nShampoo sin sal",
            "15/08",
            true
        )
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
                onClick = { /* TODO: Agregar nota */ },
                containerColor = Color(0xFF00BCD4), // Cyan más intenso
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
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalItemSpacing = 16.dp,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notas) { nota ->
                    NotaCard(nota)
                }
            }
        }
    }
}

@Composable
fun NotaCard(nota: Nota) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = nota.contenido,
                style = MaterialTheme.typography.bodyMedium,
                color = Negro
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
                    Text(
                        text = nota.titulo,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Negro
                    )
                }
                if (nota.esDestacada) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Destacada",
                        tint = Negro,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
