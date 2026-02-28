package com.warriortech.resb.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.warriortech.resb.ui.viewmodel.MenuViewModel
import com.warriortech.resb.ui.viewmodel.CartItemKey
import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.IconButton
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ScrollableTabRow
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Tab
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.TblOrderDetailsResponse
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.ui.theme.TextPrimary
import kotlinx.coroutines.launch
import com.warriortech.resb.ui.components.MobileOptimizedButton
import com.warriortech.resb.ui.components.ModernDivider
import com.warriortech.resb.ui.components.BarcodeScannerButton
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SecondaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.model.Modifiers
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.util.AnimatedSnackbarDemo
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.SuccessDialog
import kotlinx.coroutines.delay

private val DeepBlue = PrimaryGreen

@androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@SuppressLint(
    "StateFlowValueCalledInComposition", "DefaultLocale", "SuspiciousIndentation",
    "ResourceAsColor"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    isTakeaway: String,
    tableStatId: Boolean,
    tableId: Long,
    tableName: String,
    onBackPressed: () -> Unit,
    onOrderPlaced: () -> Unit,
    onBillPlaced: (orderDetailsResponse: List<TblOrderDetailsResponse>, orderId: String) -> Unit,
    viewModel: MenuViewModel = hiltViewModel(),
    drawerState: DrawerState,
    navController: NavHostController,
    sessionManager: SessionManager
) {
    val menuState by viewModel.menuState.collectAsStateWithLifecycle()
    val orderState by viewModel.orderState.collectAsStateWithLifecycle()
    val selectedItems by viewModel.selectedItems.collectAsStateWithLifecycle()
    val newselectedItems by viewModel.newselectedItems.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val tableStatusFromVM by viewModel.tableStatus.collectAsStateWithLifecycle()
    val isExistingOrderLoaded by viewModel.isExistingOrderLoaded.collectAsStateWithLifecycle()
    val orderDetailsResponse by viewModel.orderDetailsResponse.collectAsStateWithLifecycle()
    val showAlertMessage by viewModel.showAlert.collectAsStateWithLifecycle()
    val activeCartItem by viewModel.activeCartItem.collectAsStateWithLifecycle()

    // Modifier-related state
    val showModifierDialog by viewModel.showModifierDialog.collectAsStateWithLifecycle()
    val selectedMenuItemForModifier by viewModel.selectedMenuItemForModifier.collectAsStateWithLifecycle()
    val modifierGroups by viewModel.modifierGroups.collectAsStateWithLifecycle()
    val selectedModifiers: Map<Long, List<Modifiers>> by viewModel.selectedModifiers.collectAsStateWithLifecycle(initialValue = emptyMap())

    val selectedWithoutModifiers by viewModel.selectedWithoutModifiers.collectAsStateWithLifecycle()
    val selectedWithModifiers by viewModel.selectedWithModifiers.collectAsStateWithLifecycle()
    val newSelectedWithoutModifiers by viewModel.newSelectedWithoutModifiers.collectAsStateWithLifecycle()
    val newSelectedWithModifiers by viewModel.newSelectedWithModifiers.collectAsStateWithLifecycle()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showOrderDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var values by remember { mutableStateOf<PaddingValues>(PaddingValues(0.dp)) }
    var sucess by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var alert by remember { mutableStateOf(false) }
    
    var showQtyInputDialog by remember { mutableStateOf(false) }

    val effectiveStatus = remember(isTakeaway, tableStatusFromVM) {
        when (isTakeaway) {
            "TABLE" -> tableStatusFromVM
            "TAKEAWAY" -> "TAKEAWAY"
            "DELIVERY" -> "DELIVERY"
            else -> tableStatusFromVM
        }
    }

    // Handle table unlocking when leaving the screen
    DisposableEffect(tableId) {
        onDispose {
            if (isTakeaway == "TABLE") {
                viewModel.updateTableOpenStatus(tableId, false)
            }
        }
    }

    BackHandler {
        onBackPressed()
    }

    LaunchedEffect(key1 = isTakeaway, key2 = tableId, key3 = tableStatId) {
        viewModel.setTableId(tableId)
        val isTableOrderScenario = isTakeaway == "TABLE" && tableStatId
        viewModel.initializeScreen(isTableOrder = isTableOrderScenario, currentTableId = tableId)
    }

    LaunchedEffect(orderState) {
        when (val currentOrderState = orderState) {
            is MenuViewModel.OrderUiState.Success -> {
                scope.launch {
                    sucess = true
                    delay(2000)
                    onOrderPlaced()
                }
            }
            is MenuViewModel.OrderUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(currentOrderState.message)
                }
            }
            else -> {}
        }
    }

    if (showConfirmDialog) {
        OrderConfirmationDialog(
            selectedItems = if (isExistingOrderLoaded && newselectedItems.isNotEmpty()) newselectedItems else selectedItems,
            totalAmount = if (isExistingOrderLoaded && newselectedItems.isNotEmpty()) viewModel.getOrderNewTotal(
                effectiveStatus.toString()
            ) else viewModel.getOrderTotal(effectiveStatus.toString()),
            onConfirm = {
                viewModel.placeOrder(tableId, effectiveStatus, 5)
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false },
            tableStatus = effectiveStatus.toString()
        )
    }
    if (showOrderDialog) {
        OrderDetailsDialog(
            selectedItems = selectedItems,
            totalAmount = viewModel.getOrderTotal(effectiveStatus.toString()),
            onConfirm = { showOrderDialog = false },
            tableStatus = effectiveStatus.toString(),
            items = orderDetailsResponse
        )
    }

    if (showSearchDialog) {
        val allItems = when (val state = menuState) {
            is MenuViewModel.MenuUiState.Success -> state.menuItems
            else -> emptyList()
        }
        ItemSearchDialog(
            allItems = allItems,
            onItemSelected = { item ->
                viewModel.addItemToOrder(item)
            },
            onDismiss = { showSearchDialog = false }
        )
    }

    if (showQtyInputDialog && activeCartItem != null) {
        QuantityInputDialog(
            onConfirm = { qty ->
                viewModel.updateItemQuantity(activeCartItem!!, qty)
                showQtyInputDialog = false
            },
            onDismiss = { showQtyInputDialog = false }
        )
    }

    showAlertMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissAlert() },
            title = { Text("Modifier Required") },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = { viewModel.dismissAlert() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            tableName ,
                            style = MaterialTheme.typography.titleLarge,
                            color = SurfaceLight
                        )
                        if (isExistingOrderLoaded) {
                            Text(
                                text = "Editing Order #${viewModel.existingOrderId.value ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SurfaceLight
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch { drawerState.open() }
                    }) {
                        Icon(
                            Icons.Default.Menu, contentDescription = "Menu",
                            tint = SurfaceLight
                        )
                    }
                },
                actions = {
                    if ((effectiveStatus == "TAKEAWAY" || tableStatusFromVM == "DELIVERY") && (sessionManager.getUser()?.role == "ADMIN" || sessionManager.getUser()?.role == "CASHIER")){
                        IconButton(onClick = {
                            navController.navigate("takeaway_orders") {
                                launchSingleTop = true
                            }
                        }, modifier = Modifier.padding(start = 5.dp)) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = "Payment",
                                tint = SurfaceLight
                            )
                        }
                    }
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Item",
                            tint = SurfaceLight
                        )
                    }
                    BarcodeScannerButton(
                        onBarcodeScanned = { barcode ->
                            viewModel.findAndAddItemByBarcode(barcode)
                        },
                        onError = { error ->
                            scope.launch {
                                snackbarHostState.showSnackbar(error)
                            }
                        }
                    )
                    if (tableStatusFromVM != "TAKEAWAY" && tableStatusFromVM != "DELIVERY") {
                        IconButton(onClick = {
                            navController.navigate("selects") {
                                launchSingleTop = true
                            }
                        }, modifier = Modifier.padding(start = 5.dp)) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = "Dine In",
                                tint = SurfaceLight
                            )
                        }

                        IconButton(onClick = {
                            viewModel.clearOrder()
                        }) {
                            Icon(
                                imageVector = Icons.Default.RemoveShoppingCart,
                                contentDescription = "Clear Cart",
                                tint = SurfaceLight
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            viewModel.clearOrder()
                        }) {
                            Icon(
                                imageVector = Icons.Default.RemoveShoppingCart,
                                contentDescription = "Clear Cart",
                                tint = SurfaceLight
                            )
                        }
                    }

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = SecondaryGreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(SecondaryGreen, SecondaryGreen)
                        ),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(12.dp)
            ) {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val newItemCount =
                        if (isExistingOrderLoaded) newselectedItems.values.sum() else selectedItems.values.sum()
                    val existingItemCount =
                        if (isExistingOrderLoaded) orderDetailsResponse.sumOf { it.qty } else 0
                    val totalItemCount = newItemCount + existingItemCount
                    val totalAmount = viewModel.getOrderTotal(effectiveStatus.toString())
                    val newTotalAmount =
                        viewModel.getOrderNewTotal(effectiveStatus.toString())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            if (isExistingOrderLoaded) {
                                Text(
                                    text = "Existing: $existingItemCount | New: $newItemCount",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.clickable { showOrderDialog = true }
                                )
                            }
                            Text(
                                text = "Total Items: $totalItemCount",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        Column {
                            if (isExistingOrderLoaded) {
                                Text(
                                    text = "Existing Total: ${
                                        CurrencySettings.format(
                                            orderDetailsResponse.sumOf { it.actual_rate * it.qty }
                                        )
                                    }",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = if (isExistingOrderLoaded) "New Total: ${
                                    CurrencySettings.format(
                                        newTotalAmount
                                    )
                                }" else
                                    "Total: ${CurrencySettings.format(totalAmount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isExistingOrderLoaded) {
                            if (sessionManager.getUser()?.role == "ADMIN" || sessionManager.getUser()?.role == "CASHIER") {
                                MobileOptimizedButton(
                                    onClick = {
                                        if (newselectedItems.isNotEmpty()){
                                            alert= true
                                        }
                                        else{
                                            navController.navigate("billing_screen/${viewModel.existingOrderId.value ?: ""}") {
                                                launchSingleTop = true
                                            }
                                            onBillPlaced(
                                                viewModel.orderDetailsResponse.value,
                                                viewModel.existingOrderId.value ?: ""
                                            )
                                        }
                                    },
                                    text = "Bill",
                                    enabled = selectedItems.isNotEmpty(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                )
                            }
                            MobileOptimizedButton(
                                onClick = { showConfirmDialog = true },
                                enabled = (if (viewModel.isExistingOrderLoaded.value) newselectedItems.isNotEmpty() else selectedItems.isNotEmpty()) && orderState !is MenuViewModel.OrderUiState.Loading,
                                text = if (isTakeaway == "TABLE" && viewModel.isExistingOrderLoaded.value) "Update KOT" else "Place Order",
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            MobileOptimizedButton(
                                onClick = { showConfirmDialog = true },
                                text = "Place Order",
                                enabled = (selectedItems.isNotEmpty()),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            )
                        }
                    }
                }

            }

        },
        snackbarHost = {
            AnimatedSnackbarDemo(snackbarHostState)
        },
    ) { paddingValues ->
        values = paddingValues
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Content Area
            Column(modifier = Modifier.weight(1f)) {
                if (categories.isNotEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOf(selectedCategory)
                            .coerceAtLeast(0),
                        backgroundColor = Color.White,
                        contentColor = TextPrimary
                    ) {
                        categories.forEach { category ->
                            Tab(
                                selected = selectedCategory == category,
                                onClick = { viewModel.selectedCategory.value = category },
                                text = { Text(category.uppercase()) }
                            )
                        }
                    }
                }

                when (val currentMenuState = menuState) {
                    is MenuViewModel.MenuUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is MenuViewModel.MenuUiState.Success -> {
                        val menuItems = currentMenuState.menuItems
                        val filteredMenuItems = menuItems.filter {
                            if (selectedCategory == "FAVOURITES") it.is_favourite == true
                            else if (selectedCategory == "ALL" || selectedCategory == null) true
                            else it.item_cat_name == selectedCategory
                        }

                        val listState = rememberLazyListState()
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            itemsIndexed(
                                filteredMenuItems,
                                key = { _, item -> item.menu_item_id }
                            ) { _, menuItem ->
                                MenuItemCard(
                                    menuItem = menuItem,
                                    qtyWithoutModifiers = if (isExistingOrderLoaded) newSelectedWithoutModifiers[menuItem.menu_item_id] ?: 0 else selectedWithoutModifiers[menuItem.menu_item_id] ?: 0,
                                    qtyWithModifiers = if (isExistingOrderLoaded) newSelectedWithModifiers[menuItem.menu_item_id] ?: 0 else selectedWithModifiers[menuItem.menu_item_id] ?: 0,
                                    existingQuantity = if (isExistingOrderLoaded) selectedItems[menuItem] ?: 0 else 0,
                                    modifiers = selectedModifiers[menuItem.menu_item_id] ?: emptyList(),
                                    onAddItem = {
                                        viewModel.addItemToOrder(menuItem)
                                    },
                                    onRemoveItem = {
                                        viewModel.removeItemFromOrder(menuItem)
                                    },
                                    tableStatus = effectiveStatus.toString(),
                                    isExistingOrder = isExistingOrderLoaded,
                                    onModifierClick = { viewModel.showModifierDialog(menuItem) },
                                    isSelected = activeCartItem?.menuItem == menuItem,
                                    onSelect = { 
                                        viewModel.setActiveItem(menuItem)
                                    },
                                    backgroundColor = Color.White,
                                    contentColor = if (activeCartItem?.menuItem == menuItem) DeepBlue.copy(alpha = 0.1f) else Color.White,
                                    textColor = DeepBlue
                                )
                            }
                        }
                    }

                    is MenuViewModel.MenuUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Failed to load menu items")
                        }
                    }
                }
            }

            // Right-side Numeric Column
            Column(
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight()
                    .border(1.dp, Color.LightGray),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "").forEach { key ->
                    KeypadButton(text = key) {
                        activeCartItem?.let { cartKey ->
                            when (key) {
                                "." -> showQtyInputDialog = true
                                "0" -> {
                                    viewModel.updateItemQuantity(cartKey, 0)
                                }
                                in "1".."9" -> {
                                    viewModel.updateItemQuantity(cartKey, key.toInt())
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }

        if (orderState is MenuViewModel.OrderUiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Processing order...")
                }
            }
        }
    }
    if (sucess) {
        SuccessDialog(
            title = "Order Success",
            description = "Order placed successfully",
            paddingValues = values
        )
    }
    if (failed) {
        SuccessDialog(
            title = "Order Failed",
            description = "Failed to place order",
            paddingValues = values
        )
    }
    // Modifier Selection Dialog
    if (showModifierDialog && selectedMenuItemForModifier != null) {
        ModifierSelectionDialog(
            menuItem = selectedMenuItemForModifier!!,
            availableModifiers = modifierGroups,
            selectedModifiers = emptyList(),
            onModifiersSelected = { selectedModifiers ->
                viewModel.addMenuItemWithModifiers(selectedMenuItemForModifier!!, selectedModifiers)
            },
            onDismiss = { viewModel.hideModifierDialog() }
        )
    }
    if (alert) {
        BillAlertDialog(
            onConfirm = {
                alert = false
            }
        )
    }
}

