package com.warriortech.resb.data.repository

import com.warriortech.resb.model.Menu
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun getAllMenus(): List<Menu> {
        val response = apiService.getAllMenus(sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to fetch menus: ${response.message()}")
        }
    }

    suspend fun insertMenu(menu: Menu): Menu {
        val response = apiService.createMenu(menu,sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Failed to create menu")
        } else {
            throw Exception("Failed to create menu: ${response.message()}")
        }
    }

    suspend fun updateMenu(menu: Menu): Int {
        val response = apiService.updateMenu(menu.menu_id, menu,sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Failed to update menu")
        } else {
            throw Exception("Failed to update menu: ${response.message()}")
        }
    }

    suspend fun deleteMenu(menuId: Long): Response<ResponseBody> {
        val response = apiService.deleteMenu(menuId,sessionManager.getCompanyCode()?:"")
       return response
    }
    suspend fun getOrderBy(): Map<String, Long>{
        val response = apiService.getOrderBy(sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: emptyMap()
        }
        else{
            throw Exception("Failed to get OrderBy: ${response.message()}")
        }
    }
}
