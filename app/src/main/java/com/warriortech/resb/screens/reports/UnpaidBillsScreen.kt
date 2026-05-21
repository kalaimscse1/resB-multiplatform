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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.TblBillingResponse
import com.warriortech.resb.ui.components.ModernDivider
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.report.UnpaidBillsUiState
import com.warriortech.resb.ui.viewmodel.report.UnpaidBillsViewModel
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.getDeviceInfo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnpaidBillsScreen(
    navController: NavHostController,
    viewModel: UnpaidBillsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var fromDate by remember { mutableStateOf(LocalDate.now()) }
    var toDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isFromDatePicker by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    val apiFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val uiFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    LaunchedEffect(fromDate, toDate) {
        viewModel.loadUnpaidBills(fromDate.format(apiFormatter), toDate.format(apiFormatter))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unpaid Bills") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
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
                    val filteredBillsForTotal = if (uiState is UnpaidBillsUiState.Success) {
                        (uiState as UnpaidBillsUiState.Success).bills.filter { bill ->
                            bill.bill_no.contains(searchQuery, ignoreCase = true) ||
                                    (bill.customer.customer_name ?: "").contains(searchQuery, ignoreCase = true)
                        }
                    } else emptyList()
                    val totalDue = filteredBillsForTotal.sumOf { it.pending_amt }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Rs. ", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            String.format(Locale.US, "%.2f", totalDue),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFF1A237E),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Refresh Button
                    Button(
                        onClick = { viewModel.loadUnpaidBills(fromDate.format(apiFormatter), toDate.format(apiFormatter)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Refresh", color = Color.White, fontSize = 12.sp)
                    }
                }
            } else {
                // Mobile Portrait Header - Keep Existing Design
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Date Range",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        isFromDatePicker = true
                                        showDatePicker = true
                                    },
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("From", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text(fromDate.format(uiFormatter), fontWeight = FontWeight.Medium)
                                }
                            }

                            OutlinedCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        isFromDatePicker = false
                                        showDatePicker = true
                                    },
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("To", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text(toDate.format(uiFormatter), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            // Bills List Area
            when (val state = uiState) {
                is UnpaidBillsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }

                is UnpaidBillsUiState.Success -> {
                    val filteredBills = state.bills.filter { bill ->
                        bill.bill_no.contains(searchQuery, ignoreCase = true) ||
                                (bill.customer.customer_name ?: "").contains(searchQuery, ignoreCase = true)
                    }

                    if (filteredBills.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No unpaid bills found", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                        }
                    } else {
                        if (isWideScreen) {
                            // Professional Table View for wide screens
                            UnpaidBillsTable(
                                bills = filteredBills,
                                onPayClick = { bill ->
                                    navController.navigate("payment_screen/${bill.due}/${bill.order_master.order_master_id}/${bill.bill_no}/${bill.customer.customer_id}/${"DUE"}")
                                }
                            )
                        } else {
                            // Card View for mobile screens
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredBills) { bill ->
                                    UnpaidBillCard(
                                        bill = bill,
                                        onPayClick = {
                                            navController.navigate("payment_screen/${bill.due}/${bill.order_master.order_master_id}/${bill.bill_no}/${bill.customer.customer_id}/${"DUE"}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                is UnpaidBillsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadUnpaidBills(fromDate.format(apiFormatter), toDate.format(apiFormatter)) }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is UnpaidBillsUiState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
            }
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
}


@Composable
fun UnpaidBillsTable(
    bills: List<TblBillingResponse>,
    onPayClick: (TblBillingResponse) -> Unit
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
            TableHeaderCell("ORDER NUMBER", 150.dp)
            TableHeaderCell("ORDER DATE", 150.dp)
            TableHeaderCell("CUSTOMER NAME", 200.dp)
            TableHeaderCell("DUE AMOUNT", 150.dp)
            TableHeaderCell("VIEW", 100.dp)
            TableHeaderCell("PAYMENT", 100.dp)
        }

        // Table Body
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(bills) { bill ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .background(Color.White)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(bill.bill_no, 150.dp)
                    TableCell(bill.bill_date, 150.dp)
                    TableCell(bill.customer.customer_name ?: "Walk-in Customer", 200.dp)
                    TableCell(String.format(Locale.US, "%.2f", bill.pending_amt), 150.dp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                    
                    Box(modifier = Modifier.width(100.dp), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { /* View Logic */ }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Visibility, "View", tint = Color.Gray)
                        }
                    }
                    Box(modifier = Modifier.width(100.dp), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { onPayClick(bill) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Payment, "Payment", tint = PrimaryGreen)
                        }
                    }
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
        }
    }
}


@Composable
fun UnpaidBillCard(
    bill: TblBillingResponse,
    onPayClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bill #${bill.bill_no}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = bill.bill_date,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Surface(
                    color = Color.Red.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "UNPAID",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            ModernDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Customer",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = bill.customer.customer_name ?: "Walk-in Customer",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Amount Due",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = CurrencySettings.format(bill.pending_amt),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onPayClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pay Now")
                }
            }
        }
    }
}
