package com.warriortech.resb.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * System bar utilities specifically designed for Redmi/MIUI devices
 * Handles proper padding and insets for navigation buttons and system bars
 */

@Composable
fun getStatusBarHeight(): Dp {
    return WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
}

@Composable
fun getNavigationBarHeight(): Dp {
    return WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
}

@Composable
fun getSystemBarsPadding() = WindowInsets.systemBars.asPaddingValues()

/**
 * Modifier to add safe system bars padding
 * Crucial for Redmi devices with custom navigation bars
 */
fun Modifier.systemBarsPadding(): Modifier = composed {
    val systemBarsPadding = getSystemBarsPadding()
    this.then(
        Modifier.padding(systemBarsPadding)
    )
}

/**
 * Modifier to add only navigation bar padding (bottom)
 * Useful for buttons and bottom content
 */
fun Modifier.navigationBarsPadding(): Modifier = composed {
    val navigationPadding = WindowInsets.navigationBars.asPaddingValues()
    this.then(
        Modifier.padding(bottom = navigationPadding.calculateBottomPadding())
    )
}

/**
 * Modifier to add only status bar padding (top)
 * Useful for top app bars and header content
 */
fun Modifier.statusBarsPadding(): Modifier = composed {
    val statusPadding = WindowInsets.statusBars.asPaddingValues()
    this.then(
        Modifier.padding(top = statusPadding.calculateTopPadding())
    )
}

/**
 * Check if device is running MIUI (Redmi devices)
 * Useful for applying device-specific fixes
 */
fun isMIUI(): Boolean {
    return try {
        val property = System.getProperty("ro.miui.ui.version.name")
        !property.isNullOrEmpty()
    } catch (e: Exception) {
        false
    }
}

/**
 * Get safe area insets for Redmi devices
 * Handles different navigation bar configurations
 */
@Composable
fun getSafeAreaInsets() = with(LocalDensity.current) {
    val systemBars = WindowInsets.systemBars
    object {
        val top = systemBars.getTop(this@with).toDp()
        val bottom = systemBars.getBottom(this@with).toDp()
        val left = systemBars.getLeft(this@with, layoutDirection = LayoutDirection.Ltr ).toDp()
        val right = systemBars.getRight(this@with, layoutDirection = LayoutDirection.Rtl).toDp()
    }
}