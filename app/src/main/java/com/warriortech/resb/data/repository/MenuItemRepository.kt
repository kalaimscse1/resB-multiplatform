package com.warriortech.resb.data.repository

import androidx.lifecycle.viewmodel.compose.viewModel
import com.warriortech.resb.data.local.dao.MenuItemDao
import com.warriortech.resb.data.local.entity.SyncStatus
import com.warriortech.resb.data.local.entity.TblMenuItem
import com.warriortech.resb.model.Menu
import com.warriortech.resb.model.TblMenuItemRequest
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.util.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuItemRepository @Inject constructor(
    private val menuItemDao: MenuItemDao,
    private val apiService: ApiService,
    networkMonitor: NetworkMonitor,
    private val sessionManager: SessionManager
) : OfflineFirstRepository(networkMonitor) {

//    fun getAllMenuItems(): Flow<List<TblMenuItemResponse>> {
//
//        val response = apiService.getMenuItems(sessionManager.getCompanyCode()?:"")
//        return menuItemDao.getAllMenuItems()
//            .map { entities -> entities.map { it.toModel() } }
//            .onStart {
//                if (isOnline()) {
//                    syncMenuItemsFromRemote()
//                }
//            }
//    }


    suspend fun getAllMenuItems(): Flow<List<TblMenuItemResponse>> = flow{
        val response = apiService.getMenuItems(sessionManager.getCompanyCode()?:"")
        syncMenuItemsFromRemote()
        if (response.isSuccessful) {
            val menuItems = response.body()
            if (menuItems != null) {
                emit(menuItems)
            } else {
                emit(emptyList())
            }
        } else {
            emit(emptyList())
        }
    }
    suspend fun getMenuItems(category: String? = null): Flow<Result<List<TblMenuItemResponse>>> = flow {
        try {
//            getAllMenuItems()
            val response = apiService.getMenuItems(sessionManager.getCompanyCode()?:"")

            if (response.isSuccessful) {
                val menuItems = response.body()
                if (menuItems != null && category !="FAVOURITES") {
                    // If category is provided, filter by category
                    val filteredItems = if (category != null) {
                        menuItems.filter { it.item_cat_name == category }
                    } else {
                        menuItems
                    }
                    emit(Result.success(filteredItems))
                } else if (menuItems != null){
                    val filteredItems =
                        menuItems.filter { it.is_favourite == true }
                    emit(Result.success(filteredItems))
                }
                else
                    {
                        emit(Result.failure(Exception("No menu items data received")))
                    }

            } else {
                emit(Result.failure(Exception("Error fetching menu items: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }


    suspend fun getMenus(): List<Menu>{
        val response = apiService.getAllMenus(sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to fetch menus: ${response.message()}")
        }
    }
    suspend fun getMenuItemById(id: Long): TblMenuItemResponse? {
        return menuItemDao.getMenuItemById(id)?.toModel()
    }

    private suspend fun syncMenuItemsFromRemote() {
        safeApiCall(
            onSuccess = { remoteItems: List<TblMenuItemResponse> ->
                withContext(Dispatchers.IO) {
                    val entities = remoteItems.map { it.toEntity()
                    .copy(
                            is_synced = SyncStatus.SYNCED,
                            last_synced_at = System.currentTimeMillis()
                        ) }
                    menuItemDao.insertMenuItems(entities)
                }
            },
            apiCall = { apiService.getAllMenuItems(sessionManager.getCompanyCode()?:"").body()!! }
        )
    }

    suspend fun forceSyncAllMenuItems() {
        if (isOnline()) {
            syncMenuItemsFromRemote()
        }
    }


    suspend fun insertMenuItem(menuItem: TblMenuItemRequest): TblMenuItemResponse {
        val response = apiService.createMenuItem(menuItem,sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Failed to create menu item")
        } else {
            throw Exception("Failed to create menu item: ${response.message()}")
        }
    }

    suspend fun updateMenuItem(menuItem: TblMenuItemRequest): Int {
        val response = apiService.updateMenuItem(menuItem.menu_item_id, menuItem,sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Failed to update menu item")
        } else {
            throw Exception("Failed to update menu item: ${response.message()}")
        }
    }

    suspend fun deleteMenuItem(menuItemId: Int): Response<ResponseBody> {
        val response = apiService.deleteMenuItem(menuItemId,sessionManager.getCompanyCode()?:"")
        return response
    }

    suspend fun printMenuItemsReport(menuItems: List<TblMenuItemResponse>,paperWidth:Int): Response<ResponseBody> {
        val response = apiService.printMenuItems(paperWidth,menuItems ,sessionManager.getCompanyCode()?:"")
        return response
    }

    suspend fun getOrderBy(): Map<String, Long>{
        val response = apiService.getMenuItemOrderBy(sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: emptyMap()
        }
        else{
            throw Exception("Failed to get OrderBy: ${response.message()}")
        }
    }
}

// Extension functions
private fun TblMenuItemResponse.toEntity() = TblMenuItem(
    menu_item_id = menu_item_id.toInt(),
    menu_item_code = menu_item_code,
    menu_item_name = menu_item_name,
    menu_item_name_tamil = menu_item_name_tamil,
    menu_id = menu_id.toInt(),
    rate = rate,
    image = image,
    ac_rate = ac_rate,
    parcel_rate = parcel_rate,
    is_available = is_available,
    item_cat_id = item_cat_id.toInt(),
    parcel_charge = parcel_charge,
    tax_id = tax_id.toInt(),
    kitchen_cat_id = kitchen_cat_id.toInt(),
    stock_maintain = stock_maintain,
    rate_lock = rate_lock,
    unit_id = unit_id.toInt(),
    min_stock = min_stock.toInt(),
    hsn_code = hsn_code,
    order_by = order_by.toInt(),
    is_inventory = is_inventory.toInt(),
    is_raw = is_raw,
    cess_specific = cess_specific,
    is_favourite = is_favourite,
    is_active = is_active==1L,
    preparation_time = preparation_time.toInt()
)

private fun TblMenuItem.toModel() = TblMenuItemResponse(
    menu_item_id = this.menu_item_id.toLong(),
    menu_item_code = this.menu_item_code ?: "",
    menu_item_name = this.menu_item_name.toString(),
    menu_item_name_tamil = this.menu_item_name_tamil.toString(),
    menu_id = this.menu_id?.toLong() ?: 0L,
    menu_name = "",
    rate = this.rate!!,
    item_cat_id = this.item_cat_id?.toLong() ?: 0L,
    item_cat_name = "",
    image = this.image.toString(),
    ac_rate = this.ac_rate ?: 0.0,
    is_available = this.is_available ?: "YES",
    parcel_rate = this.parcel_rate ?: 0.0,
    parcel_charge = this.parcel_charge ?: 0.0,
    tax_id = this.tax_id?.toLong() ?: 0L,
    tax_name = "",
    tax_percentage = "",
    kitchen_cat_id = this.kitchen_cat_id?.toLong() ?: 0L,
    kitchen_cat_name = "",
    stock_maintain = this.stock_maintain ?: "NO",
    rate_lock = this.rate_lock ?: "NO",
    unit_id = this.unit_id?.toLong() ?: 0L,
    unit_name = "",
    min_stock = this.min_stock?.toLong() ?: 0L,
    hsn_code = this.hsn_code ?: "",
    order_by = this.order_by?.toLong() ?: 0L,
    is_inventory = this.is_inventory?.toLong() ?: 0L,
    is_raw = this.is_raw ?: "NO",
    cess_per = "",
    cess_specific = this.cess_specific!!,
    is_favourite = this.is_favourite!!,
    is_active = if (this.is_active == true) 1L else 0L,
    preparation_time = this.preparation_time?.toLong() ?: 0L
)