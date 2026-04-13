package com.warriortech.resb.data.repository

import com.warriortech.resb.model.*
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpiTypeRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    private fun getTenantId() = sessionManager.getCompanyCode() ?: ""

    suspend fun getAllActive(): List<TblUpiType> {
        val response = apiService.getAllActiveUpiTypes(getTenantId())
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    suspend fun create(upiType: TblUpiType): Result<TblUpiType> {
        return try {
            val response = apiService.createUpiType(upiType, getTenantId())
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create UPI type"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun update(upiType: TblUpiType): Result<Int> {
        return try {
            val response = apiService.updateUpiType(upiType.upi_type_id, upiType, getTenantId())
            if (response.isSuccessful) {
                Result.success(response.body() ?: 0)
            } else {
                Result.failure(Exception("Failed to update UPI type"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(id: Long): Result<Int> {
        return try {
            val response = apiService.deleteUpiType(id, getTenantId())
            if (response.isSuccessful) {
                Result.success(response.body() ?: 0)
            } else {
                Result.failure(Exception("Failed to delete UPI type"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkExists(name: String): Boolean {
        return try {
            val response = apiService.checkUpiTypeExists(name, getTenantId())
            response.isSuccessful && response.body()?.data == true
        } catch (e: Exception) {
            false
        }
    }
}
