package com.warriortech.resb.screens.reports.gst

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.*
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
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.report.gst.HsnReportViewModel
import com.warriortech.resb.util.getDeviceInfo
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HsnReportScreen(
    viewModel: HsnReportViewModel = hiltViewModel(),
    drawerState: DrawerState
) {
    val hsnReportsState by viewModel.hsnReports.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val today = LocalDate.now()
    val apiFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val uiFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    var fromDate by remember { mutableStateOf(today.minusDays(30)) }
    var toDate by remember { mutableStateOf(today) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isFromDatePicker by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(fromDate, toDate) {
        viewModel.fetchHsnReports(fromDate.format(apiFormatter), toDate.format(apiFormatter))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Hsn Reports",
                        style = MaterialTheme.typography.titleLarge,
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
                // Wide Screen Header Design matching attached image
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
                    val filteredData = if (hsnReportsState is HsnReportViewModel.HsnUiState.Success) {
                        (hsnReportsState as HsnReportViewModel.HsnUiState.Success).data.filter { report ->
                            report.hsn_code.contains(searchQuery, ignoreCase = true) ||
                                    report.item_cat_name.contains(searchQuery, ignoreCase = true)
                        }
                    } else emptyList()
                    val totalAmount = filteredData.sumOf { it.total }

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
                        onClick = { viewModel.fetchHsnReports(fromDate.format(apiFormatter), toDate.format(apiFormatter)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Refresh", color = Color.White, fontSize = 12.sp)
                    }
                }
            } else {
                // Mobile Portrait Header - Date Range Card
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
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DateRange, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("From", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Text(fromDate.format(uiFormatter), fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                            OutlinedCard(modifier = Modifier.weight(1f).clickable { isFromDatePicker = false; showDatePicker = true }) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DateRange, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("To", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Text(toDate.format(uiFormatter), fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Content Area
            when (val state = hsnReportsState) {
                is HsnReportViewModel.HsnUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }

                is HsnReportViewModel.HsnUiState.Success -> {
                    val filteredData = state.data.filter { report ->
                        report.hsn_code.contains(searchQuery, ignoreCase = true) ||
                                report.item_cat_name.contains(searchQuery, ignoreCase = true)
                    }

                    if (filteredData.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No HSN reports found", color = Color.Gray)
                        }
                    } else {
                        if (isWideScreen) {
                            // Professional Table View for wide screens (matching Image 1)
                            HsnReportProfessionalTable(data = filteredData)
                        } else {
                            // Mobile View - Original Table Design
                            HsnReportMobileTable(data = filteredData)
                        }
                    }
                }

                is HsnReportViewModel.HsnUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                
                is HsnReportViewModel.HsnUiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No HSN reports found", color = Color.Gray)
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
                        val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        if (isFromDatePicker) fromDate = selectedDate else toDate = selectedDate
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DateBox(date: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(date, fontSize = 13.sp)
        Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
    }
}

@Composable
private fun HsnReportProfessionalTable(data: List<com.warriortech.resb.model.HsnReport>) {
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
            TableHeaderCell("HSN CODE", 100.dp)
            TableHeaderCell("DESCRIPTION", 200.dp)
            TableHeaderCell("UNIT", 80.dp)
            TableHeaderCell("QTY", 80.dp)
            TableHeaderCell("TOTAL", 100.dp)
            TableHeaderCell("GST %", 80.dp)
            TableHeaderCell("TAXABLE VALUE", 120.dp)
            TableHeaderCell("CGST", 100.dp)
            TableHeaderCell("SGST", 100.dp)
            TableHeaderCell("IGST", 100.dp)
        }

        // Table Body
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(data) { report ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(report.hsn_code, 100.dp)
                    TableCell(report.item_cat_name, 200.dp)
                    TableCell(report.unit_name, 80.dp)
                    TableCell(report.qty.toString(), 80.dp, textAlign = TextAlign.Center)
                    TableCell(String.format(Locale.US, "%.2f", report.total), 100.dp, textAlign = TextAlign.End)
                    TableCell("N/A", 80.dp, textAlign = TextAlign.Center) 
                    TableCell(String.format(Locale.US, "%.2f", report.taxable_value), 120.dp, textAlign = TextAlign.End)
                    TableCell(String.format(Locale.US, "%.2f", report.cgst), 100.dp, textAlign = TextAlign.End)
                    TableCell(String.format(Locale.US, "%.2f", report.sgst), 100.dp, textAlign = TextAlign.End)
                    TableCell(String.format(Locale.US, "%.2f", report.igst), 100.dp, textAlign = TextAlign.End)
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HsnReportMobileTable(data: List<com.warriortech.resb.model.HsnReport>) {
    val scrollState = rememberScrollState()
    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .background(PrimaryGreen)
                    .padding(8.dp)
            ) {
                TableHeaderCell("HSN", 100.dp)
                TableHeaderCell("Description", 200.dp)
                TableHeaderCell("UQC", 80.dp)
                TableHeaderCell("Total Qty", 100.dp)
                TableHeaderCell("Total Value", 120.dp)
                TableHeaderCell("Taxable Value", 120.dp)
                TableHeaderCell("IGST", 100.dp)
                TableHeaderCell("CGST", 100.dp)
                TableHeaderCell("SGST", 100.dp)
            }
        }
        items(data) { report ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .background(if (data.indexOf(report) % 2 == 0) Color(0xFFF9F9F9) else Color.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableCell(report.hsn_code, 100.dp)
                TableCell(report.item_cat_name, 200.dp)
                TableCell(report.unit_name, 80.dp)
                TableCell(report.qty.toString(), 100.dp)
                TableCell(String.format(Locale.US, "%.2f", report.total), 120.dp)
                TableCell(String.format(Locale.US, "%.2f", report.taxable_value), 120.dp)
                TableCell(String.format(Locale.US, "%.2f", report.igst), 100.dp)
                TableCell(String.format(Locale.US, "%.2f", report.cgst), 100.dp)
                TableCell(String.format(Locale.US, "%.2f", report.sgst), 100.dp)
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, width: Dp) {
    Text(
        text = text,
        modifier = Modifier.width(width).padding(horizontal = 8.dp),
        color = Color.White,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun TableCell(
    text: String,
    width: Dp,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Text(
        text = text,
        modifier = Modifier.width(width).padding(horizontal = 8.dp),
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        fontWeight = fontWeight
    )
}
