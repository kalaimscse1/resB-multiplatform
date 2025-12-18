package com.warriortech.resb.data.repository

import com.warriortech.resb.data.local.entity.SyncStatus
import com.warriortech.resb.model.Modifiers
import com.warriortech.resb.model.OrderItemModifier
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.ResponseBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map
import kotlin.let

@Singleton
class ModifierRepository @Inject constructor(
    private val apiService: ApiService,
    networkMonitor: NetworkMonitor,
    private val sessionManager: SessionManager
) : OfflineFirstRepository(networkMonitor) {

    suspend fun getAllModifiers(): List<Modifiers>{
        val response = apiService.getAllModifiers(sessionManager.getCompanyCode()?:"")
        return if (response.isSuccessful){
            response.body()!!
        }
        else{
            emptyList()
        }
    }

    suspend fun createModifier(modifier: Modifiers): Result<Modifiers> {
        return try {
            if (isOnline()) {
                val response = apiService.createModifier(modifier,sessionManager.getCompanyCode()?:"")
                if (response.isSuccessful) {
                    response.body()?.let { createdModifier ->
                        Result.success(createdModifier)
                    } ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("API Error: ${response.code()}"))
                }
            } else {
                // Store locally with pending sync status
                Result.success(modifier)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateModifier(modifier: Modifiers): Result<Modifiers> {
        return try {
            if (isOnline()) {
                val response = apiService.updateModifier(modifier.add_on_id, modifier,sessionManager.getCompanyCode()?:"")
                if (response.isSuccessful) {
                    response.body()?.let { updatedModifier ->
                        Result.success(updatedModifier)
                    } ?: Result.failure(Exception("Empty response"))
                } else {
                    Result.failure(Exception("API Error: ${response.code()}"))
                }
            } else {
                Result.success(modifier)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteModifier(modifierId: Long): retrofit2.Response<ResponseBody> {
        val response = apiService.deleteModifier(modifierId,sessionManager.getCompanyCode()?:"")
        return response
    }

    fun getModifierGroupsForMenuItem(menuItemId: Long): Flow<Result<List<Modifiers>>> = flow {
        try {
            val tenantId = sessionManager.getCompanyCode()
            if (tenantId.isNullOrEmpty()) {
                emit(Result.failure(Exception("Tenant ID not found")))
                return@flow
            }

            val response = apiService.getModifierGroupsForMenuItem(menuItemId, tenantId)
            if (response.isSuccessful) {
                val modifierGroups = response.body() ?: emptyList()
                emit(Result.success(modifierGroups))
            } else {
                emit(Result.failure(Exception("Failed to fetch modifier groups: ${response.message()}")))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching modifier groups")
            emit(Result.failure(e))
        }
    }

    suspend fun getModifiersByMenuItems(menuItemId: Long): Flow<Result<List<Modifiers>>> = flow {
        try {
            val response = apiService.getModifiersByMenuItem(menuItemId, sessionManager.getCompanyCode()?:"")
            if (response.isNotEmpty()) {
                emit(Result.success(response))
            } else {
                emit(Result.success(emptyList()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching modifiers for menu item")
            emit(Result.failure(e))
        }
    }
    }
