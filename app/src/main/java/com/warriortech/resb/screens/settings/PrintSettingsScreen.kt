package com.warriortech.resb.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Print Customization", color = SurfaceLight) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SurfaceLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen),
                actions = {
                    IconButton(onClick = { /* Add template logic */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Template", tint = SurfaceLight)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(templates) { template ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = template.template_name, style = MaterialTheme.typography.titleMedium)
                        Text(text = "${template.document_type} - ${template.paper_width_mm}mm", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
