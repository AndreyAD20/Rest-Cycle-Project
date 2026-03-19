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
import com.example.rest.data.repository.UsuarioRepository
import com.example.rest.data.models.Usuario

class SplashActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Verificar si es un deep link de recuperación de contraseña
        val intentData = intent.data
        val typeParam = intentData?.getQueryParameter("type")
        android.util.Log.d("SplashActivity", "Deep link detected: ${intentData?.toString()}, type: $typeParam")
        
        if (intentData != null && typeParam == "recovery") {
            android.util.Log.d("SplashActivity", "Passing deep link to LoginComposeActivity")
            // Pasar el deep link original a LoginComposeActivity y terminar
            val deepLinkIntent = Intent(this, LoginComposeActivity::class.java)
            deepLinkIntent.data = intentData
            deepLinkIntent.putExtras(intent)
            startActivity(deepLinkIntent)
            finish()
            return
        }
        
        setContent {
            TemaRest {
                SplashScreen {
                    // La lógica real de navegación ahora está dentro de SplashScreen
                    // dependiendo del resultado de la verificación
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

    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    LaunchedEffect(true) {
        // En paralelo, mostramos el splash un tiempo mínimo y verificamos sesión
        val minSplashTime = 2000L
        val startTime = System.currentTimeMillis()
        
        var navigateToLogin = true
        var forceLogoutMessage = false
        val prefs = com.example.rest.utils.PreferencesManager(context)
        
        try {
            val idUsuario = prefs.getUserId()
            val tokenLocal = prefs.getSessionToken()
            
            if (idUsuario != -1) {
                // Hay sesión local, vamos a verificar el token en Supabase
                // Podríamos usar el repositorio, pero hacemos directo para evitar dependencias circulares si es complejo
                val repository = UsuarioRepository()
                val response = repository.obtenerUsuarioPorId(context, idUsuario)
                
                if (response is UsuarioRepository.Result.Success<*>) {
                    val userData = response.data as Usuario
                    val tokenServidor = userData.ultimoTokenSesion
                    
                    if (tokenLocal != null && tokenLocal == tokenServidor) {
                        // El token coincide, la sesión es válida
                        navigateToLogin = false
                    } else {
                        // Token inválido (se inició sesión en otro lado o se borró de DB)
                        prefs.clearPreferences()
                        forceLogoutMessage = true
                    }
                } else {
                    // Si no hay internet o error servidor, por defecto dejamos pasar 
                    // a la app asumiendo sesión válida temporal (offline support)
                    // Las demás partes de la app fallarán si necesitan online, pero no bloqueamos el inicio
                    navigateToLogin = false
                }
            }
        } catch (e: Exception) {
            // Error de hardware/encriptación o red muy severo
            prefs.clearPreferences()
        }
        
        val elapsedTime = System.currentTimeMillis() - startTime
        if (elapsedTime < minSplashTime) {
            delay(minSplashTime - elapsedTime)
        }

        if (forceLogoutMessage) {
            android.widget.Toast.makeText(context, "La sesión ha expirado o se ha iniciado en otro dispositivo", android.widget.Toast.LENGTH_LONG).show()
        }

        val activity = context as? android.app.Activity
        if (navigateToLogin) {
            val intent = Intent(context, LoginComposeActivity::class.java)
            context.startActivity(intent)
        } else {
            val intent = Intent(context, com.example.rest.features.home.InicioComposeActivity::class.java)
            context.startActivity(intent)
        }
        activity?.finish()
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
