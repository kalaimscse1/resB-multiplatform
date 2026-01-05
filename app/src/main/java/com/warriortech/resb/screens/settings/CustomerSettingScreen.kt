package com.warriortech.resb.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.TblCustomer
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.setting.CustomerSettingsViewModel
import com.warriortech.resb.util.ReusableBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSettingsScreen(
    viewModel: CustomerSettingsViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<TblCustomer?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadCustomers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Settings", color = SurfaceLight) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.Default.ArrowBack, contentDescription = "Back",
                            tint = SurfaceLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen
                ),
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            Icons.Filled.Person, contentDescription = "Add Customer",
                            tint = SurfaceLight
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = uiState) {
            is CustomerSettingsViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is CustomerSettingsViewModel.UiState.Success -> {
                if (state.customers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No customers available",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    return@Scaffold
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.customers) { customer ->
                            CustomerCard(
                                customer = customer,
                                onEdit = { editingCustomer = it },
                                onDelete = {
                                    scope.launch {
                                        viewModel.deleteCustomer(it.customer_id)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            is CustomerSettingsViewModel.UiState.Error -> {

                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }


        if (showAddDialog) {
            CustomerDialog(
                customer = null,
                onDismiss = { showAddDialog = false },
                onConfirm = { customer ->
                    scope.launch {
                        viewModel.addCustomer(customer)
                        showAddDialog = false
                    }
                }
            )
        }

        editingCustomer?.let { customer ->
            CustomerDialog(
                customer = customer,
                onDismiss = { editingCustomer = null },
                onConfirm = { customer ->
                    scope.launch {
                        viewModel.updateCustomer(customer)
                        editingCustomer = null
                    }
                }
            )
        }
    }
}

@Composable
fun CustomerCard(
    customer: TblCustomer,
    onEdit: (TblCustomer) -> Unit,
    onDelete: (TblCustomer) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.customer_name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = customer.contact_no,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = customer.email_address.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = customer.address.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onEdit(customer) }) {
                Icon(Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = BluePrimary)
            }
            IconButton(onClick = { onDelete(customer) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
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
    var isActive by remember { mutableStateOf(customer?.is_active ?: 1) }

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
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = gst,
                onValueChange = { gst = it },
                label = { Text("GST Number") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = if (igstStatus) "Yes" else "No",
                onValueChange = { igstStatus = it.equals("yes", true) },
                label = { Text("IGST Status (Yes/No)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = if (isActive == 1L) "Active" else "Inactive",
                onValueChange = { isActive = if (it.equals("active", true)) 1 else 0 },
                label = { Text("Status (Active/Inactive)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text(if (customer == null) "Add Customer" else "Edit Customer") },
//        text = {
//            Column {
//                OutlinedTextField(
//                    value = name,
//                    onValueChange = { name = it },
//                    label = { Text("Name") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                OutlinedTextField(
//                    value = phone,
//                    onValueChange = { phone = it },
//                    label = { Text("Phone") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                OutlinedTextField(
//                    value = email,
//                    onValueChange = { email = it },
//                    label = { Text("Email") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                OutlinedTextField(
//                    value = address,
//                    onValueChange = { address = it },
//                    label = { Text("Address") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//        },
//        confirmButton = {
//            TextButton(
//                onClick = { onConfirm(name, phone, email, address) }
//            ) {
//                Text(if (customer == null) "Add" else "Update")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Cancel")
//            }
//        }
//    )
}