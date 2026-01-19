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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.Menu
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.master.MenuSettingsViewModel
import com.warriortech.resb.util.ReusableBottomSheet
import com.warriortech.resb.util.SuccessDialogWithButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuSettingsScreen(
    viewModel: MenuSettingsViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    drawerState: DrawerState,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val order by viewModel.orderBy.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMenu by remember { mutableStateOf<Menu?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    var sucess by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getOrderBy()
        viewModel.loadMenus()
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            if (errorMessage == "Menu Chart deleted successfully" || errorMessage == "Menu Chart updated successfully" || errorMessage == "Menu Chart added successfully") {
                sucess = true
            } else {
                failed = true
            }
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
                title = { Text("Menu Chart", color = SurfaceLight) },
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
                actions = {
                    IconButton(onClick = {
                        showAddDialog = true
                    }) {
                        Icon(
                            Icons.Default.Add, contentDescription = "Add Menu",
                            tint = SurfaceLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen
                )
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
                    is MenuSettingsViewModel.UiState.Success -> state.menus
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
            is MenuSettingsViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is MenuSettingsViewModel.UiState.Success -> {
                if (state.menus.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No menus available. Please add a menu.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.menus) { menu ->
                            MenuCard(
                                menu = menu,
                                onEdit = { editingMenu = it },
                                onDelete = {
                                    scope.launch {
                                        viewModel.deleteMenu(it.menu_id)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            is MenuSettingsViewModel.UiState.Error -> {
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
            viewModel.getOrderBy()
            MenuDialog(
                menu = null,
                onDismiss = { showAddDialog = false },
                onConfirm = { menu ->
                    scope.launch {
                        viewModel.addMenu(
                            menu.menu_name,
                            menu.order_by,
                            menu.is_active,
                            menu.start_time,
                            menu.end_time
                        )
                        showAddDialog = false
                    }
                },
                order = order
            )
        }

        editingMenu?.let { menu ->
            MenuDialog(
                menu = menu,
                onDismiss = { editingMenu = null },
                onConfirm = { menu ->
                    scope.launch {
                        viewModel.updateMenu(
                            menu.menu_id,
                            menu.menu_name,
                            menu.order_by,
                            menu.is_active,
                            menu.start_time,
                            menu.end_time
                        )
                        editingMenu = null
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
                    viewModel.loadMenus()
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
                    viewModel.loadMenus()
                    viewModel.clearErrorMessage()
                }
            )
        }
    }
}

@Composable
fun MenuCard(
    menu: Menu,
    onEdit: (Menu) -> Unit,
    onDelete: (Menu) -> Unit
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
                    text = menu.menu_name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (menu.is_active) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (menu.is_active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            IconButton(onClick = { onEdit(menu) }) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = BluePrimary
                )
            }
            IconButton(onClick = { onDelete(menu) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@SuppressLint("AutoboxingStateCreation")
@Composable
fun MenuDialog(
    menu: Menu?,
    onDismiss: () -> Unit,
    onConfirm: (Menu) -> Unit,
    order: String
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Centralized Focus Requesters
    val focusRequesters = remember {
        mapOf(
            "name" to FocusRequester(),
            "description" to FocusRequester(),
            "startTime" to FocusRequester(),
            "endTime" to FocusRequester()
        )
    }

    // State
    var name by remember { mutableStateOf(menu?.menu_name ?: "") }
    var description by remember { mutableStateOf(menu?.order_by ?: order) }
    var startTime by remember { mutableStateOf(menu?.start_time?.toString() ?: "") }
    var endTime by remember { mutableStateOf(menu?.end_time?.toString() ?: "") }
    var isActive by remember { mutableStateOf(menu?.is_active ?: true) }

    // Validation States
    var nameError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    fun validate(): String? {
        if (name.isBlank()) {
            nameError = "Name is required"
            return "name"
        } else nameError = null

        if (description.isBlank()) {
            descriptionError = "Order By is required"
            return "description"
        } else descriptionError = null

        return null
    }

    LaunchedEffect(Unit) {
        focusRequesters["name"]?.requestFocus()
    }

    ReusableBottomSheet(
        onDismiss = onDismiss,
        title = if (menu == null) "Add Menu" else "Edit Menu",
        isSaveEnabled = name.isNotBlank() && description.isNotBlank(),
        buttonText = if (menu == null) "Add" else "Update",
        onSave = {
            val errorField = validate()
            if (errorField == null) {
                onConfirm(
                    Menu(
                        menu_id = menu?.menu_id ?: 0L,
                        menu_name = name,
                        order_by = description,
                        start_time = startTime.toFloatOrNull() ?: 0f,
                        end_time = endTime.toFloatOrNull() ?: 0f,
                        is_active = isActive
                    )
                )
            } else {
                focusRequesters[errorField]?.requestFocus()
            }
        }
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
                label = "Name *",
                focusRequester = focusRequesters["name"]!!,
                nextFocusRequester = focusRequesters["description"],
                isError = nameError != null,
                errorMessage = nameError,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                scrollState = scrollState,
                scope = scope
            )

            FormTextField(
                value = description,
                onValueChange = { description = it.uppercase() },
                label = "Order By *",
                focusRequester = focusRequesters["description"]!!,
                nextFocusRequester = focusRequesters["startTime"],
                isError = descriptionError != null,
                errorMessage = descriptionError,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                scrollState = scrollState,
                scope = scope
            )

            Text(
                text = "Menu Time Settings",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            FormTextField(
                value = startTime,
                onValueChange = { startTime = it },
                label = "Start Time",
                focusRequester = focusRequesters["startTime"]!!,
                nextFocusRequester = focusRequesters["endTime"],
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                scrollState = scrollState,
                scope = scope
            )

            FormTextField(
                value = endTime,
                onValueChange = { endTime = it },
                label = "End Time",
                focusRequester = focusRequesters["endTime"]!!,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                onImeAction = { focusManager.clearFocus() },
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