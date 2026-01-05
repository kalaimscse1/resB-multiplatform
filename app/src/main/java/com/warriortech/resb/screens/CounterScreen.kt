package com.warriortech.resb.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.warriortech.resb.ui.viewmodel.CounterViewModel
import kotlinx.coroutines.launch
import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.IconButton
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ScrollableTabRow
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Tab
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.model.TblOrderDetailsResponse
import com.warriortech.resb.ui.components.MobileOptimizedButton
import com.warriortech.resb.ui.components.ModernDivider
import com.warriortech.resb.ui.theme.Black
import com.warriortech.resb.ui.theme.BluePrimary
import com.warriortech.resb.ui.theme.DarkGreen
import com.warriortech.resb.ui.theme.ErrorRed
import com.warriortech.resb.ui.theme.LightGreen
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SecondaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.theme.ghostWhite
import com.warriortech.resb.util.AnimatedSnackbarDemo
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.getDeviceInfo
import com.warriortech.resb.util.scrollToBottomSmooth

@SuppressLint("StateFlowValueCalledInComposition", "DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterScreen(
    onBackPressed: () -> Unit,
    onProceedToBilling: (orderDetailsResponse: List<TblOrderDetailsResponse>, orderId: String) -> Unit,
    viewModel: CounterViewModel = hiltViewModel(),
    drawerState: DrawerState,
    counterId: Long? = null,
    navController: NavHostController
) {
    val menuState by viewModel.menuState.collectAsStateWithLifecycle()
    val selectedItems by viewModel.selectedItems.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val showModifierDialog by viewModel.showModifierDialog.collectAsStateWithLifecycle()
    val selectedMenuItemForModifier by viewModel.selectedMenuItemForModifier.collectAsStateWithLifecycle()
    val modifierGroups by viewModel.modifierGroups.collectAsStateWithLifecycle()
    val orderDetailsResponse by viewModel.orderDetailsResponse.collectAsStateWithLifecycle()
    val orderId by viewModel.orderId.collectAsStateWithLifecycle()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isOrderPlaced by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadMenuItems()
    }

    LaunchedEffect(counterId) {
        if (counterId != null) {
            // Load counter information by ID
            // This would typically come from a repository call
        }
    }

    BackHandler {
        navController.navigate("dashboard") {
            popUpTo("dashboard") { inclusive = true }
        }
    }

    // Observe when order is successfully placed and proceed to billing
    LaunchedEffect(orderDetailsResponse, isOrderPlaced) {
        scope.launch {
            if (isOrderPlaced && orderDetailsResponse.isNotEmpty()) {
//                delay(2000)
                onProceedToBilling(orderDetailsResponse, orderId ?: "")
                showConfirmDialog = false
                isOrderPlaced = false
            }
        }
    }

    if (showConfirmDialog) {
        BillConfirmationDialog(
            onConfirm = {
                // Set flag to indicate order should be placed and processed
                isOrderPlaced = true
                viewModel.placeOrder(2, "")
            },
            onDismiss = {
                showConfirmDialog = false
                // Reset loading state if it was set
                viewModel.resetToSuccessState()
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val currentCounter by viewModel.currentCounter.collectAsStateWithLifecycle()
                    Text(
                        if (currentCounter != null)
                            "Counter Billing - ${currentCounter!!.code}"
                        else
                            "Counter Billing",
                        color = SurfaceLight
                    )
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
                    IconButton(onClick = {
                        viewModel.clearOrder()
                    }) {
                        Icon(
                            imageVector = Icons.Default.RemoveShoppingCart,
                            contentDescription = "Clear Cart",
                            tint = SurfaceLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen
                )
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val totalItemCount = selectedItems.values.sum()
                    val totalAmount = viewModel.getOrderTotal()

                    Column {
                        Text(
                            text = "Total Items: $totalItemCount",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Total: ${CurrencySettings.format(totalAmount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    MobileOptimizedButton(
                        onClick = {
                            showConfirmDialog = true
                        },
                        enabled = selectedItems.isNotEmpty(),
                        text = "Proceed to Bill",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        snackbarHost = {
            AnimatedSnackbarDemo(snackbarHostState)
        }
    ) { paddingValues ->

        when (val currentMenuState = menuState) {
            is CounterViewModel.MenuUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is CounterViewModel.MenuUiState.Success -> {

                val menuItems = currentMenuState.menuItems
                val filteredMenuItems =
                    if (selectedCategory != null && selectedCategory == "FAVOURITES") {
                        menuItems.filter { it.is_favourite == true }// Make sure selectedCategory is handled safely
                    } else if (selectedCategory != null && selectedCategory == "ALL") {
                        menuItems
                    } else if (selectedCategory != null) {
                        menuItems.filter { it.item_cat_name == selectedCategory } // Show all items if "ALL" is selected
                    } else {
                        menuItems // No filtering if no category is selected
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
                    val listState = rememberLazyListState()
                    val scope = rememberCoroutineScope()
                    val bottomBarHeight = 80.dp

                    // 👇 Reusable helper: auto-correct when last item overlaps BottomBar
//                    listState.ensureLastItemVisible(bottomBarHeight)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(color = Color.White)
                    ) {
                        // Categories TabRow
                        if (categories.isNotEmpty()) {
                            ScrollableTabRow(
                                selectedTabIndex = categories.indexOf(selectedCategory)
                                    .coerceAtLeast(0),
                                backgroundColor = SecondaryGreen,
                                contentColor = SurfaceLight
                            ) {
                                categories.forEachIndexed { index, category ->
                                    Tab(
                                        selected = selectedCategory == category,
                                        onClick = { viewModel.selectedCategory.value = category },
                                        text = { androidx.compose.material.Text(category) }
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

                            items(filteredMenuItems, key = { it.menu_item_id }) { menuItem ->
                                CounterMenuItemCard(
                                    menuItem = menuItem,
                                    quantity = selectedItems[menuItem] ?: 0,
                                    onAddItem = {
                                        viewModel.addItemToOrder(menuItem)
                                        scope.scrollToBottomSmooth(listState) // 👈 reusable
                                    },
                                    onRemoveItem = {
                                        viewModel.removeItemFromOrder(menuItem)
                                        scope.scrollToBottomSmooth(listState) // 👈 reusable
                                    },
                                    onModifierClick = { viewModel.showModifierDialog(menuItem) },
                                    backgroundColor = if ((selectedItems[menuItem]
                                            ?: 0) > 0
                                    )
                                        LightGreen else SecondaryGreen,
                                    contentColor = if ((selectedItems[menuItem]
                                            ?: 0) > 0
                                    )
                                        LightGreen else ghostWhite,
                                    textColor = Black
                                )
                            }

                            // 👇 Spacer ensures last item never hides under BottomBar
                            item { Spacer(modifier = Modifier.height(bottomBarHeight)) }
                        }
                    }
                }
            }

            is CounterViewModel.MenuUiState.Error -> {
                val errorMessage = (menuState as CounterViewModel.MenuUiState.Error).message

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
    }
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
}

@SuppressLint("DefaultLocale")
@Composable
fun CounterMenuItemCard(
    menuItem: TblMenuItemResponse,
    quantity: Int,
    onAddItem: () -> Unit,
    onRemoveItem: () -> Unit,
    onModifierClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    textColor: Color
) {
    val IconBoldA: ImageVector = ImageVector.Builder(
        name = "BoldA",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero
        ) {
            // Outer A shape
            moveTo(12f, 4f)      // Top point
            lineTo(3f, 20f)      // Bottom left
            lineTo(7f, 20f)      // Left foot
            lineTo(8.6f, 16.8f)  // Up left leg to crossbar
            lineTo(15.4f, 16.8f) // Across crossbar
            lineTo(17f, 20f)     // Down right leg to right foot
            lineTo(21f, 20f)     // Bottom right
            close()

            // Crossbar
            moveTo(9.6f, 14f)
            lineTo(14.4f, 14f)
            lineTo(12f, 8.8f)
            close()
        }
    }.build()

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
                            color = textColor
                        )
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
                            CurrencySettings.format(menuItem.rate),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 28.dp))

                        // ---- MINUS BUTTON ----
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .border(1.dp, ErrorRed, RoundedCornerShape(4.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures { onRemoveItem() }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "-",
                                color = ErrorRed,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
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
                                fontWeight = FontWeight.Bold,
                                color = textColor
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
                                    detectTapGestures { onAddItem() }
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

//                         ---- MODIFIER BUTTON ----
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
                    }
                } else {
                    Text(
                        text = "Not Available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }


            AnimatedVisibility(visible = quantity > 0) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ModernDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$quantity × ${CurrencySettings.format(menuItem.rate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )

                        Text(
                            text = CurrencySettings.format(quantity * menuItem.rate),
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


@Composable
fun BillConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
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
            Text("Confirm To Bill", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(
                "Are you sure you want to proceed with billing? This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )
}