package com.warriortech.resb.ui.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.MenuItemRepository
import com.warriortech.resb.data.repository.ModifierRepository
import com.warriortech.resb.data.repository.OrderRepository
import com.warriortech.resb.data.repository.TableRepository
import com.warriortech.resb.model.KOTItem
import com.warriortech.resb.model.KOTRequest
import com.warriortech.resb.model.MenuItem
import com.warriortech.resb.model.Modifiers
import com.warriortech.resb.model.Order
import com.warriortech.resb.model.OrderItem
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.model.TblOrderDetailsResponse
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.getCurrentTimeAsFloat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val menuRepository: MenuItemRepository,
    private val orderRepository: OrderRepository,
    private val tableRepository: TableRepository,
    private val modifierRepository: ModifierRepository,
    private val sessionManager: SessionManager
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

    private val _selectedItems = MutableStateFlow<Map<TblMenuItemResponse, Int>>(emptyMap())
    var selectedItems: StateFlow<Map<TblMenuItemResponse, Int>> = _selectedItems.asStateFlow()

    val categories = MutableStateFlow<List<String>>(emptyList())

    val selectedCategory = MutableStateFlow<String?>(null)

    val tableStatus = MutableStateFlow<String?>(null)

    val existingOrderId = MutableStateFlow<String?>(null)

    private val _newselectedItems = MutableStateFlow<Map<TblMenuItemResponse, Int>>(emptyMap())
    var newselectedItems: StateFlow<Map<TblMenuItemResponse, Int>> = _newselectedItems.asStateFlow()

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

    private val _selectedModifiers = MutableStateFlow<Map<Long, List<Modifiers>>>(emptyMap())
    val selectedModifiers: StateFlow<Map<Long, List<Modifiers>>> = _selectedModifiers.asStateFlow()

    init {
        CurrencySettings.update(
            symbol = sessionManager.getRestaurantProfile()?.currency ?: "",
            decimals = sessionManager.getRestaurantProfile()?.decimal_point?.toInt() ?: 2
        )
    }

    fun initializeScreen(isTableOrder: Boolean, currentTableId: Long) {
        viewModelScope.launch {
            loadMenuItems()
            if (isTableOrder) {
                val existingItemsForTable =
                    orderRepository.getOpenOrderItemsForTable(currentTableId)
                if (existingItemsForTable.isNotEmpty()) {
                    orderDetailsResponse.value = existingItemsForTable
                    val menuItems = existingItemsForTable.map {

                        TblMenuItemResponse(
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
                    }
                    _selectedItems.value = menuItems.associateWith { it.qty }.toMutableMap()
                    _isExistingOrderLoaded.value = true
                    existingOrderId.value =
                        existingItemsForTable.firstOrNull()?.order_master_id
                } else {
                    _selectedItems.value = mutableMapOf()
                }
            } else {
                _selectedItems.value = mutableMapOf()
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
                                val filteredMenuItems = menuItems.filter { menuItem ->
                                    val menu = menus[menuItem.menu_id]
                                    val startTime = menu?.start_time ?: 0.00f
                                    val endTime = menu?.end_time ?: 24.00f
                                    if (startTime <= endTime) {
                                        currentTime in startTime..endTime
                                    } else {
                                        (currentTime >= startTime) || (currentTime <= endTime)
                                    }
                                }
                                filteredMenuItems
                            } else {
                                menuItems
                            }

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
                                MenuUiState.Error(error.message ?: "Failed to load menu items")
                        }
                    )
                }
            }catch (e:Exception){
                _menuState.value =
                    MenuUiState.Error(e.message ?: "Failed to load menu items")
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

    @SuppressLint("SuspiciousIndentation")
    fun addItemToOrder(menuItem: TblMenuItemResponse) {
        if (_isExistingOrderLoaded.value) {
            val currentItems = _newselectedItems.value.toMutableMap()
            val currentQuantity = currentItems[menuItem] ?: 0
            currentItems[menuItem] = currentQuantity + 1
            _newselectedItems.value = currentItems
        } else {
            val currentItems = _selectedItems.value.toMutableMap()
            val currentQuantity = currentItems[menuItem] ?: 0
            currentItems[menuItem] = currentQuantity + 1
            _selectedItems.value = currentItems
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun removeItemFromOrder(menuItem: TblMenuItemResponse) {

        if (_isExistingOrderLoaded.value) {
            val currentItems = _newselectedItems.value.toMutableMap()
            val currentQuantity = currentItems[menuItem] ?: 0
            if (currentQuantity > 1) {
                currentItems[menuItem] = currentQuantity - 1
            } else {
                currentItems.remove(menuItem)
            }
            _newselectedItems.value = currentItems
        } else {
            val currentItems = _selectedItems.value.toMutableMap()
            val currentQuantity = currentItems[menuItem] ?: 0
            if (currentQuantity > 1) {
                currentItems[menuItem] = currentQuantity - 1
            } else {
                currentItems.remove(menuItem)
            }
            _selectedItems.value = currentItems
        }
    }

    fun clearOrder() {
        _newselectedItems.value = mutableMapOf()
        _selectedItems.value = mutableMapOf()
        _isExistingOrderLoaded.value = false
        _orderState.value = OrderUiState.Idle
    }
    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun placeOrder(tableId: Long, tableStatus1: String?) {
        viewModelScope.launch {
            if (_selectedItems.value.isEmpty()) {
                _orderState.value = OrderUiState.Error("No items selected")
                return@launch
            }

            _orderState.value = OrderUiState.Loading

            if (_isExistingOrderLoaded.value) {
                val orderItems = _newselectedItems.value.map { (menuItem, quantity) ->
                    OrderItem(
                        quantity = quantity,
                        menuItem = menuItem,
                    )
                }
                orderRepository.placeOrUpdateOrder(
                    tableId, orderItems,
                    tableStatus1.toString(), existingOrderId.value
                ).collect { result ->
                    result.fold(
                        onSuccess = { order ->
                            val kotItem = orderItems.map { orderItem ->
                                val modifierNames =
                                    _selectedModifiers.value[orderItem.menuItem.menu_item_id]?.map { it.add_on_name }
                                        ?: emptyList()
                                KOTItem(
                                    name = orderItem.menuItem.menu_item_name,
                                    quantity = orderItem.quantity,
                                    category = orderItem.menuItem.kitchen_cat_name,
                                    addOn = modifierNames
                                )
                            }
                            val kotRequest = KOTRequest(
                                tableNumber = if (tableStatus1 != "TAKEAWAY" && tableStatus1 != "DELIVERY") order.table_name else tableStatus1.toString(),
                                kotId = order.kot_number,
                                orderId = order.order_master_id,
                                waiterName = sessionManager.getUser()?.user_name,
                                orderCreatedAt = order.order_create_time,
                                items = kotItem,
                                paperWidth = if(sessionManager.getBluetoothPrinter() !=null) 32 else 48
                            )
                            printKOT(kotRequest)
                        },
                        onFailure = { error ->
                            _orderState.value =
                                OrderUiState.Error(error.message ?: "Failed to place order")
                        }
                    )
                }
            } else {
                val orderItems = _selectedItems.value.map { (menuItem, quantity) ->
                    OrderItem(
                        quantity = quantity,
                        menuItem = menuItem,
                    )
                }
                orderRepository.placeOrUpdateOrder(
                    tableId, orderItems,
                    tableStatus1.toString()
                ).collect { result ->
                    result.fold(
                        onSuccess = { order ->
                            val kotItem = orderItems.map { orderItem ->
                                val modifierNames =
                                    _selectedModifiers.value[orderItem.menuItem.menu_item_id]?.map { it.add_on_name }
                                        ?: emptyList()
                                KOTItem(
                                    name = orderItem.menuItem.menu_item_name,
                                    quantity = orderItem.quantity,
                                    category = orderItem.menuItem.kitchen_cat_name,
                                    addOn = modifierNames
                                )
                            }
                            val kotRequest = KOTRequest(
                                tableNumber = if (tableStatus1 != "TAKEAWAY" && tableStatus.value != "DELIVERY") order.table_name.toString() else tableStatus1.toString(),
                                kotId = order.kot_number,
                                orderId = order.order_master_id,
                                waiterName = sessionManager.getUser()?.user_name,
                                orderCreatedAt = order.order_create_time,
                                items = kotItem,
                                paperWidth = if(sessionManager.getBluetoothPrinter() !=null) 32 else 48
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
    }
    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    private fun printKOT(orderId: KOTRequest) {
        viewModelScope.launch {
            val isKOTEnabled = sessionManager.getGeneralSetting()?.is_kot ?: false
            if (isKOTEnabled) {
                val category = orderId.items.groupBy { it.category }
                for ((category, items) in category) {
                    val kotForCategory = KOTRequest(
                        tableNumber = orderId.tableNumber,
                        kotId = orderId.kotId,
                        orderId = orderId.orderId,
                        waiterName = orderId.waiterName,
                        items = items,
                        orderCreatedAt = orderId.orderCreatedAt,
                        paperWidth = orderId.paperWidth
                    )
                    val ip = orderRepository.getIpAddress(category)
                    orderRepository.printKOT(kotForCategory, ip).collect { result ->
                        result.fold(
                            onSuccess = { printResponse ->
                                val order = Order(
                                    id = 1,
                                    tableId = 0,
                                    items = emptyList(),
                                    totalAmount = 0.0,
                                    status = "PENDING",
                                    isPrinted = true
                                )
                                _orderState.value = OrderUiState.Success(order)

                                _selectedItems.value = emptyMap()
                            },
                            onFailure = { error ->
                                _orderState.value = OrderUiState.Error(
                                    error.message ?: "Failed to print KOT for $category"
                                )
                            }
                        )
                    }
                }
            } else {
                val order = Order(
                    id = 1,
                    tableId = 0,
                    items = emptyList(),
                    totalAmount = 0.0,
                    status = "PENDING",
                    isPrinted = true
                )
                _orderState.value = OrderUiState.Success(order)

                _selectedItems.value = emptyMap()
            }
        }
    }

    fun getOrderTotal(tableStatus: String): Double {
        return if (_isExistingOrderLoaded.value) {
            _selectedItems.value.entries.sumOf { (menuItem, quantity) ->
                menuItem.actual_rate * quantity
            }
        } else {
            _selectedItems.value.entries.sumOf { (menuItem, quantity) ->
                if (tableStatus == "AC")
                    menuItem.ac_rate * quantity
                else if (tableStatus == "TAKEAWAY" || tableStatus == "DELIVERY")
                    menuItem.parcel_rate * quantity
                else
                    menuItem.rate * quantity
            }
        }
    }

    fun getOrderNewTotal(tableStatus: String): Double {
        return _newselectedItems.value.entries.sumOf { (menuItem, quantity) ->
            if (tableStatus == "AC")
                menuItem.ac_rate * quantity
            else if (tableStatus == "TAKEAWAY" || tableStatus == "DELIVERY")
                menuItem.parcel_rate * quantity
            else
                menuItem.rate * quantity
        }
    }

    fun showModifierDialog(menuItem: TblMenuItemResponse) {
        viewModelScope.launch {
            modifierRepository.getModifierGroupsForMenuItem(menuItem.menu_item_id.toLong()).collect { result ->
                result.fold(
                    onSuccess = { modifiers ->
                        if (modifiers.isEmpty()) {
                            _showAlert.value = "The item ${menuItem.menu_item_name} does not contain any modifiers."
                        } else {
                            _selectedMenuItemForModifier.value = menuItem
                            _showModifierDialog.value = true
                            _modifierGroups.value = modifiers
                        }
                    },
                    onFailure = { error ->
                        _showAlert.value = "Error loading modifiers: ${error.message}"
                        _modifierGroups.value = emptyList()
                    }
                )
            }
        }
    }

    fun hideModifierDialog() {
        _showModifierDialog.value = false
        _selectedMenuItemForModifier.value = null
        _modifierGroups.value = emptyList()
    }

    private fun loadModifierGroups(menuItemId: Long) {
        viewModelScope.launch {
            modifierRepository.getModifierGroupsForMenuItem(menuItemId).collect { result ->
                result.fold(
                    onSuccess = { groups ->
                        _modifierGroups.value = groups
                    },
                    onFailure = { error ->
                        Timber.e(error, "Failed to load modifier groups")
                        _modifierGroups.value = emptyList()
                    }
                )
            }
        }
    }

    fun addMenuItemWithModifiers(menuItem: TblMenuItemResponse, modifiers: List<Modifiers>) {
        val currentItems = _selectedItems.value.toMutableMap()
        val existingCount = currentItems[menuItem] ?: 0
        currentItems[menuItem] = existingCount + 1
        _selectedItems.value = currentItems
        hideModifierDialog()
    }

    fun loadModifiersForMenuItem(menuItemId: Long) {
        viewModelScope.launch {
            try {
                modifierRepository.getModifierGroupsForMenuItem(menuItemId).collect { result ->
                    result.fold(
                        onSuccess = { modifiers ->
                            _modifierGroups.value = modifiers
                        },
                        onFailure = { error ->
                            Timber.e(error, "Failed to load modifiers for menu item")
                            _modifierGroups.value = emptyList()
                        }
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading modifiers")
                _modifierGroups.value = emptyList()
            }
        }
    }

    // Function to select a modifier for a menu item
    fun selectModifier(menuItemId: Long, modifier: Modifiers) {
        val currentSelection = _selectedModifiers.value.toMutableMap()
        val selectedList = currentSelection[menuItemId]?.toMutableList() ?: mutableListOf()

        if (!selectedList.contains(modifier)) {
            selectedList.add(modifier)
        } else {
            selectedList.remove(modifier)
        }

        currentSelection[menuItemId] = selectedList
        _selectedModifiers.value = currentSelection
    }

    // Function to clear selected modifiers for a menu item
    fun clearSelectedModifiers(menuItem: MenuItem) {
        val currentSelection = _selectedModifiers.value.toMutableMap()
        currentSelection.remove(menuItem.menu_item_id)
        _selectedModifiers.value = currentSelection
    }

    fun findAndAddItemByBarcode(barcode: String) {
        viewModelScope.launch {
            try {
                val currentState = _menuState.value
                val foundItem = MutableStateFlow<TblMenuItemResponse?>(null)
                if (currentState is MenuUiState.Success) {
                    currentState.menuItems.forEach {
                      if( it.menu_item_code == barcode) {
                          foundItem.value = it
                      }
                    }
                    if (foundItem.value != null) {
                        addItemToOrder(foundItem.value!!)
                    } else {
                        _orderState.value = OrderUiState.Error("Item not found with barcode: $barcode")
                    }
                }
            } catch (e: Exception) {
                _orderState.value = OrderUiState.Error("Error scanning: ${e.message}")
            }
        }
    }

}
