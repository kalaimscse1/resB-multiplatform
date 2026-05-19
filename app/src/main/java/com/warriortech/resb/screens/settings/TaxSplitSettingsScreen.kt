package com.warriortech.resb.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.warriortech.resb.util.getDeviceInfo
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
    var taxSplitToDelete by remember { mutableStateOf<TblTaxSplit?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val tax = viewModel.taxes.collectAsStateWithLifecycle()

    val deviceInfo = getDeviceInfo()
    val isTabletLandscape = deviceInfo.isTablet && deviceInfo.isLandscape
    val showAdaptiveGrid = isTabletLandscape || deviceInfo.isLargeTablet

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    if (showAdaptiveGrid) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 250.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.taxSplits) { item ->
                                TaxSplitItem(
                                    taxSplit = item,
                                    onEdit = {
                                        editingTaxSplit = item
                                        showAddDialog = true
                                    },
                                    onDelete = {
                                        taxSplitToDelete = item
                                    },
                                    isGrid = true
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.taxSplits) { item ->
                                TaxSplitItem(
                                    taxSplit = item,
                                    onEdit = {
                                        editingTaxSplit = item
                                        showAddDialog = true
                                    },
                                    onDelete = {
                                        taxSplitToDelete = item
                                    }
                                )
                            }
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
                taxes = tax.value
            )
        }

        taxSplitToDelete?.let { item ->
            AlertDialog(
                onDismissRequest = { taxSplitToDelete = null },
                title = { Text("Confirm Delete") },
                text = { Text("Are you sure you want to delete the tax split '${item.tax_split_name}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.deleteTaxSplit(item.tax_split_id)
                                snackbarHostState.showSnackbar("Tax Split deleted")
                            }
                            taxSplitToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { taxSplitToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun TaxSplitItem(
    taxSplit: TblTaxSplit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isGrid: Boolean = false
) {
    MobileOptimizedCard(
        modifier = if (isGrid) Modifier.height(110.dp) else Modifier.fillMaxWidth()
    ) {
        if (isGrid) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = taxSplit.tax_name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${taxSplit.tax_split_name}: ${taxSplit.tax_split_percentage}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(44.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = BluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(44.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        } else {
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
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = BluePrimary,
                            modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(44.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
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
