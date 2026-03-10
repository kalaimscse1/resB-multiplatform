package com.warriortech.resb.data.repository

import com.warriortech.resb.model.TblAuditingResponse
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.util.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class AuditingReportRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    override val networkMonitor: NetworkMonitor,

) : OfflineFirstRepository(networkMonitor) {

    suspend fun getAuditingReport(): List<TblAuditingResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAuditing(sessionManager.getCompanyCode()?:"")
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}