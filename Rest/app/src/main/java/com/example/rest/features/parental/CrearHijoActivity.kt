package com.example.rest.features.parental

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.rest.ui.theme.Fondo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.rest.BaseComposeActivity
import com.example.rest.data.models.RegistroRequest
import com.example.rest.data.repository.UsuarioRepository
import com.example.rest.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class CrearHijoActivity : BaseComposeActivity() {

    private val usuarioRepository = UsuarioRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                var cargando by remember { mutableStateOf(false) }
                
                PantallaCrearHijo(
                    onBack = { finish() },
                    onCrear = { request, securityPass ->
                        cargando = true
                        crearCuentaHijo(request, securityPass) {
                            cargando = false
                        }
                    },
                    cargando = cargando
                )
            }
        }
    }

    private fun crearCuentaHijo(request: RegistroRequest, securityPass: String, onComplete: () -> Unit) {
        val idPadre = obtenerIdUsuarioLogueado()
        if (idPadre == -1) {
            Toast.makeText(this, "Error: No se pudo identificar al padre (sesión inválida)", Toast.LENGTH_SHORT).show()
            onComplete()
            return
        }

        lifecycleScope.launch {
            try {
                when (val result = usuarioRepository.crearHijo(idPadre, request, securityPass)) {
                    is UsuarioRepository.Result.Success<*> -> {
                        Toast.makeText(this@CrearHijoActivity, "✅ Código enviado a ${request.correo}", Toast.LENGTH_LONG).show()
                        
                        // Ir a la pantalla de verificación
                        val intent = Intent(this@CrearHijoActivity, com.example.rest.features.auth.VerificacionCodigoActivity::class.java)
                        intent.putExtra("correo", request.correo)
                        intent.putExtra("retornarAPadre", true) // Flag para volver a Gestión de Hijos
                        startActivity(intent)
                        finish() // Cerramos esta, el usuario seguirá en verificación
                    }
                    is UsuarioRepository.Result.Error -> {
                        Toast.makeText(this@CrearHijoActivity, "❌ ${result.message}", Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Toast.makeText(this@CrearHijoActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                onComplete()
            }
        }
    }

    private fun obtenerIdUsuarioLogueado(): Int {
        val prefs = getSharedPreferences("sesion_usuario", MODE_PRIVATE)
        return prefs.getInt("id_usuario", -1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearHijo(
    onBack: () -> Unit,
    onCrear: (RegistroRequest, String) -> Unit,
    cargando: Boolean
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var contrasenaSegura by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cuenta de Hijo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primario)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Fondo)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = nombre, onValueChange = { nombre = it },
                label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = apellido, onValueChange = { apellido = it },
                label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = correo, onValueChange = { correo = it },
                label = { Text("Correo Electrónico") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = fechaNacimiento, onValueChange = { fechaNacimiento = it },
                label = { Text("Fecha Nacimiento (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = contrasena, onValueChange = { contrasena = it },
                label = { Text("Contraseña de Login (Hijo)") }, modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Seguridad Parental", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text("Esta contraseña se usará para autorizar cambios.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            
            OutlinedTextField(
                value = contrasenaSegura, onValueChange = { contrasenaSegura = it },
                label = { Text("Contraseña de Seguridad") }, modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val request = RegistroRequest(
                        nombre = nombre,
                        apellido = apellido.ifBlank { null },
                        correo = correo,
                        fechaNacimiento = fechaNacimiento,
                        contraseña = contrasena,
                        rol = "hijo",
                        mayorEdad = false
                    )
                    onCrear(request, contrasenaSegura)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primario),
                enabled = !cargando && nombre.isNotBlank() && correo.isNotBlank() && contrasena.isNotBlank() && contrasenaSegura.isNotBlank()
            ) {
                if (cargando) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                else Text("Crear y Vincular Cuenta", color = Color.Black)
            }
        }
    }
}
