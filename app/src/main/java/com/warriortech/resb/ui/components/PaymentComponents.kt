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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.warriortech.resb.model.TblCustomer
import com.warriortech.resb.ui.viewmodel.payment.BillingViewModel
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.CustomerDropdown

@Composable
fun PaymentSummaryCard(uiState: BillingPaymentUiState) {
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
                    tint = MaterialTheme.colorScheme.primary,
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
                Text(
                    "Total",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    CurrencySettings.format(uiState.amountToPay),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

//@Composable
//fun PaymentMethodCard(
//    uiState: BillingPaymentUiState,
//    onPaymentMethodChange: (String) -> Unit,
//    viewModel: BillingViewModel,
//    customers: List<TblCustomer>,
//    onCustomer: (TblCustomer) -> Unit,
//    voucherType: String? = null
//) {
//    val paidAmount = uiState.cashAmount + uiState.cardAmount + uiState.upiAmount
//    val totalAmount = uiState.amountToPay
//    // Memoize the text field values to prevent unnecessary string conversions
//
//    val cash = remember(uiState.amountToPay) {
//        if (uiState.cashAmount == 0.0) uiState.amountToPay.toString() else uiState.cashAmount.toString()
//    }
//    val card = remember(uiState.amountToPay) {
//        if (uiState.cardAmount == 0.0) uiState.amountToPay.toString() else uiState.cardAmount.toString()
//    }
//    val upi = remember(uiState.amountToPay) {
//        if (uiState.upiAmount == 0.0) uiState.amountToPay.toString() else uiState.upiAmount.toString()
//    }
//    // Memoize the text field values to prevent unnecessary string conversions
//    val cashValue = remember(uiState.cashAmount) {
//        if (uiState.cashAmount == 0.0) "" else uiState.cashAmount.toString()
//    }
//    val cardValue = remember(uiState.cardAmount) {
//        if (uiState.cardAmount == 0.0) "" else uiState.cardAmount.toString()
//    }
//    val upiValue = remember(uiState.upiAmount) {
//        if (uiState.upiAmount == 0.0) "" else uiState.upiAmount.toString()
//    }
//
//    val showCustomerDropdown =
//        uiState.selectedPaymentMethod?.name == "DUE" ||
//                paidAmount < totalAmount
//    // Cache the payment methods list to prevent recreation on every recomposition
//    val paymentMethods = remember {
//        if (voucherType == "DUE") {
//            listOf(
//                "CASH" to Icons.Default.Money,
//                "CARD" to Icons.Default.CreditCard,
//                "UPI" to Icons.Default.QrCode,
//                "OTHERS" to Icons.Default.MoreHoriz
//            )
//        } else
//            listOf(
//                "CASH" to Icons.Default.Money,
//                "CARD" to Icons.Default.CreditCard,
//                "UPI" to Icons.Default.QrCode,
//                "DUE" to Icons.Default.AccountBalanceWallet,
//                "OTHERS" to Icons.Default.MoreHoriz
//            )
//    }
//    viewModel.loadCustomers()
//    ModernCard(
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Column(
//            modifier = Modifier.padding(20.dp)
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Icon(
//                    Icons.Default.Payment,
//                    contentDescription = "Payment Method",
//                    tint = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.size(28.dp)
//                )
//                Spacer(modifier = Modifier.width(12.dp))
//                Text(
//                    "Payment Method",
//                    style = MaterialTheme.typography.headlineSmall,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            paymentMethods.forEach { (method, icon) ->
//                // Use remember to create stable callback to prevent unnecessary recompositions
//                val onSelectMethod = remember(method) { { onPaymentMethodChange(method) } }
//                PaymentMethodOption(
//                    method = method,
//                    icon = icon,
//                    isSelected = uiState.selectedPaymentMethod?.name == method,
//                    onSelect = onSelectMethod
//                )
//
//                if (method != paymentMethods.last().first) {
//                    Spacer(modifier = Modifier.height(8.dp))
//                }
//            }
//
//            if (uiState.selectedPaymentMethod?.name == "CASH") {
//                Spacer(modifier = Modifier.height(16.dp))
//                OutlinedTextField(
//                    value = cash.toString(),
//                    onValueChange = { viewModel.updateCashAmount(it.toDoubleOrNull() ?: 0.0) },
//                    label = { Text("Cash Amount") },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//
//            if (uiState.selectedPaymentMethod?.name == "CARD") {
//                Spacer(modifier = Modifier.height(16.dp))
//                OutlinedTextField(
//                    value = card,
//                    onValueChange = { viewModel.updateCardAmount(it.toDoubleOrNull() ?: 0.0) },
//                    label = { Text("Card Amount") },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//
//            if (uiState.selectedPaymentMethod?.name == "UPI") {
//                Spacer(modifier = Modifier.height(16.dp))
//                OutlinedTextField(
//                    value = upi,
//                    onValueChange = { viewModel.updateUpiAmount(it.toDoubleOrNull() ?: 0.0) },
//                    label = { Text("UPI Amount") },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//
//            if (showCustomerDropdown) {
//                Spacer(modifier = Modifier.height(16.dp))
//                CustomerDropdown(
//                    customers = customers,
//                    selectedCustomer = uiState.customer,
//                    onCustomerSelected = {
//                        viewModel.updateSelectedCustomer(it)
//                        onCustomer(it)
//                    },
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//
//            if (uiState.selectedPaymentMethod?.name == "OTHERS") {
//                Spacer(modifier = Modifier.height(16.dp))
//
//                OutlinedTextField(
//                    value = cashValue,
//                    onValueChange = { viewModel.updateCashAmount(it.toDoubleOrNull() ?: 0.0) },
//                    label = { Text("Cash") },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    modifier = Modifier.fillMaxWidth()
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//
//                OutlinedTextField(
//                    value = cardValue,
//                    onValueChange = { viewModel.updateCardAmount(it.toDoubleOrNull() ?: 0.0) },
//                    label = { Text("Card") },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    modifier = Modifier.fillMaxWidth()
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//
//                OutlinedTextField(
//                    value = upiValue,
//                    onValueChange = { viewModel.updateUpiAmount(it.toDoubleOrNull() ?: 0.0) },
//                    label = { Text("UPI") },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//        }
//    }
//}

@Composable
fun PaymentMethodCard(
    uiState: BillingPaymentUiState,
    onPaymentMethodChange: (String) -> Unit,
    viewModel: BillingViewModel,
    customers: List<TblCustomer>,
    onCustomer: (TblCustomer) -> Unit,
    voucherType: String? = null
) {
    val paidAmount = uiState.cashAmount + uiState.cardAmount + uiState.upiAmount
    val totalAmount = uiState.amountToPay

    val showCustomerDropdown =
        uiState.selectedPaymentMethod?.name == "DUE" ||
                paidAmount < totalAmount

    val paymentMethods = remember {
        if (voucherType == "DUE") {
            listOf(
                "CASH" to Icons.Default.Money,
                "CARD" to Icons.Default.CreditCard,
                "UPI" to Icons.Default.QrCode,
                "OTHERS" to Icons.Default.MoreHoriz
            )
        } else {
            listOf(
                "CASH" to Icons.Default.Money,
                "CARD" to Icons.Default.CreditCard,
                "UPI" to Icons.Default.QrCode,
                "DUE" to Icons.Default.AccountBalanceWallet,
                "OTHERS" to Icons.Default.MoreHoriz
            )
        }
    }

    // Auto-fill on first selection (Option B)
    LaunchedEffect(uiState.selectedPaymentMethod) {
        when (uiState.selectedPaymentMethod?.name) {
            "CASH" -> if (uiState.cashAmount == 0.0) viewModel.updateCashAmount(totalAmount)
            "CARD" -> if (uiState.cardAmount == 0.0) viewModel.updateCardAmount(totalAmount)
            "UPI" -> if (uiState.upiAmount == 0.0) viewModel.updateUpiAmount(totalAmount)
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
                    tint = MaterialTheme.colorScheme.primary,
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
                    OutlinedTextField(
                        value = CurrencySettings.formatPlain(uiState.cashAmount).takeIf { uiState.cashAmount != 0.0 } ?: "",
                        onValueChange = {
                            viewModel.updateCashAmount(it.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("Cash Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                "CARD" -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = CurrencySettings.formatPlain(uiState.cardAmount).takeIf { uiState.cardAmount != 0.0 } ?: "",
                        onValueChange = {
                            viewModel.updateCardAmount(it.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("Card Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                "UPI" -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = CurrencySettings.formatPlain(uiState.upiAmount).takeIf { uiState.upiAmount != 0.0 } ?: "",
                        onValueChange = {
                            viewModel.updateUpiAmount(it.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("UPI Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                "OTHERS" -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = "",
                        onValueChange = {
                            viewModel.updateCashAmount(it.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("Cash") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value =  "",
                        onValueChange = {
                            viewModel.updateCardAmount(it.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("Card") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value ="",
                        onValueChange = {
                            viewModel.updateUpiAmount(it.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("UPI") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
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
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
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
                tint = if (isSelected) MaterialTheme.colorScheme.primary
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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}