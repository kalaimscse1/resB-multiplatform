package com.warriortech.resb.data.repository

import android.annotation.SuppressLint
import android.util.Log
import com.warriortech.resb.model.Registration
import com.warriortech.resb.model.RegistrationRequest
import com.warriortech.resb.model.RegistrationResponse
import com.warriortech.resb.model.RestaurantProfile
import com.warriortech.resb.network.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegistrationRepository @Inject constructor(
    private val apiService: ApiService
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
}
