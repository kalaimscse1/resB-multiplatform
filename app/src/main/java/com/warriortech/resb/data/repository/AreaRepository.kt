package com.warriortech.resb.data.repository

import com.warriortech.resb.data.local.dao.TblAreaDao
import com.warriortech.resb.data.local.entity.SyncStatus
import com.warriortech.resb.data.local.entity.TblArea
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.model.Area
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.util.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AreaRepository @Inject constructor(
    private val apiService: ApiService,
    private val areaDao: TblAreaDao,
    private val sessionManager: SessionManager,
    networkMonitor: NetworkMonitor,
) : OfflineFirstRepository(networkMonitor){
    
    fun getAllAreas(): Flow<List<Area>> = flow {
        try {
            val response = apiService.getAllAreas(sessionManager.getCompanyCode()?:"")
            syncAreasFromRemote()
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

    suspend fun syncAreasFromRemote(){
        safeApiCall(
            onSuccess = { remoteAreas: List<Area> ->
                withContext(Dispatchers.IO) {
                    val entities = remoteAreas.map {
                        TblArea(
                            area_id = it.area_id.toInt(),
                            area_name = it.area_name,
                            is_active = it.isActvice,
                            is_synced = SyncStatus.SYNCED,
                            last_synced_at = System.currentTimeMillis()
                        )
                    }
                    areaDao.insertAll(entities)
                }
            },
            apiCall = { apiService.getAllAreas(sessionManager.getCompanyCode() ?: "").body()!! }
        )
    }
}
