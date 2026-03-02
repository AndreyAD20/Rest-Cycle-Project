package com.example.rest.features.blocking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import com.example.rest.features.home.InicioComposeActivity
import com.example.rest.features.tools.BloqueoAppsComposeActivity
import com.example.rest.ui.theme.Blanco
import com.example.rest.ui.theme.Negro
import com.example.rest.ui.theme.Primario
import com.example.rest.ui.theme.TemaRest

class BloqueoActivity : BaseComposeActivity() {

    companion object {
        private const val EXTRA_APP_NAME = "extra_app_name"
        private const val EXTRA_REASON = "extra_reason"

        fun newIntent(context: Context, appName: String, reason: String = "Límite de tiempo alcanzado"): Intent {
            return Intent(context, BloqueoActivity::class.java).apply {
                putExtra(EXTRA_APP_NAME, appName)
                putExtra(EXTRA_REASON, reason)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "Aplicación"
        val reason = intent.getStringExtra(EXTRA_REASON) ?: "Acceso restringido"

        setContent {
            TemaRest {
                PantallaBloqueo(
                    appName = appName,
                    reason = reason,
                    onGoHome = {
                        val intent = Intent(this, BloqueoAppsComposeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun PantallaBloqueo(
    appName: String,
    reason: String,
    onGoHome: () -> Unit
) {
    // Interceptar botón atrás para ir al Home en vez de volver a la app bloqueada
    BackHandler {
        onGoHome()
    }

    // Frases motivacionales aleatorias
    val quotes = remember {
        listOf(
            R.string.blocking_active_quote_1,
            R.string.blocking_active_quote_2,
            R.string.blocking_active_quote_3,
            R.string.blocking_active_quote_4,
            R.string.blocking_active_quote_5,
            R.string.blocking_active_quote_6
        )
    }
    val randomQuoteRes = remember { quotes.random() }

    // Fondo degradado "Zen" (Azul noche profundo)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A237E), // Azul muy oscuro
            Color(0xFF311B92), // Violeta profundo
            Color(0xFF000000)  // Negro
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
        ) {
            // Icono/Búho con efecto de resplandor (simulado con Box detrás)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )
                Image(
                    painter = painterResource(id = R.drawable.buho_background),
                    contentDescription = "Búho Zen",
                    modifier = Modifier.size(140.dp)
                )
            }

            // Título Principal
            Text(
                text = stringResource(R.string.blocking_active_title),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                ),
                color = Blanco,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Subtítulo (Apps)
            Text(
                text = appName,
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFB39DDB), // Lavanda claro
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Tarjeta de Mensaje
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(randomQuoteRes),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = Blanco,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Botón Premium
            Button(
                onClick = onGoHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blanco
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 2.dp
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = stringResource(R.string.blocking_active_btn_return),
                    color = Color(0xFF311B92),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
            }
        }
    }
}
