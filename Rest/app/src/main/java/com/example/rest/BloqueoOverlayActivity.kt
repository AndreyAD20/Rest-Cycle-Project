package com.example.rest

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rest.ui.theme.TemaRest

class BloqueoOverlayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val blockedPackage = intent.getStringExtra("blocked_package")
        val appName = blockedPackage?.let { getAppName(it) } ?: "esta aplicación"
        
        setContent {
            TemaRest {
                PantallaOverlay(
                    appName = appName,
                    onCloseApp = {
                        val startMain = Intent(Intent.ACTION_MAIN)
                        startMain.addCategory(Intent.CATEGORY_HOME)
                        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(startMain)
                        finish()
                    }
                )
            }
        }
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
    
    override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }
}

@Composable
fun PantallaOverlay(appName: String, onCloseApp: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "¡Tiempo Agotado!",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Has alcanzado tu límite diario para $appName.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.LightGray.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(Modifier.height(32.dp))
            Button(onClick = onCloseApp) {
                Text("Cerrar Aplicación")
            }
        }
    }
}
