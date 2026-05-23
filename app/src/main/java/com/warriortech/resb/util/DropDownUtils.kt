package com.warriortech.resb.util

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.warriortech.resb.model.Area
import com.warriortech.resb.model.KitchenCategory
import com.warriortech.resb.model.Menu
import com.warriortech.resb.model.MenuCategory
import com.warriortech.resb.model.Role
import com.warriortech.resb.model.Tax
import com.warriortech.resb.model.TblCounter
import com.warriortech.resb.model.TblCustomer
import com.warriortech.resb.model.TblGroupDetails
import com.warriortech.resb.model.TblGroupNature
import com.warriortech.resb.model.TblLedgerDetails
import com.warriortech.resb.model.TblLedgerDetailsIdResponse
import com.warriortech.resb.model.TblUnit
import com.warriortech.resb.model.TblVoucherType
import kotlin.collections.forEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaDropdown(
    areas: List<Area>,
    selectedArea: Area?,
    onAreaSelected: (Area) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Area"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedArea?.area_name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (areas.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No areas available") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                areas.forEach { area ->
                    DropdownMenuItem(
                        text = { Text(area.area_name ?: "") },
                        onClick = {
                            onAreaSelected(area)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxDropdown(
    taxes: List<Tax>,
    selectedTax: Tax?,
    onTaxSelected: (Tax) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Tax"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedTax?.tax_name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (taxes.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No Taxes available") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                taxes.forEach { tax ->
                    DropdownMenuItem(
                        text = { Text(tax.tax_name ?: "") },
                        onClick = {
                            onTaxSelected(tax)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterDropdown(
    counters: List<TblCounter>,
    selectedCounter: TblCounter?,
    onCounterSelected: (TblCounter) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Counter"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCounter?.counter_name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (counters.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No Counter available") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                counters.forEach { counter ->
                    DropdownMenuItem(
                        text = { Text(counter.counter_name ?: "") },
                        onClick = {
                            onCounterSelected(counter)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherTypeDropdown(
    voucherTypes: List<TblVoucherType>,
    selectedVoucherType: TblVoucherType?,
    onVoucherTypeSelected: (TblVoucherType) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select VoucherType"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedVoucherType?.voucher_type_name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (voucherTypes.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No VoucherType available") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                voucherTypes.forEach { counter ->
                    DropdownMenuItem(
                        text = { Text(counter.voucher_type_name ?: "") },
                        onClick = {
                            onVoucherTypeSelected(counter)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDropdown(
    customers: List<TblCustomer>,
    selectedCustomer: TblCustomer?,
    onCustomerSelected: (TblCustomer) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Customer"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCustomer?.customer_name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (customers.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No Customer available") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                customers.forEach { counter ->
                    DropdownMenuItem(
                        text = { Text(counter.customer_name ?: "") },
                        onClick = {
                            onCustomerSelected(counter)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDropdown(
    roles: List<Role>,
    selectedRole: Role?,
    onRoleSelected: (Role) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Role"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedRole?.role ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (roles.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No Roles available") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                roles.forEach { counter ->
                    DropdownMenuItem(
                        text = { Text(counter.role ?: "") },
                        onClick = {
                            onRoleSelected(counter)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuDropdown(
    menus: List<Menu>,
    selectedMenu: Menu?,
    onMenuSelected: (Menu) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Menu"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedMenu?.menu_name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (menus.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No Menus available") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                menus.forEach { counter ->
                    DropdownMenuItem(
                        text = { Text(counter.menu_name ?: "") },
                        onClick = {
                            onMenuSelected(counter)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuCategoryDropdown(
    menus: List<MenuCategory>,
    selectedMenuCategory: MenuCategory?,
    onMenuCategorySelected: (MenuCategory) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Category"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedMenuCategory?.item_cat_name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (menus.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No Categories available") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                menus.forEach { counter ->
                    DropdownMenuItem(
                        text = { Text(counter.item_cat_name ?: "") },
                        onClick = {
                            onMenuCategorySelected(counter)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenGroupDropdown(
    menus: List<KitchenCategory>,
    selectedKitchenCategory: KitchenCategory?,
    onKitchenCategorySelected: (KitchenCategory) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Kitchen Group"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedKitchenCategory?.kitchen_cat_name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (menus.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No Kitchen Groups available") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                menus.forEach { counter ->
                    DropdownMenuItem(
                        text = { Text(counter.kitchen_cat_name ?: "") },
                        onClick = {
                            onKitchenCategorySelected(counter)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDropdown(
    menus: List<TblUnit>,
    selectedUnit: TblUnit?,
    onUnitSelected: (TblUnit) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Unit"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedUnit?.unit_name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (menus.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No Units available") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                menus.forEach { counter ->
                    DropdownMenuItem(
                        text = { Text(counter.unit_name ?: "") },
                        onClick = {
                            onUnitSelected(counter)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StringDropdown(
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select an Option"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No options available") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option ?: "") },
                        onClick = {
                            onOptionSelected(option ?: "")
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupNatureDropdown(
    groupNatures: List<TblGroupNature>,
    selectedGroupNature: TblGroupNature?,
    onGroupNatureSelected: (TblGroupNature) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select GroupNature"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedGroupNature?.g_nature_name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (groupNatures.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No GroupNature available") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                groupNatures.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group.g_nature_name ?: "") },
                        onClick = {
                            onGroupNatureSelected(group)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDropdown(
    groups: List<TblGroupDetails>,
    selectedGroup: TblGroupDetails?,
    onGroupSelected: (TblGroupDetails) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Group"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedGroup?.group_name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (groups.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No Groups available") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                groups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group.group_name ?: "") },
                        onClick = {
                            onGroupSelected(group)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerDropdown(
    ledgers: List<TblLedgerDetails>,
    selectedLedger: TblLedgerDetails?,
    onLedgerSelected: (TblLedgerDetails) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Ledger"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedLedger?.ledger_name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (ledgers.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No Ledgers available") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                ledgers.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group.ledger_name ?: "") },
                        onClick = {
                            onLedgerSelected(group)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerDetailsEntryDropdown(
    ledgers: List<TblLedgerDetailsIdResponse>,
    selectedLedger: TblLedgerDetailsIdResponse?,
    onLedgerSelected: (TblLedgerDetailsIdResponse) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Entry No"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedLedger?.member_id ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (ledgers.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No Entries available") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                ledgers.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group.member_id ?: "") },
                        onClick = {
                            onLedgerSelected(group)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
