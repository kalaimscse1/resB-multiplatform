package com.warriortech.resb.screens.accounts.reports


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.CircularProgressIndicator
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SecondaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.accounts.DayEntryReportViewmodel
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.LedgerDropdown
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEntryReportScreen(
    viewModel: DayEntryReportViewmodel = hiltViewModel(),
    drawerState: DrawerState,
) {
    val ledgerDetailsState by viewModel.ledgerDetailsState.collectAsStateWithLifecycle()
    val openingBalance by viewModel.openingBalance.collectAsStateWithLifecycle()
    val ledgerList by viewModel.ledgerList.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var id by remember { mutableStateOf<Long>(1) }
    var fromDate by remember { mutableStateOf(LocalDate.now().minusDays(30)) }
    var toDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isFromDatePicker by remember { mutableStateOf(true) }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    LaunchedEffect(fromDate, toDate) {
        viewModel.getOpeningBalance(fromDate.format(formatter),1)
        viewModel.loadData(id, fromDate.format(formatter), toDate.format(formatter))
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.material.Text(
                        "Ledger Reports",
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
        ) {
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
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "From",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = fromDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
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
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "To",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = toDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LedgerDropdown(
                            ledgers = ledgerList,
                            selectedLedger = ledgerList.find { it.ledger_id.toLong() == id },
                            onLedgerSelected = {
                                id = it.ledger_id.toLong()
                                viewModel.loadData(
                                    id,
                                    fromDate.format(formatter),
                                    toDate.format(formatter)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = "Select Ledger"
                        )
                    }
                }
            }
            when (val state = ledgerDetailsState) {
                is DayEntryReportViewmodel.DayEntryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is DayEntryReportViewmodel.DayEntryUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        stickyHeader {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(scrollState)
                                    .background(SecondaryGreen) // keeps header visible
                                    .padding(8.dp)
                            ) {
                                Text(
                                    "DAY ENTRY NO",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.width(140.dp)
                                )
                                Text(
                                    "LEDGER NAME",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.width(160.dp)
                                )
                                Text(
                                    "DATE",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.width(120.dp)
                                )
                                Text(
                                    "TIME",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.width(120.dp)
                                )
                                Text(
                                    "REMARKS",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.width(240.dp)
                                )
                                Text(
                                    "DEBIT",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.width(120.dp)
                                )
                                Text(
                                    "CREDIT",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.width(120.dp)
                                )
                            }
                        }


                        val amountIn = state.ledgers.sumOf { it.amount_in }
                        val amountOut = state.ledgers.sumOf { it.amount_out }
                        val bal = openingBalance["opening_balance"] ?:0.0
                        val close = bal + amountIn - amountOut
                        if (id == 1L) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(scrollState) // same scroll state as header
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                ) {
                                    Text("", modifier = Modifier.width(140.dp))
                                    Text("Opening Balance", modifier = Modifier.width(160.dp))
                                    Text("", modifier = Modifier.width(120.dp))
                                    Text("", modifier = Modifier.width(120.dp))
                                    Text("", modifier = Modifier.width(240.dp), maxLines = 1)
                                    Text(
                                        "",
                                        modifier = Modifier.width(120.dp),
                                        color = Color.Black
                                    )
                                    Text(
                                        CurrencySettings.formatPlain(bal),
                                        modifier = Modifier.width(120.dp),
                                        color = SecondaryGreen
                                    )
                                }
                            }
                        }

                        items(state.ledgers) { ledger ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(scrollState) // same scroll state as header
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Text(ledger.member_id, modifier = Modifier.width(140.dp))
                                Text(ledger.ledger.ledger_name, modifier = Modifier.width(160.dp))
                                Text(ledger.date, modifier = Modifier.width(120.dp))
                                Text(ledger.time, modifier = Modifier.width(120.dp))
                                Text(
                                    ledger.purpose,
                                    modifier = Modifier.width(240.dp),
                                    maxLines = 1
                                )
                                Text(
                                    CurrencySettings.formatPlain(ledger.amount_out),
                                    modifier = Modifier.width(120.dp),
                                    color = if (ledger.amount_out > 0) Color.Red else Color.Black
                                )
                                Text(
                                    CurrencySettings.formatPlain(ledger.amount_in),
                                    modifier = Modifier.width(120.dp),
                                    color = if (ledger.amount_in > 0) SecondaryGreen else Color.Black
                                )
                            }
                        }
                        if (id == 1L) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(scrollState) // same scroll state as header
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                ) {
                                    Text("", modifier = Modifier.width(140.dp))
                                    Text("Closing Balance", modifier = Modifier.width(160.dp))
                                    Text("", modifier = Modifier.width(120.dp))
                                    Text("", modifier = Modifier.width(120.dp))
                                    Text("", modifier = Modifier.width(240.dp), maxLines = 1)
                                    Text(
                                        "",
                                        modifier = Modifier.width(120.dp),
                                        color = Color.Black
                                    )
                                    Text(
                                        CurrencySettings.formatPlain(close),
                                        modifier = Modifier.width(120.dp),
                                        color = SecondaryGreen
                                    )
                                }
                            }
                        }
                    }
                }

                is DayEntryReportViewmodel.DayEntryUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error: ${state.message}", color = Color.Red)
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
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            if (isFromDatePicker) {
                                fromDate = selectedDate
                            } else {
                                toDate = selectedDate
                            }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}