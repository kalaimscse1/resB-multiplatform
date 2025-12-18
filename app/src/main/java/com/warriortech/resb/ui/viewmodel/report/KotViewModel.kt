package com.warriortech.resb.ui.viewmodel.report

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.KotRepository
import com.warriortech.resb.data.repository.OrderRepository
import com.warriortech.resb.data.repository.calculateGst
import com.warriortech.resb.data.repository.calculateGstAndCess
import com.warriortech.resb.model.ApiResponse
import com.warriortech.resb.model.KOTItem
import com.warriortech.resb.model.KOTRequest
import com.warriortech.resb.model.KotResponse
import com.warriortech.resb.model.OrderItem
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.model.TblOrderDetailsResponse
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.getCurrentDateModern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.collections.iterator

@HiltViewModel
class KotViewModel @Inject constructor(
    private val repository: KotRepository,
    private val orderRepository: OrderRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    sealed class KotUiState {
        object Loading : KotUiState()
        data class Success(val kotReports: List<KotResponse>) : KotUiState()
        data class Error(val message: String) : KotUiState()
    }

    sealed class KotActionState {
        object Idle : KotActionState()
        object Processing : KotActionState()
        data class Success(val items: Map<TblMenuItemResponse, Int>) : KotActionState()
        data class Error(val message: String) : KotActionState()
    }

    private val _kotReports = MutableStateFlow<KotUiState>(KotUiState.Loading)
    val kotReports: StateFlow<KotUiState> = _kotReports

    private val _kotActionState = MutableStateFlow<KotActionState>(KotActionState.Idle)
    val kotActionState: StateFlow<KotActionState> = _kotActionState

    val _orderDetails = MutableStateFlow<List<TblOrderDetailsResponse>>(emptyList())
    val _beforeBilledItems = MutableStateFlow<Map<TblMenuItemResponse, Int>>(emptyMap())
    val _billedItems = MutableStateFlow<Map<TblMenuItemResponse, Int>>(emptyMap())
    val _cessSpecific = MutableStateFlow<Double>(0.0)
    val _cessAmount = MutableStateFlow<Double>(0.0) // Cess percentage if applicable
    val _subtotal = MutableStateFlow<Double>(0.0)
    val _taxAmount = MutableStateFlow<Double>(0.0)
    val _totalAmount = MutableStateFlow<Double>(0.0)
    val _kot = MutableStateFlow<KotResponse?>(null)

    init {
        CurrencySettings.update(
            symbol = sessionManager.getRestaurantProfile()?.currency ?: "",
            decimals = sessionManager.getRestaurantProfile()?.decimal_point?.toInt() ?: 2
        )
        loadKotReports(getCurrentDateModern(), getCurrentDateModern())
    }

    fun loadKotReports(fromDate: String, toDate: String) {
        viewModelScope.launch {
            try {
                _kotReports.value = KotUiState.Success(repository.fetchKotReports(fromDate, toDate))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadKot(kot: KotResponse) {
        _kot.value = kot
    }

    fun loadOrderItems(orderId: String) {
        viewModelScope.launch {
            _kotActionState.value = KotActionState.Processing
            try {
                val order = orderRepository.getOrdersByOrderId(orderId)
                if (order.body() != null) {
                    val orderDetailsResponse = order.body()!!

                    // This function sets billing details from an existing order response
                    // Set billing details from TblOrderDetailsResponse
                    _orderDetails.value =
                        orderDetailsResponse.filter { it.kot_number == _kot.value?.kot_number?.toInt() }
                    val menuItems = _orderDetails.value.map {

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
                    // Convert TblOrderDetailsResponse to Map<MenuItem, Int> for existing billing logic
                    val itemsMap = menuItems.associateWith { it.qty }.toMutableMap()
                    var tableStatus = "TABLE" // Default
                    _billedItems.value = itemsMap
                    // Calculate totals from order details
                    val subtotal = _orderDetails.value.sumOf { it.total }
                    val taxAmount = _orderDetails.value.sumOf { it.tax_amount }
                    val cessAmount =
                        _orderDetails.value.sumOf { if (it.cess > 0) it.cess else 0.0 }
                    val cessSpecific =
                        _orderDetails.value.sumOf { if (it.cess_specific > 0) it.cess_specific else 0.0 }
                    val totalAmount = subtotal + taxAmount + cessAmount + cessSpecific

                    _subtotal.value = subtotal
                    _taxAmount.value = taxAmount
                    _cessAmount.value = cessAmount
                    _cessSpecific.value = cessSpecific
                    _totalAmount.value = totalAmount
                    _kotActionState.value = KotActionState.Success(itemsMap)
                } else {
                    _kotActionState.value = KotActionState.Error("No order details found.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _kotActionState.value =
                    KotActionState.Error("Failed to load order items: ${e.message}")
            }
        }
    }

    @SuppressLint("DefaultLocale")
    fun Double.roundTo2(): Double {
        val dec = sessionManager.getDecimalPlaces()
        return if (dec == 2L)
            BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP).toDouble()
        else if (dec == 3L)
            BigDecimal.valueOf(this).setScale(3, RoundingMode.HALF_UP).toDouble()
        else
            BigDecimal.valueOf(this).setScale(4, RoundingMode.HALF_UP).toDouble()
    }

    fun calc(items: Map<TblMenuItemResponse, Int>) {

        val subtotal = items.entries.sumOf { (item, qty) ->
            item.rate * qty
        }
        val tax = items.entries.sumOf { (item, qty) ->
            val cess = calculateGstAndCess(
                item.actual_rate,
                item.tax_percentage.toDouble(),
                item.cess_per.toDouble(),
                true,
                item.cess_specific.toDouble(),
                item.tax_percentage.toDouble() / 2,
                item.tax_percentage.toDouble() / 2
            )
            val tax = calculateGst(
                item.actual_rate,
                item.tax_percentage.toDouble(),
                true,
                item.tax_percentage.toDouble() / 2,
                item.tax_percentage.toDouble() / 2
            )
            if (_cessAmount.value > 0)
                cess.cessAmount.roundTo2() * qty
            else
                tax.gstAmount.roundTo2() * qty
        }
        val cess = items.entries.sumOf { (item, qty) ->
            val cess = calculateGstAndCess(
                item.actual_rate,
                item.tax_percentage.toDouble(),
                item.cess_per.toDouble(),
                true,
                item.cess_specific.toDouble(),
                item.tax_percentage.toDouble() / 2,
                item.tax_percentage.toDouble() / 2
            )
            val tax = calculateGst(
                item.actual_rate,
                item.tax_percentage.toDouble(),
                true,
                item.tax_percentage.toDouble() / 2,
                item.tax_percentage.toDouble() / 2
            )
            if (_cessAmount.value > 0)
                cess.cessAmount.roundTo2() * qty
            else
                0.0
        }
        val cessSpecific = _billedItems.value.entries.sumOf { (item, qty) ->
            if (_cessSpecific.value > 0) (item.cess_specific * qty).roundTo2() else 0.0
        }
        val totalAmount = subtotal + tax + cess + cessSpecific
        _subtotal.value = subtotal
        _taxAmount.value = tax
        _cessAmount.value = cess
        _cessSpecific.value = cessSpecific
        _totalAmount.value = totalAmount
    }

    fun updateItemQuantity(menuItem: TblMenuItemResponse, newQuantity: Int) {
        val currentItems = _billedItems.value.toMutableMap()
        if (newQuantity > 0) {
            currentItems[menuItem] = newQuantity
        } else {
            currentItems.remove(menuItem)
        }
        _beforeBilledItems.value = _billedItems.value
        _kotActionState.value = KotActionState.Success(currentItems)
        _billedItems.value = currentItems.toMap()
        calc(currentItems)
    }

    fun removeItem(menuItem: TblMenuItemResponse) {
        val orderId =
            _orderDetails.value.filter { it.menuItem.menu_item_id == menuItem.menu_item_id }
        viewModelScope.launch {
            val currentItems = _billedItems.value.toMutableMap()
            currentItems.remove(menuItem)
            _beforeBilledItems.value = _billedItems.value
            _kotActionState.value = KotActionState.Success(currentItems)
            _billedItems.value = currentItems.toMap()
            calc(currentItems)
            orderRepository.deleteByid(orderDeatailId = orderId.first().order_details_id)
        }

    }

    fun reprint(): ApiResponse<Boolean> {
        var data = false
        var msg = ""
        viewModelScope.launch {
            val orderItems = _billedItems.value.entries.map { (menuItem, quantity) ->
                OrderItem(
                    quantity = quantity,
                    menuItem = menuItem,
                )
            }
            val kotItem = orderItems.map { orderItem ->
                KOTItem(
                    name = orderItem.menuItem.menu_item_name,
                    quantity = orderItem.quantity,
                    category = orderItem.menuItem.kitchen_cat_name,
                    addOn = emptyList()
                )
            }
            val isKOTEnabled = sessionManager.getGeneralSetting()?.is_kot ?: false
            if (isKOTEnabled) {
                val category = kotItem.groupBy { it.category }

                for ((category, items) in category) {
                    val kotForCategory = KOTRequest(
                        tableNumber = if (_kot.value?.table_name == "--") "TAKEAWAY" else _kot.value?.table_name
                            ?: "",
                        kotId = _kot.value?.kot_number?.toInt() ?: 0,
                        orderId = _kot.value?.order_master_id,
                        waiterName = _kot.value?.staff_name,
                        items = items,
                        orderCreatedAt = "${_kot.value?.order_date} + ${_kot.value?.order_create_time}",
                        paperWidth = 48,
                        modify = "REPRINT"
                    )
                    val ip = orderRepository.getIpAddress(category)
                    orderRepository.printKOT(kotForCategory, ip).collect { result ->
                        result.fold(
                            onSuccess = {
                                data = true
                                msg = "KOT Printed Successfully for $category"
                            },
                            onFailure = {
                                data = false
                                msg = "Failed to print KOT for $category"
                                _kotActionState.value =
                                    KotActionState.Error("Failed to print KOT for $category")
                            }
                        )
                    }
                }
            }
        }
        return ApiResponse(data = data, message = msg, success = true)
    }

    fun modify(): ApiResponse<Boolean> {
        var data = false
        var msg = ""
        val orderItems = _billedItems.value.entries.map { (menuItem, quantity) ->
            val id =
                _orderDetails.value.filter { it.menuItem.menu_item_id == menuItem.menu_item_id }
            OrderItem(
                quantity = quantity,
                menuItem = menuItem,
                orderDetailsId = id.first().order_details_id
            )
        }
        val table = _kot.value?.table_name ?: ""
        viewModelScope.launch {
            orderRepository.updateOrderDetails(
                orderId = _kot.value?.order_master_id,
                items = orderItems,
                kotNumber = _kot.value?.kot_number?.toInt() ?: 0,
                tableStatus = if (table != "--") "TAKEAWAY" else "TABLE"
            ).collect { result ->
                result.fold(
                    onSuccess = { order ->
                        val orderItems = _billedItems.value.entries.map { (menuItem, quantity) ->
                            OrderItem(
                                quantity = quantity,
                                menuItem = menuItem,
                            )
                        }
                        val kotItem = orderItems.map { orderItem ->
                            KOTItem(
                                name = orderItem.menuItem.menu_item_name,
                                quantity = orderItem.quantity,
                                category = orderItem.menuItem.kitchen_cat_name,
                                addOn = emptyList()
                            )
                        }
                        val isKOTEnabled = sessionManager.getGeneralSetting()?.is_kot ?: false
                        if (isKOTEnabled) {
                            val category = kotItem.groupBy { it.category }

                            for ((category, items) in category) {
                                val kotForCategory = KOTRequest(
                                    tableNumber = if (table == "--") "TAKEAWAY" else _kot.value?.table_name
                                        ?: "",
                                    kotId = _kot.value?.kot_number?.toInt() ?: 0,
                                    orderId = _kot.value?.order_master_id,
                                    waiterName = _kot.value?.staff_name,
                                    items = items,
                                    orderCreatedAt = "${_kot.value?.order_date} + ${_kot.value?.order_create_time}",
                                    paperWidth = 48,
                                    modify = "MODIFIED"
                                )
                                val ip = orderRepository.getIpAddress(category)
                                orderRepository.printKOT(kotForCategory, ip).collect { result ->
                                    result.fold(
                                        onSuccess = {
                                            data = true
                                            msg =
                                                "Order Modified and KOT Printed Successfully for $category"
                                        },
                                        onFailure = {
                                            data = false
                                            msg =
                                                "Order Modified but Failed to print KOT for $category"
                                            _kotActionState.value =
                                                KotActionState.Error("Failed to print KOT for $category")
                                        }
                                    )
                                }
                            }
                        }
                        _kotActionState.value = KotActionState.Success(_billedItems.value)
                    },
                    onFailure = { error ->
                        _kotActionState.value = KotActionState.Error("Modify Failed.")
                    }
                )
            }
        }
        return ApiResponse(data = data, message = msg, success = true)
    }

}