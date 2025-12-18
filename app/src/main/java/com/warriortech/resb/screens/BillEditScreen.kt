package com.warriortech.resb.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.ui.components.ModernDivider
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.report.PaidBillsViewModel
import com.warriortech.resb.model.TblBillingRequest
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.util.CurrencySettings
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillEditScreen(
    navController: NavHostController,
    viewModel: PaidBillsViewModel = hiltViewModel(),
    billNo: String
) {
    val selectedBill by viewModel.selectedBill.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var editedNote by remember { mutableStateOf("") }
    var editedDiscountAmt by remember { mutableStateOf("") }
    var editedOthersAmt by remember { mutableStateOf("") }
    val editableItems by viewModel.editable.collectAsStateWithLifecycle()
    val billItems by viewModel.billedItems.collectAsStateWithLifecycle()


    LaunchedEffect(billNo) {
        viewModel.selectBill(billNo)
    }

    LaunchedEffect(selectedBill) {
        selectedBill?.let { bill ->
            editedNote = bill.note
            editedDiscountAmt = bill.disc_amt.toString()
            editedOthersAmt = bill.others.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Bill #$billNo") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearSelection()
                        navController.navigateUp()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (selectedBill == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No Bill found")
            }
        } else {
            selectedBill?.let { bill ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(SurfaceLight),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // --- Bill Info ---
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                Text(
                                    "Bill Information",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )
                                ModernDivider(modifier = Modifier.padding(vertical = 8.dp))
                                BillInfoRow("Bill Number", bill.bill_no)
                                BillInfoRow("Date", "${bill.bill_date} ${bill.bill_create_time}")
                                BillInfoRow("Customer", bill.customer.customer_name)
                                BillInfoRow("Staff", bill.staff.staff_name)
                                BillInfoRow("Order Amount", CurrencySettings.format(bill.order_amt))
                                BillInfoRow("Tax Amount", CurrencySettings.format(bill.tax_amt))
                                BillInfoRow("Grand Total", CurrencySettings.format(bill.grand_total))
                            }
                        }
                    }

                    // --- Billed Items ---
                    item {
                        Text(
                            "Billed Items",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                        ModernDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    items(billItems.toList()) { (item, _) ->
                        ItemRow(
                            menuItem = item,
                            quantity = item.qty,
                            onQuantityChange = { newQty ->
                                viewModel.updateItemQuantity(item, newQty)
                            },
                            onRemoveItem = { viewModel.removeItem(item) }
                        )
                    }

                    // --- Save Button ---
                    item {
                        Button(
                            onClick = {
                                viewModel.updateBill(bill.bill_no)
                                navController.navigateUp()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Text("Update Changes", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BillInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ItemRow(
    menuItem: TblMenuItemResponse,
    quantity: Int,
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
