package com.warriortech.resb.screens.accounts.transaction

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.room.util.copy
import com.warriortech.resb.MainActivity
import com.warriortech.resb.model.TblLedgerDetailIdRequest
import com.warriortech.resb.model.TblLedgerDetails
import com.warriortech.resb.screens.ActionButton
import com.warriortech.resb.screens.ClearDialog
import com.warriortech.resb.ui.components.ModernDivider
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.ResbTypography
import com.warriortech.resb.ui.theme.SecondaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.theme.ghostWhite
import com.warriortech.resb.ui.viewmodel.accounts.LedgerDetailsViewModel
import com.warriortech.resb.util.AnimatedSnackbarDemo
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.LedgerDetailsEntryDropdown
import com.warriortech.resb.util.LedgerDropdown
import com.warriortech.resb.util.StringDropdown
import com.warriortech.resb.util.SuccessDialogWithButton
import com.warriortech.resb.util.getCurrentDateModern
import com.warriortech.resb.util.getCurrentTimeModern
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.collections.indexOf

@SuppressLint("ConfigurationScreenWidthHeight", "ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEntryModifyScreen(
    viewModel: LedgerDetailsViewModel = hiltViewModel(),
    drawerState: DrawerState,
    navController: NavHostController,
    entryNo: String? = null
) {
    val ledgerDetailsState by viewModel.ledgerDetailsState.collectAsStateWithLifecycle()
    val transactionState by viewModel.transactionState.collectAsStateWithLifecycle()
    val modifyState by viewModel.modifyState.collectAsStateWithLifecycle()
    val ledgerList by viewModel.ledgerList.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val voucher by viewModel.voucher.collectAsStateWithLifecycle()
    val ledgerDetails by viewModel.ledgerDetails.collectAsStateWithLifecycle()

    // --- Screen state ---
    var selectedCategory by remember { mutableStateOf<String>("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    var paymentMode by remember { mutableStateOf(PaymentMode.CASH) }
    var partyId by remember { mutableStateOf<Int>(1) }
    var otherPartyId by remember { mutableStateOf<Int?>(null) }

    // Entries
    val entriesState = remember { mutableStateMapOf<Long, TblLedgerDetailIdRequest>() }

    val totals by remember {
        derivedStateOf {
            val list = entriesState.values.toList()
            Totals(
                debit = list.sumOf { it.amount_out },
                credit = list.sumOf { it.amount_in }
            )
        }
    }

    // UI state
    var isSaving by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    // Dialog state
    var showLedgerDetailDialog by remember { mutableStateOf(false) }
    var selectedLedgerDetail by remember { mutableStateOf<TblLedgerDetailIdRequest?>(null) }
    val listState = rememberLazyListState()
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }

    val context = LocalContext.current as? MainActivity
    var showDropdown by remember { mutableStateOf(false) }

    // Volume button triggers dropdown
    LaunchedEffect(Unit) {
        context?.onVolumeUpPressed = { showDropdown = true }
    }

    DisposableEffect(Unit) {
        onDispose {
            context?.onVolumeUpPressed = null
        }
    }

    // Load existing entry if entryNo provided
    LaunchedEffect(entryNo) {
        entryNo?.let { viewModel.getLedgerDetailsByEntryNo(it) }
    }

    // Transaction save/update result
    LaunchedEffect(transactionState) {
        when (val state = transactionState) {
            is LedgerDetailsViewModel.TransactionUiState.Success -> showSuccess = true
            is LedgerDetailsViewModel.TransactionUiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar("Failed: ${state.message}") }
            }

            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { AnimatedSnackbarDemo(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (entryNo != null) "Modify Entry" else "Day Entry",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = SurfaceLight
                        )
                        Text(
                            "Entry No: ${entryNo ?: "New"}",
                            style = ResbTypography.labelSmall,
                            color = SurfaceLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen),
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = SurfaceLight)
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedDate.toString(), color = SurfaceLight)
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "Calendar",
                                tint = SurfaceLight
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = SecondaryGreen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 🟥 Clear
                        ActionButton("Clear", Color.Red, enabled = true) {
                            showClearDialog = true
                        }

                        // 🟩 Modify (Update existing entry)
                        ActionButton(
                            if (isSaving) "Updating..." else "Update",
                            enabled = !isSaving,
                            color = Color(0xFF4CAF50)
                        ) {
                            if (entriesState.isEmpty()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("No entries to update")
                                }
                            } else {
                                scope.launch {
                                    isSaving = true
                                    val entries = entriesState.values.toList()
                                    viewModel.updateLedgerDetails(entries)
                                    isSaving = false
                                }
                            }
                        }

                        // 🗑️ Delete (delete all entries for entryNo)
                        ActionButton(
                            "Delete",
                            enabled = !isSaving,
                            color = Color(0xFFD32F2F)
                        ) {
                            scope.launch {
                                try {
                                    viewModel.deleteLedgerDetailsByEntryNo(entryNo.toString())
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Delete failed: ${e.message}")
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            PaymentModeRow(
                paymentMode = paymentMode,
                onModeChange = { paymentMode = it },
                ledgerList = ledgerList,
                otherPartyId = otherPartyId,
                onOtherSelected = { otherPartyId = it },
                party = partyId,
                onPartySelected = { partyId = it }
            )
            // 🧭 Modify UI State Handling
            when (val state = modifyState) {
                is LedgerDetailsViewModel.ModifyUiState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is LedgerDetailsViewModel.ModifyUiState.Success -> {
                    val res = state.ledgerDetails
                    res.forEach { item ->
                        entriesState[item.ledger_details_id] = TblLedgerDetailIdRequest(
                            ledger_details_id = item.ledger_details_id,
                            id = item.ledger.ledger_id.toLong(),
                            date = item.date,
                            party_member = item.party_member,
                            party_id = item.party.ledger_id.toLong(),
                            member = item.member,
                            member_id = item.member_id,
                            purpose = item.purpose,
                            amount_in = item.amount_in,
                            amount_out = item.amount_out,
                            bill_no = item.bill_no,
                            time = item.time,
                        )
                    }
                    // --- Payment mode and entries list ---

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SecondaryGreen)
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            "LEDGER",
                            modifier = Modifier.weight(3f),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "REMARKS",
                            modifier = Modifier.weight(3f),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "DEBIT",
                            modifier = Modifier.weight(2f),
                            color = Color.White,
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "CREDIT",
                            modifier = Modifier.weight(2f),
                            color = Color.White,
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        state = listState
                    ) {
                        items(entriesState.keys.toList(), key = { it }) { ledgerId ->
                            val entry = entriesState[ledgerId] ?: return@items
                            val ledgerName =
                                ledgerList.find { it.ledger_id.toLong() == entry.id }?.ledger_name
                                    ?: ""
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = {
                                                selectedLedgerDetail = entry
                                                showLedgerDetailDialog = true
                                            },
                                            onLongPress = { showDeleteDialog = ledgerId }
                                        )
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(ledgerName, modifier = Modifier.weight(3f), fontSize = 12.sp)
                                Text(
                                    entry.purpose,
                                    modifier = Modifier.weight(3f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    entry.amount_out.toString(),
                                    modifier = Modifier.weight(2f),
                                    textAlign = TextAlign.End,
                                    color = if (entry.amount_out > 0) Color.Red else Color.Black
                                )
                                Text(
                                    entry.amount_in.toString(),
                                    modifier = Modifier.weight(2f),
                                    textAlign = TextAlign.End,
                                    color = if (entry.amount_in > 0) SecondaryGreen else Color.Black
                                )
                            }
                            ModernDivider(color = Color.LightGray, thickness = 0.5.dp)
                        }
                    }
                    // Totals
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SecondaryGreen)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Row: ${entriesState.size}",
                            color = SurfaceLight,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Credit: ${totals.credit}",
                            color = SurfaceLight,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Debit: ${totals.debit}",
                            color = SurfaceLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                is LedgerDetailsViewModel.ModifyUiState.Error -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("Error: ${state.message}", color = Color.Red) }

                else -> Unit
            }
        }
    }

    // Delete confirmation for single row
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteByLedgerDetailsId(showDeleteDialog ?: 0)
                    entriesState.remove(showDeleteDialog)
                    showDeleteDialog = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete this entry?") }
        )
    }

    // Dropdown for selecting which entry to edit
    if (showDropdown) {
        AlertDialog(
            onDismissRequest = { showDropdown = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDropdown = false }) { Text("Cancel") }
            },
            title = { Text("Select Entry No To Edit") },
            text = {
                StringDropdown(
                    options = ledgerDetails,
                    selectedOption = ledgerDetails.firstOrNull(),
                    onOptionSelected = {
                        navController.navigate("modify_day_entry/$it")
                        showDropdown = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        )
    }

    // Success dialog
    if (showSuccess) {
        SuccessDialogWithButton(
            title = "Saved",
            description = "Ledger entries updated successfully",
            paddingValues = PaddingValues(0.dp),
            onClick = {
                viewModel.clear()
                entriesState.clear()
                viewModel.loadData()
                showSuccess = false
                navController.popBackStack()
            }
        )
    }
    if (showLedgerDetailDialog && selectedLedgerDetail != null) {
        LedgerDetailEntryDialog(
            ledgerList = ledgerList,
            ledger = selectedLedgerDetail!!,
            onDismiss = { showLedgerDetailDialog = false },
            onAdd = { remark, amount, isPayment ->
                if (amount <= 0) {
                    scope.launch { snackbarHostState.showSnackbar("Amount must be greater than zero") }
                    return@LedgerDetailEntryDialog
                }
                val id = selectedLedgerDetail!!.ledger_details_id
                entriesState[id] = entriesState[id]?.copy(
                    purpose = remark,
                    amount_in = if (isPayment) 0.0 else amount,
                    amount_out = if (isPayment) amount else 0.0,
                ) ?: return@LedgerDetailEntryDialog

                showLedgerDetailDialog = false
            }
        )
    }
}

