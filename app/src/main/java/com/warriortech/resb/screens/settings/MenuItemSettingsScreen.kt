package com.warriortech.resb.screens.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warriortech.resb.model.KitchenCategory
import com.warriortech.resb.model.Menu
import com.warriortech.resb.model.MenuCategory
import com.warriortech.resb.model.Tax
import com.warriortech.resb.model.TblMenuItemRequest
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.viewmodel.master.MenuItemSettingsViewModel
import com.warriortech.resb.util.StringDropdown
import kotlinx.coroutines.launch
import com.warriortech.resb.model.TblUnit
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.ui.components.BarcodeInputField
import com.warriortech.resb.ui.components.CameraBarcodeScanner
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.KitchenGroupDropdown
import com.warriortech.resb.util.MenuCategoryDropdown
import com.warriortech.resb.util.MenuDropdown
import com.warriortech.resb.util.ReportExport
import com.warriortech.resb.util.ReusableBottomSheet
import com.warriortech.resb.util.SuccessDialogWithButton
import com.warriortech.resb.util.TaxDropdown
import com.warriortech.resb.util.UnitDropdown

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemSettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: MenuItemSettingsViewModel = hiltViewModel(),
    sessionManager: SessionManager,
    drawerState: DrawerState
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val menus by viewModel.menus.collectAsStateWithLifecycle()
    val menuCategories by viewModel.menuCategories.collectAsStateWithLifecycle()
    val kitchenCategories by viewModel.kitchenCategories.collectAsStateWithLifecycle()
    val taxes by viewModel.taxes.collectAsStateWithLifecycle()
    val units by viewModel.units.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val menuItems by viewModel.filteredMenuItems.collectAsStateWithLifecycle() // ✅ updated

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingMenuItem by remember { mutableStateOf<TblMenuItemResponse?>(null) }

    var searchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var success by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.loadMenuItems()
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            if (errorMessage == "Menu item deleted successfully") {
                success = true
            } else {
                failed = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Item Settings", color = SurfaceLight) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = SurfaceLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen),
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = SurfaceLight)
                    }
                    IconButton(onClick = {
                        ReportExport.importMenuItems(context)
                    }) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Import", tint = SurfaceLight)
                    }
                    IconButton(onClick = {
                        if (searchMode) {
                            // close search mode
                            searchMode = false
                            searchQuery = ""
                            viewModel.searchMenuItems("") // reset full list
                        } else {
                            // open search mode
                            searchMode = true
                        }
                    }) {
                        Icon(
                            imageVector = if (searchMode) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = SurfaceLight
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomAppBar(
                containerColor = PrimaryGreen,
                contentColor = SurfaceLight,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Total: ${menuItems.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = {
                   viewModel.printMenuItems(menuItems, 48)
                }) {
                    Icon(Icons.Default.Print, contentDescription = "Print Menu Items")
                }
                IconButton(onClick = {
                    ReportExport.menuItemsExportToPdf(context, menuItems, sessionManager)
                }) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF")
                }
                IconButton(onClick = {
                    ReportExport.menuItemsExportToExcel(context, menuItems, sessionManager)
                }) {
                    Icon(Icons.Default.TableChart, contentDescription = "Export Excel")
                }
                IconButton(onClick = {
                    ReportExport.viewReport(context, "MenuItemsReport.pdf", "application/pdf")
                }) {
                    Icon(Icons.Default.Visibility, contentDescription = "View PDF")
                }
                IconButton(onClick = {
                    ReportExport.viewReport(
                        context,
                        "MenuItemsReport.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
                }) {
                    Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = "View Excel")
                }
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is MenuItemSettingsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            is MenuItemSettingsUiState.Success -> {
                if (menuItems.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No menu items found", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (searchMode) {
                            item {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = {
                                        searchQuery = it
                                        viewModel.searchMenuItems(it)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester),
                                    placeholder = { Text("Search Menu Items") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search Icon"
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                                )
                            }
                        }

                        items(menuItems) { menuItem ->
                            MenuItemCard(
                                menuItem = menuItem,
                                onEdit = { editingMenuItem = menuItem },
                                onDelete = {
                                    scope.launch {
                                        viewModel.deleteMenuItem(menuItem.menu_item_id.toInt())
                                    }
                                }
                            )
                        }
                    }
                }
            }

            is MenuItemSettingsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Add/Edit Dialog
        if (showAddDialog || editingMenuItem != null) {
            MenuItemDialog(
                menuItem = editingMenuItem,
                onDismiss = {
                    showAddDialog = false
                    editingMenuItem = null
                },
                onSave = { menuItem ->
                    scope.launch {
                        if (editingMenuItem != null) {
                            viewModel.updateMenuItem(menuItem)
                        } else {
                            viewModel.addMenuItem(menuItem)
                        }
                        showAddDialog = false
                        editingMenuItem = null
                    }
                },
                menus = menus,
                menuCategories = menuCategories,
                kitchenCategories = kitchenCategories,
                taxes = taxes,
                units = units
            )
        }

        // Success / Failure Dialogs
        if (success) {
            SuccessDialogWithButton(
                title = "Success",
                description = errorMessage.toString(),
                paddingValues = paddingValues,
                onClick = {
                    success = false
                    viewModel.loadMenuItems()
                    viewModel.clearErrorMessage()
                }
            )
        }

        if (failed) {
            SuccessDialogWithButton(
                title = "Failure",
                description = errorMessage.toString(),
                paddingValues = paddingValues,
                onClick = {
                    failed = false
                    viewModel.loadMenuItems()
                    viewModel.clearErrorMessage()
                }
            )
        }
    }
}

