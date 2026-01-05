package com.warriortech.resb.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.warriortech.resb.model.Area
import com.warriortech.resb.model.Role
import com.warriortech.resb.model.TblCounter
import com.warriortech.resb.model.TblStaff
import com.warriortech.resb.model.TblStaffRequest
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.StaffViewModel
import com.warriortech.resb.util.AreaDropdown
import com.warriortech.resb.util.CounterDropdown
import com.warriortech.resb.util.RoleDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffSettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: StaffViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingStaff by remember { mutableStateOf<TblStaff?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val areas by viewModel.areas.collectAsStateWithLifecycle()
    val counters by viewModel.counters.collectAsStateWithLifecycle()
    val roles by viewModel.roles.collectAsStateWithLifecycle()
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Settings", color = SurfaceLight) },
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
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(
                    Icons.Default.Add, contentDescription = "Add Staff",
                    tint = SurfaceLight
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                items(uiState.staff) { staff ->
                    StaffCard(
                        staff = staff,
                        onEdit = { editingStaff = staff },
                        onDelete = { viewModel.deleteStaff(staff.staff_id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddStaffDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { staff ->
                viewModel.addStaff(staff)
                showAddDialog = false
            },
            areas = areas,
            counters = counters,
            roles = roles
        )
    }

    editingStaff?.let { staff ->
        EditStaffDialog(
            staff = staff,
            onDismiss = { editingStaff = null },
            onUpdate = { updatedStaff ->
                viewModel.updateStaff(updatedStaff)
                editingStaff = null
            },
            areas = areas,
            counters = counters,
            roles = roles
        )
    }
}

@Composable
fun StaffCard(
    staff: TblStaff,
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = staff.staff_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = staff.role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = staff.address,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = staff.contact_no,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row{
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
fun AddStaffDialog(
    onDismiss: () -> Unit,
    onAdd: (TblStaffRequest) -> Unit,
    areas: List<Area>,
    counters: List<TblCounter>,
    roles: List<Role>
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(1L) }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var commission by remember { mutableStateOf("") }
    var areaId by remember { mutableStateOf(1L) }
    var counterId by remember { mutableStateOf(1L) }
    var isActive by remember { mutableStateOf(1L) }
    var isBlock by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Staff") },
        text = {
            // ✅ Make dialog scrollable
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp) // set max height so dialog doesn’t grow infinitely
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                RoleDropdown(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Select Role",
                    roles = roles,
                    selectedRole = roles.find { it.role_id == role },
                    onRoleSelected = { role = it.role_id },
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("UserName") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = commission,
                    onValueChange = { commission = it },
                    label = { Text("Commission") },
                    modifier = Modifier.fillMaxWidth()
                )
                CounterDropdown(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Select Counter",
                    counters = counters,
                    selectedCounter = counters.find { it.counter_id == counterId },
                    onCounterSelected = { counterId = it.counter_id },
                )
                AreaDropdown(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Select Area",
                    areas = areas,
                    selectedArea = areas.find { it.area_id == areaId },
                    onAreaSelected = { areaId = it.area_id },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = isBlock,
                        onCheckedChange = { isBlock = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("IsBlock")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = isActive == 1L,
                        onCheckedChange = { isActive = if (it) 1L else 0L }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Active")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAdd(
                        TblStaffRequest(
                            1L, name, phone, address, userName,
                            password = password,
                            role_id = role,
                            last_login = "",
                            is_block = isBlock,
                            counter_id = counterId,
                            is_active = isActive,
                            area_id = areaId,
                            commission = commission.toDoubleOrNull() ?: 0.0
                        )
                    )
                },
                enabled = name.isNotBlank() &&
                        phone.isNotBlank() &&
                        userName.isNotBlank() &&
                        password.isNotBlank() &&
                        role != 0L &&
                        counterId != 0L
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditStaffDialog(
    staff: TblStaff,
    onDismiss: () -> Unit,
    onUpdate: (TblStaffRequest) -> Unit,
    areas: List<Area>,
    counters: List<TblCounter>,
    roles: List<Role>
) {
    var name by remember { mutableStateOf(staff.staff_name) }
    var role by remember { mutableStateOf(staff.role_id) }
    var address by remember { mutableStateOf(staff.address) }
    var phone by remember { mutableStateOf(staff.contact_no) }
    var userName by remember { mutableStateOf(staff.user_name) }
    var password by remember { mutableStateOf(staff.password) }
    var commission by remember { mutableStateOf(staff.commission.toString()) }
    var areaId by remember { mutableStateOf(staff.area_id) }
    var counterId by remember { mutableStateOf(staff.counter_id) }
    var isActive by remember { mutableLongStateOf(staff.is_active) }
    var isBlock by remember { mutableStateOf(staff.is_block) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Staff") },
        text = {
            // ✅ Make contents scrollable
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp) // limit dialog height
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                RoleDropdown(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Select Role",
                    roles = roles,
                    selectedRole = roles.find { it.role_id == role },
                    onRoleSelected = { role = it.role_id },
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("UserName") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = commission,
                    onValueChange = { commission = it },
                    label = { Text("Commission") },
                    modifier = Modifier.fillMaxWidth()
                )
                CounterDropdown(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Select Counter",
                    counters = counters,
                    selectedCounter = counters.find { it.counter_id == counterId },
                    onCounterSelected = { counterId = it.counter_id },
                )
                AreaDropdown(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Select Area",
                    areas = areas,
                    selectedArea = areas.find { it.area_id == areaId },
                    onAreaSelected = { areaId = it.area_id },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = isBlock,
                        onCheckedChange = { isBlock = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("IsBlock")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = isActive == 1L,
                        onCheckedChange = { isActive = if (it) 1L else 0L }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Active")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onUpdate(
                        TblStaffRequest(
                            staff_id = staff.staff_id,
                            staff_name = name,
                            contact_no = phone,
                            address = address,
                            user_name = userName,
                            password = password,
                            role_id = role,
                            last_login = "",
                            is_block = isBlock,
                            counter_id = counterId,
                            is_active = isActive,
                            area_id = areaId,
                            commission = commission.toDouble()
                        )
                    )
                },
                enabled = name.isNotBlank() &&
                        address.isNotBlank() &&
                        phone.isNotBlank() &&
                        userName.isNotBlank() &&
                        password.isNotBlank()
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

