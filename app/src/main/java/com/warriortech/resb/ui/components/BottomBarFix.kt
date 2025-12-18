package com.warriortech.resb.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Redmi-safe BottomAppBar wrapper that handles navigation bar padding
 * Specifically designed to fix navigation button issues on MIUI devices
 */
@Composable
fun RedmiSafeBottomAppBar(
    modifier: Modifier = Modifier,
    containerColor: Color = BottomAppBarDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: androidx.compose.ui.unit.Dp = BottomAppBarDefaults.ContainerElevation,
    contentPadding: PaddingValues = BottomAppBarDefaults.ContentPadding,
    windowInsets: WindowInsets = BottomAppBarDefaults.windowInsets,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {}
) {
    // Add navigation bar padding to ensure buttons are not hidden on Redmi devices
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        BottomAppBar(
            containerColor = containerColor,
            contentColor = contentColor,
            tonalElevation = tonalElevation,
            contentPadding = contentPadding,
            windowInsets = WindowInsets(0), // Disable default insets since we handle them manually
            actions = actions,
            floatingActionButton = floatingActionButton
        )
    }
}

/**
 * Redmi-safe FloatingActionButton that handles proper positioning
 */
@Composable
fun RedmiSafeFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = FloatingActionButtonDefaults.shape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.navigationBarsPadding()
    ) {
        FloatingActionButton(
            onClick = onClick,
            shape = shape,
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = elevation,
            interactionSource = interactionSource,
            content = content
        )
    }
}