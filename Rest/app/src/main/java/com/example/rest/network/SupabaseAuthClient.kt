package com.example.rest.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.flow.StateFlow

/**
 * Cliente de autenticación de Supabase (Auth v2.x)
 * Maneja login, registro, recuperación de contraseña, etc.
 */
object SupabaseAuthClient {
    
    private const val SUPABASE_URL = SupabaseConfig.SUPABASE_URL
    private const val SUPABASE_ANON_KEY = SupabaseConfig.SUPABASE_ANON_KEY
    
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth) {
            // Configuración para deep links de recuperación de contraseña
            host = "login"
            scheme = "com.example.rest"
        }
    }

    val auth: Auth get() = client.auth
    
    // Alias para compatibilidad con código que use 'goTrue'
    val goTrue: Auth get() = auth
    
    /**
     * Estado de la sesión (para Deep Links)
     */
    val sessionStatus: StateFlow<SessionStatus> 
        get() = auth.sessionStatus
    
    /**
     * Verificar si hay sesión activa
     */
    fun isLoggedIn(): Boolean = auth.currentSessionOrNull() != null
    
    /**
     * Obtener el JWT token actual
     */
    fun getAccessToken(): String? = auth.currentSessionOrNull()?.accessToken
    
    /**
     * Cerrar sesión
     */
    suspend fun logout() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // Ignorar errores al cerrar sesión
        }
    }
}
