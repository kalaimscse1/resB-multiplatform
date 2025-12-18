package com.warriortech.resb.ui.viewmodel.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.CounterRepository
import com.warriortech.resb.model.TblCounter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CounterSettingsViewModel @Inject constructor(
    private val counterRepository: CounterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed class UiState {
        object Loading : UiState()
        data class Success(val counters: List<TblCounter>) : UiState()
        data class Error(val message: String) : UiState()
    }

    fun loadCounters() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val counters = counterRepository.getAllCounters()
                _uiState.value = UiState.Success(counters.filter { it.counter_name != "--" })
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addCounter(counter: TblCounter) {
        viewModelScope.launch {
            try {
                val newCounter = counterRepository.createCounter(counter)
                if (newCounter != null) {
                    loadCounters()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to add counter")
            }
        }
    }

    fun updateCounter(counter: TblCounter) {
        viewModelScope.launch {
            try {
                val updatedCounter = counterRepository.updateCounter(counter)
                if (updatedCounter != null) {
                    loadCounters()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to update counter")
            }
        }
    }

    suspend fun deleteCounter(id: Long) :String {
        val res = counterRepository.deleteCounter(id)
        loadCounters()
        return if (res.isSuccessful) {"Counter deleted successfully" } else {
           res.message()
        }
    }
}