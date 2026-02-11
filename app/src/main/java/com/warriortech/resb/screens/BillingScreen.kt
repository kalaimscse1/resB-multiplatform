package com.warriortech.resb.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.warriortech.resb.model.Bill
import com.warriortech.resb.model.BillItem
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.ui.viewmodel.payment.TemplatePreviewData


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

                    items(kotNumbers) { kot: Int ->
                        MobileOptimizedButton(
                            onClick = { onKotSelected(kot) },
                            text = "KOT #$kot",
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
    viewModel: BillingViewModel = hiltViewModel<BillingViewModel>(),
    orderDetailsResponse: List<TblOrderDetailsResponse>? = null,
    orderMasterId: String? = null,
    sessionManager: SessionManager,
    onProceedToBilling: (Map<TblMenuItemResponse, Int>) -> Unit
) {
    val uiState: BillingPaymentUiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedKotNumber by remember { mutableStateOf<Int?>(null) }
    var showKotSelectionDialog by remember { mutableStateOf(false) }
    val orderDetails: List<TblOrderDetailsResponse> by viewModel.originalOrderDetails.collectAsStateWithLifecycle()
    var previewDialog by remember { mutableStateOf(false) }
    val preview: Bitmap? by viewModel.preview.collectAsStateWithLifecycle()
    val templatePreview: TemplatePreviewData? by viewModel.templatePreview.collectAsStateWithLifecycle()

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
                            if (orderMasterId != null) {
                                viewModel.previewDetails(orderMasterId)
                                previewDialog = true
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = "Print",
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
            BillingBottomBar(
                uiState = uiState,
                orderMasterId = orderMasterId,
                onProceedToPayment = {
                    navController.navigate("payment_screen/${uiState.totalAmount}/${uiState.orderMasterId}/${"--"}/${0L}/${" "}") {
                        launchSingleTop = true
                        restoreState = true
                    }
                    onProceedToBilling(uiState.billedItems)
                })
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
            orderDetails = orderDetails
        )

    }
    if (previewDialog && (preview != null || templatePreview != null)) {
        PreviewBillDialog(
            bitmapPreview = preview,
            templatePreview = templatePreview,
            sessionManager = sessionManager,
            onDismiss = {
                previewDialog = false
                viewModel.clearPreview()
            }
        )
    }

}

