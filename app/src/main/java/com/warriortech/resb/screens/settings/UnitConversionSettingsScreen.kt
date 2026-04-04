package com.warriortech.resb.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.*
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.setting.UnitConversionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConversionSettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: UnitConversionViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val units by viewModel.units.collectAsStateWithLifecycle()
    val menuItems by viewModel.menuItems.collectAsStateWithLifecycle()
    val conversions by viewModel.conversions.collectAsStateWithLifecycle()

    var selectedUnit by remember { mutableStateOf<TblUnit?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var editingConversion by remember { mutableStateOf<TblUnitConversionResponse?>(null) }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(selectedUnit) {
        if (selectedUnit != null) {
            viewModel.loadConversions(selectedUnit!!.unit_id)
        } else {
            viewModel.loadAllActiveConversions()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unit Conversion Settings", color = SurfaceLight) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SurfaceLight)
                    }
                },
                actions = {
                    if (selectedUnit != null) {
                        IconButton(onClick = { 
                            editingConversion = null
                            showDialog = true 
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Conversion", tint = SurfaceLight)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SurfaceLight)
        ) {
            // Unit Selector with "All Active" option
            UnitSelector(
                units = units,
                selectedUnit = selectedUnit,
                onUnitSelected = { selectedUnit = it }
            )

            HorizontalDivider()

            when (uiState) {
                is UnitConversionViewModel.UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    if (conversions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                if (selectedUnit == null) "No active conversions found" 
                                else "No conversions found for ${selectedUnit!!.unit_name}"
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(conversions) { conversion ->
                                ConversionCard(
                                    conversion = conversion,
                                    showUnitName = selectedUnit == null,
                                    onEdit = {
                                        editingConversion = conversion
                                        showDialog = true
                                    },
                                    onDelete = {
                                        viewModel.deleteConversion(conversion.unit_conv_id, selectedUnit?.unit_id ?: 0L)
                                        scope.launch { snackbarHostState.showSnackbar("Deleted successfully") }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showDialog || editingConversion != null) {
            UnitConversionDialog(
                conversion = editingConversion,
                unitId = selectedUnit?.unit_id ?: editingConversion?.unit?.unit_id ?: 0L,
                menuItems = menuItems,
                onDismiss = {
                    showDialog = false
                    editingConversion = null
                },
                onSave = { request ->
                    viewModel.saveConversion(request)
                    showDialog = false
                    editingConversion = null
                    scope.launch { snackbarHostState.showSnackbar("Saved successfully") }
                }
            )
        }
    }
}

@Composable
fun UnitSelector(
    units: List<TblUnit>,
    selectedUnit: TblUnit?,
    onUnitSelected: (TblUnit?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth().clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedUnit?.unit_name ?: "All Active Conversions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            DropdownMenuItem(
                text = { Text("All Active Conversions") },
                onClick = {
                    onUnitSelected(null)
                    expanded = false
                }
            )
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.unit_name) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ConversionCard(
    conversion: TblUnitConversionResponse,
    showUnitName: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversion.item.menu_item_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (showUnitName) {
                    Text(
                        text = "Unit: ${conversion.unit.unit_name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryGreen
                    )
                }
                Text(
                    text = "Conversion: ${conversion.conversion_no}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BluePrimary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConversionDialog(
    conversion: TblUnitConversionResponse?,
    unitId: Long,
    menuItems: List<TblMenuItemResponse>,
    onDismiss: () -> Unit,
    onSave: (TblUnitConversionRequest) -> Unit
) {
    var selectedItem by remember { mutableStateOf(conversion?.item) }
    var conversionNo by remember { mutableStateOf(conversion?.conversion_no?.toString() ?: "") }
    var isActive by remember { mutableStateOf(conversion?.is_active ?: true) }
    
    var itemExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (conversion == null) "Add Conversion" else "Edit Conversion") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Item Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedItem?.menu_item_name ?: "Select Item",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Item") },
                        modifier = Modifier.fillMaxWidth().clickable { itemExpanded = true },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        enabled = false, // To make it clickable as a whole
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    // Actual overlay for click since disabled TextField might consume it
                    Box(modifier = Modifier.matchParentSize().clickable { itemExpanded = true })

                    DropdownMenu(
                        expanded = itemExpanded,
                        onDismissRequest = { itemExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.7f).heightIn(max = 300.dp)
                    ) {
                        menuItems.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.menu_item_name) },
                                onClick = {
                                    selectedItem = item
                                    itemExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = conversionNo,
                    onValueChange = { if (it.all { char -> char.isDigit() }) conversionNo = it },
                    label = { Text("Conversion Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isActive, onCheckedChange = { isActive = it })
                    Text("Active")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedItem?.let {
                        onSave(
                            TblUnitConversionRequest(
                                unit_conv_id = conversion?.unit_conv_id ?: 0L,
                                unit_id = unitId,
                                item_id = it.menu_item_id,
                                conversion_no = conversionNo.toLongOrNull() ?: 0L,
                                is_active = isActive
                            )
                        )
                    }
                },
                enabled = selectedItem != null && conversionNo.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
