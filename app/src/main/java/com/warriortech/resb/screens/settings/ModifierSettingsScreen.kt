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
import com.warriortech.resb.model.Modifiers
import com.warriortech.resb.model.MenuCategory
import com.warriortech.resb.ui.viewmodel.setting.ModifierSettingsViewModel
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.SuccessDialogWithButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifierSettingsScreen(
    viewModel: ModifierSettingsViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingModifier by remember { mutableStateOf<Modifiers?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    var sucess by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadModifiers()
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage!= null) {
            if (errorMessage=="AddOn deleted successfully") {
                sucess = true
            } else {
                failed = true
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AddOn Settings", color = SurfaceLight) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.Default.ArrowBack, contentDescription = "Back",
                            tint = SurfaceLight
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showAddDialog = true
                        viewModel.loadCategories()
                    }) {
                        Icon(
                            Icons.Default.Add, contentDescription = "Add AddOn",
                            tint = SurfaceLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        when (val state = uiState) {
            is ModifierSettingsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ModifierSettingsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.modifiers) { modifier ->
                        ModifierCard(
                            modifier = modifier,
                            categories = categories,
                            onEdit = {
                                editingModifier = modifier
                                viewModel.loadCategories()
                            },
                            onDelete = {
                                scope.launch {
                                    viewModel.deleteModifier(modifier.add_on_id)
                                }
                            }
                        )
                    }
                }
            }

            is ModifierSettingsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (showAddDialog) {
            ModifierDialog(
                modifier = null,
                categories = categories,
                onSave = { modifier ->
                    scope.launch {
                        viewModel.addModifier(modifier)
                        showAddDialog = false
                        snackbarHostState.showSnackbar("AddOn added")
                    }
                },
                onDismiss = { showAddDialog = false }
            )
        }

        editingModifier?.let { modifier ->
            ModifierDialog(
                modifier = modifier,
                categories = categories,
                onSave = { updatedModifier ->
                    scope.launch {
                        viewModel.updateModifier(updatedModifier)
                        editingModifier = null
                        snackbarHostState.showSnackbar("AddOn updated")
                    }
                },
                onDismiss = { editingModifier = null }
            )
        }

        if (sucess) {
            SuccessDialogWithButton(
                title = "Success",
                description = errorMessage.toString(),
                paddingValues = paddingValues,
                onClick = {
                    sucess = false
                    viewModel.loadModifiers()
                    viewModel.clearErrorMessage()
                }
            )
        }

        if (failed){
            SuccessDialogWithButton(
                title = "Failure",
                description = errorMessage.toString(),
                paddingValues = paddingValues,
                onClick = {
                    failed = false
                    viewModel.loadModifiers()
                    viewModel.clearErrorMessage()
                }
            )
        }
    }
}

@Composable
fun ModifierCard(
    modifier: Modifiers,
    categories: List<MenuCategory>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryName =
        categories.find { it.item_cat_id == modifier.item_cat_id }?.item_cat_name ?: "Unknown"

    MobileOptimizedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = modifier.add_on_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Category: $categoryName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Price: ${CurrencySettings.format(modifier.add_on_price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (modifier.is_active) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (modifier.is_active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifierDialog(
    modifier: Modifiers?,
    categories: List<MenuCategory>,
    onSave: (Modifiers) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(modifier?.add_on_name ?: "") }
    var price by remember { mutableStateOf(modifier?.add_on_price?.toString() ?: "") }
    var selectedCategoryId by remember { mutableStateOf(modifier?.item_cat_id ?: 0L) }
    var isActive by remember { mutableStateOf(modifier?.is_active ?: true) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (modifier == null) "Add AddOn" else "Edit AddOn") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.uppercase() },
                    label = { Text("AddOn Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.item_cat_id == selectedCategoryId }?.item_cat_name
                            ?: "Select Category",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.item_cat_name) },
                                onClick = {
                                    selectedCategoryId = category.item_cat_id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                    Text("Active")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newModifier = Modifiers(
                        add_on_id = modifier?.add_on_id ?: 0,
                        item_cat_id = selectedCategoryId,
                        add_on_name = name,
                        add_on_price = price.toDoubleOrNull() ?: 0.0,
                        is_active = isActive
                    )
                    onSave(newModifier)
                },
                enabled = name.isNotBlank() && price.isNotBlank() && selectedCategoryId != 0L
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

sealed class ModifierSettingsUiState {
    object Loading : ModifierSettingsUiState()
    data class Success(val modifiers: List<Modifiers>) : ModifierSettingsUiState()
    data class Error(val message: String) : ModifierSettingsUiState()
}
