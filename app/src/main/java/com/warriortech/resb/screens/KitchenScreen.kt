package com.warriortech.resb.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.warriortech.resb.model.KitchenKOT
import com.warriortech.resb.model.KOTStatus
import com.warriortech.resb.ui.viewmodel.KitchenViewModel
import androidx.compose.material3.DrawerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenScreen(
    navController: NavController,
    drawerState: DrawerState,
    viewModel: KitchenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    // Auto-refresh every 30 seconds
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(30000)
        viewModel.loadKOTs()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kitchen Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { viewModel.loadKOTs() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.height(30.dp)
            ) {
                Text("Refresh")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Filter Tabs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(KOTStatus.values()) { status ->
                FilterChip(
                    onClick = { viewModel.setFilter(status) },
                    label = {
                        Text(
                            text = "${status.name} (${uiState.kots.count { it.status == status }})",
                            color = if (selectedFilter == status) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    selected = selectedFilter == status,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error handling
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Loading state
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // KOT List
            val filteredKOTs = viewModel.getFilteredKOTs()

            if (filteredKOTs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Kitchen,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No ${selectedFilter.name.lowercase()} KOTs",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredKOTs) { kot ->
                        KOTCard(
                            kot = kot,
                            onStatusUpdate = { newStatus ->
                                viewModel.updateKOTStatus(kot.kotId, newStatus)
                            }
                        )
                    }
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
        modifier = Modifier
            .fillMaxWidth()
            .border(
                2.dp,
                when (kot.status) {
                    KOTStatus.PENDING -> Color.Red
                    KOTStatus.IN_PROGRESS -> Color.Blue
                    KOTStatus.READY -> Color.Green
                    KOTStatus.SERVED -> Color.Gray
                },
                RoundedCornerShape(8.dp)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${kot.tableNumber} • ${kot.orderType}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(status = kot.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Order time and waiter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = kot.orderTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                kot.waiterName?.let { waiter ->
                    Text(
                        text = "Waiter: $waiter",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items
            Column {
                kot.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.itemName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            if (item.modifiers.isNotEmpty()) {
                                Text(
                                    text = "• ${item.modifiers.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            item.specialInstructions?.let { instructions ->
                                Text(
                                    text = "Note: $instructions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }

                        Text(
                            text = "×${item.quantity}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (kot.status) {
                    KOTStatus.PENDING -> {
                        Button(
                            onClick = { onStatusUpdate(KOTStatus.IN_PROGRESS) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Start Cooking")
                        }
                    }

                    KOTStatus.IN_PROGRESS -> {
                        Button(
                            onClick = { onStatusUpdate(KOTStatus.READY) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Green
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Ready")
                        }
                    }

                    KOTStatus.READY -> {
                        Button(
                            onClick = { onStatusUpdate(KOTStatus.SERVED) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Served")
                        }
                    }

                    KOTStatus.SERVED -> {
                        // No actions for served items
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: KOTStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        KOTStatus.PENDING -> Triple(Color.Red.copy(alpha = 0.1f), Color.Red, "PENDING")
        KOTStatus.IN_PROGRESS -> Triple(Color.Blue.copy(alpha = 0.1f), Color.Blue, "COOKING")
        KOTStatus.READY -> Triple(Color.Green.copy(alpha = 0.1f), Color.Green, "READY")
        KOTStatus.SERVED -> Triple(Color.Gray.copy(alpha = 0.1f), Color.Gray, "SERVED")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}
