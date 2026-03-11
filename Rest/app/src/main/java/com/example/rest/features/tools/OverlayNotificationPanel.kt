package com.example.rest.features.tools

import android.content.Intent
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rest.data.AppNotification
import com.example.rest.data.NotificationRepository
import com.example.rest.ui.theme.TemaRest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OverlayNotificationPanel(onClosePanel: () -> Unit) {
    // Leemos el STATE FLOW de notificaciones en tiempo real
    val notificaciones by NotificationRepository.notifications.collectAsState()

    TemaRest {
        Surface(
            modifier = Modifier
                .fillMaxHeight(0.6f) // El panel ocupa máximo el 60% de la pantalla hacia abajo
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            color = Color(0xFFF8F9FA),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                
                // Cabecera del Panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE3F2FD))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Asistente Rest Cycle", 
                        fontWeight = FontWeight.Bold, 
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Divider(color = Color.LightGray, thickness = 1.dp)

                // Lista de mensajes reactiva
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    reverseLayout = false // Arriba lo más reciente
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
                            OverlayChatBubbleTemplate(notif = notif, onAction = onClosePanel)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OverlayChatBubbleTemplate(notif: AppNotification, onAction: () -> Unit) {
    val backgroundColor = if (notif.isRead) Color(0xFFEEEEEE) else Color(0xFFE3F2FD)
    val context = LocalContext.current
    
    // Convertir epoch a hora legible
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val timeString = formatter.format(Date(notif.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Marcar como leído y limpiar OS
                NotificationRepository.markAsRead(notif.id, context)
                
                // Navegar a la pantalla origen
                notif.sourceClass?.let { className ->
                    try {
                        val intent = Intent(context, Class.forName(className)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.util.Log.e("OverlayChatBubble", "Clase no encontrada o error: $className")
                    }
                }
                
                // Contraer el panel automáticamente
                onAction()
            },
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .background(backgroundColor, shape = RoundedCornerShape(12.dp))
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
