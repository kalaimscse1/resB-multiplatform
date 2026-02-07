package com.warriortech.resb.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton object for creating and managing the Retrofit client
 */
object RetrofitClient {
    // This is for local server or tenant-specific server which can change
    var currentBaseUrl: String = "http://72.61.172.248:5050/api/"
    
    // This is the fixed cloud server URL for company master operations
    const val defaultBaseUrl: String = "http://72.61.172.248:5050/api/"

    private var retrofit: Retrofit? = null
    private var masterRetrofit: Retrofit? = null

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    /**
     * Updates the local server URL. This resets the main retrofit instance.
     */
    fun updateBaseUrl(newUrl: String) {
        val formattedUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        if (currentBaseUrl != formattedUrl) {
            currentBaseUrl = formattedUrl
            retrofit = null // Reset retrofit to recreate with new URL
        }
    }

    /**
     * Main Retrofit instance using currentBaseUrl (Local Server)
     */
    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    /**
     * Master Retrofit instance using defaultBaseUrl (Cloud/Main Server)
     */
    private fun getMasterRetrofit(): Retrofit {
        if (masterRetrofit == null) {
            masterRetrofit = Retrofit.Builder()
                .baseUrl(defaultBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return masterRetrofit!!
    }

    /**
     * Service for standard operations (uses local server URL)
     */
    val apiService: ApiService
        get() = getRetrofit().create(ApiService::class.java)

    /**
     * Service for company master/server operations (uses default cloud URL)
     */
    val masterApiService: ApiService
        get() = getMasterRetrofit().create(ApiService::class.java)
}
