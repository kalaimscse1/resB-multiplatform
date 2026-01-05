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
import com.warriortech.resb.model.TaxSplit
import com.warriortech.resb.model.TblTaxSplit
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.setting.TaxSplitSettingsViewModel
import com.warriortech.resb.util.TaxDropdown
import kotlinx.coroutines.launch
import kotlin.collections.find

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxSplitSettingsScreen(
    viewModel: TaxSplitSettingsViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTaxSplit by remember { mutableStateOf<TblTaxSplit?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val tax by viewModel.taxes.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadTaxSplits()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.tax_split_settings),
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
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_tax_split),
                            tint = SurfaceLight
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is TaxSplitSettingsViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is TaxSplitSettingsViewModel.UiState.Success -> {
                if (state.taxSplits.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Tax Splits available", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.taxSplits) { table ->
                            TaxSplitItem(
                                taxSplit = table,
                                onEdit = {

                                    showAddDialog = true
                                },
                                onDelete = {
                                    scope.launch {
                                        viewModel.deleteTaxSplit(table.tax_split_id)
                                        snackbarHostState.showSnackbar("Tax Split deleted")
                                    }
                                }
                            )
                        }
                    }
                }
            }

            is TaxSplitSettingsViewModel.UiState.Error -> {
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
                        onClick = { viewModel.loadTaxSplits() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        if (showAddDialog || editingTaxSplit != null) {
            TaxSplitDialog(
                taxSplit = editingTaxSplit,
                onDismiss = {
                    showAddDialog = false
                    editingTaxSplit = null
                },
                onSave = { taxSplit ->
                    if (editingTaxSplit == null) {
                        scope.launch {
                            viewModel.addTaxSplit(taxSplit)
                            snackbarHostState.showSnackbar("Tax Split added")
                        }
                    } else {
                        scope.launch {
                            viewModel.updateTaxSplit(taxSplit)
                            snackbarHostState.showSnackbar("Tax Split updated")
                        }
                    }
                    showAddDialog = false
                    editingTaxSplit = null
                },
                taxes = tax
            )
        }
    }
}

@Composable
fun TaxSplitItem(
    taxSplit: TblTaxSplit,
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
                    text = "TaxName : ${taxSplit.tax_name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "TaxSplitName: ${taxSplit.tax_split_name} | TaxSplit %: ${taxSplit.tax_split_percentage}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
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
fun TaxSplitDialog(
    taxSplit: TblTaxSplit?,
    onDismiss: () -> Unit,
    onSave: (TaxSplit) -> Unit,
    taxes: List<Tax>
) {
    var taxId by remember { mutableLongStateOf(taxSplit?.tax_id ?: 1) }
    var taxSplitName by remember { mutableStateOf(taxSplit?.tax_split_name ?: "") }
    var taxSplitPercentage by remember { mutableStateOf(taxSplit?.tax_split_percentage ?: "") }
    var isActive by remember { mutableStateOf(taxSplit?.is_active ?: true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (taxSplit != null) "Edit TaxSplit" else "Add TaxSplit") },
        text = {
            Column {
                OutlinedTextField(
                    value = taxSplitName,
                    onValueChange = { taxSplitName = it },
                    label = { Text("TaxSplit Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TaxDropdown(
                    taxes = taxes,
                    selectedTax = taxes.find { it.tax_id == taxId },
                    onTaxSelected = { tax ->
                        taxId = tax.tax_id
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = "Select Tax"
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = taxSplitPercentage,
                    onValueChange = { taxSplitPercentage = it },
                    label = { Text("Tax Split Percentage") },
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
                    val newTable = TaxSplit(
                        tax_split_id = taxSplit?.tax_split_id ?: 0,
                        tax_id = taxSplit?.tax_id ?: taxId,
                        tax_split_name = taxSplit?.tax_split_name ?: taxSplitName,
                        tax_split_percentage = taxSplit?.tax_split_percentage ?: taxSplitPercentage,
                        is_active = taxSplit?.is_active ?: isActive
                    )
                    onSave(newTable)
                },
                enabled = taxSplitName.isNotBlank() && taxSplitPercentage.isNotBlank()
            ) {
                Text(if (taxSplit != null) "Update" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
