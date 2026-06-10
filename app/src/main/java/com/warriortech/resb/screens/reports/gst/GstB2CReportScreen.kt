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
fun GstB2CReportScreen(
    viewModel: HsnReportViewModel = hiltViewModel(),
    drawerState: DrawerState
) {
    val gstB2CState by viewModel.gstB2CReports.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val today = LocalDate.now()
    val apiFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val uiFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    var fromDate by remember { mutableStateOf(today.minusDays(30)) }
    var toDate by remember { mutableStateOf(today) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isFromDatePicker by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(fromDate, toDate) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        viewModel.fetchGstB2CReport(fromDate.format(formatter), toDate.format(formatter))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "GST B2C Report",
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
                // Wide Screen Header Design matching HsnReportScreen
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

                    // Total Tax Calculation based on filtered results
                    val filteredData = if (gstB2CState is HsnReportViewModel.GstB2CState.Success) {
                        (gstB2CState as HsnReportViewModel.GstB2CState.Success).data.filter { report ->
                            report.tax_percentage.toString().contains(searchQuery, ignoreCase = true) ||
                                    (report.type ?: "B2C").contains(searchQuery, ignoreCase = true)
                        }
                    } else emptyList()
                    val totalTax = filteredData.sumOf { it.tax_amount }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Rs. ", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            String.format(Locale.US, "%.2f", totalTax),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFF1A237E),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Refresh Button
                    Button(
                        onClick = { viewModel.fetchGstB2CReport(fromDate.format(apiFormatter), toDate.format(apiFormatter)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Refresh", color = Color.White, fontSize = 12.sp)
                    }
                }
            } else {
                // Existing Mobile Portrait Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                                colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = PrimaryGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("From", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Text(fromDate.format(uiFormatter), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                            OutlinedCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        isFromDatePicker = false
                                        showDatePicker = true
                                    },
                                colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = PrimaryGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("To", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Text(toDate.format(uiFormatter), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            when (val state = gstB2CState) {
                is HsnReportViewModel.GstB2CState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                is HsnReportViewModel.GstB2CState.Success -> {
                    val filteredData = state.data.filter { report ->
                        report.tax_percentage.toString().contains(searchQuery, ignoreCase = true) ||
                                (report.type ?: "B2C").contains(searchQuery, ignoreCase = true)
                    }

                    if (filteredData.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No B2C reports found", color = Color.Gray)
                        }
                    } else {
                        if (isWideScreen) {
                            GstB2CProfessionalTable(data = filteredData)
                        } else {
                            GstB2CMobileTable(data = filteredData)
                        }
                    }
                }
                is HsnReportViewModel.GstB2CState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = Color.Red)
                    }
                }
                HsnReportViewModel.GstB2CState.Empty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No data available", color = Color.Gray)
                    }
                }
            }
        }
    }

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
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
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
private fun GstB2CProfessionalTable(data: List<com.warriortech.resb.model.ReportGstB2CResponse>) {
    val headerColor = Color(0xFF505F79)

    Column(Modifier.fillMaxSize()) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerColor)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("TAX %", Modifier.weight(1f))
            TableHeaderCell("TAX AMOUNT", Modifier.weight(1.5f))
            TableHeaderCell("CESS", Modifier.weight(1f))
            TableHeaderCell("TYPE", Modifier.weight(1f))
        }

        // Table Body
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(data) { report ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell("${report.tax_percentage}%", Modifier.weight(1f), textAlign = TextAlign.Start)
                    TableCell(String.format(Locale.US, "%.2f", report.tax_amount), Modifier.weight(1.5f), textAlign = TextAlign.Start)
                    TableCell(String.format(Locale.US, "%.2f", report.cess), Modifier.weight(1f), textAlign = TextAlign.Start)
                    TableCell(report.type ?: "B2C", Modifier.weight(1f))
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GstB2CMobileTable(data: List<com.warriortech.resb.model.ReportGstB2CResponse>) {
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
                TableHeaderCell("Tax %", 100.dp)
                TableHeaderCell("Tax Amount", 150.dp)
                TableHeaderCell("Cess", 100.dp)
                TableHeaderCell("Type", 100.dp)
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
                TableCell("${report.tax_percentage}%", 100.dp)
                TableCell(String.format(Locale.US, "%.2f", report.tax_amount), 150.dp)
                TableCell(String.format(Locale.US, "%.2f", report.cess), 100.dp)
                TableCell(report.type ?: "B2C", 100.dp)
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, width: Dp) {
    TableHeaderCell(text, Modifier.width(width))
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(horizontal = 8.dp),
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
    TableCell(text, Modifier.width(width), textAlign, fontWeight)
}

@Composable
private fun TableCell(
    text: String,
    modifier: Modifier,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Text(
        text = text,
        modifier = modifier.padding(horizontal = 8.dp),
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        fontWeight = fontWeight
    )
}
