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
    var currentBaseUrl: String = "http://72.61.172.248:5050/api/"
    private var retrofit: Retrofit? = null

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    fun updateBaseUrl(newUrl: String) {
        if (currentBaseUrl != newUrl) {
            currentBaseUrl = newUrl
            retrofit = null // Reset retrofit to recreate with new URL
        }
    }

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

    val apiService: ApiService
        get() = getRetrofit().create(ApiService::class.java)
}