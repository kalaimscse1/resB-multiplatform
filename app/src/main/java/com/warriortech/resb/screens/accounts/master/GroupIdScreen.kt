package com.warriortech.resb.screens.accounts.master

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.IconButton
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warriortech.resb.model.TblGroupDetails
import com.warriortech.resb.model.TblGroupNature
import com.warriortech.resb.model.TblGroupRequest
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.master.GroupDetailsViewModel
import com.warriortech.resb.util.GroupDropdown
import com.warriortech.resb.util.GroupNatureDropdown
import com.warriortech.resb.util.ReusableBottomSheet
import com.warriortech.resb.util.StringDropdown
import com.warriortech.resb.util.SuccessDialogWithButton
import kotlinx.coroutines.launch
import kotlin.collections.find

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    viewModel: GroupDetailsViewModel = hiltViewModel(),
    drawerState: DrawerState,
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<TblGroupDetails?>(null) }
    val scope = rememberCoroutineScope()
    val uiSate by viewModel.groupState.collectAsStateWithLifecycle()
    val groupNature by viewModel.groupNatures.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val order by viewModel.orderBy.collectAsStateWithLifecycle()
    val msg by viewModel.msg.collectAsStateWithLifecycle()
    var showAlert by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadGroupNature()
        viewModel.loadGroups()
        viewModel.getOrderBy()
    }

    LaunchedEffect(msg) {
        if (msg.isNotEmpty()) {
            showAlert = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        Column {
                            androidx.compose.material3.Text(
                                "Group List",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = SurfaceLight
                            )
                        }
                    }
                },
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
                    IconButton(onClick = {
                        editingGroup = null
                        showDialog = true
                    }) {
                        Icon(
                            Icons.Default.Add, contentDescription = "Add Group",
                            tint = SurfaceLight
                        )
                    }
                }
            )
        },
//        floatingActionButton = {
//            FloatingActionButton(onClick = {
//                editingGroup = null
//                showDialog = true
//            }) {
//                Icon(Icons.Default.Add, contentDescription = "Add")
//            }
//        }
    ) { padding ->

        when (val state = uiSate) {
            is GroupDetailsViewModel.GroupUiState.Loading -> {
                Text(
                    "Loading...", modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                )
            }

            is GroupDetailsViewModel.GroupUiState.Error -> {
                Text(
                    "Error: ${state.message}",
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                )
            }

            is GroupDetailsViewModel.GroupUiState.Success -> {
                val group = state.groups
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(group) { group ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "${group.group_name} ",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Nature: ${group.group_nature.g_nature_name}")
                                    Text("Active: ${if (group.is_active) "Yes" else "No"}")
                                    Text("Group:${groups.find { it.group_id == group.group_by }?.group_name}")
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(onClick = {
                                        editingGroup = group
                                        showDialog = true
                                    }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = BluePrimary
                                        )
                                    }
                                    if (!group.is_default){
                                        IconButton(onClick = { viewModel.deleteGroup(group.group_id) }) {
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
                    }
                }

            }
        }

        if (showDialog) {
            GroupFormDialog(
                group = editingGroup,
                natures = groupNature,
                onDismiss = { showDialog = false },
                onSave = {
                    if (editingGroup == null)
                        viewModel.addGroup(it)
                    else
                        viewModel.updateGroup(it.group_id, it)
                    showDialog = false
                },
                groups = groups,
                order = order.toInt()
            )
        }
        if (showAlert) {
            SuccessDialogWithButton(
                title = "Success",
                paddingValues = padding,
                description = msg,
                onClick = {
                    showAlert = false
                    viewModel.loadGroups()
                    viewModel.clearMsg()
                },
            )
        }
    }
}

@Composable
fun GroupFormDialog(
    group: TblGroupDetails?,
    natures: List<TblGroupNature>,
    onDismiss: () -> Unit,
    onSave: (TblGroupRequest) -> Unit,
    groups: List<TblGroupDetails>,
    order: Int
) {
    val groupList = listOf("YES", "NO")
    var groupCode by remember { mutableStateOf(group?.group_name ?: "") }
    var groupName by remember { mutableStateOf(group?.group_fullname ?: "") }
    var groupOrder by remember { mutableStateOf(group?.group_order?.toString() ?: "$order") }
    var subGroup by remember { mutableStateOf(group?.sub_group ?: "") }
    var grossProfit by remember { mutableStateOf(group?.gross_profit ?: "") }
    var tamilText by remember { mutableStateOf(group?.tamil_text ?: "") }
    var groupBy by remember { mutableStateOf(group?.group_by ?: 1) }
    var isDefault by remember { mutableStateOf(group?.is_default ?: false) }
    var isActive by remember { mutableStateOf(group?.is_active ?: true) }

    var expanded by remember { mutableStateOf(false) }
    var selectedNature by remember { mutableStateOf(group?.group_nature ?: natures[2]) }


    ReusableBottomSheet(
        onDismiss = onDismiss,
        title = if (group != null) "Edit Group" else "Add Group",
        onSave = {
            val updatedGroup = TblGroupRequest(
                group_id = group?.group_id ?: 0,
                group_name = groupCode,
                group_fullname = groupCode,
                group_order = groupOrder.toIntOrNull() ?: 0,
                sub_group = subGroup,
                group_nature = selectedNature.g_nature_id,
                gross_profit = grossProfit,
                tamil_text = tamilText,
                is_active = isActive,
                group_by = groupBy,
                is_default = isDefault
            )
            onSave(updatedGroup)
        },
        isSaveEnabled = groupCode.isNotBlank(),
        buttonText = if (group != null) "Update" else "Add"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp) // limit dialog height
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = groupCode,
                onValueChange = { groupCode = it },
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = groupCode,
                onValueChange = { groupName = it },
                label = { Text("Group Description") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = groupOrder,
                onValueChange = { groupOrder = it },
                label = { Text("Order") },
                modifier = Modifier.fillMaxWidth()
            )
            StringDropdown(
                options = groupList,
                selectedOption = groupList.find { it == subGroup },
                onOptionSelected = { subGroup = it },
                label = "Sub Group",
                modifier = Modifier.fillMaxWidth()
            )
            GroupNatureDropdown(
                groupNatures = natures,
                selectedGroupNature = natures.find { it.g_nature_id == selectedNature.g_nature_id },
                onGroupNatureSelected = { selectedNature = it },
                label = "Group Nature",
                modifier = Modifier.fillMaxWidth()
            )
            StringDropdown(
                options = groupList,
                selectedOption = groupList.find { it == grossProfit },
                onOptionSelected = { grossProfit = it },
                label = "Gross Profit Effect",
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = tamilText,
                onValueChange = { tamilText = it },
                label = { Text("Tamil Text") },
                modifier = Modifier.fillMaxWidth()
            )
            GroupDropdown(
                groups = groups,
                selectedGroup = groups.find { it.group_id == groupBy },
                onGroupSelected = { groupBy = it.group_id },
                modifier = Modifier.fillMaxWidth(),
                label = "Select Group"
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                Text("Is Default")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isActive, onCheckedChange = { isActive = it })
                Text("Active")
            }
        }
    }
}
