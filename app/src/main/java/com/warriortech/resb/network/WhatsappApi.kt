package com.warriortech.resb.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface WhatsAppApi{

    @Multipart
    @POST("api/send/whatsapp")
    suspend fun sendWhatsApp(
        @Part("secret") secret: RequestBody,
        @Part("account") account: RequestBody,
        @Part("recipient") recipient: RequestBody,
        @Part("type") type: RequestBody,
        @Part("message") message: RequestBody
    ): Response<ResponseBody>
}