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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Menu
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
    val tablesState by viewModel.tablesState.collectAsState()
    val areas by viewModel.areas.collectAsState()
    val scope = rememberCoroutineScope()

    val displayableAreas = areas.filter { it.area_name != "--" }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { displayableAreas.size }
    )
    val currentArea = displayableAreas.getOrNull(pagerState.currentPage)
    val role = sessionManager.getUser()?.role ?: ""
    val areaId = sessionManager.getUser()?.area_name ?: ""
    var selectedArea by remember { mutableStateOf<String?>(areaId) }

    // auto-refresh
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.loadTables()
            delay(5 * 1000)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        currentArea?.let {
            viewModel.setSection(it.area_id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Table",
                        style = MaterialTheme.typography.titleLarge,
                        color = SurfaceLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = SurfaceLight)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("takeaway_menu") { launchSingleTop = true }
                    }) {
                        Icon(Icons.Default.Fastfood, contentDescription = "Takeaway Menu", tint = SurfaceLight)
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
                                    CircularProgressIndicator()
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
                if (areas.isNotEmpty()) {
                    Tab(
                        selected = true,
                        onClick = {
                            selectedArea = sessionManager.getUser()?.area_name
                            viewModel.setSection(sessionManager.getUser()?.area_id)
                        },
                        text = { Text(areaId) }
                    )
                }

                when (val currentTablesState = tablesState) {
                    is TableViewModel.TablesState.Loading -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                    }
                    is TableViewModel.TablesState.Success -> {
                        val filteredTables = currentTablesState.tables.filter { it.area_name == areaId }
                        if (filteredTables.isEmpty()) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Text("No tables available.", textAlign = TextAlign.Center)
                            }
                        } else {
                            ResponsiveTableGrid(
                                filteredTables = filteredTables,
                                onTableSelected = onTableSelected,
                                sessionManager = sessionManager,
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
                onClick = {
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
                },
                sessionManager = sessionManager,
                time = time,
                staff = staff
            )
        }
    }
}

