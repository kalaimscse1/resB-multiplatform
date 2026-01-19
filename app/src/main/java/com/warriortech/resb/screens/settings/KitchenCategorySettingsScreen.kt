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
import com.warriortech.resb.model.KitchenCategory
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.setting.KitchenCategorySettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenCategorySettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: KitchenCategorySettingsViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingKitchenCategory by remember { mutableStateOf<KitchenCategory?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadKitchenCategories()
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kitchen Category Settings", color = SurfaceLight) },
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
                            Icons.Default.Add, contentDescription = "Add Kitchen Category",
                            tint = SurfaceLight
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = uiState) {
            is KitchenCategorySettingsViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is KitchenCategorySettingsViewModel.UiState.Success -> {
                if (state.kitchenCategories.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No kitchen categories available",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.kitchenCategories) { kitchenCategory ->
                            KitchenCategoryCard(
                                kitchenCategory = kitchenCategory,
                                onEdit = {
                                    editingKitchenCategory = kitchenCategory
                                    showAddDialog = true
                                },
                                onDelete = {
                                    scope.launch {
                                        viewModel.deleteKitchenCategory(kitchenCategory.kitchen_cat_id)
                                        snackbarHostState.showSnackbar("Kitchen category deleted")
                                    }
                                }
                            )
                        }
                    }
                }
            }

            is KitchenCategorySettingsViewModel.UiState.Error -> {
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
                        onClick = { viewModel.loadKitchenCategories() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        if (showAddDialog || editingKitchenCategory != null) {
            KitchenCategoryDialog(
                kitchenCategory = editingKitchenCategory,
                onDismiss = {
                    showAddDialog = false
                    editingKitchenCategory = null
                },
                onSave = { kitchenCategory ->
                    if (editingKitchenCategory == null) {
                        scope.launch {
                            viewModel.addKitchenCategory(kitchenCategory)
                            snackbarHostState.showSnackbar("Kitchen category added successfully")
                        }
                    } else {
                        scope.launch {
                            viewModel.updateKitchenCategory(kitchenCategory)
                            snackbarHostState.showSnackbar("Kitchen category updated successfully")
                        }
                    }
                    showAddDialog = false
                    editingKitchenCategory = null
                }
            )
        }
    }
}

@Composable
fun KitchenCategoryCard(
    kitchenCategory: KitchenCategory,
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
                    text = kitchenCategory.kitchen_cat_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (kitchenCategory.is_active == 1L) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (kitchenCategory.is_active == 1L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
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
fun KitchenCategoryDialog(
    kitchenCategory: KitchenCategory?,
    onDismiss: () -> Unit,
    onSave: (KitchenCategory) -> Unit
) {
    var categoryName by remember { mutableStateOf(kitchenCategory?.kitchen_cat_name ?: "") }
    var isActive by remember { mutableStateOf(kitchenCategory?.is_active == 1L) }
    val focusManager = LocalFocusManager.current
    val nameFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        nameFocus.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (kitchenCategory != null) "Edit Kitchen Category" else "Add Kitchen Category") },
        text = {
            Column {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it.uppercase() },
                    label = { Text("Kitchen Category Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nameFocus),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (categoryName.isNotBlank()) {
                                val newKitchenCategory = KitchenCategory(
                                    kitchen_cat_id = kitchenCategory?.kitchen_cat_id ?: 0,
                                    kitchen_cat_name = categoryName,
                                    is_active = if (isActive) 1L else 0L
                                )
                                onSave(newKitchenCategory)
                            }
                        }
                    )
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
                    val newKitchenCategory = KitchenCategory(
                        kitchen_cat_id = kitchenCategory?.kitchen_cat_id ?: 0,
                        kitchen_cat_name = categoryName,
                        is_active = if (isActive) 1L else 0L
                    )
                    onSave(newKitchenCategory)
                },
                enabled = categoryName.isNotBlank()
            ) {
                Text(if (kitchenCategory != null) "Update" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}