package com.warriortech.resb.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onBackPressed: () -> Unit,
    drawerState: DrawerState,
    navController: NavController
) {
    var selectedCategory by remember { mutableStateOf<SupportCategory?>(null) }
    val uriHandler = LocalUriHandler.current
    BackHandler {
        navController.navigate("dashboard") {
            popUpTo("dashboard") { inclusive = true }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        selectedCategory?.title ?: "Help & Support",
                        color = SurfaceLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedCategory != null) {
                            selectedCategory = null
                        } else {
                            onBackPressed()
                        }
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
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
                Text(
                    text = "For support, contact : +91-7826040873, +91-9788106710, +91-9942014611, +91-8072944941\n" +
                            "Email: kingtecsolution@gmail.com\n" +
                            "Website: www.kingtecsolution.com",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    ) { paddingValues ->
        if (selectedCategory == null) {
            SupportMainScreen(
                modifier = Modifier.padding(paddingValues),
                onCategorySelected = { category -> selectedCategory = category },
                onVideoClick = { url -> uriHandler.openUri(url) }
            )
        } else {
            SupportDetailScreen(
                category = selectedCategory!!,
                modifier = Modifier.padding(paddingValues),
                onVideoClick = { url -> uriHandler.openUri(url) }
            )
        }
    }
}

@Composable
fun SupportMainScreen(
    modifier: Modifier = Modifier,
    onCategorySelected: (SupportCategory) -> Unit,
    onVideoClick: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AppOverviewCard(onVideoClick = onVideoClick)
        }

        item {
            Text(
                text = "Browse Help Topics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(supportCategories) { category ->
            SupportCategoryCard(
                category = category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun AppOverviewCard(onVideoClick: (String) -> Unit) {
    MobileOptimizedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RES-B - Restaurant Billing System",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Version 1.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "A comprehensive restaurant billing and management system built with modern Android technologies. " +
                        "Res-B provides complete solution for restaurant operations including order management, " +
                        "billing, inventory tracking, payment processing, and detailed reporting.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Justify
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Key Features:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            val features = listOf(
                "• Counter billing with real-time calculations",
                "• Table and takeaway order management",
                "• Inventory and menu management",
                "• Multiple payment methods support",
                "• Comprehensive sales and tax reporting",
                "• Staff and role management",
                "• Receipt template customization",
                "• Background data synchronization"
            )

            features.forEach { feature ->
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onVideoClick("https://www.youtube.com/watch?v=demo-app-overview") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Watch App Overview Video")
            }
        }
    }
}

@Composable
fun SupportCategoryCard(
    category: SupportCategory,
    onClick: () -> Unit
) {
    MobileOptimizedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SupportDetailScreen(
    category: SupportCategory,
    modifier: Modifier = Modifier,
    onVideoClick: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(category.items) { item ->
            SupportItemCard(
                item = item,
                onVideoClick = onVideoClick
            )
        }
    }
}

@Composable
fun SupportItemCard(
    item: SupportItem,
    onVideoClick: (String) -> Unit
) {
    MobileOptimizedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (item.videoUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { onVideoClick(item.videoUrl) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Watch Tutorial")
                }
            }
        }
    }
}

data class SupportCategory(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val items: List<SupportItem>
)

data class SupportItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val videoUrl: String = ""
)

