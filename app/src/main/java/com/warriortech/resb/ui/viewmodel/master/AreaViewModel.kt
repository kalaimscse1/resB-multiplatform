package com.warriortech.resb.ui.viewmodel.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.AreaRepository
import com.warriortech.resb.model.Area
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AreaUiState(
    val areas: List<Area> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AreaViewModel @Inject constructor(
    private val areaRepository: AreaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AreaUiState())
    val uiState: StateFlow<AreaUiState> = _uiState.asStateFlow()

    val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()


    init {
        loadAreas()
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun loadAreas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val areas = areaRepository.getAllAreas()
                areas.collect {
                    _uiState.value = _uiState.value.copy(
                        areas = it.filter { it.area_name != "--" },
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun addArea(name: String) {
        viewModelScope.launch {
            try {
                val area = Area(0, name, true)
                areaRepository.insertArea(area)
                loadAreas()
                _uiState.value = _uiState.value.copy(successMessage = "Area added successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun updateArea(area: Area) {
        viewModelScope.launch {
            try {
                areaRepository.updateArea(area)
                loadAreas()
                _uiState.value = _uiState.value.copy(successMessage = "Area updated successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun deleteArea(areaId: Long) {
        viewModelScope.launch {
            try {
                val response= areaRepository.deleteArea(areaId)
                when(response.code()){
                    in 200..299 ->{
                        loadAreas()
                        _errorMessage.value = "Area deleted successfully"
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
                        _uiState.value = _uiState.value.copy(errorMessage="${response.code()} : ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
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
