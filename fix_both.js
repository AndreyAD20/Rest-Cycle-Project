const fs = require('fs');
const path = require('path');

const filePath = path.join(__dirname, 'Rest', 'app', 'src', 'main', 'java', 'com', 'example', 'rest', 'services', 'AppMonitorService.kt');

let content = fs.readFileSync(filePath, 'utf8');

// 1. Re-apply the FGS location fix
const oldFgs = `        if (Build.VERSION.SDK_INT >= 34) { // Android 14+
            startForeground(
                NOTIFICATION_ID, 
                createNotification(), 
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC or 
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }`;

const newFgs = `        if (Build.VERSION.SDK_INT >= 34) { // Android 14+
            val hasLocation = androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED || androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            val serviceTypes = if (hasLocation) {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            } else {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            }
            
            try {
                startForeground(NOTIFICATION_ID, createNotification(), serviceTypes)
            } catch (e: SecurityException) {
                Log.e(TAG, "Error iniciando Foreground Service: \${e.message}")
                try {
                    startForeground(NOTIFICATION_ID, createNotification(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                } catch (e2: Exception) {
                    Log.e(TAG, "Error fatal iniciando FGS: \${e2.message}")
                }
            }
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }`;

content = content.replace(oldFgs, newFgs);

// 2. Fix the unblocking sync issue by removing the overly aggressive OR condition
const oldSync = `                                 // Preservar bloqueo local si ya estaba activo
                                 // (evita que la sincronización revierte bloqueos por límite de tiempo)
                                 isBlocked = vinculada.bloqueada || (localRepository.getBlockedApps()
                                     .find { it.packageName == pkg }?.isBlocked == true),`;

const newSync = `                                 // La base de datos (Supabase) debe ser la única fuente de la verdad para el estado de bloqueo manual
                                 isBlocked = vinculada.bloqueada,`;

content = content.replace(oldSync, newSync);

fs.writeFileSync(filePath, content);
console.log('patched both issues successfully');
