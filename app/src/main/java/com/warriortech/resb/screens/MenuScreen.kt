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
import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
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
import com.warriortech.resb.ui.theme.Black
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.DarkGreen
import com.warriortech.resb.ui.theme.ErrorRed
import com.warriortech.resb.ui.theme.LightGreen
import com.warriortech.resb.ui.theme.ghostWhite
import com.warriortech.resb.util.AnimatedSnackbarDemo
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.SuccessDialog
import com.warriortech.resb.util.getDeviceInfo
import kotlinx.coroutines.delay
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
    val tableStatusFromVM by viewModel.tableStatus.collectAsStateWithLifecycle() // Assuming tableStatus is part of the table info
    val isExistingOrderLoaded by viewModel.isExistingOrderLoaded.collectAsStateWithLifecycle()
    val orderDetailsResponse by viewModel.orderDetailsResponse.collectAsStateWithLifecycle()

    // Modifier-related state
    val showModifierDialog by viewModel.showModifierDialog.collectAsStateWithLifecycle()
    val selectedMenuItemForModifier by viewModel.selectedMenuItemForModifier.collectAsStateWithLifecycle()
    val modifierGroups by viewModel.modifierGroups.collectAsStateWithLifecycle()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showOrderDialog by remember { mutableStateOf(false) }
    var values by remember { mutableStateOf<PaddingValues>(PaddingValues(0.dp)) }
    var sucess by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var alert by remember { mutableStateOf(false) }
    var barcodeError by remember { mutableStateOf<String?>(null) }

    val effectiveStatus = remember(isTakeaway, tableStatusFromVM) {
        when (isTakeaway) {
            "TABLE" -> tableStatusFromVM
            "TAKEAWAY" -> "TAKEAWAY"
            "DELIVERY" -> "DELIVERY"
            else -> tableStatusFromVM
        }
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
                    if (sessionManager.getGeneralSetting()?.is_kot!!) {
                        sucess = true
                        delay(2000)
                        onOrderPlaced()
                    } else {
                        sucess = true
                        delay(2000)
                        onOrderPlaced()
                    }
                }
            }

            is MenuViewModel.OrderUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(currentOrderState.message)
                }
            }

            else -> {
                snackbarHostState.showSnackbar("Loading")
            }
        }
    }

    if (showConfirmDialog) {
        OrderConfirmationDialog(
            selectedItems = if (viewModel.isExistingOrderLoaded.value && newselectedItems.isNotEmpty()) newselectedItems else selectedItems,
            totalAmount = if (viewModel.isExistingOrderLoaded.value && newselectedItems.isNotEmpty()) viewModel.getOrderNewTotal(
                effectiveStatus.toString()
            ) else viewModel.getOrderTotal(effectiveStatus.toString()),
            onConfirm = {
                viewModel.placeOrder(tableId, effectiveStatus)
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
            items= orderDetailsResponse
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (effectiveStatus != "TAKEAWAY" && effectiveStatus != "DELIVERY") "Menu -${tableName}" else "Menu ",
                            style = MaterialTheme.typography.titleLarge,
                            color = SurfaceLight
                        )
                        if (viewModel.isExistingOrderLoaded.value) {
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
                    BarcodeScannerButton(
                        onBarcodeScanned = { barcode ->
                            viewModel.findAndAddItemByBarcode(barcode)
                        },
                        onError = { error ->
                            barcodeError = error
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
                        if (viewModel.isExistingOrderLoaded.value) newselectedItems.values.sum() else selectedItems.values.sum()
                    val existingItemCount =
                        if (viewModel.isExistingOrderLoaded.value) orderDetailsResponse.sumOf { it.qty } else 0
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
                            if (viewModel.isExistingOrderLoaded.value) {
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
                            if (viewModel.isExistingOrderLoaded.value) {
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
                                text = if (viewModel.isExistingOrderLoaded.value) "New Total: ${
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
                        if (viewModel.isExistingOrderLoaded.value) {
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
//                            MobileOptimizedButton(
//                                onClick = { showConfirmDialog = true },
//                                text = "Place Order & Bill",
//                                enabled = (selectedItems.isNotEmpty()),
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .height(48.dp)
//                            )
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
        when (val currentMenuState = menuState) {
            is MenuViewModel.MenuUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is MenuViewModel.MenuUiState.Success -> {
                val menuItems = currentMenuState.menuItems
                val filteredMenuItems =
                    if (selectedCategory != null && selectedCategory == "FAVOURITES") {
                        menuItems.filter { it.is_favourite == true }
                    } else if (selectedCategory != null && selectedCategory == "ALL") {
                        menuItems
                    } else if (selectedCategory != null) {
                        menuItems.filter { it.item_cat_name == selectedCategory }
                    } else {
                        menuItems
                    }

                if (menuItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No menu items available")
                    }
                } else {
                    val menuShow = sessionManager.getGeneralSetting()?.menu_show_in_time == true
                    val listState = rememberLazyListState()
                    val scope = rememberCoroutineScope()
                    val bottomBarHeight = 80.dp

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(color = Color.White)
                    ) {
                        if (categories.isNotEmpty()) {
                            ScrollableTabRow(
                                selectedTabIndex = categories.indexOf(selectedCategory)
                                    .coerceAtLeast(0),
                                backgroundColor = Color.White,
                                contentColor = TextPrimary
                            ) {
                                categories.forEachIndexed { index, category ->
                                    Tab(
                                        selected = selectedCategory == category,
                                        onClick = { viewModel.selectedCategory.value = category },
                                        text = { Text(category) }
                                    )
                                }
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 2.dp)
                                .background(color = Color.White),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            item { Spacer(modifier = Modifier.height(5.dp)) }

                            itemsIndexed(
                                filteredMenuItems,
                                key = { _, item -> item.menu_item_id }
                            ) { index, menuItem ->
                                MenuItemCard(
                                    menuItem = menuItem,
                                    quantity = if (viewModel.isExistingOrderLoaded.value) newselectedItems[menuItem]
                                        ?: 0
                                    else selectedItems[menuItem] ?: 0,
                                    existingQuantity = if (viewModel.isExistingOrderLoaded.value) selectedItems[menuItem]
                                        ?: 0 else 0,
                                    onAddItem = {
                                        viewModel.addItemToOrder(menuItem)
                                        scope.launch {
                                            listState.animateScrollToItem(index)
                                        }
                                    },
                                    onRemoveItem = {
                                        viewModel.removeItemFromOrder(menuItem)
                                        scope.launch {
                                            listState.animateScrollToItem(index)
                                        }
                                    },
                                    tableStatus = effectiveStatus.toString(),
                                    isExistingOrder = viewModel.isExistingOrderLoaded.value,
                                    onModifierClick = { viewModel.showModifierDialog(menuItem) },
                                    backgroundColor = if ((selectedItems[menuItem]
                                            ?: 0) > 0 || (newselectedItems[menuItem] ?: 0) > 0
                                    )
                                        LightGreen else SecondaryGreen,
                                    contentColor = if ((selectedItems[menuItem]
                                            ?: 0) > 0 || (newselectedItems[menuItem] ?: 0) > 0
                                    )
                                        LightGreen else ghostWhite,
                                    textColor = Black
                                )
                            }

                            item { Spacer(modifier = Modifier.height(bottomBarHeight)) }
                        }
                    }
                }
            }


            is MenuViewModel.MenuUiState.Error -> {
                val errorMessage = (menuState as MenuViewModel.MenuUiState.Error).message

                LaunchedEffect(errorMessage) {
                    scope.launch {
                        snackbarHostState.showSnackbar(errorMessage)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Failed to load menu items")
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("DefaultLocale")
@Composable
fun MenuItemCard(
    menuItem: TblMenuItemResponse,
    quantity: Int,
    existingQuantity: Int = 0,
    onAddItem: () -> Unit,
    onRemoveItem: () -> Unit,
    onModifierClick: () -> Unit,
    tableStatus: String,
    isExistingOrder: Boolean = false,
    backgroundColor: Color,
    contentColor: Color,
    textColor: Color
) {
    val deviceInfo = getDeviceInfo()
    val cornerRadius = if (deviceInfo.isTablet) 24.dp else 20.dp
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, backgroundColor, RoundedCornerShape(cornerRadius)),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = contentColor
        ),

        ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(2f)
                ) {
                    Row {
                        Text(
                            text = menuItem.menu_item_name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = textColor,
                        )

//                        if (menuItem.menu_item_name_tamil.isNotBlank()) {
//                            Spacer(modifier = Modifier.height(4.dp))
//                            Text(
//                                text = menuItem.menu_item_name_tamil,
//                                style = MaterialTheme.typography.bodySmall,
//                                maxLines = 2,
//                                overflow = TextOverflow.Ellipsis
//                            )
//                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (menuItem.is_available == "YES") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            when (tableStatus) {
                                "AC" -> CurrencySettings.format(
                                    menuItem.ac_rate
                                )
                                "TAKEAWAY", "DELIVERY" -> CurrencySettings.format(
                                    menuItem.parcel_rate
                                )
                                else -> CurrencySettings.format(menuItem.rate)
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 26.dp))

                        // ---- MINUS BUTTON ----
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .border(1.dp, ErrorRed, RoundedCornerShape(4.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = { onRemoveItem() })
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "-",
                                color = ErrorRed,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                        // ---- QUANTITY ----
//                        if (quantity > 0) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                ), contentAlignment = Alignment.Center
                        )
                        {
                            Text(
                                text = quantity.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
//                        } else {
//                            Spacer(modifier = Modifier.width(36.dp))
//                        }
                        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                        // ---- PLUS BUTTON ----
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .border(1.dp, DarkGreen, RoundedCornerShape(4.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = { onAddItem() })
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "+",
                                color = DarkGreen,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.padding(horizontal = 16.dp))
                        // ---- MODIFIER BUTTON ----
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .border(1.dp, BluePrimary, RoundedCornerShape(4.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = { onModifierClick() })
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "A",
                                color = DarkGreen,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
//                        IconButton(
//                            onClick = onModifierClick
//                        ) {
//                            androidx.compose.material3.Icon(
//                                Icons.Default.Tune,
//                                contentDescription = "AddOn",
//                                tint = Color.Blue
//                            )
//                        }
                    }
                } else {
                    Text(
                        text = "Not Available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            AnimatedVisibility(visible = quantity > 0 || existingQuantity > 0) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ModernDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isExistingOrder && existingQuantity > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Existing: $existingQuantity × ${
                                    if (tableStatus == "AC") CurrencySettings.format(
                                        menuItem.ac_rate
                                    ) else if (tableStatus == "TAKEAWAY" || tableStatus == "DELIVERY") CurrencySettings.format(
                                        menuItem.parcel_rate
                                    ) else "₹${String.format("%.2f", menuItem.rate)}"
                                }",
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor
                            )
                            Text(
                                text = if (tableStatus == "AC") CurrencySettings.format(
                                    existingQuantity * menuItem.ac_rate
                                ) else if (tableStatus == "TAKEAWAY" || tableStatus == "DELIVERY") CurrencySettings.format(
                                    existingQuantity * menuItem.parcel_rate
                                ) else CurrencySettings.format(
                                    existingQuantity * menuItem.rate
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor
                            )
                        }
                        if (quantity > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    if (quantity > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${if (isExistingOrder) "New: " else ""}$quantity × ${
                                    if (tableStatus == "AC") CurrencySettings.format(
                                        menuItem.ac_rate
                                    ) else if (tableStatus == "TAKEAWAY" || tableStatus == "DELIVERY") CurrencySettings.format(
                                        menuItem.parcel_rate
                                    ) else CurrencySettings.format(menuItem.rate)
                                }",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isExistingOrder) FontWeight.Bold else FontWeight.Normal,
                                color = textColor
                            )

                            Text(
                                text = if (tableStatus == "AC") CurrencySettings.format(
                                    quantity * menuItem.ac_rate
                                ) else if (tableStatus == "TAKEAWAY" || tableStatus == "DELIVERY") CurrencySettings.format(
                                    quantity * menuItem.parcel_rate
                                ) else CurrencySettings.format(quantity * menuItem.rate),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
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
                        .heightIn(max = 250.dp) // restrict height so dialog doesn’t grow infinitely
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

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "A KOT will be generated and sent to the kitchen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
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
                        .heightIn(max = 250.dp) // restrict height so dialog doesn’t grow infinitely
                        .verticalScroll(rememberScrollState())
                ) {
                    val existingItems = items.groupBy { it.kot_number }
                    existingItems.forEach {
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

//                    selectedItems.forEach { (item, qty) ->
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 4.dp),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(
//                                "${item.menu_item_name} × $qty",
//                                style = MaterialTheme.typography.bodySmall
//                            )
//
//
//
//                            Text(
//                                CurrencySettings.format((item.actual_rate * qty)),
//                                style = MaterialTheme.typography.bodySmall
//                            )
//                        }
//                    }
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