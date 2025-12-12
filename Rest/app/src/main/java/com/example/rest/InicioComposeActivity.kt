package com.example.rest

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.rest.ui.theme.*

class LoginComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                PantallaLogin(
                    alClickIniciarSesion = {
                        // Navegar a InicioActivity
                        val intencion = Intent(this, InicioActivity::class.java)
                        startActivity(intencion)
                        finish()
                    },
                    alClickRegistro = {
                        // Navegar a RegisterActivity (si existe)
                        // val intencion = Intent(this, RegisterActivity::class.java)
                        // startActivity(intencion)
                    }
                )
            }
        }
    }
}

@Composable
fun PantallaLogin(
    alClickIniciarSesion: () -> Unit,
    alClickRegistro: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    // Gradiente de fondo cyan/turquesa como en la imagen
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
            .background(brochaGradiente),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // Logo del búho
            Image(
                painter = painterResource(id = R.drawable.buho_background),
                contentDescription = "Logo Búho",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 40.dp)
            )

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

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de PIN
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                placeholder = { 
                    Text(
                        "Ingrese su PIN",
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ¿Olvido el Pin?
            Text(
                text = "¿Olvido el Pin?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF004D40),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Iniciar Sesión
            Button(
                onClick = alClickIniciarSesion,
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
                )
            ) {
                Text(
                    text = "Iniciar Sesión",
                    style = MaterialTheme.typography.labelLarge,
                    color = Negro
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ¿No tienes cuenta?
            Text(
                text = "¿No tienes cuenta?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF004D40),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Registrate
            Button(
                onClick = alClickRegistro,
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
                )
            ) {
                Text(
                    text = "Registrate",
                    style = MaterialTheme.typography.labelLarge,
                    color = Negro
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // O ingresa por
            Text(
                text = "O ingresa por",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF004D40),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Iconos de redes sociales
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.face_background),
                    contentDescription = "Facebook",
                    modifier = Modifier.size(50.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.gmail_background),
                    contentDescription = "Gmail",
                    modifier = Modifier.size(50.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.icloud_background),
                    contentDescription = "iCloud",
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}
