package com.warriortech.resb.data.repository

import com.warriortech.resb.network.ApiService
import com.warriortech.resb.model.Area
import com.warriortech.resb.network.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AreaRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    
    fun getAllAreas(): Flow<List<Area>> = flow {
        try {
            val response = apiService.getAllAreas(sessionManager.getCompanyCode()?:"")
            if (response.isSuccessful) {
                emit(response.body() ?: emptyList())
            } else {
                throw Exception("Failed to fetch areas: ${response.message()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun insertArea(area: Area): Area {
        val response = apiService.createArea(area,sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Failed to create area")
        } else {
            throw Exception("Failed to create area: ${response.message()}")
        }
    }

    suspend fun updateArea(area: Area): Int {
        val response = apiService.updateArea(area.area_id, area,sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Failed to update area")
        } else {
            throw Exception("Failed to update area: ${response.message()}")
        }
    }

    suspend fun deleteArea(areaId: Long) : Response<ResponseBody> {
        val response = apiService.deleteArea(areaId,sessionManager.getCompanyCode()?:"")
     return response
    }
}
