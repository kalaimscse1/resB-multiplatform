package com.warriortech.resb.ui.viewmodel.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.RoleRepository
import com.warriortech.resb.model.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoleSettingsUiState(
    val roles: List<Role> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RoleSettingsViewModel @Inject constructor(
    private val roleRepository: RoleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoleSettingsUiState())
    val uiState: StateFlow<RoleSettingsUiState> = _uiState

    fun loadRoles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val roles = roleRepository.getAllRoles()
                _uiState.value = _uiState.value.copy(
                    roles = roles.filter { it.role != "--" },
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun addRole(role: Role) {
        viewModelScope.launch {
            try {
                val newRole = roleRepository.createRole(role)
                if (newRole != null) {
                    loadRoles()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateRole(role: Role) {
        viewModelScope.launch {
            try {
                val updatedRole = roleRepository.updateRole(role)
                if (updatedRole != null) {
                    loadRoles()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteRole(id: Long) {
        viewModelScope.launch {
            try {
                val success = roleRepository.deleteRole(id)
                if (success) {
                    loadRoles()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