@Composable
fun MenuItemCard(
    menuItem: TblMenuItemResponse,
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = menuItem.menu_item_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = CurrencySettings.format(menuItem.rate),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (menuItem.menu_item_name_tamil.isNotEmpty()) {
                    Text(
                        text = menuItem.menu_item_name_tamil,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = BluePrimary)
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

@Composable
fun MenuItemDialog(
    menuItem: TblMenuItemResponse?,
    onDismiss: () -> Unit,
    onSave: (TblMenuItemRequest) -> Unit,
    menus: List<Menu>,
    menuCategories: List<MenuCategory>,
    kitchenCategories: List<KitchenCategory>,
    taxes: List<Tax>,
    units: List<TblUnit>,
) {
    val rateOptions = listOf("YES", "NO")

    val nameFocus = remember { FocusRequester() }
    val nameTamilFocus = remember { FocusRequester() }
    val rateFocus = remember { FocusRequester() }
    val acRateFocus = remember { FocusRequester() }
    val parcelRateFocus = remember { FocusRequester() }
    val parcelChargeFocus = remember { FocusRequester() }
    val preparationTimeFocus = remember { FocusRequester() }
    val hsnCodeFocus = remember { FocusRequester() }
    val minStockFocus = remember { FocusRequester() }
    val orderByFocus = remember { FocusRequester() }
    // Menu fields
    var name by remember { mutableStateOf(menuItem?.menu_item_name ?: "") }
    var nameTamil by remember { mutableStateOf(menuItem?.menu_item_name_tamil ?: "") }
    var menuId by remember { mutableStateOf(menuItem?.menu_id ?: 1) }
    var menuItemCatId by remember { mutableStateOf(menuItem?.item_cat_id ?: 1) }
    var kitchenCatId by remember { mutableStateOf(menuItem?.kitchen_cat_id ?: 1) }
    var isAvailable by remember { mutableStateOf(menuItem?.is_available ?: "YES") }
    var taxId by remember { mutableStateOf(menuItem?.tax_id ?: 1) }
    var rate by remember { mutableStateOf(menuItem?.rate?.toString() ?: "") }
    var rateLock by remember { mutableStateOf(menuItem?.rate_lock ?: rateOptions.first()) }
    var orderBy by remember { mutableStateOf(menuItem?.order_by ?: 1) }
    var acRate by remember { mutableStateOf(menuItem?.ac_rate?.toString() ?: "") }
    var parcelRate by remember { mutableStateOf(menuItem?.parcel_rate?.toString() ?: "") }
    var parcelCharge by remember { mutableStateOf(menuItem?.parcel_charge?.toString() ?: "") }
    var preparationTime by remember { mutableStateOf(menuItem?.preparation_time ?: 0) }
    var isFavourite by remember { mutableStateOf(menuItem?.is_favourite == true) }

    val focusManager = LocalFocusManager.current
    // Inventory fields
    var isInventory by remember { mutableStateOf(menuItem?.is_inventory ?: 0L) }
    var hsnCode by remember { mutableStateOf(menuItem?.hsn_code ?: "") }
    var minStock by remember { mutableStateOf(menuItem?.min_stock ?: 0) }
    var isRaw by remember { mutableStateOf(menuItem?.is_raw ?: "NO") }
    var stockMaintain by remember { mutableStateOf(menuItem?.stock_maintain ?: "NO") }
    var unitId by remember { mutableStateOf(menuItem?.unit_id ?: 1) }
    var isActive by remember { mutableStateOf(menuItem?.is_active ?: 1) }
    var barcode by remember { mutableStateOf(menuItem?.menu_item_code ?: "") }
    var showScanner by remember { mutableStateOf(false) }


    ReusableBottomSheet(
        onDismiss = onDismiss,
        title = if (menuItem != null) "Edit Menu Item" else "Add Menu Item",
        onSave = {
            val newMenuItem = TblMenuItemRequest(
                menu_item_id = menuItem?.menu_item_id ?: 0,
                menu_item_name = name,
                menu_item_name_tamil = nameTamil,
                item_cat_id = menuItemCatId,
                rate = rate.toDoubleOrNull() ?: 0.0,
                ac_rate = acRate.toDoubleOrNull() ?: 0.0,
                parcel_rate = parcelRate.toDoubleOrNull() ?: 0.0,
                parcel_charge = parcelCharge.toDoubleOrNull() ?: 0.0,
                tax_id = taxId,
                cess_specific = 0.0,
                kitchen_cat_id = kitchenCatId,
                stock_maintain = stockMaintain,
                rate_lock = rateLock,
                unit_id = unitId,
                min_stock = minStock,
                hsn_code = hsnCode,
                order_by = orderBy,
                is_inventory = isInventory,
                is_raw = isRaw,
                is_available = isAvailable,
                menu_item_code = barcode,
                menu_id = menuId,
                is_favourite = isFavourite,
                is_active = isActive,
                image = "",
                preparation_time = preparationTime
            )
            onSave(newMenuItem)
        },
        isSaveEnabled = name.isNotBlank() && rate.isNotBlank(),
        buttonText = if (menuItem != null) "Update" else "Add"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp) // limit dialog height
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Common Menu Fields
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.uppercase() },
                label = { Text("Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(nameFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { nameTamilFocus.requestFocus() }
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nameTamil,
                onValueChange = { nameTamil = it },
                label = { Text("Name (Tamil)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(nameTamilFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { rateFocus.requestFocus() }
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            BarcodeInputField(
                value = barcode,
                onValueChange = { barcode = it },
                onBarcodeScanned = { barcode = it },
                onCameraClick = { showScanner = true }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = rate,
                onValueChange = { rate = it },
                label = { Text("Rate") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(rateFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { acRateFocus.requestFocus() }
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = acRate,
                onValueChange = { acRate = it },
                label = { Text("AC Rate") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(acRateFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { parcelRateFocus.requestFocus() }
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = parcelRate,
                onValueChange = { parcelRate = it },
                label = { Text("Parcel Rate") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(parcelRateFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { parcelChargeFocus.requestFocus() }
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = parcelCharge,
                onValueChange = { parcelCharge = it },
                label = { Text("Parcel Charge") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(parcelChargeFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { preparationTimeFocus.requestFocus() }
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = preparationTime.toString(),
                onValueChange = { preparationTime = it.toLongOrNull() ?: 0 },
                label = { Text("Preparation Time (min)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(preparationTimeFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { hsnCodeFocus.requestFocus() }
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = hsnCode,
                onValueChange = { hsnCode = it },
                label = { Text("HSN Code") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(hsnCodeFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { minStockFocus.requestFocus() }
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = minStock.toString(),
                onValueChange = { minStock = it.toLongOrNull() ?: 0 },
                label = { Text("Min Stock") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(minStockFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { orderByFocus.requestFocus() }
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = orderBy.toString(),
                onValueChange = { orderBy = it.toLongOrNull() ?: 0 },
                label = { Text("Order By") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(orderByFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            // Dropdowns
            StringDropdown(
                options = rateOptions,
                selectedOption = rateLock,
                onOptionSelected = { rateLock = it },
                label = "Rate Lock",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            TaxDropdown(
                taxes = taxes,
                onTaxSelected = { taxId = it.tax_id },
                label = "Select Tax",
                modifier = Modifier.fillMaxWidth(),
                selectedTax = taxes.find { it.tax_id == taxId }
            )
            Spacer(modifier = Modifier.height(8.dp))

            MenuDropdown(
                menus = menus,
                onMenuSelected = { menuId = it.menu_id },
                label = "Select Menu",
                modifier = Modifier.fillMaxWidth(),
                selectedMenu = menus.find { it.menu_id == menuId }
            )
            Spacer(modifier = Modifier.height(8.dp))

            MenuCategoryDropdown(
                menus = menuCategories,
                onMenuCategorySelected = { menuItemCatId = it.item_cat_id },
                label = "Select Menu Category",
                modifier = Modifier.fillMaxWidth(),
                selectedMenuCategory = menuCategories.find { it.item_cat_id == menuItemCatId }
            )
            Spacer(modifier = Modifier.height(8.dp))

            KitchenGroupDropdown(
                menus = kitchenCategories,
                onKitchenCategorySelected = { kitchenCatId = it.kitchen_cat_id },
                label = "Select Kitchen Category",
                modifier = Modifier.fillMaxWidth(),
                selectedKitchenCategory = kitchenCategories.find { it.kitchen_cat_id == kitchenCatId }
            )
            Spacer(modifier = Modifier.height(8.dp))

            StringDropdown(
                options = listOf("YES", "NO"),
                selectedOption = isAvailable,
                onOptionSelected = { isAvailable = it },
                label = "Is Available",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = isFavourite,
                    onCheckedChange = { isFavourite = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Is Favourite")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Switch to enable Inventory
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = isInventory == 1L,
                    onCheckedChange = { isInventory = if (it) 1L else 0L }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Inventory Enabled")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Inventory-only fields
            if (isInventory == 1L) {
                OutlinedTextField(
                    value = hsnCode,
                    onValueChange = { hsnCode = it },
                    label = { Text("HSN Code") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(hsnCodeFocus),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { minStockFocus.requestFocus() }
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = minStock.toString(),
                    onValueChange = { minStock = it.toLong() },
                    label = { Text("Minimum Stock") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(minStockFocus),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { orderByFocus.requestFocus() }
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                UnitDropdown(
                    menus = units,
                    onUnitSelected = { unitId = it.unit_id },
                    label = "Select Unit",
                    modifier = Modifier.fillMaxWidth(),
                    selectedUnit = units.find { it.unit_id == unitId }
                )
                Spacer(modifier = Modifier.height(8.dp))

                StringDropdown(
                    options = listOf("YES", "NO"),
                    selectedOption = isRaw,
                    onOptionSelected = { isRaw = it },
                    label = "Is Raw",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                StringDropdown(
                    options = listOf("YES", "NO"),
                    selectedOption = stockMaintain,
                    onOptionSelected = { stockMaintain = it },
                    label = "Stock Maintain",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showScanner) {
        Dialog(onDismissRequest = { showScanner = false }) {
            Box(Modifier.fillMaxSize()) {
                CameraBarcodeScanner(
                    onResult = {
                        barcode = it
                    },
                    onClose = {
                        showScanner = false
                    }
                )
            }
        }
    }
}
//data class MenuItemFormState(
//    val name: String = "",
//    val nameTamil: String = "",
//    val menuId: Int = 1,
//    val menuItemCatId: Int = 1,
//    val kitchenCatId: Int = 1,
//    val isAvailable: String = "YES",
//    val taxId: Int = 1,
//    val rate: String = "",
//    val rateLock: String = "YES",
//    val orderBy: Int = 1,
//    val acRate: String = "",
//    val parcelRate: String = "",
//    val parcelCharge: String = "",
//    val preparationTime: Int = 0,
//    val isFavourite: Boolean = false,
//    val isInventory: Long = 0L,
//    val hsnCode: String = "",
//    val minStock: Int = 0,
//    val isRaw: String = "NO",
//    val stockMaintain: String = "NO",
//    val unitId: Int = 1,
//    val isActive: Int = 1
//)
//
//
//fun MenuItemFormState.copyFrom(menuItem: TblMenuItemResponse?): MenuItemFormState {
//    return this.copy(
//        name = menuItem?.menu_item_name ?: "",
//        nameTamil = menuItem?.menu_item_name_tamil ?: "",
//        menuId = (menuItem?.menu_id ?: 1).toInt(),
//        menuItemCatId = (menuItem?.item_cat_id ?: 1).toInt(),
//        kitchenCatId = (menuItem?.kitchen_cat_id ?: 1).toInt(),
//        isAvailable = menuItem?.is_available ?: "YES",
//        taxId = (menuItem?.tax_id ?: 1).toInt(),
//        rate = menuItem?.rate?.toString() ?: "",
//        rateLock = menuItem?.rate_lock ?: "YES",
//        orderBy = (menuItem?.order_by ?: 1).toInt(),
//        acRate = menuItem?.ac_rate?.toString() ?: "",
//        parcelRate = menuItem?.parcel_rate?.toString() ?: "",
//        parcelCharge = menuItem?.parcel_charge?.toString() ?: "",
//        preparationTime = (menuItem?.preparation_time ?: 0).toInt(),
//        isFavourite = menuItem?.is_favourite == true,
//        isInventory = menuItem?.is_inventory ?: 0L,
//        hsnCode = menuItem?.hsn_code ?: "",
//        minStock = (menuItem?.min_stock ?: 0).toInt(),
//        isRaw = menuItem?.is_raw ?: "NO",
//        stockMaintain = menuItem?.stock_maintain ?: "NO",
//        unitId = (menuItem?.unit_id ?: 1).toInt(),
//        isActive = (menuItem?.is_active ?: 1).toInt()
//    )
//}
//
//@Composable
//fun MenuItemDialog(
//    menuItem: TblMenuItemResponse?,
//    onDismiss: () -> Unit,
//    onSave: (TblMenuItemRequest) -> Unit,
//    menus: List<Menu>,
//    menuCategories: List<MenuCategory>,
//    kitchenCategories: List<KitchenCategory>,
//    taxes: List<Tax>,
//    units: List<TblUnit>,
//) {
//    var formState by remember(menuItem) {
//        mutableStateOf(MenuItemFormState().copyFrom(menuItem))
//    }
//
//    val isSaveEnabled by remember {
//        derivedStateOf {
//            formState.name.isNotBlank() &&
//                    formState.rate.toDoubleOrNull() != null &&
//                    formState.rate.isNotBlank()
//        }
//    }
//
//    val focusManager = LocalFocusManager.current
//    val coroutineScope = rememberCoroutineScope()
//
//    ReusableBottomSheet(
//        onDismiss = onDismiss,
//        title = if (menuItem != null) "Edit Menu Item" else "Add Menu Item",
//        onSave = {
//            val request = TblMenuItemRequest(
//                menu_item_id = menuItem?.menu_item_id ?: 0,
//                menu_item_name = formState.name,
//                menu_item_name_tamil = formState.nameTamil,
//                item_cat_id = formState.menuItemCatId.toLong(),
//                rate = formState.rate.toDoubleOrNull() ?: 0.0,
//                ac_rate = formState.acRate.toDoubleOrNull() ?: 0.0,
//                parcel_rate = formState.parcelRate.toDoubleOrNull() ?: 0.0,
//                parcel_charge = formState.parcelCharge.toDoubleOrNull() ?: 0.0,
//                tax_id = formState.taxId.toLong(),
//                cess_specific = 0.0,
//                kitchen_cat_id = formState.kitchenCatId.toLong(),
//                stock_maintain = formState.stockMaintain,
//                rate_lock = formState.rateLock,
//                unit_id = formState.unitId.toLong(),
//                min_stock = formState.minStock.toLong(),
//                hsn_code = formState.hsnCode,
//                order_by = formState.orderBy.toLong(),
//                is_inventory = formState.isInventory,
//                is_raw = formState.isRaw,
//                is_available = formState.isAvailable,
//                menu_item_code = menuItem?.menu_item_code ?: "",
//                menu_id = formState.menuId.toLong(),
//                is_favourite = formState.isFavourite,
//                is_active = formState.isActive.toLong(),
//                image = "",
//                preparation_time = formState.preparationTime.toLong()
//            )
//            onSave(request)
//        },
//        isSaveEnabled = isSaveEnabled,
//        buttonText = if (menuItem != null) "Update" else "Add"
//    ) {
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxWidth()
//                .fillMaxHeight(0.9f),
//            verticalArrangement = Arrangement.spacedBy(12.dp),
//            contentPadding = PaddingValues(16.dp)
//        ) {
//            item { BasicInfoSection(formState, menus, menuCategories, { newState -> formState = newState }, focusManager) }
//            item { RateSection(formState, { newState -> formState = newState }, focusManager) }
//            item { AvailabilitySection(formState, { newState -> formState = newState }) }
//            item {
//                InventorySection(
//                    formState,
//                    units,
//                    kitchenCategories,
//                    taxes,
//                    { newState -> formState = newState },
//                    focusManager
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun BasicInfoSection(
//    state: MenuItemFormState,
//    menus: List<Menu>,
//    menuCategories: List<MenuCategory>,
//    onStateChange: (MenuItemFormState) -> Unit,
//    focusManager: FocusManager
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            Text("Basic Information", style = MaterialTheme.typography.titleMedium)
//
//            OutlinedTextField(
//                value = state.name,
//                onValueChange = { onStateChange(state.copy(name = it.uppercase())) },
//                label = { Text("Name") },
//                modifier = Modifier.fillMaxWidth(),
//                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
//                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
//            )
//
//            OutlinedTextField(
//                value = state.nameTamil,
//                onValueChange = { onStateChange(state.copy(nameTamil = it)) },
//                label = { Text("Name (Tamil)") },
//                modifier = Modifier.fillMaxWidth(),
//                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
//                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
//            )
//
//            MenuDropdown(
//                menus = menus,
//                onMenuSelected = { onStateChange(state.copy(menuId = it.menu_id.toInt())) },
//                label = "Select Menu",
//                modifier = Modifier.fillMaxWidth(),
//                selectedMenu = menus.find { it.menu_id.toInt() == state.menuId }
//            )
//
//            MenuCategoryDropdown(
//                menus = menuCategories,
//                onMenuCategorySelected = { onStateChange(state.copy(menuItemCatId = it.item_cat_id.toInt())) },
//                label = "Select Menu Category",
//                modifier = Modifier.fillMaxWidth(),
//                selectedMenuCategory = menuCategories.find { it.item_cat_id.toInt() == state.menuItemCatId }
//            )
//        }
//    }
//}
//
//@Composable
//private fun RateSection(
//    state: MenuItemFormState,
//    onStateChange: (MenuItemFormState) -> Unit,
//    focusManager: FocusManager
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            Text("Pricing", style = MaterialTheme.typography.titleMedium)
//
//            OutlinedTextField(
//                value = state.rate,
//                onValueChange = { onStateChange(state.copy(rate = it)) },
//                label = { Text("Rate *") },
//                modifier = Modifier.fillMaxWidth(),
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
//                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
//            )
//
//            OutlinedTextField(
//                value = state.acRate,
//                onValueChange = { onStateChange(state.copy(acRate = it)) },
//                label = { Text("AC Rate") },
//                modifier = Modifier.fillMaxWidth(),
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
//                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
//            )
//
//            OutlinedTextField(
//                value = state.parcelRate,
//                onValueChange = { onStateChange(state.copy(parcelRate = it)) },
//                label = { Text("Parcel Rate") },
//                modifier = Modifier.fillMaxWidth(),
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
//                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
//            )
//
//            StringDropdown(
//                options = listOf("YES", "NO"),
//                selectedOption = state.rateLock,
//                onOptionSelected = { onStateChange(state.copy(rateLock = it)) },
//                label = "Rate Lock",
//                modifier = Modifier.fillMaxWidth()
//            )
//        }
//    }
//}
//
//@Composable
//private fun AvailabilitySection(
//    state: MenuItemFormState,
//    onStateChange: (MenuItemFormState) -> Unit
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            Text("Availability", style = MaterialTheme.typography.titleMedium)
//
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Switch(
//                    checked = state.isFavourite,
//                    onCheckedChange = { onStateChange(state.copy(isFavourite = it)) }
//                )
//                Spacer(modifier = Modifier.width(12.dp))
//                Text("Mark as Favourite")
//            }
//
//            StringDropdown(
//                options = listOf("YES", "NO"),
//                selectedOption = state.isAvailable,
//                onOptionSelected = { onStateChange(state.copy(isAvailable = it)) },
//                label = "Is Available",
//                modifier = Modifier.fillMaxWidth()
//            )
//        }
//    }
//}
//
//@Composable
//private fun InventorySection(
//    state: MenuItemFormState,
//    units: List<TblUnit>,
//    kitchenCategories: List<KitchenCategory>,
//    taxes: List<Tax>,
//    onStateChange: (MenuItemFormState) -> Unit,
//    focusManager: FocusManager
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Switch(
//                    checked = state.isInventory == 1L,
//                    onCheckedChange = {
//                        val newInventory = if (it) 1L else 0L
//                        onStateChange(state.copy(isInventory = newInventory))
//                    }
//                )
//                Spacer(modifier = Modifier.width(12.dp))
//                Text("Enable Inventory", style = MaterialTheme.typography.titleMedium)
//            }
//
//            if (state.isInventory == 1L) {
//                Divider(modifier = Modifier.padding(vertical = 8.dp))
//
//                KitchenGroupDropdown(
//                    menus = kitchenCategories,
//                    onKitchenCategorySelected = { onStateChange(state.copy(kitchenCatId = it.kitchen_cat_id.toInt())) },
//                    label = "Kitchen Category",
//                    modifier = Modifier.fillMaxWidth(),
//                    selectedKitchenCategory = kitchenCategories.find { it.kitchen_cat_id.toInt() == state.kitchenCatId }
//                )
//
//                TaxDropdown(
//                    taxes = taxes,
//                    onTaxSelected = { onStateChange(state.copy(taxId = it.tax_id.toInt())) },
//                    label = "Select Tax",
//                    modifier = Modifier.fillMaxWidth(),
//                    selectedTax = taxes.find { it.tax_id.toInt() == state.taxId }
//                )
//
//                OutlinedTextField(
//                    value = state.hsnCode,
//                    onValueChange = { onStateChange(state.copy(hsnCode = it)) },
//                    label = { Text("HSN Code") },
//                    modifier = Modifier.fillMaxWidth(),
//                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
//                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
//                )
//
//                OutlinedTextField(
//                    value = state.minStock.toString(),
//                    onValueChange = {
//                        val stock = it.toIntOrNull() ?: 0
//                        onStateChange(state.copy(minStock = stock))
//                    },
//                    label = { Text("Minimum Stock") },
//                    modifier = Modifier.fillMaxWidth(),
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
//                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
//                )
//
//                UnitDropdown(
//                    menus = units,
//                    onUnitSelected = { onStateChange(state.copy(unitId = it.unit_id.toInt())) },
//                    label = "Select Unit",
//                    modifier = Modifier.fillMaxWidth(),
//                    selectedUnit = units.find { it.unit_id.toInt() == state.unitId }
//                )
//
//                StringDropdown(
//                    options = listOf("YES", "NO"),
//                    selectedOption = state.isRaw,
//                    onOptionSelected = { onStateChange(state.copy(isRaw = it)) },
//                    label = "Is Raw",
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                StringDropdown(
//                    options = listOf("YES", "NO"),
//                    selectedOption = state.stockMaintain,
//                    onOptionSelected = { onStateChange(state.copy(stockMaintain = it)) },
//                    label = "Stock Maintain",
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//        }
//    }
//}
//
sealed class MenuItemSettingsUiState {
    object Loading : MenuItemSettingsUiState()
    data class Success(val menuItems: List<TblMenuItemResponse>) : MenuItemSettingsUiState()
    data class Error(val message: String) : MenuItemSettingsUiState()
}
