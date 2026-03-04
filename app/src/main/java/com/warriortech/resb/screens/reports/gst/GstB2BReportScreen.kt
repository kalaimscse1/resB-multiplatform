package com.warriortech.resb.screens.reports.gst

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.report.gst.HsnReportViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GstB2BReportScreen(
    viewModel: HsnReportViewModel = hiltViewModel(),
    drawerState: DrawerState
) {
    val gstB2BState by viewModel.gstB2BReports.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var fromDate by remember { mutableStateOf(LocalDate.now().minusDays(30)) }
    var toDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isFromDatePicker by remember { mutableStateOf(true) }

    LaunchedEffect(fromDate, toDate) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        viewModel.fetchGstB2BReport(fromDate.format(formatter), toDate.format(formatter))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "GST B2B Report",
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
            // Date Range Selection
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
                                    Text(fromDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")), style = MaterialTheme.typography.bodyMedium)
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
                                    Text(toDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            when (val state = gstB2BState) {
                is HsnReportViewModel.GstB2BState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                is HsnReportViewModel.GstB2BState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        stickyHeader {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(scrollState)
                                    .background(PrimaryGreen)
                                    .padding(8.dp)
                            ) {
                                Text("Customer", modifier = Modifier.width(150.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("GST No", modifier = Modifier.width(120.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Invoice #", modifier = Modifier.width(100.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Date", modifier = Modifier.width(100.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Taxable", modifier = Modifier.width(100.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("CGST", modifier = Modifier.width(80.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("SGST", modifier = Modifier.width(80.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("IGST", modifier = Modifier.width(80.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Total", modifier = Modifier.width(100.dp), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        items(state.data) { report ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(scrollState)
                                    .background(if (state.data.indexOf(report) % 2 == 0) Color(0xFFF9F9F9) else Color.White)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(report.customer_name, modifier = Modifier.width(150.dp))
                                Text(report.gst_no, modifier = Modifier.width(120.dp))
                                Text(report.invoice_number, modifier = Modifier.width(100.dp))
                                Text(report.invoice_date, modifier = Modifier.width(100.dp))
                                Text(String.format("%.2f", report.taxable_value), modifier = Modifier.width(100.dp))
                                Text(String.format("%.2f", report.cgst), modifier = Modifier.width(80.dp))
                                Text(String.format("%.2f", report.sgst), modifier = Modifier.width(80.dp))
                                Text(String.format("%.2f", report.igst), modifier = Modifier.width(80.dp))
                                Text(String.format("%.2f", report.invoice_value), modifier = Modifier.width(100.dp))
                            }
                        }
                    }
                }
                is HsnReportViewModel.GstB2BState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = Color.Red)
                    }
                }
                HsnReportViewModel.GstB2BState.Empty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No data available", color = Color.Gray)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        if (isFromDatePicker) fromDate = selectedDate else toDate = selectedDate
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}
