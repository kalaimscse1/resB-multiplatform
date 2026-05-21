package com.warriortech.resb.screens.reports

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
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
import com.warriortech.resb.model.TblBillingResponse
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.report.ReportViewModel
import com.warriortech.resb.util.ReportExport
import com.warriortech.resb.util.getDeviceInfo
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel(),
    drawerState: DrawerState,
    sessionManager: SessionManager
) {
    val uiState by viewModel.reportState.collectAsState()
    val context = LocalContext.current

    val today = LocalDate.now()
    val apiFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val uiFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    var fromDateApi by remember { mutableStateOf(apiFormatter.format(today)) }
    var fromDateUi by remember { mutableStateOf(uiFormatter.format(today)) }
    var toDateApi by remember { mutableStateOf(apiFormatter.format(today)) }
    var toDateUi by remember { mutableStateOf(uiFormatter.format(today)) }
    var searchQuery by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadReports(fromDateApi, toDateApi)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reports", style = MaterialTheme.typography.titleLarge,
                        color = SurfaceLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            Icons.Default.Menu, contentDescription = "Menu",
                            tint = SurfaceLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen
                )
            )
        },
        bottomBar = {
            if (uiState is ReportUiState.Success) {
                val bills = (uiState as ReportUiState.Success).bills
                BottomAppBar(
                    actions = {
                        IconButton(onClick = { ReportExport.exportToPdf(context, bills) }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF")
                        }
                        IconButton(onClick = { ReportExport.exportToExcel(context, bills) }) {
                            Icon(Icons.Default.TableChart, contentDescription = "Export Excel")
                        }
                        IconButton(onClick = {
                            ReportExport.viewReport(context, "SalesReport.pdf", "application/pdf")
                        }) {
                            Icon(Icons.Default.Visibility, contentDescription = "View PDF")
                        }
                        IconButton(onClick = {
                            ReportExport.viewReport(
                                context,
                                "SalesReport.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            )
                        }) {
                            Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = "View Excel")
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val deviceInfo = getDeviceInfo()
            val isWideScreen = deviceInfo.isTablet || deviceInfo.isLargeTablet || deviceInfo.isLandscape

            // 👉 NEW HEADER DESIGN
            if (isWideScreen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // From Date
                    DateBox(date = fromDateUi, onClick = { showFromPicker = true })
                    
                    Text("To :", style = MaterialTheme.typography.bodyMedium)
                    
                    // To Date
                    DateBox(date = toDateUi, onClick = { showToPicker = true })

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f).height(56.dp), // Height standard for OutlinedTextField
                        placeholder = { Text("SEARCH", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                    )

                    // Total Amount
                    val totalAmount = if (uiState is ReportUiState.Success) {
                        (uiState as ReportUiState.Success).bills
                            .filter { bill ->
                                bill.bill_no.contains(searchQuery, ignoreCase = true) ||
                                bill.customer.customer_name.contains(searchQuery, ignoreCase = true)
                            }
                            .sumOf { it.grand_total }
                    } else 0.0

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
                        onClick = { viewModel.loadReports(fromDateApi, toDateApi) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(48.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text("Refresh", color = Color.White, fontSize = 12.sp)
                    }
                }
            } else {
                // Mobile Portrait Header
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DateBox(date = fromDateUi, onClick = { showFromPicker = true }, modifier = Modifier.weight(1f))
                        Text(" To ", modifier = Modifier.padding(horizontal = 4.dp))
                        DateBox(date = toDateUi, onClick = { showToPicker = true }, modifier = Modifier.weight(1f))
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f).height(56.dp),
                            placeholder = { Text("SEARCH", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        Button(
                            onClick = { viewModel.loadReports(fromDateApi, toDateApi) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Refresh", color = Color.White)
                        }
                    }

                    val totalAmount = if (uiState is ReportUiState.Success) {
                        (uiState as ReportUiState.Success).bills
                            .filter { bill ->
                                bill.bill_no.contains(searchQuery, ignoreCase = true) ||
                                bill.customer.customer_name.contains(searchQuery, ignoreCase = true)
                            }
                            .sumOf { it.grand_total }
                    } else 0.0

                    Text(
                        "Total: Rs. ${String.format(Locale.US, "%.2f", totalAmount)}",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF1A237E),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // 👉 State handling
            when (val state = uiState) {
                is ReportUiState.Idle -> Text("Please select dates and load reports")

                is ReportUiState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is ReportUiState.Success -> {
                    val filteredBills = state.bills.filter { bill ->
                        bill.bill_no.contains(searchQuery, ignoreCase = true) ||
                        bill.customer.customer_name.contains(searchQuery, ignoreCase = true)
                    }
                    
                    if (isWideScreen) {
                        ReportTable(bills = filteredBills)
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredBills) { bill ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text("Bill No: ${bill.bill_no}", fontWeight = FontWeight.Bold)
                                        Text("Date: ${bill.bill_date}")
                                        Text("Customer: ${bill.customer.customer_name}")
                                        Text("Total: ₹${String.format(Locale.US, "%.2f", bill.grand_total)}")
                                    }
                                }
                            }
                        }
                    }
                }

                is ReportUiState.Error -> {
                    val message = state.message
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("⚠️ $message", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    // ✅ From Date Picker
    if (showFromPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                LocalDate.parse(fromDateApi, apiFormatter).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (e: Exception) {
                null
            }
        )
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        fromDateApi = apiFormatter.format(date)
                        fromDateUi = uiFormatter.format(date)
                    }
                    showFromPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ✅ To Date Picker
    if (showToPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                LocalDate.parse(toDateApi, apiFormatter).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (e: Exception) {
                null
            }
        )
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        val fromDateParsed = LocalDate.parse(fromDateApi, apiFormatter)
                        if (date.isBefore(fromDateParsed)) {
                            Toast.makeText(context, "To Date cannot be before From Date", Toast.LENGTH_SHORT).show()
                        } else {
                            toDateApi = apiFormatter.format(date)
                            toDateUi = uiFormatter.format(date)
                        }
                    }
                    showToPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun DateBox(date: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(date, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp))
        Icon(
            Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
    }
}

@Composable
fun ReportTable(bills: List<TblBillingResponse>) {
    val scrollState = rememberScrollState()
    val headerColor = Color(0xFF505F79) // Matching the dark gray-blue background from the image

    Column(modifier = Modifier.fillMaxSize()) {
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
            TableHeaderCell("DATE", 120.dp)
            TableHeaderCell("TYPE", 90.dp)
            TableHeaderCell("ORDER NO.", 120.dp)
            TableHeaderCell("CUSTOMER NAME", 180.dp)
            TableHeaderCell("PAY MODE", 110.dp)
            TableHeaderCell("AMOUNT", 100.dp)
            TableHeaderCell("GST", 90.dp)
            TableHeaderCell("DISCOUNT", 110.dp)
            TableHeaderCell("OTHERS", 90.dp)
            TableHeaderCell("TOTAL", 100.dp)
            TableHeaderCell("RECEIVED", 100.dp)
        }

        // Table Data
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
                    TableCell(bill.bill_date, 120.dp)
                    
                    val type = when {
                        bill.order_master.is_dine_in -> "DINE IN"
                        bill.order_master.is_take_away -> "TAKE AWAY"
                        bill.order_master.is_delivery -> "DELIVERY"
                        else -> "N/A"
                    }
                    TableCell(type, 90.dp)
                    TableCell(bill.order_master.order_master_id, 120.dp)
                    TableCell(bill.customer.customer_name, 180.dp)
                    
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
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun TableHeaderCell(text: String, width: Dp) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 8.dp),
        color = Color.White,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun TableCell(
    text: String,
    width: Dp,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 8.dp),
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        fontWeight = fontWeight
    )
}

sealed class ReportUiState {
    object Loading : ReportUiState()
    data class Success(val bills: List<TblBillingResponse>) : ReportUiState()
    data class Error(val message: String) : ReportUiState()
    object Idle : ReportUiState()
}
