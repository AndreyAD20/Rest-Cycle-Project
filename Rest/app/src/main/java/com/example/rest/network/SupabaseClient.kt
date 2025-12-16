package com.example.rest.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit configurado para Supabase
 * Proporciona una instancia singleton del cliente HTTP
 */
object SupabaseClient {
    
    /**
     * Interceptor que agrega los headers requeridos por Supabase a cada request
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .header(SupabaseConfig.HEADER_API_KEY, SupabaseConfig.SUPABASE_ANON_KEY)
            .header(SupabaseConfig.HEADER_AUTHORIZATION, "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
            .header("Content-Type", "application/json")
            .header("Prefer", "return=representation")
        
        val request = requestBuilder.build()
        chain.proceed(request)
    }
    
    /**
     * Interceptor para logging de requests/responses (útil para debugging)
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    /**
     * Cliente OkHttp configurado con interceptors y timeouts
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(SupabaseConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(SupabaseConfig.READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(SupabaseConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build()
    
    /**
     * Instancia de Retrofit configurada para Supabase
     */
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(SupabaseConfig.REST_API_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    /**
     * Obtiene una instancia de la API de Supabase
     */
    val api: SupabaseApi = retrofit.create(SupabaseApi::class.java)
}
