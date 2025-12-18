package com.warriortech.resb.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.ui.components.MobileOptimizedButton
import com.warriortech.resb.ui.components.MobileOptimizedButtonColor
import com.warriortech.resb.ui.theme.*
import com.warriortech.resb.ui.viewmodel.CounterViewModel
import com.warriortech.resb.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@SuppressLint("ConfigurationScreenWidthHeight")
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemWiseBillScreen(
    viewModel: CounterViewModel = hiltViewModel(),
    drawerState: DrawerState,
    navController: NavHostController,
    onProceedToBilling: (orderDetailsResponse: Map<TblMenuItemResponse, Int>) -> Unit
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedItems by viewModel.selectedItems.collectAsStateWithLifecycle()
    val menuState by viewModel.menuState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    var showDialog by remember { mutableStateOf(false) }
    var showBillDialog by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var isProcessingCash by remember { mutableStateOf(false) }
    var isProcessingOthers by remember { mutableStateOf(false) }

    var cartOffset by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    var values by remember { mutableStateOf(PaddingValues(0.dp)) }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val columns = when {
        screenWidthDp >= 1200 -> 10
        screenWidthDp >= 800 -> 10
        else -> 3
    }
    var isClearClicked by remember { mutableStateOf(false) }
    var isSaveClicked by remember { mutableStateOf(false) }
    var isOtherClicked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadMenuItems() }

    LaunchedEffect(selectedItems.size) {
        if (selectedItems.isNotEmpty()) listState.animateScrollToItem(selectedItems.size - 1)
    }

    Scaffold(
        snackbarHost = { AnimatedSnackbarDemo(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quick Bill", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PrimaryGreen
                ),
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = SurfaceLight)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Clear Button
                        MobileOptimizedButtonColor(
                            text = "Clear",
                            onClick = {
                                showDialog = true
                                isClearClicked = true
                                isSaveClicked = false
                                isOtherClicked = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            textColor = if (isClearClicked) Color.White else ModernErrorRed,
                            containerColor = if (isClearClicked) ModernErrorRed else Color.White,
                            borderColor = if (isClearClicked) ModernErrorRed else ModernErrorRed
                        )

                        // Cash Button
                        MobileOptimizedButtonColor(
                            text = if (isProcessingCash) "Processing..." else "Cash",
                            enabled = !isProcessingCash,
                            onClick = {
                                isSaveClicked = true
                                isClearClicked = false
                                isOtherClicked = false
                                if (selectedItems.isNotEmpty()) {
                                    scope.launch {
                                        isProcessingCash = true
                                        viewModel.cashPrintBill()
                                        success = true
                                        delay(1500)
                                        success = false
                                        isProcessingCash = false
                                    }
                                } else showBillDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            textColor = if (isSaveClicked) Color.White else ModernDarkGreen,
                            containerColor = if (isSaveClicked) ModernDarkGreen else Color.White,
                            borderColor = if (isSaveClicked) ModernDarkGreen else ModernDarkGreen
                        )

                        // Others Button
                        MobileOptimizedButtonColor(
                            text = if (isProcessingOthers) "Processing..." else "Others",
                            enabled = !isProcessingOthers,
                            onClick = {
                                isOtherClicked = true
                                isSaveClicked = false
                                isClearClicked = false
                                if (selectedItems.isNotEmpty()) {
                                    isProcessingOthers = true
                                    onProceedToBilling(selectedItems)
                                    navController.navigate("quick_bill")
                                    isProcessingOthers = false
                                } else showBillDialog = true

                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            textColor = if (isOtherClicked) Color.White else ModernOrange,
                            containerColor = if (isOtherClicked) ModernOrange else Color.White,
                            borderColor = if (isOtherClicked) ModernOrange else ModernOrange
                        )
                    }
                }
            }
        }
    ) { padding ->
        values = padding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SecondaryGreen)
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                Text("ITEM", Modifier.weight(3f), color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "QTY",
                    Modifier.weight(2f),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "RATE",
                    Modifier.weight(2f),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
                Text(
                    "TOTAL",
                    Modifier.weight(2f),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }

            // Selected items list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState
            ) {
                items(
                    selectedItems.entries.toList(),
                    key = { "${it.key.menu_item_id}_${it.key.menu_id}" }) { entry ->
                    val item = entry.key
                    val qty = entry.value
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .onGloballyPositioned { coords ->
                                    val pos = coords.localToRoot(Offset.Zero)
                                    cartOffset = pos
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                item.menu_item_name,
                                maxLines = 1,
                                fontSize = when {
                                    screenWidthDp >= 1200 -> 18.sp
                                    screenWidthDp >= 800 -> 16.sp
                                    else -> 12.sp
                                },
                                modifier = Modifier.weight(3f)
                            )
                            Row(
                                modifier = Modifier.weight(2f),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                QuantityButton("-", ModernErrorRed) {
                                    viewModel.removeItemFromOrder(item)
                                }
                                Text(
                                    "$qty",
                                    fontSize = when {
                                        screenWidthDp >= 1200 -> 18.sp
                                        screenWidthDp >= 800 -> 16.sp
                                        else -> 14.sp
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                QuantityButton("+", ModernDarkGreen) {
                                    viewModel.addItemToOrder(item)
                                }
                            }
                            Text(
                                CurrencySettings.formatPlain(item.rate),
                                textAlign = TextAlign.End,
                                fontSize = when {
                                    screenWidthDp >= 1200 -> 18.sp
                                    screenWidthDp >= 800 -> 16.sp
                                    else -> 12.sp
                                },
                                modifier = Modifier.weight(2f)
                            )
                            Text(
                                CurrencySettings.formatPlain(qty * item.rate),
                                textAlign = TextAlign.End,
                                fontSize = when {
                                    screenWidthDp >= 1200 -> 18.sp
                                    screenWidthDp >= 800 -> 16.sp
                                    else -> 12.sp
                                },
                                modifier = Modifier.weight(2f)
                            )
                        }
                        Divider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }
            }

            // Total row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SecondaryGreen)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total Items: ${selectedItems.values.sum()}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = when {
                        screenWidthDp >= 1200 -> 18.sp
                        screenWidthDp >= 800 -> 16.sp
                        else -> 12.sp
                    },
                )
                Text(
                    "Total: ${CurrencySettings.format(selectedItems.entries.sumOf { it.key.rate * it.value })}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = when {
                        screenWidthDp >= 1200 -> 18.sp
                        screenWidthDp >= 800 -> 16.sp
                        else -> 12.sp
                    },
                )
            }

            // Menu items grid
            when (val state = menuState) {
                is CounterViewModel.MenuUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ModernPrimaryGreen)
                    }
                }

                is CounterViewModel.MenuUiState.Success -> {
                    val menuItems = state.menuItems
                    val filteredMenuItems = when {
                        selectedCategory == "FAVOURITES" -> menuItems.filter { it.is_favourite == true }
                        selectedCategory == "ALL" -> menuItems
                        selectedCategory != null -> menuItems.filter { it.item_cat_name == selectedCategory }
                        else -> menuItems
                    }

                    if (menuItems.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No menu items available")
                        }
                    } else {
                        if (categories.isNotEmpty()) {
                            val selectedIndex =
                                categories.indexOf(selectedCategory).takeIf { it >= 0 } ?: 0
                            ScrollableTabRow(
                                selectedTabIndex = selectedIndex,
                                containerColor = Color.White,
                                contentColor = SecondaryGreen
                            ) {
                                categories.forEachIndexed { index, category ->
                                    Tab(
                                        selected = selectedCategory == category,
                                        onClick = { viewModel.selectedCategory.value = category },
                                        text = {
                                            Text(
                                                category, fontSize = when {
                                                    screenWidthDp >= 1200 -> 18.sp
                                                    screenWidthDp >= 800 -> 16.sp
                                                    else -> 12.sp
                                                },
                                                color = PrimaryGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            modifier = Modifier
                                .weight(1f)
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            itemsIndexed(
                                filteredMenuItems,
                                key = { index, p -> "${p.menu_item_id}_${p.menu_id}_$index" }) { _, product ->
                                Card(
                                    modifier = when {
                                        screenWidthDp >= 1200 -> Modifier
                                            .aspectRatio(1f)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .pointerInput(Unit) {
                                                detectTapGestures { tapOffset ->
                                                    FlyToCartController.current?.invoke(
                                                        product,
                                                        tapOffset,
                                                        cartOffset
                                                    )
                                                    viewModel.addItemToOrder(product)
                                                }
                                            }
                                        screenWidthDp >= 800 -> Modifier
                                            .aspectRatio(1f)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .pointerInput(Unit) {
                                                detectTapGestures { tapOffset ->
                                                    FlyToCartController.current?.invoke(
                                                        product,
                                                        tapOffset,
                                                        cartOffset
                                                    )
                                                    viewModel.addItemToOrder(product)
                                                }
                                            }
                                        else -> Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .pointerInput(Unit) {
                                                detectTapGestures { tapOffset ->
                                                    FlyToCartController.current?.invoke(
                                                        product,
                                                        tapOffset,
                                                        cartOffset
                                                    )
                                                    viewModel.addItemToOrder(product)
                                                }
                                            }
                                    } ,
                                    elevation = CardDefaults.cardElevation(4.dp),
                                    colors = CardDefaults.cardColors(containerColor = ghostWhiteDark)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            product.menu_item_name,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2,
                                            fontSize = when {
                                                screenWidthDp >= 1200 -> 18.sp
                                                screenWidthDp >= 800 -> 16.sp
                                                else -> 12.sp
                                            }
                                        )
                                        Text(
                                            CurrencySettings.format(product.rate),
                                            textAlign = TextAlign.Center,
                                            fontSize = when {
                                                screenWidthDp >= 1200 -> 18.sp
                                                screenWidthDp >= 800 -> 16.sp
                                                else -> 12.sp
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

    FlyToCartOverlay()

    if (showDialog) {
        ClearDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                viewModel.clearOrder()
                showDialog = false
                isSaveClicked = false
                isClearClicked = false
                isOtherClicked = false
            }
        )
    }

    if (showBillDialog) {
        MessageBox(
            title = "Alert",
            message = "Please select items to proceed billing.",
            onDismiss = {
                showBillDialog = false
                isSaveClicked = false
                isClearClicked = false
                isOtherClicked = false
            }
        )
    }

    if (success) {
        SuccessDialog(
            title = "Bill Successful",
            description = "Payment Done Successfully",
            paddingValues = values
        )
    }
}

@Composable
fun QuantityButton(symbol: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .pointerInput(Unit) { detectTapGestures { onClick() } },
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FlyToCartOverlay() {
    val scope = rememberCoroutineScope()
    var animItem by remember { mutableStateOf<String?>(null) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (animItem != null) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(6.dp))
                    .padding(6.dp)
            ) {
                Text(animItem ?: "", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }

    FlyToCartController.current = { label, start, end ->
        animItem = label.menu_item_name
        scope.launch {
            offsetX.snapTo(start.x)
            offsetY.snapTo(start.y)
            offsetX.animateTo(end.x, animationSpec = tween(600))
            offsetY.animateTo(end.y, animationSpec = tween(600))
            animItem = null
        }
    }
}

@Composable
fun ClearDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear Items") },
        text = { Text("Are you sure you want to clear items?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ModernDarkGreen)
            ) {
                Text("Ok")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Cancel")
            }
        }
    )
}

object FlyToCartController {
    var current: ((TblMenuItemResponse, Offset, Offset) -> Unit)? = null
}

@Composable
fun ActionButton(
    text: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.6f)
        ),
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Text(text, color = Color.White)
    }
}