package com.warriortech.resb.ui.viewmodel.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.UpiTypeRepository
import com.warriortech.resb.model.TblUpiType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpiTypeViewModel @Inject constructor(
    private val repository: UpiTypeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _upiTypes = MutableStateFlow<List<TblUpiType>>(emptyList())
    val upiTypes: StateFlow<List<TblUpiType>> = _upiTypes.asStateFlow()

    sealed class UiState {
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    init {
        loadUpiTypes()
    }

    fun loadUpiTypes() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val types = repository.getAllActive()
                _upiTypes.value = types
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load UPI types")
            }
        }
    }

    fun saveUpiType(upiType: TblUpiType) {
        viewModelScope.launch {
            val result = if (upiType.upi_type_id == 0L) {
                if (repository.checkExists(upiType.upi_type_name)) {
                    _uiState.value = UiState.Error("UPI Type name already exists")
                    return@launch
                }
                repository.create(upiType).map { 1 }
            } else {
                repository.update(upiType)
            }

            result.onSuccess {
                loadUpiTypes()
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to save UPI type")
            }
        }
    }

    fun deleteUpiType(id: Long) {
        viewModelScope.launch {
            repository.delete(id).onSuccess {
                loadUpiTypes()
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to delete UPI type")
            }
        }
    }

    fun clearError() {
        _uiState.value = UiState.Success
    }
}
