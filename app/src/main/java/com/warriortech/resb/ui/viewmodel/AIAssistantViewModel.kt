package com.warriortech.resb.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.ai.AIRepository
import com.warriortech.resb.data.local.dao.MenuItemDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager


@HiltViewModel
class AIAssistantViewModel @Inject constructor(
    private val aiRepository: AIRepository,
    private val menuItemDao: MenuItemDao,
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIAssistantUiState())
    val uiState: StateFlow<AIAssistantUiState> = _uiState.asStateFlow()

    fun setApiKey(apiKey: String) {
        aiRepository.setApiKey(apiKey)
        _uiState.update { it.copy(apiKeyConfigured = true) }
    }

    fun enhanceMenuDescriptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val menuItems = menuItemDao.getAllMenuItems()
                val results = mutableListOf<String>()

                menuItems.collect { menuItem ->
                    menuItem.map {
                        val result = aiRepository.generateMenuDescription(it.toModel())
                        result.onSuccess { description ->
                            results.add("${it.menu_item_name}: $description")
                        }.onFailure { error ->
                            results.add("${it.menu_item_name}: Failed to generate description - ${error.message}")
                        }
                    }

                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        results = results,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to enhance menu: ${e.message}"
                    )
                }
            }
        }
    }

    fun generateUpsellSuggestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Sample order items for demonstration
                val sampleOrderItems =
                    apiService.getAllOrderDetails(sessionManager.getCompanyCode() ?: "").body()!!

                val result = aiRepository.suggestUpsells(sampleOrderItems)
                result.onSuccess { suggestions ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            results = suggestions,
                            errorMessage = null
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to generate suggestions: ${error.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to generate suggestions: ${e.message}"
                    )
                }
            }
        }
    }

    fun analyzeSalesData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Sample sales data for demonstration
                val sampleSalesData =
                    apiService.getAllOrderDetails(sessionManager.getCompanyCode() ?: "").body()!!

                val result = aiRepository.analyzeSalesData(sampleSalesData)
                Log.d("AIAnalysis", "Analysis result: $result")
                result.onSuccess { analysis ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            results = listOf(analysis),
                            errorMessage = null
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to analyze sales: ${error.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to analyze sales: ${e.message}"
                    )
                }
            }
        }
    }

    fun generateCustomerRecommendations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Sample customer order history
                val customerHistory =
                    apiService.getAllOrderDetails(sessionManager.getCompanyCode() ?: "").body()!!

                val result = aiRepository.generateCustomerRecommendations(customerHistory)
                result.onSuccess { recommendations ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            results = recommendations,
                            errorMessage = null
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to generate recommendations: ${error.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to generate recommendations: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearResults() {
        _uiState.update { it.copy(results = emptyList(), errorMessage = null) }
    }
}

data class AIAssistantUiState(
    val isLoading: Boolean = false,
    val apiKeyConfigured: Boolean = false,
    val results: List<String> = emptyList(),
    val errorMessage: String? = null
)
