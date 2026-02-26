package com.example.rest.features.parental

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import com.example.rest.ui.theme.Fondo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.rest.BaseComposeActivity
import com.example.rest.data.models.ConexionParental
import com.example.rest.data.models.Usuario
import com.example.rest.data.repository.UsuarioRepository
import com.example.rest.features.tools.NotasComposeActivity
import com.example.rest.ui.theme.*
import kotlinx.coroutines.launch

class GestionHijosComposeActivity : BaseComposeActivity() {

    private val usuarioRepository = UsuarioRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                PantallaGestionHijos(
                    onBack = { finish() },
                    onAddChild = { 
                        startActivity(Intent(this, CrearHijoActivity::class.java))
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
    onAddChild: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Hijos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primario
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddChild,
                containerColor = Primario,
                contentColor = Negro
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Hijo")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Fondo)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cuentas vinculadas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Botón temporal para Enlazar Cuenta (Visual)
            OutlinedButton(
                onClick = { /* TODO: Implementar enlace */ },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primario)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Enlazar Cuenta Existente")
            }
            
            // Placeholder: En el futuro cargaremos la lista real
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no tienes cuentas vinculadas.\n¡Agrega una nueva!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
