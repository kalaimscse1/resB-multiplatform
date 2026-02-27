package com.warriortech.resb.screens.settings

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
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
import com.warriortech.resb.ui.viewmodel.master.TableSettingsViewModel
import com.warriortech.resb.util.AreaDropdown
import com.warriortech.resb.util.StringDropdown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    var tableToDelete by remember { mutableStateOf<Table?>(null) }
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
                val tablesCount = when (val state = uiState) {
                    is TableSettingsUiState.Success -> state.tables.size
                    else -> 0
                }
                Text(
                    text = "Total: $tablesCount",
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
                                    tableToDelete = table
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
                    } else {
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

        tableToDelete?.let { table ->
            AlertDialog(
                onDismissRequest = { tableToDelete = null },
                title = { Text("Confirm Delete") },
                text = { Text("Are you sure you want to delete table '${table.table_name}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.deleteTable(table.table_id)
                            }
                            tableToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { tableToDelete = null }) {
                        Text("Cancel")
                    }
                }
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
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = BluePrimary
                    )
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

@SuppressLint("AutoboxingStateCreation")
@Composable
fun TableDialog(
    table: Table?,
    onDismiss: () -> Unit,
    onSave: (TblTable) -> Unit,
    areas: List<Area>
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Centralized Focus Requesters
    val focusRequesters = remember {
        mapOf(
            "name" to FocusRequester(),
            "capacity" to FocusRequester()
        )
    }

    val acOptions = listOf("AC", "NON-AC")
    val tableStatusOptions = listOf("ACTIVE", "INACTIVE")
    
    var tableNumber by remember { mutableStateOf(table?.table_name ?: "") }
    var capacity by remember { mutableStateOf(table?.seating_capacity?.toString() ?: "1") }
    var areaId: Long by remember { mutableLongStateOf(table?.area_id ?: 1) }
    var isAc by remember { mutableStateOf(table?.is_ac ?: acOptions.first()) }
    var tableStatus by remember {
        mutableStateOf(
            table?.table_status ?: tableStatusOptions.first()
        )
    }
    var isActive by remember { mutableStateOf(table?.is_active ?: true) }

    // Validation States
    var nameError by remember { mutableStateOf<String?>(null) }
    var capacityError by remember { mutableStateOf<String?>(null) }

    fun validate(): String? {
        if (tableNumber.isBlank()) {
            nameError = "Table Name is required"
            return "name"
        } else nameError = null

        if (capacity.isBlank() || capacity.toIntOrNull() == null) {
            capacityError = "Valid Capacity is required"
            return "capacity"
        } else capacityError = null

        return null
    }

    LaunchedEffect(Unit) {
        focusRequesters["name"]?.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (table != null) "Edit Table" else "Add Table") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormTextField(
                    value = tableNumber,
                    onValueChange = { tableNumber = it.uppercase() },
                    label = "Table Name *",
                    focusRequester = focusRequesters["name"]!!,
                    nextFocusRequester = focusRequesters["capacity"],
                    isError = nameError != null,
                    errorMessage = nameError,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    scrollState = scrollState,
                    scope = scope
                )

                AreaDropdown(
                    areas = areas,
                    modifier = Modifier.fillMaxWidth(),
                    label = "Select Area",
                    selectedArea = areas.find { it.area_id.toInt().toLong() == areaId },
                    onAreaSelected = { area ->
                        areaId = area.area_id
                    }
                )

                FormTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = "Capacity *",
                    focusRequester = focusRequesters["capacity"]!!,
                    isError = capacityError != null,
                    errorMessage = capacityError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    onImeAction = {
                        val errorField = validate()
                        if (errorField == null) {
                            val newTable = TblTable(
                                table_id = table?.table_id ?: 0L,
                                area_id = areaId,
                                table_name = tableNumber,
                                seating_capacity = capacity.toInt(),
                                is_ac = isAc,
                                table_status = tableStatus,
                                table_availability = "AVAILABLE",
                                is_open = false,
                                is_active = isActive
                            )
                            onSave(newTable)
                        } else {
                            focusRequesters[errorField]?.requestFocus()
                        }
                    },
                    scrollState = scrollState,
                    scope = scope
                )

                StringDropdown(
                    options = acOptions,
                    selectedOption = isAc,
                    onOptionSelected = { isAc = it },
                    label = "AC Status",
                    modifier = Modifier.fillMaxWidth()
                )

                StringDropdown(
                    options = tableStatusOptions,
                    selectedOption = tableStatus,
                    onOptionSelected = { tableStatus = it },
                    label = "Table Status",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val errorField = validate()
                    if (errorField == null) {
                        val newTable = TblTable(
                            table_id = table?.table_id ?: 0L,
                            area_id = areaId,
                            table_name = tableNumber,
                            seating_capacity = capacity.toInt(),
                            is_ac = isAc,
                            table_status = tableStatus,
                            table_availability = "AVAILABLE",
                            is_open = false,
                            is_active = isActive
                        )
                        onSave(newTable)
                    } else {
                        focusRequesters[errorField]?.requestFocus()
                    }
                }
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
