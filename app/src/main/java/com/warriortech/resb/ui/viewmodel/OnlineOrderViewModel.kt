package com.warriortech.resb.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.BillRepository
import com.warriortech.resb.data.repository.MenuItemRepository
import com.warriortech.resb.data.repository.OrderRepository
import com.warriortech.resb.model.*
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.ui.viewmodel.payment.PaymentMethod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OnlineOrderViewModel @Inject constructor(
    private val menuRepository: MenuItemRepository,
    private val orderRepository: OrderRepository,
    private val billRepository: BillRepository,
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _onlinePlatforms = MutableStateFlow<List<TblOnline>>(emptyList())
    val onlinePlatforms: StateFlow<List<TblOnline>> = _onlinePlatforms.asStateFlow()

    private val _selectedPlatform = MutableStateFlow<TblOnline?>(null)
    val selectedPlatform: StateFlow<TblOnline?> = _selectedPlatform.asStateFlow()

    private val _refNo = MutableStateFlow("")
    val refNo: StateFlow<String> = _refNo.asStateFlow()

    private val _cartItems = MutableStateFlow<Map<TblMenuItemResponse, Int>>(emptyMap())
    val cartItems: StateFlow<Map<TblMenuItemResponse, Int>> = _cartItems.asStateFlow()

    private val _menuState = MutableStateFlow<MenuViewModel.MenuUiState>(MenuViewModel.MenuUiState.Loading)
    val menuState: StateFlow<MenuViewModel.MenuUiState> = _menuState.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String>("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _uiState = MutableStateFlow<OnlineOrderUiState>(OnlineOrderUiState.Idle)
    val uiState: StateFlow<OnlineOrderUiState> = _uiState.asStateFlow()

    private val _showAlert = MutableStateFlow<String?>(null)
    val showAlert: StateFlow<String?> = _showAlert.asStateFlow()

    val newOrderId = MutableStateFlow<String?>(null)

    sealed class OnlineOrderUiState {
        object Idle : OnlineOrderUiState()
        object Loading : OnlineOrderUiState()
        data class Success(val billNo: String) : OnlineOrderUiState()
        data class Error(val message: String) : OnlineOrderUiState()
    }

    init {
        loadPlatforms()
        loadMenu()
        viewModelScope.launch {
            try {
                val new = apiService.getOrderNo(
                    sessionManager.getCompanyCode() ?: "",
                    sessionManager.getUser()?.counter_id ?: 0,
                    "ORDER"
                )
                newOrderId.value = new["order_master_id"]
            } catch (e: Exception) {
                Timber.e(e, "Error fetching new order ID")
            }
        }
    }

    private fun loadPlatforms() {
        viewModelScope.launch {
            try {
                val response = apiService.getAllOnlineOrders(sessionManager.getCompanyCode() ?: "")
                if (response.isSuccessful) {
                    _onlinePlatforms.value = response.body()?.filter { it.online_order_name != "--" } ?: emptyList()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load online platforms")
            }
        }
    }

    fun loadMenu() {
        viewModelScope.launch {
            _menuState.value = MenuViewModel.MenuUiState.Loading
            menuRepository.getMenuItems().collect { result ->
                result.fold(
                    onSuccess = { items ->
                        _menuState.value = MenuViewModel.MenuUiState.Success(items)
                        _categories.value = listOf("ALL") + items.map { it.item_cat_name }.distinct()
                    },
                    onFailure = {
                        _menuState.value = MenuViewModel.MenuUiState.Error(it.message ?: "Error")
                    }
                )
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectPlatform(platform: TblOnline) {
        _selectedPlatform.value = platform
    }

    fun updateRefNo(ref: String) {
        _refNo.value = ref
    }

    private fun getTotalItemQtyInCart(menuItem: TblMenuItemResponse): Int {
        return _cartItems.value[menuItem] ?: 0
    }

    private suspend fun syncTmpStock(menuItem: TblMenuItemResponse, newQty: Int) {
        if (sessionManager.getGeneralSetting()?.is_inventory != true) return
        val orderIdStr = newOrderId.value ?: ""
        val tenantId = sessionManager.getCompanyCode() ?: ""

        try {
            if (newQty <= 0) {
                apiService.deleteByOrderIdAndItemId(orderIdStr, menuItem.menu_item_id, tenantId)
                return
            }

            val response = apiService.checkExists(orderIdStr, menuItem.menu_item_id, tenantId)
            if (response.data == true) {
                apiService.updateQty(orderIdStr, menuItem.menu_item_id, newQty.toDouble(), tenantId)
            } else {
                apiService.createTmpItemMaster(
                    TblTmpItemMasterRequest(
                        order_id = orderIdStr,
                        item_id = menuItem.menu_item_id,
                        tmp_qty = newQty.toDouble(),
                        is_active = 1L
                    ),
                    tenantId
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing temporary stock")
        }
    }

    fun addToCart(item: TblMenuItemResponse) {
        viewModelScope.launch {
            val currentQty = getTotalItemQtyInCart(item)
            val newQty = currentQty + 1
            if (sessionManager.getGeneralSetting()?.is_inventory == true) {
                if (!performStockCheck(item, newQty)) return@launch
                syncTmpStock(item, newQty)
            }
            val current = _cartItems.value.toMutableMap()
            current[item] = newQty
            _cartItems.value = current
        }
    }

    fun removeFromCart(item: TblMenuItemResponse) {
        viewModelScope.launch {
            val currentQty = getTotalItemQtyInCart(item)
            if (currentQty <= 0) return@launch
            val newQty = currentQty - 1
            
            if (sessionManager.getGeneralSetting()?.is_inventory == true) {
                syncTmpStock(item, newQty)
            }
            
            val current = _cartItems.value.toMutableMap()
            if (newQty > 0) current[item] = newQty else current.remove(item)
            _cartItems.value = current
        }
    }

    private suspend fun performStockCheck(menuItem: TblMenuItemResponse, targetQty: Int): Boolean {
        try {
            val tenantId = sessionManager.getCompanyCode() ?: ""
            val response = menuRepository.getItemMasterById(menuItem.menu_item_id)
            if (response.isSuccessful) {
                val itemMaster = response.body()
                if (itemMaster != null) {
                    val tmpStockResponse = apiService.sumQty(menuItem.menu_item_id, tenantId)
                    val totalTempHoldsByAll = tmpStockResponse["tmp_qty"] ?: 0.0
                    val myCurrentQty = getTotalItemQtyInCart(menuItem)
                    
                    val availableStock = itemMaster.qty - (totalTempHoldsByAll - myCurrentQty.toDouble())

                    if (availableStock <= 0.00) {
                        _showAlert.value = "${menuItem.menu_item_name} is Out of Stock."
                        return false
                    }
                    if (availableStock < (targetQty - myCurrentQty).toDouble() + myCurrentQty) { // targetQty is the absolute total wanted
                         if (availableStock < targetQty.toDouble()) {
                            _showAlert.value = "Only $availableStock Stock remaining for ${menuItem.menu_item_name}."
                            return false
                        }
                    }
                    
                    if (menuItem.stock_maintain.equals("YES", ignoreCase = true)) {
                        if (availableStock <= menuItem.min_stock.toDouble()) {
                            _showAlert.value = "Stock Alert: ${menuItem.menu_item_name} is at Minimum Stock Level ($availableStock remaining)"
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking stock")
        }
        return true
    }

    fun dismissAlert() { _showAlert.value = null }

    fun clearCart() {
        viewModelScope.launch {
            if (sessionManager.getGeneralSetting()?.is_inventory == true) {
                val orderIdStr = newOrderId.value ?: ""
                try {
                    apiService.deleteByOrderId(orderIdStr, sessionManager.getCompanyCode() ?: "")
                } catch (e: Exception) {
                    Timber.e(e, "Error clearing temporary stock")
                }
            }
            _cartItems.value = emptyMap()
            _refNo.value = ""
            _selectedPlatform.value = null
            _selectedCategory.value = "ALL"
        }
    }

    fun placeAndBillOrder() {
        val platform = _selectedPlatform.value
        val items = _cartItems.value
        if (platform == null || items.isEmpty()) {
            _uiState.value = OnlineOrderUiState.Error("Please select a platform and add items to cart")
            return
        }

        _uiState.value = OnlineOrderUiState.Loading
        viewModelScope.launch {
            try {
                val tenantId = sessionManager.getCompanyCode() ?: ""
                
                val orderItems = items.map { (menuItem, quantity) ->
                    OrderItem(quantity = quantity, menuItem = menuItem)
                }

                orderRepository.placeOrUpdateOrder(
                    tableId = 1,
                    itemsToPlace = orderItems,
                    tableStatus = "ONLINE",
                    deliveryBoyId = 5,
                    isOnline = true,
                    onlineRefNo = _refNo.value,
                    onlineOrderId = platform.online_order_id.toInt(),
                ).collect { orderResult ->
                    orderResult.fold(
                        onSuccess = { orderResponse ->
                            val total = orderItems.sumOf{ it.menuItem.parcel_rate * it.quantity }
                            val totalAmount = items.entries.sumOf { it.key.rate * it.value }
                            billRepository.bill(
                                orderMasterId = orderResponse.order_master_id,
                                paymentMethod = PaymentMethod("online", "ONLINE"),
                                receivedAmt = total,
                                customer = TblCustomer(1L, "ONLINE CUSTOMER", "", "", "", "", false, 1L),
                                billNo = "--",
                                voucherType = "BILL",
                                total = total
                            ).collect { billResult ->
                                billResult.fold(
                                    onSuccess = { billingResponse ->
                                        _uiState.value = OnlineOrderUiState.Success(billingResponse.bill_no)
                                        clearCart()
                                        // Refresh new order ID for next order
                                        val next = apiService.getOrderNo(tenantId, sessionManager.getUser()?.counter_id ?: 0, "ORDER")
                                        newOrderId.value = next["order_master_id"]
                                    },
                                    onFailure = { throw it }
                                )
                            }
                        },
                        onFailure = { throw it }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = OnlineOrderUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
