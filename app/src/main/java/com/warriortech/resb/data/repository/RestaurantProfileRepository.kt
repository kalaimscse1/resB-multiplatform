package com.warriortech.resb.data.repository


import com.warriortech.resb.model.RestaurantProfile
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantProfileRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun getRestaurantProfile(): RestaurantProfile? {
        return try {
            apiService.getRestaurantProfile(sessionManager.getCompanyCode()?:"",sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateRestaurantProfile(profile: RestaurantProfile): Int? {
        return try {
            apiService.updateRestaurantProfile(sessionManager.getCompanyCode()?:"",profile,sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addRestaurantProfile(profile: RestaurantProfile) :RestaurantProfile?{
        return apiService.addRestaurantProfile(profile,profile.company_code).body()
    }

}
