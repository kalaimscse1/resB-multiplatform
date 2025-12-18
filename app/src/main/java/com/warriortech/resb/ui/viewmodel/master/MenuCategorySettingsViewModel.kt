package com.warriortech.resb.ui.viewmodel.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.MenuCategoryRepository
import com.warriortech.resb.model.MenuCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuCategorySettingsViewModel @Inject constructor(
    private val categoryRepository: MenuCategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _orderBy = MutableStateFlow<String>("")
    val orderBy: StateFlow<String> = _orderBy.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    sealed class UiState {
        object Loading : UiState()
        data class Success(val categories: List<MenuCategory>) : UiState()
        data class Error(val message: String) : UiState()
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val categories = categoryRepository.getAllCategories()
                _uiState.value = UiState.Success(categories.filter { it.item_cat_name != "--" })
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    fun addCategory(name: String, sortOrder: String, is_active: Boolean) {
        viewModelScope.launch {
            try {
                val category = MenuCategory(
                    item_cat_id = 0,
                    item_cat_name = name,
                    order_by = sortOrder,
                    is_active = is_active
                )
                categoryRepository.insertCategory(category)
                loadCategories()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to add category")
            }
        }
    }

    fun updateCategory(id: Long, name: String, sortOrder: String, is_active: Boolean) {
        viewModelScope.launch {
            try {
                val category = MenuCategory(
                    item_cat_id = id,
                    item_cat_name = name,
                    order_by = sortOrder,
                    is_active = is_active
                )
                categoryRepository.updateCategory(category)
                loadCategories()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to update category")
            }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            try {
               val response= categoryRepository.deleteCategory(id)
                when(response.code()){
                    in 200..299 ->{
                        loadCategories()
                        _errorMessage.value = "Menu Category deleted successfully"
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
                        _uiState.value = UiState.Error("${response.code()} : ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to delete category")
            }
        }
    }

    fun getOrderBy() {
        viewModelScope.launch {
            try {
                val response = categoryRepository.getOrderBy()
                _orderBy.value = response["order_by"].toString()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to getOrderBy")
            }
        }
    }
}
