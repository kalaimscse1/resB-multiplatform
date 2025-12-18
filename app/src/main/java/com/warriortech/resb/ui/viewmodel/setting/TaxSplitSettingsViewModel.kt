package com.warriortech.resb.ui.viewmodel.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.TaxSplitRepository
import com.warriortech.resb.model.Tax
import com.warriortech.resb.model.TaxSplit
import com.warriortech.resb.model.TblTaxSplit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaxSplitSettingsViewModel @Inject constructor(
    private val taxSplitRepository: TaxSplitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    private val _tax = MutableStateFlow<List<Tax>>(emptyList())
    val taxes: StateFlow<List<Tax>> = _tax

    sealed class UiState {
        object Loading : UiState()
        data class Success(val taxSplits: List<TblTaxSplit>) : UiState()
        data class Error(val message: String) : UiState()
    }

    fun loadTaxSplits() {
        viewModelScope.launch {

            try {
                _tax.value = taxSplitRepository.getTaxes()
                _uiState.value = UiState.Loading
                val taxSplits = taxSplitRepository.getAllTaxSplits()
                _uiState.value = UiState.Success(taxSplits.filter { it.tax_split_name != "--" })
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addTaxSplit(taxSplit: TaxSplit) {
        viewModelScope.launch {
            try {
                val newTaxSplit = taxSplitRepository.createTaxSplit(taxSplit)
                if (newTaxSplit != null) {
                    loadTaxSplits()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to add tax split")
            }
        }
    }

    fun updateTaxSplit(taxSplit: TaxSplit) {
        viewModelScope.launch {
            try {
                val updatedTaxSplit = taxSplitRepository.updateTaxSplit(taxSplit)
                if (updatedTaxSplit != null) {
                    loadTaxSplits()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to update tax split")
            }
        }
    }

    fun deleteTaxSplit(id: Long) {
        viewModelScope.launch {
            try {
                val success = taxSplitRepository.deleteTaxSplit(id)
                if (success) {
                    loadTaxSplits()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to delete tax split")
            }
        }
    }
}