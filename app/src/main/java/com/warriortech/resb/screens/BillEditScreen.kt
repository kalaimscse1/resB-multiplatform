package com.warriortech.resb.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.ui.components.ModernDivider
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.report.PaidBillsViewModel
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.util.CurrencySettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillEditScreen(
    navController: NavHostController,
    viewModel: PaidBillsViewModel = hiltViewModel(),
    billNo: String
) {
    val selectedBill by viewModel.selectedBill.collectAsStateWithLifecycle()
    val billItems by viewModel.billedItems.collectAsStateWithLifecycle()
    
    val orderAmt by viewModel.orderAmt.collectAsStateWithLifecycle()
    val taxAmt by viewModel.taxAmt.collectAsStateWithLifecycle()
    val grandTotal by viewModel.grandTotal.collectAsStateWithLifecycle()
    val cash by viewModel.cash.collectAsStateWithLifecycle()
    val card by viewModel.card.collectAsStateWithLifecycle()
    val upi by viewModel.upi.collectAsStateWithLifecycle()
    val discount by viewModel.discount.collectAsStateWithLifecycle()
    val due by viewModel.due.collectAsStateWithLifecycle()

    LaunchedEffect(billNo) {
        viewModel.selectBill(billNo)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Bill #$billNo", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearSelection()
                        navController.navigateUp()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        }
    ) { paddingValues ->
        if (selectedBill == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(SurfaceLight),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Bill Summary Card ---
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Bill Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                            ModernDivider(modifier = Modifier.padding(vertical = 8.dp))
                            BillInfoRow("Sub Total", CurrencySettings.format(orderAmt))
                            BillInfoRow("Tax Total", CurrencySettings.format(taxAmt))
                            BillInfoRow("Discount", CurrencySettings.format(discount))
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Grand Total", fontWeight = FontWeight.Bold)
                                Text(CurrencySettings.format(grandTotal), fontWeight = FontWeight.Bold, color = PrimaryGreen)
                            }
                            if (due > 0) {
                                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Remaining Due", color = Color.Red, fontWeight = FontWeight.Bold)
                                    Text(CurrencySettings.format(due), color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // --- Payment Modes Card ---
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Payment Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PaymentEditField("Cash", cash, Modifier.weight(1f)) { viewModel.updatePaymentAmounts(it, card, upi, discount) }
                                PaymentEditField("Card", card, Modifier.weight(1f)) { viewModel.updatePaymentAmounts(cash, it, upi, discount) }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PaymentEditField("UPI", upi, Modifier.weight(1f)) { viewModel.updatePaymentAmounts(cash, card, it, discount) }
                                PaymentEditField("Discount", discount, Modifier.weight(1f)) { viewModel.updatePaymentAmounts(cash, card, upi, it) }
                            }
                        }
                    }
                }

                // --- Billed Items ---
                item {
                    Text("Billed Items", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                }

                items(billItems.toList()) { (item, qty) ->
                    ItemRow(
                        menuItem = item,
                        quantity = qty,
                        onQuantityChange = { newQty -> viewModel.updateItemQuantity(item, newQty) },
                        onRemoveItem = { viewModel.removeItem(item) }
                    )
                }

                // --- Action Button ---
                item {
                    Button(
                        onClick = {
                            viewModel.updateBill(billNo)
                            navController.navigateUp()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save All Changes", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentEditField(label: String, value: Double, modifier: Modifier = Modifier, onValueChange: (Double) -> Unit) {
    OutlinedTextField(
        value = if (value == 0.0) "" else value.toString(),
        onValueChange = { str ->
            val num = str.toDoubleOrNull() ?: 0.0
            onValueChange(num)
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
        singleLine = true
    )
}

@Composable
fun BillInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ItemRow(
    menuItem: TblMenuItemResponse,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onRemoveItem: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(menuItem.menu_item_name, fontWeight = FontWeight.Bold)
                Text(CurrencySettings.format(menuItem.actual_rate), style = MaterialTheme.typography.bodySmall)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (quantity > 1) onQuantityChange(quantity - 1) }) {
                    Icon(Icons.Default.Remove, "Decrease", tint = Color.Gray)
                }
                Text(quantity.toString(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = { onQuantityChange(quantity + 1) }) {
                    Icon(Icons.Default.Add, "Increase", tint = PrimaryGreen)
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onRemoveItem) {
                    Icon(Icons.Default.Delete, "Remove", tint = Color.Red)
                }
            }
        }
    }
}
