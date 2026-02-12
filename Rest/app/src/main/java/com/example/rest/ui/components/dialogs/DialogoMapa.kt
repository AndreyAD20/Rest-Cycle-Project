package com.example.rest.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun DialogoMapa(
    onDismiss: () -> Unit,
    onConfirmar: (Double, Double) -> Unit
) {
    // Ubicación inicial (Bogotá por defecto, o 0,0)
    val defaultLocation = LatLng(4.6097, -74.0817)
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Pantalla completa
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Text(
                    "Selecciona una Ubicación",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                
                // Mapa
                Box(modifier = Modifier.weight(1f)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = false),
                        uiSettings = MapUiSettings(zoomControlsEnabled = true),
                        onMapClick = { latLng ->
                            selectedLocation = latLng
                        }
                    ) {
                        selectedLocation?.let {
                            Marker(
                                state = MarkerState(position = it),
                                title = "Ubicación Seleccionada"
                            )
                        }
                    }
                    
                    if (selectedLocation == null) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(8.dp),
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Toca el mapa para marcar un punto",
                                color = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
                
                // Footer Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedLocation?.let {
                                onConfirmar(it.latitude, it.longitude)
                            }
                        },
                        enabled = selectedLocation != null
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}
