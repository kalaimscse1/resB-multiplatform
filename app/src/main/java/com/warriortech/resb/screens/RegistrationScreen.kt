package com.warriortech.resb.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.R
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.ui.components.MobilePasswordOptimizedTextField
import com.warriortech.resb.ui.theme.GradientStart
import com.warriortech.resb.ui.viewmodel.login.RegistrationViewModel
import com.warriortech.resb.util.MobileUtils
import com.warriortech.resb.util.StringDropdown
import com.warriortech.resb.util.SubscriptionManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    val configuration = LocalContext.current
    val isTablet = MobileUtils.isTablet(configuration)

    var showInstallDatePicker by remember { mutableStateOf(false) }
    val installDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    val orderPlan = listOf("Trail", "CLosed")
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadCompanyCode()
    }
    // Handle registration result
    LaunchedEffect(registrationResult) {
        registrationResult?.let { message ->
            snackbarHostState.showSnackbar(message)
            if (message=="Registration successful!") {
                // Navigate to login screen after successful registration
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
                title = { Text(stringResource(R.string.register)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GradientStart
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(if (isTablet) 32.dp else 16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Company Information",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    OutlinedTextField(
                        value = uiState.companyMasterCode,
                        onValueChange = viewModel::updateCompanyMasterCode,
                        label = { Text("Company Master Code *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.companyName,
                        onValueChange = viewModel::updateCompanyName,
                        label = { Text("Company Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.ownerName,
                        onValueChange = viewModel::updateOwnerName,
                        label = { Text("Owner Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

//                    OutlinedTextField(
//                        value = uiState.databaseName,
//                        onValueChange = viewModel::updateDatabaseName,
//                        label = { Text("Database Name *") },
//                        modifier = Modifier.fillMaxWidth(),
//                        singleLine = true
//                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Address Information",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    OutlinedTextField(
                        value = uiState.address1,
                        onValueChange = viewModel::updateAddress1,
                        label = { Text("Address Line 1 *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.address2,
                        onValueChange = viewModel::updateAddress2,
                        label = { Text("Address Line 2 *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.place,
                            onValueChange = viewModel::updatePlace,
                            label = { Text("Place ") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = uiState.pincode,
                            onValueChange = viewModel::updatePincode,
                            label = { Text("PinCode ") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.state,
                            onValueChange = viewModel::updateState,
                            label = { Text("State *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = uiState.country,
                            onValueChange = viewModel::updateCountry,
                            label = { Text("Country *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Contact Information",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    OutlinedTextField(
                        value = uiState.contactNo,
                        onValueChange = viewModel::updateContactNo,
                        label = { Text("Contact Number *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.mailId,
                        onValueChange = viewModel::updateMailId,
                        label = { Text("Email ID *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )

                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Subscription Details",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    OutlinedTextField(
                        value = uiState.year,
                        onValueChange = viewModel::updateYear,
                        label = { Text("Year *") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
//
//                    OutlinedTextField(
//                        value = uiState.orderPlan,
//                        onValueChange = viewModel::updateOrderPlan,
//                        label = { Text("Order Plan *") },
//                        modifier = Modifier.fillMaxWidth(),
//                        singleLine = true
//                    )
                    StringDropdown(
                        options = orderPlan,
                        selectedOption = uiState.orderPlan,
                        onOptionSelected = { selectedStatus ->
                            viewModel.updateOrderPlan(selectedStatus) // Update your selectedStatus // Update your status state
                        },
                        label = "Order Plan *",
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Install Date with DatePicker
                    OutlinedTextField(
                        value = uiState.installDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        onValueChange = { },
                        label = { Text("Install Date *") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showInstallDatePicker = true }) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Select Date"
                                )
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.subscriptionDays.toString(),
                            onValueChange = { days ->
                                viewModel.updateSubscriptionDays(days)
                                // Calculate and save subscription end date
                                val subscriptionDays = days.toLongOrNull() ?: 0
                                val endDate = LocalDate.now().plusDays(subscriptionDays)
                                val subscriptionManager = SubscriptionManager(sessionManager)
                                subscriptionManager.saveSubscriptionEndDate(endDate)
                                viewModel.updateExpiryDate(
                                    LocalDate.now().plusDays(subscriptionDays)
                                        .toString()
                                )
                            },
                            label = { Text("Subscription Days *") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = uiState.expiryDate,
                            onValueChange = viewModel::updateExpiryDate,
                            label = { Text("Expiry Date ") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.isBlock,
                            onCheckedChange = viewModel::updateIsBlock
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Block Account")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MobilePasswordOptimizedTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.updatePassword(it) },
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
                    }
                }

            }

            Button(
                onClick = { viewModel.registerCompany() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Register Company")
                }
            }
        }
    }

    // Install Date Picker Dialog
    if (showInstallDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showInstallDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        installDatePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            viewModel.updateInstallDate(selectedDate)
                        }
                        showInstallDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInstallDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = installDatePickerState,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        }
    }
}
