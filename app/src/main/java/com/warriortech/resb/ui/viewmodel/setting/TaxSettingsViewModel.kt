package com.warriortech.resb.ui.viewmodel.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.TaxRepository
import com.warriortech.resb.model.Tax
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaxSettingsViewModel @Inject constructor(
    private val taxRepository: TaxRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed class UiState {
        object Loading : UiState()
        data class Success(val taxes: List<Tax>) : UiState()
        data class Error(val message: String) : UiState()
    }

    fun loadTaxes() {
        viewModelScope.launch {

            try {
                _uiState.value = UiState.Loading
                val taxes = taxRepository.getAllTaxes()
                _uiState.value = UiState.Success(taxes.filter { it.tax_name != "--" })
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addTax(tax: Tax) {
        viewModelScope.launch {
            try {
                val newTax = taxRepository.createTax(tax)
                if (newTax != null) {
                    loadTaxes()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to add tax")
            }
        }
    }

    fun updateTax(tax: Tax) {
        viewModelScope.launch {
            try {
                val updatedTax = taxRepository.updateTax(tax)
                if (updatedTax != null) {
                    loadTaxes()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to update tax")
            }
        }
    }

    fun deleteTax(id: Long) {
        viewModelScope.launch {
            try {
                val success = taxRepository.deleteTax(id)
                if (success) {
                    loadTaxes()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to delete tax")
            }
        }
    }
}