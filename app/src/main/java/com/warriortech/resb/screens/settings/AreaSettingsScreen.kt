package com.warriortech.resb.screens.settings

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.Area
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.master.AreaViewModel
import com.warriortech.resb.util.ReusableBottomSheet
import com.warriortech.resb.util.SuccessDialogWithButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaSettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: AreaViewModel = hiltViewModel(),
    drawerState: DrawerState,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingArea by remember { mutableStateOf<Area?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    var sucess by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Handle messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    BackHandler {
        navController.navigate("dashboard") {
            popUpTo("dashboard") { inclusive = true }
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            if (errorMessage == "Area deleted successfully") {
                sucess = true
            } else {
                failed = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Area", color = SurfaceLight) },
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
                            Icons.Default.Add, contentDescription = "Add Area",
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
                val areas = uiState.areas
                Text(
                    text = "Total: ${areas.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.areas) { area ->
                    AreaCard(
                        area = area,
                        onEdit = { editingArea = area },
                        onDelete = { viewModel.deleteArea(area.area_id) }
                    )
                }
            }
        }
        if (sucess) {
            SuccessDialogWithButton(
                title = "Success",
                description = errorMessage.toString(),
                paddingValues = paddingValues,
                onClick = {
                    sucess = false
                    viewModel.loadAreas()
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
                    viewModel.loadAreas()
                    viewModel.clearErrorMessage()
                }
            )
        }
    }

    if (showAddDialog) {
        AreaDialog(
            area = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name , active ->
                viewModel.addArea(name)
                showAddDialog = false
            },
            onAdd = { name ->
                viewModel.addArea(name)
                showAddDialog = false
            }
        )
    }

    editingArea?.let { area ->
        AreaDialog(
            area = area,
            onDismiss = { editingArea = null },
            onConfirm = { name, active ->
                viewModel.updateArea(area.copy(area_name = name))
                editingArea = null
            }
        )
    }
}

@Composable
fun AreaCard(
    area: Area,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    MobileOptimizedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = area.area_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (area.is_active==1L) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (area.is_active==1L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
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
fun AreaDialog(
    area: Area?,
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit = { _, _ -> },
    onAdd: (String) -> Unit = { }
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Centralized Focus Requesters
    val focusRequesters = remember {
        mapOf(
            "name" to FocusRequester()
        )
    }

    // State
    var name by remember { mutableStateOf(area?.area_name ?: "") }
    var isActive by remember { mutableStateOf(area?.is_active ?: 1L) }

    // Validation State
    var nameError by remember { mutableStateOf<String?>(null) }

    fun validate(): String? {
        if (name.isBlank()) {
            nameError = "Area Name is required"
            return "name"
        } else nameError = null
        return null
    }

    LaunchedEffect(Unit) {
        focusRequesters["name"]?.requestFocus()
    }

    ReusableBottomSheet(
        onDismiss = onDismiss,
        title = if (area == null) "Add Area" else "Edit Area",
        isSaveEnabled = name.isNotBlank(),
        buttonText = if (area == null) "Add" else "Update",
        onSave = {
            val errorField = validate()
            if (errorField == null) {
                if (area == null) {
                    onAdd(name)
                } else {
                    onConfirm(name, isActive)
                }
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
                label = "Area Name *",
                focusRequester = focusRequesters["name"]!!,
                isError = nameError != null,
                errorMessage = nameError,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Characters
                ),
                onImeAction = {
                    val errorField = validate()
                    if (errorField == null) {
                        if (area == null) onAdd(name) else onConfirm(name, isActive)
                    } else {
                        focusRequesters[errorField]?.requestFocus()
                    }
                },
                scrollState = scrollState,
                scope = scope
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}
