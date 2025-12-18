package com.warriortech.resb.screens

import android.icu.text.NumberFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.warriortech.resb.model.*
import com.warriortech.resb.ui.theme.GradientStart
import com.warriortech.resb.ui.viewmodel.TemplateViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillTemplateScreen(
    navController: NavController,
    billId: Long,
    templateViewModel: TemplateViewModel = hiltViewModel(),
) {
    val templateUiState by templateViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTemplate by remember { mutableStateOf<ReceiptTemplate?>(null) }
    var showPreview by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }

    // Load bill and templates
    LaunchedEffect(billId) {
        templateViewModel.loadTemplates()
    }

    // Get default bill template
    LaunchedEffect(templateUiState.templates) {
        selectedTemplate = templateUiState.templates.find {
            it.type == ReceiptType.BILL && it.isDefault
        } ?: templateUiState.templates.firstOrNull { it.type == ReceiptType.BILL }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Bill Template") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GradientStart
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {

        }
    }

    // Preview Dialog


    // Edit Dialog
    if (showEdit && selectedTemplate != null) {
        BillTemplateEditDialog(
            template = selectedTemplate!!,
            onSave = { updatedTemplate ->
                templateViewModel.saveTemplate(updatedTemplate)
                selectedTemplate = updatedTemplate
                showEdit = false
            },
            onDismiss = { showEdit = false }
        )
    }
}

@Composable
fun BillTemplateActions(
    template: ReceiptTemplate,
    bill: PaidBill,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onPrint: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onPreview,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Preview, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Preview")
        }

        Button(
            onClick = onEdit,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit")
        }

        Button(
            onClick = onPrint,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Icon(Icons.Default.Print, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Print")
        }
    }
}

@Composable
fun TemplateSelector(
    templates: List<ReceiptTemplate>,
    selectedTemplate: ReceiptTemplate,
    onTemplateSelected: (ReceiptTemplate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Select Template",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            templates.forEach { template ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = template.id == selectedTemplate.id,
                        onClick = { onTemplateSelected(template) }
                    )
                    Text(
                        text = template.name,
                        modifier = Modifier.weight(1f)
                    )
                    if (template.isDefault) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "DEFAULT",
                                modifier = Modifier.padding(4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BillTemplatePreview(
    bill: PaidBill,
    template: ReceiptTemplate
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = when (template.headerSettings.textAlign) {
                TextAligns.LEFT -> Alignment.Start
                TextAligns.CENTER -> Alignment.CenterHorizontally
                TextAligns.RIGHT -> Alignment.End
            }
        ) {
            // Header Section
            Text(
                text = template.headerSettings.businessName,
                fontSize = template.headerSettings.fontSize.sp,
                fontWeight = when (template.headerSettings.fontWeight) {
                    FontWeights.NORMAL -> FontWeight.Normal
                    FontWeights.BOLD -> FontWeight.Bold
                    FontWeights.LIGHT -> FontWeight.Light
                },
                textAlign = when (template.headerSettings.textAlign) {
                    TextAligns.LEFT -> TextAlign.Start
                    TextAligns.CENTER -> TextAlign.Center
                    TextAligns.RIGHT -> TextAlign.End
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (template.headerSettings.businessAddress.isNotEmpty()) {
                Text(
                    text = template.headerSettings.businessAddress,
                    fontSize = (template.headerSettings.fontSize - 2).sp,
                    textAlign = when (template.headerSettings.textAlign) {
                        TextAligns.LEFT -> TextAlign.Start
                        TextAligns.CENTER -> TextAlign.Center
                        TextAligns.RIGHT -> TextAlign.End
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (template.headerSettings.businessPhone.isNotEmpty()) {
                Text(
                    text = "Phone: ${template.headerSettings.businessPhone}",
                    fontSize = (template.headerSettings.fontSize - 2).sp,
                    textAlign = when (template.headerSettings.textAlign) {
                        TextAligns.LEFT -> TextAlign.Start
                        TextAligns.CENTER -> TextAlign.Center
                        TextAligns.RIGHT -> TextAlign.End
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bill Information
            Text(
                text = "BILL",
                fontSize = template.bodySettings.fontSize.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Bill #: ${bill.billNo}",
                    fontSize = template.bodySettings.fontSize.sp
                )
                Text(
                    text = dateFormatter.format(bill.paymentDate),
                    fontSize = template.bodySettings.fontSize.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Customer: ${bill.customerName}",
                    fontSize = template.bodySettings.fontSize.sp
                )
                if (bill.customerPhone.isNotEmpty()) {
                    Text(
                        text = "Phone: ${bill.customerPhone}",
                        fontSize = template.bodySettings.fontSize.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Items Section
            Text(
                text = "Items:",
                fontSize = template.bodySettings.fontSize.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Note: In a real implementation, you'd need to get the actual bill items
            // For now, showing placeholder items
            repeat(3) { index ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Item ${index + 1}",
                        fontSize = template.bodySettings.fontSize.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (template.bodySettings.showQuantity) {
                        Text(
                            text = "x2",
                            fontSize = template.bodySettings.fontSize.sp
                        )
                    }
                    Text(
                        text = currencyFormatter.format(10.0 + index),
                        fontSize = template.bodySettings.fontSize.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Section
            if (template.bodySettings.showTotal) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total:",
                        fontSize = template.bodySettings.fontSize.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currencyFormatter.format(bill.totalAmount),
                        fontSize = template.bodySettings.fontSize.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Section
            if (template.footerSettings.showThankYou) {
                Text(
                    text = "THANK YOU!",
                    fontSize = template.footerSettings.fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (template.footerSettings.showDateTime) {
                Text(
                    text = "Printed: ${dateFormatter.format(Date())}",
                    fontSize = (template.footerSettings.fontSize - 2).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Notes
            if (!bill.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notes: ${bill.notes}",
                    fontSize = template.bodySettings.fontSize.sp,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun BillPreviewDialog(
    bill: PaidBill,
    template: ReceiptTemplate,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bill Preview") },
        text = {
            BillTemplatePreview(
                bill = bill,
                template = template
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun BillTemplateEditDialog(
    template: ReceiptTemplate,
    onSave: (ReceiptTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    var editedTemplate by remember { mutableStateOf(template) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Template") },
        text = {
            Column {
                OutlinedTextField(
                    value = editedTemplate.name,
                    onValueChange = { editedTemplate = editedTemplate.copy(name = it) },
                    label = { Text("Template Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedTemplate.headerSettings.businessName,
                    onValueChange = {
                        editedTemplate = editedTemplate.copy(
                            headerSettings = editedTemplate.headerSettings.copy(businessName = it)
                        )
                    },
                    label = { Text("Business Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = editedTemplate.footerSettings.showThankYou,
                        onCheckedChange = {
                            editedTemplate = editedTemplate.copy(
                                footerSettings = editedTemplate.footerSettings.copy(showThankYou = it)
                            )
                        }
                    )
                    Text("Show Thank You")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(editedTemplate) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
