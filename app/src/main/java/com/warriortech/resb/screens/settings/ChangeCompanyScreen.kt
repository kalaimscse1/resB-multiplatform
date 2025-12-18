package com.warriortech.resb.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.ui.components.MobileOptimizedTextField
import com.warriortech.resb.ui.components.MobilePasswordOptimizedTextField
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.ui.theme.SurfaceLight
import com.warriortech.resb.util.AnimatedSnackbarDemo


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChangeCompanyScreen(
    onBackPressed: () -> Unit,
    navController: NavController,
    sessionManager: SessionManager,
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var companyCode by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Change Company",
                        color = SurfaceLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.Default.ArrowBack, contentDescription = "Back",
                            tint = SurfaceLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen
                )
            )
        },
        snackbarHost = {
            AnimatedSnackbarDemo(snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {

                item {
                    MobileOptimizedTextField(
                        value = companyCode,
                        onValueChange = { companyCode = it },
                        label = "Email Id",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "CompanyCode",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
                item {
                    MobilePasswordOptimizedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "",
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image =
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            val description =
                                if (passwordVisible) "Hide password" else "Show password"

                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = description)
                            }
                        },
                    )
                }
                item {
                    Button(
                        onClick = {
                            showDialog = true
                        },
                        enabled = password == "kingtec2025#",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Company")
                    }
                }
            }
        }
    }
    if (showDialog) {
        ChangeDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                sessionManager.saveEmail(companyCode)
                showDialog = false
                navController.navigate("login") {
                    popUpTo("settings") { inclusive = true }
                }
            }
        )
    }
}

@Composable
fun ChangeDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Company") },
        text = { Text("Are you sure you want to Change Company Code?") },
        confirmButton = {
            TextButton(
                onClick = { onConfirm() },
                enabled = true
            ) {
                Text("Ok")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}