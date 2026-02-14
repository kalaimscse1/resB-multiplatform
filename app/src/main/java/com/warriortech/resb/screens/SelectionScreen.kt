package com.warriortech.resb.screens

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.warriortech.resb.model.Table
import com.warriortech.resb.model.TableStatusResponse
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.ui.theme.*
import com.warriortech.resb.ui.viewmodel.TableViewModel
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.NetworkStatusBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionScreen(
    onTableSelected: (Table) -> Unit,
    viewModel: TableViewModel = hiltViewModel(),
    drawerState: DrawerState,
    sessionManager: SessionManager,
    navController: NavHostController
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedTables by viewModel.selectedTables.collectAsState()
    val selectionAction by viewModel.selectionAction.collectAsState()
    
    var showTableOptions by remember { mutableStateOf<TableStatusResponse?>(null) }
    
    BackHandler {
        if (isSelectionMode) {
            viewModel.disableSelectionMode()
        } else {
            navController.navigate("dashboard") {
                popUpTo("dashboard") { inclusive = true }
            }
        }
    }
    val tablesState by viewModel.tablesState.collectAsState()
    val areas by viewModel.areas.collectAsState()
    val scope = rememberCoroutineScope()

    val displayableAreas = areas.filter { it.area_name != "--" }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { displayableAreas.size }
    )
    val currentArea = displayableAreas.getOrNull(pagerState.currentPage)
    val user = sessionManager.getUser()
    val role = user?.role ?: ""
    val areaId = user?.area_name ?: ""
    
    // auto-refresh
    LaunchedEffect(Unit) {
        // Initial setup for non-admin roles to focus on their assigned area
        if (role != "ADMIN" && role != "RESBADMIN" && role != "CHEF") {
            user?.area_id?.let { assignedAreaId ->
                if (assignedAreaId != 0L) {
                    viewModel.setSection(assignedAreaId)
                }
            }
        }
        while (true) {
            viewModel.loadTables()
            delay(7 * 1000)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (role == "ADMIN" || role == "RESBADMIN" || role == "CHEF") {
            currentArea?.let {
                viewModel.setSection(it.area_id)
            }
        }
    }

    if (showTableOptions != null) {
        AlertDialog(
            onDismissRequest = { showTableOptions = null },
            title = { Text("Table Options - ${showTableOptions?.table_name}") },
            text = { Text("What would you like to do?") },
            confirmButton = {
                TextButton(onClick = {
                    showTableOptions?.let { 
                        viewModel.enableSelectionMode(TableViewModel.SelectionAction.ChangeTable, it.table_id) 
                    }
                    showTableOptions = null
                }) {
                    Text("Change Table")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showTableOptions?.let { 
                            viewModel.enableSelectionMode(TableViewModel.SelectionAction.MergeTable, it.table_id) 
                        }
                        showTableOptions = null
                    }) {
                        Text("Merge Tables")
                    }
                    TextButton(onClick = { showTableOptions = null }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isSelectionMode) {
                            when (selectionAction) {
                                TableViewModel.SelectionAction.ChangeTable -> "Select Target Table"
                                TableViewModel.SelectionAction.MergeTable -> "Select Tables to Merge"
                                null -> "Table"
                            }
                        } else "Table",
                        style = MaterialTheme.typography.titleLarge,
                        color = SurfaceLight
                    )
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = { viewModel.disableSelectionMode() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = SurfaceLight)
                        }
                    } else {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = SurfaceLight)
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = { viewModel.confirmSelection() }) {
                            Icon(Icons.Default.Check, contentDescription = "Confirm", tint = SurfaceLight)
                        }
                    } else {
                        IconButton(onClick = {
                            navController.navigate("takeaway_menu") { launchSingleTop = true }
                        }) {
                            Icon(Icons.Default.Fastfood, contentDescription = "Takeaway Menu", tint = SurfaceLight)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NetworkStatusBar(connectionState = connectionState)

            if (role == "ADMIN" || role == "RESBADMIN" || role == "CHEF") {
                if (displayableAreas.isNotEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        backgroundColor = SecondaryGreen,
                        contentColor = SurfaceLight,
                        edgePadding = 0.dp
                    ) {
                        displayableAreas.forEachIndexed { index, areaItem ->
                            Tab(
                                selected = index == pagerState.currentPage,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                text = { Text(areaItem.area_name) }
                            )
                        }
                    }

                    HorizontalPager(
                        pageSize = PageSize.Fill,
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val area = displayableAreas[page]
                        val filteredTables = when (val state = tablesState) {
                            is TableViewModel.TablesState.Success -> state.tables.filter { it.area_name == area.area_name }
                            else -> emptyList()
                        }

                        when (val currentTablesState = tablesState) {
                            is TableViewModel.TablesState.Loading -> {
                                Box(Modifier.fillMaxSize(), Alignment.Center) {
                                    CircularProgressIndicator(color = PrimaryGreen)
                                }
                            }
                            is TableViewModel.TablesState.Success -> {
                                if (filteredTables.isEmpty()) {
                                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                                        Text("No tables in ${area.area_name}.", style = MaterialTheme.typography.bodyMedium)
                                    }
                                } else {
                                    ResponsiveTableGrid(
                                        filteredTables = filteredTables,
                                        onTableSelected = onTableSelected,
                                        sessionManager = sessionManager,
                                        isSelectionMode = isSelectionMode,
                                        selectedTables = selectedTables,
                                        onTableLongClick = { if (!isSelectionMode) showTableOptions = it },
                                        onTableSelectionToggle = { viewModel.toggleTableSelection(it.table_id) },
                                        time = "",
                                        staff = ""
                                    )
                                }
                            }
                            is TableViewModel.TablesState.Error -> {
                                Box(Modifier.fillMaxSize(), Alignment.Center) {
                                    Text(currentTablesState.message, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            } else {
                // Fix for non-admin roles: wrap single Tab in TabRow and ensure filtered load
                if (areaId.isNotEmpty()) {
                    TabRow(
                        selectedTabIndex = 0,
                        backgroundColor = SecondaryGreen,
                        contentColor = SurfaceLight
                    ) {
                        Tab(
                            selected = true,
                            onClick = { 
                                user?.area_id?.let { viewModel.setSection(it) }
                            },
                            text = { 
                                Text(
                                    text = areaId, 
                                    style = MaterialTheme.typography.titleSmall,
                                    color = SurfaceLight
                                ) 
                            }
                        )
                    }
                }

                when (val currentTablesState = tablesState) {
                    is TableViewModel.TablesState.Loading -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryGreen)
                        }
                    }
                    is TableViewModel.TablesState.Success -> {
                        // Filter tables by user's assigned area
                        val filteredTables = if (areaId.isNotEmpty()) {
                            currentTablesState.tables.filter { it.area_name == areaId }
                        } else {
                            currentTablesState.tables
                        }

                        if (filteredTables.isEmpty()) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Text(
                                    text = if (areaId.isNotEmpty()) "No tables available in $areaId." else "No tables available.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            ResponsiveTableGrid(
                                filteredTables = filteredTables,
                                onTableSelected = onTableSelected,
                                sessionManager = sessionManager,
                                isSelectionMode = isSelectionMode,
                                selectedTables = selectedTables,
                                onTableLongClick = { if (!isSelectionMode) showTableOptions = it },
                                onTableSelectionToggle = { viewModel.toggleTableSelection(it.table_id) },
                                time = "",
                                staff = ""
                            )
                        }
                    }
                    is TableViewModel.TablesState.Error -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Text(currentTablesState.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ResponsiveTableGrid(
    filteredTables: List<TableStatusResponse>,
    onTableSelected: (Table) -> Unit,
    sessionManager: SessionManager,
    isSelectionMode: Boolean = false,
    selectedTables: Set<Long> = emptySet(),
    onTableLongClick: (TableStatusResponse) -> Unit = {},
    onTableSelectionToggle: (TableStatusResponse) -> Unit = {},
    time: String,
    staff: String
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val tableSize = when {
        screenWidthDp < 600 -> GridCells.Fixed(3)     // Phones → exactly 3 columns
        screenWidthDp < 840 -> GridCells.Adaptive(150.dp) // Small tablets
        else -> GridCells.Adaptive(180.dp)                // large tablet
    }
    LazyVerticalGrid(
        columns = tableSize,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(filteredTables) { table ->
            TableItem(
                table = table,
                isSelected = selectedTables.contains(table.table_id),
                onClick = {
                    if (isSelectionMode) {
                        onTableSelectionToggle(table)
                    } else {
                        val tbl = Table(
                            table_id = table.table_id,
                            area_id = table.area_id,
                            area_name = table.area_name,
                            table_name = table.table_name,
                            seating_capacity = table.seating_capacity.toInt(),
                            is_ac = table.is_ac,
                            table_status = table.table_status,
                            table_availability = table.table_availability,
                            is_active = table.is_active
                        )
                        onTableSelected(tbl)
                    }
                },
                onLongClick = { onTableLongClick(table) },
                sessionManager = sessionManager,
                time = time,
                staff = staff
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun TableItem(
    table: TableStatusResponse,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    sessionManager: SessionManager,
    time: String,
    staff: String
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    // 🧱 Responsive sizing
    val tableSize = when {
        screenWidthDp < 600 -> 100.dp
        screenWidthDp < 840 -> 130.dp
        else -> 160.dp
    }
    val color = when {
        isSelected -> MaterialTheme.colorScheme.primary
        table.table_availability == "AVAILABLE" -> TextSecondary
        table.table_availability == "OCCUPIED" -> SuccessGreen
        table.table_availability == "RESERVED" -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor: Color = color
    val cornerRadius: Dp = 12.dp
    val borderWidth: Dp = if (isSelected) 8.dp else 6.dp

    Surface(
        modifier = Modifier
            .width(tableSize)
            .height(tableSize)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .border(
                if (isSelected) 4.dp else 1.dp, 
                if (isSelected) MaterialTheme.colorScheme.primary else borderColor, 
                RoundedCornerShape(cornerRadius)
            ),
        shape = RoundedCornerShape(cornerRadius),
        tonalElevation = 8.dp,
        color = ghostWhite
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(cornerRadius))
                .drawWithContent {
                    drawContent()
                    val stroke = borderWidth.toPx()
                    val width = size.width
                    drawRoundRect(
                        color = borderColor,
                        topLeft = Offset(0f, 0f),
                        size = Size(width, stroke),
                        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                    )
                }
        ) {
            // 🧾 Table Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (tableSize==100.dp){
                    if (table.grandTotal > 0) {
                        // Top Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = table.order_time,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.weight(1f)
                            )
                            Text("₹${table.grandTotal + table.delivery_amt - table.disc_amt}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = if(table.bill_no.isEmpty()) ErrorRed else BluePrimary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold
                            )
                        }


                        // Table Name
                        Text(
                            text = table.table_name.padStart(2, '0'),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        // Bottom Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = table.staff_name,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.weight(1.1f)
                            )
                            Text("${table.seating_capacity}/S",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(0.3f))
                        }

                    } else {
                        // Empty Table (Available)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = table.table_name.padStart(2, '0'),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color =MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("${table.seating_capacity} Seats", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }else{

                    if (table.grandTotal > 0) {
                        // Top Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(table.order_time, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text(CurrencySettings.format(table.grandTotal), color = if(table.bill_no.isEmpty()) ErrorRed else BluePrimary, fontSize = 12.sp,fontWeight = FontWeight.Bold,)
                        }



                        // Table Name
                        Text(
                            text = table.table_name.padStart(2, '0'),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        // Bottom Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(table.staff_name, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text("${table.seating_capacity} Seats", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    } else {
                        // Empty Table (Available)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = table.table_name.padStart(2, '0'),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color =MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("${table.seating_capacity} Seats", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
