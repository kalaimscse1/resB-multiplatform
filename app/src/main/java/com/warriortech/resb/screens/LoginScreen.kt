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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.BorderStroke
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.IconButton
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Scaffold
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warriortech.resb.R
import com.warriortech.resb.ui.components.*
import com.warriortech.resb.ui.theme.Dimensions
import com.warriortech.resb.ui.viewmodel.login.LoginViewModel
import com.warriortech.resb.ui.viewmodel.login.LoginUiState
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.focus.onFocusChanged
import com.warriortech.resb.network.RetrofitClient
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.util.getDeviceInfo

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
    sessionManager: SessionManager
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    var passwordVisible by remember { mutableStateOf(false) }
    val code = sessionManager.getCompanyCode() ?: ""

    var showIpDialog by remember { mutableStateOf(false) }
    var showSettingsIcon by remember { mutableStateOf(false) }
    var ipInput by remember { mutableStateOf("") }
    
    val deviceInfo = getDeviceInfo()
    // Show side keyboard for tablets in landscape or dedicated POS devices (usually wide landscape)
    val isLandscapeMode = deviceInfo.isLandscape
    val showSideKeyboard = isLandscapeMode && (deviceInfo.isTablet || deviceInfo.isLargeTablet)
    
    var focusedField by remember { mutableStateOf("username") }

    // Detect keyboard height in dp
    val imeHeight = WindowInsets.ime.getBottom(LocalDensity.current)
    val animatedPadding by animateDpAsState(
        targetValue = if (imeHeight > 0) imeHeight.dp else 0.dp,
        animationSpec = spring()
    )

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            keyboardController?.hide()
            onLoginSuccess()
            viewModel.onLoginHandled()
        }
    }

    LaunchedEffect(uiState.loginError) {
        uiState.loginError?.let { error ->
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(error)
            }
        }
    }

    if (showIpDialog) {
        AlertDialog(
            onDismissRequest = { showIpDialog = false },
            title = { Text("Server Configuration") },
            text = {
                Column {
                    Text("Enter Server API URL:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ipInput,
                        onValueChange = { ipInput = it },
                        label = { Text("Base URL") },
                        placeholder = { Text("192.168.1.1:5050") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (ipInput.isNotBlank()) {
                        sessionManager.saveBaseUrl("http://$ipInput/api/")
                        coroutineScope.launch {
                            scaffoldState.snackbarHostState.showSnackbar("Base URL updated")
                        }
                    }
                    showIpDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showOtpDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissOtpDialog,
            title = { Text("OTP Verification") },
            text = {
                Column {
                    Text("Enter the 4-digit OTP sent to your Admin:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.otpInput,
                        onValueChange = viewModel::onOtpInputChange,
                        label = { Text("OTP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::verifyOtpAndLogin) {
                    Text("Verify & Login")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissOtpDialog) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { },
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                actions = {
                    if (showSettingsIcon) {
                        IconButton(onClick = { showIpDialog = true }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Server Settings",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showSideKeyboard) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Login form (logo + fields)
                Column(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Dimensions.spacingXL),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LoginFormContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        sessionManager = sessionManager,
                        code = code,
                        onRegisterClick = onRegisterClick,
                        onFocusField = { focusedField = it },
                        passwordVisible = passwordVisible,
                        onPasswordVisibleChange = { passwordVisible = it },
                        showSettingsIcon = showSettingsIcon,
                        onSettingsIconToggle = { showSettingsIcon = !showSettingsIcon },
                        keyboardController = keyboardController,
                        logoSize = 120.dp
                    )
                }

                // Right side: Full QWERTY keyboard
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Screen Keyboard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    FullKeyboard(
                        onKeyClick = { char ->
                            when (focusedField) {
                                "email" -> viewModel.onCompanyCodeChange(uiState.companyCode + char)
                                "username" -> viewModel.onUsernameChange(uiState.username + char)
                                "password" -> viewModel.onPasswordChange(uiState.password + char)
                            }
                        },
                        onDelete = {
                            when (focusedField) {
                                "email" -> viewModel.onCompanyCodeChange(uiState.companyCode.dropLast(1))
                                "username" -> viewModel.onUsernameChange(uiState.username.dropLast(1))
                                "password" -> viewModel.onPasswordChange(uiState.password.dropLast(1))
                            }
                        },
                        onClear = {
                            when (focusedField) {
                                "email" -> viewModel.onCompanyCodeChange("")
                                "username" -> viewModel.onUsernameChange("")
                                "password" -> viewModel.onPasswordChange("")
                            }
                        }
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = animatedPadding) // animate upward shift
                    .padding(horizontal = Dimensions.spacingL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LoginFormContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    sessionManager = sessionManager,
                    code = code,
                    onRegisterClick = onRegisterClick,
                    onFocusField = { focusedField = it },
                    passwordVisible = passwordVisible,
                    onPasswordVisibleChange = { passwordVisible = it },
                    showSettingsIcon = showSettingsIcon,
                    onSettingsIconToggle = { showSettingsIcon = !showSettingsIcon },
                    keyboardController = keyboardController
                )
            }
        }
    }
}

@Composable
fun LoginFormContent(
    uiState: LoginUiState,
    viewModel: LoginViewModel,
    sessionManager: SessionManager,
    code: String,
    onRegisterClick: () -> Unit,
    onFocusField: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibleChange: (Boolean) -> Unit,
    showSettingsIcon: Boolean,
    onSettingsIconToggle: () -> Unit,
    keyboardController: SoftwareKeyboardController?,
    logoSize: androidx.compose.ui.unit.Dp = 180.dp
) {
    // Logo
    Image(
        painter = painterResource(id = R.drawable.resb_logo1),
        contentDescription = "Restaurant Logo",
        modifier = Modifier
            .size(logoSize)
            .padding(bottom = Dimensions.spacingL)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onSettingsIconToggle()
                    }
                )
            }
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
        if (sessionManager.getEmail().isNullOrBlank()){
            MobileOptimizedTextField(
                value = uiState.companyCode,
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
                modifier = Modifier.onFocusChanged { if (it.isFocused) onFocusField("email") }
            )
            Spacer(modifier = Modifier.height(Dimensions.spacingM))
        }

        MobileOptimizedTextField(
            value = uiState.username,
            onValueChange = viewModel::onUsernameChange,
            label = "Username",
            leadingIcon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Username",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.onFocusChanged { if (it.isFocused) onFocusField("username") }
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingM))

        MobilePasswordOptimizedTextField(
            value = uiState.password,
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

                IconButton(onClick = { onPasswordVisibleChange(!passwordVisible) }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
            modifier = Modifier.onFocusChanged { if (it.isFocused) onFocusField("password") }
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingL))

        MobileOptimizedButton(
            onClick = {
                keyboardController?.hide()
                viewModel.attemptLogin()
            },
            enabled = !uiState.isLoading,
            text = if (uiState.isLoading) "Logging in..." else "Login",
            icon = if (uiState.isLoading) null else Icons.Default.Login
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Register button
        if (sessionManager.getCompanyCode().isNullOrBlank()) {
            OutlinedButton(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading

            ) {
                Text(
                    text = stringResource(R.string.register),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun FullKeyboard(
    onKeyClick: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isShifted by remember { mutableStateOf(false) }

    val rows = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("Shift", "z", "x", "c", "v", "b", "n", "m", "⌫"),
        listOf("C", "@", ".", "_", "-", "Space")
    )

    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    val weight = when (key) {
                        "Space" -> 3f
                        "Shift", "⌫", "C" -> 1.5f
                        else -> 1f
                    }
                    
                    KeyboardKey(
                        text = if (isShifted && key.length == 1) key.uppercase() else key,
                        modifier = Modifier.weight(weight),
                        onClick = {
                            when (key) {
                                "Shift" -> isShifted = !isShifted
                                "⌫" -> onDelete()
                                "C" -> onClear()
                                "Space" -> onKeyClick(" ")
                                else -> {
                                    val char = if (isShifted) key.uppercase() else key
                                    onKeyClick(char)
                                }
                            }
                        },
                        isSpecial = key in listOf("Shift", "⌫", "C", "Space")
                    )
                }
            }
        }
    }
}

@Composable
fun KeyboardKey(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isSpecial: Boolean = false
) {
    Surface(
        modifier = modifier
            .height(52.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSpecial) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = if (!isSpecial) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Medium,
                color = if (isSpecial) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
