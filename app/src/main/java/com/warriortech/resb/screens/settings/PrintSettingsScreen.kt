package com.warriortech.resb.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.warriortech.resb.data.local.entity.PrintTemplateEntity
import com.warriortech.resb.data.local.entity.PrintTemplateSectionEntity
import com.warriortech.resb.data.local.entity.PrintTemplateLineEntity
import com.warriortech.resb.ui.viewmodel.setting.PrintSettingsViewModel
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.data.local.entity.PrintTemplateColumnEntity
import androidx.compose.ui.text.style.TextAlign
import com.warriortech.resb.data.local.entity.KotSettingsEntity
import kotlinx.coroutines.flow.flowOf
import com.warriortech.resb.data.local.entity.PrintPlatformOverrideEntity
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Image

@Composable
fun PrintPreview(
    template: PrintTemplateEntity,
    sections: List<PrintTemplateSectionEntity>,
    viewModel: PrintSettingsViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(0.dp) // Paper look
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            sections.forEach { section ->
                val lines by viewModel.getLinesForSection(section.section_id).collectAsState(initial = emptyList())
                lines.forEach { line ->
                    val columns by viewModel.getColumnsForLine(line.line_id).collectAsState(initial = emptyList())
                    
                    if (line.field_key == "LOGO") {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "Logo Placeholder",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    } else if (line.field_key == "QRCODE") {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = "QR Code Placeholder",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (columns.isEmpty()) {
                                // If no columns, just show the field key as a full line
                                Text(
                                    text = "[ ${line.field_key} ]",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = when(line.align_type.uppercase()) {
                                        "RIGHT" -> TextAlign.End
                                        "CENTER" -> TextAlign.Center
                                        else -> TextAlign.Start
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                columns.forEach { column ->
                                    Text(
                                        text = "[ ${column.field_key} ]",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(column.width_pct.toFloat()),
                                        textAlign = when (column.align_type.uppercase()) {
                                            "RIGHT" -> TextAlign.End
                                            "CENTER" -> TextAlign.Center
                                            else -> TextAlign.Start
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }
                // Add a divider between sections for clarity
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
            
            Text(
                text = "--- End of Preview ---",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintSettingsScreen(
    viewModel: PrintSettingsViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val templates by viewModel.templates.collectAsState()
    val selectedTemplate by viewModel.selectedTemplate.collectAsState()
    val sections by viewModel.sections.collectAsState()
    
    var showAddTemplateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (selectedTemplate == null) "Print Customization" else "Template: ${selectedTemplate?.template_name}",
                        color = SurfaceLight
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedTemplate != null) {
                            viewModel.selectTemplate(null) // Deselect
                        } else {
                            onBackPressed()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SurfaceLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen),
                actions = {
                    if (selectedTemplate == null) {
                        IconButton(onClick = { showAddTemplateDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Template", tint = SurfaceLight)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (selectedTemplate == null) {
                TemplateList(templates, onTemplateClick = { viewModel.selectTemplate(it) })
            } else {
                val platformOverrides by viewModel.getPlatformOverrides(selectedTemplate!!.template_id).collectAsState(initial = emptyList())
                val kotSettings by viewModel.getKotSettings(selectedTemplate!!.template_id).collectAsState(initial = null)
                TemplateEditor(
                    selectedTemplate!!,
                    sections,
                    kotSettings,
                    platformOverrides,
                    viewModel
                )
            }
        }
    }

    if (showAddTemplateDialog) {
        AddTemplateDialog(
            onDismiss = { showAddTemplateDialog = false },
            onConfirm = { name, type, platform, width ->
                viewModel.addTemplate(name, type, platform, width)
                showAddTemplateDialog = false
            }
        )
    }
}

@Composable
fun TemplateList(templates: List<PrintTemplateEntity>, onTemplateClick: (PrintTemplateEntity) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(templates) { template ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTemplateClick(template) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = template.template_name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${template.document_type} • ${template.paper_width_mm}mm • ${template.platform}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun TemplateEditor(
    template: PrintTemplateEntity,
    sections: List<PrintTemplateSectionEntity>,
    kotSettings: KotSettingsEntity?,
    platformOverrides: List<PrintPlatformOverrideEntity>,
    viewModel: PrintSettingsViewModel
) {
    var showAddSectionDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Layout", modifier = Modifier.padding(8.dp))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Settings", modifier = Modifier.padding(8.dp))
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("Preview", modifier = Modifier.padding(8.dp))
            }
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sections) { section ->
                        SectionItem(section, viewModel)
                    }

                    item {
                        Button(
                            onClick = { showAddSectionDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Section")
                        }
                    }
                }
            }
            1 -> {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                    if (template.document_type == "KOT") {
                        Text("KOT Settings", style = MaterialTheme.typography.titleMedium)
                        KotSettingsView(template, kotSettings, viewModel)
                        Spacer(Modifier.height(16.dp))
                    }
                    Text("Platform Overrides", style = MaterialTheme.typography.titleMedium)
                    PlatformOverridesView(template, platformOverrides, viewModel)
                }
            }
            2 -> {
                Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                    PrintPreview(template, sections, viewModel)
                }
            }
        }
    }

    if (showAddSectionDialog) {
        var sectionName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddSectionDialog = false },
            title = { Text("Add Section") },
            text = {
                OutlinedTextField(
                    value = sectionName,
                    onValueChange = { sectionName = it },
                    label = { Text("Section Name (e.g., HEADER, BODY, FOOTER)") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addSection(template.template_id.toLong(), sectionName, sections.size)
                    showAddSectionDialog = false
                }) { Text("Add") }
            }
        )
    }
}

@Composable
fun ColumnBadge(column: PrintTemplateColumnEntity, viewModel: PrintSettingsViewModel) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.clickable { showEditDialog = true }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${column.field_key} (${column.width_pct}%)",
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Default.Close,
                contentDescription = "Delete Column",
                modifier = Modifier
                    .size(12.dp)
                    .clickable { showDeleteConfirm = true }
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete column '${column.field_key}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteColumn(column)
                    showDeleteConfirm = false
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showEditDialog) {
        var fieldKey by remember { mutableStateOf(column.field_key) }
        var width by remember { mutableStateOf(column.width_pct.toString()) }
        var align by remember { mutableStateOf(column.align_type) }
        var sortOrder by remember { mutableStateOf(column.sort_order.toString()) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Column") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(value = fieldKey, onValueChange = { fieldKey = it }, label = { Text("Field Key") })
                    OutlinedTextField(value = width, onValueChange = { width = it }, label = { Text("Width %") })
                    OutlinedTextField(value = align, onValueChange = { align = it }, label = { Text("Align (LEFT/CENTER/RIGHT)") })
                    OutlinedTextField(value = sortOrder, onValueChange = { sortOrder = it }, label = { Text("Sort Order") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateColumn(column.copy(
                        field_key = fieldKey, 
                        width_pct = width.toIntOrNull() ?: 100, 
                        align_type = align,
                        sort_order = sortOrder.toIntOrNull() ?: 0
                    ))
                    showEditDialog = false
                }) { Text("Update") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }
}


@Composable
fun SectionItem(section: PrintTemplateSectionEntity, viewModel: PrintSettingsViewModel) {
    val lines by viewModel.getLinesForSection(section.section_id).collectAsState(initial = emptyList())
    var showAddLineDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = section.section_type, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Section", tint = MaterialTheme.colorScheme.error)
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                lines.forEach { line ->
                    LineItem(line, viewModel)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.addLogo(section.section_id, lines.size) }) {
                    Icon(Icons.Default.Image, contentDescription = "Add Logo", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { viewModel.addQrCode(section.section_id, lines.size) }) {
                    Icon(Icons.Default.QrCode, contentDescription = "Add QR Code", modifier = Modifier.size(20.dp))
                }
                TextButton(
                    onClick = { showAddLineDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Add Line", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this section and all its contents?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSection(section)
                    showDeleteConfirm = false
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddLineDialog) {
        var lineName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddLineDialog = false },
            title = { Text("Add Line") },
            text = {
                OutlinedTextField(
                    value = lineName,
                    onValueChange = { lineName = it },
                    label = { Text("Line Name/Tag") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addLine(section.section_id.toLong(), lineName, lines.size)
                    showAddLineDialog = false
                }) { Text("Add") }
            }
        )
    }
}

@Composable
fun LineItem(line: PrintTemplateLineEntity, viewModel: PrintSettingsViewModel) {
    val columns by viewModel.getColumnsForLine(line.line_id).collectAsState(initial = emptyList())
    var showAddColumnDialog by remember { mutableStateOf(false) }
    var showEditLineDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DragHandle, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = line.field_key, 
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable { showEditLineDialog = true }
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Line", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
            }
            if (line.field_key != "LOGO" && line.field_key != "QRCODE") {
                IconButton(onClick = { showAddColumnDialog = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Column", modifier = Modifier.size(16.dp))
                }
            }
        }
        
        if (columns.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                columns.sortedBy { it.sort_order }.forEach { column ->
                    ColumnBadge(column, viewModel)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete line '${line.field_key}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteLine(line.line_id)
                    showDeleteConfirm = false
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showEditLineDialog) {
        var fieldKey by remember { mutableStateOf(line.field_key) }
        var displayText by remember { mutableStateOf(line.display_text ?: "") }
        var alignType by remember { mutableStateOf(line.align_type) }
        var fontSize by remember { mutableStateOf(line.font_size) }
        var isBold by remember { mutableStateOf(line.is_bold) }
        var isUnderline by remember { mutableStateOf(line.is_underline) }
        var sortOrder by remember { mutableStateOf(line.sort_order.toString()) }
        var isRepeatable by remember { mutableStateOf(line.is_repeatable) }

        AlertDialog(
            onDismissRequest = { showEditLineDialog = false },
            title = { Text("Edit Line") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(value = fieldKey, onValueChange = { fieldKey = it }, label = { Text("Field Key") })
                    OutlinedTextField(value = displayText, onValueChange = { displayText = it }, label = { Text("Display Text") })
                    OutlinedTextField(value = alignType, onValueChange = { alignType = it }, label = { Text("Align (LEFT/CENTER/RIGHT)") })
                    OutlinedTextField(value = fontSize, onValueChange = { fontSize = it }, label = { Text("Font Size (SMALL/NORMAL/LARGE)") })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isBold, onCheckedChange = { isBold = it })
                        Text("Bold")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isUnderline, onCheckedChange = { isUnderline = it })
                        Text("Underline")
                    }
                    OutlinedTextField(value = sortOrder, onValueChange = { sortOrder = it }, label = { Text("Sort Order") })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isRepeatable, onCheckedChange = { isRepeatable = it })
                        Text("Repeatable (for BODY items)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateLine(line.copy(
                        field_key = fieldKey,
                        display_text = displayText,
                        align_type = alignType,
                        font_size = fontSize,
                        is_bold = isBold,
                        is_underline = isUnderline,
                        sort_order = sortOrder.toIntOrNull() ?: 0,
                        is_repeatable = isRepeatable
                    ))
                    showEditLineDialog = false
                }) { Text("Update") }
            }
        )
    }

    if (showAddColumnDialog) {
        var fieldKey by remember { mutableStateOf("") }
        var width by remember { mutableStateOf("100") }
        var align by remember { mutableStateOf("LEFT") }
        
        AlertDialog(
            onDismissRequest = { showAddColumnDialog = false },
            title = { Text("Add Column") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = fieldKey, onValueChange = { fieldKey = it }, label = { Text("Field Key (e.g., ITEM_NAME)") })
                    OutlinedTextField(value = width, onValueChange = { width = it }, label = { Text("Width %") })
                    OutlinedTextField(value = align, onValueChange = { align = it }, label = { Text("Align (LEFT/CENTER/RIGHT)") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addColumn(line.line_id, fieldKey, width.toIntOrNull() ?: 100, align, columns.size)
                    showAddColumnDialog = false
                }) { Text("Add") }
            }
        )
    }
}

