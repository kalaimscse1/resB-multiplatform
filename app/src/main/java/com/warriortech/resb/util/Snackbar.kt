package com.warriortech.resb.util

import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.warriortech.resb.ui.theme.DarkGreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedSnackbarDemo(snackbarHostState: SnackbarHostState) {
    SnackbarHost(
        hostState = snackbarHostState
    ) { data ->
        // Animated visibility tied to whether snackbar is shown
        AnimatedVisibility(
            visible = true, // Always visible while host is rendering current snackbar
            enter = slideInVertically(
                initialOffsetY = { it }, // slide up from bottom
                animationSpec = tween(300)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it }, // slide down
                animationSpec = tween(300)
            ) + fadeOut()
        ) {
            Snackbar(
                snackbarData = data,
                containerColor = Color.White,
                contentColor = DarkGreen,
                actionColor = Color.Yellow,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

