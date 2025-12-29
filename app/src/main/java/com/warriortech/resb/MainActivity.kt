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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Surface
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import com.warriortech.resb.ui.theme.ResbTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.warriortech.resb.data.sync.SyncManager
import com.warriortech.resb.model.Table
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.screens.LoginScreen
import com.warriortech.resb.screens.MenuScreen
import com.warriortech.resb.screens.SelectionScreen
import com.warriortech.resb.util.ConnectionState
import com.warriortech.resb.util.NetworkMonitor
import com.warriortech.resb.util.NetworkStatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.content.edit
import coil.compose.AsyncImage
import com.warriortech.resb.model.KotResponse
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.model.TblOrderDetailsResponse
import com.warriortech.resb.network.RetrofitClient
import com.warriortech.resb.network.RetrofitClient.apiService
import com.warriortech.resb.screens.BillingScreen
import com.warriortech.resb.screens.PaymentScreen
import com.warriortech.resb.screens.reports.OrderScreen
import com.warriortech.resb.screens.SettingsScreen
import com.warriortech.resb.screens.SupportScreen
import com.warriortech.resb.screens.DashboardScreen
import com.warriortech.resb.screens.CounterScreen
import com.warriortech.resb.screens.KitchenScreen
import com.warriortech.resb.screens.reports.ReportScreen
import com.warriortech.resb.screens.RegistrationScreen
import com.warriortech.resb.screens.AIAssistantScreen
import com.warriortech.resb.screens.BillEditScreen
import com.warriortech.resb.screens.BillTemplateScreen
import com.warriortech.resb.screens.reports.CategoryWiseReportScreen
import com.warriortech.resb.screens.CounterSelectionScreen
import com.warriortech.resb.screens.accounts.master.GroupScreen
import com.warriortech.resb.screens.settings.AreaSettingsScreen
import com.warriortech.resb.screens.settings.CounterSettingsScreen
import com.warriortech.resb.screens.settings.CustomerSettingsScreen
import com.warriortech.resb.screens.settings.GeneralSettingsScreen
import com.warriortech.resb.screens.settings.LanguageSettingsScreen
import com.warriortech.resb.screens.settings.MenuCategorySettingsScreen
import com.warriortech.resb.screens.settings.MenuItemSettingsScreen
import com.warriortech.resb.screens.settings.MenuSettingsScreen
import com.warriortech.resb.screens.settings.ModifierSettingsScreen
import com.warriortech.resb.screens.settings.PrinterSettingsScreen
import com.warriortech.resb.screens.settings.RestaurantProfileScreen
import com.warriortech.resb.screens.settings.RoleSettingsScreen
import com.warriortech.resb.screens.settings.StaffSettingsScreen
import com.warriortech.resb.screens.settings.TableSettingsScreen
import com.warriortech.resb.screens.settings.TaxSettingsScreen
import com.warriortech.resb.screens.settings.TaxSplitSettingsScreen
import com.warriortech.resb.screens.settings.VoucherSettingsScreen
import com.warriortech.resb.screens.TemplateScreen
import com.warriortech.resb.screens.TemplateEditorScreen
import com.warriortech.resb.screens.TemplatePreviewScreen
import com.warriortech.resb.util.LocaleHelper
import com.warriortech.resb.screens.ItemWiseBillScreen
import com.warriortech.resb.screens.reports.ItemWiseReportScreen
import com.warriortech.resb.screens.KotModifyScreen
import com.warriortech.resb.screens.accounts.master.LedgerScreen
import com.warriortech.resb.screens.reports.KotReportScreen
import com.warriortech.resb.screens.reports.PaidBillsScreen
import com.warriortech.resb.screens.QuickBillScreen
import com.warriortech.resb.screens.SplashScreen
import com.warriortech.resb.screens.accounts.reports.DayBookReportScreen
import com.warriortech.resb.screens.accounts.reports.DayEntryReportScreen
import com.warriortech.resb.screens.accounts.transaction.DayEntryModifyScreen
import com.warriortech.resb.screens.accounts.transaction.DayEntryScreen
import com.warriortech.resb.screens.reports.UnpaidBillsScreen
import com.warriortech.resb.screens.reports.gst.GSTRReportScreen
import com.warriortech.resb.screens.reports.gst.HsnReportScreen
import com.warriortech.resb.screens.settings.ChangePasswordScreen
import com.warriortech.resb.screens.settings.ResetScreen
import com.warriortech.resb.ui.components.ModernDivider
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.util.SubscriptionManager
import com.warriortech.resb.screens.settings.ChangeCompanyScreen
import com.warriortech.resb.screens.settings.KitchenCategorySettingsScreen
import com.warriortech.resb.screens.settings.UnitSettingsScreen
import com.warriortech.resb.screens.settings.VoucherTypeSettingsScreen
import com.warriortech.resb.util.BluetoothPrinterScreen

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

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var syncManager: SyncManager

    @SuppressLint("ConfigurationScreenWidthHeight")
    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        // Enable edge-to-edge display for proper system bar handling
