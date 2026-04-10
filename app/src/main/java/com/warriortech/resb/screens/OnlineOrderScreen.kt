package com.warriortech.resb.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.model.TblOnline
import com.warriortech.resb.ui.components.MobileOptimizedButton
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.MenuViewModel
import com.warriortech.resb.ui.viewmodel.OnlineOrderViewModel
import com.warriortech.resb.util.CurrencySettings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineOrderScreen(
    onBackPressed: () -> Unit,
    viewModel: OnlineOrderViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val platforms by viewModel.onlinePlatforms.collectAsStateWithLifecycle()
    val selectedPlatform by viewModel.selectedPlatform.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val menuState by viewModel.menuState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    
    var showPlatformSelection by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        if (uiState is OnlineOrderViewModel.OnlineOrderUiState.Success) {
            val billNo = (uiState as OnlineOrderViewModel.OnlineOrderUiState.Success).billNo
            snackbarHostState.showSnackbar("Order placed and billed successfully. Bill No: $billNo")
            showPlatformSelection = true
        } else if (uiState is OnlineOrderViewModel.OnlineOrderUiState.Error) {
            snackbarHostState.showSnackbar((uiState as OnlineOrderViewModel.OnlineOrderUiState.Error).message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showPlatformSelection) "Select Online Platform" else "Online Order - ${selectedPlatform?.online_order_name}", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (showPlatformSelection) onBackPressed() else showPlatformSelection = true
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(SurfaceLight)) {
            if (showPlatformSelection) {
                PlatformSelectionContent(
                    platforms = platforms,
                    onPlatformSelected = {
                        viewModel.selectPlatform(it)
                        showPlatformSelection = false
                    }
                )
            } else {
                OnlineMenuContent(
                    menuState = menuState,
                    cartItems = cartItems,
                    refNo = viewModel.refNo.collectAsStateWithLifecycle().value,
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) },
                    onRefNoChange = { viewModel.updateRefNo(it) },
                    onAddItem = { viewModel.addToCart(it) },
                    onRemoveItem = { viewModel.removeFromCart(it) },
                    onPlaceAndBill = { viewModel.placeAndBillOrder() },
                    isLoading = uiState is OnlineOrderViewModel.OnlineOrderUiState.Loading
                )
            }
        }
    }
}

@Composable
fun PlatformSelectionContent(
    platforms: List<TblOnline>,
    onPlatformSelected: (TblOnline) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(platforms) { platform ->
            Card(
                modifier = Modifier.fillMaxWidth().height(120.dp).clickable { onPlatformSelected(platform) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = platform.online_order_name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun OnlineMenuContent(
    menuState: MenuViewModel.MenuUiState,
    cartItems: Map<TblMenuItemResponse, Int>,
    refNo: String,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onRefNoChange: (String) -> Unit,
    onAddItem: (TblMenuItemResponse) -> Unit,
    onRemoveItem: (TblMenuItemResponse) -> Unit,
    onPlaceAndBill: () -> Unit,
    isLoading: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = refNo,
            onValueChange = onRefNoChange,
            label = { Text("Online Reference No (e.g. Zomato ID)") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true
        )

        if (categories.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                containerColor = Color.White,
                contentColor = PrimaryGreen,
                edgePadding = 16.dp,
                divider = {}
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        text = { Text(category.uppercase(), style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }
        }

        Row(modifier = Modifier.weight(1f)) {
            // Menu Items List
            Box(modifier = Modifier.weight(0.6f)) {
                when (menuState) {
                    is MenuViewModel.MenuUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is MenuViewModel.MenuUiState.Success -> {
                        val filteredItems = if (selectedCategory == "ALL") menuState.menuItems else menuState.menuItems.filter { it.item_cat_name == selectedCategory }
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
                            items(filteredItems) { item ->
                                OnlineMenuItemRow(item, onAddItem)
                            }
                        }
                    }
                    else -> Text("Error loading menu", modifier = Modifier.align(Alignment.Center))
                }
            }

            // Cart Summary
            Surface(
                modifier = Modifier.weight(0.4f).fillMaxHeight(),
                tonalElevation = 2.dp,
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Cart Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(cartItems.entries.toList()) { (item, qty) ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.menu_item_name, style = MaterialTheme.typography.bodyMedium)
                                    Text(CurrencySettings.format(item.rate), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { onRemoveItem(item) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove", tint = Color.Red)
                                    }
                                    Text("$qty", modifier = Modifier.padding(horizontal = 8.dp))
                                    IconButton(onClick = { onAddItem(item) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Add", tint = PrimaryGreen)
                                    }
                                }
                            }
                        }
                    }

                    val total = cartItems.entries.sumOf { it.key.rate * it.value }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontWeight = FontWeight.Bold)
                        Text(CurrencySettings.format(total), fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    MobileOptimizedButton(
                        onClick = onPlaceAndBill,
                        text = if (isLoading) "Processing..." else "Place & Bill",
                        enabled = cartItems.isNotEmpty() && !isLoading && refNo.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun OnlineMenuItemRow(item: TblMenuItemResponse, onAdd: (TblMenuItemResponse) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onAdd(item) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.menu_item_name, fontWeight = FontWeight.Medium)
                Text(CurrencySettings.format(item.rate), style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = PrimaryGreen)
        }
    }
}
