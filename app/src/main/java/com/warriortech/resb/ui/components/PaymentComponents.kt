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
import com.warriortech.resb.screens.BillingSummaryRow
import com.warriortech.resb.screens.EditableBillingRow
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

//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    "Total",
//                    style = MaterialTheme.typography.titleLarge,
//                    fontWeight = FontWeight.Bold
//                )
//                Text(
//                    CurrencySettings.format(uiState.amountToPay),
//                    style = MaterialTheme.typography.titleLarge,
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.primary
//                )
//            }
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
            "CASH" -> {
                if (isTendered) {
                    if (uiState.amountReceived == 0.0) viewModel.updateAmountReceived(totalAmount)
                } else {
                    if (uiState.cashAmount == 0.0) viewModel.updateCashAmount(totalAmount)
                }
            }

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
                    if (isTendered) {
                        OutlinedTextField(
                            value = if (uiState.amountReceived == 0.0) "" else uiState.amountReceived.toString(),
                            onValueChange = {
                                viewModel.updateAmountReceived(it.toDoubleOrNull() ?: 0.0)
                            },
                            label = { Text("Amount Received") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
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
                        OutlinedTextField(
                            value = CurrencySettings.formatPlain(uiState.cashAmount)
                                .takeIf { uiState.cashAmount != 0.0 } ?: "",
                            onValueChange = {
                                viewModel.updateCashAmount(it.toDoubleOrNull() ?: 0.0)
                            },
                            label = { Text("Cash Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    viewModel.updateCardAmount(0.0)
                    viewModel.updateUpiAmount(0.0)
                }

                "CARD" -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = CurrencySettings.formatPlain(uiState.cardAmount)
                            .takeIf { uiState.cardAmount != 0.0 } ?: "",
                        onValueChange = {
                            viewModel.updateCardAmount(it.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("Card Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    viewModel.updateCashAmount(0.0)
                    viewModel.updateUpiAmount(0.0)
                }

                "UPI" -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = CurrencySettings.formatPlain(uiState.upiAmount)
                            .takeIf { uiState.upiAmount != 0.0 } ?: "",
                        onValueChange = {
                            viewModel.updateUpiAmount(it.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("UPI Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    viewModel.updateCashAmount(0.0)
                    viewModel.updateCardAmount(0.0)
                }

                "OTHERS" -> {
                    viewModel.updateCashAmount(0.0)
                    viewModel.updateCardAmount(0.0)
                    viewModel.updateUpiAmount(0.0)
                    val cashValue = remember(uiState.cashAmount) {
                        if (uiState.cashAmount == 0.0) "" else uiState.cashAmount.toString()
                    }
                    val cardValue = remember(uiState.cardAmount) {
                        if (uiState.cardAmount == 0.0) "" else uiState.cardAmount.toString()
                    }
                    val upiValue = remember(uiState.upiAmount) {
                        if (uiState.upiAmount == 0.0) "" else uiState.upiAmount.toString()
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = cashValue,
                        onValueChange = {
                            viewModel.updateCashAmount(it.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("Cash") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cardValue,
                        onValueChange = {
                            viewModel.updateCardAmount(it.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("Card") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = upiValue,
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