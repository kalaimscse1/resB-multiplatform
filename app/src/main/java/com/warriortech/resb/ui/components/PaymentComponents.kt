package com.warriortech.resb.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import com.warriortech.resb.ui.viewmodel.payment.BillingPaymentUiState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.warriortech.resb.model.TblCustomer
import com.warriortech.resb.screens.BillingSummaryRow
import com.warriortech.resb.screens.EditableBillingRow
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.viewmodel.payment.BillingViewModel
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.CustomerDropdown
import java.text.NumberFormat
import java.util.Locale
import kotlin.toString

@Composable
fun PaymentSummaryCard(uiState: BillingPaymentUiState, viewModel: BillingViewModel) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    ModernCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = "Payment Summary",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Payment Summary",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditableBillingRow(
                    label = "Subtotal",
                    amount = uiState.subtotal,
                    currencyFormatter = currencyFormatter
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                EditableBillingRow(
                    label = "Tax Amount",
                    amount = uiState.taxAmount,
                    currencyFormatter = currencyFormatter
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditableBillingRow(
                    label = "Discount",
                    amount = uiState.discountFlat,
                    currencyFormatter = currencyFormatter,
                    isEditable = true,
                    onValueChange = {
                        viewModel.updateDiscountFlat(it)
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditableBillingRow(
                    label = "Other Charges",
                    amount = uiState.otherChrages,
                    currencyFormatter = currencyFormatter,
                    isEditable = true,
                    onValueChange = {
                        viewModel.updateOtherCharges(it)
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditableBillingRow(
                    label = "RoundOff",
                    amount = uiState.roundOff,
                    currencyFormatter = currencyFormatter
                )
            }
            ModernDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                BillingSummaryRow(
                    label = "Total Amount",
                    amount = uiState.totalAmount,
                    currencyFormatter = currencyFormatter,
                    isTotal = true
                )
            }
        }
    }
}

