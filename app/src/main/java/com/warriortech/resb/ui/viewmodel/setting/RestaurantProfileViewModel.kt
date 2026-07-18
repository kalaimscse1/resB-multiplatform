package com.warriortech.resb.ui.viewmodel.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.RestaurantProfileRepository
import com.warriortech.resb.model.RestaurantProfile
import com.warriortech.resb.model.TblBranchRequest
import com.warriortech.resb.model.TblBranchResponse
import com.warriortech.resb.network.ApiService
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
    private val sessionManager: SessionManager,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _branchState = MutableStateFlow<BranchState>(BranchState.Idle)
    val branchState: StateFlow<BranchState> = _branchState.asStateFlow()

    private val _suggestedBranchCode = MutableStateFlow<String>("")
    val suggestedBranchCode: StateFlow<String> = _suggestedBranchCode.asStateFlow()

    sealed class UiState {
        object Loading : UiState()
        data class Success(val profile: RestaurantProfile) : UiState()
        data class Error(val message: String) : UiState()
    }

    sealed class BranchState {
        object Idle : BranchState()
        object Loading : BranchState()
        data class Success(val branch: TblBranchResponse) : BranchState()
        data class Error(val message: String) : BranchState()
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

    fun fetchBranchCode() {
        viewModelScope.launch {
            try {
                val tenantId = sessionManager.getCompanyCode() ?: ""
                val response = apiService.getBranchCode("KTS-COMPANY_MASTER")
                if (response.isSuccessful) {
                    val branchCode = response.body()!!
                    branchCode.let {
                        _suggestedBranchCode.value = it["branch_code"] ?: ""
                    }
                }
            } catch (e: Exception) {
                // Non-critical; leave blank so user can type manually
            }
        }
    }

    fun createBranch(branch: TblBranchRequest) {
        viewModelScope.launch {
            _branchState.value = BranchState.Loading
            try {
                val tenantId = sessionManager.getCompanyCode() ?: ""
                val response = apiService.createBranch(branch, "KTS-COMPANY_MASTER")
                if (response.isSuccessful && response.body() != null) {
                    _branchState.value = BranchState.Success(response.body()!!)
                } else {
                    _branchState.value = BranchState.Error("Failed to create branch: ${response.message()}")
                }
            } catch (e: Exception) {
                _branchState.value = BranchState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetBranchState() {
        _branchState.value = BranchState.Idle
    }
}