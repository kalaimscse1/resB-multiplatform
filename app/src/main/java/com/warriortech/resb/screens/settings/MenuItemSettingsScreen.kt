package com.warriortech.resb.screens.settings

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.*
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.ui.components.BarcodeInputField
import com.warriortech.resb.ui.components.CameraBarcodeScanner
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.master.MenuItemSettingsViewModel
import com.warriortech.resb.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemSettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: MenuItemSettingsViewModel = hiltViewModel(),
    sessionManager: SessionManager,
    drawerState: DrawerState,
    navController: NavHostController
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val order by viewModel.orderBy.collectAsStateWithLifecycle()
    val menus by viewModel.menus.collectAsStateWithLifecycle()
    val menuCategories by viewModel.menuCategories.collectAsStateWithLifecycle()
    val kitchenCategories by viewModel.kitchenCategories.collectAsStateWithLifecycle()
    val taxes by viewModel.taxes.collectAsStateWithLifecycle()
    val units by viewModel.units.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val menuItems by viewModel.filteredMenuItems.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingMenuItem by remember { mutableStateOf<TblMenuItemResponse?>(null) }
    var menuItemToDelete by remember { mutableStateOf<TblMenuItemResponse?>(null) }

    var searchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var success by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.getOrderBy()
        viewModel.loadMenuItems()
    }

    BackHandler {
        navController.navigate("dashboard") {
            popUpTo("dashboard") { inclusive = true }
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            if (errorMessage == "Menu item deleted successfully" || errorMessage == "Menu item updated successfully" || errorMessage == "Menu item added successfully") {
                success = true
            } else {
                failed = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Item", color = SurfaceLight) },
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
                            searchMode = false
                            searchQuery = ""
                            viewModel.searchMenuItems("")
                        } else {
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
                                onDelete = { menuItemToDelete = menuItem }
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
                units = units,
                order = order.toLong()
            )
        }

        if (menuItemToDelete != null) {
            AlertDialog(
                onDismissRequest = { menuItemToDelete = null },
                title = { Text("Confirm Delete") },
                text = { Text("Are you sure you want to delete '${menuItemToDelete?.menu_item_name}'?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            menuItemToDelete?.let {
                                scope.launch {
                                    viewModel.deleteMenuItem(it.menu_item_id.toInt())
                                }
                            }
                            menuItemToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { menuItemToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

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

@SuppressLint("AutoboxingStateCreation")
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
    order: Long
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Centralized Focus Requesters
    val focusRequesters = remember {
        mapOf(
            "name" to FocusRequester(),
            "nameTamil" to FocusRequester(),
            "rate" to FocusRequester(),
            "acRate" to FocusRequester(),
            "parcelRate" to FocusRequester(),
            "parcelCharge" to FocusRequester(),
            "preparationTime" to FocusRequester(),
            "minStock" to FocusRequester(),
            "orderBy" to FocusRequester(),
            "hsnCode" to FocusRequester(),
            "barcode" to FocusRequester()
        )
    }

    // Form States
    var name by remember { mutableStateOf(menuItem?.menu_item_name ?: "") }
    var nameTamil by remember { mutableStateOf(menuItem?.menu_item_name_tamil ?: "") }
    var menuId by remember { mutableLongStateOf(menuItem?.menu_id ?: 1L) }
    var menuItemCatId by remember { mutableLongStateOf(menuItem?.item_cat_id ?: 1L) }
    var kitchenCatId by remember { mutableLongStateOf(menuItem?.kitchen_cat_id ?: 1L) }
    var isAvailable by remember { mutableStateOf(menuItem?.is_available ?: "YES") }
    var taxId by remember { mutableLongStateOf(menuItem?.tax_id ?: 1L) }
    var rate by remember { mutableStateOf(menuItem?.rate?.toString() ?: "") }
    var rateLock by remember { mutableStateOf(menuItem?.rate_lock ?: "YES") }
    var orderBy by remember { mutableLongStateOf(menuItem?.order_by ?: order) }
    var acRate by remember { mutableStateOf(menuItem?.ac_rate?.toString() ?: "") }
    var parcelRate by remember { mutableStateOf(menuItem?.parcel_rate?.toString() ?: "") }
    var parcelCharge by remember { mutableStateOf(0.0) }
    var preparationTime by remember { mutableStateOf(menuItem?.preparation_time?.toString() ?: "") }
    var isFavourite by remember { mutableStateOf(menuItem?.is_favourite == true) }

    // Inventory fields
    var isInventory by remember { mutableLongStateOf(menuItem?.is_inventory ?: 0L) }
    var hsnCode by remember { mutableStateOf(menuItem?.hsn_code ?: "") }
    var minStock by remember { mutableStateOf(menuItem?.min_stock?.toString() ?: "") }
    var isRaw by remember { mutableStateOf(menuItem?.is_raw ?: "NO") }
    var stockMaintain by remember { mutableStateOf(menuItem?.stock_maintain ?: "NO") }
    var unitId by remember { mutableLongStateOf(menuItem?.unit_id ?: 1L) }
    var isActive by remember { mutableLongStateOf(menuItem?.is_active ?: 1L) }
    var barcode by remember { mutableStateOf(menuItem?.menu_item_code ?: "") }
    var showScanner by remember { mutableStateOf(false) }

    // Validation States
    var nameError by remember { mutableStateOf<String?>(null) }
    var rateError by remember { mutableStateOf<String?>(null) }
    var parcelRateError by remember { mutableStateOf<String?>(null) }

    fun validate(): String? {
        // Top to bottom validation logic
        if (name.isBlank()) {
            nameError = "Name is required"
            return "name"
        } else nameError = null

        if (rate.isBlank() || rate.toDoubleOrNull() == null) {
            rateError = "Valid Rate is required"
            return "rate"
        } else rateError = null

        if (parcelRate.isBlank() || parcelRate.toDoubleOrNull() == null) {
            parcelRateError = "Valid Parcel Rate is required"
            return "parcelRate"
        } else parcelRateError = null

        return null
    }

    ReusableBottomSheet(
        onDismiss = onDismiss,
        title = if (menuItem != null) "Edit Menu Item" else "Add Menu Item",
        onSave = {
            val errorField = validate()
            if (errorField == null) {
                val newMenuItem = TblMenuItemRequest(
                    menu_item_id = menuItem?.menu_item_id ?: 0,
                    menu_item_name = name,
                    menu_item_name_tamil = nameTamil,
                    item_cat_id = menuItemCatId,
                    rate = rate.toDoubleOrNull() ?: 0.0,
                    ac_rate = acRate.toDoubleOrNull() ?: 0.0,
                    parcel_rate = parcelRate.toDoubleOrNull() ?: 0.0,
                    parcel_charge = 0.0,
                    tax_id = taxId,
                    cess_specific = 0.0,
                    kitchen_cat_id = kitchenCatId,
                    stock_maintain = stockMaintain,
                    rate_lock = rateLock,
                    unit_id = unitId,
                    min_stock = minStock.toLongOrNull() ?: 0L,
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
                    preparation_time = preparationTime.toLongOrNull() ?: 0L
                )
                onSave(newMenuItem)
            } else {
                focusRequesters[errorField]?.requestFocus()
            }
        },
        isSaveEnabled = name.isNotBlank() && rate.isNotBlank() && menuItemCatId != 1L && menuId != 1L && taxId != 1L,
        buttonText = if (menuItem != null) "Update" else "Add"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 450.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            BarcodeInputField(
                value = barcode,
                onValueChange = { barcode = it },
                onBarcodeScanned = { barcode = it },
                onCameraClick = { showScanner = true }
            )

            FormTextField(
                value = name,
                onValueChange = { name = it.uppercase() },
                label = "Name *",
                focusRequester = focusRequesters["name"]!!,
                nextFocusRequester = focusRequesters["nameTamil"],
                isError = nameError != null,
                errorMessage = nameError,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                scrollState = scrollState,
                scope = scope
            )

            FormTextField(
                value = nameTamil,
                onValueChange = { nameTamil = it },
                label = "Name (Tamil)",
                focusRequester = focusRequesters["nameTamil"]!!,
                nextFocusRequester = focusRequesters["rate"],
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                scrollState = scrollState,
                scope = scope
            )

            FormTextField(
                value = rate,
                onValueChange = { rate = it },
                label = "Rate *",
                focusRequester = focusRequesters["rate"]!!,
                nextFocusRequester = focusRequesters["acRate"],
                isError = rateError != null,
                errorMessage = rateError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                scrollState = scrollState,
                scope = scope
            )

            FormTextField(
                value = acRate,
                onValueChange = { acRate = it },
                label = "AC Rate",
                focusRequester = focusRequesters["acRate"]!!,
                nextFocusRequester = focusRequesters["parcelRate"],
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                scrollState = scrollState,
                scope = scope
            )

            FormTextField(
                value = parcelRate,
                onValueChange = { parcelRate = it },
                label = "Parcel Rate *",
                focusRequester = focusRequesters["parcelRate"]!!,
                nextFocusRequester = focusRequesters["preparationTime"],
                isError = parcelRateError != null,
                errorMessage = parcelRateError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                scrollState = scrollState,
                scope = scope
            )

//            FormTextField(
//                value = parcelCharge,
//                onValueChange = { parcelCharge = it },
//                label = "Parcel Charge",
//                focusRequester = focusRequesters["parcelCharge"]!!,
//                nextFocusRequester = focusRequesters["preparationTime"],
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
//                scrollState = scrollState,
//                scope = scope
//            )

            FormTextField(
                value = preparationTime,
                onValueChange = { preparationTime = it },
                label = "Preparation Time (min)",
                focusRequester = focusRequesters["preparationTime"]!!,
                nextFocusRequester = if (isInventory == 1L) focusRequesters["hsnCode"] else focusRequesters["orderBy"],
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                scrollState = scrollState,
                scope = scope
            )

            if (isInventory == 1L) {
                FormTextField(
                    value = hsnCode,
                    onValueChange = { hsnCode = it },
                    label = "HSN Code",
                    focusRequester = focusRequesters["hsnCode"]!!,
                    nextFocusRequester = focusRequesters["minStock"],
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    scrollState = scrollState,
                    scope = scope
                )

                FormTextField(
                    value = minStock,
                    onValueChange = { minStock = it },
                    label = "Min Stock",
                    focusRequester = focusRequesters["minStock"]!!,
                    nextFocusRequester = focusRequesters["orderBy"],
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    scrollState = scrollState,
                    scope = scope
                )
            }


            FormTextField(
                value = orderBy.toString(),
                onValueChange = { orderBy = it.toLongOrNull() ?: 0L },
                label = "Order By",
                focusRequester = focusRequesters["orderBy"]!!,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                onImeAction = { focusManager.clearFocus() },
                scrollState = scrollState,
                scope = scope
            )

            StringDropdown(
                options = listOf("YES", "NO"),
                selectedOption = rateLock,
                onOptionSelected = { rateLock = it },
                label = "Rate Lock",
                modifier = Modifier.fillMaxWidth()
            )

            TaxDropdown(
                taxes = taxes,
                onTaxSelected = { taxId = it.tax_id },
                label = "Select Tax *",
                modifier = Modifier.fillMaxWidth(),
                selectedTax = taxes.find { it.tax_id == taxId }
            )

            MenuDropdown(
                menus = menus,
                onMenuSelected = { menuId = it.menu_id },
                label = "Select Menu *",
                modifier = Modifier.fillMaxWidth(),
                selectedMenu = menus.find { it.menu_id == menuId }
            )

            MenuCategoryDropdown(
                menus = menuCategories,
                onMenuCategorySelected = { menuItemCatId = it.item_cat_id },
                label = "Select Menu Category *",
                modifier = Modifier.fillMaxWidth(),
                selectedMenuCategory = menuCategories.find { it.item_cat_id == menuItemCatId }
            )

            KitchenGroupDropdown(
                menus = kitchenCategories,
                onKitchenCategorySelected = { kitchenCatId = it.kitchen_cat_id },
                label = "Select Kitchen Category ",
                modifier = Modifier.fillMaxWidth(),
                selectedKitchenCategory = kitchenCategories.find { it.kitchen_cat_id == kitchenCatId }
            )

            StringDropdown(
                options = listOf("YES", "NO"),
                selectedOption = isAvailable,
                onOptionSelected = { isAvailable = it },
                label = "Is Available",
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Switch(checked = isFavourite, onCheckedChange = { isFavourite = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text("Is Favourite")
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Switch(checked = isInventory == 1L, onCheckedChange = { isInventory = if (it) 1L else 0L })
                Spacer(modifier = Modifier.width(8.dp))
                Text("Inventory Enabled")
            }

            if (isInventory == 1L) {
                UnitDropdown(
                    menus = units,
                    onUnitSelected = { unitId = it.unit_id },
                    label = "Select Unit",
                    modifier = Modifier.fillMaxWidth(),
                    selectedUnit = units.find { it.unit_id == unitId }
                )

                StringDropdown(
                    options = listOf("YES", "NO"),
                    selectedOption = isRaw,
                    onOptionSelected = { isRaw = it },
                    label = "Is Raw",
                    modifier = Modifier.fillMaxWidth()
                )

                StringDropdown(
                    options = listOf("YES", "NO"),
                    selectedOption = stockMaintain,
                    onOptionSelected = { stockMaintain = it },
                    label = "Stock Maintain",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showScanner) {
        Dialog(onDismissRequest = { showScanner = false }) {
            Box(Modifier.fillMaxSize()) {
                CameraBarcodeScanner(
                    onResult = { barcode = it },
                    onClose = { showScanner = false }
                )
            }
        }
    }
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    nextFocusRequester: FocusRequester? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    errorMessage: String? = null,
    onImeAction: (() -> Unit)? = null,
    scrollState: ScrollState? = null,
    scope: CoroutineScope? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    var yPosition by remember { mutableFloatStateOf(0f) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onGloballyPositioned { coordinates ->
                yPosition = coordinates.positionInParent().y
            }
            .onFocusChanged { focusState ->
                if (focusState.isFocused && scrollState != null && scope != null) {
                    scope.launch {
                        // Scrolling to the field with a small offset
                        val target = (yPosition - 40f).coerceAtLeast(0f)
                        scrollState.animateScrollTo(target.roundToInt())
                    }
                }
            },
        singleLine = true,
        isError = isError,
        supportingText = errorMessage?.let { 
            { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) } 
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(
            onNext = { nextFocusRequester?.requestFocus() },
            onDone = { onImeAction?.invoke() },
            onSearch = { onImeAction?.invoke() }
        ),
        interactionSource = interactionSource
    )
}

sealed class MenuItemSettingsUiState {
    object Loading : MenuItemSettingsUiState()
    data class Success(val menuItems: List<TblMenuItemResponse>) : MenuItemSettingsUiState()
    data class Error(val message: String) : MenuItemSettingsUiState()
}
