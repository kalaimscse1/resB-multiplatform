package com.warriortech.resb.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.TblCustomer
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.model.TblUpiType
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.screens.settings.CustomerDialog
import com.warriortech.resb.ui.components.PaymentMethodCard
import com.warriortech.resb.ui.components.PaymentSummaryCard
import com.warriortech.resb.ui.theme.ModernDarkGreen
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SecondaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.payment.BillingPaymentUiState
import com.warriortech.resb.ui.viewmodel.payment.BillingViewModel
import com.warriortech.resb.ui.viewmodel.payment.PaymentProcessingState
import com.warriortech.resb.util.AnimatedSnackbarDemo
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.StringDropdown
import kotlinx.coroutines.launch

@androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavHostController,
    viewModel: BillingViewModel = hiltViewModel(),
    amountToPayFromRoute: Double? = null,
    orderMasterId: String? = null,
    billNo: String? = null,
    customerId: Long? = null,
    voucherType: String? = null,
    sessionManager: SessionManager,
    orderDetailsResponse: Map<TblMenuItemResponse, Int>? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val table = sessionManager.getGeneralSetting()?.is_table_allowed == true
    var showCustomerDialog by remember { mutableStateOf(false) }
    var customer by remember { mutableStateOf<TblCustomer?>(null) }
    val scope = rememberCoroutineScope()

    var showTenderDialog by remember { mutableStateOf(false) }
    var showUpiDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadCustomers()
    }

    LaunchedEffect(customerId, billNo) {
        if (customerId != null && billNo != null) {
            viewModel.updateCustomerId(customerId)
            viewModel.updateBillNo(billNo)
        }
    }
    LaunchedEffect(key1 = orderDetailsResponse) {
        Log.d("PaymentScreen", "$orderDetailsResponse")
        orderDetailsResponse?.let {
            val value = viewModel.recalcTotals(it)
            viewModel.updateBillState(
                uiState.copy(
                    billedItems = value.billedItems,
                    subtotal = value.subtotal,
                    taxAmount = value.taxAmount,
                    cessAmount = value.cessAmount,
                    cessSpecific = value.cessSpecific,
                    totalAmount = value.totalAmount,
                    amountToPay = value.totalAmount,
                    roundOff = value.roundOff
                )
            )
        }
    }

    LaunchedEffect(key1 = amountToPayFromRoute, key2 = orderMasterId) {
        amountToPayFromRoute?.let { viewModel.updateAmountToPay(it) }
        orderMasterId?.let { viewModel.updateOrderMasterId(it) }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(uiState.paymentProcessingState) {
        if (uiState.paymentProcessingState is PaymentProcessingState.Success) {
            viewModel.resetPaymentState()
            if (table)
                navController.navigate("selects")
            else
                navController.navigate("quick_bills")
        } else if (uiState.paymentProcessingState is PaymentProcessingState.Error) {
            val errorState = uiState.paymentProcessingState as PaymentProcessingState.Error
            snackbarHostState.showSnackbar("Payment Failed: ${errorState.message}")
            viewModel.resetPaymentState()
        }
    }

    Scaffold(
        snackbarHost = { AnimatedSnackbarDemo(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Complete Payment",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = SurfaceLight,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.paymentProcessingState !is PaymentProcessingState.Processing) {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SurfaceLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen
                ),
                actions = {
                    IconButton(onClick = {
                        showCustomerDialog = true
                    }) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Customer",
                            tint = SurfaceLight
                        )
                    }
                }
            )
        },
        bottomBar = {
            PaymentBottomBar(
                uiState = uiState,
                onConfirmPayment = {
                    val isTenderEnabled = sessionManager.getGeneralSetting()?.is_tendered == true
                    val isCash = uiState.selectedPaymentMethod?.name == "CASH"
                    val isUpi = uiState.selectedPaymentMethod?.name == "UPI"

                    if (isCash && isTenderEnabled) {
                        showTenderDialog = true
                    } else if (isUpi && uiState.selectedUpiTypeId == 0L && uiState.upiTypes.isNotEmpty()) {
                        showUpiDialog = true
                    } else {
                        viewModel.updateAmountToPay(uiState.totalAmount)
                        viewModel.processPayment(voucherType = voucherType ?: "BILL")
                    }
                },
                customer = customers.find { it.customer_id == customer?.customer_id },
                sessionManager = sessionManager
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            if (uiState.paymentProcessingState is PaymentProcessingState.Processing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(48.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "Processing Payment...",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Please wait while we process your payment",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            } else if (uiState.paymentProcessingState is PaymentProcessingState.Success) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(48.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "Payment Successful!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Your payment has been processed successfully",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .imePadding(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        PaymentSummaryCard(uiState = uiState, viewModel = viewModel)
                    }

                    item {
                        PaymentMethodCard(
                            uiState = uiState,
                            onPaymentMethodChange = { method ->
                                viewModel.updatePaymentMethod(method)
                                if (method == "UPI" && uiState.upiTypes.isNotEmpty()) {
                                    showUpiDialog = true
                                }
                            },
                            viewModel = viewModel,
                            onCustomer = {
                                customer = it
                                viewModel.setCustomer(it)
                            },
                            customers = customers,
                            voucherType = voucherType,
                            isTendered = sessionManager.getGeneralSetting()?.is_tendered == true
                        )
                    }
                }
            }
        }
    }

    if (showUpiDialog) {
        UpiTypeSelectionDialog(
            upiTypes = uiState.upiTypes,
            onSelect = { id ->
                viewModel.selectUpiType(id)
                showUpiDialog = false
            },
            onDismiss = { showUpiDialog = false }
        )
    }

    if (showTenderDialog) {
        PaymentTenderDialog(
            totalAmount = uiState.totalAmount,
            onConfirm = { received ->
                showTenderDialog = false
                viewModel.updateAmountReceived(received)
                viewModel.updateAmountToPay(uiState.totalAmount)
                viewModel.processPayment(voucherType = voucherType ?: "BILL")
            },
            onDismiss = { showTenderDialog = false }
        )
    }

    if (showCustomerDialog) {
        CustomerDialog(
            customer = null,
            onDismiss = { showCustomerDialog = false },
            onConfirm = { customer ->
                scope.launch {
                    viewModel.addCustomer(customer)
                    showCustomerDialog = false
                }
            }
        )
    }
}

