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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.warriortech.resb.data.local.entity.PrintTemplateEntity
import com.warriortech.resb.data.local.entity.PrintTemplateSectionEntity
import com.warriortech.resb.data.local.entity.PrintTemplateLineEntity
import com.warriortech.resb.ui.viewmodel.setting.PrintSettingsViewModel
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight

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
                            viewModel.selectTemplate(null as PrintTemplateEntity?) // Deselect
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
                TemplateEditor(
                    selectedTemplate!!,
                    sections,
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
    viewModel: PrintSettingsViewModel
) {
    var showAddSectionDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
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
                    viewModel.addSection(template.id, sectionName, sections.size)
                    showAddSectionDialog = false
                }) { Text("Add") }
            }
        )
    }
}

@Composable
fun SectionItem(section: PrintTemplateSectionEntity, viewModel: PrintSettingsViewModel) {
    val lines by viewModel.getLinesForSection(section.id).collectAsState(initial = emptyList())
    var showAddLineDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = section.section_name, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { viewModel.deleteSection(section) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Section", tint = MaterialTheme.colorScheme.error)
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                lines.forEach { line ->
                    LineItem(line)
                }
            }

            TextButton(
                onClick = { showAddLineDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Add Line", style = MaterialTheme.typography.labelMedium)
            }
        }
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
                    viewModel.addLine(section.id, lineName, lines.size)
                    showAddLineDialog = false
                }) { Text("Add") }
            }
        )
    }
}

@Composable
fun LineItem(line: PrintTemplateLineEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.DragHandle, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text = line.line_name, style = MaterialTheme.typography.bodySmall)
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
