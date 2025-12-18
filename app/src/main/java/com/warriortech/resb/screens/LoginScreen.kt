package com.warriortech.resb.screens

import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warriortech.resb.R
import com.warriortech.resb.ui.components.*
import com.warriortech.resb.ui.theme.Dimensions
import com.warriortech.resb.ui.viewmodel.login.LoginViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.warriortech.resb.network.SessionManager

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
    sessionManager: SessionManager
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    var passwordVisible by remember { mutableStateOf(false) }
    val code = sessionManager.getCompanyCode() ?: ""

    // Detect keyboard height in dp
    val imeHeight = WindowInsets.ime.getBottom(LocalDensity.current)
    val animatedPadding by animateDpAsState(
        targetValue = if (imeHeight > 0) imeHeight.dp else 0.dp,
        animationSpec = spring()
    )

    LaunchedEffect(uiState.value.loginSuccess) {
        if (uiState.value.loginSuccess) {
            keyboardController?.hide()
            onLoginSuccess()
            viewModel.onLoginHandled()
        }
    }

    LaunchedEffect(uiState.value.loginError) {
        uiState.value.loginError?.let { error ->
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(error)
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = animatedPadding) // animate upward shift
                .padding(horizontal = Dimensions.spacingL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.resb_logo1),
                contentDescription = "Restaurant Logo",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = Dimensions.spacingL)
            )

            // Welcome text
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = Dimensions.spacingS)
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingXL))

            /**
             * Mobile-optimized login form
             */
            MobileOptimizedCardLogin(
                modifier = Modifier.fillMaxWidth()
                    .padding(10.dp)
            ) {
                MobileOptimizedTextField(
                    value = uiState.value.companyCode,
                    onValueChange = viewModel::onCompanyCodeChange,
                    label = "Email Id",
                    enabled = code.isBlank(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "CompanyCode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                )
                Spacer(modifier = Modifier.height(Dimensions.spacingM))
                MobileOptimizedTextField(
                    value = uiState.value.username,
                    onValueChange = viewModel::onUsernameChange,
                    label = "Username",
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Username",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingM))

                MobilePasswordOptimizedTextField(
                    value = uiState.value.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Password",
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Password",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image =
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val description =
                            if (passwordVisible) "Hide password" else "Show password"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingL))

                MobileOptimizedButton(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.attemptLogin()
                    },
                    enabled = !uiState.value.isLoading,
                    text = if (uiState.value.isLoading) "Logging in..." else "Login",
                    icon = if (uiState.value.isLoading) null else Icons.Default.Login
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Register button
                if (sessionManager.getCompanyCode().isNullOrBlank()) {
                    OutlinedButton(
                        onClick = onRegisterClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.value.isLoading
                    ) {
                        Text(
                            text = stringResource(R.string.register),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}