package com.warriortech.resb.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warriortech.resb.model.MenuCategory
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.master.MenuCategorySettingsViewModel
import com.warriortech.resb.ui.viewmodel.master.MenuSettingsViewModel
import com.warriortech.resb.util.ReusableBottomSheet
import com.warriortech.resb.util.SuccessDialogWithButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuCategorySettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: MenuCategorySettingsViewModel = hiltViewModel(),
    drawerState: DrawerState
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val order by viewModel.orderBy.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<MenuCategory?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    var sucess by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getOrderBy()
        viewModel.loadCategories()
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage!= null) {
            if (errorMessage=="Menu Category deleted successfully") {
                sucess = true
            } else {
                failed = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MenuCategory Settings", color = SurfaceLight) },
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
                            Icons.Default.Add, contentDescription = "Add MenuCategory",
                            tint = SurfaceLight
                        )
                    }
                }
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
                    is MenuCategorySettingsViewModel.UiState.Success -> state.categories
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
            is MenuCategorySettingsViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is MenuCategorySettingsViewModel.UiState.Success -> {
                if (state.categories.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No categories found", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.categories) { category ->
                            CategoryCard(
                                category = category,
                                onEdit = { editingCategory = it },
                                onDelete = {
                                    scope.launch {
                                        viewModel.deleteCategory(it.item_cat_id)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            is MenuCategorySettingsViewModel.UiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }


        if (showAddDialog) {
            CategoryDialog(
                category = null,
                onDismiss = { showAddDialog = false },
                onConfirm = { name, description, sortOrder ->
                    scope.launch {
                        viewModel.addCategory(name, description, sortOrder)
                        showAddDialog = false
                    }
                },
                order = order
            )
        }

        editingCategory?.let { category ->
            CategoryDialog(
                category = category,
                onDismiss = { editingCategory = null },
                onConfirm = { name, description, sortOrder ->
                    scope.launch {
                        viewModel.updateCategory(category.item_cat_id, name, description, sortOrder)
                        editingCategory = null
                    }
                },
                order = order
            )
        }

        if (sucess) {
            SuccessDialogWithButton(
                title = "Success",
                description = errorMessage.toString(),
                paddingValues = paddingValues,
                onClick = {
                    sucess = false
                    viewModel.loadCategories()
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
                    viewModel.loadCategories()
                    viewModel.clearErrorMessage()
                }
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: MenuCategory,
    onEdit: (MenuCategory) -> Unit,
    onDelete: (MenuCategory) -> Unit
) {
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
                    text = category.item_cat_name,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            IconButton(onClick = { onEdit(category) }) {
                Icon(Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = BluePrimary)
            }
            IconButton(onClick = { onDelete(category) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CategoryDialog(
    category: MenuCategory?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean) -> Unit,
    order : String
) {
    var name by remember { mutableStateOf(category?.item_cat_name ?: "") }
    var orderBy by remember { mutableStateOf(category?.order_by ?: order) }
    var isActive by remember { mutableStateOf(category?.is_active != false) }

    ReusableBottomSheet(
        onDismiss = onDismiss,
        title = if (category == null) "Add Category" else "Edit Category",
        onSave = { onConfirm(name, orderBy, isActive) },
        isSaveEnabled = name.isNotBlank(),
        buttonText = if (category == null) "Add" else "Update"
    ) {
        Column {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.uppercase() },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = orderBy,
                onValueChange = { orderBy = it },
                label = { Text("Order") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
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
    }
}
