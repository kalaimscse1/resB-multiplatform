package com.warriortech.resb.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.TblUpiType
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.setting.UpiTypeViewModel
import com.warriortech.resb.util.getDeviceInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpiTypeSettingScreen(
    onBackPressed: () -> Unit,
    viewModel: UpiTypeViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val upiTypes by viewModel.upiTypes.collectAsStateWithLifecycle()
    
    var showDialog by remember { mutableStateOf(false) }
    var editingType by remember { mutableStateOf<TblUpiType?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val deviceInfo = getDeviceInfo()
    val isTabletLandscape = deviceInfo.isTablet && deviceInfo.isLandscape
    val showAdaptiveGrid = isTabletLandscape || deviceInfo.isLargeTablet

    LaunchedEffect(uiState) {
        if (uiState is UpiTypeViewModel.UiState.Error) {
            snackbarHostState.showSnackbar((uiState as UpiTypeViewModel.UiState.Error).message)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UPI Type Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        editingType = null
                        showDialog = true 
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add UPI Type", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(SurfaceLight)) {
            if (uiState is UpiTypeViewModel.UiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryGreen)
            } else {
                if (showAdaptiveGrid) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 200.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(upiTypes) { type ->
                            UpiTypeItem(
                                upiType = type,
                                onEdit = {
                                    editingType = type
                                    showDialog = true
                                },
                                onDelete = { viewModel.deleteUpiType(type.upi_type_id) },
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
                        items(upiTypes) { type ->
                            UpiTypeItem(
                                upiType = type,
                                onEdit = {
                                    editingType = type
                                    showDialog = true
                                },
                                onDelete = { viewModel.deleteUpiType(type.upi_type_id) }
                            )
                        }
                    }
                }
            }
        }

        if (showDialog || editingType != null) {
            UpiTypeDialog(
                upiType = editingType,
                onDismiss = {
                    showDialog = false
                    editingType = null
                },
                onSave = { name, active ->
                    viewModel.saveUpiType(
                        TblUpiType(
                            upi_type_id = editingType?.upi_type_id ?: 0L,
                            upi_type_name = name,
                            is_active = active
                        )
                    )
                    showDialog = false
                    editingType = null
                }
            )
        }
    }
}

@Composable
fun UpiTypeItem(
    upiType: TblUpiType,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isGrid: Boolean = false
) {
    Card(
        modifier = if (isGrid) Modifier.height(110.dp) else Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (isGrid) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = upiType.upi_type_name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (upiType.is_active) "Active" else "Inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (upiType.is_active) PrimaryGreen else Color.Red
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryGreen, modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(24.dp))
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = upiType.upi_type_name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (upiType.is_active) "Active" else "Inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (upiType.is_active) PrimaryGreen else Color.Red
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryGreen)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun UpiTypeDialog(
    upiType: TblUpiType?,
    onDismiss: () -> Unit,
    onSave: (String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(upiType?.upi_type_name ?: "") }
    var active by remember { mutableStateOf(upiType?.is_active ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (upiType == null) "Add UPI Type" else "Edit UPI Type") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.uppercase() },
                    label = { Text("UPI Type Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = active, onCheckedChange = { active = it })
                    Text("Active")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, active) },
                enabled = name.isNotBlank(),
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
