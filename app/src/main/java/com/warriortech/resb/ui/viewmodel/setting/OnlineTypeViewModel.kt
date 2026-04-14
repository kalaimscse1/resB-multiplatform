package com.warriortech.resb.ui.viewmodel.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.model.ApiResponse
import com.warriortech.resb.model.TblOnline
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OnlineTypeViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _onlineTypes = MutableStateFlow<List<TblOnline>>(emptyList())
    val onlineTypes: StateFlow<List<TblOnline>> = _onlineTypes.asStateFlow()

    sealed class UiState {
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    init {
        loadOnlineTypes()
    }

    fun loadOnlineTypes() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val tenantId = sessionManager.getCompanyCode() ?: ""
                val response = apiService.getAllOnlineOrders(tenantId)
                if (response.isSuccessful) {
                    _onlineTypes.value = response.body()?.filter { it.online_order_name != "--" } ?: emptyList()
                    _uiState.value = UiState.Success
                } else {
                    _uiState.value = UiState.Error("Failed to load platforms: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun saveOnlineType(onlineType: TblOnline) {
        viewModelScope.launch {
            try {
                val tenantId = sessionManager.getCompanyCode() ?: ""
                if (onlineType.online_order_id == 0L) {
                    // Check existence
                    val existsResponse = apiService.checkExistsByOnlineName(onlineType.online_order_name, tenantId)
                    if (existsResponse.data == true) {
                        _uiState.value = UiState.Error("Platform name already exists")
                        return@launch
                    }
                    apiService.createOnline(onlineType, tenantId)
                } else {
                    apiService.updateOnline(onlineType.online_order_id, onlineType, tenantId)
                }
                loadOnlineTypes()
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to save: ${e.message}")
            }
        }
    }

    fun deleteOnlineType(id: Long) {
        viewModelScope.launch {
            try {
                val tenantId = sessionManager.getCompanyCode() ?: ""
                apiService.deleteOnlineById(id, tenantId)
                loadOnlineTypes()
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to delete: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = UiState.Success



    }
}
