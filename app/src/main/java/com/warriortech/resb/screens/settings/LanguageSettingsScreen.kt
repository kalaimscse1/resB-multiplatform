package com.warriortech.resb.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.warriortech.resb.R
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.util.LocaleHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf(LocaleHelper.getLanguage(context)) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingLanguage by remember { mutableStateOf("") }

    val availableLanguages = remember { LocaleHelper.getAvailableLanguages() }
    val currentLocale = remember { LocaleHelper.getCurrentLocale(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.language_settings),
                        color = SurfaceLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack, contentDescription = "Back",
                            tint = SurfaceLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen
                ),
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Language,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.select_language),
                    style = MaterialTheme.typography.h6
                )
            }

            // English Option
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (selectedLanguage == "en"),
                        onClick = {
                            pendingLanguage = "en"
                            showConfirmDialog = true
                        },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedLanguage == "en"),
                    onClick = null
                )
                Text(
                    text = stringResource(R.string.english),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            // Tamil Option
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (selectedLanguage == "ta"),
                        onClick = {
                            pendingLanguage = "ta"
                            showConfirmDialog = true
                        },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedLanguage == "ta"),
                    onClick = null
                )
                Text(
                    text = stringResource(R.string.tamil),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Language") },
            text = { Text("Are you sure you want to change the language? The app will restart.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedLanguage = pendingLanguage
                        LocaleHelper.setLocale(context, pendingLanguage)
                        showConfirmDialog = false
                        // Restart the activity to apply the language change
                        (context as? android.app.Activity)?.recreate()
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
