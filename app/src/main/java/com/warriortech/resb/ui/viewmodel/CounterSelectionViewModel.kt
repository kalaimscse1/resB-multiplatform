package com.warriortech.resb.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.CounterRepository
import com.warriortech.resb.model.Counters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CounterSelectionViewModel @Inject constructor(
    private val counterRepository: CounterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CounterUiState>(CounterUiState.Loading)
    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

    private val _selectedCounter = MutableStateFlow<Counters?>(null)
    val selectedCounter: StateFlow<Counters?> = _selectedCounter.asStateFlow()

    sealed class CounterUiState {
        object Loading : CounterUiState()
        data class Success(val counters: List<Counters>) : CounterUiState()
        data class Error(val message: String) : CounterUiState()
    }

    fun loadCounters() {
        viewModelScope.launch {
            _uiState.value = CounterUiState.Loading
            try {
                counterRepository.getActiveCounters().collect { result ->
                    result.fold(
                        onSuccess = { counters ->
                            _uiState.value = CounterUiState.Success(counters)
                        },
                        onFailure = { exception ->
                            _uiState.value = CounterUiState.Error(
                                exception.message ?: "Unknown error occurred"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = CounterUiState.Error(
                    e.message ?: "Failed to load counters"
                )
            }
        }
    }

    fun selectCounter(counter: Counters) {
        _selectedCounter.value = counter
        viewModelScope.launch {
            try {
                // Create a new session for the selected counter
                counterRepository.createCounterSession(counter.id)
            } catch (e: Exception) {
                // Handle session creation error if needed
            }
        }
    }

    fun getCurrentCounter(): Counters? = _selectedCounter.value
}
