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

    sealed class OnlineOrderUiState {
        object Idle : OnlineOrderUiState()
        object Loading : OnlineOrderUiState()
        data class Success(val billNo: String) : OnlineOrderUiState()
        data class Error(val message: String) : OnlineOrderUiState()
    }

    init {
        loadPlatforms()
        loadMenu()
    }

    private fun loadPlatforms() {
        viewModelScope.launch {
            try {
                val response = apiService.getAllOnlineOrders(sessionManager.getCompanyCode() ?: "")
                if (response.isSuccessful) {
                    _onlinePlatforms.value = response.body() ?: emptyList()
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

    fun addToCart(item: TblMenuItemResponse) {
        val current = _cartItems.value.toMutableMap()
        current[item] = (current[item] ?: 0) + 1
        _cartItems.value = current
    }

    fun removeFromCart(item: TblMenuItemResponse) {
        val current = _cartItems.value.toMutableMap()
        val qty = current[item] ?: 0
        if (qty > 1) current[item] = qty - 1 else current.remove(item)
        _cartItems.value = current
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
        _refNo.value = ""
        _selectedPlatform.value = null
        _selectedCategory.value = "ALL"
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
                
                // 1. Prepare Order Items
                val orderItems = items.map { (menuItem, quantity) ->
                    OrderItem(quantity = quantity, menuItem = menuItem)
                }

                // 2. Place Order
                orderRepository.placeOrUpdateOrder(
                    tableId = 1, // Generic ID for online orders
                    itemsToPlace = orderItems,
                    tableStatus = "DELIVERY", 
                    deliveryBoyId = 5 
                ).collect { orderResult ->
                    orderResult.fold(
                        onSuccess = { orderResponse ->
                            // Update order with online details
                            val orderMaster = apiService.getOrderMasterById(orderResponse.order_master_id, tenantId).body()
                            if (orderMaster != null) {
                                // We need to set is_online and other fields. 
                                // Assuming the backend supports updating these or we send them during creation.
                                // The current placeOrUpdateOrder doesn't take online details, so we might need a direct API call if supported.
                                // For now, we proceed to billing.
                            }
                            
                            // 3. Bill the order immediately using OTHERS
                            val totalAmount = items.entries.sumOf { it.key.rate * it.value }
                            billRepository.bill(
                                orderMasterId = orderResponse.order_master_id,
                                paymentMethod = PaymentMethod("others", "OTHERS"),
                                receivedAmt = totalAmount,
                                customer = TblCustomer(1L, "ONLINE CUSTOMER", "", "", "", "", false, 0L),
                                billNo = "--",
                                voucherType = "BILL",
                                total = totalAmount
                            ).collect { billResult ->
                                billResult.fold(
                                    onSuccess = { billingResponse ->
                                        _uiState.value = OnlineOrderUiState.Success(billingResponse.bill_no)
                                        clearCart()
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