//        enableEdgeToEdge()
//
//        // Configure window for edge-to-edge display (important for Redmi/MIUI devices)
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//
        val sessionManager = SessionManager(this)
        // Initialize sync when app starts
        lifecycleScope.launch {
            networkMonitor.isOnline.collect { connectionState ->
                if (connectionState == ConnectionState.Available) {
                    syncManager.scheduleSyncWork()
                }
            }
        }
        // Set the content view with the main theme
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


                // Responsive drawer width based on screen size
                val drawerWidth = when {
                    isTablet -> if (isCollapsed.value) 80.dp else 320.dp
                    isLargeScreen -> if (isCollapsed.value) 72.dp else 280.dp
                    isLandscape -> if (isCollapsed.value) 72.dp else (screenWidth * 0.6f).dp.coerceAtMost(
                        300.dp
                    )

                    else -> if (isCollapsed.value) 72.dp else (screenWidth * 0.8f).dp.coerceAtMost(
                        300.dp
                    )
                }
                val animatedDrawerWidth by animateDpAsState(targetValue = drawerWidth)

                val currentRoute =
                    navController.currentBackStackEntryAsState().value?.destination?.route
                val showDrawer = currentRoute != "login"
                // Drawer content composable
                val drawerContent = @Composable {
                    DrawerContent(
                        isCollapsed = isCollapsed.value,
                        drawerWidth = animatedDrawerWidth,
                        onCollapseToggle = { isCollapsed.value = !isCollapsed.value },
                        onDestinationClicked = { route ->
                            scope.launch { drawerState.close() }
                            if (route == "logout") {
                                scope.launch {
                                    val sharedPref =
                                        context.getSharedPreferences("user_prefs", MODE_PRIVATE)
                                    sessionManager.saveUserLogin(false)
                                    sessionManager.clearBluetoothPrinter()
                                    sharedPref.edit { clear() }
                                    Toast.makeText(
                                        context,
                                        "Logged out successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true } // completely clears stack
                                        launchSingleTop = true
                                    }


                                    // ✅ Finish activity to prevent going back
//                                    (context as? ComponentActivity)?.finish()
                                }
                            } else {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
//                                (context as? ComponentActivity)?.finish()
                            }
                        },
                        navController = navController,
                        sessionManager = sessionManager
                    )
                }

                if (showDrawer) {
                    when {
                        isLargeScreen -> {
                            ModalNavigationDrawer(
                                drawerState = drawerState,
                                drawerContent = drawerContent,
                                gesturesEnabled = true
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    elevation = 2.dp,
                                    shape = MaterialTheme.shapes.large,
                                    color = MaterialTheme.colorScheme.background
                                ) {
                                    Column {
                                        NetworkStatusBar(connectionState = connectionState)
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppNavigation(
                                                drawerState,
                                                navController,
                                                sessionManager
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            ModalNavigationDrawer(
                                drawerState = drawerState,
                                drawerContent = drawerContent,
                                gesturesEnabled = true
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    elevation = 2.dp,
                                    shape = MaterialTheme.shapes.large,
                                    color = MaterialTheme.colorScheme.background
                                ) {
                                    Column {
                                        NetworkStatusBar(connectionState = connectionState)
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppNavigation(
                                                drawerState,
                                                navController,
                                                sessionManager
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // No drawer, just show the main content
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        elevation = 2.dp,
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column {
                            NetworkStatusBar(connectionState = connectionState)
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                AppNavigation(drawerState, navController, sessionManager)
                            }
                        }
                    }
                }
            }
        }
    }
}

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

    // Check subscription status
    LaunchedEffect(Unit) {
        val subscriptionManager = SubscriptionManager(sessionManager)
        if (subscriptionManager.isSubscriptionExpired()) {
            // Clear all user data and navigate to login
            sessionManager.clearSession()
            navController.navigate("login") {
                popUpTo(0) // Clear entire back stack
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
                            navController.navigate("selects") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            navController.navigate("dashboard") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    } else {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )

        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    if (sessionManager.getUser()?.role == "WAITER") {
                        navController.navigate("selects") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("dashboard") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                },
                onRegisterClick = {
                    navController.navigate("registration")
                },
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

        composable("menu")  @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT) {
            val tableId = selectedTable?.table_id ?: 1L
            val tableStatId = selectedTable != null || isTakeaway == "TABLE"
            val tableNumber = selectedTable?.table_name ?: ""

            MenuScreen(
                isTakeaway = isTakeaway,
                tableStatId = tableStatId,
                tableId = tableId,
                onBackPressed = { navController.popBackStack() },
                onOrderPlaced = { navController.popBackStack() },
                drawerState = drawerState,
                onBillPlaced = { items, orderId ->
                    selectedItems = items
                    navController.navigate("billing_screen/${orderId}")
                },
                navController = navController,
                sessionManager = sessionManager,
                tableName = tableNumber
            )
        }

        composable("takeaway_menu") {
            MenuScreen(
                isTakeaway = "TAKEAWAY",
                tableStatId = false,
                tableId = 1L,
                onBackPressed = { navController.popBackStack() },
                onOrderPlaced = {
                    navController.popBackStack()
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
        composable("billing_screen/{orderMasterId}") { backStackEntry ->
            BillingScreen(
                navController = navController,
                orderDetailsResponse = selectedItems,
                orderMasterId = backStackEntry.arguments?.getString("orderMasterId")
            )
        }

        composable("payment_screen/{amountToPayFromRoute}/{orderId}/{bill_no}/{customerId}/{voucherType}") { it ->
            PaymentScreen(
                navController = navController,
                amountToPayFromRoute = it.arguments?.getString("amountToPayFromRoute")
                    ?.toDoubleOrNull() ?: 0.0,
                orderMasterId = it.arguments?.getString("orderId") ?: "",
                sessionManager = sessionManager,
                billNo = it.arguments?.getString("bill_no") ?: "",
                customerId = it.arguments?.getString("customerId")?.toLongOrNull() ?: 0L,
                voucherType = it.arguments?.getString("voucherType") ?: ""
            )
        }

        composable("report_screen") {
            ReportScreen(
                sessionManager = sessionManager,
                drawerState = drawerState
            )
        }

        composable("kitchen") {
            KitchenScreen(
                navController = navController,
                drawerState = drawerState
            )
        }

        composable("orders") {
            OrderScreen(
                drawerState = drawerState,
                onNavigateToBilling = { items, orderId ->
                    selectedItems = items
                    navController.navigate("billing_screen/${orderId}") {
                        popUpTo("orders") { inclusive = true }
                    }
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

        composable("area_setting") {
            AreaSettingsScreen(
                onBackPressed = { navController.popBackStack() },
                drawerState = drawerState
            )
        }

        composable("table_setting") {
            TableSettingsScreen(onBackPressed = {
                navController.popBackStack()
            },
                drawerState = drawerState)
        }

        composable("menu_setting") {
            MenuSettingsScreen(
                onBackPressed = {
                    navController.popBackStack()
                },
                drawerState = drawerState
            )
        }

        composable("menu_item_setting") {
            MenuItemSettingsScreen(
                onBackPressed = {
                    navController.popBackStack()
                },
                sessionManager = sessionManager,
                drawerState = drawerState
            )
        }

        composable("menu_Category_setting") {
            MenuCategorySettingsScreen(
                onBackPressed = {
                    navController.popBackStack()
                },
                drawerState = drawerState
            )
        }

        composable("staff_setting") {
            StaffSettingsScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }

        composable("customer_setting") {
            CustomerSettingsScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }

        composable("counter_selection") {
            CounterSelectionScreen(
                onCounterSelected = { counter ->
                    navController.navigate("counter/${counter.id}") {
                        popUpTo("counter_selection") { inclusive = true }
                    }
                },
                drawerState = drawerState
            )
        }

        composable("counter/{counterId}") { backStackEntry ->
            val counterId = backStackEntry.arguments?.getString("counterId")?.toLongOrNull() ?: 1L
            CounterScreen(
                onBackPressed = { navController.popBackStack() },
                onProceedToBilling = { items, orderId ->
                    selectedItems = items
                    navController.navigate("billing_screen/${orderId}")
                },
                drawerState = drawerState,
                counterId = counterId
            )
        }

        composable("counter") {
            CounterScreen(
                onBackPressed = { navController.popBackStack() },
                onProceedToBilling = { items, orderId ->
                    selectedItems = items
                    navController.navigate("billing_screen/${orderId}")
                },
                drawerState = drawerState
            )
        }

        composable("dashboard") {
            DashboardScreen(
                drawerState = drawerState,
                onNavigateToOrders = { navController.navigate("orders") },
                onNavigateToBilling = { navController.navigate("counter") },
                onDineInSelected = { navController.navigate("selects") },
                onTakeawaySelected = {
                    isTakeaway = "TAKEAWAY"
                    selectedTable = null
                    navController.navigate("takeaway_menu")
                },
                sessionManager = sessionManager,
                onQuickBill = {
                    navController.navigate("quick_bills")
                },
                onNavigateToDue = {
                    navController.navigate("due")
                },
            )
        }

        composable("report_screen") {
            ReportScreen(
                sessionManager = sessionManager,
                drawerState = drawerState
            )
        }
        composable("reports") {
            ReportScreen(
                sessionManager = sessionManager,
                drawerState = drawerState
            )
        }
        composable("registration") {
            RegistrationScreen(
                navController = navController,
                sessionManager = sessionManager
            )
        }

        composable("ai_assistant") {
            AIAssistantScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }

        composable("template_screen") {
            TemplateScreen(navController = navController)
        }

        composable("template_editor/{templateId}") {
            val templateId = it.arguments?.getString("templateId") ?: ""
            TemplateEditorScreen(navController = navController, templateId = templateId)
        }

        composable("template_preview/{templateId}") { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId") ?: ""
            TemplatePreviewScreen(
                navController = navController,
                templateId = templateId
            )
        }

        composable("language_setting") {
            LanguageSettingsScreen(navController = navController)
        }

        composable("role_setting") {
            RoleSettingsScreen(onBackPressed = { navController.popBackStack() })
        }

        composable("printer_setting") {
            PrinterSettingsScreen(
                onBackPressed = { navController.popBackStack() },
                navController = navController
            )
        }

        composable("tax_setting") {
            TaxSettingsScreen(onBackPressed = { navController.popBackStack() })
        }

        composable("tax_split_setting") {
            TaxSplitSettingsScreen(onBackPressed = { navController.popBackStack() })
        }

        composable("restaurant_profile_setting") {
            RestaurantProfileScreen(
                onBackPressed = { navController.popBackStack() },
                apiService = apiService,
                sessionManager = sessionManager
            )
        }

        composable("general_settings") {
            GeneralSettingsScreen(
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        composable("voucher_setting") {
            VoucherSettingsScreen(onBackPressed = { navController.popBackStack() })
        }

        composable("counter_setting") {
            CounterSettingsScreen(onBackPressed = { navController.popBackStack() })
        }

        composable(
            route = "bill_template/{billId}"
        ) { backStackEntry ->
            val billId = backStackEntry.arguments?.getLong("billId") ?: 0L
            BillTemplateScreen(navController, billId)
        }

        composable("change_password") {
            ChangePasswordScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable(route = "reset_data") {
            ResetScreen(
                onBackPressed = { navController.popBackStack() },
                sessionManager = sessionManager,
                navController = navController
            )
        }
        composable("quick_bills")  {
            ItemWiseBillScreen(
                drawerState = drawerState,
                navController = navController,
                onProceedToBilling = {
                    selecteItems = it
                }
            )
        }
        composable("modifier_setting") {
            ModifierSettingsScreen(onBackPressed = { navController.popBackStack() })
        }
        composable("item_wise") {
            ItemWiseReportScreen(
                sessionManager = sessionManager,
                drawerState = drawerState
            )
        }
        composable("category_wise") {
            CategoryWiseReportScreen(
                sessionManager = sessionManager,
                drawerState = drawerState
            )
        }

        composable("change_company") {
            ChangeCompanyScreen(
                onBackPressed = { navController.popBackStack() },
                sessionManager = sessionManager,
                navController = navController
            )
        }

        composable("support_screen") {
            SupportScreen(
                onBackPressed = { navController.popBackStack() },
                drawerState = drawerState
            )
        }

        composable("kot_report") {
            KotReportScreen(
                onEditClick = {
                    navController.navigate("kot_modify/${it.order_master_id}")
                    kotRes = it
                },
                drawerState = drawerState
            )
        }
        composable("kot_modify/{orderId}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            KotModifyScreen(
                navController = navController,
                orderMasterId = orderId,
                kotResponse = kotRes
            )
        }
        composable("modifier_setting") {
            ModifierSettingsScreen(onBackPressed = { navController.popBackStack() })
        }
        composable("unit_setting") {
            UnitSettingsScreen(onBackPressed = { navController.popBackStack() })
        }
        composable("kitchen_category_setting") {
            KitchenCategorySettingsScreen(onBackPressed = { navController.popBackStack() })
        }
        composable("voucher_type_setting") {
            VoucherTypeSettingsScreen(onBackPressed = { navController.popBackStack() })
        }
        composable("change_password") {
            ChangePasswordScreen(onBackPressed = { navController.popBackStack() })
        }
        composable("paid_bills") {
            PaidBillsScreen(
                navController = navController,
            )
        }
        composable("bill_edit/{bill_no}") { backStackEntry ->
            val bill_no = backStackEntry.arguments?.getString("bill_no") ?: ""
            BillEditScreen(
                navController = navController,
                billNo = bill_no
            )
        }
        composable("due") {
            UnpaidBillsScreen(
                navController = navController,
            )
        }
        composable("quick_bill") {
            QuickBillScreen(
                navController = navController,
                orderDetailsResponse = selecteItems
            )
        }
        composable("hsn_reports") {
            HsnReportScreen(
                drawerState = drawerState
            )
        }
        composable("gstR_Docs") {
            GSTRReportScreen(
                drawerState = drawerState
            )
        }
        composable("group_screen") {
            GroupScreen(
                drawerState = drawerState,
            )
        }
        composable("ledger_screen") {
            LedgerScreen(
                drawerState = drawerState
            )
        }
        composable("day_entry") {
            DayEntryScreen(
                drawerState = drawerState,
                navController = navController
            )
        }
        composable("day_entry_report") {
            DayEntryReportScreen(
                drawerState = drawerState
            )
        }
        composable("modify_day_entry/{entry_no}") { backStackEntry ->
            val entry_no = backStackEntry.arguments?.getString("entry_no") ?: ""
            DayEntryModifyScreen(
                drawerState = drawerState,
                navController = navController,
                entryNo = entry_no.trim().replace(Regex("[^a-zA-Z0-9_-]"), "")
            )
        }

        composable("day_book_report") {
            DayBookReportScreen(
                drawerState = drawerState
            )
        }

        composable("bluetooth") {
            BluetoothPrinterScreen(
                sessionManager = sessionManager,
                navController = navController
            )
        }
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
    val imageUrl = "${RetrofitClient.BASE_URL}logo/getLogo/${sessionManager.getCompanyCode()}"
    val companyName = sessionManager.getRestaurantProfile()?.company_name ?: "Resb"
    val setting = sessionManager.getGeneralSetting()

    // 👇 Single state instead of 4 booleans
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
                    .weight(1f) // fills available space
                    .verticalScroll(rememberScrollState())
            ) {

                // 🔹 Profile Section
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.medium,
                    elevation = 1.dp
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

                                Text(
                                    companyName,
                                    fontWeight = FontWeight.Bold
                                )
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

                // 🔹 Dashboard
                if (role in listOf("RESBADMIN", "ADMIN", "CASHIER")) {
                    NavigationDrawerItem(
                        label = { if (!isCollapsed) Text("Dashboard") else Text("") },
                        icon = { DrawerIcon(Icons.Default.Dashboard, null, isCollapsed) },
                        selected = currentDestination?.route == "dashboard",
                        onClick = { onDestinationClicked("dashboard") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = drawerItemColors
                    )
                }

                // 🔹 Masters
                if (role in listOf("RESBADMIN", "ADMIN", "CASHIER")) {
                    NavigationDrawerItem(
                        label = { if (!isCollapsed) Text("Masters") else Text("") },
                        icon = {
                            DrawerIcon(
                                Icons.Filled.Inventory,
                                contentDescription = null,
                                isCollapsed
                            )
                        },
                        selected = currentDestination?.route in listOf(
                            "menu_item_setting",
                            "menu_setting",
                            "menu_Category_setting",
                            "table_setting",
                            "area_setting",
                            "group_screen",
                            "ledger_screen"
                        ),
                        onClick = {
                            setExpandedMenu(if (expandedMenu == ExpandedMenu.MASTERS) ExpandedMenu.NONE else ExpandedMenu.MASTERS)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = drawerItemColors
                    )
                    AnimatedVisibility(expandedMenu == ExpandedMenu.MASTERS) {
                        Column(modifier = Modifier.padding(start = if (!isCollapsed) 32.dp else 0.dp)) {
                            NavigationDrawerItem(
                                label = { if (!isCollapsed) Text("Menu Items") else Text("") },
                                icon = {
                                    DrawerIcon(
                                        Icons.Default.Restaurant,
                                        contentDescription = null,
                                        isCollapsed
                                    )
                                },
                                selected = currentDestination?.route == "menu_item_setting",
                                onClick = { onDestinationClicked("menu_item_setting") },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = subMenuColors
                            )
                            NavigationDrawerItem(
                                label = { if (!isCollapsed) Text("Menu Chart") else Text("") },
                                icon = {
                                    DrawerIcon(
                                        Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        isCollapsed
                                    )
                                },
                                selected = currentDestination?.route == "menu_setting",
                                onClick = { onDestinationClicked("menu_setting") },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = subMenuColors
                            )
                            NavigationDrawerItem(
                                label = { if (!isCollapsed) Text("Menu Categories") else Text("") },
                                icon = {
                                    DrawerIcon(
                                        Icons.Default.Category,
                                        contentDescription = null,
                                        isCollapsed
                                    )
                                },
                                selected = currentDestination?.route == "menu_Category_setting",
                                onClick = { onDestinationClicked("menu_Category_setting") },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = subMenuColors
                            )
                            if (sessionManager.getGeneralSetting()?.is_table_allowed == true) {
                                NavigationDrawerItem(
                                    label = { if (!isCollapsed) Text("Tables") else Text("") },
                                    icon = {
                                        DrawerIcon(
                                            Icons.Default.TableRestaurant,
                                            contentDescription = null,
                                            isCollapsed
                                        )
                                    },
                                    selected = currentDestination?.route == "table_setting",
                                    onClick = { onDestinationClicked("table_setting") },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                    colors = subMenuColors
                                )
                                NavigationDrawerItem(
                                    label = { if (!isCollapsed) Text("Areas") else Text("") },
                                    icon = {
                                        DrawerIcon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            isCollapsed
                                        )
                                    },
                                    selected = currentDestination?.route == "area_setting",
                                    onClick = { onDestinationClicked("area_setting") },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                    colors = subMenuColors
                                )
                            }
                                if (sessionManager.getGeneralSetting()?.is_accounts == true) {
                                    NavigationDrawerItem(
                                        label = { if (!isCollapsed) Text("Ledger") else Text("") },
                                        icon = {
                                            DrawerIcon(
                                                Icons.Default.AccountBalance,
                                                contentDescription = null,
                                                isCollapsed
                                            )
                                        },
                                        selected = currentDestination?.route == "ledger_screen",
                                        onClick = { onDestinationClicked("ledger_screen") },
                                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                        colors = subMenuColors
                                    )

                                    NavigationDrawerItem(
                                        label = { if (!isCollapsed) Text("Account Group") else Text("") },
                                        icon = {
                                            DrawerIcon(
                                                Icons.Default.Exposure,
                                                contentDescription = null,
                                                isCollapsed
                                            )
                                        },
                                        selected = currentDestination?.route == "group_screen",
                                        onClick = { onDestinationClicked("group_screen") },
                                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                        colors = subMenuColors
                                    )
                                }

                        }
                    }
                }

                // 🔹 Orders

                    if (role in listOf("RESBADMIN", "ADMIN", "WAITER", "CASHIER")) {
                        NavigationDrawerItem(
                            label = { if (!isCollapsed) Text("Orders") else Text("") },
                            icon = {
                                DrawerIcon(
                                    Icons.Default.Receipt,
                                    contentDescription = null,
                                    isCollapsed
                                )
                            },
                            selected = currentDestination?.route in listOf(
                                "selects",
                                "takeaway_menu"
                            ),
                            onClick = {
                                setExpandedMenu(if (expandedMenu == ExpandedMenu.ORDERS) ExpandedMenu.NONE else ExpandedMenu.ORDERS)
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = drawerItemColors
                        )
                        AnimatedVisibility(expandedMenu == ExpandedMenu.ORDERS) {
                            Column(modifier = Modifier.padding(start = if (!isCollapsed) 32.dp else 0.dp)) {
                                if (sessionManager.getGeneralSetting()?.is_table_allowed == true) {
                                    NavigationDrawerItem(
                                        label = { if (!isCollapsed) Text("Dine In") else Text("") },
                                        icon = {
                                            DrawerIcon(
                                                Icons.Default.Restaurant,
                                                contentDescription = null,
                                                isCollapsed
                                            )
                                        },
                                        selected = currentDestination?.route == "selects",
                                        onClick = { onDestinationClicked("selects") },
                                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                        colors = subMenuColors
                                    )
                                }
                                NavigationDrawerItem(
                                    label = { if (!isCollapsed) Text("Takeaway") else Text("") },
                                    icon = {
                                        DrawerIcon(
                                            Icons.Default.Fastfood,
                                            contentDescription = null,
                                            isCollapsed
                                        )
                                    },
                                    selected = currentDestination?.route == "takeaway_menu",
                                    onClick = { onDestinationClicked("takeaway_menu") },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                    colors = subMenuColors
                                )

                            }
                        }
                    }



                // 🔹 Billing

                if (role in listOf("RESBADMIN", "ADMIN", "CASHIER")) {
                    if(sessionManager.getGeneralSetting()?.is_accounts == true){
                        NavigationDrawerItem(
                            label = { if (!isCollapsed) Text("Accounts Entry") else Text("") },
                            icon = {
                                DrawerIcon(
                                    Icons.Default.AccountBox,
                                    contentDescription = null,
                                    isCollapsed
                                )
                            },
                            selected = currentDestination?.route in listOf("daily_entry"),
                            onClick = {
                                setExpandedMenu(if (expandedMenu == ExpandedMenu.ACCOUNTSENTRY) ExpandedMenu.NONE else ExpandedMenu.ACCOUNTSENTRY)
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = drawerItemColors
                        )
                        AnimatedVisibility(expandedMenu == ExpandedMenu.ACCOUNTSENTRY) {
                            Column(modifier = Modifier.padding(start = if (!isCollapsed) 32.dp else 0.dp)) {
                                NavigationDrawerItem(
                                    label = { if (!isCollapsed) Text("Day Entry") else Text("") },
                                    icon = {
                                        DrawerIcon(
                                            Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            isCollapsed
                                        )
                                    },
                                    selected = currentDestination?.route == "day_entry",
                                    onClick = { onDestinationClicked("day_entry") },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                    colors = subMenuColors
                                )
                            }
                        }
                    }


                    NavigationDrawerItem(
                        label = { if (!isCollapsed) Text("Bills") else Text("") },
                        icon = {
                            DrawerIcon(
                                Icons.Default.AttachMoney,
                                contentDescription = null,
                                isCollapsed
                            )
                        },
                        selected = currentDestination?.route in listOf("counter", "quick_bills"),
                        onClick = {
                            setExpandedMenu(if (expandedMenu == ExpandedMenu.BILLING) ExpandedMenu.NONE else ExpandedMenu.BILLING)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = drawerItemColors
                    )
                    AnimatedVisibility(expandedMenu == ExpandedMenu.BILLING) {
                        Column(modifier = Modifier.padding(start = if (!isCollapsed) 32.dp else 0.dp)) {
                            NavigationDrawerItem(
                                label = { if (!isCollapsed) Text("Counter Billing") else Text("") },
                                icon = {
                                    DrawerIcon(
                                        Icons.Default.PointOfSale,
                                        contentDescription = null,
                                        isCollapsed
                                    )
                                },
                                selected = currentDestination?.route == "counter",
                                onClick = { onDestinationClicked("counter") },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = subMenuColors
                            )
                            NavigationDrawerItem(
                                label = { if (!isCollapsed) Text("Quick Bills") else Text("") },
                                icon = {
                                    DrawerIcon(
                                        Icons.Default.Bolt,
                                        contentDescription = "Quick Bills",
                                        isCollapsed
                                    )
                                },
                                selected = currentDestination?.route == "quick_bills",
                                onClick = { onDestinationClicked("quick_bills") },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = drawerItemColors
                            )

                        }
                    }
                }
                if (role in listOf("RESBADMIN", "ADMIN", "CASHIER")) {
                    NavigationDrawerItem(
                        label = { if (!isCollapsed) Text("Reports") else Text("") },
                        icon = {
                            DrawerIcon(
                                Icons.Default.Assessment,
                                contentDescription = null,
                                isCollapsed
                            )
                        },
                        selected = currentDestination?.route in listOf(
                            "report_screen",
                            "orders",
                            "item_wise",
                            "category_wise",
                            "due",
                            "kot_report",
                            "paid_bills"
                        ),
                        onClick = { setExpandedMenu(if (expandedMenu == ExpandedMenu.REPORTS) ExpandedMenu.NONE else ExpandedMenu.REPORTS) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = drawerItemColors
                    )
                    AnimatedVisibility(expandedMenu == ExpandedMenu.REPORTS) {
                        Column(modifier = Modifier.padding(start = if (!isCollapsed) 32.dp else 0.dp)) {
                            NavigationDrawerItem(
                                label = { if (!isCollapsed) Text("Orders") else Text("") },
                                icon = {
                                    DrawerIcon(
                                        Icons.Default.Receipt,
                                        contentDescription = null,
                                        isCollapsed
                                    )
                                },
                                selected = currentDestination?.route == "orders",
                                onClick = { onDestinationClicked("orders") },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = subMenuColors
                            )
                            NavigationDrawerItem(
                                label = { if (!isCollapsed) Text("Paid Bills") else Text("") },
                                icon = {
                                    DrawerIcon(
                                        Icons.AutoMirrored.Filled.ListAlt,
                                        contentDescription = null,
                                        isCollapsed
                                    )
                                },
                                selected = currentDestination?.route == "paid_bills",
                                onClick = { onDestinationClicked("paid_bills") },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = subMenuColors
                            )
                            NavigationDrawerItem(
                                label = { if (!isCollapsed) Text("Due") else Text("") },
                                icon = {
                                    Icon(
                                        Icons.Filled.Payment,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(if (isCollapsed) 50.dp else 24.dp)
                                    )
                                },
                                selected = currentDestination?.route == "due",
                                onClick = { onDestinationClicked("due") },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = subMenuColors
                            )
                            NavigationDrawerItem(
                                label = { if (!isCollapsed) Text("Sales") else Text("") },
                                icon = {
                                    DrawerIcon(
                                        Icons.Default.Assessment,
                                        contentDescription = null,
                                        isCollapsed
                                    )
                                },
                                selected = currentDestination?.route == "report_screen",
                                onClick = { onDestinationClicked("report_screen") },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = subMenuColors
                            )
                            NavigationDrawerItem(
                                label = { if (!isCollapsed) Text("Item Wise") else Text("") },
                                icon = {
                                    DrawerIcon(
                                        Icons.Default.Restaurant,
                                        contentDescription = null,
                                        isCollapsed
                                    )
                                },
                                selected = currentDestination?.route == "item_wise",
                                onClick = { onDestinationClicked("item_wise") },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = subMenuColors
                            )
                            NavigationDrawerItem(
                                label = { if (!isCollapsed) Text("Category Wise") else Text("") },
                                icon = {
                                    DrawerIcon(
                                        Icons.Default.Category,
                                        contentDescription = null,
                                        isCollapsed
                                    )
                                },
                                selected = currentDestination?.route == "category_wise",
                                onClick = { onDestinationClicked("category_wise") },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = subMenuColors
                            )
                            NavigationDrawerItem(
                                label = { if (!isCollapsed) Text("KOT Report") else Text("") },
                                icon = {
                                    DrawerIcon(
                                        Icons.Default.Kitchen,
                                        contentDescription = null,
                                        isCollapsed
                                    )
                                },
                                selected = currentDestination?.route == "kot_report",
                                onClick = { onDestinationClicked("kot_report") },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = subMenuColors
                            )
                        }
                    }


                    NavigationDrawerItem(
                        label = { if (!isCollapsed) Text("GST Reports") else Text("") },
                        icon = {
                            DrawerIcon(
                                Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                isCollapsed
                            )
                        },
                        selected = currentDestination?.route in listOf(
                            "hsn_reports",
                            "gstR_Docs"
                        ),
                        onClick = { setExpandedMenu(if (expandedMenu == ExpandedMenu.GSTREPORTS) ExpandedMenu.NONE else ExpandedMenu.GSTREPORTS) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = drawerItemColors
                    )
                    AnimatedVisibility(expandedMenu == ExpandedMenu.GSTREPORTS) {
                        Column(modifier = Modifier.padding(start = if (!isCollapsed) 32.dp else 0.dp)) {

                            NavigationDrawerItem(
                                label = { if (!isCollapsed) Text("HSN Reports") else Text("") },
                                icon = {
                                    DrawerIcon(
                                        Icons.AutoMirrored.Filled.ListAlt,
                                        contentDescription = null,
                                        isCollapsed
                                    )
                                },
                                selected = currentDestination?.route == "hsn_reports",
                                onClick = { onDestinationClicked("hsn_reports") },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = subMenuColors
                            )

                            NavigationDrawerItem(
                                label = { if (!isCollapsed) Text("GSTR-Docs") else Text("") },
                                icon = {
                                    DrawerIcon(
                                        Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = null,
                                        isCollapsed
                                    )
                                },
                                selected = currentDestination?.route == "gstR_Docs",
                                onClick = { onDestinationClicked("gstR_Docs") },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = subMenuColors
                            )
                        }
                    }

                    if (sessionManager.getGeneralSetting()?.is_accounts == true) {
                        NavigationDrawerItem(
                            label = { if (!isCollapsed) Text("Accounts Reports") else Text("") },
                            icon = {
                                DrawerIcon(
                                    Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    isCollapsed
                                )
                            },
                            selected = currentDestination?.route in listOf(
                                "day_entry_report",
                                "day_book_report"
                            ),
                            onClick = { setExpandedMenu(if (expandedMenu == ExpandedMenu.ACCOUNTSREPORT) ExpandedMenu.NONE else ExpandedMenu.ACCOUNTSREPORT) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = drawerItemColors
                        )
                        AnimatedVisibility(expandedMenu == ExpandedMenu.ACCOUNTSREPORT) {
                            Column(modifier = Modifier.padding(start = if (!isCollapsed) 32.dp else 0.dp)) {

                                NavigationDrawerItem(
                                    label = { if (!isCollapsed) Text("Ledger Report") else Text("") },
                                    icon = {
                                        DrawerIcon(
                                            Icons.AutoMirrored.Filled.ListAlt,
                                            contentDescription = null,
                                            isCollapsed
                                        )
                                    },
                                    selected = currentDestination?.route == "day_entry_report",
                                    onClick = { onDestinationClicked("day_entry_report") },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                    colors = subMenuColors
                                )

                                NavigationDrawerItem(
                                    label = { if (!isCollapsed) Text("Day Book Report") else Text("") },
                                    icon = {
                                        DrawerIcon(
                                            Icons.AutoMirrored.Filled.ReceiptLong,
                                            contentDescription = null,
                                            isCollapsed
                                        )
                                    },
                                    selected = currentDestination?.route == "day_book_report",
                                    onClick = { onDestinationClicked("day_book_report") },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                    colors = subMenuColors
                                )
                            }
                        }
                    }

                }

                // 🔹 Settings (admin only)
                if (role in listOf("RESBADMIN", "ADMIN")) {
                    NavigationDrawerItem(
                        label = { if (!isCollapsed) Text("Settings") else Text("") },
                        icon = {
                            DrawerIcon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                isCollapsed
                            )
                        },
                        selected = currentDestination?.route == "settings",
                        onClick = { onDestinationClicked("settings") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = drawerItemColors
                    )
//                NavigationDrawerItem(
//                    label = { if (!isCollapsed) Text("AI Assistant") else Text("") },
//                    icon = { Icon(Icons.Default.Assistant, contentDescription = null) },
//                    selected = currentDestination?.route == "ai_assistant",
//                    onClick = { onDestinationClicked("ai_assistant") },
//                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
//                    colors = drawerItemColors
//                )

//                    NavigationDrawerItem(
//                        label = { if (!isCollapsed) Text("Template") else Text("") },
//                        icon = { DrawerIcon(Icons.Default.Business, contentDescription = null,isCollapsed) },
//                        selected = currentDestination?.route == "template_screen",
//                        onClick = { onDestinationClicked("template_screen") },
//                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
//                        colors = drawerItemColors
//                    )
                }

                if (role in listOf("RESBADMIN", "ADMIN", "WAITER", "CASHIER", "CHEF")) {
                    NavigationDrawerItem(
                        label = { if (!isCollapsed) Text("Support") else Text("") },
                        icon = {
                            DrawerIcon(
                                Icons.Default.SupportAgent,
                                contentDescription = null,
                                isCollapsed
                            )
                        },
                        selected = currentDestination?.route == "support_screen",
                        onClick = { onDestinationClicked("support_screen") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = drawerItemColors
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // 🔹 Collapse Toggle
                NavigationDrawerItem(
                    label = { if (!isCollapsed) Text("Collapse Drawer") else Text("") },
                    icon = {
                        DrawerIcon(
                            if (isCollapsed) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Toggle Collapse",
                            isCollapsed = isCollapsed
                        )
                    },
                    selected = false,
                    onClick = onCollapseToggle,
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = drawerItemColors
                )

                Spacer(modifier = Modifier.weight(1f))
            }
            ModernDivider()

            // 🔹 Logout
            if (role in listOf("RESBADMIN", "ADMIN", "WAITER", "CHEF", "CASHIER")) {
                NavigationDrawerItem(
                    label = { if (!isCollapsed) Text("Logout") else Text("") },
                    icon = {
                        DrawerIcon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            isCollapsed
                        )
                    },
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
        modifier = Modifier.size(if (isCollapsed) 50.dp else 24.dp) // 👈 bigger when collapsed
    )
}
