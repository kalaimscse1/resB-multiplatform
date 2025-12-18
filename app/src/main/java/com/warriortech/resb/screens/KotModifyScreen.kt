package com.warriortech.resb.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomAppBar
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Button
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.KotResponse
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.ui.components.ModernDivider
import com.warriortech.resb.ui.theme.DarkGreen
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SecondaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.report.KotViewModel
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.SuccessDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KotModifyScreen(
    navController: NavHostController,
    viewModel: KotViewModel = hiltViewModel(),
    orderMasterId: String? = null,
    kotResponse: KotResponse? = null
) {
    val uiState = viewModel.kotActionState.collectAsStateWithLifecycle()
    val status by viewModel._kot.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var menuItems by remember { mutableStateOf<TblMenuItemResponse?>(null) }
    var success by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var values by remember { mutableStateOf<PaddingValues>(PaddingValues(0.dp)) }
    var msg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(orderMasterId, kotResponse) {
        when {
            orderMasterId != null && kotResponse != null -> {
                viewModel.loadOrderItems(orderMasterId)
                viewModel.loadKot(kotResponse)
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Kot Modify", color = SurfaceLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
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
            androidx.compose.material3.BottomAppBar(
                containerColor = SecondaryGreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(SecondaryGreen, SecondaryGreen)
                        ),
                        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                    )
            ) {
                Button(
                    onClick = {
                        val res = viewModel.reprint()
                        if (res.data == true) {
                            success = true
                            msg = res.message
                        } else {
                            failed = true
                            msg = res.message
                        }
                        navController.navigate("kot_report")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = DarkGreen
                    )
                ) {
                    Text("Reprint", color = SurfaceLight)
                }
                if (status?.order_status == "RUNNING") {
                    Button(
                        onClick = {
                            scope.launch {
                                val res = viewModel.modify()
                                if (res.data == true) {
                                    success = true
                                    msg = res.message
                                } else {
                                    failed = true
                                    msg = res.message
                                }
                                delay(2000)
                                navController.navigate("kot_report")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = DarkGreen
                        )
                    ) {
                        Text("Modify", color = SurfaceLight)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
//            BottomAppBar(backgroundColor = SecondaryGreen) {
//                Button(
//                    onClick = {
//                        val res = viewModel.reprint()
//                        if (res.data == true) {
//                            success = true
//                            msg = res.message
//                        } else {
//                            failed = true
//                            msg = res.message
//                        }
//                        navController.navigate("kot_report")
//                    },
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(8.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        backgroundColor = DarkGreen
//                    )
//                ) {
//                    Text("Reprint", color = SurfaceLight)
//                }
//                if (status?.order_status == "RUNNING") {
//                    Button(
//                        onClick = {
//                            scope.launch {
//                                val res = viewModel.modify()
//                                if (res.data == true) {
//                                    success = true
//                                    msg = res.message
//                                } else {
//                                    failed = true
//                                    msg = res.message
//                                }
//                                delay(2000)
//                                navController.navigate("kot_report")
//                            }
//                        },
//                        modifier = Modifier
//                            .weight(1f)
//                            .padding(8.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            backgroundColor = DarkGreen
//                        )
//                    ) {
//                        Text("Modify", color = SurfaceLight)
//                    }
//                } else {
//                    Spacer(modifier = Modifier.weight(1f))
//                }
//            }
        }
    ) { paddingValues ->
        values = paddingValues
        Column(modifier = Modifier.padding(paddingValues)) {
            when (val state = uiState.value) {
                is KotViewModel.KotActionState.Idle -> {
                    // Initial state, show nothing or a placeholder
                    Column {
                        Text(
                            "No items toModify.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                is KotViewModel.KotActionState.Processing -> {
                    // Show a loading indicator
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is KotViewModel.KotActionState.Success -> {
                    // Show the loaded data
                    KotItemsContent(
                        items = state.items,
                        uiState = viewModel,
                        onUpdateQuantity = { menuItem, newQuantity ->
                            viewModel.updateItemQuantity(menuItem, newQuantity)
                        },
                        onRemoveItem = { menuItem ->
                            menuItems = menuItem
                            showDialog = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is KotViewModel.KotActionState.Error -> {
                    // Show error message
                    Text("Error: ${state.message}")
                }
            }
        }
    }
    if (showDialog) {
        KotDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                viewModel.removeItem(menuItems!!)
                showDialog = false
            }
        )
    }
    if (success) {
        SuccessDialog(
            title = msg,
            description = msg,
            paddingValues = values
        )
    }
    if (failed) {
        SuccessDialog(
            title = msg,
            description = msg,
            paddingValues = values
        )
    }
}

@Composable
fun KotDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { androidx.compose.material3.Text("Delete Items") },
        text = { androidx.compose.material3.Text("Are you sure you want to Delete Item? ") },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = { onConfirm() },
                enabled = true
            ) {
                Text("Ok")
            }
        },
        dismissButton = {
            androidx.compose.material3.Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun KotItemsContent(
    modifier: Modifier = Modifier,
    items: Map<TblMenuItemResponse, Int>,
    uiState: KotViewModel,
    onUpdateQuantity: (TblMenuItemResponse, Int) -> Unit,
    onRemoveItem: (TblMenuItemResponse) -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Display Billed Items
        if (items.isNotEmpty()) {
            item {
                Text("Items", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }
            items(items.toList()) { (menuItem, quantity) ->
                KotItemRow(
                    menuItem = menuItem,
                    quantity = quantity,
                    tableStatus = "",
                    currencyFormatter = currencyFormatter,
                    onQuantityChange = { newQuantity ->
                        onUpdateQuantity(menuItem, newQuantity)
                    },
                    onRemoveItem = {
                        onRemoveItem(menuItem)
                    }
                )
            }
            item { ModernDivider(modifier = Modifier.padding(vertical = 8.dp)) }
        } else {
            item {
                Text(
                    "No items in the bill.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        // Subtotal
        item {
            EditableBillingRow(
                label = "Subtotal",
                amount = uiState._subtotal.value,
                currencyFormatter = currencyFormatter,
                onValueChange = { newValue ->

                }
            )
        }
        // Tax (Allow editing if needed)
        item {
            EditableBillingRow(
                label = "Tax Amount",
                amount = uiState._taxAmount.value,
                currencyFormatter = currencyFormatter,
                onValueChange = { newValue ->

                }
            )
        }
        if (uiState._cessAmount.value > 0) {
            item {
                EditableBillingRow(
                    label = "Cess Amount",
                    amount = uiState._cessAmount.value,
                    currencyFormatter = currencyFormatter,
                    onValueChange = { newValue ->

                    }
                )
            }
        }
        if (uiState._cessSpecific.value > 0) {
            item {
                EditableBillingRow(
                    label = "Cess Specific",
                    amount = uiState._cessSpecific.value,
                    currencyFormatter = currencyFormatter,
                    onValueChange = { newValue ->

                    }
                )
            }
        }
        // Discount (Allow editing)
//        item {
//            EditableBillingRow(
//                label = "Discount",
//                amount = uiState.discountFlat,
//                currencyFormatter = currencyFormatter
//            )
//        }
        // Total
        item {
            ModernDivider(modifier = Modifier.padding(vertical = 8.dp))
            KotSummaryRow(
                label = "Total Amount",
                amount = uiState._totalAmount.value,
                currencyFormatter = currencyFormatter,
                isTotal = true
            )
        }
    }
}

@Composable
fun KotItemRow(
    menuItem: TblMenuItemResponse,
    quantity: Int,
    tableStatus: String,
    currencyFormatter: NumberFormat,
    onQuantityChange: (Int) -> Unit,
    onRemoveItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = menuItem.menu_item_name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (tableStatus == "AC") {
                    Text(
                        text = "Table Service",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = CurrencySettings.format(menuItem.actual_rate * quantity),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Quantity controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (quantity > 1) {
                                onQuantityChange(quantity - 1)
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease quantity"
                        )
                    }

                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = { onQuantityChange(quantity + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase quantity"
                        )
                    }
                }
                // Remove item button
                IconButton(
                    onClick = onRemoveItem,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove item",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun KotSummaryRow(
    label: String,
    amount: Double,
    currencyFormatter: NumberFormat,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            CurrencySettings.format(amount),
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
    }
}
