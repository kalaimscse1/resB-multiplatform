package com.warriortech.resb.data.repository

import com.warriortech.resb.model.GeneralSettings
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralSettingsRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun getGeneralSettings(): List<GeneralSettings> {
            val response = apiService.getGeneralSettings(sessionManager.getCompanyCode()?:"")
            if (response.isSuccessful) {
               return response.body() ?: emptyList()
            } else {
                throw Exception("Failed to fetch general settings: ${response.message()}")
            }
        }

    suspend fun updateGeneralSettings(settings: GeneralSettings): Int? {
        return try {
            apiService.updateGeneralSettings(settings.id.toLong(),settings,sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            null
        }
    }
}
