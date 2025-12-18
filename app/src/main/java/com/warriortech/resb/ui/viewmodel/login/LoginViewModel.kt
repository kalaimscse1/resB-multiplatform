package com.warriortech.resb.ui.viewmodel.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.model.LoginRequest
import com.warriortech.resb.network.RetrofitClient
import com.warriortech.resb.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    val loginSuccess: Boolean = false
)

/**
 * ViewModel for handling user login functionality.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() { // Assuming you might inject dependencies later

//    var uiState by mutableStateOf(LoginUiState())
//        private set

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Handles changes to the company code input field.
     */

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

    /**
     * Handles changes to the username input field.
     */
    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username, loginError = null) }
    }

    /**
     * Handles changes to the password input field.
     */
    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, loginError = null) }
    }

    /**
     * Toggles the visibility of the password input field.
     */
    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !uiState.value.isPasswordVisible) }
    }

    /**
     * Validates the input fields to ensure they are not empty.
     */
    private fun validateInput(): Boolean {
        return uiState.value.username.isNotBlank() &&
                uiState.value.password.isNotBlank() &&
                uiState.value.companyCode.isNotBlank()
    }

    /**
     * Login function that attempts to log in the user with the provided credentials.
     */

    fun attemptLogin() {
        if (!validateInput()) {
            _uiState.update { it.copy(loginError = "Please fill all fields") }
            return
        }

        _uiState.update { it.copy(isLoading = true, loginError = null) }
        viewModelScope.launch {
            try {
                val check = RetrofitClient.apiService.checkIsBlockByMailId(
                    uiState.value.companyCode.trim(),
                    "KTS-COMPANY_MASTER"
                )
                sessionManager.saveCompanyCode(
                    check.data?.company_master_code?:""
                )

                val generalSetting = RetrofitClient.apiService.getGeneralSettings(
                    sessionManager.getCompanyCode()?:""
                )
                val profile = RetrofitClient.apiService.getRestaurantProfile(
                    tenantId = sessionManager.getCompanyCode()?:"",
                    companyCode = sessionManager.getCompanyCode()?:""
                )
                if (check.data !=null) {
                    val response = RetrofitClient.apiService.login(
                        request = LoginRequest(
                            companyCode = uiState.value.companyCode,
                            user_name = uiState.value.username,
                            password = uiState.value.password
                        ),
                        tenantId = sessionManager.getCompanyCode()?:""
                    )

                    if (response.success && response.data != null) {
                        sessionManager.saveEmail(_uiState.value.companyCode)
                        val authResponse = response.data
                        val general = generalSetting.body()
                        Log.d("LoginViewModel", "General Settings: ${authResponse.user}")
                        sessionManager.saveUserLogin(true)
                        sessionManager.saveAuthToken(authResponse.token)
                        sessionManager.saveUser(authResponse.user)
                        sessionManager.saveGeneralSetting(
                            general?.get(0) ?: error("general setting failed")
                        )
                        sessionManager.saveDecimalPlaces(profile.decimal_point)
                        sessionManager.saveRestaurantProfile(profile)

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

    /**
     * Resets the login success state after handling the login result.
     */
    fun onLoginHandled() {
        _uiState.update { it.copy(loginSuccess = false, loginError = null) }
    }
}