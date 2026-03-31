package com.warriortech.resb.ui.viewmodel.setting


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.CustomerRepository
import com.warriortech.resb.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerSettingsViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _customers = MutableStateFlow<List<TblCustomer>>(emptyList())
    val customers: StateFlow<List<TblCustomer>> = _customers.asStateFlow()

    private val _additionalInfos = MutableStateFlow<List<TblCustomerInfoResponse>>(emptyList())
    val additionalInfos: StateFlow<List<TblCustomerInfoResponse>> = _additionalInfos.asStateFlow()

    sealed class UiState {
        object Loading : UiState()
        data class Success(val customers: List<TblCustomer>) : UiState()
        data class Error(val message: String) : UiState()
    }

    init {
        loadCustomers()
    }

    fun loadCustomers() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val customers = customerRepository.getAllCustomers()
                _customers.value = customers
                _uiState.value = UiState.Success(customers)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun selectCustomer(customer: TblCustomer) {
        viewModelScope.launch {
            try {
                _additionalInfos.value = customerRepository.getCustomerInfosByCustomerId(customer.customer_id)
            } catch (e: Exception) {
                _additionalInfos.value = emptyList()
            }
        }
    }

    fun clearAdditionalInfos() {
        _additionalInfos.value = emptyList()
    }

    fun saveCustomer(customer: TblCustomer, additionalInfos: List<TblCustomerInfoRequest>) {
        viewModelScope.launch {
            try {
                val savedCustomer = if (customer.customer_id == 0L) {
                    customerRepository.insertCustomer(customer)
                } else {
                    customerRepository.updateCustomer(customer)
                }
                
                // Save additional infos
                additionalInfos.forEach { info ->
                    val request = TblCustomerInfoRequest(
                        customer_info_id = info.customer_info_id,
                        customer_id = savedCustomer.customer_id,
                        address = info.address,
                        contact_no = info.contact_no,
                        is_active = true
                    )

                    if (request.customer_info_id == 0L) {
                        customerRepository.createCustomerInfo(request)
                    } else {
                        customerRepository.updateCustomerInfo(request)
                    }
                }
                
                loadCustomers()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to save customer")
            }
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch {
            try {
                customerRepository.deleteCustomer(id)
                loadCustomers()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to delete customer")
            }
        }
    }
}
