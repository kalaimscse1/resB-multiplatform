package com.warriortech.resb.ui.viewmodel.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.LedgerDetailsRepository
import com.warriortech.resb.data.repository.LedgerRepository
import com.warriortech.resb.model.TblLedgerDetails
import com.warriortech.resb.model.TblLedgerDetailsIdResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DayBookReportViewmodel @Inject constructor(
    private val ledgerDetailsRepository: LedgerDetailsRepository,
    private val ledgerRepository: LedgerRepository,
) : ViewModel() {
    sealed class DayBookUiState {
        object Loading : DayBookUiState()
        data class Success(val ledgers: List<TblLedgerDetailsIdResponse>) : DayBookUiState()
        data class Error(val message: String) : DayBookUiState()
    }

    private val _ledgerDetailsState = MutableStateFlow<DayBookUiState>(DayBookUiState.Loading)
    val ledgerDetailsState: StateFlow<DayBookUiState> = _ledgerDetailsState.asStateFlow()

    private val _ledgerList = MutableStateFlow<List<TblLedgerDetails>>(emptyList())
    val ledgerList = _ledgerList.asStateFlow()
    private val _openingBalance = MutableStateFlow<Map<String, Double>>(emptyMap())
    val openingBalance = _openingBalance.asStateFlow()


    fun loadData(fromDate: String, toDate: String) {
        viewModelScope.launch {
            try {
                val ledgers = ledgerRepository.getLedgers().orEmpty()
                val ledger = ledgerDetailsRepository.getDayBook(fromDate, toDate)!!
                _ledgerList.value = ledgers
                _ledgerDetailsState.value = DayBookUiState.Success(ledgers = ledger)
            } catch (e: Exception) {
                _ledgerDetailsState.value = DayBookUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun getOpeningBalance(fromDate: String, ledgerId: Long) {
        viewModelScope.launch {
            try {
                val data = ledgerDetailsRepository.getOpeningBalance(fromDate, ledgerId)
                _openingBalance.value = data
            } catch (e: Exception) {
                _openingBalance.value = emptyMap()
            }
        }
    }
}
