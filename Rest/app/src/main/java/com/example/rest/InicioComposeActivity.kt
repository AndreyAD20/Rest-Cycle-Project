package com.example.rest

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rest.ui.theme.*

class InicioComposeActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaRest {
                PantallaModosDeUso(
                    alClickRegresar = {
                        finish()
                    },
                    alClickConfiguracion = {
                        // TODO: Navegar a pantalla de configuración
                    },
                    alClickControlParental = {
                        // TODO: Navegar a Control Parental
                    },
                    alClickHabitosSaludables = {
                        // TODO: Navegar a Hábitos Saludables
                    }
                )
            }
        }
    }
}

@Composable
fun PantallaModosDeUso(
    alClickRegresar: () -> Unit,
    alClickConfiguracion: () -> Unit,
    alClickControlParental: () -> Unit,
    alClickHabitosSaludables: () -> Unit
) {
    // Gradiente de fondo cyan/turquesa
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
        // Botón de regresar en la esquina superior izquierda
        IconButton(
            onClick = alClickRegresar,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Regresar",
                tint = Color(0xFF004D40),
                modifier = Modifier.size(32.dp)
            )
        }

        // Botón de configuración en la esquina superior derecha
        IconButton(
            onClick = alClickConfiguracion,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configuración",
                tint = Color(0xFF004D40),
                modifier = Modifier.size(32.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            // Título "Modos de Uso"
            Text(
                text = "Modos de Uso",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Negro,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // Botón Control Parental
            Button(
                onClick = alClickControlParental,
                modifier = Modifier
                    .width(260.dp)
                    .height(56.dp)
                    .border(
                        width = 2.dp,
                        color = Negro,
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00ACC1)
                )
            ) {
                Text(
                    text = "Control Parental",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Negro
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Logo del búho
            Image(
                painter = painterResource(id = R.drawable.buho_background),
                contentDescription = "Logo Búho",
                modifier = Modifier
                    .size(180.dp)
                    .padding(vertical = 20.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botón Hábitos Saludables
            Button(
                onClick = alClickHabitosSaludables,
                modifier = Modifier
                    .width(260.dp)
                    .height(56.dp)
                    .border(
                        width = 2.dp,
                        color = Negro,
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00ACC1)
                )
            ) {
                Text(
                    text = "Habitos Saludables",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Negro
                )
            }
        }
    }
}
