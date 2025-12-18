package com.warriortech.resb.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.warriortech.resb.model.*
import com.warriortech.resb.ui.theme.GradientStart
import com.warriortech.resb.ui.viewmodel.TemplateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditorScreen(
    navController: NavController,
    templateId: String,
    viewModel: TemplateViewModel = hiltViewModel()
) {
    val editingTemplate by viewModel.editingTemplate.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val template = editingTemplate ?: return
    LaunchedEffect(templateId) {
        val template = uiState.templates.find { it.id == templateId }
        viewModel.startEditingTemplate(template!!)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Template") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.cancelEditing()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveTemplate(template)
                            navController.popBackStack()
                        }
                    ) {
                        Text("Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GradientStart
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Template Name
            OutlinedTextField(
                value = template.name,
                onValueChange = {
                    viewModel.updateEditingTemplate(template.copy(name = it))
                },
                label = { Text("Template Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Template Type
            Text(
                text = "Template Type: ${template.type.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Header Settings
            HeaderSettingsSection(
                headerSettings = template.headerSettings,
                onHeaderSettingsChange = {
                    viewModel.updateEditingTemplate(template.copy(headerSettings = it))
                }
            )

            // Body Settings
            BodySettingsSection(
                bodySettings = template.bodySettings,
                onBodySettingsChange = {
                    viewModel.updateEditingTemplate(template.copy(bodySettings = it))
                }
            )

            // Footer Settings
            FooterSettingsSection(
                footerSettings = template.footerSettings,
                onFooterSettingsChange = {
                    viewModel.updateEditingTemplate(template.copy(footerSettings = it))
                }
            )

            // Paper Settings
            PaperSettingsSection(
                paperSettings = template.paperSettings,
                onPaperSettingsChange = {
                    viewModel.updateEditingTemplate(template.copy(paperSettings = it))
                }
            )
        }
    }
}

@Composable
fun HeaderSettingsSection(
    headerSettings: HeaderSettings,
    onHeaderSettingsChange: (HeaderSettings) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Header Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = headerSettings.showLogo,
                    onCheckedChange = {
                        onHeaderSettingsChange(headerSettings.copy(showLogo = it))
                    }
                )
                Text("Show Logo")
            }

            OutlinedTextField(
                value = headerSettings.businessName,
                onValueChange = {
                    onHeaderSettingsChange(headerSettings.copy(businessName = it))
                },
                label = { Text("Business Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = headerSettings.businessAddress,
                onValueChange = {
                    onHeaderSettingsChange(headerSettings.copy(businessAddress = it))
                },
                label = { Text("Business Address") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = headerSettings.businessPhone,
                onValueChange = {
                    onHeaderSettingsChange(headerSettings.copy(businessPhone = it))
                },
                label = { Text("Business Phone") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Font Size: ${headerSettings.fontSize}sp")
            Slider(
                value = headerSettings.fontSize.toFloat(),
                onValueChange = {
                    onHeaderSettingsChange(headerSettings.copy(fontSize = it.toInt()))
                },
                valueRange = 8f..24f,
                steps = 15
            )

            FontWeightDropdown(
                selectedWeight = headerSettings.fontWeight,
                onWeightChange = {
                    onHeaderSettingsChange(headerSettings.copy(fontWeight = it))
                }
            )

            TextAlignDropdown(
                selectedAlign = headerSettings.textAlign,
                onAlignChange = {
                    onHeaderSettingsChange(headerSettings.copy(textAlign = it))
                }
            )
        }
    }
}

@Composable
fun BodySettingsSection(
    bodySettings: BodySettings,
    onBodySettingsChange: (BodySettings) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Body Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = bodySettings.showItemDetails,
                    onCheckedChange = {
                        onBodySettingsChange(bodySettings.copy(showItemDetails = it))
                    }
                )
                Text("Show Item Details")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = bodySettings.showQuantity,
                    onCheckedChange = {
                        onBodySettingsChange(bodySettings.copy(showQuantity = it))
                    }
                )
                Text("Show Quantity")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = bodySettings.showPrice,
                    onCheckedChange = {
                        onBodySettingsChange(bodySettings.copy(showPrice = it))
                    }
                )
                Text("Show Price")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = bodySettings.showBorders,
                    onCheckedChange = {
                        onBodySettingsChange(bodySettings.copy(showBorders = it))
                    }
                )
                Text("Show Borders")
            }

            Text("Font Size: ${bodySettings.fontSize}sp")
            Slider(
                value = bodySettings.fontSize.toFloat(),
                onValueChange = {
                    onBodySettingsChange(bodySettings.copy(fontSize = it.toInt()))
                },
                valueRange = 8f..20f,
                steps = 11
            )

            FontWeightDropdown(
                selectedWeight = bodySettings.fontWeight,
                onWeightChange = {
                    onBodySettingsChange(bodySettings.copy(fontWeight = it))
                }
            )

            Text("Line Spacing: ${bodySettings.lineSpacing}dp")
            Slider(
                value = bodySettings.lineSpacing.toFloat(),
                onValueChange = {
                    onBodySettingsChange(bodySettings.copy(lineSpacing = it.toInt()))
                },
                valueRange = 1f..8f,
                steps = 6
            )
        }
    }
}

