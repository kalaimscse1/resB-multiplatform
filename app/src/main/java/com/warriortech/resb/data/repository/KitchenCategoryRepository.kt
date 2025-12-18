package com.warriortech.resb.data.repository

import com.warriortech.resb.model.KitchenCategory
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class KitchenCategoryRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun getAllKitchenCategories(): List<KitchenCategory> {
        return try {
            apiService.getAllKitchenCategories(sessionManager.getCompanyCode()?:"").body()!!
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getKitchenCategory(id: Int): KitchenCategory? {
        return try {
            apiService.getKitchenCategoryById(id,sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createKitchenCategory(printer: KitchenCategory): KitchenCategory? {
        return try {
            apiService.createKitchenCategory(printer,sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateKitchenCategory(printer: KitchenCategory): Int? {
        return try {
            apiService.updateKitchenCategory(printer.kitchen_cat_id, printer,sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteKitchenCategory(id: Long): Boolean {
        return try {
            apiService.deleteKitchenCategory(id,sessionManager.getCompanyCode()?:"")
            true
        } catch (e: Exception) {
            false
        }
    }
}