@Composable
fun PreviewBillDialog(
    bitmapPreview: Bitmap?,
    templatePreview: TemplatePreviewData?,
    sessionManager: SessionManager,
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
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                if (templatePreview != null) {
                    TemplateBasedPreviewContent(templatePreview, sessionManager)
                } else if (bitmapPreview != null) {
                    Image(
                        bitmap = bitmapPreview.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    )
}

@Composable
fun TemplateBasedPreviewContent(data: TemplatePreviewData, sessionManager: SessionManager) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        data.sections.forEach { sectionData ->
            sectionData.lines.forEach { lineData ->
                val line = lineData.line
                if (line.is_repeatable && sectionData.section.section_type.uppercase() == "BODY") {
                    data.bill.items.forEach { billItem ->
                        RenderLineRow(lineData, data.bill, billItem, sessionManager)
                    }
                } else {
                    RenderLineRow(lineData, data.bill, null, sessionManager)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
fun RenderLineRow(
    lineData: com.warriortech.resb.ui.viewmodel.payment.TemplatePreviewLine,
    bill: Bill,
    item: BillItem?,
    sessionManager: SessionManager
) {
    val line = lineData.line
    val columns = lineData.columns
    
    if (columns.isEmpty()) {
        val text = resolvePreviewValue(line.field_key, bill, item, sessionManager)
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            textAlign = when (line.align_type.uppercase()) {
                "CENTER" -> TextAlign.Center
                "RIGHT" -> TextAlign.End
                else -> TextAlign.Start
            },
            fontWeight = if (line.is_bold) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodySmall
        )
    } else {
        Row(modifier = Modifier.fillMaxWidth()) {
            columns.sortedBy { it.sort_order }.forEach { column ->
                val text = resolvePreviewValue(column.field_key, bill, item, sessionManager)
                Text(
                    text = text,
                    modifier = Modifier.weight(column.width_pct.toFloat()),
                    textAlign = when (column.align_type.uppercase()) {
                        "CENTER" -> TextAlign.Center
                        "RIGHT" -> TextAlign.End
                        else -> TextAlign.Start
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
fun resolvePreviewValue(key: String, bill: Bill, item: BillItem?, sessionManager: SessionManager): String {
    val profile = sessionManager.getRestaurantProfile()
    val settings = sessionManager.getGeneralSetting()
    val cleanKey = key.uppercase().trim()
    
    return when {
        cleanKey == "BUSINESS NAME" || cleanKey == "RESTAURANT NAME" || cleanKey == "COMPANY VALUE" -> profile?.company_name ?: ""
        cleanKey == "BUSINESS ADDRESS" || cleanKey == "ADDRESS" -> "${profile?.address1 ?: ""} ${profile?.address2 ?: ""}".trim()
        cleanKey == "ADDRESS1 VALUE" -> profile?.address1 ?: ""
        cleanKey == "ADDRESS2 VALUE" -> profile?.address2 ?: ""
        cleanKey == "PLACE VALUE" -> profile?.place ?: ""
        cleanKey == "PINCODE VALUE" -> profile?.pincode ?: ""
        cleanKey == "PHONE" || cleanKey == "CONTACT NO" || cleanKey == "BUSINESS PHONE" -> profile?.contact_no ?: ""
        cleanKey == "GST NO" || cleanKey == "TAX NO" || cleanKey == "BUSINESS GSTIN" -> profile?.tax_no ?: ""
        cleanKey == "BILL VALUE" -> bill.billNo
        cleanKey == "DATE VALUE" -> bill.date
        cleanKey == "TIME VALUE" -> bill.time
        cleanKey == "ORDER VALUE" -> bill.orderNo
        cleanKey == "TABLE VALUE" -> bill.tableNo
        cleanKey == "COUNTER VALUE" -> bill.counter
        cleanKey == "SUB TOTAL" -> String.format("%.2f", bill.subtotal)
        cleanKey == "TOTAL" -> String.format("%.2f", bill.total)
        cleanKey == "DISCOUNT" -> String.format("%.2f", bill.discount)
        cleanKey == "TAX AMOUNT" -> String.format("%.2f", bill.items.sumOf { it.taxAmount })
        cleanKey == "RECEIVED AMT" -> String.format("%.2f", bill.received_amt)
        cleanKey == "PENDING AMT" -> String.format("%.2f", bill.received_amt - bill.total)
        cleanKey == "CUST NAME" -> bill.custName
        cleanKey == "CUST NO" -> bill.custNo
        cleanKey == "CUST ADDRESS" -> bill.custAddress
        cleanKey == "CUST GSTIN" -> bill.custGstin
        cleanKey == "ITEM VALUE" -> item?.itemName ?: ""
        cleanKey == "QTY VALUE" -> item?.qty?.toString() ?: ""
        cleanKey == "RATE" || cleanKey == "PRICE VALUE" -> String.format("%.2f", item?.price ?: 0.0)
        cleanKey == "AMT VALUE" -> String.format("%.2f", item?.amount ?: 0.0)
        cleanKey == "SN" -> item?.sn?.toString() ?: ""
        cleanKey == "FOOTER" || cleanKey == "BILL FOOTER" || cleanKey == "THANK VALUE" -> settings?.bill_footer ?: ""
        cleanKey == "SEPARATOR" -> "--------------------------------"
        cleanKey == "DOUBLE_SEPARATOR" -> "================================"
        key.startsWith("FIXED:") -> key.removePrefix("FIXED:")
        else -> key
    }
}

@Composable
fun BillingContent(
    modifier: Modifier = Modifier,
    uiState: BillingPaymentUiState,
    onUpdateQuantity: (TblMenuItemResponse, Int) -> Unit,
    onRemoveItem: (TblMenuItemResponse) -> Unit,
    orderDetails: List<TblOrderDetailsResponse>
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
            items(
                items = filteredOrderDetails.toList(),
                key = { it.first }
            ) { entry: Pair<Int, List<TblOrderDetailsResponse>> ->
                Text(
                    text = "KOT #${entry.first}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                entry.second.forEach { details ->
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