@Composable
fun KotSettingsView(template: PrintTemplateEntity, settings: KotSettingsEntity?, viewModel: PrintSettingsViewModel) {
    val currentSettings = settings ?: KotSettingsEntity(template_id = template.template_id)
    
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = currentSettings.print_item_notes, onCheckedChange = { viewModel.updateKotSettings(currentSettings.copy(print_item_notes = it)) })
            Text("Print Item Notes")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = currentSettings.print_order_time, onCheckedChange = { viewModel.updateKotSettings(currentSettings.copy(print_order_time = it)) })
            Text("Print Order Time")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = currentSettings.group_by_category, onCheckedChange = { viewModel.updateKotSettings(currentSettings.copy(group_by_category = it)) })
            Text("Group by Category")
        }
    }
}

@Composable
fun PlatformOverridesView(template: PrintTemplateEntity, overrides: List<PrintPlatformOverrideEntity>, viewModel: PrintSettingsViewModel) {
    overrides.forEach { override ->
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("${override.platform} - DPI: ${override.dpi ?: "Default"}")
            }
        }
    }
    Button(onClick = { viewModel.addPlatformOverride(PrintPlatformOverrideEntity(
        template_id = template.template_id, platform = "WINDOWS", dpi = 203,
        char_width = 10, supports_image = true, supports_qr = true,
    )) }) {
        Text("Add Windows Override")
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTemplateDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var docType by remember { mutableStateOf("BILL") }
    var platform by remember { mutableStateOf("ANDROID") }
    var width by remember { mutableStateOf("80") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Template") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                // Simple dropdowns could be here, using TextFields for speed
                OutlinedTextField(value = docType, onValueChange = { docType = it }, label = { Text("Doc Type (BILL/KOT)") })
                OutlinedTextField(value = width, onValueChange = { width = it }, label = { Text("Width (58/80)") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, docType, platform, width.toIntOrNull() ?: 80) }) {
                Text("Create")
            }
        }
    )
}
