package com.warriortech.resb.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection

/**
 * Enhanced Scaffold specifically designed for Redmi/MIUI devices
 * Handles proper system bar padding and navigation button positioning
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedmiSafeScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = {
            // Wrap bottom bar with navigation bar padding for Redmi devices
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                bottomBar()
            }
        },
        snackbarHost = snackbarHost,
        floatingActionButton = {
            // Add navigation bar padding to FAB for proper positioning on Redmi devices
            Box(
                modifier = Modifier.navigationBarsPadding()
            ) {
                floatingActionButton()
            }
        },
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = WindowInsets(0) // Use our custom padding instead
    ) { paddingValues ->
        // Add system bars padding to content for edge-to-edge support
        val combinedPadding = PaddingValues(
            top = paddingValues.calculateTopPadding(),
            bottom = paddingValues.calculateBottomPadding() + 
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            start = paddingValues.calculateStartPadding(layoutDirection = LayoutDirection.Ltr),
            end = paddingValues.calculateEndPadding(layoutDirection = LayoutDirection.Ltr)
        )
        
        content(combinedPadding)
    }
}

/**
 * Simple extension for screens that need basic system bar handling
 */
@Composable
fun SafeColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.systemBarsPadding(),
        content = content
    )
}

/**
 * Safe area wrapper for content that needs to avoid system bars
 */
@Composable
fun SafeArea(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.systemBarsPadding(),
        content = content
    )
}