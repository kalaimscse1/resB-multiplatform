package com.warriortech.resb.data.repository

import com.warriortech.resb.model.ApiResponse
import com.warriortech.resb.model.TblGroupDetails
import com.warriortech.resb.model.TblGroupNature
import com.warriortech.resb.model.TblGroupRequest
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import okhttp3.ResponseBody
import javax.inject.Inject

class GroupRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun getGroups(): List<TblGroupDetails>? {
        return try {
            apiService.getAllGroups(sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getGroupById(groupId: Int): TblGroupDetails? {
        return try {
            apiService.getGroupById(groupId, sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createGroup(group: TblGroupRequest): TblGroupDetails? {
        return try {
            apiService.createGroup(group, sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateGroup(groupId: Int, group: TblGroupRequest): Boolean? {
        return try {
            apiService.updateGroup(groupId, group, sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteGroup(groupId: Int): Boolean? {
        return try {
            apiService.deleteGroup(groupId, sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getGroupNatures():List<TblGroupNature>?{
        return try {
            apiService.getGroupNatures(sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getOrderBy(): Map<String, Long>{
        val response = apiService.getMaxOrderBy(sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: emptyMap()
        }
        else{
            throw Exception("Failed to get OrderBy: ${response.message()}")
        }
    }

    suspend fun checkExists(groupName:String): ApiResponse<Boolean>{
        return try {
            apiService.checkExistsOrNotGroup(groupName, sessionManager.getCompanyCode()?:"").body()!!
        }catch (e:Exception){
            ApiResponse(
                false,
                message ="",
                data = false
            )
        }
    }
}