package com.warriortech.resb.ui.viewmodel.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.KitchenCategoryRepository
import com.warriortech.resb.model.KitchenCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KitchenCategorySettingsViewModel @Inject constructor(
    private val kitchenCategoryRepository: KitchenCategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed class UiState {
        object Loading : UiState()
        data class Success(val kitchenCategories: List<KitchenCategory>) : UiState()
        data class Error(val message: String) : UiState()
    }

    fun loadKitchenCategories() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val kitchenCategories = kitchenCategoryRepository.getAllKitchenCategories()
                _uiState.value =
                    UiState.Success(kitchenCategories.filter { it.kitchen_cat_name != "--" })
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addKitchenCategory(kitchenCategory: KitchenCategory) {
        viewModelScope.launch {
            try {
                val newKitchenCategory =
                    kitchenCategoryRepository.createKitchenCategory(kitchenCategory)
                if (newKitchenCategory != null) {
                    loadKitchenCategories()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to add kitchen category")
            }
        }
    }

    fun updateKitchenCategory(kitchenCategory: KitchenCategory) {
        viewModelScope.launch {
            try {
                val updatedKitchenCategory =
                    kitchenCategoryRepository.updateKitchenCategory(kitchenCategory)
                if (updatedKitchenCategory != null) {
                    loadKitchenCategories()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to update kitchen category")
            }
        }
    }

    fun deleteKitchenCategory(id: Long) {
        viewModelScope.launch {
            try {
                val success = kitchenCategoryRepository.deleteKitchenCategory(id)
                if (success) {
                    loadKitchenCategories()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to delete kitchen category")
            }
        }
    }
}