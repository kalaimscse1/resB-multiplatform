package com.warriortech.resb.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.ui.components.MobileOptimizedButton
import com.warriortech.resb.ui.components.ModernDivider
import com.warriortech.resb.ui.components.BarcodeScannerButton
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.ui.viewmodel.payment.BillingViewModel
import com.warriortech.resb.util.CurrencySettings
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickBillScreen(
    navController: NavHostController,
    viewModel: BillingViewModel = hiltViewModel(),
    orderDetailsResponse: Map<TblMenuItemResponse, Int>? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val selectedItems by viewModel.selectedItems.collectAsStateWithLifecycle()
    val totalAmount by viewModel.totalAmount.collectAsStateWithLifecycle()
    val orderId by viewModel.orderId.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var isOrderPlaced by remember { mutableStateOf(false) }
    val orderDetailsResponse1 by viewModel.orderDetailsResponse1.collectAsStateWithLifecycle()



    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(orderDetailsResponse) {
        when {
            orderDetailsResponse != null -> viewModel.setMenuDetails(orderDetailsResponse)
        }
        viewModel.updateTotal()
    }

    LaunchedEffect(orderDetailsResponse1, isOrderPlaced) {
        scope.launch {
            if (isOrderPlaced && orderDetailsResponse1.isNotEmpty()) {
//                delay(2000)
                navController.navigate("payment_screen/${totalAmount}/${orderId}/${"--"}/${0L}/${""}") {
                    launchSingleTop = true
                    restoreState = true
                }
                isOrderPlaced = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Bill Summary", color = SurfaceLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
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
            BottomAppBar(
                containerColor = PrimaryGreen,
                contentColor = SurfaceLight
            ) {
                MobileOptimizedButton(
                    onClick = {
                        scope.launch {
                            viewModel.placeOrder(selectedItems)
                            isOrderPlaced = true
                        }
                    },
                    text = "Proceed to Payment",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) { paddingValues ->
        BillContent(
            modifier = Modifier.padding(paddingValues),
            uiState = selectedItems,
            onUpdateQuantity = { item, newQuantity ->
                viewModel.updateItemQuantity(item, newQuantity)
            },
            onRemoveItem = { item ->
                viewModel.removeItem(item)
            }
        )
//        if (selectedItems.isEmpty()){
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//                    .padding(16.dp),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = "No items Found.",
//                    style = MaterialTheme.typography.bodyLarge,
//                    fontWeight = FontWeight.Medium,
//                    textAlign = TextAlign.Center
//                )
//            }
//            return@Scaffold
//        }
//        else{
//            BillContent(
//                modifier = Modifier.padding(paddingValues),
//                uiState = selectedItems,
//                onUpdateQuantity = { item, newQuantity ->
//                    viewModel.updateItemQuantity(item, newQuantity)
//                },
//                onRemoveItem = { item ->
//                    viewModel.removeItem(item)
//                }
//            )
//        }
    }
}

@Composable
fun BillContent(
    modifier: Modifier = Modifier,
    uiState: Map<TblMenuItemResponse, Int>,
    onUpdateQuantity: (TblMenuItemResponse, Int) -> Unit,
    onRemoveItem: (TblMenuItemResponse) -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Text("Items", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }
        items(uiState.size) { index ->
            val menuItem = uiState.keys.elementAt(index)
            val quantity = uiState[menuItem] ?: 0
            BillItemRow(
                menuItem = menuItem,
                quantity = quantity,
                tableStatus = "",
            )
        }
        item { ModernDivider(modifier = Modifier.padding(vertical = 8.dp)) }
    }
}


@Composable
fun BillItemRow(
    menuItem: TblMenuItemResponse,
    quantity: Int,
    tableStatus: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = menuItem.menu_item_name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (tableStatus == "AC") {
                    Text(
                        text = "Table Service",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = CurrencySettings.format(menuItem.rate * quantity),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}