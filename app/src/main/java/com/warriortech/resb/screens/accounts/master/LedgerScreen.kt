package com.warriortech.resb.screens.accounts.master

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warriortech.resb.model.TblGroupDetails
import com.warriortech.resb.model.TblLedgerDetails
import com.warriortech.resb.model.TblLedgerRequest
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.master.LedgerViewModel
import com.warriortech.resb.util.GroupDropdown
import com.warriortech.resb.util.ReusableBottomSheet
import com.warriortech.resb.util.SuccessDialogWithButton
import com.warriortech.resb.util.getCurrentDateModern
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    viewModel: LedgerViewModel = hiltViewModel(),
    drawerState: DrawerState,
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<TblLedgerDetails?>(null) }
    val scope = rememberCoroutineScope()
    val ledgerState by viewModel.ledgerState.collectAsStateWithLifecycle()
    val groups by viewModel.group.collectAsStateWithLifecycle()
    val order by viewModel.orderBy.collectAsStateWithLifecycle()
    val msg by viewModel.msg.collectAsStateWithLifecycle()
    var showAlert by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadLedgers()
        viewModel.getGroups()
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
                                "Ledger List",
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
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            Icons.Default.Add, contentDescription = "Add Ledger",
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
    ) { paddingValues ->
        when (val state = ledgerState) {
            is LedgerViewModel.LedgerUiState.Loading -> {
                // Show loading indicator)
                Text(
                    "Loading...", modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                )
            }

            is LedgerViewModel.LedgerUiState.Error -> {
                // Show error message
                Text(
                    "Error: ${state.message}",
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                )
            }

            is LedgerViewModel.LedgerUiState.Success -> {
                val ledgers = state.ledgers
                if (ledgers.isEmpty()) {
                    Text(
                        "No ledger details found.",
                        modifier = Modifier
                            .padding(paddingValues)
                            .padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        items(ledgers) { group ->
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
                                            "${group.ledger_name} (${group.ledger_fullname})",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(group.group.group_name)
                                        Text(if (group.is_active) "Yes" else "No")
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
                                            IconButton(onClick = { viewModel.deleteLedger(group.ledger_id) }) {
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
        }
        if (showDialog) {
            LedgerDialog(
                ledger = editingGroup,
                onDismiss = {
                    showDialog = false
                },
                onSave = {
                    if (editingGroup == null)
                        viewModel.addLedger(it)
                    else
                        viewModel.updateLedger(it.ledger_id, it)
                    showDialog = false
                },
                group = groups,
                onBankDialog = {},
                order = order.toInt()
            )
        }
        if (showAlert) {
            SuccessDialogWithButton(
                title = "Success",
                paddingValues = paddingValues,
                description = msg,
                onClick = {
                    showAlert = false
                    viewModel.loadLedgers()
                    viewModel.clearMsg()
                },
            )
        }

    }

}

@Composable
fun LedgerDialog(
    ledger: TblLedgerDetails?,
    onDismiss: () -> Unit,
    onSave: (TblLedgerRequest) -> Unit,
    group: List<TblGroupDetails>,
    onBankDialog: () -> Unit,
    order: Int
) {
    val options = listOf("YES", "NO")
    val opening = listOf("0DR", "0CR")

    // Ledger Expense fields
    var ledgerName by remember { mutableStateOf(ledger?.ledger_name ?: "") }
    var ledgerFullName by remember { mutableStateOf(ledger?.ledger_fullname ?: "") }
    var orderBy by remember { mutableStateOf(ledger?.order_by ?: order) }
    var groupId by remember { mutableStateOf(ledger?.group?.group_id ?: 1) }

    // Ledger Other Fields
    var address by remember { mutableStateOf(ledger?.address ?: "") }
    var address1 by remember { mutableStateOf(ledger?.address1 ?: "") }
    var place by remember { mutableStateOf(ledger?.place ?: "") }
    var distance by remember { mutableStateOf(ledger?.distance ?: 0.0) }
    var pincode by remember { mutableStateOf(ledger?.pincode ?: 0) }
    var country by remember { mutableStateOf(ledger?.country ?: "") }
    var stateCode by remember { mutableStateOf(ledger?.state_code ?: "") }
    var stateName by remember { mutableStateOf(ledger?.state_name ?: "") }
    var contact_no by remember { mutableStateOf(ledger?.contact_no ?: "") }
    var email by remember { mutableStateOf(ledger?.email ?: "") }
    var bankDetails by remember { mutableStateOf(ledger?.bank_details ?: options.first()) }
    var sacCode by remember { mutableStateOf(ledger?.sac_code ?: "") }
    var panNo by remember { mutableStateOf(ledger?.pan_no ?: "") }
    var gstNo by remember { mutableStateOf(ledger?.gst_no ?: "") }
    var igst_status by remember { mutableStateOf(ledger?.igst_status ?: options.first()) }
    var openingBalance by remember { mutableStateOf(ledger?.opening_balance ?: opening.first()) }
    var tamilText by remember { mutableStateOf(ledger?.tamil_text ?: "") }
    var dueDate by remember { mutableStateOf(ledger?.due_date ?: getCurrentDateModern()) }
    var isDefault by remember { mutableStateOf(ledger?.is_default ?: false) }
    var isActive by remember { mutableStateOf(ledger?.is_active ?: true) }


    ReusableBottomSheet(
        onDismiss = onDismiss,
        title = if (ledger != null) "Edit Ledger" else "Add Ledger",
        onSave = {
            val ledger = TblLedgerRequest(
                ledger_id = ledger?.ledger_id ?: 0,
                ledger_name = ledger?.ledger_name ?: ledgerName,
                ledger_fullname = ledger?.ledger_fullname ?: ledgerName,
                order_by = ledger?.order_by ?: orderBy,
                group_id = ledger?.group?.group_id ?: groupId,
                address = ledger?.address ?: address,
                address1 = ledger?.address1 ?: address1,
                place = ledger?.place ?: place,
                distance = ledger?.distance ?: distance,
                pincode = ledger?.pincode ?: pincode,
                country = ledger?.country ?: country,
                contact_no = ledger?.contact_no ?: contact_no,
                email = ledger?.email ?: email,
                gst_no = ledger?.gst_no ?: gstNo,
                pan_no = ledger?.pan_no ?: panNo,
                state_code = ledger?.state_code ?: stateCode,
                state_name = ledger?.state_name ?: stateName,
                sac_code = ledger?.sac_code ?: sacCode,
                igst_status = ledger?.igst_status ?: igst_status,
                opening_balance = ledger?.opening_balance ?: openingBalance,
                due_date = ledger?.due_date ?: dueDate,
                bank_details = ledger?.bank_details ?: bankDetails,
                tamil_text = ledger?.tamil_text ?: tamilText,
                is_active = ledger?.is_active ?: isActive,
                is_default = ledger?.is_default ?: isDefault
            )
            onSave(ledger)
        },
        isSaveEnabled = ledgerName.isNotBlank(),
        buttonText = if (ledger != null) "Update" else "Add"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp) // limit dialog height
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Common Ledger Fields
            OutlinedTextField(
                value = ledgerName,
                onValueChange = { ledgerName = it },
                label = { Text("Ledger Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = ledgerName,
                onValueChange = { ledgerFullName = it },
                label = { Text("Ledger Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = orderBy.toString(),
                onValueChange = { orderBy = it.toInt() },
                label = { androidx.compose.material3.Text("Order") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            GroupDropdown(
                groups = group,
                selectedGroup = group.find { it.group_id == groupId },
                onGroupSelected = { groupId = it.group_id },
                modifier = Modifier.fillMaxWidth(),
                label = "Select Group"
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                Text("Is Default")
            }

        }

    }
}