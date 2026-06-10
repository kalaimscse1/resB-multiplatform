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
fun GSTRReportScreen(
    viewModel: HsnReportViewModel = hiltViewModel(),
    drawerState: DrawerState
) {
    val gstrDocsState by viewModel.gstrDocs.collectAsState()
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
        viewModel.fetchGSTRDocs(fromDate.format(apiFormatter), toDate.format(apiFormatter))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "GST-R Report",
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

                    // Refresh Button
                    Button(
                        onClick = { viewModel.fetchGSTRDocs(fromDate.format(apiFormatter), toDate.format(apiFormatter)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Refresh", color = Color.White, fontSize = 12.sp)
                    }
                }
            } else {
                // Mobile Portrait Header - Existing design
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
                                        Text(fromDate.format(uiFormatter), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                            OutlinedCard(modifier = Modifier.weight(1f).clickable { isFromDatePicker = false; showDatePicker = true }) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DateRange, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("To", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Text(toDate.format(uiFormatter), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Content Area
            when (val state = gstrDocsState) {
                is HsnReportViewModel.GSTRUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }

                is HsnReportViewModel.GSTRUiState.Success -> {
                    val filteredData = state.data.filter { report ->
                        (report.description ?: "").contains(searchQuery, ignoreCase = true)
                    }

                    if (filteredData.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No reports found", color = Color.Gray)
                        }
                    } else {
                        if (isWideScreen) {
                            GSTRProfessionalTable(data = filteredData)
                        } else {
                            GSTRMobileTable(data = filteredData)
                        }
                    }
                }

                is HsnReportViewModel.GSTRUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Color.Red)
                    }
                }
                
                HsnReportViewModel.GSTRUiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No reports found", color = Color.Gray)
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
private fun GSTRProfessionalTable(data: List<com.warriortech.resb.model.GSTRDOCS>) {
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
            TableHeaderCell("DESCRIPTION", Modifier.weight(2f))
            TableHeaderCell("FROM", Modifier.weight(1f))
            TableHeaderCell("TO", Modifier.weight(1f))
            TableHeaderCell("NOS", Modifier.weight(1f))
            TableHeaderCell("CANCELLED", Modifier.weight(1f))
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
                    TableCell(report.description ?: "", Modifier.weight(2f))
                    TableCell(report.billFrom ?: "", Modifier.weight(1f))
                    TableCell(report.billTo ?: "", Modifier.weight(1f))
                    TableCell(report.nos?.toString() ?: "0", Modifier.weight(1f), textAlign = TextAlign.Center)
                    TableCell(report.cancelled?.toString() ?: "0", Modifier.weight(1f), textAlign = TextAlign.Center)
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GSTRMobileTable(data: List<com.warriortech.resb.model.GSTRDOCS>) {
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
                TableHeaderCell("Description", 200.dp)
                TableHeaderCell("From", 100.dp)
                TableHeaderCell("To", 120.dp)
                TableHeaderCell("NOS", 100.dp)
                TableHeaderCell("Cancelled", 120.dp)
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
                TableCell(report.description ?: "", 200.dp)
                TableCell(report.billFrom ?: "", 100.dp)
                TableCell(report.billTo ?: "", 120.dp)
                TableCell(report.nos?.toString() ?: "0", 100.dp)
                TableCell(report.cancelled?.toString() ?: "0", 120.dp)
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
