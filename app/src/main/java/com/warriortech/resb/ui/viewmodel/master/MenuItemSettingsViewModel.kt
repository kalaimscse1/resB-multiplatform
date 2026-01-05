package com.warriortech.resb.ui.viewmodel.master

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.BillRepository
import com.warriortech.resb.data.repository.MenuCategoryRepository
import com.warriortech.resb.data.repository.MenuItemRepository
import com.warriortech.resb.data.repository.MenuRepository
import com.warriortech.resb.data.repository.OrderRepository
import com.warriortech.resb.data.repository.TaxRepository
import com.warriortech.resb.model.*
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.screens.settings.MenuItemSettingsUiState
import com.warriortech.resb.ui.viewmodel.master.MenuCategorySettingsViewModel.UiState
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.PrinterHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MenuItemSettingsViewModel @Inject constructor(
    private val menuItemRepository: MenuItemRepository,
    private val menuRepository: MenuRepository,
    private val menuCategoryRepository: MenuCategoryRepository,
    private val taxRepository: TaxRepository,
    private val sessionManager: SessionManager,
    private val printerHelper: PrinterHelper,
    private val orderRepository: OrderRepository
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow<MenuItemSettingsUiState>(MenuItemSettingsUiState.Loading)
    val uiState: StateFlow<MenuItemSettingsUiState> = _uiState.asStateFlow()

    // Master data
    private val _menus = MutableStateFlow<List<Menu>>(emptyList())
    val menus: StateFlow<List<Menu>> = _menus.asStateFlow()

    private val _menuCategories = MutableStateFlow<List<MenuCategory>>(emptyList())
    val menuCategories: StateFlow<List<MenuCategory>> = _menuCategories.asStateFlow()

    private val _kitchenCategories = MutableStateFlow<List<KitchenCategory>>(emptyList())
    val kitchenCategories: StateFlow<List<KitchenCategory>> = _kitchenCategories.asStateFlow()

    private val _taxes = MutableStateFlow<List<Tax>>(emptyList())
    val taxes: StateFlow<List<Tax>> = _taxes.asStateFlow()

    private val _units = MutableStateFlow<List<TblUnit>>(emptyList())
    val units: StateFlow<List<TblUnit>> = _units.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ✅ Separate state for all items and filtered items
    private val _menuItems = MutableStateFlow<List<TblMenuItemResponse>>(emptyList())
    val menuItems: StateFlow<List<TblMenuItemResponse>> = _menuItems.asStateFlow()

    private val _orderBy = MutableStateFlow<String>("")
    val orderBy: StateFlow<String> = _orderBy.asStateFlow()

    private val _filteredMenuItems = MutableStateFlow<List<TblMenuItemResponse>>(emptyList())
    val filteredMenuItems: StateFlow<List<TblMenuItemResponse>> = _filteredMenuItems.asStateFlow()

    init {
        CurrencySettings.update(
            symbol = sessionManager.getRestaurantProfile()?.currency ?: "",
            decimals = sessionManager.getRestaurantProfile()?.decimal_point?.toInt() ?: 2
        )
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // ✅ Improved search using full list reference
    fun searchMenuItems(query: String) {
        val allItems = _menuItems.value
        val filtered = if (query.isBlank()) {
            allItems
        } else {
            allItems.filter {
                it.menu_item_name.contains(query, ignoreCase = true)
                        || it.menu_item_name_tamil.contains(query, ignoreCase = true)
            }
        }
        _filteredMenuItems.value = filtered
    }

    // ✅ Load all menu items and supporting data
    fun loadMenuItems() {
        viewModelScope.launch {
            try {
                _uiState.value = MenuItemSettingsUiState.Loading

                // Run all API calls concurrently
                val menusDeferred = async { menuRepository.getAllMenus() }
                val menuCategoriesDeferred = async { menuCategoryRepository.getAllCategories() }
                val taxesDeferred = async { taxRepository.getAllTaxes() }
                val kitchenCategoriesDeferred = async { menuCategoryRepository.getAllKitchenCategories() }
                val unitsDeferred = async { menuCategoryRepository.getAllUnits() }

                // Await results
                val menus = menusDeferred.await()
                val menuCategories = menuCategoriesDeferred.await()
                val taxes = taxesDeferred.await()
                val kitchenCategories = kitchenCategoriesDeferred.await()
                val units = unitsDeferred.await()

                // Update master data
                _menus.value = menus
                _menuCategories.value = menuCategories
                _taxes.value = taxes
                _kitchenCategories.value = kitchenCategories
                _units.value = units

                // Collect menu items last
                menuItemRepository.getAllMenuItems().collect { items ->
                    _menuItems.value = items
                    _filteredMenuItems.value = items // Default view = all items
                    _uiState.value = MenuItemSettingsUiState.Success(items)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = MenuItemSettingsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ✅ Add item
    fun addMenuItem(menuItem: TblMenuItemRequest) {
        viewModelScope.launch {
            try {
                menuItemRepository.insertMenuItem(menuItem)
                _errorMessage.value = "Menu item added successfully"
                loadMenuItems()
            } catch (e: Exception) {
                _uiState.value =
                    MenuItemSettingsUiState.Error(e.message ?: "Failed to add menu item")
            }
        }
    }

    // ✅ Update item
    fun updateMenuItem(menuItem: TblMenuItemRequest) {
        viewModelScope.launch {
            try {
                menuItemRepository.updateMenuItem(menuItem)
                _errorMessage.value = "Menu item updated successfully"
                loadMenuItems()
            } catch (e: Exception) {
                _uiState.value =
                    MenuItemSettingsUiState.Error(e.message ?: "Failed to update menu item")
            }
        }
    }

    fun getOrderBy() {
        viewModelScope.launch {
            try {
                val response = menuItemRepository.getOrderBy()
                _orderBy.value = response["order_by"].toString()
            } catch (e: Exception) {
                _uiState.value = MenuItemSettingsUiState.Error(e.message ?: "Failed to getOrderBy")
            }
        }
    }
    // ✅ Delete item
    fun deleteMenuItem(menuItemId: Int) {
        viewModelScope.launch {
            try {
                val response = menuItemRepository.deleteMenuItem(menuItemId)
                when (response.code()) {
                    in 200..299 -> {
                        loadMenuItems()
                        _errorMessage.value = "Menu item deleted successfully"
                    }
                    else -> {
                        _errorMessage.value = response.errorBody()?.string()
                    }
                }
            } catch (e: Exception) {
                _uiState.value =
                    MenuItemSettingsUiState.Error(e.message ?: "Failed to delete menu item")
            }
        }
    }
    @SuppressLint("DefaultLocale")
    fun printMenuItems(menuItems:List<TblMenuItemResponse>, paperWidth:Int) {
        viewModelScope.launch {
            try {
                val data = menuItemRepository.printMenuItemsReport(menuItems, paperWidth)
                val ip = orderRepository.getIpAddress("COUNTER")
                printerHelper.printViaTcp(ip, data=data.body()?.bytes()!!){
                    success, message ->
                    if (!success) {
                        Timber.e("Print failed: $message")
                    }
                }
            } catch (e: Exception) {
                _uiState.value =
                    MenuItemSettingsUiState.Error(e.message ?: "Failed to print menu items")
            }
        }
    }
}
