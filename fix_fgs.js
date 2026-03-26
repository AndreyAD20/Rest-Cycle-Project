const fs = require('fs');
const path = require('path');

const filePath = path.join(__dirname, 'Rest', 'app', 'src', 'main', 'java', 'com', 'example', 'rest', 'services', 'AppMonitorService.kt');

const newContent = `        if (Build.VERSION.SDK_INT >= 34) { // Android 14+
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
        }\n`;

let lines = fs.readFileSync(filePath, 'utf8').split('\n');

// 258..268
lines.splice(258, 11, newContent);

fs.writeFileSync(filePath, lines.join('\n'));
console.log('patched node.js');
