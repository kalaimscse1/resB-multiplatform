package com.warriortech.resb.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warriortech.resb.ui.components.MobileOptimizedCard
import com.warriortech.resb.ui.components.MobileOptimizedButton
import com.warriortech.resb.ui.viewmodel.AIAssistantViewModel

/**
 * Screen for the AI Assistant feature in the restaurant management application.
 * This screen allows users to interact with various AI-powered features such as menu enhancement,
 * smart upselling, sales analytics, and customer recommendations.
 * It also provides a dialog for configuring the OpenAI API key required for these features.
 *
 * @param onBackPressed Callback to handle back navigation.
 * @param viewModel The [AIAssistantViewModel] instance to manage the AI assistant state.
 * @OptIn ExperimentalMaterial3Api is used for Material 3 components.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(
    onBackPressed: () -> Unit,
    viewModel: AIAssistantViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var apiKey by remember { mutableStateOf("") }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "AI Restaurant Assistant",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showApiKeyDialog = true }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Features Grid
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MobileOptimizedCard {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Menu Enhancement",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Generate appealing descriptions for your menu items")
                        Spacer(modifier = Modifier.height(12.dp))
                        MobileOptimizedButton(
                            onClick = { viewModel.enhanceMenuDescriptions() },
                            enabled = !uiState.isLoading,
                            text = "Enhance Menu"
                        )
                    }
                }
            }

            item {
                MobileOptimizedCard {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Smart Upselling",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Get AI-powered suggestions to increase order value")
                        Spacer(modifier = Modifier.height(12.dp))
                        MobileOptimizedButton(
                            onClick = { viewModel.generateUpsellSuggestions() },
                            enabled = !uiState.isLoading,
                            text = "Get Suggestions"
                        )
                    }
                }
            }

            item {
                MobileOptimizedCard {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Sales Analytics",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("AI-powered insights from your sales data")
                        Spacer(modifier = Modifier.height(12.dp))
                        MobileOptimizedButton(
                            onClick = { viewModel.analyzeSalesData() },
                            enabled = !uiState.isLoading,
                            text = "Analyze Sales"
                        )
                    }
                }
            }

            item {
                MobileOptimizedCard {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Recommend,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Customer Recommendations",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Personalized recommendations based on order history")
                        Spacer(modifier = Modifier.height(12.dp))
                        MobileOptimizedButton(
                            onClick = { viewModel.generateCustomerRecommendations() },
                            enabled = !uiState.isLoading,
                            text = "Get Recommendations"
                        )
                    }
                }
            }

            // Results Display
            if (uiState.results.isNotEmpty()) {
                item {
                    MobileOptimizedCard {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "AI Results",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            uiState.results.forEach { result ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = result,
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }

    // API Key Configuration Dialog
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("Configure OpenAI API Key") },
            text = {
                Column {
                    Text("Enter your OpenAI API key to enable AI features:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                MobileOptimizedButton(
                    onClick = {
                        apiKey =
                            "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"
                        viewModel.setApiKey(apiKey)
                        showApiKeyDialog = false
                    },
                    text = "Save"
                )
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
