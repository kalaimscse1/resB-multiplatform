package com.warriortech.resb.screens.settings

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.warriortech.resb.R
import com.warriortech.resb.ui.viewmodel.setting.GeneralSettingsViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.GeneralSettings
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(
    viewModel: GeneralSettingsViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTable by remember { mutableStateOf<GeneralSettings?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.general_settings),
                        color = SurfaceLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = SurfaceLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is GeneralSettingsViewModel.UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is GeneralSettingsViewModel.UiState.Success -> {
                    if (state.generalSettings.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No General Setting found",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        return@Column
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(state.generalSettings) { setting ->

                                GeneralSettingDialog(
                                    setting = setting,
                                    onDismiss = {
                                        showAddDialog = false
                                        editingTable = null
                                    },
                                    onSave = { newSetting ->
                                        scope.launch {
                                            viewModel.updateSettings(newSetting)
                                            snackbarHostState.showSnackbar("General Settings updated successfully")
                                        }
                                        showAddDialog = false
                                        editingTable = null
                                    }
                                )
                            }
                        }
                    }
                }

                is GeneralSettingsViewModel.UiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center

                    ) {

                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { viewModel.loadSettings() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GeneralSettingDialog(
    setting: GeneralSettings?,
    onDismiss: () -> Unit,
    onSave: (GeneralSettings) -> Unit
) {
    var companyNameFont by remember { mutableStateOf(setting?.company_name_font?.toString() ?: "") }
    var addressFont by remember { mutableStateOf(setting?.address_font?.toString() ?: "") }
    var isTax by remember { mutableStateOf(setting?.is_tax ?: false) }
    var isTaxIncluded by remember { mutableStateOf(setting?.is_tax_included ?: false) }
    var isRoundOff by remember { mutableStateOf(setting?.is_round_off ?: false) }
    var isAllowedDisc by remember { mutableStateOf(setting?.is_allowed_disc ?: false) }
    var discBy by remember { mutableStateOf(setting?.disc_by?.toString() ?: "") }
    var discAmt by remember { mutableStateOf(setting?.disc_amt?.toString() ?: "") }
    var isTendered by remember { mutableStateOf(setting?.is_tendered ?: false) }
    var isGstSummary by remember { mutableStateOf(setting?.is_gst_summary ?: false) }
    var isReceipt by remember { mutableStateOf(setting?.is_receipt ?: false) }
    var isKot by remember { mutableStateOf(setting?.is_kot ?: false) }
    var isLogo by remember { mutableStateOf(setting?.is_logo ?: false) }
    var logoPath by remember { mutableStateOf(setting?.logo_path ?: "") }
    var cess by remember { mutableStateOf(setting?.is_cess ?: false) }
    var deliveryCharge by remember { mutableStateOf(setting?.is_delivery_charge ?: false) }
    var isTableAllowed by remember { mutableStateOf(setting?.is_table_allowed ?: true) }
    var isWaiterAllowed by remember { mutableStateOf(setting?.is_waiter_allowed ?: true) }
    var menuShowInTime by remember { mutableStateOf(setting?.menu_show_in_time ?: true) }
    var tamilReceiptPrint by remember { mutableStateOf(setting?.tamil_receipt_print ?: false) }
    var logoSize by remember { mutableStateOf(setting?.logo_size ?: 0L) }
    var isSplitGst by remember { mutableStateOf(setting?.is_split_gst ?: false) }
    var billFooter by remember { mutableStateOf(setting?.bill_footer ?: "") }
    var isCompanyShow by remember { mutableStateOf(setting?.is_split_gst ?: false) }
    var isQrShow by remember { mutableStateOf(setting?.is_qr_show ?: false) }
    var remark1 by remember { mutableStateOf(setting?.remark1 ?: "") }
    var remark2 by remember { mutableStateOf(setting?.remark2 ?: "") }
    var businessDate by remember { mutableStateOf(setting?.business_date ?: "") }
    var isAccounts by remember { mutableStateOf(setting?.is_accounts ?: false) }
    var isInventory by remember { mutableStateOf(setting?.is_inventory ?: false) }

    Column {
        OutlinedTextField(
            value = companyNameFont,
            onValueChange = { companyNameFont = it },
            label = { Text("Company Name Font") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = addressFont,
            onValueChange = { addressFont = it },
            label = { Text("Address Font") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isTax,
                onCheckedChange = { isTax = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tax Applicable")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isTaxIncluded,
                onCheckedChange = { isTaxIncluded = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tax Included")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isRoundOff,
                onCheckedChange = { isRoundOff = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("IS Round Off")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isAllowedDisc,
                onCheckedChange = { isAllowedDisc = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Discount Allowed")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = discBy,
            onValueChange = { discBy = it },
            label = { Text("Discount By % OR Amount") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = discAmt,
            onValueChange = { discAmt = it },
            label = { Text("Discount Amount") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isTendered,
                onCheckedChange = { isTendered = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("IS Tendered")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isGstSummary,
                onCheckedChange = { isGstSummary = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tax Summary in Bill")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isReceipt,
                onCheckedChange = { isReceipt = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Print Bill")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isKot,
                onCheckedChange = { isKot = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("KOT")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isLogo,
                onCheckedChange = { isLogo = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logo In Bill")
        }
        OutlinedTextField(
            value = logoPath,
            onValueChange = { logoPath = it },
            label = { Text("LOGO PATH") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = cess,
                onCheckedChange = { cess = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cess Applicable")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = deliveryCharge,
                onCheckedChange = { deliveryCharge = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delivery Charge Applicable")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isWaiterAllowed,
                onCheckedChange = { isWaiterAllowed = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Waiter Validation")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isTableAllowed,
                onCheckedChange = { isTableAllowed = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Table Validation")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = menuShowInTime,
                onCheckedChange = { menuShowInTime = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Menu Show In Time")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = tamilReceiptPrint,
                onCheckedChange = { tamilReceiptPrint = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Receipt Print In Tamil")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isCompanyShow,
                onCheckedChange = { isCompanyShow = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Show Company Details in Bill")
        }
        OutlinedTextField(
            value = billFooter,
            onValueChange = { billFooter = it },
            label = { Text("Bill Footer") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = logoSize.toString(),
            onValueChange = { logoSize = it.toLongOrNull() ?: 0L },
            label = { Text("Logo Size") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isSplitGst,
                onCheckedChange = { isSplitGst = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Split GST")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isQrShow,
                onCheckedChange = { isQrShow = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Show QR Code in Bill")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = remark1,
            onValueChange = { remark1 = it },
            label = { Text("Remark 1") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = remark2,
            onValueChange = { remark2 = it },
            label = { Text("Remark 2") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = businessDate,
            onValueChange = { businessDate = it },
            label = { Text("Business Date") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isAccounts,
                onCheckedChange = { isAccounts = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Accounts Module Enabled")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isInventory,
                onCheckedChange = { isInventory = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Inventory Module Enabled")
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                val newSetting = GeneralSettings(
                    id = setting?.id ?: 0,
                    company_name_font = companyNameFont.toInt(),
                    address_font = addressFont.toInt(),
                    is_tax = isTax,
                    is_tax_included = isTaxIncluded,
                    is_round_off = isRoundOff,
                    is_allowed_disc = isAllowedDisc,
                    disc_by = discBy.toIntOrNull() ?: 0,
                    disc_amt = discAmt.toDoubleOrNull() ?: 0.0,
                    is_tendered = isTendered,
                    is_gst_summary = isGstSummary,
                    is_receipt = isReceipt,
                    is_kot = isKot,
                    is_logo = isLogo,
                    logo_path = logoPath,
                    is_cess = cess,
                    is_delivery_charge = deliveryCharge,
                    is_table_allowed = isTableAllowed,
                    is_waiter_allowed = isWaiterAllowed,
                    menu_show_in_time = menuShowInTime,
                    tamil_receipt_print = tamilReceiptPrint,
                    logo_size = logoSize,
                    is_split_gst = isSplitGst,
                    bill_footer = billFooter,
                    is_company_show = isCompanyShow,
                    is_qr_show = isQrShow,
                    remark1 = remark1,
                    remark2 = remark2,
                    business_date = businessDate,
                    is_accounts = isAccounts,
                    is_inventory = isInventory
                )
                onSave(newSetting)
            }
        ) {
            Text(
                text = if (setting == null) "Add Setting" else "Update ",
                fontWeight = FontWeight.Bold
            )
        }
    }
}