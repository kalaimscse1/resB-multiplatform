package com.warriortech.resb.screens.settings

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import androidx.compose.foundation.background
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.warriortech.resb.R
import com.warriortech.resb.model.RestaurantProfile
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
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
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
    }
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
                "${RetrofitClient.BASE_URL}logo/getLogo/${sessionManager.getCompanyCode()}"
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