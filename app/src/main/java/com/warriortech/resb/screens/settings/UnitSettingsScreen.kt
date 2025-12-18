package com.warriortech.resb.screens.settings

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
import com.warriortech.resb.model.TblUnit
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.setting.UnitSettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: UnitSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingUnit by remember { mutableStateOf<TblUnit?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadUnits()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unit Settings", color = SurfaceLight) },
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
                            Icons.Default.Add, contentDescription = "Add Unit",
                            tint = SurfaceLight
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = uiState) {
            is UnitSettingsViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is UnitSettingsViewModel.UiState.Success -> {
                if (state.units.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No units available", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.units) { unit ->
                            UnitCard(
                                unit = unit,
                                onEdit = {
                                    editingUnit = unit
                                    showAddDialog = true
                                },
                                onDelete = {
                                    scope.launch {
                                        viewModel.deleteUnit(unit.unit_id)
                                        snackbarHostState.showSnackbar("Unit deleted")
                                    }
                                }
                            )
                        }
                    }
                }
            }

            is UnitSettingsViewModel.UiState.Error -> {
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
                        onClick = { viewModel.loadUnits() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        if (showAddDialog || editingUnit != null) {
            UnitDialog(
                unit = editingUnit,
                onDismiss = {
                    showAddDialog = false
                    editingUnit = null
                },
                onSave = { unit ->
                    if (editingUnit == null) {
                        scope.launch {
                            viewModel.addUnit(unit)
                            snackbarHostState.showSnackbar("Unit added successfully")
                        }
                    } else {
                        scope.launch {
                            viewModel.updateUnit(unit)
                            snackbarHostState.showSnackbar("Unit updated successfully")
                        }
                    }
                    showAddDialog = false
                    editingUnit = null
                }
            )
        }
    }
}

@Composable
fun UnitCard(
    unit: TblUnit,
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
                    text = unit.unit_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (unit.is_active == 1L) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (unit.is_active == 1L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
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
fun UnitDialog(
    unit: TblUnit?,
    onDismiss: () -> Unit,
    onSave: (TblUnit) -> Unit
) {
    var unitName by remember { mutableStateOf(unit?.unit_name ?: "") }
    var isActive by remember { mutableStateOf(unit?.is_active == 1L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (unit != null) "Edit Unit" else "Add Unit") },
        text = {
            Column {
                OutlinedTextField(
                    value = unitName,
                    onValueChange = { unitName = it.uppercase() },
                    label = { Text("Unit Name") },
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
                    val newUnit = TblUnit(
                        unit_id = unit?.unit_id ?: 0,
                        unit_name = unitName,
                        is_active = if (isActive) 1L else 0L
                    )
                    onSave(newUnit)
                },
                enabled = unitName.isNotBlank()
            ) {
                Text(if (unit != null) "Update" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}