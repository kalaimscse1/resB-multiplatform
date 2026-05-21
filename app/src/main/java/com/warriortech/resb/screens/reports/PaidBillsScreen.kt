package com.warriortech.resb.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.TblBillingResponse
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.ui.components.ModernDivider
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.report.PaidBillsUiState
import com.warriortech.resb.ui.viewmodel.report.PaidBillsViewModel
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.getDeviceInfo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaidBillsScreen(
    navController: NavHostController,
    sessionManager: SessionManager,
    viewModel: PaidBillsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val otpState by viewModel.otpState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var billToDelete by remember { mutableStateOf<TblBillingResponse?>(null) }
    var fromDate by remember { mutableStateOf(LocalDate.now()) }
    var toDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isFromDatePicker by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    // OTP related local states
    var showOtpDialog by remember { mutableStateOf(false) }
    var enteredOtp by remember { mutableStateOf("") }
    var billNoToEdit by remember { mutableStateOf("") }

    val apiFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val uiFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    LaunchedEffect(fromDate, toDate) {
        viewModel.loadPaidBills(fromDate.format(apiFormatter), toDate.format(apiFormatter))
    }

    // Show error messages
    LaunchedEffect(uiState) {
        if (uiState is PaidBillsUiState.Error) {
            snackbarHostState.showSnackbar((uiState as PaidBillsUiState.Error).message)
            viewModel.clearError()
        }
    }

    // Handle OTP State changes
    LaunchedEffect(otpState) {
        when (val state = otpState) {
            is PaidBillsViewModel.OtpState.Sent -> showOtpDialog = true
            is PaidBillsViewModel.OtpState.Verified -> {
                showOtpDialog = false
                enteredOtp = ""
                navController.navigate("bill_edit/$billNoToEdit")
                viewModel.clearOtpState()
            }
            is PaidBillsViewModel.OtpState.Error -> snackbarHostState.showSnackbar(state.message)
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paid Bills") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SurfaceLight)
        ) {
            val deviceInfo = getDeviceInfo()
            val isWideScreen = deviceInfo.isTablet || deviceInfo.isLargeTablet || deviceInfo.isLandscape

            if (isWideScreen) {
                // 👉 NEW HEADER DESIGN for Tablets/Landscape
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // From Date Box
                    DateBox(date = fromDate.format(uiFormatter), onClick = { isFromDatePicker = true; showDatePicker = true })
                    
                    Text("To :", style = MaterialTheme.typography.bodyMedium)
                    
                    // To Date Box
                    DateBox(date = toDate.format(uiFormatter), onClick = { isFromDatePicker = false; showDatePicker = true })

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f).height(56.dp),
                        placeholder = { Text("SEARCH", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                    )

                    // Total Amount Calculation based on filtered results
                    val filteredBillsForTotal = if (uiState is PaidBillsUiState.Success) {
                        (uiState as PaidBillsUiState.Success).bills.filter { bill ->
                            bill.bill_no.contains(searchQuery, ignoreCase = true) ||
                                    bill.customer.customer_name.contains(searchQuery, ignoreCase = true)
                        }
                    } else emptyList()
                    val totalAmount = filteredBillsForTotal.sumOf { it.grand_total }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Rs. ", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            String.format(Locale.US, "%.2f", totalAmount),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFF1A237E),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Refresh Button
                    Button(
                        onClick = { viewModel.loadPaidBills(fromDate.format(apiFormatter), toDate.format(apiFormatter)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Refresh", color = Color.White, fontSize = 12.sp)
                    }
                }
            } else {
                // Mobile Portrait Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Date Range", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedCard(modifier = Modifier.weight(1f).clickable { isFromDatePicker = true; showDatePicker = true }) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("From", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text(fromDate.format(uiFormatter), fontWeight = FontWeight.Medium)
                                }
                            }
                            OutlinedCard(modifier = Modifier.weight(1f).clickable { isFromDatePicker = false; showDatePicker = true }) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("To", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text(toDate.format(uiFormatter), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            // Content
            when (val state = uiState) {
                is PaidBillsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }

                is PaidBillsUiState.Success -> {
                    val filteredBills = state.bills.filter { bill ->
                        bill.bill_no.contains(searchQuery, ignoreCase = true) ||
                                bill.customer.customer_name.contains(searchQuery, ignoreCase = true)
                    }

                    if (filteredBills.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No paid bills found", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        if (isWideScreen) {
                            // Professional Table View for wide screens
                            PaidBillsTable(
                                bills = filteredBills,
                                onEdit = { bill ->
                                    if (sessionManager.getUser()?.role == "CASHIER" && sessionManager.getGeneralSetting()?.is_otp == true) {
                                        billNoToEdit = bill.bill_no
                                        viewModel.requestOtpForEdit(bill.bill_no)
                                    } else {
                                        navController.navigate("bill_edit/${bill.bill_no}")
                                    }
                                },
                                onPrint = { viewModel.printBill(it) }
                            )
                        } else {
                            // Card View for mobile screens
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredBills) { bill ->
                                    PaidBillItem(
                                        bill = bill,
                                        onEditClick = {
                                            if (sessionManager.getUser()?.role == "CASHIER" && sessionManager.getGeneralSetting()?.is_otp == true) {
                                                billNoToEdit = bill.bill_no
                                                viewModel.requestOtpForEdit(bill.bill_no)
                                            } else {
                                                navController.navigate("bill_edit/${bill.bill_no}")
                                            }
                                        },
                                        onDeleteClick = { billToDelete = bill; showDeleteDialog = true },
                                        onPrintClick = { viewModel.printBill(it) },
                                        onWhatsappClick = {  viewModel.sendBillViaWhatsApp(it, context) }
                                    )
                                }
                            }
                        }
                    }
                }

                is PaidBillsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {}
            }
        }

        // Date Picker Dialog
        if (showDatePicker) {
            val initialDate = if (isFromDatePicker) fromDate else toDate
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selected = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            if (isFromDatePicker) fromDate = selected else toDate = selected
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Delete confirmation
        if (showDeleteDialog && billToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false; billToDelete = null },
                title = { Text("Delete Bill") },
                text = { Text("Are you sure you want to delete bill ${billToDelete?.bill_no}?") },
                confirmButton = {
                    TextButton(onClick = {
                        billToDelete?.let { viewModel.deleteBill(it.bill_no) }
                        showDeleteDialog = false; billToDelete = null
                    }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false; billToDelete = null }) { Text("Cancel") } }
            )
        }

        // OTP Dialog
        if (showOtpDialog) {
            AlertDialog(
                onDismissRequest = { showOtpDialog = false; viewModel.clearOtpState() },
                title = { Text("Verification Required") },
                text = {
                    Column {
                        Text("An OTP has been sent to the owner's Number. Please enter it to edit this bill.")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = enteredOtp,
                            onValueChange = { if (it.length <= 6) enteredOtp = it },
                            label = { Text("Enter OTP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (otpState is PaidBillsViewModel.OtpState.Sending) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.verifyOtp(enteredOtp) },
                        enabled = enteredOtp.length == 6 && otpState !is PaidBillsViewModel.OtpState.Sending
                    ) { Text("Verify") }
                },
                dismissButton = {
                    TextButton(onClick = { showOtpDialog = false; enteredOtp = ""; viewModel.clearOtpState() }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun DateBox(date: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(date, fontSize = 13.sp)
        Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
    }
}

@Composable
fun PaidBillsTable(
    bills: List<TblBillingResponse>,
    onEdit: (TblBillingResponse) -> Unit,
    onPrint: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val headerColor = Color(0xFF505F79) // Matching the dark gray-blue background from the image

    Column(Modifier.fillMaxSize()) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .background(headerColor)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("BILL NO.", 100.dp)
            TableHeaderCell("BILL DATE", 110.dp)
            TableHeaderCell("TYPE", 90.dp)
            TableHeaderCell("ORDER NO.", 110.dp)
            TableHeaderCell("CUSTOMER NAME", 160.dp)
            TableHeaderCell("PAY MODE", 110.dp)
            TableHeaderCell("AMOUNT", 100.dp)
            TableHeaderCell("GST", 90.dp)
            TableHeaderCell("DISC.", 90.dp)
            TableHeaderCell("OTHERS", 90.dp)
            TableHeaderCell("TOTAL", 100.dp)
            TableHeaderCell("RECEIVED", 100.dp)
            TableHeaderCell("TENDERED", 100.dp)
            TableHeaderCell("CHANGE", 100.dp)
            TableHeaderCell("EDIT", 60.dp)
            TableHeaderCell("PRINT", 60.dp)
        }

        // Table Body
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(bills) { bill ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(bill.bill_no, 100.dp)
                    TableCell(bill.bill_date, 110.dp)
                    val type = when {
                        bill.order_master.is_dine_in -> "DINE IN"
                        bill.order_master.is_take_away -> "TAKE AWAY"
                        bill.order_master.is_delivery -> "DELIVERY"
                        else -> "N/A"
                    }
                    TableCell(type, 90.dp)
                    TableCell(bill.order_master.order_master_id, 110.dp)
                    TableCell(bill.customer.customer_name, 160.dp)
                    
                    val payModes = mutableListOf<String>()
                    if (bill.cash > 0) payModes.add("CASH")
                    if (bill.card > 0) payModes.add("CARD")
                    if (bill.upi > 0) payModes.add("UPI")
                    val payModeText = payModes.joinToString(", ").ifEmpty { "N/A" }
                    TableCell(payModeText, 110.dp)
                    
                    TableCell(String.format(Locale.US, "%.2f", bill.order_amt), 100.dp, textAlign = TextAlign.End)
                    TableCell(String.format(Locale.US, "%.2f", bill.tax_amt), 90.dp, textAlign = TextAlign.End)
                    TableCell(String.format(Locale.US, "%.2f", bill.disc_amt), 110.dp, textAlign = TextAlign.End)
                    TableCell(String.format(Locale.US, "%.2f", bill.others), 90.dp, textAlign = TextAlign.End)
                    TableCell(String.format(Locale.US, "%.2f", bill.grand_total), 100.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                    TableCell(String.format(Locale.US, "%.2f", bill.received_amt), 100.dp, textAlign = TextAlign.End)
                    TableCell(String.format(Locale.US, "%.2f", bill.tendered_amt), 100.dp, textAlign = TextAlign.End)
                    TableCell(String.format(Locale.US, "%.2f", bill.change), 100.dp, textAlign = TextAlign.End)

                    Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { onEdit(bill) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Edit, "Edit", tint = PrimaryGreen)
                        }
                    }
                    Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { onPrint(bill.bill_no) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Print, "Print", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
        }
    }
}


@Composable
fun PaidBillItem(
    bill: TblBillingResponse,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPrintClick: (bill_no: String) -> Unit,
    onWhatsappClick: (bill: TblBillingResponse) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bill #${bill.bill_no}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${bill.bill_date} ${bill.bill_create_time}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "Customer: ${bill.customer.customer_name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onPrintClick(bill.bill_no) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = "Print",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { onWhatsappClick(bill) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Whatsapp,
                            contentDescription = "Whatsapp",
                            tint = Color(0xFF25D366)
                        )
                    }
                    IconButton(
                        onClick = { onEditClick() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = PrimaryGreen
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            ModernDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Order Amount: ${CurrencySettings.format(bill.order_amt)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Tax: ${CurrencySettings.format(bill.tax_amt)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Grand Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = CurrencySettings.format(bill.grand_total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
            }

            if (bill.note.isNotEmpty()) {
                Text(
                    text = "Note: ${bill.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
