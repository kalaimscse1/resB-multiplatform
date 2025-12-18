package com.warriortech.resb.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.warriortech.resb.ui.theme.GradientEnd
import com.warriortech.resb.ui.theme.GradientStart

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    startColor: Color = GradientStart,
    endColor: Color = GradientEnd,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(startColor, endColor)
                )
            )
    ) {
        content()
    }
}

@Composable
fun CardGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFF8F9FA)
                    )
                )
            )
    ) {
        content()
    }
}
