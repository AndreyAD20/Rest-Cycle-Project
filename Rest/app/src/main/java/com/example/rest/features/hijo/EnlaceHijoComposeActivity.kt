package com.example.rest.features.hijo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.rest.services.AppMonitorService
import com.example.rest.services.UbicacionScheduler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.rest.BaseComposeActivity
import com.example.rest.R
import com.example.rest.data.repository.UsuarioRepository
import com.example.rest.features.auth.LoginComposeActivity
import com.example.rest.ui.theme.*
import com.example.rest.utils.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EnlaceHijoComposeActivity : BaseComposeActivity() {

    private val repository = UsuarioRepository()
    private lateinit var prefs: PreferencesManager

    // Launcher para permiso de ubicación en segundo plano (Android 10+)
    private val backgroundLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            prefs.saveTrackingHijoActivo(true)
            UbicacionScheduler.iniciar(this)
            AppMonitorService.startService(this)
            Toast.makeText(this, "✅ Rastreo permanente activado.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "⚠️ Sin permiso permanente: el rastreo se detendrá al cerrar la app.", Toast.LENGTH_LONG).show()
        }
    }

    // Launcher para solicitar permiso de ubicación (Primer plano)
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // En Android 10+ necesitamos pedir el de segundo plano por separado
                mostrarDialogoPermisoBackground()
            } else {
                prefs.saveTrackingHijoActivo(true)
                UbicacionScheduler.iniciar(this)
                AppMonitorService.startService(this)
            }
        } else {
            Toast.makeText(this, "Sin permiso de ubicación: tu padre no podrá ver dónde estás.", Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarDialogoPermisoBackground() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Rastreo Permanente")
            .setMessage("Para que tu padre pueda ver tu ubicación incluso cuando la aplicación está cerrada o el teléfono bloqueado, debes seleccionar la opción \"Permitir todo el tiempo\" en la siguiente pantalla.")
            .setPositiveButton("Entendido") { _, _ ->
                backgroundLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            .setNegativeButton("Ahora no") { _, _ ->
                prefs.saveTrackingHijoActivo(true)
                UbicacionScheduler.iniciar(this)
                AppMonitorService.startService(this)
            }
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PreferencesManager(this)
        val idHijo = prefs.getUserId()

        // Solicitar permiso de ubicación al iniciar
        locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)

        setContent {
            TemaRest {
                PantallaEnlaceHijo(
                    idHijo = idHijo,
                    onVolver = {
                        finish()
                    },
                    onVerificarVinculacion = { vinculado ->
                        if (vinculado) {
                            // Al confirmar vinculación, iniciar flujo de permisos
                            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                            Toast.makeText(this, "✅ ¡Ya estás vinculado con tu padre!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "⏳ Aún no estás vinculado. Muéstrale el código a tu padre.", Toast.LENGTH_LONG).show()
                        }
                    },
                    repository = repository
                )
            }
        }
    }
}

@Composable
fun PantallaEnlaceHijo(
    idHijo: Int,
    onVolver: () -> Unit,
    onVerificarVinculacion: (Boolean) -> Unit,
    repository: UsuarioRepository
) {
    var codigoVinculacion by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var vinculado by remember { mutableStateOf(false) }
    var verificando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Cargar/generar código al iniciar
    LaunchedEffect(idHijo) {
        if (idHijo != -1) {
            // Verificar si ya está vinculado
            val vinculadoResult = repository.estaVinculado(idHijo)
            if (vinculadoResult is UsuarioRepository.Result.Success && vinculadoResult.data) {
                vinculado = true
                cargando = false
                return@LaunchedEffect
            }
            // Obtener o generar código de vinculación
            when (val result = repository.obtenerYGenerarCodigoVinculacion(idHijo)) {
                is UsuarioRepository.Result.Success -> codigoVinculacion = result.data
                is UsuarioRepository.Result.Error -> codigoVinculacion = "ERROR"
                else -> {}
            }
            cargando = false
        }
    }

    // Animación de pulso del código
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scalePulse"
    )

    // Gradiente principal turquesa profundo
    val gradiente = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0D47A1),   // Azul profundo
            Color(0xFF00838F),   // Teal
            Color(0xFF00BFA5)    // Verde menta
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 2000f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradiente)
    ) {
        // Botón de Volver (esquina superior izquierda)
        TextButton(
            onClick = onVolver,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 8.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Volver", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (cargando) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cargando tu información...", color = Color.White.copy(alpha = 0.8f))
            } else if (vinculado) {
                // ---- ESTADO: VINCULADO ----
                VinculadoPanel()
            } else {
                // ---- ESTADO: SIN VINCULAR ----
                // Icono decorativo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ChildCare,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "¡Hola! Aún no estás\nconectado con tu familia",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Muéstrale este código a tu padre o tutor para que te agregue desde su aplicación:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Tarjeta del código
                Card(
                    modifier = Modifier
                        .scale(scalePulse)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 28.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tu código de vinculación",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF546E7A),
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (codigoVinculacion == null || codigoVinculacion == "ERROR") {
                            Text(
                                text = "⚠ Sin conexión",
                                color = Color(0xFFE53935),
                                style = MaterialTheme.typography.titleLarge
                            )
                        } else {
                            // Código mostrado como letras separadas
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                codigoVinculacion!!.forEach { letra ->
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFE0F7FA))
                                            .border(1.5.dp, Color(0xFF00BFA5), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = letra.toString(),
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF004D40)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Este código expira una vez que te vinculen",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF90A4AE),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botón de verificar estado
                Button(
                    onClick = {
                        verificando = true
                        scope.launch {
                            val result = repository.estaVinculado(idHijo)
                            delay(600)
                            verificando = false
                            if (result is UsuarioRepository.Result.Success) {
                                if (result.data) {
                                    vinculado = true
                                }
                                onVerificarVinculacion(result.data)
                            }
                        }
                    },
                    enabled = !verificando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF00695C)
                    )
                ) {
                    if (verificando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color(0xFF00695C),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "¿Ya te vincularon? Verificar",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VinculadoPanel() {
    val infiniteTransition = rememberInfiniteTransition(label = "success_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "alpha_anim"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(Color(0xFF00E676).copy(alpha = 0.2f))
                .border(3.dp, Color(0xFF00E676).copy(alpha = alpha), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF00E676),
                modifier = Modifier.size(64.dp)
            )
        }

        Text(
            text = "¡Estás vinculado!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Tu familia puede ver y gestionar tus actividades desde su aplicación.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, tint = Color.White, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Tu dispositivo está siendo supervisado. Actúa con responsabilidad.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
