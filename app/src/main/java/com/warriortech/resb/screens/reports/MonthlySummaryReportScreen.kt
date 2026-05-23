package com.warriortech.resb.screens.reports

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.report.SummaryReportViewModel
import com.warriortech.resb.util.ReportExport
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
import java.util.*

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MonthlySummaryReportScreen(
    viewModel: SummaryReportViewModel = hiltViewModel(),
    drawerState: DrawerState
) {
    val monthlySummaryState by viewModel.monthlySummary.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    var selectedMonth by remember { mutableStateOf(LocalDate.now().month.name) }
    var selectedYear by remember { mutableStateOf(LocalDate.now().year.toString()) }
    
    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    val months = Month.entries.map { it.name }
    val years = (2020..LocalDate.now().year).map { it.toString() }.reversed()

    LaunchedEffect(selectedMonth, selectedYear) {
        viewModel.fetchMonthlySummary(selectedMonth, selectedYear)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Summary Report", color = SurfaceLight) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = SurfaceLight)
                    }
                },
                actions = {
                    val bills = (monthlySummaryState as? SummaryReportViewModel.SummaryUiState.Success)?.data ?: emptyList()
                    if (bills.isNotEmpty()) {
                        IconButton(onClick = { ReportExport.exportToPdf(context, bills) }) {
                            Icon(Icons.Default.Share, contentDescription = "Export PDF", tint = SurfaceLight)
                        }
                        IconButton(onClick = { ReportExport.exportToExcel(context, bills) }) {
                            Icon(Icons.Default.GridOn, contentDescription = "Export Excel", tint = SurfaceLight)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(SurfaceLight)) {
            // Selectors Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Month Selector
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth().clickable { monthExpanded = true },
                            colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Month", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text(selectedMonth, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                            months.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m) },
                                    onClick = { selectedMonth = m; monthExpanded = false }
                                )
                            }
                        }
                    }

                    // Year Selector
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth().clickable { yearExpanded = true },
                            colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Year", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text(selectedYear, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                            years.forEach { y ->
                                DropdownMenuItem(
                                    text = { Text(y) },
                                    onClick = { selectedYear = y; yearExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            when (val state = monthlySummaryState) {
                is SummaryReportViewModel.SummaryUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryGreen) }
                }
                is SummaryReportViewModel.SummaryUiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        stickyHeader {
                            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState).background(PrimaryGreen).padding(8.dp)) {
                                Text("Bill No", modifier = Modifier.width(100.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Date", modifier = Modifier.width(100.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Customer", modifier = Modifier.width(150.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Amount", modifier = Modifier.width(100.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Tax", modifier = Modifier.width(100.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Discount", modifier = Modifier.width(100.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Total", modifier = Modifier.width(100.dp), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        items(state.data) { bill ->
                            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState).background(Color.White).padding(8.dp)) {
                                Text(bill.bill_no, modifier = Modifier.width(100.dp))
                                Text(bill.bill_date, modifier = Modifier.width(100.dp))
                                Text(bill.customer?.customer_name ?: "Walking Customer", modifier = Modifier.width(150.dp))
                                Text(String.format("%.2f", bill.order_amt), modifier = Modifier.width(100.dp))
                                Text(String.format("%.2f", bill.tax_amt), modifier = Modifier.width(100.dp))
                                Text(String.format("%.2f", bill.disc_amt), modifier = Modifier.width(100.dp))
                                Text(String.format("%.2f", bill.grand_total), modifier = Modifier.width(100.dp))
                            }
                        }
                    }
                }
                is SummaryReportViewModel.SummaryUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: ${state.message}", color = Color.Red) }
                }
                SummaryReportViewModel.SummaryUiState.Empty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No records found", color = Color.Gray) }
                }
            }
        }
    }
}
