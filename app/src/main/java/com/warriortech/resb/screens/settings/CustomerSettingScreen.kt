package com.warriortech.resb.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.TblCustomer
import com.warriortech.resb.model.TblCustomerInfoRequest
import com.warriortech.resb.ui.components.ModernDivider
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.setting.CustomerSettingsViewModel
import com.warriortech.resb.util.ReusableBottomSheet
import com.warriortech.resb.util.getDeviceInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSettingsScreen(
    viewModel: CustomerSettingsViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val additionalInfos by viewModel.additionalInfos.collectAsStateWithLifecycle()
    
    var showDialog by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<TblCustomer?>(null) }
    
    val deviceInfo = getDeviceInfo()
    val isTabletLandscape = deviceInfo.isTablet && deviceInfo.isLandscape
    val showAdaptiveGrid = isTabletLandscape || deviceInfo.isLargeTablet

    // Form States
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var gstNo by remember { mutableStateOf("") }
    
    // Multiple Additional Info States
    var additionalInfoList by remember { mutableStateOf(listOf<TblCustomerInfoRequest>()) }

    LaunchedEffect(selectedCustomer, additionalInfos) {
        if (selectedCustomer != null) {
            val it = selectedCustomer!!
            name = it.customer_name
            contact = it.contact_no
            email = it.email_address
            address = it.address
            gstNo = it.gst_no
            
            additionalInfoList = additionalInfos.map { 
                TblCustomerInfoRequest(
                    customer_info_id = it.customer_info_id,
                    customer_id = it.customer.customer_id,
                    address = it.address, 
                    contact_no = it.contact_no,
                    is_active = it.is_active
                )
            }
        } else {
            name = ""; contact = ""; email = ""; address = ""; gstNo = ""
            additionalInfoList = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.Default.ArrowBack, contentDescription = "Back",
                            tint = SurfaceLight
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        selectedCustomer = null
                        viewModel.clearAdditionalInfos()
                        showDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Customer", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(SurfaceLight)) {
            if (uiState is CustomerSettingsViewModel.UiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryGreen)
            } else {
                if (showAdaptiveGrid) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 250.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(customers) { customer ->
                            CustomerItem(
                                customer = customer,
                                onClick = {
                                    viewModel.selectCustomer(customer)
                                    selectedCustomer = customer
                                    showDialog = true
                                },
                                onDelete = { viewModel.deleteCustomer(customer.customer_id) },
                                isGrid = true
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(customers) { customer ->
                            CustomerItem(
                                customer = customer,
                                onClick = {
                                    viewModel.selectCustomer(customer)
                                    selectedCustomer = customer
                                    showDialog = true
                                },
                                onDelete = { viewModel.deleteCustomer(customer.customer_id) }
                            )
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (selectedCustomer == null) "Add Customer" else "Edit Customer") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact No") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = gstNo, onValueChange = { gstNo = it }, label = { Text("GST No") }, modifier = Modifier.fillMaxWidth())
                        
                        ModernDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Additional Information", style = MaterialTheme.typography.labelMedium, color = PrimaryGreen)
                            IconButton(onClick = {
                                additionalInfoList = additionalInfoList + TblCustomerInfoRequest(customer_id = selectedCustomer?.customer_id ?: 0L, address="", contact_no="")
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add More Info", tint = PrimaryGreen)
                            }
                        }
                        
                        additionalInfoList.forEachIndexed { index, info ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        IconButton(onClick = {
                                            additionalInfoList = additionalInfoList.filterIndexed { i, _ -> i != index }
                                        }) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    OutlinedTextField(
                                        value = info.contact_no, 
                                        onValueChange = { newVal ->
                                            additionalInfoList = additionalInfoList.mapIndexed { i, item -> if (i == index) item.copy(contact_no = newVal) else item }
                                        }, 
                                        label = { Text("Secondary Contact No") }, 
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = info.address, 
                                        onValueChange = { newVal ->
                                            additionalInfoList = additionalInfoList.mapIndexed { i, item -> if (i == index) item.copy(address = newVal) else item }
                                        }, 
                                        label = { Text("Secondary Address") }, 
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val customer = TblCustomer(
                                customer_id = selectedCustomer?.customer_id ?: 0L,
                                customer_name = name,
                                contact_no = contact,
                                email_address = email,
                                address = address,
                                gst_no = gstNo,
                                is_active = 1L,
                                igst_status = false
                            )
                            viewModel.saveCustomer(customer, additionalInfoList)
                            showDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun CustomerItem(
    customer: TblCustomer,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    isGrid: Boolean = false
) {
    Card(
        modifier = (if (isGrid) Modifier.height(110.dp) else Modifier.fillMaxWidth()).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (isGrid) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = customer.customer_name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = customer.contact_no,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = customer.customer_name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = customer.contact_no, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                Row {
                    IconButton(onClick = onClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BluePrimary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerDialog(
    customer: TblCustomer?,
    onDismiss: () -> Unit,
    onConfirm: (TblCustomer) -> Unit
) {
    var name by remember { mutableStateOf(customer?.customer_name ?: "") }
    var phone by remember { mutableStateOf(customer?.contact_no ?: "") }
    var email by remember { mutableStateOf(customer?.email_address ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }
    var gst by remember { mutableStateOf(customer?.gst_no ?: "") }
    var igstStatus by remember { mutableStateOf(customer?.igst_status ?: false) }
    var isActive by remember { mutableStateOf(customer?.is_active ?: 1L) }

    val focusManager = LocalFocusManager.current
    val phoneFocus = remember { FocusRequester() }
    val emailFocus = remember { FocusRequester() }
    val addressFocus = remember { FocusRequester() }
    val gstFocus = remember { FocusRequester() }
    val igstFocus = remember { FocusRequester() }
    val statusFocus = remember { FocusRequester() }

    ReusableBottomSheet(
        onDismiss = onDismiss,
        title = if (customer == null) "Add Customer" else "Edit Customer",
        onSave = {
            onConfirm(
                TblCustomer(
                    customer_id = customer?.customer_id ?: 0,
                    customer_name = name,
                    contact_no = phone,
                    address = address,
                    email_address = email,
                    gst_no = customer?.gst_no ?: gst,
                    igst_status = customer?.igst_status ?: igstStatus,
                    is_active = customer?.is_active ?: isActive
                )
            )
        },
        isSaveEnabled = name.isNotBlank() && phone.isNotBlank(),
        buttonText = if (customer == null) "Add" else "Update"
    ) {
        Column {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.uppercase() },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Characters
                ),
                keyboardActions = KeyboardActions(onNext = { phoneFocus.requestFocus() })
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(phoneFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() })
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { addressFocus.requestFocus() })
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(addressFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { gstFocus.requestFocus() })
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = gst,
                onValueChange = { gst = it.uppercase() },
                label = { Text("GST Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(gstFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Characters
                ),
                keyboardActions = KeyboardActions(onNext = { igstFocus.requestFocus() })
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = if (igstStatus) "Yes" else "No",
                onValueChange = { igstStatus = it.equals("yes", true) },
                label = { Text("IGST Status (Yes/No)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(igstFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { statusFocus.requestFocus() })
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = if (isActive == 1L) "Active" else "Inactive",
                onValueChange = { isActive = if (it.equals("active", true)) 1L else 0L },
                label = { Text("Status (Active/Inactive)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(statusFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )
        }
    }
}
