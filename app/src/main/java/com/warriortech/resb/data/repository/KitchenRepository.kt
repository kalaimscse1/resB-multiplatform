package com.warriortech.resb.data.repository

import com.warriortech.resb.model.KitchenKOT
import com.warriortech.resb.model.KOTStatus
import com.warriortech.resb.model.KOTStatusUpdate
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.util.getCurrentDateModern
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KitchenRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun getKitchenKOTs(): Flow<List<KitchenKOT>> = flow {
        try {
            val response = apiService.getKitchenKOTs(sessionManager.getCompanyCode()?:"",getCurrentDateModern(),getCurrentDateModern())
            if (response.isSuccessful) {
                emit(response.body() ?: emptyList())
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun updateKOTStatus(kotId: Int, status: KOTStatus): Flow<Result<Unit>> = flow {
        try {
            val statusUpdate = KOTStatusUpdate(kotId, status)
            val response = apiService.updateKOTStatus(kotId, statusUpdate, sessionManager.getCompanyCode()?:"")

            if (response.isSuccessful) {
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(Exception("Failed to update KOT status: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
