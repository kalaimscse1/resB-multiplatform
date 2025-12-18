package com.warriortech.resb.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.CircularProgressIndicator
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.warriortech.resb.model.KotResponse
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SecondaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.report.KotViewModel
import com.warriortech.resb.util.CurrencySettings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KotReportScreen(
    viewModel: KotViewModel = hiltViewModel(),
    drawerState: DrawerState,
    onEditClick: (KotResponse) -> Unit
) {
    val kotReports = viewModel.kotReports.collectAsState()
    val scope = rememberCoroutineScope()
    // one shared horizontal scroll state for header + rows
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.material.Text(
                        "Kot Reports",
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
        Column(modifier = Modifier.padding(paddingValues)) {
            when (val state = kotReports.value) {
                is KotViewModel.KotUiState.Loading -> {
                    // Show loading indicator
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is KotViewModel.KotUiState.Success -> {
                    LazyColumn(modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)) {

                        // Sticky Header
                        stickyHeader {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(scrollState)
                                    .background(SecondaryGreen) // keeps header visible
                                    .padding(8.dp)
                            ) {
                                Text(
                                    "KOT",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.width(80.dp)
                                )
                                Text(
                                    "Area",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.width(100.dp)
                                )
                                Text(
                                    "Table",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.width(100.dp)
                                )
                                Text(
                                    "Type",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.width(100.dp)
                                )
                                Text(
                                    "Order No",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.width(140.dp)
                                )
                                Text(
                                    "Total",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.width(100.dp)
                                )
                                Text(
                                    "Status",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.width(100.dp)
                                )
                                Text(
                                    "Action",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.width(100.dp)
                                )
                            }
                        }

                        // Data Rows
                        items(state.kotReports) { kot ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(scrollState) // same scroll state as header
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Text(kot.kot_number.toString(), modifier = Modifier.width(80.dp))
                                Text(kot.area_name, modifier = Modifier.width(100.dp))
                                Text(kot.table_name, modifier = Modifier.width(100.dp))
                                Text(kot.order_type, modifier = Modifier.width(100.dp))
                                Text(kot.order_master_id, modifier = Modifier.width(140.dp))
                                Text(
                                    CurrencySettings.formatPlain(kot.grand_total),
                                    modifier = Modifier.width(100.dp)
                                )
                                Text(kot.order_status, modifier = Modifier.width(100.dp))

                                Button(
                                    onClick = { onEditClick(kot) },
                                    modifier = Modifier.width(100.dp)
                                ) {
                                    Text("Edit")
                                }
                            }
                        }
                    }
                }

                is KotViewModel.KotUiState.Error -> {
                    // Show error message
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


}
