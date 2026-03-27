package com.example.rest.network

/**
 * Configuración de Supabase
 * Contiene las credenciales y configuración para conectarse a Supabase
 */
object SupabaseConfig {
    // URL base de tu proyecto Supabase
    const val SUPABASE_URL = "https://pumiiajdveojxbebgtok.supabase.co"
    
    // API Key (anon/public) de Supabase
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InB1bWlpYWpkdmVvanhiZWJndG9rIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU0OTgyMDEsImV4cCI6MjA4MTA3NDIwMX0.JqN5DIjhzcCnHos1SFXXdn--ELZnVovVd3djApZ8c78"
    
    // URL base para la API REST de Supabase
    const val REST_API_URL = "$SUPABASE_URL/rest/v1/"
    
    // Headers requeridos por Supabase
    const val HEADER_API_KEY = "apikey"
    const val HEADER_AUTHORIZATION = "Authorization"
    
    // Timeouts en segundos
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}
