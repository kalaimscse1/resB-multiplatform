package com.warriortech.resb.ui.viewmodel.setting

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.MenuCategoryRepository
import com.warriortech.resb.data.repository.ModifierRepository
import com.warriortech.resb.model.MenuCategory
import com.warriortech.resb.model.Modifiers
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.screens.settings.ModifierSettingsUiState
import com.warriortech.resb.ui.viewmodel.master.MenuCategorySettingsViewModel.UiState
import com.warriortech.resb.util.CurrencySettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModifierSettingsViewModel @Inject constructor(
    private val modifierRepository: ModifierRepository,
    private val menuCategoryRepository: MenuCategoryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ModifierSettingsUiState>(ModifierSettingsUiState.Loading)
    val uiState: StateFlow<ModifierSettingsUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<MenuCategory>>(emptyList())
    val categories: StateFlow<List<MenuCategory>> = _categories.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        CurrencySettings.update(
            symbol = sessionManager.getRestaurantProfile()?.currency ?: "",
            decimals = sessionManager.getRestaurantProfile()?.decimal_point?.toInt() ?: 2
        )

    }

    fun loadModifiers() {
        viewModelScope.launch {
            try {
                _uiState.value = ModifierSettingsUiState.Loading
                val modifiers = modifierRepository.getAllModifiers()
                _uiState.value = ModifierSettingsUiState.Success(modifiers)
            } catch (e: Exception) {
                _uiState.value = ModifierSettingsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = menuCategoryRepository.getAllCategories()
                _categories.value = categories
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    @SuppressLint("SuspiciousIndentation")
    fun addModifier(modifier: Modifiers) {
        viewModelScope.launch {
            try {
              val res=  modifierRepository.createModifier(modifier)
                if (res.isSuccess)
                loadModifiers()
            } catch (e: Exception) {
                _uiState.value =
                    ModifierSettingsUiState.Error(e.message ?: "Failed to add modifier")
            }
        }
    }

    fun updateModifier(modifier: Modifiers) {
        viewModelScope.launch {
            try {
                modifierRepository.updateModifier(modifier)
                loadModifiers()
            } catch (e: Exception) {
                _uiState.value =
                    ModifierSettingsUiState.Error(e.message ?: "Failed to update modifier")
            }
        }
    }

    fun deleteModifier(modifierId: Long) {
        viewModelScope.launch {
            try {
                val response= modifierRepository.deleteModifier(modifierId)
                when(response.code()){
                    in 200..299 ->{
                        loadCategories()
                        _errorMessage.value = "AddOn deleted successfully"
                    }
                    400 -> {
                        _errorMessage.value = response.errorBody()?.string()
                    }
                    401 -> {
                        _errorMessage.value = response.errorBody()?.string()
                    }
                    409 -> {
                        _errorMessage.value = response.errorBody()?.string()
                    }
                    404 -> {
                        _errorMessage.value = response.errorBody()?.string()
                    }
                    500 -> {
                        _errorMessage.value = response.errorBody()?.string()
                    }
                    else -> {
                        _uiState.value = ModifierSettingsUiState.Error("Failed to delete addOn")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ModifierSettingsUiState.Error(e.message ?: "Failed to delete addOn")
            }
        }
    }
}