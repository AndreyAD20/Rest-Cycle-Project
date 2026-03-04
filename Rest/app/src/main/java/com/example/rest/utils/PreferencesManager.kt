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

    // Instancia de las preferencias encriptadas
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

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
     * Limpia todas las preferencias encriptadas (ideal para Cerrar Sesión)
     */
    fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }
}