//@Composable
//fun TableItem(
//    table: TableStatusResponse,
//    onClick: () -> Unit,
//    sessionManager: SessionManager,
//    time: String,
//    staff: String
//) {
//    val configuration = LocalConfiguration.current
//    val screenWidthDp = configuration.screenWidthDp
//    val tableSize = when {
//        screenWidthDp < 600 -> 100.dp   // phone
//        screenWidthDp < 840 -> 130.dp   // small tablet
//        else -> 160.dp                  // large tablet
//    }
//
//    val color = when (table.table_availability) {
//        "AVAILABLE" -> TextSecondary
//        "OCCUPIED" -> SuccessGreen
//        "RESERVED" -> MaterialTheme.colorScheme.tertiaryContainer
//        else -> MaterialTheme.colorScheme.surfaceVariant
//    }
//
//    val borderColor: Color = color
//    val cornerRadius: Dp = 12.dp
//    val borderWidth: Dp = 6.dp
//
//    Surface(
//        modifier = Modifier
//            .width(tableSize)
//            .height(tableSize)
//            .clickable(onClick = onClick)
//            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius)),
//        shape = RoundedCornerShape(12.dp),
//        tonalElevation = 8.dp,
//        color = ghostWhite
//    ) {
//        Box(
//            modifier = Modifier
//                .clip(RoundedCornerShape(cornerRadius))
//                .drawWithContent {
//                    drawContent()
//                    val stroke = borderWidth.toPx()
//                    val width = size.width
//                    drawRoundRect(
//                        color = borderColor,
//                        topLeft = Offset(0f, 0f),
//                        size = Size(width, stroke),
//                        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
//                    )
//                }
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(8.dp),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.SpaceBetween
//            ) {
//                if (table.grandTotal>0){
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text(table.staff_name, color = Color.White, fontSize = 12.sp)
//                        Text("${table.seating_capacity} Seats", color = Color.White, fontSize = 12.sp)
//                    }
//                }
//                else{
//                    Text(
//                        "${table.seating_capacity} Seats",
//                        color = Color(0xFF005B9F),
//                        fontSize = 12.sp,
//                        textAlign = TextAlign.Center
//                    )
//                }
//
//                Text(
//                    text = table.table_name.padStart(2, '0'),
//                    fontSize = 28.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = if (table.grandTotal>0) Color.White else Color(0xFF005B9F)
//                )
//
//                if (table.grandTotal>0){
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text(table.order_time, color = Color.White, fontSize = 12.sp)
//                        Text(CurrencySettings.format(table.grandTotal), color = Color.White, fontSize = 12.sp)
//                    }
//                }
//                // 🟢 Table name + seats
////                Column(horizontalAlignment = Alignment.CenterHorizontally) {
////                    Text(
////                        text = table.table_name,
////                        style = MaterialTheme.typography.titleMedium.copy(
////                            fontSize = when {
////                                screenWidthDp < 600 -> 14.sp
////                                else -> 16.sp
////                            },
////                            fontWeight = FontWeight.Bold
////                        ),
////                        textAlign = TextAlign.Center
////                    )
////                    Spacer(Modifier.height(4.dp))
////                    Text(
////                        text = "${table.seating_capacity} Seats",
////                        style = MaterialTheme.typography.bodySmall.copy(
////                            fontSize = when {
////                                screenWidthDp < 600 -> 12.sp
////                                else -> 13.sp
////                            }
////                        )
////                    )
////                }
//
//                // 🟣 Time - Amount/New - Staff
////                Row(
////                    modifier = Modifier
////                        .fillMaxWidth()
////                        .padding(horizontal = 4.dp),
////                    verticalAlignment = Alignment.CenterVertically
////                ) {
////                    Text(
////                        text = table.order_time,
////                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
////                        color = MaterialTheme.colorScheme.onSurfaceVariant,
////                        modifier = Modifier.weight(0.4f)
////                    )
////
////                    Text(
////                        text = if (table.grandTotal > 0)
////                            CurrencySettings.format(table.grandTotal)
////                        else "New",
////                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
////                        color = if (table.grandTotal > 0) ErrorRed else DarkGreen,
////                        textAlign = TextAlign.Center,
////                        modifier = Modifier.weight(1.2f)
////                    )
////
//                    Text(
//                        text = table.staff_name,
//                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        textAlign = TextAlign.End,
//                        modifier = Modifier.weight(0.4f)
//                    )
////                }
//            }
//        }
//    }
//}
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun TableItem(
    table: TableStatusResponse,
    onClick: () -> Unit,
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
    val color = when (table.table_availability) {
        "AVAILABLE" -> TextSecondary
        "OCCUPIED" -> SuccessGreen
        "RESERVED" -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor: Color = color
    val cornerRadius: Dp = 12.dp
    val borderWidth: Dp = 6.dp

    Surface(
        modifier = Modifier
            .width(tableSize)
            .height(tableSize)
            .clickable(onClick = onClick)
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius)),
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
//                        Text(table.order_time, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            Text(
                                text = table.order_time,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.weight(1f)
                            )
                            Text("₹${table.grandTotal.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = ErrorRed,
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
//                        Text(table.staff_name, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
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
                            Text(CurrencySettings.format(table.grandTotal), color = ErrorRed, fontSize = 12.sp,fontWeight = FontWeight.Bold,)
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
//
///**
// * 🕒 Format order time (e.g. "2025-10-14T13:45:22" → "01:45 PM")
// */
//fun formatOrderTime(orderTime: String?): String {
//    return try {
//        val parser = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
//        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
//        LocalDateTime.parse(orderTime, parser).format(formatter)
//    } catch (e: Exception) {
//        ""
//    }
//}
//
//@Composable
//fun TableItem(
//    table: TableStatusResponse,
//    onClick: () -> Unit,
//    sessionManager: SessionManager
//) {
//    val configuration = LocalConfiguration.current
//    val screenWidthDp = configuration.screenWidthDp
//    val tableSize = when {
//        screenWidthDp < 600 -> 100.dp
//        screenWidthDp < 840 -> 130.dp
//        else -> 160.dp
//    }
//
//    val borderColor = when (table.table_availability) {
//        "AVAILABLE" -> Color(0xFF4CAF50) // green
//        "OCCUPIED" -> Color(0xFFEF5350) // red
//        "RESERVED" -> Color(0xFFFFA726) // orange
//        else -> Color(0xFFB0BEC5)
//    }
//
//    val isOccupied = table.grandTotal > 0
//    val backgroundColor = if (isOccupied) borderColor else Color(0xFFF9F9F9)
//    val contentColor = if (isOccupied) Color.White else Color(0xFF005B9F)
//
//    Surface(
//        modifier = Modifier
//            .width(tableSize)
//            .height(tableSize)
//            .clickable(onClick = onClick)
//            .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(12.dp)),
//        color = backgroundColor,
//        shape = RoundedCornerShape(12.dp),
//        tonalElevation = 6.dp
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(8.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.SpaceBetween
//        ) {
//            // Top row
//            if (isOccupied) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(table.staff_name, color = contentColor, fontSize = 11.sp)
//                    Text("${table.seating_capacity} Seats", color = contentColor, fontSize = 11.sp)
//                }
//            } else {
//                Text(
//                    "${table.seating_capacity} Seats",
//                    color = contentColor,
//                    fontSize = 12.sp,
//                    textAlign = TextAlign.Center
//                )
//            }
//
//            // Table name
//            Text(
//                text = table.table_name.padStart(2, '0'),
//                fontSize = 26.sp,
//                fontWeight = FontWeight.Bold,
//                color = contentColor,
//                textAlign = TextAlign.Center
//            )
//
//            // Bottom row
//            if (isOccupied) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(
//                        table.order_time,
//                        color = contentColor,
//                        fontSize = 11.sp
//                    )
//                    Text(
//                        CurrencySettings.format(table.grandTotal),
//                        color = contentColor,
//                        fontSize = 11.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//            }
//        }
//    }
//}