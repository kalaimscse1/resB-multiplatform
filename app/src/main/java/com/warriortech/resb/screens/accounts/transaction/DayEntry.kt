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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import com.warriortech.resb.MainActivity
import com.warriortech.resb.model.TblLedgerDetailIdRequest
import com.warriortech.resb.model.TblLedgerDetails
import com.warriortech.resb.screens.ActionButton
import com.warriortech.resb.screens.ClearDialog
import com.warriortech.resb.ui.components.GradientFloatingActionButton
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
fun DayEntryScreen(
    viewModel: LedgerDetailsViewModel = hiltViewModel(),
    drawerState: DrawerState,
    navController: NavHostController,
) {
    val ledgerDetailsState by viewModel.ledgerDetailsState.collectAsStateWithLifecycle()
    val transactionState by viewModel.transactionState.collectAsStateWithLifecycle()
    val ledgerList by viewModel.ledgerList.collectAsStateWithLifecycle()
    val entryNo by viewModel.entryNo.collectAsStateWithLifecycle()
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
    var showLedgerDialog by remember { mutableStateOf(false) }
    var selectedLedger by remember { mutableStateOf<TblLedgerDetails?>(null) }
    val listState = rememberLazyListState()
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }
    var showLedgerDetailDialog by remember { mutableStateOf(false) }
    var selectedLedgerDetail by remember { mutableStateOf<TblLedgerDetailIdRequest?>(null) }

    var showDropdown by remember { mutableStateOf(false) }


    LaunchedEffect(transactionState) {
        when (val state = transactionState) {
            is LedgerDetailsViewModel.TransactionUiState.Success -> showSuccess = true
            is LedgerDetailsViewModel.TransactionUiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar("Entry Failed To Add") }
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
                            "Day Entry",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = SurfaceLight
                        )
                        Text(
                            "Entry No: $entryNo",
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
        floatingActionButton = {
            GradientFloatingActionButton(
                onClick = { showDropdown = true },
                icon = Icons.Default.Search,
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = SecondaryGreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(SecondaryGreen, SecondaryGreen)
                        ),
                        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionButton("Clear", Color.Red, enabled = true) { showClearDialog = true }
                        ActionButton(
                            if (isSaving) "Saving..." else "Save",
                            enabled = !isSaving,
                            color = Color(0xFF4CAF50)
                        ) {
                            if (entriesState.isEmpty()) {
                                scope.launch { snackbarHostState.showSnackbar("Add at least one ledger entry") }
                            } else {
                                scope.launch {
                                    isSaving = true
                                    val entries = entriesState.values.toList()
                                    viewModel.addLedgerDetails(entries)
                                    isSaving = false
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
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "DEBIT",
                    modifier = Modifier.weight(2f),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End
                )
                Text(
                    "CREDIT",
                    modifier = Modifier.weight(2f),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End
                )
            }
            LazyColumn(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth(),
                state = listState
            ) {
                items(entriesState.keys.toList(), key = { it }) { ledgerId ->
                    val entry = entriesState[ledgerId] ?: return@items
                    val ledgerName =
                        ledgerList.find { it.ledger_id.toLong() == ledgerId }?.ledger_name ?: ""
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp, vertical = 2.dp)
                                .padding(start = 4.dp, end = 4.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            selectedLedgerDetail = entry
                                            showLedgerDetailDialog = true
                                        },
                                        onLongPress = {
                                            showDeleteDialog = ledgerId
                                        }
                                    )
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                ledgerName,
                                maxLines = 1,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(3f)
                            )
                            Text(
                                entry.purpose,
                                maxLines = 1,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(2f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                entry.amount_out.toString(),
                                maxLines = 1,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(2f),
                                textAlign = TextAlign.End,
                                color = if (entry.amount_out > 0) Color.Red else Color.Black
                            )
                            Text(
                                entry.amount_in.toString(),
                                maxLines = 1,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(2f),
                                textAlign = TextAlign.End,
                                color = if (entry.amount_in > 0) SecondaryGreen else Color.Black
                            )
                        }
                    }
                    ModernDivider(color = Color.LightGray, thickness = 0.5.dp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SecondaryGreen)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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

            when (val state = ledgerDetailsState) {
                is LedgerDetailsViewModel.LedgerDetailsUiState.Success -> {
                    val filtered =
                        when {
                            selectedCategory == "ALL" -> state.ledgers
                            selectedCategory.isNotEmpty() -> state.ledgers.filter { it.group.group_nature.g_nature_name == selectedCategory }
                            else -> state.ledgers
                        }
                    if (filtered.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No ledger found")
                        }
                    } else {
                        // Category tabs
                        if (categories.isNotEmpty()) {
                            val cats = categories
                            val selectedIndex =
                                cats.indexOf(selectedCategory).takeIf { it >= 0 } ?: 0
                            ScrollableTabRow(
                                selectedTabIndex = selectedIndex,
                                containerColor = SecondaryGreen,
                                contentColor = SurfaceLight
                            ) {
                                cats.forEachIndexed { idx, cat ->
                                    Tab(
                                        selected = selectedCategory == cat,
                                        onClick = { selectedCategory = cat },
                                        text = { Text(cat) }
                                    )
                                }
                            }
                        }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxHeight(0.5f)
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(
                                filtered,
                                key = { index, product -> "${product.ledger_id}_${product.group.group_nature.g_nature_id}_$index" }
                            ) { _, product ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f) // makes all cards square (equal width & height)
                                        .clip(MaterialTheme.shapes.medium)
                                        .pointerInput(Unit) {
                                            detectTapGestures {
                                                selectedLedger = product
                                                showLedgerDialog = true
                                            }
                                        },
                                    elevation = CardDefaults.cardElevation(4.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(containerColor = ghostWhite)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(5.dp)
                                            .fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = product.ledger_name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 2,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                is LedgerDetailsViewModel.LedgerDetailsUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is LedgerDetailsViewModel.LedgerDetailsUiState.Error ->
                    Text("Error: ${state.message}", modifier = Modifier.padding(16.dp))
            }
        }
    }

    // Dialogs
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selected = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        selectedDate = selected
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showClearDialog) {
        ClearDialog(
            onDismiss = { showClearDialog = false },
            onConfirm = {
                viewModel.clear()
                entriesState.clear()
                showClearDialog = false
            }
        )
    }

    if (showSuccess) {
        SuccessDialogWithButton(
            title = "Saved",
            description = "Day entries saved successfully",
            paddingValues = PaddingValues(0.dp),
            onClick = {
                viewModel.clear()
                entriesState.clear()
                viewModel.loadData()
                showSuccess = false
            },
        )
    }

    // Ledger popup
    if (showLedgerDialog && selectedLedger != null) {
        LedgerEntryDialog(
            ledger = selectedLedger!!,
            onDismiss = { showLedgerDialog = false },
            onAdd = { remark, amount, isPayment ->
                if (amount <= 0) {
                    scope.launch { snackbarHostState.showSnackbar("Amount must be greater than zero") }
                    return@LedgerEntryDialog
                }
                val id = selectedLedger!!.ledger_id.toLong()
                entriesState[id] = TblLedgerDetailIdRequest(
                    id = id,
                    date = getCurrentDateModern(),
                    party_member = voucher?.voucher_name ?: "",
                    party_id = (otherPartyId ?: partyId).toLong(),
                    member = voucher?.voucher_id.toString(),
                    member_id = entryNo,
                    purpose = remark,
                    amount_in = if (isPayment) 0.0 else amount,
                    amount_out = if (isPayment) amount else 0.0,
                    bill_no = "",
                    time = getCurrentTimeModern(),
                )
                viewModel.addItemToOrder(selectedLedger!!)
                showLedgerDialog = false
            }
        )
    }
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            confirmButton = {
                TextButton(onClick = {
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

    if (showDropdown) {
        AlertDialog(
            onDismissRequest = { showDropdown = false },
            confirmButton = {

            },
            dismissButton = {
                TextButton(onClick = { showDropdown = false }) { Text("Cancel") }
            },
            title = { Text("Select Entry No To Edit") },
            text = {
                StringDropdown(
                    options = ledgerDetails,
                    selectedOption = ledgerDetails.firstOrNull(),
                    onOptionSelected = {
                        navController.navigate("modify_day_entry/{$it}")
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
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
                val id = selectedLedgerDetail!!.id
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

/** Ledger popup for remark, amount, payment/receipt with autofocus on Amount */
@Composable
fun LedgerEntryDialog(
    ledger: TblLedgerDetails,
    onDismiss: () -> Unit,
    onAdd: (remark: String, amount: Double, isPayment: Boolean) -> Unit
) {
    var remark by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isPayment by remember { mutableStateOf(true) }

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
        title = { Text("Add Entry for ${ledger.ledger_name}") },
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


@Composable
fun PaymentModeRow(
    paymentMode: PaymentMode,
    onModeChange: (PaymentMode) -> Unit,
    ledgerList: List<TblLedgerDetails>,
    otherPartyId: Int?,
    onOtherSelected: (Int) -> Unit,
    party: Int?,
    onPartySelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SecondaryGreen)
            .padding(8.dp)
    ) {
        // 🔹 Row for Payment Mode Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PaymentMode.entries.forEach { mode ->
                val isSelected = paymentMode == mode

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) Color.White else Color.Transparent
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            val ledgerName = when (mode) {
                                PaymentMode.CASH -> "CASH"
                                PaymentMode.CARD -> "CARD"
                                PaymentMode.UPI -> "UPI"
                                else -> null
                            }
                            if (ledgerName != null) {
                                val ledger = ledgerList.find { it.ledger_name == ledgerName }
                                onPartySelected(ledger?.ledger_id ?: 1)
                            } else {
                                onPartySelected(party ?: 1)
                            }
                            onModeChange(mode)
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.label,
                        color = if (isSelected) SecondaryGreen else Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 🔹 Ledger Dropdown shown below when "OTHERS" is selected
        if (paymentMode == PaymentMode.OTHERS) {
            Spacer(modifier = Modifier.height(8.dp))
            LedgerDropdown(
                ledgers = ledgerList,
                selectedLedger = ledgerList.find { it.ledger_id == (otherPartyId ?: -1) },
                onLedgerSelected = { onOtherSelected(it.ledger_id) },
                modifier = Modifier.fillMaxWidth(),
                label = "Select Ledger"
            )
        }
    }
}

data class Totals(val debit: Double, val credit: Double)

enum class PaymentMode(val label: String) {
    CASH("CASH"),
    CARD("CARD"),
    UPI("UPI"),
    OTHERS("OTHERS")
}
