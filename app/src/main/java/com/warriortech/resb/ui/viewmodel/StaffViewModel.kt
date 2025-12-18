package com.warriortech.resb.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.CounterRepository
import com.warriortech.resb.data.repository.RoleRepository
import com.warriortech.resb.data.repository.StaffRepository
import com.warriortech.resb.data.repository.TableRepository
import com.warriortech.resb.model.Area
import com.warriortech.resb.model.Role
import com.warriortech.resb.model.TblCounter
import com.warriortech.resb.model.TblStaff
import com.warriortech.resb.model.TblStaffRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StaffUiState(
    val staff: List<TblStaff> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val staffRepository: StaffRepository,
    private val areaRepository: TableRepository,
    private val roleRepository: RoleRepository,
    private val counterRepository: CounterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffUiState())
    val uiState: StateFlow<StaffUiState> = _uiState.asStateFlow()

    private val _area = MutableStateFlow<List<Area>>(emptyList())
    val areas: StateFlow<List<Area>> = _area

    private val _counter = MutableStateFlow<List<TblCounter>>(emptyList())
    val counters: StateFlow<List<TblCounter>> = _counter

    private val _role = MutableStateFlow<List<Role>>(emptyList())
    val roles: StateFlow<List<Role>> = _role

    init {
        loadStaff()
    }

    private fun loadStaff() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val staff = staffRepository.getAllStaff()
                val areas = areaRepository.getAllAreas()
                val roles = roleRepository.getAllRoles()
                val counters = counterRepository.getAllCounters()
                _area.value = areas
                _role.value = roles
                _counter.value = counters
                _uiState.value = _uiState.value.copy(
                    staff = staff,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun addStaff(staff: TblStaffRequest) {
        viewModelScope.launch {
            try {
//                val staff = TblStaffRequest(
//                    staff_id = 1,
//                    staff_name = name,
//                    contact_no = phone,
//                    address = "",
//                    user_name = email,
//                    password = "",
//                    role_id = 1,
//                    last_login = "",
//                    is_block = false,
//                    counter_id = 1,
//                    area_id = 1,
//                    is_active = 1,
//                    commission = TODO()
//                )
                staffRepository.insertStaff(staff)
                loadStaff()
                _uiState.value = _uiState.value.copy(successMessage = "Staff added successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun updateStaff(staff: TblStaffRequest) {
        viewModelScope.launch {
            try {
                staffRepository.updateStaff(staff)
                loadStaff()
                _uiState.value = _uiState.value.copy(successMessage = "Staff updated successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun deleteStaff(staffId: Long) {
        viewModelScope.launch {
            try {
                staffRepository.deleteStaff(staffId)
                loadStaff()
                _uiState.value = _uiState.value.copy(successMessage = "Staff deleted successfully")
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
