package com.warriortech.resb.ui.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.MenuItemRepository
import com.warriortech.resb.data.repository.ModifierRepository
import com.warriortech.resb.data.repository.OrderRepository
import com.warriortech.resb.data.repository.TableRepository
import com.warriortech.resb.model.*
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.getCurrentTimeAsFloat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class CartItemKey(
    val menuItem: TblMenuItemResponse,
    val modifiers: List<Modifiers> = emptyList()
)

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val menuRepository: MenuItemRepository,
    private val orderRepository: OrderRepository,
    private val tableRepository: TableRepository,
    private val modifierRepository: ModifierRepository,
    private val sessionManager: SessionManager,
    private val apiService: ApiService
) : ViewModel() {

    sealed class MenuUiState {
        object Loading : MenuUiState()
        data class Success(val menuItems: List<TblMenuItemResponse>) : MenuUiState()
        data class Error(val message: String) : MenuUiState()
    }

    sealed class OrderUiState {
        object Idle : OrderUiState()
        object Loading : OrderUiState()
        data class Success(val order: Order) : OrderUiState()
        data class Error(val message: String) : OrderUiState()
    }

    private val _menuState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val menuState: StateFlow<MenuUiState> = _menuState.asStateFlow()

    private val _orderState = MutableStateFlow<OrderUiState>(OrderUiState.Idle)
    val orderState: StateFlow<OrderUiState> = _orderState.asStateFlow()

    private val _selectedTableId = MutableStateFlow<Long?>(null)

    private val _selectedCartItems = MutableStateFlow<Map<CartItemKey, Int>>(emptyMap())
    private val _newSelectedCartItems = MutableStateFlow<Map<CartItemKey, Int>>(emptyMap())

    val cartItems: StateFlow<Map<CartItemKey, Int>> = _selectedCartItems.asStateFlow()
    val newCartItems: StateFlow<Map<CartItemKey, Int>> = _newSelectedCartItems.asStateFlow()

    // Aggregated quantities for display on MenuItemCards
    val selectedItems: StateFlow<Map<TblMenuItemResponse, Int>> = _selectedCartItems.map { map ->
        map.entries.groupBy { it.key.menuItem }
            .mapValues { entry -> entry.value.sumOf { it.value } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val newselectedItems: StateFlow<Map<TblMenuItemResponse, Int>> =
        _newSelectedCartItems.map { map ->
            map.entries.groupBy { it.key.menuItem }
                .mapValues { entry -> entry.value.sumOf { it.value } }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    // Granular quantities for UI (With and Without Modifiers)
    val selectedWithoutModifiers: StateFlow<Map<Long, Int>> = _selectedCartItems.map { map ->
        map.filter { it.key.modifiers.isEmpty() }.entries.associate { it.key.menuItem.menu_item_id to it.value }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val selectedWithModifiers: StateFlow<Map<Long, Int>> = _selectedCartItems.map { map ->
        map.filter { it.key.modifiers.isNotEmpty() }.entries.groupBy { it.key.menuItem.menu_item_id }
            .mapValues { it.value.sumOf { entry -> entry.value } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val newSelectedWithoutModifiers: StateFlow<Map<Long, Int>> = _newSelectedCartItems.map { map ->
        map.filter { it.key.modifiers.isEmpty() }.entries.associate { it.key.menuItem.menu_item_id to it.value }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val newSelectedWithModifiers: StateFlow<Map<Long, Int>> = _newSelectedCartItems.map { map ->
        map.filter { it.key.modifiers.isNotEmpty() }.entries.groupBy { it.key.menuItem.menu_item_id }
            .mapValues { it.value.sumOf { entry -> entry.value } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val selectedModifiers: StateFlow<Map<Long, List<Modifiers>>> =
        combine(_selectedCartItems, _newSelectedCartItems) { selected, new ->
            (selected + new).keys.associate { it.menuItem.menu_item_id to it.modifiers }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    // Tracks the last interacted variation for the keypad to affect
    private val _activeCartItem = MutableStateFlow<CartItemKey?>(null)
    val activeCartItem: StateFlow<CartItemKey?> = _activeCartItem.asStateFlow()

    val categories = MutableStateFlow<List<String>>(emptyList())
    val selectedCategory = MutableStateFlow<String?>(null)
    val tableStatus = MutableStateFlow<String?>(null)
    val existingOrderId = MutableStateFlow<String?>(null)
    val newOrderId = MutableStateFlow<String?>(null)
    private val _isExistingOrderLoaded = MutableStateFlow(false)
    val isExistingOrderLoaded: StateFlow<Boolean> = _isExistingOrderLoaded.asStateFlow()

    val orderDetailsResponse = MutableStateFlow<List<TblOrderDetailsResponse>>(emptyList())

    private val _selectedMenuItemForModifier = MutableStateFlow<TblMenuItemResponse?>(null)
    val selectedMenuItemForModifier: StateFlow<TblMenuItemResponse?> =
        _selectedMenuItemForModifier.asStateFlow()

    private val _showModifierDialog = MutableStateFlow<Boolean>(false)
    val showModifierDialog: StateFlow<Boolean> = _showModifierDialog.asStateFlow()

    private val _modifierGroups = MutableStateFlow<List<Modifiers>>(emptyList())
    val modifierGroups: StateFlow<List<Modifiers>> = _modifierGroups.asStateFlow()

    init {
        CurrencySettings.update(
            symbol = sessionManager.getRestaurantProfile()?.currency ?: "",
            decimals = sessionManager.getRestaurantProfile()?.decimal_point?.toInt() ?: 2
        )
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

    private fun getTotalItemQtyInCart(menuItem: TblMenuItemResponse): Int {
        val cart = if (_isExistingOrderLoaded.value) _newSelectedCartItems.value else _selectedCartItems.value
        return cart.entries
            .filter { it.key.menuItem.menu_item_id == menuItem.menu_item_id }
            .sumOf { it.value }
    }

    fun initializeScreen(isTableOrder: Boolean, currentTableId: Long) {
        viewModelScope.launch {
            loadMenuItems()
            if (isTableOrder) {
                val existingItemsForTable =
                    orderRepository.getOpenOrderItemsForTable(currentTableId)
                if (existingItemsForTable.isNotEmpty()) {
                    orderDetailsResponse.value = existingItemsForTable
                    val items = existingItemsForTable.associate {
                        val menuItem = TblMenuItemResponse(
                            menu_item_id = it.menuItem.menu_item_id,
                            menu_item_name = it.menuItem.menu_item_name,
                            menu_item_name_tamil = it.menuItem.menu_item_name_tamil,
                            item_cat_id = it.menuItem.item_cat_id,
                            item_cat_name = it.menuItem.item_cat_name,
                            rate = it.rate,
                            ac_rate = it.rate,
                            parcel_rate = it.rate,
                            parcel_charge = it.rate,
                            tax_id = it.menuItem.tax_id,
                            tax_name = it.menuItem.tax_name,
                            tax_percentage = it.menuItem.tax_percentage,
                            kitchen_cat_id = it.menuItem.kitchen_cat_id,
                            kitchen_cat_name = it.menuItem.kitchen_cat_name,
                            stock_maintain = it.menuItem.stock_maintain,
                            rate_lock = it.menuItem.rate_lock,
                            unit_id = it.menuItem.unit_id,
                            unit_name = it.menuItem.unit_name,
                            min_stock = it.menuItem.min_stock,
                            hsn_code = it.menuItem.hsn_code,
                            order_by = it.menuItem.order_by,
                            is_inventory = it.menuItem.is_inventory,
                            is_raw = it.menuItem.is_raw,
                            is_available = it.menuItem.is_available,
                            image = it.menuItem.image,
                            qty = it.qty,
                            cess_specific = it.cess_specific,
                            cess_per = it.cess_per.toString(),
                            is_favourite = it.menuItem.is_favourite,
                            menu_item_code = it.menuItem.menu_item_code,
                            menu_id = it.menuItem.menu_id,
                            menu_name = it.menuItem.menu_name,
                            is_active = it.menuItem.is_active,
                            preparation_time = it.menuItem.preparation_time,
                            actual_rate = it.actual_rate
                        )
                        CartItemKey(menuItem) to it.qty
                    }
                    _selectedCartItems.value = items
                    _isExistingOrderLoaded.value = true
                    existingOrderId.value = existingItemsForTable.firstOrNull()?.order_master_id
                } else {
                    _selectedCartItems.value = emptyMap()
                }
            } else {
                _selectedCartItems.value = emptyMap()
            }
        }
    }

    fun loadMenuItems(category: String? = null) {
        viewModelScope.launch {
            try {
                _menuState.value = MenuUiState.Loading
                val menus = menuRepository.getMenus().associateBy { it.menu_id }
                tableStatus.value = _selectedTableId.value?.let { tableRepository.getstatus(it) }
                menuRepository.getMenuItems(category).collect { result ->
                    result.fold(
                        onSuccess = { menuItems ->
                            val showMenu =
                                sessionManager.getGeneralSetting()?.menu_show_in_time ?: false
                            val itemsToShow = if (showMenu) {
                                val currentTime = getCurrentTimeAsFloat()
                                menuItems.filter { menuItem ->
                                    val menu = menus[menuItem.menu_id]
                                    val startTime = menu?.start_time ?: 0.00f
                                    val endTime = menu?.end_time ?: 24.00f
                                    if (startTime <= endTime) currentTime in startTime..endTime
                                    else (currentTime >= startTime) || (currentTime <= endTime)
                                }
                            } else menuItems

                            _menuState.value = MenuUiState.Success(itemsToShow)
                            val data = buildList {
                                add("FAVOURITES")
                                add("ALL")
                                addAll(itemsToShow.map { it.item_cat_name }.distinct())
                            }
                            categories.value = data
                            selectedCategory.value = categories.value.firstOrNull()
                        },
                        onFailure = { error ->
                            _menuState.value =
                                MenuUiState.Error(error.message ?: "Failed to load items")
                        }
                    )
                }
            } catch (e: Exception) {
                _menuState.value = MenuUiState.Error(e.message ?: "Failed to load items")
            }
        }
    }

    fun setTableId(tableId: Long?) {
        _selectedTableId.value = tableId
    }

    private val _showAlert = MutableStateFlow<String?>(null)
    val showAlert: StateFlow<String?> = _showAlert.asStateFlow()
    fun dismissAlert() {
        _showAlert.value = null
    }

    private suspend fun performStockCheck(
        menuItem: TblMenuItemResponse,
        targetTotalQty: Int
    ): Boolean {
        if (sessionManager.getGeneralSetting()?.is_inventory != true) return true
        
        try {
            val tenantId = sessionManager.getCompanyCode() ?: ""
            val response = menuRepository.getItemMasterById(menuItem.menu_item_id)

            if (response.isSuccessful) {
                val itemMaster = response.body()
                if (itemMaster != null) {
                    val totalStock = itemMaster.qty
                    
                    val tmpStockResponse = apiService.sumQty(menuItem.menu_item_id, tenantId)
                    val totalTempHoldsByAll = tmpStockResponse["tmp_qty"] ?: 0.0
                    
                    val totalInCartBefore = getTotalItemQtyInCart(menuItem)
                    
                    // Available Stock = Master - (HoldsByOthers)
                    // HoldsByOthers = totalTempHoldsByAll - totalInCartBefore
                    val availableStock = totalStock - (totalTempHoldsByAll - totalInCartBefore.toDouble())

                    if (availableStock <= 0.00) {
                        _showAlert.value = "Stock Alert: ${menuItem.menu_item_name} is Out of Stock."
                        return false
                    }

                    if (availableStock < targetTotalQty.toDouble()) {
                        _showAlert.value = "Stock Alert: Only $availableStock remaining for ${menuItem.menu_item_name}."
                        return false
                    }

                    if (menuItem.stock_maintain.equals("YES", ignoreCase = true)) {
                        if (availableStock <= menuItem.min_stock.toDouble()) {
                            _showAlert.value = "Stock Alert: ${menuItem.menu_item_name} is at Minimum Stock Level ($availableStock remaining)"
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking stock for item ${menuItem.menu_item_id}")
        }
        return true
    }

    private suspend fun syncTmpStock(menuItem: TblMenuItemResponse, newTotalQty: Int) {
        if (sessionManager.getGeneralSetting()?.is_inventory != true) return
        val orderIdStr = existingOrderId.value ?: newOrderId.value ?: ""
        val tenantId = sessionManager.getCompanyCode() ?: ""

        try {
            if (newTotalQty <= 0) {
                apiService.deleteByOrderIdAndItemId(orderIdStr, menuItem.menu_item_id, tenantId)
            } else {
                val response = apiService.checkExists(orderIdStr, menuItem.menu_item_id, tenantId)
                if (response.data == true) {
                    apiService.updateQty(orderIdStr, menuItem.menu_item_id, newTotalQty.toDouble(), tenantId)
                } else {
                    apiService.createTmpItemMaster(
                        TblTmpItemMasterRequest(
                            order_id = orderIdStr,
                            item_id = menuItem.menu_item_id,
                            tmp_qty = newTotalQty.toDouble(),
                            is_active = 1L
                        ),
                        tenantId
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing temporary stock")
        }
    }

    fun addItemToOrder(menuItem: TblMenuItemResponse) {
        viewModelScope.launch {
            val totalInCart = getTotalItemQtyInCart(menuItem)
            val newTotal = totalInCart + 1
            
            if (performStockCheck(menuItem, newTotal)) {
                syncTmpStock(menuItem, newTotal)
                doAddItem(menuItem)
            }
        }
    }

    private fun doAddItem(menuItem: TblMenuItemResponse, modifiers: List<Modifiers> = emptyList()) {
        val key = CartItemKey(menuItem, modifiers)
        if (_isExistingOrderLoaded.value) {
            val current = _newSelectedCartItems.value.toMutableMap()
            current[key] = (current[key] ?: 0) + 1
            _newSelectedCartItems.value = current
        } else {
            val current = _selectedCartItems.value.toMutableMap()
            current[key] = (current[key] ?: 0) + 1
            _selectedCartItems.value = current
        }
        _activeCartItem.value = key
    }

    fun setActiveItem(key: CartItemKey) {
        _activeCartItem.value = key
    }

    fun setActiveItem(menuItem: TblMenuItemResponse) {
        _activeCartItem.value = CartItemKey(menuItem)
    }

    fun setActiveItemWithModifiers(menuItem: TblMenuItemResponse) {
        val cart =
            if (_isExistingOrderLoaded.value) _newSelectedCartItems.value else _selectedCartItems.value
        val keyWithModifiers =
            cart.keys.find { it.menuItem.menu_item_id == menuItem.menu_item_id && it.modifiers.isNotEmpty() }
        if (keyWithModifiers != null) {
            _activeCartItem.value = keyWithModifiers
        }
    }

    fun updateItemQuantity(cartItemKey: CartItemKey, newQtyForVariation: Int) {
        viewModelScope.launch {
            val totalInCartBefore = getTotalItemQtyInCart(cartItemKey.menuItem)
            val currentQtyForVariation = (if (_isExistingOrderLoaded.value) _newSelectedCartItems.value else _selectedCartItems.value)[cartItemKey] ?: 0
            val newTotalForItem = (totalInCartBefore - currentQtyForVariation) + newQtyForVariation
            
            if (newQtyForVariation > currentQtyForVariation) {
                if (!performStockCheck(cartItemKey.menuItem, newTotalForItem)) return@launch
            }

            syncTmpStock(cartItemKey.menuItem, newTotalForItem)

            if (_isExistingOrderLoaded.value) {
                val current = _newSelectedCartItems.value.toMutableMap()
                if (newQtyForVariation <= 0) current.remove(cartItemKey) else current[cartItemKey] = newQtyForVariation
                _newSelectedCartItems.value = current
            } else {
                val current = _selectedCartItems.value.toMutableMap()
                if (newQtyForVariation <= 0) current.remove(cartItemKey) else current[cartItemKey] = newQtyForVariation
                _selectedCartItems.value = current
            }
            if (newQtyForVariation <= 0 && _activeCartItem.value == cartItemKey) {
                _activeCartItem.value = null
            }
        }
    }

    fun updateItemQuantity(menuItem: TblMenuItemResponse, newQty: Int) {
        updateItemQuantity(CartItemKey(menuItem), newQty)
    }

    fun removeItemFromOrder(cartItemKey: CartItemKey) {
        viewModelScope.launch {
            val totalInCartBefore = getTotalItemQtyInCart(cartItemKey.menuItem)
            val currentMap = if (_isExistingOrderLoaded.value) _newSelectedCartItems.value else _selectedCartItems.value
            val currentQtyForVariation = currentMap[cartItemKey] ?: 0
            val newTotalForItem = totalInCartBefore - 1

            syncTmpStock(cartItemKey.menuItem, newTotalForItem)

            if (_isExistingOrderLoaded.value) {
                val current = _newSelectedCartItems.value.toMutableMap()
                if (currentQtyForVariation > 1) current[cartItemKey] = currentQtyForVariation - 1 else current.remove(cartItemKey)
                _newSelectedCartItems.value = current
            } else {
                val current = _selectedCartItems.value.toMutableMap()
                if (currentQtyForVariation > 1) current[cartItemKey] = currentQtyForVariation - 1 else current.remove(cartItemKey)
                _selectedCartItems.value = current
            }
            
            if ((!_newSelectedCartItems.value.containsKey(cartItemKey) && !_selectedCartItems.value.containsKey(
                    cartItemKey
                )) && _activeCartItem.value == cartItemKey
            ) {
                _activeCartItem.value = null
            }
        }
    }

    fun removeItemFromOrder(menuItem: TblMenuItemResponse) {
        removeItemFromOrder(CartItemKey(menuItem))
    }

    fun clearOrder() {
        viewModelScope.launch {
            if (sessionManager.getGeneralSetting()?.is_inventory == true) {
                val orderIdStr = existingOrderId.value ?: newOrderId.value ?: ""
                try {
                    apiService.deleteByOrderId(orderIdStr, sessionManager.getCompanyCode() ?: "")
                } catch (e: Exception) {
                    Timber.e(e, "Error clearing temporary stock")
                }
            }
            _newSelectedCartItems.value = emptyMap()
            _selectedCartItems.value = emptyMap()
            _activeCartItem.value = null
            _isExistingOrderLoaded.value = false
            _orderState.value = OrderUiState.Idle
        }
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun placeOrder(tableId: Long, tableStatus1: String?, deliveryBoyId: Long?) {
        viewModelScope.launch {
            val currentItems =
                if (_isExistingOrderLoaded.value) _newSelectedCartItems.value else _selectedCartItems.value
            if (currentItems.isEmpty()) {
                _orderState.value = OrderUiState.Error("No items selected")
                return@launch
            }

            _orderState.value = OrderUiState.Loading
            val orderItems = currentItems.map { (key, quantity) ->
                OrderItem(
                    quantity = quantity,
                    menuItem = key.menuItem,
                    notes = if (key.modifiers.isNotEmpty()) key.modifiers.joinToString(", ") { it.add_on_name } else null
                )
            }

            orderRepository.placeOrUpdateOrder(
                tableId, orderItems, tableStatus1.toString(),
                if (_isExistingOrderLoaded.value) existingOrderId.value else null,
                deliveryBoyId = deliveryBoyId
            ).collect { result ->
                result.fold(
                    onSuccess = { order ->
//                        if (sessionManager.getGeneralSetting()?.is_inventory == true) {
//                            try {
//                                apiService.deleteByOrderId(existingOrderId.value ?: newOrderId.value ?: "", sessionManager.getCompanyCode() ?: "")
//                            } catch (e: Exception) {
//                                Timber.e(e, "Error clearing temporary stock after order")
//                            }
//                        }
                        val kotItems = currentItems.map { (key, quantity) ->
                            KOTItem(
                                name = key.menuItem.menu_item_name,
                                quantity = quantity,
                                category = key.menuItem.kitchen_cat_name,
                                addOn = key.modifiers.map { it.add_on_name }
                            )
                        }
                        val kotRequest = KOTRequest(
                            tableNumber = if (tableStatus1 != "TAKEAWAY" && tableStatus1 != "DELIVERY") order.table_name else tableStatus1.toString(),
                            kotId = order.kot_number,
                            orderId = order.order_master_id,
                            waiterName = sessionManager.getUser()?.user_name,
                            orderCreatedAt = order.order_create_time,
                            items = kotItems,
                            paperWidth = sessionManager.getPaperWidth(),
                            kottype = if (tableStatus1 != "TAKEAWAY" && tableStatus1 != "DELIVERY") "DINE-IN" else tableStatus1.toString()
                        )
                        printKOT(kotRequest)
                    },
                    onFailure = { error ->
                        _orderState.value =
                            OrderUiState.Error(error.message ?: "Failed to place order")
                    }
                )
            }
        }
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    private fun printKOT(kotRequest: KOTRequest) {
        viewModelScope.launch {
            val isKOTEnabled = sessionManager.getGeneralSetting()?.is_kot ?: false
            if (isKOTEnabled) {
                val itemsByCategory = kotRequest.items.groupBy { it.category }
                for ((category, items) in itemsByCategory) {
                    val kotForCategory = kotRequest.copy(items = items)
                    val ip = orderRepository.getIpAddress(category)
                    orderRepository.printKOT(kotForCategory, ip).collect { result ->
                        result.fold(
                            onSuccess = {
                                _orderState.value = OrderUiState.Success(
                                    Order(
                                        id = 1,
                                        tableId = 0,
                                        items = emptyList(),
                                        totalAmount = 0.0,
                                        status = "PENDING",
                                        isPrinted = true
                                    )
                                )
                                _selectedCartItems.value = emptyMap()
                                _newSelectedCartItems.value = emptyMap()
                                _activeCartItem.value = null
                            },
                            onFailure = { error ->
                                _orderState.value =
                                    OrderUiState.Error(error.message ?: "Failed to print KOT")
                            }
                        )
                    }
                }
            } else {
                _orderState.value = OrderUiState.Success(
                    Order(
                        id = 1,
                        tableId = 0,
                        items = emptyList(),
                        totalAmount = 0.0,
                        status = "PENDING",
                        isPrinted = true
                    )
                )
                _selectedCartItems.value = emptyMap()
                _newSelectedCartItems.value = emptyMap()
                _activeCartItem.value = null
            }
        }
    }

    fun getOrderTotal(tableStatus: String): Double {
        return _selectedCartItems.value.entries.sumOf { (key, quantity) ->
            val baseRate = if (tableStatus == "AC") key.menuItem.ac_rate
            else if (tableStatus == "TAKEAWAY" || tableStatus == "DELIVERY") key.menuItem.parcel_rate
            else key.menuItem.rate
            val modifiersRate = key.modifiers.sumOf { it.add_on_price }
            (baseRate + modifiersRate) * quantity
        }
    }

    fun getOrderNewTotal(tableStatus: String): Double {
        return _newSelectedCartItems.value.entries.sumOf { (key, quantity) ->
            val baseRate = if (tableStatus == "AC") key.menuItem.ac_rate
            else if (tableStatus == "TAKEAWAY" || tableStatus == "DELIVERY") key.menuItem.parcel_rate
            else key.menuItem.rate
            val modifiersRate = key.modifiers.sumOf { it.add_on_price }
            (baseRate + modifiersRate) * quantity
        }
    }

    fun showModifierDialog(menuItem: TblMenuItemResponse) {
        viewModelScope.launch {
            modifierRepository.getModifierGroupsForMenuItem(menuItem.item_cat_id)
                .collect { result ->
                    result.fold(
                        onSuccess = { modifiers ->
                            if (modifiers.isEmpty()) _showAlert.value =
                                "No modifiers for ${menuItem.menu_item_name}"
                            else {
                                _selectedMenuItemForModifier.value = menuItem
                                _showModifierDialog.value = true
                                _modifierGroups.value = modifiers
                            }
                        },
                        onFailure = { _showAlert.value = "Error loading modifiers" }
                    )
                }
        }
    }

    fun hideModifierDialog() {
        _showModifierDialog.value = false
        _selectedMenuItemForModifier.value = null
    }

    fun addMenuItemWithModifiers(menuItem: TblMenuItemResponse, modifiers: List<Modifiers>) {
        viewModelScope.launch {
            val totalInCartBefore = getTotalItemQtyInCart(menuItem)
            val cartItemKey = CartItemKey(menuItem, modifiers)
            val currentQtyForVariation = (if (_isExistingOrderLoaded.value) _newSelectedCartItems.value else _selectedCartItems.value)[cartItemKey] ?: 0
            val newTotalForItem = totalInCartBefore + 1
            
            if (performStockCheck(menuItem, newTotalForItem)) {
                syncTmpStock(menuItem, newTotalForItem)
                doAddItem(menuItem, modifiers)
                hideModifierDialog()
            }
        }
    }

    fun findAndAddItemByBarcode(barcode: String) {
        val state = _menuState.value
        if (state is MenuUiState.Success) {
            val found = state.menuItems.find { it.menu_item_code == barcode }
            if (found != null) addItemToOrder(found)
            else _orderState.value = OrderUiState.Error("Item not found: $barcode")
        }
    }

    fun updateTableOpenStatus(tableId: Long, status: Boolean) {
        viewModelScope.launch {
            try {
                tableRepository.updateTableOpenStatus(tableId, status)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update table open status")
            }
        }
    }
}
