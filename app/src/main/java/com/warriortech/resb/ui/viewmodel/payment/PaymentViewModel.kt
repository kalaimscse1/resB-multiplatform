package com.warriortech.resb.ui.viewmodel.payment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.BillRepository
import com.warriortech.resb.model.TblBillingResponse
import com.warriortech.resb.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

sealed class PaymentUiState {
    object Loading : PaymentUiState()
    data class Success(val bill: TblBillingResponse) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
    object PaymentSuccess : PaymentUiState()
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val billRepository: BillRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Loading)
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _selectedBill = MutableStateFlow<TblBillingResponse?>(null)
    val selectedBill: StateFlow<TblBillingResponse?> = _selectedBill.asStateFlow()

    fun loadBillDetails(billNo: String) {
        viewModelScope.launch {
            try {
                _uiState.value = PaymentUiState.Loading

                // This is a placeholder - you'll need to implement getBillByNumber in your repository
                // For now, we'll simulate loading the bill from the unpaid bills list
                val tenantId = sessionManager.getCompanyCode() ?: ""
                val currentDate = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val fromDate = LocalDate.now().minusDays(365)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                billRepository.getUnpaidBills(tenantId, fromDate, currentDate).collect { result ->
                    result.onSuccess { bills ->
                        val bill = bills.find { it.bill_no == billNo }
                        if (bill != null) {
                            _selectedBill.value = bill
                            _uiState.value = PaymentUiState.Success(bill)
                        } else {
                            _uiState.value = PaymentUiState.Error("Bill not found")
                        }
                    }.onFailure { error ->
                        _uiState.value =
                            PaymentUiState.Error(error.message ?: "Error loading bill details")
                        Log.e("PaymentViewModel", "Error loading bill details", error)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PaymentUiState.Error(e.message ?: "Unknown error")
                Log.e("PaymentViewModel", "Exception loading bill details", e)
            }
        }
    }

    fun processPayment(bill: TblBillingResponse, paymentMethod: PaymentMethod, amount: Double) {
        viewModelScope.launch {
            try {
                // Here you would implement the payment processing logic
                // This might involve calling an API endpoint to update the bill payment status

                Log.d(
                    "PaymentViewModel",
                    "Processing payment: Bill=${bill.bill_no}, Method=${paymentMethod.name}, Amount=$amount"
                )

                // Simulate payment processing
                delay(2000)

                // For now, we'll just show success
                _uiState.value = PaymentUiState.PaymentSuccess

            } catch (e: Exception) {
                _uiState.value = PaymentUiState.Error(e.message ?: "Payment processing failed")
                Log.e("PaymentViewModel", "Error processing payment", e)
            }
        }
    }
}
