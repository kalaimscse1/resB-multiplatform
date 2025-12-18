package com.warriortech.resb.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.warriortech.resb.model.Modifiers
import com.warriortech.resb.model.TblMenuItemResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifierSelectionDialog(
    menuItem: TblMenuItemResponse,
    availableModifiers: List<Modifiers>,
    selectedModifiers: List<Modifiers>,
    onModifiersSelected: (List<Modifiers>) -> Unit,
    onDismiss: () -> Unit
) {
    var currentSelectedModifiers by remember { mutableStateOf(selectedModifiers) }

    val totalPrice = remember(currentSelectedModifiers) {
        menuItem.rate + currentSelectedModifiers.sumOf { it.add_on_price }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = "Customize ${menuItem.menu_item_name}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // AddOn List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableModifiers) { modifier ->
                        ModifierItem(
                            modifier = modifier,
                            isSelected = currentSelectedModifiers.contains(modifier),
                            onSelectionChanged = { isSelected ->
                                currentSelectedModifiers = if (isSelected) {
                                    currentSelectedModifiers + modifier
                                } else {
                                    currentSelectedModifiers - modifier
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
//
//                // Total Price
//                Text(
//                    text = "Total: ₹%.2f".format(totalPrice),
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.primary
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onModifiersSelected(currentSelectedModifiers)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add to Order")
                    }
                }
            }
        }
    }
}

@Composable
fun ModifierSelectionDialog(
    availableModifiers: List<Modifiers>,
    selectedModifiers: List<Modifiers>,
    onModifiersSelected: (List<Modifiers>) -> Unit,
    onDismiss: () -> Unit
) {
    var currentSelectedModifiers by remember { mutableStateOf(selectedModifiers) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select AddOn",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableModifiers) { modifier ->
                        ModifierItem(
                            modifier = modifier,
                            isSelected = currentSelectedModifiers.any { it.add_on_id == modifier.add_on_id },
                            onSelectionChanged = { isSelected ->
                                currentSelectedModifiers = if (isSelected) {
                                    currentSelectedModifiers + modifier
                                } else {
                                    currentSelectedModifiers.filter { it.add_on_id != modifier.add_on_id }
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onModifiersSelected(currentSelectedModifiers)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun ModifierItem(
    modifier: Modifiers,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = { onSelectionChanged(!isSelected) }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChanged
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = modifier.add_on_name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

//                if (modifier.modifier_name_tamil.isNotEmpty()) {
//                    Text(
//                        text = modifier.modifier_name_tamil,
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
//                    val typeText = when (modifier.modifier_type) {
//                        ModifierType.ADDITION -> "Addition"
//                        ModifierType.REMOVAL -> "Remove"
//                        ModifierType.SUBSTITUTION -> "Substitute"
//                    }
//
//                    Text(
//                        text = typeText,
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.secondary
//                    )

                    if (modifier.add_on_price != 0.0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (modifier.add_on_price > 0)
                                "+₹${modifier.add_on_price}"
                            else
                                "₹${modifier.add_on_price}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (modifier.add_on_price > 0)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
