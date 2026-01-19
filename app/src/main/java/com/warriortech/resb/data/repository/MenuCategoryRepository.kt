package com.warriortech.resb.data.repository

import com.warriortech.resb.model.KitchenCategory
import com.warriortech.resb.model.MenuCategory
import com.warriortech.resb.model.TblUnit
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import okhttp3.Response
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuCategoryRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun getAllCategories(): List<MenuCategory> {
        val response = apiService.getAllMenuCategories(sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to fetch categories: ${response.message()}")
        }
    }

    suspend fun insertCategory(category: MenuCategory): MenuCategory {
        val response = apiService.createMenuCategory(category,sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Failed to create category")
        } else {
            throw Exception("Failed to create category: ${response.message()}")
        }
    }

    suspend fun updateCategory(category: MenuCategory): Int {
        val response = apiService.updateMenuCategory(category.item_cat_id, category,sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Failed to update category")
        } else {
            throw Exception("Failed to update category: ${response.message()}")
        }
    }

    suspend fun deleteCategory(categoryId: Long): retrofit2.Response<ResponseBody> {
        val response = apiService.deleteMenuCategory(categoryId,sessionManager.getCompanyCode()?:"")
        return response
    }

    suspend fun getAllKitchenCategories(): List<KitchenCategory> {
        try {
            val response = apiService.getAllKitchenCategories(sessionManager.getCompanyCode()?:"")
            if (response.isSuccessful) {
                return response.body() ?: emptyList()
            } else {
                throw Exception("Failed to fetch categories: ${response.message()}")
            }
        }catch (e: Exception){
            throw e
        }
    }

    suspend fun getAllUnits(): List<TblUnit> {
        val response = apiService.getAllUnits(sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to fetch units: ${response.message()}")
        }
    }

    suspend fun getOrderBy(): Map<String, Long>{
        val response = apiService.getMenuCategoryOrderBy(sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: emptyMap()
        }
        else{
            throw Exception("Failed to get OrderBy: ${response.message()}")
        }
    }
}