@Composable
fun UpiTypeSelectionDialog(
    upiTypes: List<TblUpiType>,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select UPI Type") },
        text = {
            Box(modifier = Modifier.heightIn(max = 300.dp)) {
                LazyColumn {
                    items(upiTypes) { type ->
                        ListItem(
                            headlineContent = { Text(type.upi_type_name) },
                            modifier = Modifier.clickable { onSelect(type.upi_type_id) }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PaymentTenderDialog(
    totalAmount: Double,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var tenderedAmount by remember { mutableStateOf("") }
    val balance = (tenderedAmount.toDoubleOrNull() ?: 0.0) - totalAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cash Tender") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Payable:", style = MaterialTheme.typography.bodyLarge)
                    Text(CurrencySettings.format(totalAmount), fontWeight = FontWeight.Bold)
                }
                OutlinedTextField(
                    value = tenderedAmount,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) tenderedAmount = it },
                    label = { Text("Tendered Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (balance >= 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Balance to Return:", color = ModernDarkGreen)
                        Text(CurrencySettings.format(balance), fontWeight = FontWeight.Bold, color = ModernDarkGreen)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(tenderedAmount.toDoubleOrNull() ?: 0.0) },
                enabled = (tenderedAmount.toDoubleOrNull() ?: 0.0) >= totalAmount
            ) {
                Text("Confirm & Pay")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PaymentBottomBar(
    uiState: BillingPaymentUiState,
    onConfirmPayment: (Boolean) -> Unit,
    customer: TblCustomer? = null,
    sessionManager: SessionManager
) {
    val isTendered = sessionManager.getGeneralSetting()?.is_tendered == true
    val totalAmount = uiState.totalAmount
    val method = uiState.selectedPaymentMethod?.name.orEmpty()

    val hasManualEntry = when (method) {
        "CASH" -> if (isTendered) true else uiState.cashAmount > 0.0 // True because tender dialog will handle it
        "CARD" -> uiState.cardAmount > 0.0
        "UPI" -> uiState.upiAmount > 0.0
        "OTHERS" -> (uiState.cashAmount + uiState.cardAmount + uiState.upiAmount) > 0.0
        "DUE" -> true
        else -> false
    }

    val paidAmount = when (method) {
        "CASH" -> {
            if (isTendered) {
                totalAmount // Assume full payment, balance handled in dialog
            } else {
                if (uiState.cashAmount == 0.0) totalAmount else uiState.cashAmount
            }
        }

        "CARD" -> if (uiState.cardAmount == 0.0) totalAmount else uiState.cardAmount
        "UPI" -> if (uiState.upiAmount == 0.0) totalAmount else uiState.upiAmount
        "OTHERS" -> uiState.cashAmount + uiState.cardAmount + uiState.upiAmount
        else -> totalAmount
    }

    val scope = rememberCoroutineScope()
    val isIdle = uiState.paymentProcessingState == PaymentProcessingState.Idle
    val hasCustomer = customer?.customer_id != null

    val isDue = paidAmount < totalAmount || method == "DUE"

    val enabled = when {
        method.isBlank() -> false
        method != "DUE" && !hasManualEntry -> false
        isDue && paidAmount > totalAmount -> false
        isDue && !hasCustomer -> false
        paidAmount <= 0 -> false
        paidAmount > totalAmount -> false
        else -> isIdle
    }

    BottomAppBar(containerColor = SecondaryGreen) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(CurrencySettings.format(paidAmount), color = Color.White, fontWeight = FontWeight.Bold)
            Button(
                onClick = { onConfirmPayment(isDue) },
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (enabled) PrimaryGreen else Color.Gray,
                    contentColor = Color.White
                )
            ) { Text("Confirm Payment") }
        }
    }
}