@Composable
fun FooterSettingsSection(
    footerSettings: FooterSettings,
    onFooterSettingsChange: (FooterSettings) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Footer Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = footerSettings.showThankYou,
                    onCheckedChange = {
                        onFooterSettingsChange(footerSettings.copy(showThankYou = it))
                    }
                )
                Text("Show Thank You")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = footerSettings.showDateTime,
                    onCheckedChange = {
                        onFooterSettingsChange(footerSettings.copy(showDateTime = it))
                    }
                )
                Text("Show Date/Time")
            }

            OutlinedTextField(
                value = footerSettings.customMessage,
                onValueChange = {
                    onFooterSettingsChange(footerSettings.copy(customMessage = it))
                },
                label = { Text("Custom Message") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Font Size: ${footerSettings.fontSize}sp")
            Slider(
                value = footerSettings.fontSize.toFloat(),
                onValueChange = {
                    onFooterSettingsChange(footerSettings.copy(fontSize = it.toInt()))
                },
                valueRange = 8f..16f,
                steps = 7
            )

            FontWeightDropdown(
                selectedWeight = footerSettings.fontWeight,
                onWeightChange = {
                    onFooterSettingsChange(footerSettings.copy(fontWeight = it))
                }
            )

            TextAlignDropdown(
                selectedAlign = footerSettings.textAlign,
                onAlignChange = {
                    onFooterSettingsChange(footerSettings.copy(textAlign = it))
                }
            )
        }
    }
}

@Composable
fun PaperSettingsSection(
    paperSettings: PaperSettings,
    onPaperSettingsChange: (PaperSettings) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Paper Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            PaperSizeDropdown(
                selectedSize = paperSettings.paperSize,
                onSizeChange = {
                    onPaperSettingsChange(paperSettings.copy(paperSize = it))
                }
            )

            Text("Character Width: ${paperSettings.characterWidth}")
            Slider(
                value = paperSettings.characterWidth.toFloat(),
                onValueChange = {
                    onPaperSettingsChange(paperSettings.copy(characterWidth = it.toInt()))
                },
                valueRange = 24f..80f,
                steps = 55
            )

            Text("Margins")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Top: ${paperSettings.margins.top}dp")
                    Slider(
                        value = paperSettings.margins.top.toFloat(),
                        onValueChange = {
                            onPaperSettingsChange(
                                paperSettings.copy(
                                    margins = paperSettings.margins.copy(top = it.toInt())
                                )
                            )
                        },
                        valueRange = 0f..20f,
                        steps = 19,
                        modifier = Modifier.width(120.dp)
                    )
                }
                Column {
                    Text("Bottom: ${paperSettings.margins.bottom}dp")
                    Slider(
                        value = paperSettings.margins.bottom.toFloat(),
                        onValueChange = {
                            onPaperSettingsChange(
                                paperSettings.copy(
                                    margins = paperSettings.margins.copy(bottom = it.toInt())
                                )
                            )
                        },
                        valueRange = 0f..20f,
                        steps = 19,
                        modifier = Modifier.width(120.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontWeightDropdown(
    selectedWeight: FontWeights,
    onWeightChange: (FontWeights) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val weights = FontWeights.values()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedWeight.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Font Weight") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            weights.forEach { weight ->
                DropdownMenuItem(
                    text = { Text(weight.name) },
                    onClick = {
                        onWeightChange(weight)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextAlignDropdown(
    selectedAlign: TextAligns,
    onAlignChange: (TextAligns) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val alignments = TextAligns.values()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedAlign.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Text Align") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            alignments.forEach { align ->
                DropdownMenuItem(
                    text = { Text(align.name) },
                    onClick = {
                        onAlignChange(align)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaperSizeDropdown(
    selectedSize: PaperSize,
    onSizeChange: (PaperSize) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val sizes = PaperSize.values()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedSize.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Paper Size") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            sizes.forEach { size ->
                DropdownMenuItem(
                    text = { Text(size.displayName) },
                    onClick = {
                        onSizeChange(size)
                        expanded = false
                    }
                )
            }
        }
    }
}
