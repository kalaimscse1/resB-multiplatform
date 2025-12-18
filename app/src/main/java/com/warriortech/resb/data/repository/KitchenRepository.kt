package com.warriortech.resb.data.repository

import com.warriortech.resb.model.KitchenKOT
import com.warriortech.resb.model.KOTStatus
import com.warriortech.resb.model.KOTStatusUpdate
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KitchenRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun getKitchenKOTs(): Flow<Result<List<KitchenKOT>>> = flow {
        try {
            val response = apiService.getKitchenKOTs(sessionManager.getCompanyCode()?:"")
            if (response.isSuccessful) {
                val kotResponse = response.body()
                if (kotResponse?.success == true && kotResponse.data != null) {
                    emit(Result.success(kotResponse.data))

                } else {
                    emit(Result.failure(Exception(kotResponse?.message ?: "Failed to load KOTs")))
                }
            } else {
                emit(Result.failure(Exception("API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun updateKOTStatus(kotId: Int, status: KOTStatus): Flow<Result<Unit>> = flow {
        try {
            val statusUpdate = KOTStatusUpdate(kotId, status)
            val response = apiService.updateKOTStatus(kotId, statusUpdate,sessionManager.getCompanyCode()?:"")

            if (response.isSuccessful) {
                val updateResponse = response.body()
                if (updateResponse?.success == true) {
                    emit(Result.success(Unit))
                } else {
                    emit(Result.failure(Exception(updateResponse?.message ?: "Failed to update KOT status")))
                }
            } else {
                emit(Result.failure(Exception("API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
