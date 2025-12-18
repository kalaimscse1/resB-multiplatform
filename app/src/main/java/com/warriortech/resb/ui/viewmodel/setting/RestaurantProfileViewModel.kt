package com.warriortech.resb.ui.viewmodel.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.RestaurantProfileRepository
import com.warriortech.resb.model.RestaurantProfile
import com.warriortech.resb.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantProfileViewModel @Inject constructor(
    private val restaurantProfileRepository: RestaurantProfileRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed class UiState {
        object Loading : UiState()
        data class Success(val profile: RestaurantProfile) : UiState()
        data class Error(val message: String) : UiState()
    }

    fun loadProfile() {
        viewModelScope.launch {

            try {
                _uiState.value = UiState.Loading
                val profile = sessionManager.getRestaurantProfile()
                val check = restaurantProfileRepository.getRestaurantProfile()
                if (check != null) {
                    _uiState.value = UiState.Success(check)
                } else if (profile != null) {
                    val pro = restaurantProfileRepository.addRestaurantProfile(profile)
                    _uiState.value = UiState.Success(pro!!)
                } else {
                    _uiState.value = UiState.Error("Profile not found")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateProfile(profile: RestaurantProfile) {
        viewModelScope.launch {
            try {
                val updatedProfile = restaurantProfileRepository.updateRestaurantProfile(profile)
                if (updatedProfile != null) {
                    loadProfile()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to update profile")
            }
        }
    }
}