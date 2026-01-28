package com.warriortech.resb.ui.viewmodel.login

import android.annotation.SuppressLint
import android.util.Log
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

    fun sendOtp() {
        val email = _uiState.value.mailId
        val phone = _uiState.value.contactNo

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(emailError = "Invalid Email Address")
            return
        }
        
        if (phone.isBlank() || phone.length < 10) {
            _registrationResult.value = "Invalid Contact Number (minimum 10 digits required)"
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, emailError = null)
        
        viewModelScope.launch {
            try {
                // Generate two different 6-digit OTPs
                val emailOtp = (100000..999999).random().toString()
                val mobileOtp = (100000..999999).random().toString()
                val adminOtp = (100000..999999).random().toString()
                val msgOtp = uiState.value.databaseName + " - " + adminOtp
                val emailResult = registrationRepository.sendEmailOtp(email, emailOtp)
                val mobileResult = registrationRepository.sendOtp(phone, mobileOtp)
                val adminResult = registrationRepository.sendOtp("120363042991809443@g.us",msgOtp)
                
                if (emailResult.isNotEmpty() || mobileResult.isNotEmpty() || adminResult.isNotEmpty()){
                    _uiState.value = _uiState.value.copy(
                        generatedEmailOtp = emailOtp,
                        generatedMobileOtp = mobileOtp,
                        generatedAdminOtp = adminOtp,
                        isOtpSent = true,
                        isLoading = false
                    )
                    
                    val message = when {
                        emailResult.isNotEmpty() && mobileResult.isNotEmpty() && adminResult.isNotEmpty()-> "OTPs sent to $email and Whatsapp $phone and Admin Contact +91-7826040873, +91-9788106710, +91-9942014611, +91-8072944941"
                        emailResult.isNotEmpty() -> "OTP sent to $email (Whatsapp failed)"
                        else -> "OTP sent to Whatsapp $phone (Email failed)"
                    }
                    _registrationResult.value = message
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _registrationResult.value = "Failed to send OTP to both Email and Mobile."
                }

            } catch (e: Exception) {
                Log.d("RegistrationViewModel", "Error sending OTP: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false)
                _registrationResult.value = "Error sending OTP: ${e.message}"
            }
        }
    }

    fun verifyOtpsAndRegister(emailOtp: String, mobileOtp: String,adminOtp: String) {
        val state = _uiState.value
        
        var isEmailValid = emailOtp == state.generatedEmailOtp
        var isMobileValid = mobileOtp == state.generatedMobileOtp
        var isAdminValid = adminOtp == state.generatedAdminOtp
        
        // Handle cases where one of them might have failed to send but we allowed proceeding
        if (state.generatedEmailOtp.isEmpty()) isEmailValid = true 
        if (state.generatedMobileOtp.isEmpty()) isMobileValid = true
        if (state.generatedAdminOtp.isEmpty()) isAdminValid = true


        if (isEmailValid && isMobileValid && isAdminValid) {
            _uiState.value = _uiState.value.copy(isOtpVerified = true)
            registerCompanyInternal()
        } else {
            val errorMsg = when {
                !isEmailValid && !isMobileValid -> "Invalid Email and Mobile OTPs"
                !isEmailValid -> "Invalid Email OTP"
                !isAdminValid-> "Invalid Admin OTP"
                else -> "Invalid Mobile OTP"
            }
            _registrationResult.value = errorMsg
        }
    }

    private fun registerCompanyInternal() {
        val state = _uiState.value

        _uiState.value = _uiState.value.copy(isLoading = true)
        
        // Auto-process hidden fields
        val companyName = state.companyName
        val ownerName = state.ownerName
        val contactNo = state.contactNo
        val mailId = state.mailId
        val country = state.country
        val stateName = state.state
        
        val address1 = "N/A"
        val address2 = "N/A"
        val place = "N/A"
        val pincode = "000000"
        val year = LocalDate.now().year.toString()
        val orderPlan = "TRAIL"
        val subscriptionDays = 30L
        val expiryDate = LocalDate.now().plusDays(subscriptionDays).toString()

        viewModelScope.launch {
            try {
                val request = RegistrationRequest(
                    company_master_code = state.companyMasterCode,
                    company_name = companyName,
                    owner_name = ownerName,
                    address1 = address1,
                    address2 = address2,
                    place = place,
                    pincode = pincode,
                    contact_no = contactNo,
                    mail_id = mailId,
                    country = country,
                    state = stateName,
                    year = year,
                    database_name = state.companyMasterCode,
                    order_plan = orderPlan,
                    install_date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    subscription_days = subscriptionDays,
                    expiry_date = expiryDate,
                    is_block = false
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
    }

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
    val generatedEmailOtp: String = "",
    val generatedMobileOtp: String = "",
    val generatedAdminOtp: String = "",
    val isOtpSent: Boolean = false,
    val isOtpVerified: Boolean = false
)
