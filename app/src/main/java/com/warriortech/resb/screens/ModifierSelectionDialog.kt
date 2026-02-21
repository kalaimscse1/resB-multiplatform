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
    var manualModifier by remember { 
        mutableStateOf(selectedModifiers.find { it.add_on_id == -1L }?.add_on_name ?: "") 
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
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

                // Manual Input
                OutlinedTextField(
                    value = manualModifier,
                    onValueChange = { manualModifier = it },
                    label = { Text("Manual Instructions") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    placeholder = { Text("e.g. Less Spicy, No Onions") }
                )

                // AddOn List
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
                            val finalModifiers = currentSelectedModifiers.toMutableList()
                            if (manualModifier.isNotBlank()) {
                                finalModifiers.add(
                                    Modifiers(
                                        add_on_id = -1L,
                                        add_on_name = manualModifier,
                                        add_on_price = 0.0,
                                        item_cat = availableModifiers.firstOrNull()?.item_cat ?: menuItem.toFakeCategory()
                                    )
                                )
                            }
                            onModifiersSelected(finalModifiers)
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

// Helper to handle cases where item_cat is needed but not available
fun TblMenuItemResponse.toFakeCategory() = com.warriortech.resb.model.MenuCategory(
    item_cat_id = this.item_cat_id,
    item_cat_name = this.item_cat_name,
    order_by = "0",
    is_active = true
)

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
                .padding(12.dp),
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

                if (modifier.add_on_price != 0.0) {
                    Text(
                        text = "+₹${modifier.add_on_price}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
