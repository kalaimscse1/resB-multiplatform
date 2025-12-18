package com.warriortech.resb.ui.viewmodel.setting

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.GeneralSettingsRepository
import com.warriortech.resb.model.GeneralSettings
import com.warriortech.resb.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeneralSettingsViewModel @Inject constructor(
    private val generalSettingsRepository: GeneralSettingsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed class UiState {
        object Loading : UiState()
        data class Success(val generalSettings: List<GeneralSettings>) : UiState()
        data class Error(val message: String) : UiState()
    }

    fun loadSettings() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val settings = generalSettingsRepository.getGeneralSettings()
                _uiState.value = UiState.Success(settings)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateSettings(settings: GeneralSettings) {
        viewModelScope.launch {
            try {
                val updatedSettings = generalSettingsRepository.updateGeneralSettings(settings)

                if (updatedSettings != null) {
                    loadSettings()
                    Log.d("GeneralSettingsViewModel", "Settings updated successfully$settings")
                    sessionManager.saveGeneralSetting(settings)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to update settings")
            }
        }
    }
}