package com.warriortech.resb.data.repository

import com.warriortech.resb.model.*
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnitConversionRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    private fun getTenantId() = sessionManager.getCompanyCode() ?: ""

    suspend fun findByUnitId(unitId: Long): List<TblUnitConversionResponse> {
        val response = apiService.getUnitConversionByUnitId(unitId, getTenantId())
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    suspend fun findAllActive(): List<TblUnitConversionResponse> {
        return try {
            val response = apiService.findAllActiveUnitConversions(getTenantId())
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun create(request: TblUnitConversionRequest): Result<TblUnitConversionResponse> {
        return try {
            val response = apiService.createUnitConversion(request, getTenantId())
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create unit conversion: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun update(id: Long, request: TblUnitConversionRequest): Result<TblUnitConversionResponse> {
        return try {
            val response = apiService.updateUnitConversion(id, request, getTenantId())
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update unit conversion: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(id: Long): Result<Boolean> {
        return try {
            val response = apiService.deleteUnitConversion(id, getTenantId())
            if (response.isSuccessful) {
                Result.success(response.body() ?: false)
            } else {
                Result.failure(Exception("Failed to delete unit conversion: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllUnits(): List<TblUnit> {
        val response = apiService.getAllUnits(getTenantId())
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }
}
