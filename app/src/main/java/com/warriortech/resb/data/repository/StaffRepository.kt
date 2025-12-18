package com.warriortech.resb.data.repository

import com.warriortech.resb.model.ChangePasswordRequest
import com.warriortech.resb.model.TblStaff
import com.warriortech.resb.model.TblStaffRequest
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaffRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun getAllStaff(): List<TblStaff> {
        val response = apiService.getAllStaff(sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to fetch staff: ${response.message()}")
        }
    }

    suspend fun insertStaff(staff: TblStaffRequest): TblStaff {
        val response = apiService.createStaff(staff,sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Failed to create staff")
        } else {
            throw Exception("Failed to create staff: ${response.message()}")
        }
    }

    suspend fun updateStaff(staff: TblStaffRequest): Int {
        val response = apiService.updateStaff(staff.staff_id, staff,sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Failed to update staff")
        } else {
            throw Exception("Failed to update staff: ${response.message()}")
        }
    }

    suspend fun deleteStaff(staffId: Long) {
        val response = apiService.deleteStaff(staffId,sessionManager.getCompanyCode()?:"")
        if (!response.isSuccessful) {
            throw Exception("Failed to delete staff: ${response.message()}")
        }
    }


    suspend fun changePassword(currentPassword: String, newPassword: String): Result<String> {
        return try {
            val request = ChangePasswordRequest(currentPassword, newPassword)
            val response = apiService.changePassword(sessionManager.getUser()?.staff_id?:0, request,sessionManager.getCompanyCode()?:"")
            if (response.success) {
                Result.success(response.message)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}