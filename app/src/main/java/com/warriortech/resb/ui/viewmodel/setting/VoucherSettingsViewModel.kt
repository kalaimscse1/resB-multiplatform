package com.warriortech.resb.ui.viewmodel.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.CounterRepository
import com.warriortech.resb.data.repository.VoucherRepository
import com.warriortech.resb.model.TblCounter
import com.warriortech.resb.model.TblVoucherRequest
import com.warriortech.resb.model.TblVoucherResponse
import com.warriortech.resb.model.TblVoucherType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoucherSettingsViewModel @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val counterRepository: CounterRepository,

    ) : ViewModel() {

    private val _uiState = MutableStateFlow<VoucherSettingsUiState>(
        VoucherSettingsUiState.Loading
    )
    val uiState: StateFlow<VoucherSettingsUiState> = _uiState.asStateFlow()

    private val _counter = MutableStateFlow<List<TblCounter>>(emptyList())
    val counters: StateFlow<List<TblCounter>> = _counter

    private val _voucherTypes = MutableStateFlow<List<TblVoucherType>>(emptyList())
    val voucherTypes: StateFlow<List<TblVoucherType>> = _voucherTypes


    sealed class VoucherSettingsUiState {
        object Loading : VoucherSettingsUiState()
        data class Success(val vouchers: List<TblVoucherResponse>) : VoucherSettingsUiState()
        data class Error(val message: String) : VoucherSettingsUiState()
    }

    fun loadVouchers() {
        viewModelScope.launch {
            _uiState.value = VoucherSettingsUiState.Loading
            try {
                val counters = counterRepository.getAllCounters()
                val voucherTypes = voucherRepository.getAllVoucherTypes()
                val vouchers = voucherRepository.getAllVouchers()
                _counter.value = counters
                _voucherTypes.value = voucherTypes
                _uiState.value =
                    VoucherSettingsUiState.Success(vouchers.filter { it.voucher_name != "--" })
            } catch (e: Exception) {
                _uiState.value = VoucherSettingsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addVoucher(voucher: TblVoucherRequest) {
        viewModelScope.launch {
            try {
                val newVoucher = voucherRepository.createVoucher(voucher)
                if (newVoucher != null) {
                    loadVouchers()
                }
            } catch (e: Exception) {
                _uiState.value = VoucherSettingsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateVoucher(voucher: TblVoucherRequest) {
        viewModelScope.launch {
            try {
                val updatedVoucher = voucherRepository.updateVoucher(voucher)
                if (updatedVoucher != null) {
                    loadVouchers()
                }
            } catch (e: Exception) {
                _uiState.value = VoucherSettingsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteVoucher(id: Long) {
        viewModelScope.launch {
            try {
                val success = voucherRepository.deleteVoucher(id)
                if (success) {
                    loadVouchers()
                }
            } catch (e: Exception) {
                _uiState.value = VoucherSettingsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
