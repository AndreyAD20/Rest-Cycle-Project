import sys

file_path = r"Rest\app\src\main\java\com\example\rest\services\AppMonitorService.kt"

with open(file_path, "r", encoding="utf-8") as f:
    lines = f.readlines()

new_content = """        if (Build.VERSION.SDK_INT >= 34) { // Android 14+
            val hasLocation = androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED || androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            val serviceTypes = if (hasLocation) {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            } else {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            }
            
            try {
                startForeground(NOTIFICATION_ID, createNotification(), serviceTypes)
            } catch (e: SecurityException) {
                Log.e(TAG, "Error iniciando Foreground Service: ${e.message}")
                try {
                    startForeground(NOTIFICATION_ID, createNotification(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                } catch (e2: Exception) {
                    Log.e(TAG, "Error fatal iniciando FGS: ${e2.message}")
                }
            }
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }\n"""

# lines[258] is index 258. lines[268] is index 268.
# We replace slice [258:269] (which is lines 259 to 269 inclusive) 
# Wait, index 258 is line 259. index 268 is line 269.
# The slice [258:269] removes 11 lines.
lines[258:269] = [new_content]

with open(file_path, "w", encoding="utf-8", newline='') as f:
    f.writelines(lines)

print("File patched successfully!")
