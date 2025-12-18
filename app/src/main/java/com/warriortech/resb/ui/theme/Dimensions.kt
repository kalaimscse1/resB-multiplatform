package com.warriortech.resb.ui.theme

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun getScreenSizeInfo(): ScreenSizeInfo {
    val configuration = LocalConfiguration.current
    return ScreenSizeInfo(
        hPX = configuration.screenHeightDp.dp,
        wPX = configuration.screenWidthDp.dp
    )
}

data class ScreenSizeInfo(
    val hPX: Dp,
    val wPX: Dp
) {
    val isCompact: Boolean get() = wPX < 600.dp
    val isMedium: Boolean get() = wPX >= 600.dp && wPX < 840.dp
    val isExpanded: Boolean get() = wPX >= 840.dp
    val isLandscape: Boolean get() = wPX > hPX
}


object Dimensions {
    // Spacing
    val spacingXS = 4.dp
    val spacingS = 8.dp
    val spacingM = 16.dp
    val spacingL = 24.dp
    val spacingXL = 32.dp
    val spacingXXL = 48.dp

    // Corner Radius
    val cornerRadiusS = 4.dp
    val cornerRadiusM = 8.dp
    val cornerRadiusL = 12.dp
    val cornerRadiusXL = 16.dp
    val cornerRadiusXXL = 24.dp

    // Elevation
    val elevationS = 2.dp
    val elevationM = 4.dp
    val elevationL = 8.dp
    val elevationXL = 12.dp

    // Touch Targets
    val touchTargetMinimum = 48.dp
    val touchTargetComfortable = 56.dp

    // Icon Sizes
    val iconS = 16.dp
    val iconM = 24.dp
    val iconL = 32.dp
    val iconXL = 48.dp

    // Component Heights
    val buttonHeight = 48.dp
    val inputHeight = 56.dp
    val appBarHeight = 64.dp
    val bottomNavHeight = 80.dp

    // Layout
    val screenPadding = 16.dp
    val cardPadding = 16.dp
    val listItemPadding = 16.dp
    val SpaceExtraSmall = 4.dp
    val SpaceSmall = 8.dp
    val SpaceMedium = 16.dp
    val SpaceLarge = 24.dp
    val SpaceExtraLarge = 32.dp
    val SpaceHuge = 48.dp

    // Card and Surface
    val CardElevation = 8.dp
    val CardCornerRadius = 12.dp
    val ButtonCornerRadius = 8.dp


    // Component Widths
    val MinButtonWidth = 88.dp
    val IconSize = 24.dp
    val IconSizeLarge = 32.dp

    // Padding
    val ContentPadding = 12.dp

    @Composable
    fun getHorizontalPadding(): Dp {
        val screenInfo = getScreenSizeInfo()
        return when {
            screenInfo.isCompact -> spacingM
            screenInfo.isMedium -> spacingL
            else -> spacingXL
        }
    }

    @Composable
    fun getVerticalPadding(): Dp {
        val screenInfo = getScreenSizeInfo()
        return when {
            screenInfo.isCompact -> spacingS
            else -> spacingM
        }
    }
}

