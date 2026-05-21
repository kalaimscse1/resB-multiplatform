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

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun YearlySummaryReportScreen(
    viewModel: SummaryReportViewModel = hiltViewModel(),
    drawerState: DrawerState
) {
    val yearlySummaryState by viewModel.yearlySummary.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var selectedYear by remember { mutableStateOf(LocalDate.now().year.toString()) }
    var expanded by remember { mutableStateOf(false) }
    val years = (2020..LocalDate.now().year).map { it.toString() }.reversed()

    LaunchedEffect(selectedYear) {
        viewModel.fetchYearlySummary(selectedYear)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yearly Summary Report", color = SurfaceLight) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = SurfaceLight)
                    }
                },
                actions = {
                    val summaries = (yearlySummaryState as? SummaryReportViewModel.SummaryUiState.Success)?.data ?: emptyList()
                    if (summaries.isNotEmpty()) {
                        IconButton(onClick = { ReportExport.yearlySummaryExportToPdf(context, summaries) }) {
                            Icon(Icons.Default.Share, contentDescription = "Export PDF", tint = SurfaceLight)
                        }
                        IconButton(onClick = { ReportExport.yearlySummaryExportToExcel(context, summaries) }) {
                            Icon(Icons.Default.GridOn, contentDescription = "Export Excel", tint = SurfaceLight)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(SurfaceLight)) {
            // Year Selector Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = PrimaryGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Select Year", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text(selectedYear, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        years.forEach { year ->
                            DropdownMenuItem(

                                text = { Text(year) },
                                onClick = {
                                    selectedYear = year
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            when (val state = yearlySummaryState) {
                is SummaryReportViewModel.SummaryUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryGreen) }
                }
                is SummaryReportViewModel.SummaryUiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        stickyHeader {
                            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState).background(PrimaryGreen).padding(8.dp)) {
                                Text("Month", modifier = Modifier.width(100.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Amount", modifier = Modifier.width(100.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Discount", modifier = Modifier.width(100.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Tax", modifier = Modifier.width(100.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Others", modifier = Modifier.width(100.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Bills", modifier = Modifier.width(80.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Total", modifier = Modifier.width(100.dp), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        items(state.data) { item ->
                            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState).background(Color.White).padding(8.dp)) {
                                Text(item.month ?: "", modifier = Modifier.width(100.dp))
                                Text(String.format("%.2f", item.amount ?: 0.0), modifier = Modifier.width(100.dp))
                                Text(String.format("%.2f", item.discount ?: 0.0), modifier = Modifier.width(100.dp))
                                Text(String.format("%.2f", item.tax ?: 0.0), modifier = Modifier.width(100.dp))
                                Text(String.format("%.2f", item.others ?: 0.0), modifier = Modifier.width(100.dp))
                                Text(item.billCount?.toString() ?: "0", modifier = Modifier.width(80.dp))
                                Text(String.format("%.2f", item.total ?: 0.0), modifier = Modifier.width(100.dp))
                            }
                        }
                    }
                }
                is SummaryReportViewModel.SummaryUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: ${state.message}", color = Color.Red) }
                }
                SummaryReportViewModel.SummaryUiState.Empty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No records found for $selectedYear", color = Color.Gray) }
                }
            }
        }
    }
}