@Composable
fun PaymentMethodCard(
    uiState: BillingPaymentUiState,
    onPaymentMethodChange: (String) -> Unit,
    viewModel: BillingViewModel,
    customers: List<TblCustomer>,
    onCustomer: (TblCustomer) -> Unit,
    voucherType: String? = null,
    isTendered: Boolean
) {
    val paidAmount = uiState.cashAmount + uiState.cardAmount + uiState.upiAmount + uiState.onlineAmount
    val totalAmount = uiState.totalAmount

    val showCustomerDropdown =
        uiState.selectedPaymentMethod?.name == "DUE" ||
                paidAmount < totalAmount

    val paymentMethods = remember {
        if (voucherType == "DUE") {
            listOf(
                "CASH" to Icons.Default.Money,
                "CARD" to Icons.Default.CreditCard,
                "UPI" to Icons.Default.QrCode,
                "CASH/CARD" to Icons.Default.MoneyOff,
                "ONLINE" to Icons.Default.MoreHoriz
            )
        } else {
            listOf(
                "CASH" to Icons.Default.Money,
                "CARD" to Icons.Default.CreditCard,
                "UPI" to Icons.Default.QrCode,
                "CASH/CARD" to Icons.Default.MoneyOff,
                "DUE" to Icons.Default.AccountBalanceWallet,
                "ONLINE" to Icons.Default.MoreHoriz
            )
        }
    }

    val focusRequester = remember { FocusRequester() }

    // Auto-fill on first selection (Option B)
    LaunchedEffect(uiState.selectedPaymentMethod) {
        when (uiState.selectedPaymentMethod?.name) {
            "CASH" -> {
                if (isTendered) {
                    if (uiState.amountReceived == 0.0) viewModel.updateAmountReceived(totalAmount)
                } else {
                    if (uiState.cashAmount == 0.0) viewModel.updateCashAmount(totalAmount)
                }
                focusRequester.requestFocus()
            }

            "CARD" -> {
                if (uiState.cardAmount == 0.0) viewModel.updateCardAmount(totalAmount)
                focusRequester.requestFocus()
            }
            "UPI" -> {
                if (uiState.upiAmount == 0.0) viewModel.updateUpiAmount(totalAmount)
                focusRequester.requestFocus()
            }
            "CASH/CARD"->{
                focusRequester.requestFocus()
            }
            "ONLINE" -> {
                if (uiState.onlineAmount == 0.0) viewModel.updateOnlineAmount(totalAmount)
                focusRequester.requestFocus()
            }
        }
    }

    ModernCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Payment,
                    contentDescription = "Payment Method",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Payment Method",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            paymentMethods.forEach { (method, icon) ->
                PaymentMethodOption(
                    method = method,
                    icon = icon,
                    isSelected = uiState.selectedPaymentMethod?.name == method,
                    onSelect = { onPaymentMethodChange(method) }
                )
                if (method != paymentMethods.last().first)
                    Spacer(modifier = Modifier.height(8.dp))
            }

            when (uiState.selectedPaymentMethod?.name) {

                "CASH" -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (isTendered) {
                        var amountReceivedText by remember { 
                            mutableStateOf(TextFieldValue(if (uiState.amountReceived == 0.0) "" else uiState.amountReceived.toString())) 
                        }
                        // Sync with uiState if needed (e.g. on focus)
                        LaunchedEffect(uiState.amountReceived) {
                            if (amountReceivedText.text != uiState.amountReceived.toString() && !(uiState.amountReceived == 0.0 && amountReceivedText.text == "")) {
                                amountReceivedText = amountReceivedText.copy(text = if (uiState.amountReceived == 0.0) "" else uiState.amountReceived.toString())
                            }
                        }

                        OutlinedTextField(
                            value = amountReceivedText,
                            onValueChange = {
                                amountReceivedText = it
                                viewModel.updateAmountReceived(it.text.toDoubleOrNull() ?: 0.0)
                            },
                            label = { Text("Amount Received") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .onFocusChanged { 
                                    if (it.isFocused) {
                                        amountReceivedText = amountReceivedText.copy(selection = TextRange(0, amountReceivedText.text.length))
                                    }
                                }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = CurrencySettings.format(uiState.changeAmount),
                            onValueChange = { },
                            label = { Text("Change") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        var cashAmountText by remember { 
                            mutableStateOf(TextFieldValue(if (uiState.cashAmount == 0.0) "" else uiState.cashAmount.toString())) 
                        }
                        LaunchedEffect(uiState.cashAmount) {
                            if (cashAmountText.text != uiState.cashAmount.toString() && !(uiState.cashAmount == 0.0 && cashAmountText.text == "")) {
                                cashAmountText = cashAmountText.copy(text = if (uiState.cashAmount == 0.0) "" else uiState.cashAmount.toString())
                            }
                        }

                        OutlinedTextField(
                            value = cashAmountText,
                            onValueChange = {
                                cashAmountText = it
                                viewModel.updateCashAmount(it.text.toDoubleOrNull() ?: 0.0)
                            },
                            label = { Text("Cash Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .onFocusChanged { 
                                    if (it.isFocused) {
                                        cashAmountText = cashAmountText.copy(selection = TextRange(0, cashAmountText.text.length))
                                    }
                                }
                        )
                    }
                    viewModel.updateCardAmount(0.0)
                    viewModel.updateUpiAmount(0.0)
                    viewModel.updateOnlineAmount(0.0)
                }

                "CARD" -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    var cardAmountText by remember { 
                        mutableStateOf(TextFieldValue(if (uiState.cardAmount == 0.0) "" else uiState.cardAmount.toString())) 
                    }
                    LaunchedEffect(uiState.cardAmount) {
                        if (cardAmountText.text != uiState.cardAmount.toString() && !(uiState.cardAmount == 0.0 && cardAmountText.text == "")) {
                            cardAmountText = cardAmountText.copy(text = if (uiState.cardAmount == 0.0) "" else uiState.cardAmount.toString())
                        }
                    }

                    OutlinedTextField(
                        value = cardAmountText,
                        onValueChange = {
                            cardAmountText = it
                            viewModel.updateCardAmount(it.text.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("Card Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { 
                                if (it.isFocused) {
                                    cardAmountText = cardAmountText.copy(selection = TextRange(0, cardAmountText.text.length))
                                }
                            }
                    )
                    viewModel.updateCashAmount(0.0)
                    viewModel.updateUpiAmount(0.0)
                    viewModel.updateOnlineAmount(0.0)
                }

                "UPI" -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    var upiAmountText by remember { 
                        mutableStateOf(TextFieldValue(if (uiState.upiAmount == 0.0) "" else uiState.upiAmount.toString())) 
                    }
                    LaunchedEffect(uiState.upiAmount) {
                        if (upiAmountText.text != uiState.upiAmount.toString() && !(uiState.upiAmount == 0.0 && upiAmountText.text == "")) {
                            upiAmountText = upiAmountText.copy(text = if (uiState.upiAmount == 0.0) "" else uiState.upiAmount.toString())
                        }
                    }

                    OutlinedTextField(
                        value = upiAmountText,
                        onValueChange = {
                            upiAmountText = it
                            viewModel.updateUpiAmount(it.text.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("UPI Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { 
                                if (it.isFocused) {
                                    upiAmountText = upiAmountText.copy(selection = TextRange(0, upiAmountText.text.length))
                                }
                            }
                    )
                    viewModel.updateCashAmount(0.0)
                    viewModel.updateCardAmount(0.0)
                    viewModel.updateOnlineAmount(0.0)
                }

                "ONLINE"->{
                    Spacer(modifier = Modifier.height(16.dp))
                    var onlineAmountText by remember {
                        mutableStateOf(TextFieldValue(if (uiState.onlineAmount == 0.0) "" else uiState.onlineAmount.toString()))
                    }
                    LaunchedEffect(uiState.onlineAmount) {
                        if (onlineAmountText.text != uiState.onlineAmount.toString() && !(uiState.onlineAmount == 0.0 && onlineAmountText.text == "")) {
                            onlineAmountText = onlineAmountText.copy(text = if (uiState.onlineAmount == 0.0) "" else uiState.onlineAmount.toString())
                        }
                    }

                    OutlinedTextField(
                        value = onlineAmountText,
                        onValueChange = {
                            onlineAmountText = it
                            viewModel.updateOnlineAmount(it.text.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("ONLINE Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    onlineAmountText = onlineAmountText.copy(selection = TextRange(0, onlineAmountText.text.length))
                                }
                            }
                    )
                    viewModel.updateCashAmount(0.0)
                    viewModel.updateCardAmount(0.0)
                    viewModel.updateUpiAmount(0.0)
                }

                "CASH/CARD" -> {
                    var cashAmountText by remember { 
                        mutableStateOf(TextFieldValue(if (uiState.cashAmount == 0.0) "" else uiState.cashAmount.toString())) 
                    }
                    var cardAmountText by remember { 
                        mutableStateOf(TextFieldValue(if (uiState.cardAmount == 0.0) "" else uiState.cardAmount.toString())) 
                    }
                    var upiAmountText by remember { 
                        mutableStateOf(TextFieldValue(if (uiState.upiAmount == 0.0) "" else uiState.upiAmount.toString())) 
                    }

                    LaunchedEffect(uiState.cashAmount) {
                        if (cashAmountText.text != uiState.cashAmount.toString() && !(uiState.cashAmount == 0.0 && cashAmountText.text == "")) {
                            cashAmountText = cashAmountText.copy(text = if (uiState.cashAmount == 0.0) "" else uiState.cashAmount.toString())
                        }
                    }
                    LaunchedEffect(uiState.cardAmount) {
                        if (cardAmountText.text != uiState.cardAmount.toString() && !(uiState.cardAmount == 0.0 && cardAmountText.text == "")) {
                            cardAmountText = cardAmountText.copy(text = if (uiState.cardAmount == 0.0) "" else uiState.cardAmount.toString())
                        }
                    }
                    LaunchedEffect(uiState.upiAmount) {
                        if (upiAmountText.text != uiState.upiAmount.toString() && !(uiState.upiAmount == 0.0 && upiAmountText.text == "")) {
                            upiAmountText = upiAmountText.copy(text = if (uiState.upiAmount == 0.0) "" else uiState.upiAmount.toString())
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = cashAmountText,
                        onValueChange = {
                            cashAmountText = it
                            viewModel.updateCashAmount(it.text.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("Cash") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { 
                                if (it.isFocused) {
                                    cashAmountText = cashAmountText.copy(selection = TextRange(0, cashAmountText.text.length))
                                }
                            }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cardAmountText,
                        onValueChange = {
                            cardAmountText = it
                            viewModel.updateCardAmount(it.text.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("Card") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { 
                                if (it.isFocused) {
                                    cardAmountText = cardAmountText.copy(selection = TextRange(0, cardAmountText.text.length))
                                }
                            }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = upiAmountText,
                        onValueChange = {
                            upiAmountText = it
                            viewModel.updateUpiAmount(it.text.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("UPI") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { 
                                if (it.isFocused) {
                                    upiAmountText = upiAmountText.copy(selection = TextRange(0, upiAmountText.text.length))
                                }
                            }
                    )
                }
            }

            if (showCustomerDropdown) {
                Spacer(modifier = Modifier.height(16.dp))
                CustomerDropdown(
                    customers = customers,
                    selectedCustomer = uiState.customer,
                    onCustomerSelected = {
                        viewModel.updateSelectedCustomer(it)
                        onCustomer(it)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodOption(
    method: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected)
            BorderStroke(2.dp, PrimaryGreen)
        else
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = method,
                tint = if (isSelected) PrimaryGreen
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                method,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}