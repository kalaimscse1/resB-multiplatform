package com.warriortech.resb.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.warriortech.resb.model.KitchenKOT
import com.warriortech.resb.model.KOTStatus
import com.warriortech.resb.ui.viewmodel.KitchenViewModel
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenScreen(
    navController: NavController,
    drawerState: DrawerState,
    viewModel: KitchenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current

    // Auto-refresh logic
    LaunchedEffect(Unit) {
        while(true) {
            viewModel.loadKOTs()
            kotlinx.coroutines.delay(30000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kitchen Dashboard", color = SurfaceLight) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = SurfaceLight)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadKOTs() }) {
                        Icon(Icons.Default.Restaurant, contentDescription = "Refresh", tint = SurfaceLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Status Filter Tabs
            when(val state = uiState){
                is KitchenViewModel.KitchenUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                is KitchenViewModel.KitchenUiState.Success -> {
                    val kotItems = state.kots
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 2.dp,
                        color = Color.White
                    ) {
                        LazyRow(
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(KOTStatus.values()) { status ->
                                FilterChip(
                                    onClick = { viewModel.setFilter(status) },
                                    label = {
                                        Text(
                                            text = "${status.name} (${kotItems.count { it.status == status }})",
                                        )
                                    },
                                    selected = selectedFilter == status,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White,
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                    if (state.kots.isEmpty() && uiState !is KitchenViewModel.KitchenUiState.Loading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryGreen)
                        }
                    } else {
                        val filteredKOTs = state.kots.filter { it.status == selectedFilter }

                        if (filteredKOTs.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Kitchen, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                                    Spacer(Modifier.height(16.dp))
                                    Text("No ${selectedFilter.name.lowercase()} KOTs", color = Color.Gray)
                                }
                            }
                        } else {
                            // Responsive Grid based on device width
                            val gridColumns = when {
                                configuration.screenWidthDp < 600 -> 1 // Mobile
                                configuration.screenWidthDp < 900 -> 2 // Tablet
                                else -> 3 // Android TV / Large Tablet
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(gridColumns),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredKOTs) { kot ->
                                    KOTCard(
                                        kot = kot,
                                        onStatusUpdate = { newStatus ->
                                            viewModel.updateKOTStatus(kot.kotNumber.toInt(), newStatus)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                is KitchenViewModel.KitchenUiState.Error -> {
                    val error = state.message
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        action = {
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text("Dismiss", color = Color.White)
                            }
                        }
                    ) { Text(error) }
                }
            }
        }
    }
}

@Composable
fun KOTCard(
    kot: KitchenKOT,
    onStatusUpdate: (KOTStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "KOT #${kot.kotNumber}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Text(
                        text = "${kot.tableNumber} • ${kot.orderType}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                StatusBadge(status = kot.status)
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Time and Waiter
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(kot.orderTime, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                kot.waiterName?.let {
                    Text("Waiter: $it", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Items List
            Column {
                kot.items.forEach { item ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.itemName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "×${item.quantity}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }
                        if (item.addOns.isNotEmpty()) {
                            val extras = ( item.addOns).joinToString(", ")
                            Text(
                                text = "Extras: $extras",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (kot.status) {
                    KOTStatus.PENDING -> {
                        Button(
                            onClick = { onStatusUpdate(KOTStatus.IN_PROGRESS) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) { Text("Start Cooking") }
                    }
                    KOTStatus.IN_PROGRESS -> {
                        Button(
                            onClick = { onStatusUpdate(KOTStatus.COMPLETED) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) { Text("Mark Ready") }
                    }
                    KOTStatus.COMPLETED -> {
                        Button(
                            onClick = { onStatusUpdate(KOTStatus.COMPLETED) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                        ) { Text("Mark Served") }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: KOTStatus) {
    val color = when (status) {
        KOTStatus.PENDING -> Color.Red
        KOTStatus.IN_PROGRESS -> Color.Blue
        KOTStatus.COMPLETED -> Color(0xFF4CAF50)
        KOTStatus.CANCELLED -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
