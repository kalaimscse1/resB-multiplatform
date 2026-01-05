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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.R
import com.warriortech.resb.model.Tax
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.setting.TaxSettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxSettingsScreen(
    viewModel: TaxSettingsViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTax by remember { mutableStateOf<Tax?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.loadTaxes()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.tax_settings),
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
                ),
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            Icons.Default.Add, contentDescription = "Add Tax",
                            tint = SurfaceLight
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is TaxSettingsViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is TaxSettingsViewModel.UiState.Success -> {
                if (state.taxes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Taxes available", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.taxes) { tax ->
                            TaxItem(
                                tax = tax,
                                onEdit = {
                                    editingTax = tax
                                    showAddDialog = true
                                },
                                onDelete = {
                                    scope.launch {
                                        viewModel.deleteTax(tax.tax_id)
                                        snackbarHostState.showSnackbar("Tax deleted")
                                    }
                                }
                            )
                        }
                    }
                }
            }

            is TaxSettingsViewModel.UiState.Error -> {
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
                        onClick = { viewModel.loadTaxes() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        if (showAddDialog || editingTax != null) {
            TaxDialog(
                tax = editingTax,
                onDismiss = {
                    showAddDialog = false
                    editingTax = null
                },
                onSave = { tax ->
                    if (editingTax == null) {
                        viewModel.addTax(tax)
                    } else {
                        viewModel.updateTax(tax)
                    }
                    showAddDialog = false
                    editingTax = null
                }
            )
        }
    }
}

@Composable
fun TaxItem(
    tax: Tax,
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
                    text = tax.tax_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Tax % : ${tax.tax_percentage} ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun TaxDialog(
    tax: Tax?,
    onDismiss: () -> Unit,
    onSave: (Tax) -> Unit
) {
    var taxName by remember { mutableStateOf(tax?.tax_name ?: "") }
    var taxPercentage by remember { mutableStateOf(tax?.tax_percentage?.toString() ?: "") }
    var cessPercentage by remember { mutableStateOf(tax?.cess_percentage?.toString() ?: "") }
    var isActive by remember { mutableStateOf(tax?.is_active ?: true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (tax != null) "Edit Tax" else "Add Tax") },
        text = {
            Column {
                OutlinedTextField(
                    value = taxName,
                    onValueChange = { taxName = it },
                    label = { Text("Tax Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = taxPercentage,
                    onValueChange = { taxPercentage = it },
                    label = { Text("Tax Percentage") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = cessPercentage,
                    onValueChange = { cessPercentage = it },
                    label = { Text("Cess Percentage") },
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
                    val newTable = Tax(
                        tax_id = tax?.tax_id ?: 0,
                        tax_name = tax?.tax_name ?: taxName,
                        tax_percentage = tax?.tax_percentage ?: taxPercentage.toDoubleOrNull()
                        ?: 0.0,
                        cess_percentage = tax?.cess_percentage ?: cessPercentage.toDoubleOrNull()
                        ?: 0.0,
                        is_active = tax?.is_active ?: isActive
                    )
                    onSave(newTable)
                },
                enabled = taxName.isNotBlank() && taxPercentage.isNotBlank() && cessPercentage.isNotBlank() &&
                        taxPercentage.toDoubleOrNull() != null && cessPercentage.toDoubleOrNull() != null
            ) {
                Text(if (tax != null) "Update" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}