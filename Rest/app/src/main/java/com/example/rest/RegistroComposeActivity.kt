package com.example.rest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.rest.data.models.RegistroRequest
import com.example.rest.data.repository.UsuarioRepository
import com.example.rest.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class RegistroComposeActivity : BaseComposeActivity() {
    
    private val usuarioRepository = UsuarioRepository()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                var cargando by remember { mutableStateOf(false) }
                
                PantallaRegistro(
                    alClickInicioSesion = {
                        // Navegar a LoginComposeActivity
                        val intencion = Intent(this, LoginComposeActivity::class.java)
                        startActivity(intencion)
                        finish()
                    },
                    alClickRegistro = { nombre, apellido, correo, telefono, fechaNac, contraseña, confirmarContra ->
                        // Validaciones
                        when {
                            nombre.isBlank() -> {
                                Toast.makeText(this, "Por favor ingresa tu nombre", Toast.LENGTH_SHORT).show()
                            }
                            correo.isBlank() || !correo.contains("@") -> {
                                Toast.makeText(this, "Por favor ingresa un correo válido", Toast.LENGTH_SHORT).show()
                            }
                            telefono.isBlank() -> {
                                Toast.makeText(this, "Por favor ingresa tu teléfono", Toast.LENGTH_SHORT).show()
                            }
                            fechaNac.isBlank() -> {
                                Toast.makeText(this, "Por favor ingresa tu fecha de nacimiento", Toast.LENGTH_SHORT).show()
                            }
                            contraseña.isBlank() || contraseña.length < 6 -> {
                                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                            }
                            contraseña != confirmarContra -> {
                                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                // Realizar registro
                                cargando = true
                                realizarRegistro(nombre, apellido, correo, telefono, fechaNac, contraseña) {
                                    cargando = false
                                }
                            }
                        }
                    },
                    cargando = cargando
                )
            }
        }
    }
    
    private fun realizarRegistro(
        nombre: String,
        apellido: String,
        correo: String,
        telefono: String,
        fechaNacimiento: String,
        contraseña: String,
        onComplete: () -> Unit
    ) {
        lifecycleScope.launch {
            try {
                // Calcular si es mayor de edad
                val esMayorDeEdad = try {
                    val fechaNac = LocalDate.parse(fechaNacimiento, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    val edad = Period.between(fechaNac, LocalDate.now()).years
                    edad >= 18
                } catch (e: Exception) {
                    // Si no se puede parsear la fecha, asumir mayor de edad
                    true
                }
                
                // Convertir fecha al formato de la BD (YYYY-MM-DD)
                val fechaBD = try {
                    val fechaNac = LocalDate.parse(fechaNacimiento, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    fechaNac.format(DateTimeFormatter.ISO_LOCAL_DATE)
                } catch (e: Exception) {
                    fechaNacimiento
                }
                
                
                // Usar los campos directamente ya que están separados
                val nombreFinal = nombre.trim()
                val apellidoFinal = apellido.trim().ifBlank { null }
                
                val request = RegistroRequest(
                    nombre = nombreFinal,
                    apellido = apellidoFinal,
                    correo = correo,
                    telefono = telefono,
                    fechaNacimiento = fechaBD,
                    contraseña = contraseña,
                    rol = if (esMayorDeEdad) "padre" else "hijo",
                    mayorEdad = esMayorDeEdad
                )
                
                when (val resultado = usuarioRepository.registrar(request)) {
                    is UsuarioRepository.Result.Success -> {
                        val usuario = resultado.data
                        runOnUiThread {
                            Toast.makeText(
                                this@RegistroComposeActivity,
                                "¡Registro exitoso! Bienvenido ${usuario.nombre}",
                                Toast.LENGTH_LONG
                            ).show()
                            
                            // Navegar a LoginComposeActivity
                            val intencion = Intent(this@RegistroComposeActivity, LoginComposeActivity::class.java)
                            startActivity(intencion)
                            finish()
                        }
                    }
                    is UsuarioRepository.Result.Error -> {
                        runOnUiThread {
                            Toast.makeText(
                                this@RegistroComposeActivity,
                                "Error: ${resultado.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    is UsuarioRepository.Result.Loading -> {
                        // Ya está manejado por el estado cargando
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@RegistroComposeActivity,
                        "Error inesperado: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                onComplete()
            }
        }
    }
}

@Composable
fun PantallaRegistro(
    alClickInicioSesion: () -> Unit,
    alClickRegistro: (String, String, String, String, String, String, String) -> Unit,
    cargando: Boolean = false
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var numeroCelular by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    var confirmarContraseña by remember { mutableStateOf("") }
    var aceptaTerminos by remember { mutableStateOf(false) }

    val brochaGradiente = Brush.linearGradient(
        colors = listOf(
            Primario,
            Color(0xFF80DEEA)
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brochaGradiente)
    ) {
        // Botón de volver en la esquina superior izquierda
        IconButton(
            onClick = alClickInicioSesion,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color(0xFF004D40),
                modifier = Modifier.size(32.dp)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // Logo del búho y texto de bienvenida en horizontal
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                // Logo del búho
                Image(
                    painter = painterResource(id = R.drawable.buho_background),
                    contentDescription = "Logo Búho",
                    modifier = Modifier.size(120.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))

                // Texto de bienvenida
                Text(
                    text = "Aquí podrás\nRegistrarte en\nnuestra APP",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF004D40),
                    textAlign = TextAlign.Start
                )
            }

            // Campo de Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = { 
                    Text(
                        "Nombre",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF757575)
                    ) 
                },
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco,
                    focusedBorderColor = Color(0xFF6B4EFF),
                    unfocusedBorderColor = Color(0xFFB0BEC5),
                    focusedTextColor = Negro,
                    unfocusedTextColor = Negro
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de Apellido
            OutlinedTextField(
                value = apellido,
                onValueChange = { apellido = it },
                placeholder = { 
                    Text(
                        "Apellido",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF757575)
                    ) 
                },
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco,
                    focusedBorderColor = Color(0xFF6B4EFF),
                    unfocusedBorderColor = Color(0xFFB0BEC5),
                    focusedTextColor = Negro,
                    unfocusedTextColor = Negro
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de Correo Electrónico
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                placeholder = { 
                    Text(
                        "Correo Electronico",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF757575)
                    ) 
                },
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco,
                    focusedBorderColor = Color(0xFF6B4EFF),
                    unfocusedBorderColor = Color(0xFFB0BEC5),
                    focusedTextColor = Negro,
                    unfocusedTextColor = Negro
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de Número de Celular
            OutlinedTextField(
                value = numeroCelular,
                onValueChange = { numeroCelular = it },
                placeholder = { 
                    Text(
                        "Numero de Celular",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF757575)
                    ) 
                },
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco,
                    focusedBorderColor = Color(0xFF6B4EFF),
                    unfocusedBorderColor = Color(0xFFB0BEC5),
                    focusedTextColor = Negro,
                    unfocusedTextColor = Negro
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de Fecha de Nacimiento
            OutlinedTextField(
                value = fechaNacimiento,
                onValueChange = { fechaNacimiento = it },
                placeholder = { 
                    Text(
                        "Fecha de Nacimiento",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF757575)
                    ) 
                },
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco,
                    focusedBorderColor = Color(0xFF6B4EFF),
                    unfocusedBorderColor = Color(0xFFB0BEC5),
                    focusedTextColor = Negro,
                    unfocusedTextColor = Negro
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de Contraseña
            OutlinedTextField(
                value = contraseña,
                onValueChange = { contraseña = it },
                placeholder = { 
                    Text(
                        "Ingrese su Contraseña",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF757575)
                    ) 
                },
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco,
                    focusedBorderColor = Color(0xFF6B4EFF),
                    unfocusedBorderColor = Color(0xFFB0BEC5),
                    focusedTextColor = Negro,
                    unfocusedTextColor = Negro
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de Confirmar Contraseña
            OutlinedTextField(
                value = confirmarContraseña,
                onValueChange = { confirmarContraseña = it },
                placeholder = { 
                    Text(
                        "Confirme su Contraseña",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF757575)
                    ) 
                },
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco,
                    focusedBorderColor = Color(0xFF6B4EFF),
                    unfocusedBorderColor = Color(0xFFB0BEC5),
                    focusedTextColor = Negro,
                    unfocusedTextColor = Negro
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Checkbox de términos y condiciones
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .width(330.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Checkbox(
                    checked = aceptaTerminos,
                    onCheckedChange = { aceptaTerminos = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Primario,
                        uncheckedColor = Color(0xFF004D40)
                    )
                )
                Text(
                    text = "Usted Acepta Los Terminos De Uso De\nRest Cycle Y La Politica De Privacidad.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF004D40),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón Registrarse
            Button(
                onClick = {
                    alClickRegistro(
                        nombre,
                        apellido,
                        correo,
                        numeroCelular,
                        fechaNacimiento,
                        contraseña,
                        confirmarContraseña
                    )
                },
                modifier = Modifier
                    .width(158.dp)
                    .height(48.dp)
                    .border(
                        width = 2.dp,
                        color = Negro,
                        shape = RoundedCornerShape(8.dp)
                    ),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primario
                ),
                enabled = aceptaTerminos && !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Negro,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Registrarse",
                        style = MaterialTheme.typography.labelLarge,
                        color = Negro
                    )
                }
            }
        }
    }
}
