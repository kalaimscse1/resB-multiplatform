package com.warriortech.resb.ui.viewmodel.login

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.RegistrationRepository
import com.warriortech.resb.model.Registration
import com.warriortech.resb.model.RegistrationRequest
import com.warriortech.resb.model.RestaurantProfile
import com.warriortech.resb.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val registrationRepository: RegistrationRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    private val _registrationResult = MutableStateFlow<String?>(null)
    val registrationResult: StateFlow<String?> = _registrationResult.asStateFlow()

    fun updateCompanyMasterCode(value: String) {
        _uiState.value = _uiState.value.copy(companyMasterCode = value)
    }

    fun updateCompanyName(value: String) {
        _uiState.value = _uiState.value.copy(companyName = value)
    }

    fun updateOwnerName(value: String) {
        _uiState.value = _uiState.value.copy(ownerName = value)
    }

    fun updateAddress1(value: String) {
        _uiState.value = _uiState.value.copy(address1 = value)
    }

    fun updateAddress2(value: String) {
        _uiState.value = _uiState.value.copy(address2 = value)
    }

    fun updatePlace(value: String) {
        _uiState.value = _uiState.value.copy(place = value)
    }

    fun updatePincode(value: String) {
        _uiState.value = _uiState.value.copy(pincode = value)
    }

    fun updateContactNo(value: String) {
        _uiState.value = _uiState.value.copy(contactNo = value)
    }

    fun updateMailId(value: String) {
        _uiState.value = _uiState.value.copy(mailId = value)
    }

    fun updateCountry(value: String) {
        _uiState.value = _uiState.value.copy(country = value)
    }

    fun updateState(value: String) {
        _uiState.value = _uiState.value.copy(state = value)
    }

    fun updateYear(value: String) {
        _uiState.value = _uiState.value.copy(year = value)
    }

    fun updateDatabaseName(value: String) {
        _uiState.value = _uiState.value.copy(databaseName = value)
    }

    fun updateOrderPlan(value: String) {
        _uiState.value = _uiState.value.copy(orderPlan = value)
    }

    fun updateInstallDate(value: LocalDate) {
        _uiState.value = _uiState.value.copy(installDate = value)
    }

    fun updateSubscriptionDays(value: String) {
        val days = value.toLongOrNull() ?: 0L
        _uiState.value = _uiState.value.copy(subscriptionDays = days)
    }

    fun updateExpiryDate(value: String) {
        _uiState.value = _uiState.value.copy(expiryDate = value)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun updateIsBlock(value: Boolean) {
        _uiState.value = _uiState.value.copy(isBlock = value)
    }

    fun clearRegistrationResult() {
        _registrationResult.value = null
    }

    @SuppressLint("SuspiciousIndentation")
    fun loadCompanyCode() {
        viewModelScope.launch {
            try {
                val companyCode = registrationRepository.getCompanyCode()
                companyCode.let {
                    val code = it["company_master_code"] ?: ""
                    _uiState.value = _uiState.value.copy(
                        companyMasterCode = code,
                        databaseName = code
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun registerCompany() {
        val state = _uiState.value

        if (!validateForm(state)) {
            _registrationResult.value = "Please fill all required fields"
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        if (state.password == "kingtec2025#") {
            viewModelScope.launch {
                try {
                    val request = RegistrationRequest(
                        company_master_code = state.companyMasterCode,
                        company_name = state.companyName,
                        owner_name = state.ownerName,
                        address1 = state.address1,
                        address2 = state.address2,
                        place = state.place,
                        pincode = state.pincode,
                        contact_no = state.contactNo,
                        mail_id = state.mailId,
                        country = state.country,
                        state = state.state,
                        year = state.year,
                        database_name = state.databaseName,
                        order_plan = state.orderPlan,
                        install_date = state.installDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        subscription_days = state.subscriptionDays,
                        expiry_date = state.expiryDate,
                        is_block = state.isBlock
                    )
                    val response = registrationRepository.registerCompany(request)
                    response.collect { result ->
                        result.fold(
                            onSuccess = { res ->

                                val profile = RestaurantProfile(
                                    company_code = res.company_master_code,
                                    company_name = res.company_name,
                                    owner_name = res.owner_name,
                                    address1 = res.address1,
                                    address2 = res.address2,
                                    place = res.place,
                                    pincode = res.pincode,
                                    contact_no = res.contact_no,
                                    mail_id = res.mail_id,
                                    country = res.country,
                                    state = res.state,
                                    currency = "Rs",
                                    tax_no = "",
                                    decimal_point = 2L,
                                    upi_id = "",
                                    upi_name = ""
                                )
                                sessionManager.saveEmail(res.mail_id)
                                sessionManager.saveRestaurantProfile(profile)
                                sessionManager.saveCompanyCode(res.company_master_code)
                                createCompany(res)
                                _registrationResult.value = "Registration successful!"
                            },
                            onFailure = { error ->
                                _registrationResult.value = error.message
                            }
                        )
                    }
                } catch (e: Exception) {
                    _registrationResult.value = "Registration failed: ${e.message}"
                } finally {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        } else {
            _registrationResult.value = "Invalid Admin Password!"
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    /**
     * Creates a restaurant profile after successful registration.
     * @param response The registration response containing company details.
     */
    private fun createCompany(response: Registration) {
        viewModelScope.launch {
            try {
                val res = response

                val profile = RestaurantProfile(
                    company_code = res.company_master_code,
                    company_name = res.company_name,
                    owner_name = res.owner_name,
                    address1 = res.address1,
                    address2 = res.address2,
                    place = res.place,
                    pincode = res.pincode,
                    contact_no = res.contact_no,
                    mail_id = res.mail_id,
                    country = res.country,
                    state = res.state,
                    currency = "Rs.",
                    tax_no = "",
                    decimal_point = 2L,
                    upi_id = "",
                    upi_name = ""
                )
                val result = registrationRepository.addRestaurantProfile(profile)
                if (result != null) {
                    sessionManager.saveRestaurantProfile(profile)
                } else {
                    _registrationResult.value = "Registration UnSuccessful!"
                }
            } catch (e: Exception) {
                _registrationResult.value = "Registration failed: ${e.message}"
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun validateForm(state: RegistrationUiState): Boolean {
        return state.companyMasterCode.isNotBlank() &&
                state.companyName.isNotBlank() &&
                state.ownerName.isNotBlank() &&
                state.address1.isNotBlank() &&
                state.address2.isNotBlank() &&
                state.contactNo.isNotBlank() &&
                state.country.isNotBlank() &&
                state.state.isNotBlank() &&
                state.year.isNotBlank() &&
                state.orderPlan.isNotBlank() &&
                state.expiryDate.isNotBlank() &&
                state.password.isNotBlank()
    }
}

data class RegistrationUiState(
    val companyMasterCode: String = "",
    val companyName: String = "",
    val ownerName: String = "",
    val address1: String = "",
    val address2: String = "",
    val place: String = "",
    val pincode: String = "",
    val contactNo: String = "",
    val mailId: String = "",
    val country: String = "",
    val state: String = "",
    val year: String = "",
    val databaseName: String = "",
    val orderPlan: String = "",
    val installDate: LocalDate = LocalDate.now(),
    val subscriptionDays: Long = 0L,
    val expiryDate: String = "",
    val isBlock: Boolean = false,
    val isLoading: Boolean = false,
    val password: String = "",
    val emailError: String? = null,
)
