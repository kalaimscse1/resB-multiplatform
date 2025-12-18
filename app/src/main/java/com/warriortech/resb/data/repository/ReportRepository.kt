package com.warriortech.resb.data.repository

import com.warriortech.resb.model.*
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.util.getCurrentDateModern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun getTodaySales(): Flow<Result<TodaySalesReport>> = flow {
        try {
            val response = apiService.getTodaySales(sessionManager.getCompanyCode()?:"")
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Failed to fetch today's sales: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("Network error: ${e.message}")))
        }
    }

    suspend fun getReportsForDate(date: String?): ReportResponse {
        // Pass null/blank to let backend default to "today"
        val safe = date?.takeIf { it.isNotBlank() }
        return apiService.getReportsForDate(safe)
    }
    suspend fun getGSTSummary(): Flow<Result<GSTSummaryReport>> = flow {
        try {
            val response = apiService.getGSTSummary(sessionManager.getCompanyCode()?:"")
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Failed to fetch GST summary: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("Network error: ${e.message}")))
        }
    }

//    suspend fun getSalesSummaryByDate(date: String): Flow<Result<SalesSummaryReport>> = flow {
//        try {
//            val response = apiService.getSalesSummaryByDate(date,sessionManager.getCompanyCode()?:"")
//            if (response.isSuccessful && response.body() != null) {
//                emit(Result.success(response.body()!!))
//            } else {
//                emit(Result.failure(Exception("Failed to fetch sales summary: ${response.message()}")))
//            }
//        } catch (e: Exception) {
//            emit(Result.failure(Exception("Network error: ${e.message}")))
//        }
//    }

    suspend fun getSalesReport(fromDate: String, toDate: String): Flow<Result<List<TblBillingResponse>>> = flow {
        try {
            val response = apiService.getSalesReport(
                sessionManager.getCompanyCode() ?: "",
                fromDate,
                toDate
            )
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Failed: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("Network error: ${e.message}")))
        }
    }


    suspend fun getItemReport(fromDate: String, toDate: String): Flow<Result<List<ItemReport>>> = flow{
        try {
            val response = apiService.getItemReport(
                sessionManager.getCompanyCode() ?: "",
                fromDate,
                toDate
            )
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Failed: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("Network error: ${e.message}")))
        }
    }


    suspend fun getCategoryReport(fromDate: String, toDate: String): Flow<Result<List<CategoryReport>>> = flow {
        try {
            val response = apiService.getCategoryReport(
                sessionManager.getCompanyCode() ?: "",
                fromDate,
                toDate
            )
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Failed: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("Network error: ${e.message}")))
        }
    }

}
