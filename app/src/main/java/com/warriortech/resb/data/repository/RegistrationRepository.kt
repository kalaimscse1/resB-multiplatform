package com.warriortech.resb.data.repository

import android.annotation.SuppressLint
import android.util.Log
import com.warriortech.resb.model.Registration
import com.warriortech.resb.model.RegistrationRequest
import com.warriortech.resb.model.RegistrationResponse
import com.warriortech.resb.model.RestaurantProfile
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.WhatsAppApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Part
@Singleton
class RegistrationRepository @Inject constructor(
    private val apiService: ApiService,
    private val whatsappApi: WhatsAppApi
) {
    @SuppressLint("SuspiciousIndentation")
    fun registerCompany(registrationRequest: RegistrationRequest): Flow<Result<Registration>> = flow {
        try {
            val response = apiService.registerCompany(registrationRequest,"KTS-COMPANY_MASTER")

            if (response.isSuccessful) {
                val res = response.body()!!
                emit(Result.success(res))
            } else {
                emit(Result.failure(Exception("Company registration failed: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("Error occurred: ${e.localizedMessage}")))
        }
    }

    suspend fun getCompanyCode(): Map<String, String> {
        val response = apiService.getCompanyCode("KTS-COMPANY_MASTER")
        return if (response.isSuccessful) {
            response.body()!!
        } else {
           mapOf("mas" to "Error fetching company code: ${response.message()}")
        }
    }

    suspend fun addRestaurantProfile(profile: RestaurantProfile) :RestaurantProfile?{
        return apiService.addRestaurantProfile(profile,profile.company_code).body()
    }

    suspend fun sendEmailOtp(mailId:String,otp:String):String{
        return apiService.sendMailOtp(mailId,otp,"KTS-COMPANY_MASTER").body()?.toString() ?:""
    }

    suspend fun sendOtp(phone:String,otp:String): String{
        val response = whatsappApi.sendWhatsApp(
            secret = "66a02ca4cbae00a9b996ba9d1f62a51c56cbccd1".toRequestBody(),
            account = "1768990496a87ff679a2f3e71d9181a67b7542122c6970a7204c38d".toRequestBody(),
            recipient = phone.toRequestBody(),           // +919876543210
            type = "text".toRequestBody(),
            message = "Your OTP is $otp".toRequestBody()
        )

        return if (response.isSuccessful) {
            response.body()?.toString()?:""
        } else {
            ""
        }
    }
}
