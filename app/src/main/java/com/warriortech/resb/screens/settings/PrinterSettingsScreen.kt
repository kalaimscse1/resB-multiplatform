package com.warriortech.resb.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.R
import com.warriortech.resb.model.KitchenCategory
import com.warriortech.resb.model.Printer
import com.warriortech.resb.model.TblPrinterResponse
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.setting.PrinterSettingsViewModel
import com.warriortech.resb.util.KitchenGroupDropdown
import com.warriortech.resb.util.getDeviceInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterSettingsScreen(
    viewModel: PrinterSettingsViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    val kitchenCategories by viewModel.kitchenCategories.collectAsStateWithLifecycle()
    var editingPrinter by remember { mutableStateOf<TblPrinterResponse?>(null) }
    var printerToDelete by remember { mutableStateOf<TblPrinterResponse?>(null) }
    val printerType by viewModel.printerType.collectAsStateWithLifecycle()
    val paperWidth by viewModel.paperWidth.collectAsStateWithLifecycle()
    
    val deviceInfo = getDeviceInfo()
    val isTabletLandscape = deviceInfo.isTablet && deviceInfo.isLandscape
    val showAdaptiveGrid = isTabletLandscape || deviceInfo.isLargeTablet

    LaunchedEffect(Unit) {
        viewModel.loadPrinters()
    }
    BackHandler {
        navController.navigate("settings") {
            popUpTo("settings") { inclusive = true }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.printer_settings),
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

                    IconButton(onClick = { navController.navigate("bluetooth") }) {
                        Icon(
                            Icons.Default.Bluetooth,
                            contentDescription = "Bluetooth Printers",
                            tint = SurfaceLight
                        )
                    }

                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_printer),
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
            // Printer Type Selection
            MobileOptimizedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Printer Connection Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = printerType == "BT",
                                onClick = { viewModel.savePrinterType("BT") }
                            )
                            Text(
                                text = "BT",
                                modifier = Modifier.clickable { viewModel.savePrinterType("BT") }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = printerType == "TCP",
                                onClick = { viewModel.savePrinterType("TCP") }
                            )
                            Text(
                                text = "TCP",
                                modifier = Modifier.clickable { viewModel.savePrinterType("TCP") }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = printerType == "InBuilt",
                                onClick = { viewModel.savePrinterType("InBuilt") }
                            )
                            Text(
                                text = "InBuilt",
                                modifier = Modifier.clickable { viewModel.savePrinterType("InBuilt") }
                            )
                        }
                    }
                }
            }

            // Paper Width Selection
            MobileOptimizedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Paper Size",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = paperWidth == 58,
                                onClick = { viewModel.savePaperWidth(58) }
                            )
                            Text(
                                text = "2 inch (58mm)",
                                modifier = Modifier.clickable { viewModel.savePaperWidth(58) }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = paperWidth == 80,
                                onClick = { viewModel.savePaperWidth(80) }
                            )
                            Text(
                                text = "3 inch (80mm)",
                                modifier = Modifier.clickable { viewModel.savePaperWidth(80) }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            when (val state = uiState) {
                is PrinterSettingsViewModel.PrinterSettingsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is PrinterSettingsViewModel.PrinterSettingsUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error: ${state.message}")
                    }
                }

                is PrinterSettingsViewModel.PrinterSettingsUiState.Success -> {
                    if (state.printers.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No Printer available", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        if (showAdaptiveGrid) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 250.dp),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.printers) { printer ->
                                    PrinterItem(
                                        printer = printer,
                                        onEdit = { editingPrinter = printer },
                                        onDelete = { printerToDelete = printer },
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
                                items(state.printers) { printer ->
                                    PrinterItem(
                                        printer = printer,
                                        onEdit = { editingPrinter = printer },
                                        onDelete = { printerToDelete = printer }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        PrinterDialog(
            printer = null,
            kitchenCategories = kitchenCategories,
            onDismiss = { showAddDialog = false },
            onSave = { printer ->
                viewModel.addPrinter(printer)
                showAddDialog = false
            }
        )
    }

    editingPrinter?.let { printer ->
        PrinterDialog(
            printer = printer,
            kitchenCategories = kitchenCategories,
            onDismiss = { editingPrinter = null },
            onSave = { updatedPrinter ->
                viewModel.updatePrinter(updatedPrinter)
                editingPrinter = null
            }
        )
    }

    printerToDelete?.let { printer ->
        AlertDialog(
            onDismissRequest = { printerToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete the printer '${printer.printer_name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePrinter(printer.printer_id)
                        printerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { printerToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PrinterItem(
    printer: TblPrinterResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isGrid: Boolean = false
) {
    MobileOptimizedCard(
        modifier = if (isGrid) Modifier.height(130.dp) else Modifier.fillMaxWidth()
    ) {
        if (isGrid) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = printer.printer_name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = printer.kitchen_cat.kitchen_cat_name,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = printer.ip_address,
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = printer.printer_name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = printer.kitchen_cat.kitchen_cat_name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = printer.ip_address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterDialog(
    printer: TblPrinterResponse?,
    kitchenCategories: List<KitchenCategory>,
    onDismiss: () -> Unit,
    onSave: (Printer) -> Unit
) {
    var name by remember { mutableStateOf(printer?.printer_name ?: "") }
    var ipAddress by remember { mutableStateOf(printer?.ip_address ?: "") }
    var kitchenCatId by remember { mutableStateOf(printer?.kitchen_cat?.kitchen_cat_id ?: 1) }
    var isActive by remember { mutableStateOf(printer?.is_active ?: 1) }
    
    val focusManager = LocalFocusManager.current
    val nameFocus = remember { FocusRequester() }
    val ipFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        nameFocus.requestFocus()
    }

    var ipError by remember { mutableStateOf<String?>(null) }
    val ipRegex = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$".toRegex()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (printer == null) stringResource(R.string.add_printer) else stringResource(
                    R.string.edit_printer
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.printer)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nameFocus),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { ipFocus.requestFocus() })
                )
                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { 
                        ipAddress = it
                        ipError = if (!ipRegex.matches(it) && it.isNotBlank()) "Invalid IP Address" else null
                    },
                    label = { Text(stringResource(R.string.ip_address)) },
                    isError = ipError != null,
                    supportingText = { ipError?.let { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(ipFocus),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                KitchenGroupDropdown(
                    menus = kitchenCategories,
                    onKitchenCategorySelected = { kitchenCatId = it.kitchen_cat_id },
                    label = "Select Kitchen Category",
                    modifier = Modifier.fillMaxWidth(),
                    selectedKitchenCategory = kitchenCategories.find { it.kitchen_cat_id == kitchenCatId }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = isActive.toInt() == 1,
                        onCheckedChange = { isActive = if (it) 1 else 0 }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Active")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newPrinter = Printer(
                        printer_id = printer?.printer_id ?: 0, // Use 0 for new printers
                        printer_name = name,
                        kitchen_cat_id = kitchenCatId, // Default or selected category ID
                        ip_address = ipAddress,
                        is_active = isActive
                    )
                    onSave(newPrinter)
                },
                enabled = name.isNotBlank() && ipAddress.isNotBlank() && kitchenCatId.toInt() != 0
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
