package com.warriortech.resb.ui.viewmodel.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetUiState())
    val uiState: StateFlow<ResetUiState> = _uiState.asStateFlow()
    fun resetDatabase() {
        viewModelScope.launch {
            // You can perform any additional actions here if needed
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = apiService.resetData(sessionManager.getCompanyCode() ?: "")
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        successMessage = response.body()?.message ?: "Database reset successfully"
                    )
                } else {
                    _uiState.value =
                        _uiState.value.copy(errorMessage = "Failed to reset database: ${response.message()}")
                }
            } catch (e: Exception) {
                throw Exception("Error during database reset: ${e.message}")
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}

data class ResetUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)