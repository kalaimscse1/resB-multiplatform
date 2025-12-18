package com.warriortech.resb.screens.settings

import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warriortech.resb.R
import com.warriortech.resb.model.TblCounter
import com.warriortech.resb.model.TblVoucherRequest
import com.warriortech.resb.model.TblVoucherResponse
import com.warriortech.resb.model.TblVoucherType
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.GradientStart
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.setting.VoucherSettingsViewModel
import com.warriortech.resb.util.CounterDropdown
import com.warriortech.resb.util.VoucherTypeDropdown
import com.warriortech.resb.util.stringResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherSettingsScreen(
    viewModel: VoucherSettingsViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    var editingVoucher by remember { mutableStateOf<TblVoucherResponse?>(null) }
    val counters by viewModel.counters.collectAsStateWithLifecycle()
    val voucherTypes by viewModel.voucherTypes.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadVouchers()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.voucher_settings), color = SurfaceLight) },
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
                ), actions = {
                    IconButton(onClick = { showAddSheet = true }) {
                        Icon(
                            Icons.Default.Add, contentDescription = "Add Voucher",
                            tint = SurfaceLight
                        )
                    }
                }

            )
        }
    ) { paddingValues ->

        when (val state = uiState) {
            is VoucherSettingsViewModel.VoucherSettingsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GradientStart)
                }
            }

            is VoucherSettingsViewModel.VoucherSettingsUiState.Success -> {
                if (state.vouchers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.no_vouchers_found))
                    }
                } else {
                    // Display list of vouchers
                    // You can implement a LazyColumn here to show the vouchers
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.vouchers) { counter ->
                            VoucherCard(
                                voucher = counter,
                                onEdit = {
                                    // Handle edit action
                                    editingVoucher = counter
                                },
                                onDelete = {
                                    viewModel.deleteVoucher(counter.voucher_id)
                                }
                            )
                        }
                    }
                }
            }

            is VoucherSettingsViewModel.VoucherSettingsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message)
                }
            }
        }
        if (showAddSheet) {
            VoucherBottomSheet(
                title = "Add Voucher",
                confirmText = "Add",
                onDismiss = { showAddSheet = false },
                onConfirm = { newVoucher ->
                    viewModel.addVoucher(newVoucher)
                    showAddSheet = false
                },
                counters = counters,
                voucherTypes = voucherTypes
            )
        }

        editingVoucher?.let { voucher ->
            VoucherBottomSheet(
                title = "Edit Voucher",
                initialVoucher = voucher,
                confirmText = "Update",
                onDismiss = { editingVoucher = null },
                onConfirm = { updatedVoucher ->
                    viewModel.updateVoucher(updatedVoucher)
                    editingVoucher = null
                },
                counters = counters,
                voucherTypes = voucherTypes
            )
        }
    }
}

@Composable
fun VoucherCard(
    voucher: TblVoucherResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    MobileOptimizedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = voucher.voucher_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Voucher Type:" + voucher.voucherType.voucher_type_name,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Prefix:" + voucher.voucher_prefix,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Suffix:" + voucher.voucher_suffix,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Starting:" + voucher.starting_no,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = BluePrimary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherBottomSheet(
    title: String,
    initialVoucher: TblVoucherResponse? = null,
    counters: List<TblCounter>,
    voucherTypes: List<TblVoucherType>,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (TblVoucherRequest) -> Unit
) {
    // Prefill state if editing, otherwise empty values
    var voucherName by remember { mutableStateOf(initialVoucher?.voucher_name ?: "") }
    var voucherPrefix by remember { mutableStateOf(initialVoucher?.voucher_prefix ?: "") }
    var voucherSuffix by remember { mutableStateOf(initialVoucher?.voucher_suffix ?: "") }
    var startingNo by remember { mutableStateOf(initialVoucher?.starting_no ?: 1) }
    var isActive by remember { mutableStateOf(initialVoucher?.is_active ?: true) }
    var counterId by remember { mutableStateOf(initialVoucher?.counter?.counter_id ?: 0L) }
    var voucherTypeId by remember {
        mutableStateOf(
            initialVoucher?.voucherType?.voucher_Type_id ?: 1L
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(50)
                        )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Fields
            OutlinedTextField(
                value = voucherName,
                onValueChange = { voucherName = it },
                label = { Text("Voucher Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = voucherPrefix,
                onValueChange = { voucherPrefix = it },
                label = { Text("Prefix") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = voucherSuffix,
                onValueChange = { voucherSuffix = it },
                label = { Text("Suffix") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = startingNo.toString(),
                onValueChange = { input ->
                    startingNo = input.toIntOrNull() ?: 1
                },
                label = { Text("Starting No") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            CounterDropdown(
                modifier = Modifier.fillMaxWidth(),
                label = "Select Counter",
                counters = counters,
                selectedCounter = counters.find { it.counter_id == counterId },
                onCounterSelected = { counterId = it.counter_id },
            )
            VoucherTypeDropdown(
                modifier = Modifier.fillMaxWidth(),
                label = "Select Voucher Type",
                voucherTypes = voucherTypes,
                selectedVoucherType = voucherTypes.find { it.voucher_Type_id == voucherTypeId },
                onVoucherTypeSelected = { voucherTypeId = it.voucher_Type_id },
            )

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                }) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val updatedVoucher = TblVoucherRequest(
                            voucher_name = voucherName,
                            voucher_prefix = voucherPrefix,
                            voucher_suffix = voucherSuffix,
                            starting_no = startingNo.toString(),
                            voucher_id = initialVoucher?.voucher_id ?: 0L,
                            counter_id = counterId,
                            is_active = isActive,
                            voucher_type = voucherTypeId
                        )
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            onConfirm(updatedVoucher)
                        }
                    },
                    enabled = voucherName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(confirmText)
                }
            }
        }
    }
}
