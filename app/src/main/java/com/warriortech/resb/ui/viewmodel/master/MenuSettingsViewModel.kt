package com.warriortech.resb.ui.viewmodel.master

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.MenuRepository
import com.warriortech.resb.model.Menu
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.util.PrinterHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.forEach

@HiltViewModel
class MenuSettingsViewModel @Inject constructor(
    private val menuRepository: MenuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _orderBy = MutableStateFlow<String>("")
    val orderBy: StateFlow<String> = _orderBy.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    sealed class UiState {
        object Loading : UiState()
        data class Success(val menus: List<Menu>) : UiState()
        data class Error(val message: String) : UiState()
    }

    fun loadMenus() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val menus = menuRepository.getAllMenus()
                _uiState.value = UiState.Success(menus.filter { it.menu_name != "--" })
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun addMenu(
        name: String,
        description: String,
        isActive: Boolean,
        startTime: Float,
        endTime: Float
    ) {
        viewModelScope.launch {
            try {
                val menu = Menu(
                    menu_id = 0,
                    menu_name = name,
                    order_by = description,
                    start_time = startTime,
                    end_time = endTime,
                    is_active = isActive,
                )
                menuRepository.insertMenu(menu)
                _errorMessage.value = "Menu Chart added successfully"
                loadMenus()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to add menu")
            }
        }
    }

    fun updateMenu(
        id: Long,
        name: String,
        description: String,
        isActive: Boolean,
        startTime: Float,
        endTime: Float
    ) {
        viewModelScope.launch {
            try {
                val menu = Menu(
                    menu_id = id,
                    menu_name = name,
                    order_by = description,
                    start_time = startTime,
                    end_time = endTime,
                    is_active = isActive,
                )
                menuRepository.updateMenu(menu)
                _errorMessage.value = "Menu Chart updated successfully"
                loadMenus()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to update menu")
            }
        }
    }

    fun deleteMenu(id: Long) {
        viewModelScope.launch {
            try {
                val response= menuRepository.deleteMenu(id)

                when(response.code()){
                    in 200..299 ->{
                        loadMenus()
                        _errorMessage.value = "Menu Chart deleted successfully"
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
                _uiState.value = UiState.Error(e.message ?: "Failed to delete Menu")
            }
        }
    }

    fun getOrderBy() {
        viewModelScope.launch {
            try {
                val response = menuRepository.getOrderBy()
                _orderBy.value = response["order_by"].toString()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to getOrderBy")
            }
        }
    }
}
