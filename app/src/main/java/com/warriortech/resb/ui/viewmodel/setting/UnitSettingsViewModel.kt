package com.warriortech.resb.ui.viewmodel.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.UnitRepository
import com.warriortech.resb.model.TblUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnitSettingsViewModel @Inject constructor(
    private val unitRepository: UnitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed class UiState {
        object Loading : UiState()
        data class Success(val units: List<TblUnit>) : UiState()
        data class Error(val message: String) : UiState()
    }

    fun loadUnits() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val units = unitRepository.getAllUnits()
                _uiState.value = UiState.Success(units.filter { it.unit_name != "--" })
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addUnit(unit: TblUnit) {
        viewModelScope.launch {
            try {
                val newUnit = unitRepository.createUnit(unit)
                if (newUnit != null) {
                    loadUnits()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to add unit")
            }
        }
    }

    fun updateUnit(unit: TblUnit) {
        viewModelScope.launch {
            try {
                val updatedUnit = unitRepository.updateUnit(unit)
                if (updatedUnit != null) {
                    loadUnits()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to update unit")
            }
        }
    }

    fun deleteUnit(id: Long) {
        viewModelScope.launch {
            try {
                val success = unitRepository.deleteUnit(id)
                if (success) {
                    loadUnits()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to delete unit")
            }
        }
    }
}