package com.example.rest.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Gestor seguro para preferencias compartidas utilizando Jetpack Security.
 * Encripta localmente los datos guardados como el Token, ID de Usuario, Nombre, etc.
 */
class PreferencesManager(context: Context) {

    private val PREFS_FILE = "RestCycleEncryptedPrefs"

    // Crear la llave maestra generada por el sistema Android (Keystore)
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Instancia de las preferencias encriptadas con mecanismo de auto-recuperación
    private val sharedPreferences: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Si hay un error de seguridad (ej: la llave maestra cambió, app reinstalada, o datos corruptos)
        // Eliminamos el archivo corrupto físicamente para evitar que la app se cierre por completo.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            context.deleteSharedPreferences(PREFS_FILE)
        } else {
            val prefFile = java.io.File("${context.applicationInfo.dataDir}/shared_prefs/$PREFS_FILE.xml")
            if (prefFile.exists()) {
                prefFile.delete()
            }
        }
        
        // Intentamos crearlo de nuevo desde cero
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Guarda el ID del usuario de forma encriptada
     */
    fun saveUserId(id: Int) {
        sharedPreferences.edit().putInt("ID_USUARIO", id).apply()
    }

    /**
     * Recupera el ID del usuario encriptado (-1 si no existe)
     */
    fun getUserId(): Int {
        return sharedPreferences.getInt("ID_USUARIO", -1)
    }

    /**
     * Guarda el Nombre del usuario de forma encriptada
     */
    fun saveUserName(name: String) {
        sharedPreferences.edit().putString("NOMBRE_USUARIO", name).apply()
    }

    /**
     * Recupera el Nombre del usuario encriptado
     */
    fun getUserName(): String? {
        return sharedPreferences.getString("NOMBRE_USUARIO", null)
    }

    /**
     * Guarda el Correo del usuario de forma encriptada
     */
    fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString("CORREO_USUARIO", email).apply()
    }

    /**
     * Recupera el Correo del usuario encriptado
     */
    fun getUserEmail(): String? {
        return sharedPreferences.getString("CORREO_USUARIO", null)
    }

    /**
     * Guarda si el usuario es mayor de edad (true = mayor/padre, false = menor/hijo)
     */
    fun saveMayorEdad(mayorEdad: Boolean) {
        sharedPreferences.edit().putBoolean("MAYOR_EDAD", mayorEdad).apply()
    }

    /**
     * Recupera si el usuario es mayor de edad
     */
    fun getMayorEdad(): Boolean {
        return sharedPreferences.getBoolean("MAYOR_EDAD", false)
    }

    /**
     * Guarda el Token Único de Sesión
     */
    fun saveSessionToken(token: String) {
        sharedPreferences.edit().putString("SESSION_TOKEN", token).apply()
    }

    /**
     * Recupera el Token Único de Sesión
     */
    fun getSessionToken(): String? {
        return sharedPreferences.getString("SESSION_TOKEN", null)
    }

    /**
     * Guarda si el rastreo de ubicación del hijo debe estar activo
     */
    fun saveTrackingHijoActivo(activo: Boolean) {
        sharedPreferences.edit().putBoolean("TRACKING_HIJO_ACTIVO", activo).apply()
    }

    /**
     * Recupera si el rastreo de ubicación del hijo debe estar activo
     */
    fun isTrackingHijoActivo(): Boolean {
        return sharedPreferences.getBoolean("TRACKING_HIJO_ACTIVO", false)
    }

    /**
     * Limpia todas las preferencias encriptadas (ideal para Cerrar Sesión)
     */
    fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }
    
    /**
     * Guarda la contraseña parental de forma encriptada
     */
    fun saveParentalPassword(password: String) {
        sharedPreferences.edit().putString("PARENTAL_PASSWORD", password).apply()
    }
    
    /**
     * Recupera la contraseña parental encriptada
     */
    fun getParentalPassword(): String? {
        return sharedPreferences.getString("PARENTAL_PASSWORD", null)
    }
}
