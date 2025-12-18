package com.warriortech.resb.util

import android.content.Context
import android.content.res.Configuration
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object MobileUtils {

    fun isTablet(context: Context): Boolean {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val density = displayMetrics.density
        val dpHeight = displayMetrics.heightPixels / density
        val dpWidth = displayMetrics.widthPixels / density

        // Enhanced tablet detection
        val smallestWidth = minOf(dpWidth, dpHeight)
        return smallestWidth >= 600
    }

    fun isLargeTablet(context: Context): Boolean {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val density = displayMetrics.density
        val dpHeight = displayMetrics.heightPixels / density
        val dpWidth = displayMetrics.widthPixels / density

        val smallestWidth = minOf(dpWidth, dpHeight)
        return smallestWidth >= 840
    }

    fun isLandscape(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    fun getScreenDensity(context: Context): Float {
        return context.resources.displayMetrics.density
    }

    fun getOptimalColumnCount(context: Context): Int {
        return when {
            isLargeTablet(context) -> if (isLandscape(context)) 4 else 3
            isTablet(context) -> if (isLandscape(context)) 3 else 2
            else -> if (isLandscape(context)) 2 else 1
        }
    }

    fun getOptimalGridSpacing(context: Context): Dp {
        return when {
            isLargeTablet(context) -> 16.dp
            isTablet(context) -> 12.dp
            else -> 8.dp
        }
    }
}

@Composable
fun pxToDp(px: Int): Dp {
    val density = LocalDensity.current
    return with(density) { px.toDp() }
}

@Composable
fun dpToPx(dp: Dp): Float {
    val density = LocalDensity.current
    return with(density) { dp.toPx() }
}

@Composable
fun getDeviceInfo(): DeviceInfo {
    val context = LocalContext.current
    return remember {
        DeviceInfo(
            isTablet = MobileUtils.isTablet(context),
            isLargeTablet = MobileUtils.isLargeTablet(context),
            isLandscape = MobileUtils.isLandscape(context),
            density = MobileUtils.getScreenDensity(context),
            optimalColumnCount = MobileUtils.getOptimalColumnCount(context),
            optimalSpacing = MobileUtils.getOptimalGridSpacing(context)
        )
    }
}

@Composable
fun getScreenSizeInfo(): ScreenSizeInfo {
    val context = LocalContext.current
    return remember {
        ScreenSizeInfo(
            isCompact = !MobileUtils.isTablet(context),
            isMedium = MobileUtils.isTablet(context) && !MobileUtils.isLargeTablet(context),
            isExpanded = MobileUtils.isLargeTablet(context),
            isLandscape = MobileUtils.isLandscape(context)
        )
    }
}

data class DeviceInfo(
    val isTablet: Boolean,
    val isLargeTablet: Boolean,
    val isLandscape: Boolean,
    val density: Float,
    val optimalColumnCount: Int,
    val optimalSpacing: Dp
)

data class ScreenSizeInfo(
    val isCompact: Boolean,
    val isMedium: Boolean,
    val isExpanded: Boolean,
    val isLandscape: Boolean
)
