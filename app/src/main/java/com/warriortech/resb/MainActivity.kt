package com.warriortech.resb

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.warriortech.resb.data.sync.SyncManager
import com.warriortech.resb.model.*
import com.warriortech.resb.network.RetrofitClient
import com.warriortech.resb.network.RetrofitClient.apiService
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.screens.*
import com.warriortech.resb.screens.accounts.master.GroupScreen
import com.warriortech.resb.screens.accounts.master.LedgerScreen
import com.warriortech.resb.screens.accounts.reports.DayBookReportScreen
import com.warriortech.resb.screens.accounts.reports.DayEntryReportScreen
import com.warriortech.resb.screens.accounts.transaction.DayEntryModifyScreen
import com.warriortech.resb.screens.accounts.transaction.DayEntryScreen
import com.warriortech.resb.screens.reports.*
import com.warriortech.resb.screens.reports.gst.GSTRReportScreen
import com.warriortech.resb.screens.reports.gst.HsnReportScreen
import com.warriortech.resb.screens.settings.*
import com.warriortech.resb.ui.components.ModernDivider
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.ResbTheme
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    var onVolumeUpPressed: (() -> Unit)? = null

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase ?: this))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.onAttach(this)
        recreate()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            onVolumeUpPressed?.let {
                it()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var syncManager: SyncManager

    @SuppressLint("ConfigurationScreenWidthHeight")
    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
        lifecycleScope.launch {
            networkMonitor.isOnline.collect { connectionState ->
                if (connectionState == ConnectionState.Available) {
                    syncManager.scheduleSyncWork()
                }
            }
        }
        setContent {
            ResbTheme(darkTheme = isSystemInDarkTheme()) {
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val navController = rememberNavController()
                val context = LocalContext.current
                val connectionState by networkMonitor.isOnline.collectAsState(initial = ConnectionState.Available)
                val configuration = LocalConfiguration.current
                val screenWidth = configuration.screenWidthDp
                val screenHeight = configuration.screenHeightDp
                val isLargeScreen = screenWidth >= 600
                val isTablet = screenWidth >= 600 && screenHeight >= 960
                val isLandscape = screenWidth > screenHeight
                val isCollapsed = remember { mutableStateOf(false) }

                val drawerWidth = when {
                    isTablet -> if (isCollapsed.value) 80.dp else 320.dp
                    isLargeScreen -> if (isCollapsed.value) 72.dp else 280.dp
                    isLandscape -> if (isCollapsed.value) 72.dp else (screenWidth * 0.6f).dp.coerceAtMost(300.dp)
                    else -> if (isCollapsed.value) 72.dp else (screenWidth * 0.8f).dp.coerceAtMost(300.dp)
                }
                val animatedDrawerWidth by animateDpAsState(targetValue = drawerWidth, label = "drawerWidth")

                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                val showDrawer = currentRoute != null && currentRoute !in listOf("login", "splash", "registration")
                
                val drawerContent = @Composable {
                    DrawerContent(
                        isCollapsed = isCollapsed.value,
                        drawerWidth = animatedDrawerWidth,
                        onCollapseToggle = { isCollapsed.value = !isCollapsed.value },
                        onDestinationClicked = { route ->
                            scope.launch { drawerState.close() }
                            if (route == "logout") {
                                scope.launch {
                                    val sharedPref = context.getSharedPreferences("user_prefs", MODE_PRIVATE)
                                    sessionManager.saveUserLogin(false)
                                    sharedPref.edit { clear() }
                                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                        navController = navController,
                        sessionManager = sessionManager
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        NetworkStatusBar(connectionState = connectionState)
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (showDrawer) {
                                ModalNavigationDrawer(
                                    drawerState = drawerState,
                                    drawerContent = drawerContent,
                                    gesturesEnabled = true
                                ) {
                                    AppNavigation(drawerState, navController, sessionManager)
                                }
                            } else {
                                AppNavigation(drawerState, navController, sessionManager)
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun AppNavigation(
    drawerState: DrawerState,
    navController: NavHostController,
    sessionManager: SessionManager
) {
    var selectedTable by remember { mutableStateOf<Table?>(null) }
    var isTakeaway by remember { mutableStateOf("") }
    var selectedItems by remember { mutableStateOf(listOf<TblOrderDetailsResponse>()) }
    var kotRes by remember { mutableStateOf<KotResponse?>(null) }
    var selecteItems by remember { mutableStateOf<Map<TblMenuItemResponse, Int>>(mutableMapOf()) }

    LaunchedEffect(Unit) {
        val subscriptionManager = SubscriptionManager(sessionManager)
        if (subscriptionManager.isSubscriptionExpired()) {
            sessionManager.clearSession()
            navController.navigate("login") {
                popUpTo(0)
            }
        }
    }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    val isLoggedInPref = sessionManager.getUserLogin()
                    if (isLoggedInPref) {
                        if (sessionManager.getUser()?.role == "WAITER") {
                            navController.navigate("selects") { popUpTo("splash") { inclusive = true } }
                        } else {
                            navController.navigate("dashboard") { popUpTo("splash") { inclusive = true } }
                        }
                    } else {
                        navController.navigate("login") { popUpTo("splash") { inclusive = true } }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    if (sessionManager.getUser()?.role == "WAITER") {
                        navController.navigate("selects") { popUpTo("splash") { inclusive = true } }
                    } else {
                        navController.navigate("dashboard") { popUpTo("splash") { inclusive = true } }
                    }
                },
                onRegisterClick = { navController.navigate("registration") },
                sessionManager = sessionManager
            )
        }
        composable("selects") {
            SelectionScreen(
                onTableSelected = { table ->
                    isTakeaway = "TABLE"
                    selectedTable = table
                    navController.navigate("menu")
                },
                drawerState = drawerState,
                sessionManager = sessionManager,
                navController = navController
            )
        }
        composable("menu") {
            MenuScreen(
                isTakeaway = isTakeaway,
                tableStatId = selectedTable != null || isTakeaway == "TABLE",
                tableId = selectedTable?.table_id ?: 1L,
                onBackPressed = { navController.popBackStack() },
                onOrderPlaced = { navController.popBackStack() },
                drawerState = drawerState,
                onBillPlaced = { items, orderId ->
                    selectedItems = items
                    navController.navigate("billing_screen/${orderId}")
                },
                navController = navController,
                sessionManager = sessionManager,
                tableName = selectedTable?.table_name ?: ""
            )
        }
        composable("takeaway_menu") {
            MenuScreen(
                isTakeaway = "TAKEAWAY",
                tableStatId = false,
                tableId = 1L,
                onBackPressed = { navController.popBackStack() },
                onOrderPlaced = {
                    navController.navigate("takeaway_menu") { popUpTo("takeaway_menu") { inclusive = true } }
                    selectedTable = null
                },
                drawerState = drawerState,
                onBillPlaced = { items, orderId ->
                    selectedItems = items
                    navController.navigate("billing_screen/${orderId}")
                },
                navController = navController,
                sessionManager = sessionManager,
                tableName = "TAKEAWAY"
            )
        }
        composable("delivery_menu") {
            MenuScreen(
                isTakeaway = "DELIVERY",
                tableStatId = false,
                tableId = 1L,
                onBackPressed = { navController.popBackStack() },
                onOrderPlaced = {
                    navController.navigate("delivery_menu") { popUpTo("delivery_menu") { inclusive = true } }
                    selectedTable = null
                },
                drawerState = drawerState,
                onBillPlaced = { items, orderId ->
                    selectedItems = items
                    navController.navigate("billing_screen/${orderId}")
                },
                navController = navController,
                sessionManager = sessionManager,
                tableName = "DELIVERY"
            )
        }
        composable("billing_screen/{orderMasterId}") { backStackEntry ->
            BillingScreen(
                navController = navController,
                orderDetailsResponse = selectedItems,
                orderMasterId = backStackEntry.arguments?.getString("orderMasterId"),
                onProceedToBilling = { selecteItems = it },
                sessionManager = sessionManager
            )
        }
        composable("print_settings") {
            PrintSettingsScreen(onBackPressed = { navController.popBackStack() })
        }
        composable("payment_screen/{amountToPayFromRoute}/{orderId}/{bill_no}/{customerId}/{voucherType}") { entry ->
            PaymentScreen(
                navController = navController,
                amountToPayFromRoute = entry.arguments?.getString("amountToPayFromRoute")?.toDoubleOrNull() ?: 0.0,
                orderMasterId = entry.arguments?.getString("orderId") ?: "",
                sessionManager = sessionManager,
                billNo = entry.arguments?.getString("bill_no") ?: "",
                customerId = entry.arguments?.getString("customerId")?.toLongOrNull() ?: 0L,
                voucherType = entry.arguments?.getString("voucherType") ?: "",
                orderDetailsResponse = selecteItems
            )
        }
        composable("kitchen") { KitchenScreen(navController = navController, drawerState = drawerState) }
        composable("orders") {
            OrderScreen(
                drawerState = drawerState,
                onNavigateToBilling = { items, orderId ->
                    selectedItems = items
                    navController.navigate("billing_screen/${orderId}") { popUpTo("orders") { inclusive = true } }
                }
            )
        }
        composable("takeaway_orders") {
            TakeAwayOrderScreen(
                drawerState = drawerState,
                onNavigateToBilling = { items, orderId ->
                    selectedItems = items
                    navController.navigate("billing_screen/${orderId}") { popUpTo("orders") { inclusive = true } }
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBackPressed = { navController.popBackStack() },
                drawerState = drawerState,
                navController = navController,
                sessionManager = sessionManager
            )
        }
        composable("area_setting") { AreaSettingsScreen(onBackPressed = { navController.popBackStack() }, drawerState = drawerState, navController = navController) }
        composable("table_setting") { TableSettingsScreen(onBackPressed = { navController.popBackStack() }, drawerState = drawerState, navController = navController) }
        composable("menu_setting") { MenuSettingsScreen(onBackPressed = { navController.popBackStack() }, drawerState = drawerState, navController = navController) }
        composable("menu_item_setting") { MenuItemSettingsScreen(onBackPressed = { navController.popBackStack() }, sessionManager = sessionManager, drawerState = drawerState, navController = navController) }
        composable("menu_Category_setting") { MenuCategorySettingsScreen(onBackPressed = { navController.popBackStack() }, drawerState = drawerState, navController = navController) }
        composable("staff_setting") { StaffSettingsScreen(onBackPressed = { navController.popBackStack() }, navController = navController) }
        composable("customer_setting") { CustomerSettingsScreen(onBackPressed = { navController.popBackStack() }, navController = navController) }
        composable("counter_selection") {
            CounterSelectionScreen(
                onCounterSelected = { counter ->
                    navController.navigate("counter/${counter.id}") { popUpTo("counter_selection") { inclusive = true } }
                },
                drawerState = drawerState
            )
        }
        composable("counter/{counterId}") { backStackEntry ->
            val counterId = backStackEntry.arguments?.getString("counterId")?.toLongOrNull() ?: 1L
            CounterScreen(onBackPressed = { navController.popBackStack() }, onProceedToBilling = { items, orderId -> selectedItems = items; navController.navigate("billing_screen/${orderId}") }, drawerState = drawerState, counterId = counterId, navController = navController)
        }
        composable("counter") {
            CounterScreen(onBackPressed = { navController.popBackStack() }, onProceedToBilling = { items, orderId -> selectedItems = items; navController.navigate("billing_screen/${orderId}") }, drawerState = drawerState, navController = navController)
        }
        composable("dashboard") {
            DashboardScreen(
                drawerState = drawerState,
                onNavigateToOrders = { navController.navigate("orders") },
                onNavigateToBilling = { navController.navigate("counter") },
                onDineInSelected = { navController.navigate("selects") },
                onTakeawaySelected = { isTakeaway = "TAKEAWAY"; selectedTable = null; navController.navigate("takeaway_menu") },
                sessionManager = sessionManager,
                onQuickBill = { navController.navigate("quick_bills") },
                onNavigateToDue = { navController.navigate("due") },
                navController = navController
            )
        }
        composable("reports") { ReportScreen(sessionManager = sessionManager, drawerState = drawerState) }
        composable("registration") { RegistrationScreen(navController = navController, sessionManager = sessionManager) }
        composable("ai_assistant") { AIAssistantScreen(onBackPressed = { navController.popBackStack() }) }
        composable("language_setting") { LanguageSettingsScreen(navController = navController) }
        composable("role_setting") { RoleSettingsScreen(onBackPressed = { navController.popBackStack() }, navController = navController) }
        composable("printer_setting") { PrinterSettingsScreen(onBackPressed = { navController.popBackStack() }, navController = navController) }
        composable("tax_setting") { TaxSettingsScreen(onBackPressed = { navController.popBackStack() }, navController = navController) }
        composable("tax_split_setting") { TaxSplitSettingsScreen(onBackPressed = { navController.popBackStack() }, navController = navController) }
        composable("restaurant_profile_setting") { RestaurantProfileScreen(onBackPressed = { navController.popBackStack() }, apiService = apiService, sessionManager = sessionManager, navController = navController) }
        composable("general_settings") { GeneralSettingsScreen(onBackPressed = { navController.popBackStack() }, navController = navController) }
        composable("voucher_setting") { VoucherSettingsScreen(onBackPressed = { navController.popBackStack() }, navController = navController) }
        composable("counter_setting") { CounterSettingsScreen(onBackPressed = { navController.popBackStack() }, navController = navController) }
        composable(route = "reset_data") { ResetScreen(onBackPressed = { navController.popBackStack() }, sessionManager = sessionManager, navController = navController) }
        composable("quick_bills") { ItemWiseBillScreen(drawerState = drawerState, navController = navController, onProceedToBilling = { selecteItems = it }, sessionManager = sessionManager) }
        composable("modifier_setting") { ModifierSettingsScreen(onBackPressed = { navController.popBackStack() }, navController = navController) }
        composable("item_wise") { ItemWiseReportScreen(sessionManager = sessionManager, drawerState = drawerState, navController = navController) }
        composable("category_wise") { CategoryWiseReportScreen(sessionManager = sessionManager, drawerState = drawerState, navController = navController) }
        composable("change_company") { ChangeCompanyScreen(onBackPressed = { navController.popBackStack() }, sessionManager = sessionManager, navController = navController) }
        composable("support_screen") { SupportScreen(onBackPressed = { navController.popBackStack() }, drawerState = drawerState, navController = navController) }
        composable("kot_report") {
            KotReportScreen(
                onEditClick = { navController.navigate("kot_modify/${it.order_master_id}"); kotRes = it },
                drawerState = drawerState
            )
        }
        composable("kot_modify/{orderId}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            KotModifyScreen(navController = navController, orderMasterId = orderId, kotResponse = kotRes)
        }
        composable("unit_setting") { UnitSettingsScreen(onBackPressed = { navController.popBackStack() }, navController = navController) }
        composable("kitchen_category_setting") { KitchenCategorySettingsScreen(onBackPressed = { navController.popBackStack() }, navController = navController) }
        composable("voucher_type_setting") { VoucherTypeSettingsScreen(onBackPressed = { navController.popBackStack() }, navController = navController) }
        composable("change_password") { ChangePasswordScreen(onBackPressed = { navController.popBackStack() }, navController = navController) }
        composable("paid_bills") { PaidBillsScreen(navController = navController) }
        composable("bill_edit/{bill_no}") { backStackEntry ->
            val billNo = backStackEntry.arguments?.getString("bill_no") ?: ""
            BillEditScreen(navController = navController, billNo = billNo)
        }
        composable("due") { UnpaidBillsScreen(navController = navController) }
        composable("quick_bill") { QuickBillScreen(navController = navController, orderDetailsResponse = selecteItems) }
        composable("hsn_reports") { HsnReportScreen(drawerState = drawerState) }
        composable("gstR_Docs") { GSTRReportScreen(drawerState = drawerState) }
        composable("group_screen") { GroupScreen(drawerState = drawerState) }
        composable("ledger_screen") { LedgerScreen(drawerState = drawerState) }
        composable("day_entry") { DayEntryScreen(drawerState = drawerState, navController = navController) }
        composable("day_entry_report") { DayEntryReportScreen(drawerState = drawerState) }
        composable("modify_day_entry/{entry_no}") { backStackEntry ->
            val entryNo = backStackEntry.arguments?.getString("entry_no") ?: ""
            DayEntryModifyScreen(drawerState = drawerState, navController = navController, entryNo = entryNo.trim().replace(Regex("[^a-zA-Z0-9_-]"), ""))
        }
        composable("day_book_report") { DayBookReportScreen(drawerState = drawerState) }
        composable("bluetooth") { BluetoothPrinterScreen(sessionManager = sessionManager, navController = navController) }
    }
}

enum class ExpandedMenu {
    NONE, ORDERS, BILLING, MASTERS, REPORTS, GSTREPORTS, ACCOUNTSENTRY, ACCOUNTSREPORT
}

@Composable
fun DrawerContent(
    isCollapsed: Boolean,
    drawerWidth: Dp,
    onCollapseToggle: () -> Unit,
    onDestinationClicked: (String) -> Unit,
    navController: NavHostController,
    sessionManager: SessionManager
) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val role = sessionManager.getUser()?.role ?: ""
    val imageUrl = "${RetrofitClient.currentBaseUrl}logo/getLogo/${sessionManager.getCompanyCode()}"
    val companyName = sessionManager.getRestaurantProfile()?.company_name ?: "Resb"

    val (expandedMenu, setExpandedMenu) = remember { mutableStateOf(ExpandedMenu.NONE) }

    val drawerItemColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = PrimaryGreen.copy(alpha = 0.15f),
        selectedIconColor = SurfaceLight,
        selectedTextColor = SurfaceLight,
        unselectedContainerColor = Color.Transparent,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    val subMenuColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedContainerColor = Color.Transparent,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    ModalDrawerSheet(
        modifier = Modifier.width(drawerWidth),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerTonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Company Logo",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentScale = ContentScale.Crop
                        )
                        if (!isCollapsed) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(companyName, fontWeight = FontWeight.Bold)
                                Text(
                                    "${sessionManager.getUser()?.staff_name ?: ""}-${sessionManager.getUser()?.role ?: ""}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                ModernDivider()
                Spacer(modifier = Modifier.height(8.dp))

                if (role in listOf("RESBADMIN", "ADMIN", "CASHIER")) {
                    NavigationDrawerItem(
                        label = { if (!isCollapsed) Text("Dashboard") },
                        icon = { DrawerIcon(Icons.Default.Dashboard, null, isCollapsed) },
                        selected = currentDestination?.route == "dashboard",
                        onClick = { onDestinationClicked("dashboard") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = drawerItemColors
                    )
                }

                if (role in listOf("RESBADMIN", "ADMIN", "CASHIER")) {
                    NavigationDrawerItem(
                        label = { if (!isCollapsed) Text("Masters") },
                        icon = { DrawerIcon(Icons.Filled.Inventory, null, isCollapsed) },
                        selected = currentDestination?.route in listOf("menu_item_setting", "menu_setting", "menu_Category_setting", "table_setting", "area_setting", "group_screen", "ledger_screen"),
                        onClick = { setExpandedMenu(if (expandedMenu == ExpandedMenu.MASTERS) ExpandedMenu.NONE else ExpandedMenu.MASTERS) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = drawerItemColors
                    )
                    AnimatedVisibility(expandedMenu == ExpandedMenu.MASTERS) {
                        Column(modifier = Modifier.padding(start = if (!isCollapsed) 32.dp else 0.dp)) {
                            NavigationDrawerItem(label = { if (!isCollapsed) Text("Menu Items") }, icon = { DrawerIcon(Icons.Default.Restaurant, null, isCollapsed) }, selected = currentDestination?.route == "menu_item_setting", onClick = { onDestinationClicked("menu_item_setting") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                            NavigationDrawerItem(label = { if (!isCollapsed) Text("Menu Chart") }, icon = { DrawerIcon(Icons.AutoMirrored.Filled.MenuBook, null, isCollapsed) }, selected = currentDestination?.route == "menu_setting", onClick = { onDestinationClicked("menu_setting") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                            NavigationDrawerItem(label = { if (!isCollapsed) Text("Menu Categories") }, icon = { DrawerIcon(Icons.Default.Category, null, isCollapsed) }, selected = currentDestination?.route == "menu_Category_setting", onClick = { onDestinationClicked("menu_Category_setting") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                            if (sessionManager.getGeneralSetting()?.is_table_allowed == true) {
                                NavigationDrawerItem(label = { if (!isCollapsed) Text("Tables") }, icon = { DrawerIcon(Icons.Default.TableRestaurant, null, isCollapsed) }, selected = currentDestination?.route == "table_setting", onClick = { onDestinationClicked("table_setting") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                                NavigationDrawerItem(label = { if (!isCollapsed) Text("Areas") }, icon = { DrawerIcon(Icons.Default.LocationOn, null, isCollapsed) }, selected = currentDestination?.route == "area_setting", onClick = { onDestinationClicked("area_setting") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                            }
                            if (sessionManager.getGeneralSetting()?.is_accounts == true) {
                                NavigationDrawerItem(label = { if (!isCollapsed) Text("Ledger") }, icon = { DrawerIcon(Icons.Default.AccountBalance, null, isCollapsed) }, selected = currentDestination?.route == "ledger_screen", onClick = { onDestinationClicked("ledger_screen") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                                NavigationDrawerItem(label = { if (!isCollapsed) Text("Account Group") }, icon = { DrawerIcon(Icons.Default.Exposure, null, isCollapsed) }, selected = currentDestination?.route == "group_screen", onClick = { onDestinationClicked("group_screen") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                            }
                        }
                    }
                }

                if (role in listOf("RESBADMIN", "ADMIN", "WAITER", "CASHIER")) {
                    NavigationDrawerItem(
                        label = { if (!isCollapsed) Text("Orders") },
                        icon = { DrawerIcon(Icons.Default.Receipt, null, isCollapsed) },
                        selected = currentDestination?.route in listOf("selects", "takeaway_menu"),
                        onClick = { setExpandedMenu(if (expandedMenu == ExpandedMenu.ORDERS) ExpandedMenu.NONE else ExpandedMenu.ORDERS) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = drawerItemColors
                    )
                    AnimatedVisibility(expandedMenu == ExpandedMenu.ORDERS) {
                        Column(modifier = Modifier.padding(start = if (!isCollapsed) 32.dp else 0.dp)) {
                            if (sessionManager.getGeneralSetting()?.is_table_allowed == true) {
                                NavigationDrawerItem(label = { if (!isCollapsed) Text("Dine In") }, icon = { DrawerIcon(Icons.Default.Restaurant, null, isCollapsed) }, selected = currentDestination?.route == "selects", onClick = { onDestinationClicked("selects") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                            }
                            NavigationDrawerItem(label = { if (!isCollapsed) Text("Takeaway") }, icon = { DrawerIcon(Icons.Default.Fastfood, null, isCollapsed) }, selected = currentDestination?.route == "takeaway_menu", onClick = { onDestinationClicked("takeaway_menu") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                            NavigationDrawerItem(label = { if (!isCollapsed) Text("Delivery") }, icon = { DrawerIcon(Icons.Default.DeliveryDining, null, isCollapsed) }, selected = currentDestination?.route == "delivery_menu", onClick = { onDestinationClicked("delivery_menu") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                        }
                    }
                }

                if (role in listOf("RESBADMIN", "ADMIN", "CASHIER")) {
                    if (sessionManager.getGeneralSetting()?.is_accounts == true) {
                        NavigationDrawerItem(
                            label = { if (!isCollapsed) Text("Accounts Entry") },
                            icon = { DrawerIcon(Icons.Default.AccountBox, null, isCollapsed) },
                            selected = currentDestination?.route == "day_entry",
                            onClick = { setExpandedMenu(if (expandedMenu == ExpandedMenu.ACCOUNTSENTRY) ExpandedMenu.NONE else ExpandedMenu.ACCOUNTSENTRY) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = drawerItemColors
                        )
                        AnimatedVisibility(expandedMenu == ExpandedMenu.ACCOUNTSENTRY) {
                            Column(modifier = Modifier.padding(start = if (!isCollapsed) 32.dp else 0.dp)) {
                                NavigationDrawerItem(label = { if (!isCollapsed) Text("Day Entry") }, icon = { DrawerIcon(Icons.Default.AccountCircle, null, isCollapsed) }, selected = currentDestination?.route == "day_entry", onClick = { onDestinationClicked("day_entry") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                            }
                        }
                    }

                    NavigationDrawerItem(
                        label = { if (!isCollapsed) Text("Bills") },
                        icon = { DrawerIcon(Icons.Default.AttachMoney, null, isCollapsed) },
                        selected = currentDestination?.route in listOf("counter", "quick_bills"),
                        onClick = { setExpandedMenu(if (expandedMenu == ExpandedMenu.BILLING) ExpandedMenu.NONE else ExpandedMenu.BILLING) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = drawerItemColors
                    )
                    AnimatedVisibility(expandedMenu == ExpandedMenu.BILLING) {
                        Column(modifier = Modifier.padding(start = if (!isCollapsed) 32.dp else 0.dp)) {
                            NavigationDrawerItem(label = { if (!isCollapsed) Text("Counter Billing") }, icon = { DrawerIcon(Icons.Default.PointOfSale, null, isCollapsed) }, selected = currentDestination?.route == "counter", onClick = { onDestinationClicked("counter") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                            NavigationDrawerItem(label = { if (!isCollapsed) Text("Quick Bills") }, icon = { DrawerIcon(Icons.Default.Bolt, null, isCollapsed) }, selected = currentDestination?.route == "quick_bills", onClick = { onDestinationClicked("quick_bills") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                        }
                    }
                }

                if (role in listOf("RESBADMIN", "ADMIN", "CASHIER")) {
                    NavigationDrawerItem(
                        label = { if (!isCollapsed) Text("Reports") },
                        icon = { DrawerIcon(Icons.Default.Assessment, null, isCollapsed) },
                        selected = currentDestination?.route in listOf("reports", "orders", "item_wise", "category_wise", "due", "kot_report", "paid_bills"),
                        onClick = { setExpandedMenu(if (expandedMenu == ExpandedMenu.REPORTS) ExpandedMenu.NONE else ExpandedMenu.REPORTS) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = drawerItemColors
                    )
                    AnimatedVisibility(expandedMenu == ExpandedMenu.REPORTS) {
                        Column(modifier = Modifier.padding(start = if (!isCollapsed) 32.dp else 0.dp)) {
                            NavigationDrawerItem(label = { if (!isCollapsed) Text("Orders") }, icon = { DrawerIcon(Icons.Default.Receipt, null, isCollapsed) }, selected = currentDestination?.route == "orders", onClick = { onDestinationClicked("orders") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                            NavigationDrawerItem(label = { if (!isCollapsed) Text("Paid Bills") }, icon = { DrawerIcon(Icons.AutoMirrored.Filled.ListAlt, null, isCollapsed) }, selected = currentDestination?.route == "paid_bills", onClick = { onDestinationClicked("paid_bills") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                            NavigationDrawerItem(label = { if (!isCollapsed) Text("Due") }, icon = { Icon(Icons.Filled.Payment, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(if (isCollapsed) 50.dp else 24.dp)) }, selected = currentDestination?.route == "due", onClick = { onDestinationClicked("due") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                            NavigationDrawerItem(label = { if (!isCollapsed) Text("Sales") }, icon = { DrawerIcon(Icons.Default.Assessment, null, isCollapsed) }, selected = currentDestination?.route == "reports", onClick = { onDestinationClicked("reports") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                            NavigationDrawerItem(label = { if (!isCollapsed) Text("Item Wise") }, icon = { DrawerIcon(Icons.Default.Restaurant, null, isCollapsed) }, selected = currentDestination?.route == "item_wise", onClick = { onDestinationClicked("item_wise") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                            NavigationDrawerItem(label = { if (!isCollapsed) Text("Category Wise") }, icon = { DrawerIcon(Icons.Default.Category, null, isCollapsed) }, selected = currentDestination?.route == "category_wise", onClick = { onDestinationClicked("category_wise") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                            NavigationDrawerItem(label = { if (!isCollapsed) Text("KOT Report") }, icon = { DrawerIcon(Icons.Default.Kitchen, null, isCollapsed) }, selected = currentDestination?.route == "kot_report", onClick = { onDestinationClicked("kot_report") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                        }
                    }

                    NavigationDrawerItem(
                        label = { if (!isCollapsed) Text("GST Reports") },
                        icon = { DrawerIcon(Icons.AutoMirrored.Filled.ReceiptLong, null, isCollapsed) },
                        selected = currentDestination?.route in listOf("hsn_reports", "gstR_Docs"),
                        onClick = { setExpandedMenu(if (expandedMenu == ExpandedMenu.GSTREPORTS) ExpandedMenu.NONE else ExpandedMenu.GSTREPORTS) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = drawerItemColors
                    )
                    AnimatedVisibility(expandedMenu == ExpandedMenu.GSTREPORTS) {
                        Column(modifier = Modifier.padding(start = if (!isCollapsed) 32.dp else 0.dp)) {
                            NavigationDrawerItem(label = { if (!isCollapsed) Text("HSN Reports") }, icon = { DrawerIcon(Icons.AutoMirrored.Filled.ListAlt, null, isCollapsed) }, selected = currentDestination?.route == "hsn_reports", onClick = { onDestinationClicked("hsn_reports") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                            NavigationDrawerItem(label = { if (!isCollapsed) Text("GSTR-Docs") }, icon = { DrawerIcon(Icons.AutoMirrored.Filled.ReceiptLong, null, isCollapsed) }, selected = currentDestination?.route == "gstR_Docs", onClick = { onDestinationClicked("gstR_Docs") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                        }
                    }

                    if (sessionManager.getGeneralSetting()?.is_accounts == true) {
                        NavigationDrawerItem(
                            label = { if (!isCollapsed) Text("Accounts Reports") },
                            icon = { DrawerIcon(Icons.AutoMirrored.Filled.ReceiptLong, null, isCollapsed) },
                            selected = currentDestination?.route in listOf("day_entry_report", "day_book_report"),
                            onClick = { setExpandedMenu(if (expandedMenu == ExpandedMenu.ACCOUNTSREPORT) ExpandedMenu.NONE else ExpandedMenu.ACCOUNTSREPORT) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = drawerItemColors
                        )
                        AnimatedVisibility(expandedMenu == ExpandedMenu.ACCOUNTSREPORT) {
                            Column(modifier = Modifier.padding(start = if (!isCollapsed) 32.dp else 0.dp)) {
                                NavigationDrawerItem(label = { if (!isCollapsed) Text("Ledger Report") }, icon = { DrawerIcon(Icons.AutoMirrored.Filled.ListAlt, null, isCollapsed) }, selected = currentDestination?.route == "day_entry_report", onClick = { onDestinationClicked("day_entry_report") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                                NavigationDrawerItem(label = { if (!isCollapsed) Text("Day Book Report") }, icon = { DrawerIcon(Icons.AutoMirrored.Filled.ReceiptLong, null, isCollapsed) }, selected = currentDestination?.route == "day_book_report", onClick = { onDestinationClicked("day_book_report") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding), colors = subMenuColors)
                            }
                        }
                    }
                }

                if (role in listOf("RESBADMIN", "ADMIN")) {
                    NavigationDrawerItem(
                        label = { if (!isCollapsed) Text("Settings") },
                        icon = { DrawerIcon(Icons.Default.Settings, null, isCollapsed) },
                        selected = currentDestination?.route == "settings",
                        onClick = { onDestinationClicked("settings") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = drawerItemColors
                    )
                }

                if (role in listOf("RESBADMIN", "ADMIN", "WAITER", "CASHIER", "CHEF")) {
                    NavigationDrawerItem(
                        label = { if (!isCollapsed) Text("Support") },
                        icon = { DrawerIcon(Icons.Default.SupportAgent, null, isCollapsed) },
                        selected = currentDestination?.route == "support_screen",
                        onClick = { onDestinationClicked("support_screen") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = drawerItemColors
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { if (!isCollapsed) Text("Collapse Drawer") },
                    icon = { DrawerIcon(if (isCollapsed) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Toggle Collapse", isCollapsed) },
                    selected = false,
                    onClick = onCollapseToggle,
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = drawerItemColors
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            ModernDivider()
            if (role in listOf("RESBADMIN", "ADMIN", "WAITER", "CHEF", "CASHIER")) {
                NavigationDrawerItem(
                    label = { if (!isCollapsed) Text("Logout") },
                    icon = { DrawerIcon(Icons.AutoMirrored.Filled.Logout, null, isCollapsed) },
                    selected = false,
                    onClick = { onDestinationClicked("logout") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = drawerItemColors
                )
            }
        }
    }
}

@Composable
fun DrawerIcon(icon: ImageVector, contentDescription: String?, isCollapsed: Boolean) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = Modifier.size(if (isCollapsed) 50.dp else 24.dp)
    )
}
