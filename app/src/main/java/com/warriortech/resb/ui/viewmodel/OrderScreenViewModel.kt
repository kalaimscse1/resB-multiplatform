package com.warriortech.resb.ui.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.OrderRepository
import com.warriortech.resb.model.TblOrderDetailsResponse
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.screens.reports.OrderDisplayItem
import com.warriortech.resb.util.CurrencySettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class OrderScreenViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _dineInOrders = MutableStateFlow<List<OrderDisplayItem>>(emptyList())
    val dineInOrders: StateFlow<List<OrderDisplayItem>> = _dineInOrders.asStateFlow()

    private val _takeawayOrders = MutableStateFlow<List<OrderDisplayItem>>(emptyList())
    val takeawayOrders: StateFlow<List<OrderDisplayItem>> = _takeawayOrders.asStateFlow()

    private val _deliveryOrders = MutableStateFlow<List<OrderDisplayItem>>(emptyList())
    val deliveryOrders: StateFlow<List<OrderDisplayItem>> = _deliveryOrders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val tblOrderDetailsResponse = MutableStateFlow<List<TblOrderDetailsResponse>>(emptyList())

    init {
        CurrencySettings.update(
            symbol = sessionManager.getRestaurantProfile()?.currency ?: "",
            decimals = sessionManager.getRestaurantProfile()?.decimal_point?.toInt() ?: 2
        )
    }
    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val orders = orderRepository.getAllOrders()
                val orderDisplayItems = orders.map { order ->
                    val totalAmount = orderRepository.getRunningOrderAmount(order.order_master_id)
                    OrderDisplayItem(
                        orderId = order.order_master_id,
                        areaName = order.area_name,
                        tableName = order.table_name,
                        totalAmount = totalAmount["grand_total"] ?: 0.0,
                        status = order.order_status,
                        timestamp = formatTimestamp(order.order_date),
                        orderType = when {
                            order.table_id == 1L -> "TAKEAWAY"
                            order.table_id == 0L -> "DELIVERY"
                            order.table_id > 1L -> "DINE_IN"
                            else -> "UNKNOWN"
                        }
                    )
                }

                _dineInOrders.value = orderDisplayItems.filter { it.orderType == "DINE_IN" }
                _takeawayOrders.value = orderDisplayItems.filter { it.orderType == "TAKEAWAY" }
                _deliveryOrders.value = orderDisplayItems.filter { it.orderType == "DELIVERY" }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun formatTimestamp(timestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            date?.let { outputFormat.format(it) } ?: timestamp
        } catch (e: Exception) {
            timestamp
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun getOrdersByOrderId(lng: String): List<TblOrderDetailsResponse> {
        viewModelScope.launch {
            val order = orderRepository.getOrdersByOrderId(lng)
            if (order.body() != null)
                tblOrderDetailsResponse.value = order.body()!!
            return@launch
        }
        return emptyList()
    }
}
