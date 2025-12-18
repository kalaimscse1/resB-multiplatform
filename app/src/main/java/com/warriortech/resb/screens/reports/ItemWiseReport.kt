package com.warriortech.resb.screens.reports

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.warriortech.resb.model.ItemReport
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.report.ItemWiseViewModel
import com.warriortech.resb.util.ReportExport
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemWiseReportScreen(
    viewModel: ItemWiseViewModel = hiltViewModel(),
    drawerState: DrawerState,
    sessionManager: SessionManager
) {
    val uiState by viewModel.reportState.collectAsState()
    val context = LocalContext.current

    var fromDateApi by remember { mutableStateOf("") }
    var fromDateUi by remember { mutableStateOf("") }
    var toDateApi by remember { mutableStateOf("") }
    var toDateUi by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    val apiFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val uiFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

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
            if (uiState is ItemWiseReportReportUiState.Success) {
                val bills = (uiState as ItemWiseReportReportUiState.Success).bills
                BottomAppBar(
                    actions = {
                        IconButton(onClick = { ReportExport.itemExportToPdf(context, bills) }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF")
                        }
                        IconButton(onClick = { ReportExport.itemExportToExcel(context, bills) }) {
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
                            Icon(Icons.Default.InsertDriveFile, contentDescription = "View Excel")
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
            // 👉 Date selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = { showFromPicker = true }) {
                    Text(if (fromDateUi.isEmpty()) "From Date" else fromDateUi)
                }
                OutlinedButton(onClick = {
                    if (fromDateApi.isEmpty()) {
                        Toast.makeText(context, "Select From Date first", Toast.LENGTH_SHORT).show()
                    } else {
                        showToPicker = true
                    }
                }) {
                    Text(if (toDateUi.isEmpty()) "To Date" else toDateUi)
                }
            }

            Spacer(Modifier.height(12.dp))

            // 👉 Load Button
            Button(
                onClick = {
                    if (fromDateApi.isNotEmpty() && toDateApi.isNotEmpty()) {
                        viewModel.loadReports(fromDateApi, toDateApi)
                    } else {
                        Toast.makeText(context, "Select both dates", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Load Reports")
            }

            Spacer(Modifier.height(16.dp))

            // 👉 State handling
            when (val state =uiState) {
                is ItemWiseReportReportUiState.Idle -> Text("Please select dates and load reports")

                is ItemWiseReportReportUiState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is ItemWiseReportReportUiState.Success -> {
                    val bills = state.bills
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(bills) { bill ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(
                                        "Item Name: ${bill.menu_item_name}",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Qty: ${bill.qty}")
                                    Text("Rate: ${bill.rate}")
                                    Text("Total: ₹${bill.total}")
                                }
                            }
                        }
                    }
                }

                is ItemWiseReportReportUiState.Error -> {
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
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = { showFromPicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) { Text("Cancel") }
            }
        ) {
            val state = rememberDatePickerState()
            DatePicker(state = state)
            LaunchedEffect(state.selectedDateMillis) {
                state.selectedDateMillis?.let { millis ->
                    val date =
                        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    fromDateApi = apiFormatter.format(date)
                    fromDateUi = uiFormatter.format(date)
                    toDateApi = ""
                    toDateUi = ""
                }
            }
        }
    }

    // ✅ To Date Picker
    if (showToPicker) {
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = { showToPicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) { Text("Cancel") }
            }
        ) {
            val state = rememberDatePickerState()
            DatePicker(state = state)
            LaunchedEffect(state.selectedDateMillis) {
                state.selectedDateMillis?.let { millis ->
                    val date =
                        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    val fromDateParsed = if (fromDateApi.isNotEmpty())
                        LocalDate.parse(fromDateApi, apiFormatter) else null
                    if (fromDateParsed != null && date.isBefore(fromDateParsed)) {
                        Toast.makeText(
                            context,
                            "To Date cannot be before From Date",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        toDateApi = apiFormatter.format(date)
                        toDateUi = uiFormatter.format(date)
                    }
                }
            }
        }
    }
}


sealed class ItemWiseReportReportUiState {
    object Loading : ItemWiseReportReportUiState()
    data class Success(val bills: List<ItemReport>) : ItemWiseReportReportUiState()
    data class Error(val message: String) : ItemWiseReportReportUiState()
    object Idle : ItemWiseReportReportUiState()
}
