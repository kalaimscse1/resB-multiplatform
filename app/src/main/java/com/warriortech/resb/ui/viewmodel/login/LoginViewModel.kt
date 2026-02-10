package com.warriortech.resb.ui.viewmodel.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.model.LoginRequest
import com.warriortech.resb.network.RetrofitClient
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.network.WhatsAppApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import kotlin.random.Random

/**
 * ViewModel for managing the login state and actions.
 */
data class LoginUiState(
    val companyCode: String = "",
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val loginError: String? = null,
    val loginSuccess: Boolean = false,
    val showOtpDialog: Boolean = false,
    val generatedOtp: String = "",
    val otpInput: String = ""
)

/**
 * ViewModel for handling user login functionality.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val whatsappApi: WhatsAppApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Pre-fill company code when ViewModel is created
        val savedCompanyCode = sessionManager.getEmail() ?: ""
        _uiState.update { it.copy(companyCode = savedCompanyCode) }

        val savedUserName = sessionManager.getUser()?.user_name ?: ""
        _uiState.update { it.copy(username = savedUserName) }
    }

    fun onCompanyCodeChange(companyCode: String) {
        _uiState.update { it.copy(companyCode = companyCode, loginError = null) }
    }

    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username, loginError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, loginError = null) }
    }

    fun onOtpInputChange(otp: String) {
        _uiState.update { it.copy(otpInput = otp) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !uiState.value.isPasswordVisible) }
    }

    private fun validateInput(): Boolean {
        return uiState.value.username.isNotBlank() &&
                uiState.value.password.isNotBlank() &&
                uiState.value.companyCode.isNotBlank()
    }

    fun attemptLogin() {
        if (!validateInput()) {
            _uiState.update { it.copy(loginError = "Please fill all fields") }
            return
        }

        val code = sessionManager.getCompanyCode() ?: ""
        if (code.isEmpty()) {
            sendAdminOtp()
        } else {
            processLogin()
        }
    }

    private fun sendAdminOtp() {
        _uiState.update { it.copy(isLoading = true, loginError = null) }
        viewModelScope.launch {
            try {
                val otp = Random.nextInt(1000, 9999).toString()
                val msgOtp = uiState.value.companyCode + " - " + otp
                val response = whatsappApi.sendWhatsApp(
                    secret = "66a02ca4cbae00a9b996ba9d1f62a51c56cbccd1".toRequestBody(),
                    account = "1768990496a87ff679a2f3e71d9181a67b7542122c6970a7204c38d".toRequestBody(),
                    recipient = "120363042991809443@g.us".toRequestBody(),           // +919876543210
                    type = "text".toRequestBody(),
                    message = "Your OTP For $msgOtp".toRequestBody()
                )

                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showOtpDialog = true,
                            generatedOtp = otp
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginError = "Failed to send verification OTP. Please check your Email Id."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginError = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun verifyOtpAndLogin() {
        if (uiState.value.otpInput == uiState.value.generatedOtp) {
            _uiState.update { it.copy(showOtpDialog = false) }
            processLogin()
        } else {
            _uiState.update { it.copy(loginError = "Invalid OTP") }
        }
    }

    fun dismissOtpDialog() {
        _uiState.update { it.copy(showOtpDialog = false, isLoading = false) }
    }

    private fun processLogin() {
        _uiState.update { it.copy(isLoading = true, loginError = null) }
        viewModelScope.launch {
            try {
                val check = RetrofitClient.masterApiService.checkIsBlockByMailId(
                    uiState.value.companyCode.trim(),
                    "KTS-COMPANY_MASTER"
                )
                
                if (check.data != null) {
                    val companyMasterCode = check.data.company_master_code
                    sessionManager.saveCompanyCode(companyMasterCode)

                    val generalSetting = RetrofitClient.apiService.getGeneralSettings(companyMasterCode)
                    val profile = RetrofitClient.apiService.getRestaurantProfile(
                        tenantId = companyMasterCode,
                        companyCode = companyMasterCode
                    )

                    val response = RetrofitClient.apiService.login(
                        request = LoginRequest(
                            companyCode = uiState.value.companyCode,
                            user_name = uiState.value.username,
                            password = uiState.value.password
                        ),
                        tenantId = companyMasterCode
                    )

                    if (response.success && response.data != null) {
                        sessionManager.saveEmail(_uiState.value.companyCode)
                        val authResponse = response.data
                        val general = generalSetting.body()
                        
                        sessionManager.saveUserLogin(true)
                        sessionManager.saveAuthToken(authResponse.token)
                        sessionManager.saveUser(authResponse.user)
                        sessionManager.saveGeneralSetting(
                            general?.get(0) ?: throw Exception("General settings not found")
                        )
                        sessionManager.saveDecimalPlaces(profile.decimal_point)
                        sessionManager.saveRestaurantProfile(profile)
                        Log.d("LoginViewModel", "${sessionManager.getGeneralSetting()}")

                        _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loginError = "Login failed: ${response.message}"
                            )
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, loginError = check.message) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginError = "Error: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun onLoginHandled() {
        _uiState.update { it.copy(loginSuccess = false, loginError = null) }
    }
}
