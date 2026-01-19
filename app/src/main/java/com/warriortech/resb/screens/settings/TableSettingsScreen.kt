package com.warriortech.resb.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.Area
import com.warriortech.resb.model.Table
import com.warriortech.resb.model.TblTable
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.master.MenuSettingsViewModel
import com.warriortech.resb.ui.viewmodel.master.TableSettingsViewModel
import kotlinx.coroutines.launch
import com.warriortech.resb.util.AreaDropdown
import com.warriortech.resb.util.StringDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableSettingsScreen(
    viewModel: TableSettingsViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    drawerState: DrawerState,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTable by remember { mutableStateOf<Table?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val areas by viewModel.areas.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadTables()
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage ?: "An error occurred")
        }
    }
    BackHandler {
        navController.navigate("dashboard") {
            popUpTo("dashboard") { inclusive = true }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Table", color = SurfaceLight) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
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
                            Icons.Default.Add, contentDescription = "Add Table",
                            tint = SurfaceLight
                        )
                    }
                },

                )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomAppBar(
                containerColor = PrimaryGreen,
                contentColor = SurfaceLight,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val menuItems = when (val state = uiState) {
                    is TableSettingsUiState.Success -> state.tables
                    else -> emptyList()
                }
                Text(
                    text = "Total: ${menuItems.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is TableSettingsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is TableSettingsUiState.Success -> {
                if (state.tables.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No tables available", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.tables) { table ->
                            TableItem(
                                table = table,
                                onEdit = { editingTable = table },
                                onDelete = {
                                    scope.launch {
                                        viewModel.deleteTable(table.table_id)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            is TableSettingsUiState.Error -> {
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
                        onClick = { viewModel.loadTables() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
        if (showAddDialog) {
            TableDialog(
                table = null,
                onDismiss = { showAddDialog = false },
                onSave = { table ->
                    if (table.area_id.toInt() == 1 || table.area_id.toInt() == 0) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please select a valid area")
                        }
                        return@TableDialog
                    }else{
                        scope.launch {
                            viewModel.addTable(table)
                            showAddDialog = false
                        }
                    }
                },
                areas = areas
            )
        }

        editingTable?.let {
            TableDialog(
                table = it,
                onDismiss = { editingTable = null },
                onSave = { table ->
                    scope.launch {
                        viewModel.updateTable(table)
                        editingTable = null
                    }
                },
                areas = areas
            )
        }
    }
}

@Composable
fun TableItem(
    table: Table,
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
                    text = table.table_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Capacity: ${table.seating_capacity} | Status: ${table.table_availability}",
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
fun TableDialog(
    table: Table?,
    onDismiss: () -> Unit,
    onSave: (TblTable) -> Unit,
    areas: List<Area>
) {
    val acOptions = listOf("AC", "NON-AC")
    val tableStatusOptions = listOf("ACTIVE", "INACTIVE")
    var tableNumber by remember { mutableStateOf(table?.table_name ?: "") }
    var capacity by remember { mutableStateOf(table?.seating_capacity?.toString() ?: "1") }
    var areaId by remember { mutableStateOf(table?.area_id ?: 1) }
    var isAc by remember { mutableStateOf(table?.is_ac ?: acOptions.firstOrNull()) }
    var tableStatus by remember {
        mutableStateOf(
            table?.table_status ?: tableStatusOptions.firstOrNull()
        )
    }
    var isActive by remember { mutableStateOf(table?.is_active ?: true) }
    val focusManager = LocalFocusManager.current
    val nameFocus = remember { FocusRequester() }
    val capacityFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        nameFocus.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (table != null) "Edit Table" else "Add Table") },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                OutlinedTextField(
                    value = tableNumber,
                    onValueChange = { tableNumber = it.uppercase() },
                    label = { Text("Table Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nameFocus),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { capacityFocus.requestFocus() }
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                AreaDropdown(
                    areas = areas,
                    modifier = Modifier.fillMaxWidth(),
                    label = "Select Area",
                    selectedArea = areas.find { it.area_id == areaId },
                    onAreaSelected = { area ->
                        areaId = area.area_id
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("Capacity") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(capacityFocus),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                StringDropdown(
                    options = acOptions,
                    selectedOption = isAc,
                    onOptionSelected = { selectedStatus ->
                        isAc = selectedStatus // Update your status state
                    },
                    label = "AC Status",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                StringDropdown(
                    options = tableStatusOptions,
                    selectedOption = tableStatus,
                    onOptionSelected = { selectedStatus ->
                        tableStatus = selectedStatus // Update your status state
                    },
                    label = "Table Status",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newTable = TblTable(
                        table_id = table?.table_id ?: 0L,
                        area_id = areaId,
                        table_name = tableNumber,
                        seating_capacity =  capacity.toInt(),
                        is_ac =  isAc.toString(),
                        table_status =  tableStatus.toString(),
                        table_availability = "AVAILABLE",
                        is_active =  isActive
                    )
                    onSave(newTable)
                },
                enabled = tableNumber.isNotBlank() && capacity.isNotBlank() && capacity.toIntOrNull() != null
            ) {
                Text(if (table != null) "Update" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


sealed class TableSettingsUiState {
    object Loading : TableSettingsUiState()
    data class Success(val tables: List<Table>) : TableSettingsUiState()
    data class Error(val message: String) : TableSettingsUiState()
}