val supportCategories = listOf(
    SupportCategory(
        id = "navigation",
        title = "Navigation & Menu",
        description = "Learn how to navigate through different sections of the app",
        icon = Icons.Default.Menu,
        items = listOf(
            SupportItem(
                title = "Dashboard",
                description = "Main overview screen showing daily sales, active orders, and quick access to key features. View your restaurant's performance at a glance.",
                icon = Icons.Default.Dashboard,
                videoUrl = "https://www.youtube.com/watch?v=dashboard-tutorial"
            ),
            SupportItem(
                title = "Masters - Menu Items",
                description = "Add, edit, and manage your restaurant's menu items. Set prices, categories, taxes, and availability for each dish.",
                icon = Icons.Default.Restaurant,
                videoUrl = "https://www.youtube.com/watch?v=menu-items-tutorial"
            ),
            SupportItem(
                title = "Masters - Menu",
                description = "Organize your menu items into different menus for various times of day or special occasions.",
                icon = Icons.Default.MenuBook,
                videoUrl = "https://www.youtube.com/watch?v=menu-tutorial"
            ),
            SupportItem(
                title = "Masters - Menu Categories",
                description = "Create and manage categories to organize your menu items logically (starters, mains, desserts, etc.).",
                icon = Icons.Default.Category,
                videoUrl = "https://www.youtube.com/watch?v=categories-tutorial"
            ),
            SupportItem(
                title = "Masters - Tables",
                description = "Set up and manage dining tables with capacity, status tracking, and area assignments.",
                icon = Icons.Default.TableRestaurant,
                videoUrl = "https://www.youtube.com/watch?v=tables-tutorial"
            ),
            SupportItem(
                title = "Masters - Areas",
                description = "Define different dining areas in your restaurant (indoor, outdoor, VIP, etc.) for better organization.",
                icon = Icons.Default.LocationOn,
                videoUrl = "https://www.youtube.com/watch?v=areas-tutorial"
            ),
            SupportItem(
                title = "Orders - Dine In",
                description = "Take orders for customers dining in your restaurant. Select tables, add items, and track order status.",
                icon = Icons.Default.Restaurant,
                videoUrl = "https://www.youtube.com/watch?v=dine-in-tutorial"
            ),
            SupportItem(
                title = "Orders - Takeaway",
                description = "Process takeaway orders efficiently with customer details and pickup scheduling.",
                icon = Icons.Default.Fastfood,
                videoUrl = "https://www.youtube.com/watch?v=takeaway-tutorial"
            ),
            SupportItem(
                title = "Bills - Counter Billing",
                description = "Complete billing solution with itemwise calculations, tax computation, and multiple payment methods.",
                icon = Icons.Default.PointOfSale,
                videoUrl = "https://www.youtube.com/watch?v=counter-billing-tutorial"
            ),
            SupportItem(
                title = "Bills - Quick Bills",
                description = "Fast billing for quick service with preset items and instant payment processing.",
                icon = Icons.Default.Bolt,
                videoUrl = "https://www.youtube.com/watch?v=quick-bills-tutorial"
            )
        )
    ),
    SupportCategory(
        id = "reports",
        title = "Reports & Analytics",
        description = "Understand your restaurant's performance with detailed reports",
        icon = Icons.Default.Assessment,
        items = listOf(
            SupportItem(
                title = "Orders Report",
                description = "Track all orders with detailed information including time, table, items, and payment status.",
                icon = Icons.Default.Receipt,
                videoUrl = "https://www.youtube.com/watch?v=orders-report-tutorial"
            ),
            SupportItem(
                title = "Sales Report",
                description = "Comprehensive sales analytics with daily, weekly, and monthly breakdowns including tax calculations.",
                icon = Icons.Default.Assessment,
                videoUrl = "https://www.youtube.com/watch?v=sales-report-tutorial"
            ),
            SupportItem(
                title = "Item Wise Report",
                description = "Analyze performance of individual menu items to identify best sellers and optimize your menu.",
                icon = Icons.Default.Restaurant,
                videoUrl = "https://www.youtube.com/watch?v=item-wise-tutorial"
            ),
            SupportItem(
                title = "Category Wise Report",
                description = "Review sales performance by menu categories to understand customer preferences.",
                icon = Icons.Default.Category,
                videoUrl = "https://www.youtube.com/watch?v=category-wise-tutorial"
            ),
            SupportItem(
                title = "KOT Report",
                description = "Kitchen Order Ticket reports for tracking kitchen performance and order fulfillment times.",
                icon = Icons.Default.Kitchen,
                videoUrl = "https://www.youtube.com/watch?v=kot-report-tutorial"
            )
        )
    ),
    SupportCategory(
        id = "settings",
        title = "Settings & Configuration",
        description = "Configure your restaurant system according to your business needs",
        icon = Icons.Default.Settings,
        items = listOf(
            SupportItem(
                title = "Customer Management",
                description = "Add and manage customer information including contact details, preferences, and order history.",
                icon = Icons.Default.Person,
                videoUrl = "https://www.youtube.com/watch?v=customer-management-tutorial"
            ),
            SupportItem(
                title = "Staff Management",
                description = "Manage your team members with role assignments, contact information, and access permissions.",
                icon = Icons.Default.People,
                videoUrl = "https://www.youtube.com/watch?v=staff-management-tutorial"
            ),
            SupportItem(
                title = "Role Management",
                description = "Define user roles and permissions to control access to different features based on job responsibilities.",
                icon = Icons.Default.Security,
                videoUrl = "https://www.youtube.com/watch?v=role-management-tutorial"
            ),
            SupportItem(
                title = "Printer Settings",
                description = "Configure kitchen printers, receipt printers, and thermal printers for different locations.",
                icon = Icons.Default.Print,
                videoUrl = "https://www.youtube.com/watch?v=printer-settings-tutorial"
            ),
            SupportItem(
                title = "Tax Configuration",
                description = "Set up tax rates including GST, CGST, SGST, IGST, and CESS according to local regulations.",
                icon = Icons.Default.Calculate,
                videoUrl = "https://www.youtube.com/watch?v=tax-configuration-tutorial"
            ),
            SupportItem(
                title = "Tax Split Settings",
                description = "Configure complex tax splitting for items with multiple tax components.",
                icon = Icons.Default.CallSplit,
                videoUrl = "https://www.youtube.com/watch?v=tax-split-tutorial"
            ),
            SupportItem(
                title = "Restaurant Profile",
                description = "Manage your restaurant's basic information including name, address, contact details, and business hours.",
                icon = Icons.Default.Store,
                videoUrl = "https://www.youtube.com/watch?v=restaurant-profile-tutorial"
            ),
            SupportItem(
                title = "General Settings",
                description = "Configure currency, language, timezone, and other general application preferences.",
                icon = Icons.Default.Settings,
                videoUrl = "https://www.youtube.com/watch?v=general-settings-tutorial"
            ),
            SupportItem(
                title = "Create Voucher",
                description = "Design and create discount vouchers with expiry dates, usage limits, and redemption rules.",
                icon = Icons.Default.LocalOffer,
                videoUrl = "https://www.youtube.com/watch?v=create-voucher-tutorial"
            ),
            SupportItem(
                title = "Counter Settings",
                description = "Set up multiple billing counters for larger restaurants with different stations.",
                icon = Icons.Default.PointOfSale,
                videoUrl = "https://www.youtube.com/watch?v=counter-settings-tutorial"
            ),
            SupportItem(
                title = "Language Settings",
                description = "Configure multi-language support for menu items and receipts to serve diverse customers.",
                icon = Icons.Default.Language,
                videoUrl = "https://www.youtube.com/watch?v=language-settings-tutorial"
            ),
            SupportItem(
                title = "Receipt Template",
                description = "Customize receipt layouts, add logos, and configure what information appears on printed receipts.",
                icon = Icons.Default.Kitchen,
                videoUrl = "https://www.youtube.com/watch?v=receipt-template-tutorial"
            ),
            SupportItem(
                title = "AddOn",
                description = "Create menu item modifiers like extra cheese, spice levels, cooking preferences, and add-ons.",
                icon = Icons.Default.Add,
                videoUrl = "https://www.youtube.com/watch?v=modifiers-tutorial"
            ),
            SupportItem(
                title = "Change Password",
                description = "Update your account password for security. Ensure your account remains protected.",
                icon = Icons.Default.ChangeCircle,
                videoUrl = "https://www.youtube.com/watch?v=change-password-tutorial"
            ),
            SupportItem(
                title = "Reset Data",
                description = "Safely reset application data when needed. This action will clear all stored information.",
                icon = Icons.Default.Delete,
                videoUrl = "https://www.youtube.com/watch?v=reset-data-tutorial"
            ),
            SupportItem(
                title = "Change Company",
                description = "Switch between different restaurant branches or companies if you manage multiple establishments.",
                icon = Icons.Default.Business,
                videoUrl = "https://www.youtube.com/watch?v=change-company-tutorial"
            )
        )
    )
)