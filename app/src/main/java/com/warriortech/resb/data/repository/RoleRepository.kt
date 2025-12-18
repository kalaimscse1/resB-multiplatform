package com.warriortech.resb.data.repository

import com.warriortech.resb.network.ApiService
import com.warriortech.resb.model.Role
import com.warriortech.resb.network.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoleRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun getAllRoles(): List<Role> {
        return try {
            apiService.getRoles(sessionManager.getCompanyCode()?:"").body()!!
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRoleById(id: Int): Role? {
        return try {
            apiService.getRoleById(id,sessionManager.getCompanyCode()?:"").body()!!
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createRole(role: Role): Role? {
        return try {
            apiService.createRole(role,sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateRole(role: Role): Int? {
        return try {
            apiService.updateRole(role.role_id, role,sessionManager.getCompanyCode()?:"").body()!!
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteRole(id: Long): Boolean {
        return try {
            apiService.deleteRole(id,sessionManager.getCompanyCode()?:"")
            true
        } catch (e: Exception) {
            false
        }
    }
}
