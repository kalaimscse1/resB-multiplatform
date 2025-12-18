package com.warriortech.resb.ui.viewmodel.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.VoucherTypeRepository
import com.warriortech.resb.model.TblVoucherType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoucherTypeSettingsViewModel @Inject constructor(
    private val voucherTypeRepository: VoucherTypeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed class UiState {
        object Loading : UiState()
        data class Success(val voucherTypes: List<TblVoucherType>) : UiState()
        data class Error(val message: String) : UiState()
    }

    fun loadVoucherTypes() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val voucherTypes = voucherTypeRepository.getAllVoucherTypes()
                _uiState.value =
                    UiState.Success(voucherTypes.filter { it.voucher_type_name != "--" })
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addVoucherType(voucherType: TblVoucherType) {
        viewModelScope.launch {
            try {
                val newVoucherType = voucherTypeRepository.createVoucherType(voucherType)
                if (newVoucherType != null) {
                    loadVoucherTypes()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to add voucher type")
            }
        }
    }

    fun updateVoucherType(voucherType: TblVoucherType) {
        viewModelScope.launch {
            try {
                val updatedVoucherType = voucherTypeRepository.updateVoucherType(voucherType)
                if (updatedVoucherType != null) {
                    loadVoucherTypes()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to update voucher type")
            }
        }
    }

    fun deleteVoucherType(id: Long) {
        viewModelScope.launch {
            try {
                val success = voucherTypeRepository.deleteVoucherType(id)
                if (success) {
                    loadVoucherTypes()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to delete voucher type")
            }
        }
    }
}