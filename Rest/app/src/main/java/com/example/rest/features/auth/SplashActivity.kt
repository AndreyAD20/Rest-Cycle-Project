package com.example.rest.features.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import com.example.rest.ui.theme.TemaRest
import kotlinx.coroutines.delay

class SplashActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            TemaRest {
                SplashScreen {
                    // Navegar a Login al terminar
                    val intent = Intent(this, LoginComposeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // Color azul claro definido en recursos (#E0F7FA)
    // O podemos usarlo directamente si no resolvemos el recurso en Compose fácilmente sin contexto
    // Usaremos Color(0xFFE0F7FA) que corresponde a azul_splash_fondo o el recurso si está disponible
    val backgroundColor = Color(0xFFE0F7FA) // Light Cyan

    LaunchedEffect(true) {
        delay(2500) // Esperar 2.5 segundos
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo del Búho
            Image(
                painter = painterResource(id = R.drawable.buho_background), // Usamos el recurso existente
                contentDescription = "Logo Rest Cycle",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 24.dp)
            )
            
            // Nombre de la App
            Text(
                text = "Rest Cycle",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color(0xFF006064) // Cyan Oscuro para contraste
                )
            )
        }
    }
}
