package com.warriortech.resb.data.repository

import com.warriortech.resb.model.GSTRDOCS
import com.warriortech.resb.model.HsnReport
import com.warriortech.resb.model.ReportGSTResponse
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GstRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
){
    suspend fun getGstReport(fromDate: String, toDate: String): Flow<Result<List<ReportGSTResponse>>> = flow{
        try {
            val tenantId = sessionManager.getCompanyCode() ?: ""
            val response = apiService.getGSTReport(tenantId, fromDate, toDate)
            if (response.isSuccessful) {
                response.body()?.let { gstReports ->
                    emit(Result.success(gstReports))
                } ?: emit(Result.failure(Exception("No data received")))
            } else {
                emit(Result.failure(Exception("API Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }

    }

    suspend fun getHsnReport(fromDate: String, toDate: String): Flow<Result<List<HsnReport>>> = flow{
        try {
            val tenantId = sessionManager.getCompanyCode() ?: ""
            val response = apiService.getHsnReport(tenantId, fromDate, toDate)
            if (response.isSuccessful) {
                response.body()?.let { hsnReports ->
                    emit(Result.success(hsnReports))
                } ?: emit(Result.failure(Exception("No data received")))
            } else {
                emit(Result.failure(Exception("API Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }

    }

    suspend fun getGstDocs(fromDate: String, toDate: String): Flow<Result<List<GSTRDOCS>>> = flow{
        try {
            val tenantId = sessionManager.getCompanyCode() ?: ""
            val response = apiService.getGstDocs(tenantId, fromDate, toDate)
            if (response.isSuccessful) {
                response.body()?.let { gstDocs ->
                    emit(Result.success(gstDocs))
                } ?: emit(Result.failure(Exception("No data received")))
            } else {
                emit(Result.failure(Exception("API Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }

    }
}