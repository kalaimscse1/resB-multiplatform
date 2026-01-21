package com.warriortech.resb.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://eco.hashwa.in/"

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

    val api: WhatsAppApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(WhatsAppApi::class.java)
}
