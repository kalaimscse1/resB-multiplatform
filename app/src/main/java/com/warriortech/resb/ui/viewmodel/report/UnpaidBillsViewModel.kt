package com.warriortech.resb.ui.viewmodel.report

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.BillRepository
import com.warriortech.resb.model.TblBillingResponse
import com.warriortech.resb.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UnpaidBillsUiState {
    object Loading : UnpaidBillsUiState()
    data class Success(val bills: List<TblBillingResponse>) : UnpaidBillsUiState()
    data class Error(val message: String) : UnpaidBillsUiState()
    object Idle : UnpaidBillsUiState()
}

@HiltViewModel
class UnpaidBillsViewModel @Inject constructor(
    private val billRepository: BillRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UnpaidBillsUiState>(UnpaidBillsUiState.Idle)
    val uiState: StateFlow<UnpaidBillsUiState> = _uiState.asStateFlow()

    private val _selectedBill = MutableStateFlow<TblBillingResponse?>(null)
    val selectedBill: StateFlow<TblBillingResponse?> = _selectedBill.asStateFlow()

    fun loadUnpaidBills(fromDate: String, toDate: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UnpaidBillsUiState.Loading
                val tenantId = sessionManager.getCompanyCode() ?: ""
                val response = billRepository.getUnpaidBills(tenantId, fromDate, toDate)

                response.collect { result ->
                    result.onSuccess { bills ->
                        _uiState.value = UnpaidBillsUiState.Success(bills)
                    }.onFailure { error ->
                        _uiState.value = UnpaidBillsUiState.Error(error.message ?: "Unknown error")
                        Log.e("UnpaidBillsViewModel", "Error loading unpaid bills", error)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UnpaidBillsUiState.Error(e.message ?: "Unknown error")
                Log.e("UnpaidBillsViewModel", "Exception loading unpaid bills", e)
            }
        }
    }

    fun selectBill(bill: TblBillingResponse) {
        _selectedBill.value = bill
    }

    fun clearSelectedBill() {
        _selectedBill.value = null
    }
}
