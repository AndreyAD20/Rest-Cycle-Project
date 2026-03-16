package com.example.rest.features.parental

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.Intent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rest.BaseComposeActivity
import com.example.rest.network.SupabaseClient
import com.example.rest.ui.theme.TemaRest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class UbicacionComposeActivity : BaseComposeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val idHijo = intent.getIntExtra("id_hijo", -1)
        val nombreHijo = intent.getStringExtra("nombre_hijo") ?: "Hijo"

        setContent {
            TemaRest {
                PantallaUbicacion(
                    idHijo = idHijo,
                    nombreHijo = nombreHijo,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaUbicacion(
    idHijo: Int,
    nombreHijo: String,
    onBackClick: () -> Unit
) {
    val brochaGradiente = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0D47A1),
            Color(0xFF00838F),
            Color(0xFF00BFA5)
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 2000f)
    )

    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var timestamp by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var sinUbicacion by remember { mutableStateOf(false) }

    // Cargar última ubicación del hijo
    LaunchedEffect(idHijo) {
        if (idHijo == -1) {
            cargando = false
            sinUbicacion = true
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            try {
                val response = SupabaseClient.api.obtenerUltimaUbicacion(
                    idUsuario = "eq.$idHijo"
                )
                if (response.isSuccessful) {
                    val ubicaciones = response.body()
                    if (!ubicaciones.isNullOrEmpty()) {
                        latitud = ubicaciones[0].latitud
                        longitud = ubicaciones[0].longitud
                        timestamp = ubicaciones[0].timestamp
                    } else {
                        sinUbicacion = true
                    }
                } else {
                    sinUbicacion = true
                }
            } catch (e: Exception) {
                sinUbicacion = true
            } finally {
                cargando = false
            }
        }
    }

    // Estado de la cámara del mapa
    val posicionHijo = if (latitud != null && longitud != null)
        LatLng(latitud!!, longitud!!)
    else null

    val cameraPositionState = rememberCameraPositionState()

    // Centrar mapa cuando se carga la ubicación
    LaunchedEffect(posicionHijo) {
        posicionHijo?.let { pos ->
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(pos, 15f)
                )
            )
        }
    }

    // Formatear timestamp legible
    val timestampFormateado = remember(timestamp) {
        if (timestamp == null) return@remember null
        try {
            // Formatos comunes que devuelve Supabase/PostgreSQL
            val patterns = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd HH:mm:ss.SSSSSSXXX",
                "yyyy-MM-dd HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd HH:mm:ss"
            )
            
            var date: java.util.Date? = null
            for (pattern in patterns) {
                try {
                    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                    // Si el patrón no tiene zona horaria al final, asumimos que Supabase mandó UTC
                    if (!pattern.contains("X") && !pattern.contains("Z")) {
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                    }
                    date = sdf.parse(timestamp!!)
                    if (date != null) break
                } catch (_: Exception) {}
            }

            date?.let {
                // Mostrar siempre en la zona horaria LOCAL del dispositivo
                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                outputFormat.timeZone = TimeZone.getDefault()
                outputFormat.format(it)
            } ?: timestamp
        } catch (e: Exception) {
            timestamp
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Ubicación de $nombreHijo",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Regresar", tint = Color.White)
                    }
                },
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(onClick = {
                        val intent = Intent(context, HistorialUbicacionComposeActivity::class.java)
                        intent.putExtra("id_hijo", idHijo)
                        intent.putExtra("nombre_hijo", nombreHijo)
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.History, "Ver Historial", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brochaGradiente)
                .padding(padding)
        ) {
            when {
                cargando -> {
                    // Estado de carga
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
                        Text("Obteniendo ubicación...", color = Color.White, fontSize = 16.sp)
                    }
                }

                sinUbicacion -> {
                    // Sin datos aún
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOff,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(72.dp)
                        )
                        Text(
                            "$nombreHijo aún no ha enviado su ubicación",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            "La ubicación se envía automáticamente cada hora desde el dispositivo del hijo.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                posicionHijo != null -> {
                    // Mapa con la ubicación
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Mapa ocupa la mayor parte de la pantalla
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                uiSettings = MapUiSettings(
                                    zoomControlsEnabled = true,
                                    myLocationButtonEnabled = false
                                )
                            ) {
                                Marker(
                                    state = MarkerState(position = posicionHijo),
                                    title = nombreHijo,
                                    snippet = "Última ubicación conocida"
                                )
                            }
                        }

                        // Tarjeta inferior con información
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        "Última actualización",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        timestampFormateado ?: "Desconocida",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        "Lat: ${"%.5f".format(latitud)}  |  Lon: ${"%.5f".format(longitud)}",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
