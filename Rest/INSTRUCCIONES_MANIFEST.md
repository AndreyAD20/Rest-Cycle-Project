# Instrucciones para el Manifest

Para habilitar la nueva pantalla de Perfil (Dashboard), debes agregar manualmente la siguiente entrada en tu archivo `AndroidManifest.xml`.

**Archivo:** `app/src/main/AndroidManifest.xml`

**Acción:** Copia y pega el siguiente bloque `<activity>` dentro de la etiqueta `<application>`, preferiblemente al final de la lista de actividades existentes.

```xml
        <activity
            android:name=".PerfilComposeActivity"
            android:exported="false"
            android:theme="@style/Theme.Material3.DayNight.NoActionBar" />

        <activity
            android:name=".EstadisticasComposeActivity"
            android:exported="false"
            android:theme="@style/Theme.Material3.DayNight.NoActionBar" />

        <activity
            android:name=".NotasComposeActivity"
            android:exported="false"
            android:theme="@style/Theme.Material3.DayNight.NoActionBar" />

        <activity
            android:name=".BloqueoAppsComposeActivity"
            android:exported="false"
            android:theme="@style/Theme.Material3.DayNight.NoActionBar" />

        <activity
            android:name=".HoraDescansoComposeActivity"
            android:exported="false"
            android:theme="@style/Theme.Material3.DayNight.NoActionBar" />

        <activity
            android:name=".TareasComposeActivity"
            android:exported="false"
            android:theme="@style/Theme.Material3.DayNight.NoActionBar" />

        <activity
            android:name=".CalendarioComposeActivity"
            android:exported="false"
            android:theme="@style/Theme.Material3.DayNight.NoActionBar" />
```

Si deseas que esta sea la actividad principal temporalmente para probarla, puedes cambiar el `intent-filter` de la actividad actual (Login o Inicio) a esta nueva actividad, o simplemente navegar a ella desde tu flujo actual.
