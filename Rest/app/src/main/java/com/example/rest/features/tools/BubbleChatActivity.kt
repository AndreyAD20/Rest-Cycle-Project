package com.example.rest.features.tools

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rest.BaseComposeActivity
import com.example.rest.data.AppNotification
import com.example.rest.data.NotificationRepository
import com.example.rest.ui.theme.TemaRest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Actividad ligera diseñada para ser la ventana de chat del "Asistente Rest Cycle".
 * Muestra todas las notificaciones de la app como mensajes en un hilo cronológico.
 */
class BubbleChatActivity : BaseComposeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val chatId = intent.getStringExtra("EXTRA_CHAT_ID") ?: "rest_cycle_assistant"
        val chatName = intent.getStringExtra("EXTRA_CHAT_NAME") ?: "Asistente Rest Cycle"

        setContent {
            TemaRest {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF8F9FA)
                ) {
                    ChatView(chatId = chatId, chatName = chatName)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatView(chatId: String, chatName: String) {
    // Leemos el STATE FLOW de notificaciones en tiempo real
    val notificaciones by NotificationRepository.notifications.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        
        // Cabecera
        CenterAlignedTopAppBar(
            title = { Text(chatName, fontWeight = FontWeight.Bold, color = Color.Black) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFE3F2FD))
        )

        Divider(color = Color.LightGray, thickness = 1.dp)

        // Lista de mensajes reactiva al Flow
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            reverseLayout = false // Mostramos de arriba abajo, el AddNotification ya lo agrega arriba (más reciente primero)
        ) {
            if (notificaciones.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes notificaciones recientes.",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(notificaciones) { notif ->
                    ChatBubbleTemplate(notif = notif)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun ChatBubbleTemplate(notif: AppNotification) {
    val backgroundColor = if (notif.isRead) Color(0xFFEEEEEE) else Color(0xFFE3F2FD) // Resaltado si no se ha leído
    val context = LocalContext.current
    
    // Convertir epoch a hora legible
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val timeString = formatter.format(Date(notif.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Marcar como leído
                NotificationRepository.markAsRead(notif.id, context)
                
                // Navegar a la pantalla origen
                notif.sourceClass?.let { className ->
                    try {
                        val intent = Intent(context, Class.forName(className)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(intent)
                    } catch (e: ClassNotFoundException) {
                        android.util.Log.e("ChatBubble", "Clase no encontrada: $className")
                    }
                }
            },
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .background(backgroundColor, shape = RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Indicador de no leído
                    if (!notif.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    
                    // Categoría y título
                    Text(
                        text = "${notif.category} • ${notif.title}", 
                        color = Color.DarkGray, 
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                // Cuerpo del mensaje
                Text(text = notif.message, color = Color.Black, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                // Hora
                Text(
                    text = timeString, 
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
