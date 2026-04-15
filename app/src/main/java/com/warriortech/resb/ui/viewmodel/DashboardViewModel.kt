package com.warriortech.resb.ui.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.warriortech.resb.data.repository.BillRepository
import com.warriortech.resb.data.repository.DashboardRepository
import com.warriortech.resb.data.repository.OrderRepository
import com.warriortech.resb.model.*
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.getCurrentDateModern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val orderRepository: OrderRepository,
    private val sessionManager: SessionManager,
    private val billRepository: BillRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    val tblOrderDetailsResponse = MutableStateFlow<List<TblOrderDetailsResponse>>(emptyList())

    sealed class UiState {
        object Loading : UiState()
        data class Success(
            val metrics: DashboardMetrics,
            val piechart: List<PaymentModeData>,
            val barchart: List<WeeklySalesData>,
            val dinerWiseSales: List<DineTypeSummaryRow>
        ) : UiState()

        data class Error(val message: String) : UiState()
    }

    init {
        CurrencySettings.update(
            symbol = sessionManager.getRestaurantProfile()?.currency ?: "",
            decimals = sessionManager.getRestaurantProfile()?.decimal_point?.toInt() ?: 2
        )

    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val chartData = dashboardRepository.getChartData()
                val metrics = dashboardRepository.getDashboardMetrics()
                val piechart = chartData.paymentModeData
                val barchart = chartData.weeklySalesData
                val dinerWiseSales = dashboardRepository.getDinerWiseSales()

                _uiState.value = UiState.Success(
                    metrics = metrics,
                    piechart = piechart,
                    barchart = barchart,
                    dinerWiseSales = dinerWiseSales
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load dashboard: ${e.message}")
            }
        }
    }

    fun refreshDashboard() {
        loadDashboardData()
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

    fun dayClose(navController: NavHostController){
        viewModelScope.launch {
            dashboardRepository.addDayClose(sessionManager.getUser()?.staff_id?:1)
            navController.navigate("login")
        }
    }

    @SuppressLint("MissingPermission")
    fun printEOD(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val today = getCurrentDateModern()
            val paperWidth = sessionManager.getPaperWidth()
            billRepository.printEOD(today, today, paperWidth).collect { result ->
                result.fold(
                    onSuccess = { onResult(it) },
                    onFailure = { onResult("Error: ${it.message}") }
                )
            }
        }
    }
}
