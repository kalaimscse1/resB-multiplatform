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

    private val _uiState = MutableStateFlow(KitchenUiState())
    val uiState: StateFlow<KitchenUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow(KOTStatus.PENDING)
    val selectedFilter: StateFlow<KOTStatus> = _selectedFilter.asStateFlow()

    init {
        loadKOTs()
    }

    fun loadKOTs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            kitchenRepository.getKitchenKOTs().collect { result ->
                result.fold(
                    onSuccess = { kots ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            kots = kots,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load KOTs"
                        )
                    }
                )
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
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to update KOT status"
                        )
                    }
                )
            }
        }
    }

    fun setFilter(status: KOTStatus) {
        _selectedFilter.value = status
    }

    fun getFilteredKOTs(): List<KitchenKOT> {
        return _uiState.value.kots.filter { it.status == _selectedFilter.value }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class KitchenUiState(
    val isLoading: Boolean = false,
    val kots: List<KitchenKOT> = emptyList(),
    val error: String? = null
)
