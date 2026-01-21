package com.warriortech.resb.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.R
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.viewmodel.login.RegistrationViewModel
import com.warriortech.resb.util.MobileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    navController: NavHostController,
    viewModel: RegistrationViewModel = hiltViewModel(),
    sessionManager: SessionManager
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val registrationResult by viewModel.registrationResult.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val isTablet = MobileUtils.isTablet(context)

    var emailOtpInput by remember { mutableStateOf("") }
    var mobileOtpInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadCompanyCode()
    }

    LaunchedEffect(registrationResult) {
        registrationResult?.let { message ->
            snackbarHostState.showSnackbar(message)
            if (message == "Registration successful!") {
                navController.navigate("login") {
                    popUpTo("registration") { inclusive = true }
                }
            }
            viewModel.clearRegistrationResult()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.register), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isTablet) 32.dp else 16.dp)
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Business Registration",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )

                Text(
                    text = "Enter your details to register and verify your account.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.mailId,
                            onValueChange = viewModel::updateMailId,
                            label = { Text("Email ID *") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            isError = uiState.emailError != null,
                            enabled = !uiState.isOtpSent
                        )

                        if (uiState.emailError != null) {
                            Text(
                                text = uiState.emailError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        OutlinedTextField(
                            value = uiState.ownerName,
                            onValueChange = viewModel::updateOwnerName,
                            label = { Text("Owner Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            enabled = !uiState.isOtpSent
                        )

                        OutlinedTextField(
                            value = uiState.companyName,
                            onValueChange = viewModel::updateCompanyName,
                            label = { Text("Company Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                            singleLine = true,
                            enabled = !uiState.isOtpSent
                        )

                        OutlinedTextField(
                            value = uiState.contactNo,
                            onValueChange = viewModel::updateContactNo,
                            label = { Text("WhatsApp Number with Country Code *") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            enabled = !uiState.isOtpSent
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = uiState.country,
                                onValueChange = viewModel::updateCountry,
                                label = { Text("Country *") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                enabled = !uiState.isOtpSent
                            )
                            OutlinedTextField(
                                value = uiState.state,
                                onValueChange = viewModel::updateState,
                                label = { Text("State *") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                enabled = !uiState.isOtpSent
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = uiState.isOtpSent,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "OTP Verification",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Enter the 6-digit OTPs sent to your email and mobile",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                            
                            OutlinedTextField(
                                value = emailOtpInput,
                                onValueChange = { if (it.length <= 6) emailOtpInput = it },
                                label = { Text("Email OTP") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 4.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            OutlinedTextField(
                                value = mobileOtpInput,
                                onValueChange = { if (it.length <= 6) mobileOtpInput = it },
                                label = { Text("Mobile OTP") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 4.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Button(
                                onClick = { viewModel.verifyOtpsAndRegister(emailOtpInput, mobileOtpInput) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                enabled = emailOtpInput.length == 6 && mobileOtpInput.length == 6 && !uiState.isLoading
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                } else {
                                    Text("Verify & Register")
                                }
                            }
                        }
                    }
                }

                if (!uiState.isOtpSent) {
                    Button(
                        onClick = { viewModel.sendOtp() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        enabled = !uiState.isLoading && validateFields(uiState)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Get OTP", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                } else {
                    TextButton(onClick = { viewModel.sendOtp() }) {
                        Text("Resend OTP", color = PrimaryGreen)
                    }
                }
            }
        }
    }
}

private fun validateFields(state: com.warriortech.resb.ui.viewmodel.login.RegistrationUiState): Boolean {
    return state.mailId.isNotBlank() &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(state.mailId).matches() &&
            state.ownerName.isNotBlank() &&
            state.companyName.isNotBlank() &&
            state.contactNo.isNotBlank() &&
            state.country.isNotBlank() &&
            state.state.isNotBlank()
}
