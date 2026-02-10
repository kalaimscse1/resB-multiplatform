package com.warriortech.resb.network

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton object for creating and managing the Retrofit client
 */
object RetrofitClient {
    // Fixed cloud server URL for company master operations
    const val defaultBaseUrl: String = "http://72.61.172.248:5050/api/"

    private var retrofit: Retrofit? = null
    private var masterRetrofit: Retrofit? = null
    private var sessionManager: SessionManager? = null

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Interceptor to dynamically change the Base URL for standard apiService calls.
     */
    private val dynamicBaseUrlInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        
        // If it's a WhatsApp API request, don't change anything
        if (originalUrl.host == "eco.hashwa.in") {
            return@Interceptor chain.proceed(originalRequest)
        }

        val sessionUrl = sessionManager?.getBaseUrl()?.toHttpUrlOrNull()
        if (sessionUrl != null) {
            val newUrl = originalRequest.url.newBuilder()
                .scheme(sessionUrl.scheme)
                .host(sessionUrl.host)
                .port(sessionUrl.port)
                .build()
            return@Interceptor chain.proceed(originalRequest.newBuilder().url(newUrl).build())
        }
        
        chain.proceed(originalRequest)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(dynamicBaseUrlInterceptor)
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private val masterOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    /**
     * Must be called once at app startup (e.g., in Application class) to enable dynamic base URL.
     */
    fun init(session: SessionManager) {
        this.sessionManager = session
    }

    /**
     * Returns the current base URL being used.
     */
    val currentBaseUrl: String
        get() = sessionManager?.getBaseUrl() ?: defaultBaseUrl

    /**
     * Main Retrofit instance using dynamic URL from sessionManager (Local Server)
     */
    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(defaultBaseUrl) // Start with default, but interceptor will swap it
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    /**
     * Master Retrofit instance using defaultBaseUrl (Fixed Cloud/Main Server)
     */
    private fun getMasterRetrofit(): Retrofit {
        if (masterRetrofit == null) {
            masterRetrofit = Retrofit.Builder()
                .baseUrl(defaultBaseUrl)
                .client(masterOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return masterRetrofit!!
    }

    /**
     * Service for standard operations (uses session.getBaseUrl via Interceptor)
     */
    val apiService: ApiService
        get() = getRetrofit().create(ApiService::class.java)

    /**
     * Service for company master operations (always uses defaultBaseUrl)
     */
    val masterApiService: ApiService
        get() = getMasterRetrofit().create(ApiService::class.java)

    /**
     * Backwards compatibility helper
     */
    fun updateBaseUrl(newUrl: String) {
        // Base URL is now handled dynamically by the interceptor reading from SessionManager
    }
}
