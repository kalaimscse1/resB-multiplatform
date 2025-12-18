package com.warriortech.resb.ui.viewmodel.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.GroupRepository
import com.warriortech.resb.data.repository.LedgerRepository
import com.warriortech.resb.model.TblGroupDetails
import com.warriortech.resb.model.TblLedgerDetails
import com.warriortech.resb.model.TblLedgerRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val ledgerRepository: LedgerRepository,
    private val groupRepository: GroupRepository
) : ViewModel(){

    sealed class LedgerUiState {
        object Loading : LedgerUiState()
        data class Success(val ledgers: List<TblLedgerDetails>) : LedgerUiState()
        data class Error(val message: String) : LedgerUiState()
    }

    private val _legerState = MutableStateFlow<LedgerUiState>(LedgerUiState.Loading)
    val ledgerState: StateFlow<LedgerUiState> = _legerState.asStateFlow()

    private val _msg = MutableStateFlow<String>("")
    val msg: StateFlow<String> = _msg.asStateFlow()

    private val _groups = MutableStateFlow< List<TblGroupDetails>>(emptyList())
    val group : StateFlow<List<TblGroupDetails>> = _groups.asStateFlow()

    private val _orderBy = MutableStateFlow<String>("")
    val orderBy: StateFlow<String> = _orderBy.asStateFlow()
    fun loadLedgers() {
        viewModelScope.launch {
            val ledgers = ledgerRepository.getLedgers()
            _legerState.value = LedgerUiState.Success(ledgers ?: emptyList())
        }
    }

    fun addLedger(ledger: TblLedgerRequest) {
        viewModelScope.launch {
            val res = ledgerRepository.checkexists(ledger.ledger_name)
            if (res.data==true){
                val newLedger = ledgerRepository.createLedger(ledger)
                if (newLedger != null)
                    _msg.value = "Ledger added successfully"
            }
            else{
                val msg = res.message
                _msg.value = msg
            }
            val newLedger = ledgerRepository.createLedger(ledger)
            if (newLedger!=null)
            loadLedgers()
        }
    }

    fun updateLedger(ledgerId: Int, ledger: TblLedgerRequest) {
        viewModelScope.launch {
           val res =  ledgerRepository.updateLedger(ledgerId, ledger)
            if (res!=null)
                _msg.value = "Ledger updated successfully"
        }
    }

    fun getOrderBy() {
        viewModelScope.launch {
            try {
                val response = ledgerRepository.getOrderBy()
                _orderBy.value = response["order_by"].toString()
            } catch (e: Exception) {
                _legerState.value = LedgerUiState.Error(e.message ?: "Failed to getOrderBy")
            }
        }
    }
    fun deleteLedger(ledgerId: Int) {
        viewModelScope.launch {
            val res = ledgerRepository.deleteLedger(ledgerId)
            if (res!=null)
                _msg.value = "Ledger deleted successfully"
        }
    }

    fun clearMsg(){
        _msg.value = ""
    }
    fun getBankDetails() {
        viewModelScope.launch {
            val bankDetails = ledgerRepository.getBankDetails()
            // Handle bank details as needed
        }
    }

    fun getGroups(){
        viewModelScope.launch {
           val groups = groupRepository.getGroups()!!
            _groups.value = groups
        }
    }
    fun addBankDetail() {
        // Implementation to add bank detail
    }

    fun updateBankDetail() {
        // Implementation to update bank detail
    }

    fun checkExists(ledgerName: String) {
        viewModelScope.launch {
            try {
                val res = ledgerRepository.checkexists(ledgerName)
                if (res.success) {

                }
            }catch (e: Exception){

            }
        }
    }
}