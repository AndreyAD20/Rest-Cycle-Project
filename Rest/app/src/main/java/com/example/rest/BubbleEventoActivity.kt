package com.example.rest

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rest.features.tools.CalendarioComposeActivity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Activity liviana que se muestra dentro de la burbuja flotante al expandirla.
 * Diseñada para verse compacta (~300dp de ancho) igual a la ventana de Messenger.
 */
class BubbleEventoActivity : BaseComposeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val titulo    = intent.getStringExtra("EVENTO_TITULO") ?: "Evento"
        val tipo      = intent.getStringExtra("EVENTO_TIPO")   ?: ""
        val fechaIso  = intent.getStringExtra("EVENTO_FECHA")  ?: ""

        setContent {
            BubbleEventoContent(
                titulo   = titulo,
                tipo     = tipo,
                fechaIso = fechaIso,
                onAbrirCalendario = {
                    val i = Intent(this, CalendarioComposeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    startActivity(i)
                    finish()
                },
                onDescartar = { finish() }
            )
        }
    }
}

@Composable
private fun BubbleEventoContent(
    titulo: String,
    tipo: String,
    fechaIso: String,
    onAbrirCalendario: () -> Unit,
    onDescartar: () -> Unit
) {
    // Gradiente idéntico al Login y Calendario de la app
    val gradiente = Brush.linearGradient(
        colors = listOf(Color(0xFF006064), Color(0xFF00BCD4)),
        start  = Offset(0f, 0f),
        end    = Offset(0f, 800f)
    )

    // Emoji e info del tipo de evento
    val (tipoEmoji, tipoColor) = when (tipo.lowercase()) {
        "reunión", "reunion" -> Pair("🤝", Color(0xFF5C6BC0))
        "trabajo"            -> Pair("💼", Color(0xFFEF5350))
        "salud"              -> Pair("❤️", Color(0xFF66BB6A))
        "personal"           -> Pair("👤", Color(0xFFFF9800))
        else                 -> Pair("📅", Color(0xFF00BCD4))
    }

    // Formatear hora del evento (si viene en el intent)
    val horaTexto = remember(fechaIso) {
        if (fechaIso.isBlank()) return@remember ""
        try {
            val clean = fechaIso.replace("Z", "").replace("+00:00", "")
                .substringBefore("+").substringBefore(".")
            val dt = LocalDateTime.parse(clean)
            val hoy = LocalDateTime.now()
            val diaLabel = when {
                dt.toLocalDate() == hoy.toLocalDate()                   -> "Hoy"
                dt.toLocalDate() == hoy.toLocalDate().plusDays(1)       -> "Mañana"
                else -> dt.format(DateTimeFormatter.ofPattern("dd MMM", Locale("es")))
            }
            val hora = dt.format(DateTimeFormatter.ofPattern("HH:mm"))
            "$diaLabel · $hora"
        } catch (e: Exception) { "" }
    }

    // ── Layout principal de la burbuja ───────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradiente)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Header: avatar + nombre app + botón X ──────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar circular con emoji del tipo
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(tipoEmoji, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rest Cycle",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                // Botón cerrar (X) — descarta la burbuja
                IconButton(
                    onClick = onDescartar,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Separador sutil
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 0.5.dp)

            // ── Contenido central ───────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Título del evento
                Text(
                    text = titulo,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )

                // Badge del tipo (estilo pill — idéntico a las notificaciones de Messenger)
                if (tipo.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = tipoColor.copy(alpha = 0.30f)
                    ) {
                        Text(
                            text = "$tipoEmoji  $tipo",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                // Hora del evento
                if (horaTexto.isNotEmpty()) {
                    Text(
                        text = "🕐  $horaTexto",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Botones de acción ──────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primario: abrir el Calendario completo
                Button(
                    onClick = onAbrirCalendario,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor   = Color(0xFF006064)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Abrir Calendario",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                // Secundario: descartar sin abrir nada (igual al swipe en Messenger)
                OutlinedButton(
                    onClick = onDescartar,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, Color.White.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Text(
                        "Descartar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}
