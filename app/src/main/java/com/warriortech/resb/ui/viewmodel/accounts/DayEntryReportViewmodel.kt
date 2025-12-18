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
class DayEntryReportViewmodel @Inject constructor(
    private val ledgerDetailsRepository: LedgerDetailsRepository,
    private val ledgerRepository: LedgerRepository,
) : ViewModel() {
    sealed class DayEntryUiState {
        object Loading : DayEntryUiState()
        data class Success(val ledgers: List<TblLedgerDetailsIdResponse>) : DayEntryUiState()
        data class Error(val message: String) : DayEntryUiState()
    }

    private val _ledgerDetailsState = MutableStateFlow<DayEntryUiState>(DayEntryUiState.Loading)
    val ledgerDetailsState : StateFlow<DayEntryUiState> = _ledgerDetailsState.asStateFlow()

    private val _ledgerList = MutableStateFlow<List<TblLedgerDetails>>(emptyList())
    val ledgerList = _ledgerList.asStateFlow()
    private val _openingBalance = MutableStateFlow<Map<String, Double>>(emptyMap())
    val openingBalance = _openingBalance.asStateFlow()



    fun loadData(ledgerId: Long,fromDate: String, toDate: String){
        viewModelScope.launch {
            try{
                val ledgers = ledgerRepository.getLedgers().orEmpty()
                val ledger = ledgerDetailsRepository.getLedgerDetailsById(ledgerId,fromDate,toDate)
                _ledgerList.value = ledgers
                _ledgerDetailsState.value = DayEntryUiState.Success(ledgers = ledger)
            }catch (e: Exception) {
                _ledgerDetailsState.value = DayEntryUiState.Error(e.message ?: "Unknown error")
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