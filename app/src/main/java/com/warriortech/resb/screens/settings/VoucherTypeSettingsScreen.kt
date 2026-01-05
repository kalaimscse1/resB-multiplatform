package com.warriortech.resb.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.TblVoucherType
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.setting.VoucherTypeSettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherTypeSettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: VoucherTypeSettingsViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingVoucherType by remember { mutableStateOf<TblVoucherType?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadVoucherTypes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voucher Type Settings", color = SurfaceLight) },
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
                            Icons.Default.Add, contentDescription = "Add Voucher Type",
                            tint = SurfaceLight
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = uiState) {
            is VoucherTypeSettingsViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is VoucherTypeSettingsViewModel.UiState.Success -> {
                if (state.voucherTypes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No voucher types available",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.voucherTypes) { voucherType ->
                            VoucherTypeCard(
                                voucherType = voucherType,
                                onEdit = {
                                    editingVoucherType = voucherType
                                    showAddDialog = true
                                },
                                onDelete = {
                                    scope.launch {
                                        viewModel.deleteVoucherType(voucherType.voucher_Type_id)
                                        snackbarHostState.showSnackbar("Voucher type deleted")
                                    }
                                }
                            )
                        }
                    }
                }
            }

            is VoucherTypeSettingsViewModel.UiState.Error -> {
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
                        onClick = { viewModel.loadVoucherTypes() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        if (showAddDialog || editingVoucherType != null) {
            VoucherTypeDialog(
                voucherType = editingVoucherType,
                onDismiss = {
                    showAddDialog = false
                    editingVoucherType = null
                },
                onSave = { voucherType ->
                    if (editingVoucherType == null) {
                        scope.launch {
                            viewModel.addVoucherType(voucherType)
                            snackbarHostState.showSnackbar("Voucher type added successfully")
                        }
                    } else {
                        scope.launch {
                            viewModel.updateVoucherType(voucherType)
                            snackbarHostState.showSnackbar("Voucher type updated successfully")
                        }
                    }
                    showAddDialog = false
                    editingVoucherType = null
                }
            )
        }
    }
}

@Composable
fun VoucherTypeCard(
    voucherType: TblVoucherType,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    MobileOptimizedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Type: ${voucherType.voucher_type_name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (voucherType.is_active) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (voucherType.is_active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = BluePrimary)
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun VoucherTypeDialog(
    voucherType: TblVoucherType?,
    onDismiss: () -> Unit,
    onSave: (TblVoucherType) -> Unit
) {
    var voucherTypeName by remember { mutableStateOf(voucherType?.voucher_type_name ?: "") }
    var isActive by remember { mutableStateOf(voucherType?.is_active ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (voucherType != null) "Edit Voucher Type" else "Add Voucher Type") },
        text = {
            Column {
                OutlinedTextField(
                    value = voucherTypeName,
                    onValueChange = { voucherTypeName = it.uppercase() },
                    label = { Text("Voucher Type Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Active")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newVoucherType = TblVoucherType(
                        voucher_Type_id = voucherType?.voucher_Type_id ?: 0,
                        voucher_type_name = voucherTypeName,
                        is_active = isActive
                    )
                    onSave(newVoucherType)
                },
                enabled = voucherTypeName.isNotBlank()
            ) {
                Text(if (voucherType != null) "Update" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}