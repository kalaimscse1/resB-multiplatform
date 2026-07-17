package com.warriortech.resb.screens.settings

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.warriortech.resb.R
import com.warriortech.resb.model.RestaurantProfile
import com.warriortech.resb.model.TblBranchRequest
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.RetrofitClient
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.setting.RestaurantProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantProfileScreen(
    viewModel: RestaurantProfileViewModel = hiltViewModel(),
    apiService: ApiService,
    sessionManager: SessionManager,
    onBackPressed: () -> Unit,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val branchState by viewModel.branchState.collectAsStateWithLifecycle()
    val suggestedBranchCode by viewModel.suggestedBranchCode.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showAddBranchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    // React to branch creation result
    LaunchedEffect(branchState) {
        when (val state = branchState) {
            is RestaurantProfileViewModel.BranchState.Success -> {
                Toast.makeText(context, "Branch created successfully", Toast.LENGTH_SHORT).show()
                showAddBranchDialog = false
                viewModel.resetBranchState()
            }
            is RestaurantProfileViewModel.BranchState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetBranchState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.restaurant_profile),
                        color = SurfaceLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = SurfaceLight
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.fetchBranchCode()
                        showAddBranchDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Branch",
                            tint = SurfaceLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is RestaurantProfileViewModel.UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is RestaurantProfileViewModel.UiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            CompanySettingDialog(
                                setting = state.profile,
                                onSave = { newSetting ->
                                    scope.launch {
                                        viewModel.updateProfile(newSetting)
                                        snackbarHostState.showSnackbar("General Settings updated successfully")
                                    }
                                },
                                apiService,
                                sessionManager = sessionManager
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                is RestaurantProfileViewModel.UiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { viewModel.loadProfile() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }

        if (showAddBranchDialog) {
            AddBranchDialog(
                suggestedBranchCode = suggestedBranchCode,
                companyMasterCode = sessionManager.getCompanyCode() ?: "",
                isLoading = branchState is RestaurantProfileViewModel.BranchState.Loading,
                onDismiss = {
                    showAddBranchDialog = false
                    viewModel.resetBranchState()
                },
                onConfirm = { branchRequest ->
                    viewModel.createBranch(branchRequest)
                }
            )
        }
    }
}

@Composable
fun AddBranchDialog(
    suggestedBranchCode: String,
    companyMasterCode: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (TblBranchRequest) -> Unit
) {
    var branchCode by remember(suggestedBranchCode) { mutableStateOf(suggestedBranchCode) }
    var companyName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var address1 by remember { mutableStateOf("") }
    var address2 by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var contactNo by remember { mutableStateOf("") }
    var mailId by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var databaseName by remember { mutableStateOf("") }
    var orderPlan by remember { mutableStateOf("") }
    var installDate by remember { mutableStateOf("") }
    var subscriptionDays by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }

    // Validation errors
    var branchCodeError by remember { mutableStateOf("") }
    var companyNameError by remember { mutableStateOf("") }
    var ownerNameError by remember { mutableStateOf("") }
    var address1Error by remember { mutableStateOf("") }
    var placeError by remember { mutableStateOf("") }
    var pincodeError by remember { mutableStateOf("") }
    var contactNoError by remember { mutableStateOf("") }
    var mailIdError by remember { mutableStateOf("") }
    var countryError by remember { mutableStateOf("") }
    var stateError by remember { mutableStateOf("") }
    var yearError by remember { mutableStateOf("") }
    var databaseNameError by remember { mutableStateOf("") }
    var orderPlanError by remember { mutableStateOf("") }
    var installDateError by remember { mutableStateOf("") }
    var subscriptionDaysError by remember { mutableStateOf("") }
    var expiryDateError by remember { mutableStateOf("") }

    fun validate(): Boolean {
        var valid = true
        branchCodeError = if (branchCode.isBlank()) { valid = false; "Branch code is required" } else ""
        companyNameError = if (companyName.isBlank()) { valid = false; "Company name is required" } else ""
        ownerNameError = if (ownerName.isBlank()) { valid = false; "Owner name is required" } else ""
        address1Error = if (address1.isBlank()) { valid = false; "Address line 1 is required" } else ""
        placeError = if (place.isBlank()) { valid = false; "Place is required" } else ""
        pincodeError = if (pincode.isBlank()) { valid = false; "Pincode is required" } else ""
        contactNoError = if (contactNo.isBlank() || contactNo.length < 10) { valid = false; "Valid contact number required" } else ""
        mailIdError = if (mailId.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(mailId).matches()) { valid = false; "Valid email is required" } else ""
        countryError = if (country.isBlank()) { valid = false; "Country is required" } else ""
        stateError = if (state.isBlank()) { valid = false; "State is required" } else ""
        yearError = if (year.isBlank()) { valid = false; "Year is required" } else ""
        databaseNameError = if (databaseName.isBlank()) { valid = false; "Database name is required" } else ""
        orderPlanError = if (orderPlan.isBlank()) { valid = false; "Order plan is required" } else ""
        installDateError = if (installDate.isBlank()) { valid = false; "Install date is required" } else ""
        subscriptionDaysError = if (subscriptionDays.isBlank() || subscriptionDays.toLongOrNull() == null) { valid = false; "Subscription days must be a number" } else ""
        expiryDateError = if (expiryDate.isBlank()) { valid = false; "Expiry date is required" } else ""
        return valid
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Add Branch", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Branch Code
                OutlinedTextField(
                    value = branchCode,
                    onValueChange = { branchCode = it },
                    label = { Text("Branch Code *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = branchCodeError.isNotEmpty(),
                    singleLine = true
                )
                if (branchCodeError.isNotEmpty()) Text(branchCodeError, color = Color.Red, fontSize = 12.sp)

                // Company Name
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = companyNameError.isNotEmpty(),
                    singleLine = true
                )
                if (companyNameError.isNotEmpty()) Text(companyNameError, color = Color.Red, fontSize = 12.sp)

                // Owner Name
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Owner Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = ownerNameError.isNotEmpty(),
                    singleLine = true
                )
                if (ownerNameError.isNotEmpty()) Text(ownerNameError, color = Color.Red, fontSize = 12.sp)

                // Address Line 1
                OutlinedTextField(
                    value = address1,
                    onValueChange = { address1 = it },
                    label = { Text("Address Line 1 *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = address1Error.isNotEmpty(),
                    singleLine = true
                )
                if (address1Error.isNotEmpty()) Text(address1Error, color = Color.Red, fontSize = 12.sp)

                // Address Line 2
                OutlinedTextField(
                    value = address2,
                    onValueChange = { address2 = it },
                    label = { Text("Address Line 2") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Place
                OutlinedTextField(
                    value = place,
                    onValueChange = { place = it },
                    label = { Text("Place *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = placeError.isNotEmpty(),
                    singleLine = true
                )
                if (placeError.isNotEmpty()) Text(placeError, color = Color.Red, fontSize = 12.sp)

                // Pincode
                OutlinedTextField(
                    value = pincode,
                    onValueChange = { pincode = it },
                    label = { Text("Pincode *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = pincodeError.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                if (pincodeError.isNotEmpty()) Text(pincodeError, color = Color.Red, fontSize = 12.sp)

                // Contact No
                OutlinedTextField(
                    value = contactNo,
                    onValueChange = { contactNo = it },
                    label = { Text("Contact No *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = contactNoError.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                if (contactNoError.isNotEmpty()) Text(contactNoError, color = Color.Red, fontSize = 12.sp)

                // Mail ID
                OutlinedTextField(
                    value = mailId,
                    onValueChange = { mailId = it },
                    label = { Text("Email *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = mailIdError.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                if (mailIdError.isNotEmpty()) Text(mailIdError, color = Color.Red, fontSize = 12.sp)

                // Country
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = countryError.isNotEmpty(),
                    singleLine = true
                )
                if (countryError.isNotEmpty()) Text(countryError, color = Color.Red, fontSize = 12.sp)

                // State
                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("State *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = stateError.isNotEmpty(),
                    singleLine = true
                )
                if (stateError.isNotEmpty()) Text(stateError, color = Color.Red, fontSize = 12.sp)

                // Year
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = yearError.isNotEmpty(),
                    singleLine = true
                )
                if (yearError.isNotEmpty()) Text(yearError, color = Color.Red, fontSize = 12.sp)

                // Database Name
                OutlinedTextField(
                    value = databaseName,
                    onValueChange = { databaseName = it },
                    label = { Text("Database Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = databaseNameError.isNotEmpty(),
                    singleLine = true
                )
                if (databaseNameError.isNotEmpty()) Text(databaseNameError, color = Color.Red, fontSize = 12.sp)

                // Order Plan
                OutlinedTextField(
                    value = orderPlan,
                    onValueChange = { orderPlan = it },
                    label = { Text("Order Plan *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = orderPlanError.isNotEmpty(),
                    singleLine = true
                )
                if (orderPlanError.isNotEmpty()) Text(orderPlanError, color = Color.Red, fontSize = 12.sp)

                // Install Date
                OutlinedTextField(
                    value = installDate,
                    onValueChange = { installDate = it },
                    label = { Text("Install Date * (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = installDateError.isNotEmpty(),
                    singleLine = true
                )
                if (installDateError.isNotEmpty()) Text(installDateError, color = Color.Red, fontSize = 12.sp)

                // Subscription Days
                OutlinedTextField(
                    value = subscriptionDays,
                    onValueChange = { subscriptionDays = it },
                    label = { Text("Subscription Days *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = subscriptionDaysError.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                if (subscriptionDaysError.isNotEmpty()) Text(subscriptionDaysError, color = Color.Red, fontSize = 12.sp)

                // Expiry Date
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Expiry Date * (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = expiryDateError.isNotEmpty(),
                    singleLine = true
                )
                if (expiryDateError.isNotEmpty()) Text(expiryDateError, color = Color.Red, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validate()) {
                        onConfirm(
                            TblBranchRequest(
                                branch_code = branchCode.trim(),
                                companyMasterCode = companyMasterCode,
                                company_name = companyName.trim(),
                                owner_name = ownerName.trim(),
                                address1 = address1.trim(),
                                address2 = address2.trim(),
                                place = place.trim(),
                                pincode = pincode.trim(),
                                contact_no = contactNo.trim(),
                                mail_id = mailId.trim(),
                                country = country.trim(),
                                state = state.trim(),
                                year = year.trim(),
                                database_name = databaseName.trim(),
                                order_plan = orderPlan.trim(),
                                install_date = installDate.trim(),
                                subscription_days = subscriptionDays.toLong(),
                                expiry_date = expiryDate.trim()
                            )
                        )
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Branch")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!isLoading) onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CompanySettingDialog(
    setting: RestaurantProfile?,
    onSave: (RestaurantProfile) -> Unit,
    apiService: ApiService,
    sessionManager: SessionManager
) {
    var companyName by remember { mutableStateOf(setting?.company_name ?: "") }
    var ownerName by remember { mutableStateOf(setting?.owner_name ?: "") }
    var address1 by remember { mutableStateOf(setting?.address1 ?: "") }
    var address2 by remember { mutableStateOf(setting?.address2 ?: "") }
    var place by remember { mutableStateOf(setting?.place ?: "") }
    var pincode by remember { mutableStateOf(setting?.pincode ?: "") }
    var contactNo by remember { mutableStateOf(setting?.contact_no ?: "") }
    var mailId by remember { mutableStateOf(setting?.mail_id ?: "") }
    var country by remember { mutableStateOf(setting?.country ?: "") }
    var state by remember { mutableStateOf(setting?.state ?: "") }
    var currency by remember { mutableStateOf(setting?.currency ?: "") }
    var taxNo by remember { mutableStateOf(setting?.tax_no ?: "") }
    var decimalPoint by remember { mutableStateOf(setting?.decimal_point?.toString() ?: "2") }
    val context = LocalContext.current
    val logoUri = remember { mutableStateOf<Uri?>(null) }
    val logoUploadProgress = remember { mutableStateOf(false) }
    val uploadSuccess = remember { mutableStateOf(false) }
    var upiId by remember { mutableStateOf(setting?.upi_id ?: "") }
    var upiName by remember { mutableStateOf(setting?.upi_name ?: "") }

    val companyNameError = remember { mutableStateOf("") }
    val ownerNameError = remember { mutableStateOf("") }
    val address1Error = remember { mutableStateOf("") }
    val placeError = remember { mutableStateOf("") }
    val pincodeError = remember { mutableStateOf("") }
    val contactNoError = remember { mutableStateOf("") }
    val mailIdError = remember { mutableStateOf("") }
    val countryError = remember { mutableStateOf("") }
    val stateError = remember { mutableStateOf("") }
    val currencyError = remember { mutableStateOf("") }
    val taxNoError = remember { mutableStateOf("") }
    val decimalPointError = remember { mutableStateOf("") }


    fun validateInputs(): Boolean {
        var isValid = true

        if (companyName.isBlank()) {
            companyNameError.value = "Company Name is required"
            isValid = false
        } else companyNameError.value = ""

        if (ownerName.isBlank()) {
            ownerNameError.value = "Owner Name is required"
            isValid = false
        } else ownerNameError.value = ""

        if (address1.isBlank()) {
            address1Error.value = "Address Line 1 is required"
            isValid = false
        } else address1Error.value = ""

        if (place.isBlank()) {
            placeError.value = "Place is required"
            isValid = false
        } else placeError.value = ""

        if (pincode.isBlank()) {
            pincodeError.value = "Pincode is required"
            isValid = false
        } else pincodeError.value = ""

        if (contactNo.isBlank() || contactNo.length < 10) {
            contactNoError.value = "Valid contact number is required"
            isValid = false
        } else contactNoError.value = ""

        if (mailId.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(mailId).matches()) {
            mailIdError.value = "Valid email is required"
            isValid = false
        } else mailIdError.value = ""

        if (country.isBlank()) {
            countryError.value = "Country is required"
            isValid = false
        } else countryError.value = ""

        if (state.isBlank()) {
            stateError.value = "State is required"
            isValid = false
        } else stateError.value = ""

        if (currency.isBlank()) {
            currencyError.value = "Currency is required"
            isValid = false
        } else currencyError.value = ""

        if (taxNo.isBlank()) {
            taxNoError.value = "Tax number is required"
            isValid = false
        } else taxNoError.value = ""

        if (decimalPoint.isBlank() || decimalPoint.toIntOrNull() == null) {
            decimalPointError.value = "Decimal point must be a number"
            isValid = false
        } else decimalPointError.value = ""

        return isValid
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        logoUri.value = uri
        uploadSuccess.value = false
    }
    Column {
        OutlinedTextField(
            value = companyName,
            onValueChange = { companyName = it },
            label = { Text("Company Name ") },
            modifier = Modifier.fillMaxWidth()
        )
        if (companyNameError.value.isNotEmpty()) {
            Text(
                text = companyNameError.value,
                color = Color.Red,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = ownerName,
            onValueChange = { ownerName = it },
            label = { Text("Owner Name") },
            modifier = Modifier.fillMaxWidth()
        )
        if (ownerNameError.value.isNotEmpty()) {
            Text(
                text = ownerNameError.value,
                color = Color.Red,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = address1,
            onValueChange = { address1 = it },
            label = { Text("Address Line1") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = address2,
            onValueChange = { address2 = it },
            label = { Text("Address Line2") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = place,
            onValueChange = { place = it },
            label = { Text("Place") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = pincode,
            onValueChange = { pincode = it },
            label = { Text("Pincode") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = contactNo,
            onValueChange = { contactNo = it },
            label = { Text("Contact No") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = mailId,
            onValueChange = { mailId = it },
            label = { Text("Mail Id") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = country,
            onValueChange = { country = it },
            label = { Text("Country") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state,
            onValueChange = { state = it },
            label = { Text("State") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = currency,
            onValueChange = { currency = it },
            label = { Text("Currency") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = taxNo,
            onValueChange = { taxNo = it },
            label = { Text("Tax No") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = decimalPoint,
            onValueChange = { decimalPoint = it },
            label = { Text("Decimal Point") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = upiId,
            onValueChange = { upiId = it },
            label = { Text("Upi Id") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = upiName,
            onValueChange = { upiName = it },
            label = { Text("Upi Name") },
            modifier = Modifier.fillMaxWidth()
        )

        if (logoUploadProgress.value) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { launcher.launch("*/*") }) {
                Text("Choose Logo")
            }
            Spacer(Modifier.width(8.dp))
            logoUri.value?.let {
                Text(it.lastPathSegment ?: "Image selected", fontSize = 12.sp)
            }
        }

        logoUri.value?.let { uri ->
            Spacer(modifier = Modifier.height(8.dp))
            val mimeType = context.contentResolver.getType(uri)
            val supportedTypes = listOf("image/png", "image/jpeg", "image/svg+xml")
            if (mimeType !in supportedTypes) {
                Text("Unsupported file type", color = MaterialTheme.colorScheme.error)
            } else {
                Button(
                    onClick = {
                        logoUploadProgress.value = true
                        uploadLogo(
                            uri = uri,
                            context = context,
                            apiService = apiService,
                            sessionManager = sessionManager,
                            onSuccess = {
                                logoUploadProgress.value = false
                                uploadSuccess.value = true

                            },
                            onFailure = {
                                logoUploadProgress.value = false
                                Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                ) {
                    Text("Upload Logo")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (uploadSuccess.value) {
            val imageUrl =
                "${RetrofitClient.currentBaseUrl}logo/getLogo/${sessionManager.getCompanyCode()}"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Company Logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RectangleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Crop
                )
            }
            AlertDialog(
                onDismissRequest = { uploadSuccess.value = false },
                confirmButton = {
                    TextButton(onClick = { uploadSuccess.value = false }) {
                        Text("OK")
                    }
                },
                title = { Text("Upload Successful") },
                text = { Text("Your logo has been uploaded successfully.") }
            )
        }
        Button(
            onClick = {
                val newSetting = RestaurantProfile(
                    company_code = setting?.company_code ?: "",
                    company_name = companyName,
                    owner_name = ownerName,
                    address1 = address1,
                    address2 = address2,
                    place = place,
                    pincode = pincode,
                    contact_no = contactNo,
                    mail_id = mailId,
                    country = country,
                    state = state,
                    currency = currency,
                    tax_no = taxNo,
                    decimal_point = decimalPoint.toLongOrNull() ?: 2L,
                    upi_id = upiId,
                    upi_name = upiName
                )
                onSave(newSetting)
            }
        ) {
            Text(
                text = if (setting == null) "Add" else "Update",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun uploadLogo(
    uri: Uri,
    context: Context,
    apiService: ApiService,
    sessionManager: SessionManager,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
) {
    val contentResolver = context.contentResolver
    val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
    val inputStream = contentResolver.openInputStream(uri) ?: return onFailure()

    val requestBody = inputStream.readBytes().toRequestBody(mimeType.toMediaTypeOrNull())
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
    val filePart = MultipartBody.Part.createFormData("file", "logo.$extension", requestBody)

    val token = sessionManager.getCompanyCode() ?: ""
    val cleanedCompanyCode = token
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = apiService.uploadLogo(cleanedCompanyCode, filePart, cleanedCompanyCode)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) onSuccess() else onFailure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) { onFailure() }
        }
    }
}