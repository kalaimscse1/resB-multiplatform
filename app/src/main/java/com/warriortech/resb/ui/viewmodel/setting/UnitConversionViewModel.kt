package com.warriortech.resb.ui.viewmodel.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.MenuItemRepository
import com.warriortech.resb.data.repository.UnitConversionRepository
import com.warriortech.resb.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnitConversionViewModel @Inject constructor(
    private val repository: UnitConversionRepository,
    private val menuItemRepository: MenuItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _units = MutableStateFlow<List<TblUnit>>(emptyList())
    val units: StateFlow<List<TblUnit>> = _units.asStateFlow()

    private val _menuItems = MutableStateFlow<List<TblMenuItemResponse>>(emptyList())
    val menuItems: StateFlow<List<TblMenuItemResponse>> = _menuItems.asStateFlow()

    private val _consumeItems = MutableStateFlow<List<TblMenuItemResponse>>(emptyList())
    val consumeItems: StateFlow<List<TblMenuItemResponse>> = _consumeItems.asStateFlow()


    private val _conversions = MutableStateFlow<List<TblUnitConversionResponse>>(emptyList())
    val conversions: StateFlow<List<TblUnitConversionResponse>> = _conversions.asStateFlow()

    sealed class UiState {
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                _units.value = repository.getAllUnits()
                menuItemRepository.getMenuItemByIsInventory().collect { items ->
                    _menuItems.value = items
                }
                menuItemRepository.getAllMenuItems().collect { items ->
                    _consumeItems.value = items
                }
                loadAllActiveConversions()
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load data")
            }
        }
    }

    fun loadAllActiveConversions() {
        viewModelScope.launch {
            try {
                _conversions.value = repository.findAllActive()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun loadConversions(unitId: Long) {
        viewModelScope.launch {
            try {
                _conversions.value = repository.findByUnitId(unitId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun saveConversion(request: TblUnitConversionRequest) {
        viewModelScope.launch {
            val result = if (request.unit_conv_id == 0L) {
                repository.create(request)
            } else {
                repository.update(request.unit_conv_id, request)
            }
            
            result.onSuccess {
                loadConversions(request.unit_id)
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Save failed")
            }
        }
    }

    fun deleteConversion(id: Long, unitId: Long) {
        viewModelScope.launch {
            repository.delete(id).onSuccess {
                if (unitId == 0L) loadAllActiveConversions() else loadConversions(unitId)
            }
        }
    }
}
