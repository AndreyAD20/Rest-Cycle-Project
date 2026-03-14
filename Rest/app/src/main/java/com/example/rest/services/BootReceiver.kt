package com.example.rest.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.rest.utils.PreferencesManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Dispositivo reiniciado. Verificando si se debe iniciar el rastreo.")
            
            val prefs = PreferencesManager(context)
            val trackingActivo = prefs.isTrackingHijoActivo()
            val idUsuario = prefs.getUserId()
            val esMayorEdad = prefs.getMayorEdad()

            if (trackingActivo && idUsuario != -1 && !esMayorEdad) {
                Log.i("BootReceiver", "Reiniciando rastreo y monitoreo para el usuario $idUsuario")
                UbicacionScheduler.iniciar(context)
                AppMonitorService.startService(context)
            } else {
                Log.d("BootReceiver", "No se requiere iniciar el rastreo (Tracking: $trackingActivo, ID: $idUsuario, EsPadre: $esMayorEdad)")
            }
        }
    }
}