@Composable
fun LedgerDetailEntryDialog(
    ledgerList: List<TblLedgerDetails>,
    ledger: TblLedgerDetailIdRequest,
    onDismiss: () -> Unit,
    onAdd: (remark: String, amount: Double, isPayment: Boolean) -> Unit
) {
    val ledgerName =
        ledgerList.find { it.ledger_id.toLong() == ledger.id }?.ledger_name ?: ""
    var remark by remember { mutableStateOf(ledger.purpose) }
    var amount by remember { mutableStateOf(if (ledger.amount_in > 0) ledger.amount_in.toString() else ledger.amount_out.toString()) }
    var isPayment by remember { mutableStateOf(if (ledger.amount_in > 0) false else true) }

    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            delay(200) // slight delay to allow dialog to appear
            focusRequester.requestFocus()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                onAdd(remark, amt, isPayment)
                remark = ""
                amount = ""
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Modify Entry for $ledgerName") },
        text = {
            Column {
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("Remark") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isPayment,
                        onClick = { isPayment = true },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.Red,
                            unselectedColor = Color.White
                        )
                    )
                    Text("Payment", color = Color.Red)
                    Spacer(Modifier.width(16.dp))
                    RadioButton(
                        selected = !isPayment,
                        onClick = { isPayment = false },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = SecondaryGreen,
                            unselectedColor = Color.White
                        )
                    )
                    Text("Receipt", color = SecondaryGreen)
                }
            }
        }
    )
}