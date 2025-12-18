package com.warriortech.resb.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.ui.components.MobileOptimizedButton
import com.warriortech.resb.ui.viewmodel.payment.BillingViewModel
import com.warriortech.resb.ui.viewmodel.payment.BillingPaymentUiState
import java.text.NumberFormat
import java.util.Locale
import com.warriortech.resb.model.TblOrderDetailsResponse
import com.warriortech.resb.ui.components.ModernDivider
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SecondaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.util.CurrencySettings
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType


@Composable
fun KotSelectionDialog(
    orderDetails: List<TblOrderDetailsResponse>,
    onKotSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val kotNumbers = orderDetails.map { it.kot_number }.distinct().sorted()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            MobileOptimizedButton(
                onClick = onDismiss,
                text = "Cancel",
                modifier = Modifier.fillMaxWidth()
            )
        },
        title = {
            Text("Select KOT Number", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column {
                Text("Choose a KOT to view its items:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    item {
                        MobileOptimizedButton(
                            onClick = {
                                onKotSelected(-1) // -1 means show all items
                            },
                            text = "Show All Items",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(kotNumbers) { kotNumber ->
                        MobileOptimizedButton(
                            onClick = { onKotSelected(kotNumber) },
                            text = "KOT #$kotNumber",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    navController: NavHostController,
    viewModel: BillingViewModel = hiltViewModel(),
    orderDetailsResponse: List<TblOrderDetailsResponse>? = null,
    orderMasterId: String? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedKotNumber by remember { mutableStateOf<Int?>(null) }
    var showKotSelectionDialog by remember { mutableStateOf(false) }
    val orderDetails by viewModel._originalOrderDetails.collectAsStateWithLifecycle()
    var previewDialog by remember { mutableStateOf(false) }
    val preview by viewModel.preview.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = orderDetailsResponse, key2 = orderMasterId) {
        when {
            orderDetailsResponse != null && orderMasterId != null -> {
                viewModel.setBillingDetailsFromOrderResponse(orderDetailsResponse, orderMasterId)
            }
        }
    }

    if (showKotSelectionDialog && orderDetailsResponse != null) {
        KotSelectionDialog(
            orderDetails = orderDetailsResponse,
            onKotSelected = { kotNumber ->
                selectedKotNumber = kotNumber
                showKotSelectionDialog = false
                viewModel.filterByKotNumber(kotNumber)
            },
            onDismiss = { showKotSelectionDialog = false }
        )
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedKotNumber != null) "Bill Summary - KOT #$selectedKotNumber"
                        else "Bill Summary", color = SurfaceLight
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
                actions = {
                    IconButton(
                        onClick = {
                            previewDialog = true
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = "Print",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen
                )
            )
        },
        bottomBar = {
            BillingBottomBar(uiState = uiState, orderMasterId = orderMasterId) {
                navController.navigate("payment_screen/${uiState.totalAmount}/${uiState.orderMasterId}/${"--"}/${0L}/${""}") {
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    ) { paddingValues ->
        BillingContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onUpdateQuantity = { menuItem, newQuantity ->
                viewModel.updateItemQuantity(menuItem, newQuantity)
            },
            onRemoveItem = { menuItem ->
                viewModel.removeItem(menuItem)
            },
            orderDetails = orderDetails,
            onDiscountChange = { discount ->
                viewModel.updateDiscountFlat(discount)
            },
            onOtherChargesChange = { charges ->
                viewModel.updateOtherCharges(charges)
            }
        )

    }
    if (previewDialog){
        PreviewBillDialog(
            preview = preview!!,
            onDismiss = { previewDialog = false }
        )
    }
}

@Composable
fun PreviewBillDialog(
    preview: Bitmap,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            MobileOptimizedButton(
                onClick = onDismiss,
                text = "Close",
                modifier = Modifier.fillMaxWidth()
            )
        },
        title = {
            Text("Bill Preview", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    bitmap = preview.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
@Composable
fun BillingContent(
    modifier: Modifier = Modifier,
    uiState: BillingPaymentUiState,
    onUpdateQuantity: (TblMenuItemResponse, Int) -> Unit,
    onRemoveItem: (TblMenuItemResponse) -> Unit,
    orderDetails: List<TblOrderDetailsResponse>,
    onDiscountChange: (Double) -> Unit = {},
    onOtherChargesChange: (Double) -> Unit = {}
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (uiState.billedItems.isNotEmpty()) {
            item {
                Text("Items", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }
            val filteredOrderDetails = orderDetails.groupBy { it.kot_number }
            items(filteredOrderDetails.toList()) { it ->
                Text(
                    text = "KOT #${it.first}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                it.second.forEach { details ->
                    BilledItemRow(
                        menuItem = details.menuItem,
                        quantity = details.qty,
                        tableStatus = uiState.tableStatus,
                        currencyFormatter = currencyFormatter,
                        onQuantityChange = { newQuantity ->
                            onUpdateQuantity(details.menuItem, newQuantity)
                        },
                        onRemoveItem = {
                            onRemoveItem(details.menuItem)
                        },
                        rate = details.actual_rate
                    )
                    Spacer(Modifier.height(8.dp))
                }
                ModernDivider(modifier = Modifier.padding(vertical = 8.dp))
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

        item {
            EditableBillingRow(
                label = "Subtotal",
                amount = uiState.subtotal,
                currencyFormatter = currencyFormatter
            )
        }

        item {
            EditableBillingRow(
                label = "Tax Amount",
                amount = uiState.taxAmount,
                currencyFormatter = currencyFormatter
            )
        }
        if (uiState.cessAmount > 0) {
            item {
                EditableBillingRow(
                    label = "Cess Amount",
                    amount = uiState.cessAmount,
                    currencyFormatter = currencyFormatter
                )
            }
        }
        if (uiState.cessSpecific > 0) {
            item {
                EditableBillingRow(
                    label = "Cess Specific",
                    amount = uiState.cessSpecific,
                    currencyFormatter = currencyFormatter
                )
            }
        }

        item {
            EditableBillingRow(
                label = "Discount",
                amount = uiState.discountFlat,
                currencyFormatter = currencyFormatter,
                isEditable = true,
                onValueChange = onDiscountChange
            )
        }

        item {
            EditableBillingRow(
                label = "Other Charges",
                amount = uiState.otherChrages,
                currencyFormatter = currencyFormatter,
                isEditable = true,
                onValueChange = onOtherChargesChange
            )
        }

        item {
            ModernDivider(modifier = Modifier.padding(vertical = 8.dp))
            BillingSummaryRow(
                label = "Total Amount",
                amount = uiState.totalAmount,
                currencyFormatter = currencyFormatter,
                isTotal = true
            )
        }
    }
}

@Composable
fun BilledItemRow(
    menuItem: TblMenuItemResponse,
    quantity: Int,
    tableStatus: String,
    currencyFormatter: NumberFormat,
    onQuantityChange: (Int) -> Unit,
    onRemoveItem: () -> Unit,
    modifier: Modifier = Modifier,
    rate: Double
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
                    text = CurrencySettings.format(rate * quantity),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@Composable
fun BillingSummaryRow(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableBillingRow(
    label: String,
    amount: Double,
    currencyFormatter: NumberFormat,
    isEditable: Boolean = false,
    onValueChange: ((Double) -> Unit)? = null
) {
    var textValue by remember(amount) { mutableStateOf(if (amount == 0.0) "" else amount.toString()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        if (isEditable && onValueChange != null) {
            OutlinedTextField(
                value = textValue,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                        textValue = newValue
                        onValueChange(newValue.toDoubleOrNull() ?: 0.0)
                    }
                },
                modifier = Modifier.width(100.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.End),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                )
            )
        } else {
            Text(
                CurrencySettings.formatPlain(amount),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.End
            )
        }
    }
}


@Composable
fun BillingBottomBar(
    uiState: BillingPaymentUiState,
    orderMasterId: String? = null,
    onProceedToPayment: () -> Unit
) {
    BottomAppBar(
        containerColor = SecondaryGreen
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Total Due",
                    style = MaterialTheme.typography.labelMedium,
                    color = SurfaceLight
                )
                Text(
                    CurrencySettings.format(uiState.totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SurfaceLight
                )
            }
            Spacer(modifier = Modifier.padding(horizontal = 25.dp))
            Column {
                MobileOptimizedButton(
                    onClick = {
                        if (orderMasterId != null) {
                            onProceedToPayment()
                        }
                    },
                    text = "Proceed to Payment",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)