package com.warriortech.resb.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.KitchenRepository
import com.warriortech.resb.model.KitchenKOT
import com.warriortech.resb.model.KOTStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.filter
import kotlin.fold

@HiltViewModel
class KitchenViewModel @Inject constructor(
    private val kitchenRepository: KitchenRepository
) : ViewModel() {

    sealed class KitchenUiState {
        object Loading : KitchenUiState()
        data class Success(val kots: List<KitchenKOT>) : KitchenUiState()
        data class Error(val message: String) : KitchenUiState()
    }
    private val _uiState = MutableStateFlow<KitchenUiState>(KitchenUiState.Loading)
    val uiState: StateFlow<KitchenUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow(KOTStatus.PENDING)
    val selectedFilter: StateFlow<KOTStatus> = _selectedFilter.asStateFlow()

    init {
        loadKOTs()
    }

    fun loadKOTs() {
        viewModelScope.launch {
            _uiState.value = KitchenUiState.Loading

            kitchenRepository.getKitchenKOTs().collect { result ->
                _uiState.value = KitchenUiState.Success(result)
            }
        }
    }

    fun updateKOTStatus(kotId: Int, newStatus: KOTStatus) {
        viewModelScope.launch {
            kitchenRepository.updateKOTStatus(kotId, newStatus).collect { result ->
                result.fold(
                    onSuccess = {
                        // Refresh the KOTs list after successful update
                        loadKOTs()
                    },
                    onFailure = { error ->
                        _uiState.value = KitchenUiState.Error("Failed to update KOT status.")
                    }
                )
            }
        }
    }

    fun setFilter(status: KOTStatus) {
        _selectedFilter.value = status
    }


    fun clearError() {
        _uiState.value = KitchenUiState.Loading
    }
}