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
    const val BASE_URL = "http://72.61.172.248:5055/api/" // Replace with your actual API URL
//    const val BASE_URL = "http://154.210.206.184:5050/api/"
    // Create OkHttpClient with logging and timeout settings


    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttpClient with timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(120, TimeUnit.SECONDS) // connection timeout
        .readTimeout(120, TimeUnit.SECONDS)    // socket read timeout
        .writeTimeout(120, TimeUnit.SECONDS)   // socket write timeout
        .build()
    val apiService: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}