@Composable
fun KeypadButton(text: String, onClick: () -> Unit) {
    if (text.isEmpty()) {
        Spacer(modifier = Modifier.size(48.dp))
    } else {
        Box(
            modifier = Modifier
                .size(48.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = DeepBlue,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun QuantityInputDialog(
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var qtyText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Quantity") },
        text = {
            OutlinedTextField(
                value = qtyText,
                onValueChange = { if (it.all { char -> char.isDigit() }) qtyText = it },
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { 
                qtyText.toIntOrNull()?.let { onConfirm(it) }
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@SuppressLint("DefaultLocale")
@Composable
fun MenuItemCard(
    menuItem: TblMenuItemResponse,
    qtyWithoutModifiers: Int,
    qtyWithModifiers: Int,
    existingQuantity: Int = 0,
    modifiers: List<Modifiers> = emptyList(),
    onAddItem: () -> Unit,
    onRemoveItem: () -> Unit,
    onModifierClick: () -> Unit,
    tableStatus: String,
    isExistingOrder: Boolean = false,
    isSelected: Boolean = false,
    onSelect: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(1.dp, if (isSelected) BluePrimary else DeepBlue, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = contentColor
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = menuItem.menu_item_name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = DeepBlue,
                    )
                    Text(
                        text = when (tableStatus) {
                            "AC" -> CurrencySettings.format(menuItem.ac_rate)
                            "TAKEAWAY", "DELIVERY" -> CurrencySettings.format(menuItem.parcel_rate)
                            else -> CurrencySettings.format(menuItem.rate)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepBlue
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (qtyWithoutModifiers > 0) {
                        Text(
                            text = "x $qtyWithoutModifiers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepBlue,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                    if (qtyWithModifiers > 0) {
                        Text(
                            text = "x $qtyWithModifiers (M)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                    
                    // ---- MODIFIER BUTTON ----
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .border(1.dp, BluePrimary, RoundedCornerShape(4.dp))
                            .clickable { onModifierClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "A",
                            color = BluePrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            AnimatedVisibility(visible = (qtyWithoutModifiers + qtyWithModifiers) > 0 || existingQuantity > 0 || modifiers.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ModernDivider()
                    Spacer(modifier = Modifier.height(4.dp))

                    if (modifiers.isNotEmpty()) {
                        Text(
                            text = modifiers.joinToString(", ") { it.add_on_name },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = DeepBlue,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (isExistingOrder && existingQuantity > 0) {
                        Text(
                            text = "Existing: $existingQuantity",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemSearchDialog(
    allItems: List<TblMenuItemResponse>,
    onItemSelected: (TblMenuItemResponse) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = allItems.filter { it.menu_item_name.contains(searchQuery, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Search Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Enter item name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    itemsIndexed(filteredItems) { _, item ->
                        ListItem(
                            headlineContent = { Text(item.menu_item_name) },
                            modifier = Modifier.clickable { 
                                onItemSelected(item)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun OrderConfirmationDialog(
    selectedItems: Map<TblMenuItemResponse, Int>,
    totalAmount: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    tableStatus: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            MobileOptimizedButton(
                onClick = onConfirm,
                text = "Confirm",
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            MobileOptimizedButton(
                onClick = onDismiss,
                text = "Cancel",
                modifier = Modifier.fillMaxWidth()
            )
        },
        title = {
            Text("Confirm Order", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column {
                Text(
                    "Review your order before placing it:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                ModernDivider(modifier = Modifier.padding(vertical = 8.dp))
                Column(
                    modifier = Modifier
                        .heightIn(max = 250.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    selectedItems.forEach { (item, qty) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${item.menu_item_name} × $qty",
                                style = MaterialTheme.typography.bodySmall
                            )

                            val rate = when (tableStatus.uppercase()) {
                                "AC" -> item.ac_rate
                                "TAKEAWAY", "DELIVERY" -> item.parcel_rate
                                else -> item.rate
                            }

                            Text(
                                CurrencySettings.format((rate * qty)),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                ModernDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        CurrencySettings.format(totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}


@Composable
fun OrderDetailsDialog(
    selectedItems: Map<TblMenuItemResponse, Int>,
    totalAmount: Double,
    onConfirm: () -> Unit,
    tableStatus: String,
    items : List<TblOrderDetailsResponse>
) {
    val amount = items.sumOf { it.actual_rate * it.qty }
    AlertDialog(
        onDismissRequest = onConfirm,
        confirmButton = {
            MobileOptimizedButton(
                onClick = onConfirm,
                text = "OK",
                modifier = Modifier.fillMaxWidth()
            )
        },
        title = {
            Text("Order Details", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column {
                Text(
                    "Review your existing order :",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .heightIn(max = 250.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    items.groupBy { it.kot_number }.forEach {
                        Text(
                            "KOT No: ${it.key}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        ModernDivider()
                        it.value.forEach { orderDetail ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${orderDetail.menuItem.menu_item_name} × ${orderDetail.qty}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    CurrencySettings.format((orderDetail.actual_rate * orderDetail.qty)),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                ModernDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        CurrencySettings.format(amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

            }
        }
    )
}

@Composable
fun BillAlertDialog(
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onConfirm,
        confirmButton = {
            MobileOptimizedButton(
                onClick = onConfirm,
                text = "OK",
                modifier = Modifier.fillMaxWidth()
            )
        },
        title = {
            Text("Alert", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column {
                Text(
                    "Clear Selection to proceed to Billing / Click UpdateKot to proceed with existing selection to add items to Order.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
}
