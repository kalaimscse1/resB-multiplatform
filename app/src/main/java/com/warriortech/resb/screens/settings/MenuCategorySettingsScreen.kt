package com.warriortech.resb.screens.settings

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.MenuCategory
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.master.MenuCategorySettingsViewModel
import com.warriortech.resb.util.ReusableBottomSheet
import com.warriortech.resb.util.SuccessDialogWithButton
import com.warriortech.resb.util.getDeviceInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuCategorySettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: MenuCategorySettingsViewModel = hiltViewModel(),
    drawerState: DrawerState,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val order by viewModel.orderBy.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<MenuCategory?>(null) }
    var categoryToDelete by remember { mutableStateOf<MenuCategory?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    var sucess by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    val deviceInfo = getDeviceInfo()
    val isTabletLandscape = deviceInfo.isTablet && deviceInfo.isLandscape
    val showAdaptiveGrid = isTabletLandscape || deviceInfo.isLargeTablet

    LaunchedEffect(Unit) {
        viewModel.getOrderBy()
        viewModel.loadCategories()
    }
    BackHandler {
        navController.navigate("dashboard") {
            popUpTo("dashboard") { inclusive = true }
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            if (errorMessage == "Menu Category deleted successfully" || errorMessage == "Menu Category updated successfully" || errorMessage == "Menu Category added successfully") {
                sucess = true
            } else {
                failed = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Category", color = SurfaceLight) },
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
                            items(state.categories) { category ->
                                CategoryCard(
                                    category = category,
                                    onEdit = { editingCategory = it },
                                    onDelete = {
                                        categoryToDelete = it
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
                            items(state.categories) { category ->
                                CategoryCard(
                                    category = category,
                                    onEdit = { editingCategory = it },
                                    onDelete = {
                                        categoryToDelete = it
                                    }
                                )
                            }
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

        categoryToDelete?.let { category ->
            AlertDialog(
                onDismissRequest = { categoryToDelete = null },
                title = { Text("Confirm Delete") },
                text = { Text("Are you sure you want to delete the category '${category.item_cat_name}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.deleteCategory(category.item_cat_id)
                            }
                            categoryToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { categoryToDelete = null }) {
                        Text("Cancel")
                    }
                }
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

        if (failed) {
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
    onDelete: (MenuCategory) -> Unit,
    isGrid: Boolean = false
) {
    MobileOptimizedCard(
        modifier = if (isGrid) Modifier.height(130.dp) else Modifier.fillMaxWidth()
    ) {
        if (isGrid) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = category.item_cat_name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (category.is_active != false) "Active" else "Inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (category.is_active != false) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onEdit(category) }, modifier = Modifier.size(44.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = BluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { onDelete(category) }, modifier = Modifier.size(44.dp)) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.item_cat_name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (category.is_active != false) "Active" else "Inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (category.is_active != false) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                IconButton(onClick = { onEdit(category) }, modifier = Modifier.size(44.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = BluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = { onDelete(category) }, modifier = Modifier.size(44.dp)) {
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

@SuppressLint("AutoboxingStateCreation")
@Composable
fun CategoryDialog(
    category: MenuCategory?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean) -> Unit,
    order: String
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Centralized Focus Requesters
    val focusRequesters = remember {
        mapOf(
            "name" to FocusRequester(),
            "order" to FocusRequester()
        )
    }

    // State
    var name by remember { mutableStateOf(category?.item_cat_name ?: "") }
    var orderBy by remember { mutableStateOf(category?.order_by ?: order) }
    var isActive by remember { mutableStateOf(category?.is_active != false) }

    // Validation States
    var nameError by remember { mutableStateOf<String?>(null) }
    var orderError by remember { mutableStateOf<String?>(null) }

    fun validate(): String? {
        if (name.isBlank()) {
            nameError = "Category Name is required"
            return "name"
        } else nameError = null

        if (orderBy.isBlank()) {
            orderError = "Order is required"
            return "order"
        } else orderError = null

        return null
    }

    LaunchedEffect(Unit) {
        focusRequesters["name"]?.requestFocus()
    }

    ReusableBottomSheet(
        onDismiss = onDismiss,
        title = if (category == null) "Add Category" else "Edit Category",
        onSave = {
            val errorField = validate()
            if (errorField == null) {
                onConfirm(name, orderBy, isActive)
            } else {
                focusRequesters[errorField]?.requestFocus()
            }
        },
        isSaveEnabled = name.isNotBlank() && orderBy.isNotBlank(),
        buttonText = if (category == null) "Add" else "Update"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .verticalScroll(scrollState)
                .imePadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FormTextField(
                value = name,
                onValueChange = { name = it.uppercase() },
                label = "Category Name *",
                focusRequester = focusRequesters["name"]!!,
                nextFocusRequester = focusRequesters["order"],
                isError = nameError != null,
                errorMessage = nameError,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Characters
                ),
                scrollState = scrollState,
                scope = scope
            )

            FormTextField(
                value = orderBy,
                onValueChange = { orderBy = it.uppercase() },
                label = "Order *",
                focusRequester = focusRequesters["order"]!!,
                isError = orderError != null,
                errorMessage = orderError,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Characters
                ),
                onImeAction = {
                    val errorField = validate()
                    if (errorField == null) {
                        onConfirm(name, orderBy, isActive)
                    } else {
                        focusRequesters[errorField]?.requestFocus()
                    }
                },
                scrollState = scrollState,
                scope = scope
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
                Spacer(Modifier.width(8.dp))
                Text("Active")
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}
