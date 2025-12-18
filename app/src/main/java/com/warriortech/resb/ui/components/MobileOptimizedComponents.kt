package com.warriortech.resb.ui.components

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.util.getDeviceInfo
import com.warriortech.resb.util.getScreenSizeInfo

@Composable
fun MobileOptimizedCard(
    onClick: (() -> Unit)? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    elevated: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val deviceInfo = getDeviceInfo()
    val cornerRadius = if (deviceInfo.isTablet) 24.dp else 20.dp
    val elevation = if (elevated) {
        if (deviceInfo.isTablet) 16.dp else 12.dp
    } else {
        if (deviceInfo.isTablet) 6.dp else 4.dp
    }
    val padding = if (deviceInfo.isTablet) 24.dp else 5.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        indication = ripple(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onClick() }
                } else Modifier
            ),
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        )
                    )
                )
                .padding(padding),
            content = content
        )
    }
}

@Composable
fun MobileOptimizedCardLogin(
    onClick: (() -> Unit)? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    elevated: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val deviceInfo = getDeviceInfo()
    val cornerRadius = if (deviceInfo.isTablet) 24.dp else 20.dp
    val elevation = if (elevated) {
        if (deviceInfo.isTablet) 16.dp else 12.dp
    } else {
        if (deviceInfo.isTablet) 6.dp else 4.dp
    }
    val padding = if (deviceInfo.isTablet) 24.dp else 20.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        indication = ripple(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onClick() }
                } else Modifier
            ),
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        )
                    )
                )
                .padding(padding),
            content = content
        )
    }
}
@Composable
fun MobileOptimizedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    val deviceInfo = getDeviceInfo()
    val textFieldHeight = if (deviceInfo.isTablet) 72.dp else 64.dp
    val cornerRadius = if (deviceInfo.isTablet) 20.dp else 16.dp
    val fontSize = if (deviceInfo.isTablet) 18.sp else 16.sp

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = fontSize
                )
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .height(textFieldHeight),
        enabled = enabled,
        isError = isError,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(cornerRadius),
        singleLine = singleLine,
        maxLines = maxLines,
        textStyle = LocalTextStyle.current.copy(fontSize = fontSize),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
    )
}


@Composable
fun MobilePasswordOptimizedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    visualTransformation: VisualTransformation = PasswordVisualTransformation(),
) {
    val deviceInfo = getDeviceInfo()
    val textFieldHeight = if (deviceInfo.isTablet) 72.dp else 64.dp
    val cornerRadius = if (deviceInfo.isTablet) 20.dp else 16.dp
    val fontSize = if (deviceInfo.isTablet) 18.sp else 16.sp

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = fontSize
                )
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .height(textFieldHeight),
        enabled = enabled,
        isError = isError,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(cornerRadius),
        singleLine = singleLine,
        maxLines = maxLines,
        textStyle = LocalTextStyle.current.copy(fontSize = fontSize),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        )
    )
}

@Composable
fun MobileOptimizedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    text: String,
    isPrimary: Boolean = true,
    isLoading: Boolean = false
) {
    val screenInfo = getScreenSizeInfo()
    val buttonHeight = when {
        screenInfo.isExpanded -> 72.dp
        screenInfo.isMedium -> 64.dp
        else -> 56.dp
    }

    val cornerRadius = when {
        screenInfo.isExpanded -> 20.dp
        screenInfo.isMedium -> 18.dp
        else -> 16.dp
    }

    val fontSize = when {
        screenInfo.isExpanded -> 18.sp
        screenInfo.isMedium -> 16.sp
        else -> 14.sp
    }

    val gradientColors = if (isPrimary) {
        if (enabled) {
            listOf(
                PrimaryGreen,
                PrimaryGreen.copy(alpha = 0.8f)
            )
        } else {
            listOf(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
        }
    } else {
        if (enabled) {
            listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant
            )
        } else {
            listOf(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight)
//            .shadow(
//                elevation = if (enabled) 8.dp else 0.dp,
//                shape = RoundedCornerShape(cornerRadius),
//                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
//            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush = Brush.verticalGradient(gradientColors))
            .clickable(
                enabled = enabled && !isLoading,
                indication = ripple(color = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary),
                interactionSource = remember { MutableInteractionSource() }
            ) { if (!isLoading) onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(if (screenInfo.isExpanded) 24.dp else 20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = fontSize
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AdaptiveGrid(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val deviceInfo = getDeviceInfo()

    LazyVerticalGrid(
        columns = GridCells.Fixed(deviceInfo.optimalColumnCount),
        modifier = modifier,
        contentPadding = PaddingValues(deviceInfo.optimalSpacing),
        horizontalArrangement = Arrangement.spacedBy(deviceInfo.optimalSpacing),
        verticalArrangement = Arrangement.spacedBy(deviceInfo.optimalSpacing)
    ) {
        // Grid items would be added here
    }
}

@Composable
fun ModernDivider(
    modifier: Modifier = Modifier,
    thickness: androidx.compose.ui.unit.Dp = 1.dp,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color
    )
}

@Composable
fun OptimizedLazyColumn(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val deviceInfo = getDeviceInfo()
    val padding = if (deviceInfo.isTablet) 16.dp else 12.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = padding,
                vertical = padding
            )
            .then(
                if (contentPadding != PaddingValues(0.dp)) {
                    Modifier.padding(contentPadding)
                } else Modifier
            ),
        content = content
    )
}

@Composable
fun ResponsiveText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val deviceInfo = getDeviceInfo()
    val scaleFactor = when {
        deviceInfo.isLargeTablet -> 1.2f
        deviceInfo.isTablet -> 1.1f
        else -> 1.0f
    }

    Text(
        text = text,
        modifier = modifier,
        style = style.copy(fontSize = style.fontSize * scaleFactor),
        maxLines = maxLines,
        overflow = overflow
    )
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun MobileOptimizedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimary,
    gradient: Boolean = false
) {
    val deviceInfo = getDeviceInfo()
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val buttonModifier = if (gradient) {
        modifier
            .height(deviceInfo.optimalSpacing)
            .fillMaxWidth()
            .background(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(
                        androidx.compose.ui.graphics.Color(0xFF667eea),
                        androidx.compose.ui.graphics.Color(0xFF764ba2)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
    } else {
        modifier
            .height(deviceInfo.optimalSpacing)
            .fillMaxWidth()
    }

    Button(
        onClick = onClick,
        modifier = buttonModifier,
        enabled = enabled,
        colors = if (gradient) {
            ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentColor = contentColor
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor
            )
        },
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        androidx.compose.material3.Text(text = text, fontSize = when {
            screenWidthDp >= 1200 -> 18.sp
            screenWidthDp >= 800 -> 16.sp
            else -> 12.sp})
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun MobileOptimizedButtonColor(
    text: String,
    textColor : androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimary,
    gradient: Boolean = false,
    borderColor: androidx.compose.ui.graphics.Color
) {
    val deviceInfo = getDeviceInfo()
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val buttonModifier = if (gradient) {
        modifier
            .height(deviceInfo.optimalSpacing)
            .fillMaxWidth()
            .background(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(
                        androidx.compose.ui.graphics.Color(0xFF667eea),
                        androidx.compose.ui.graphics.Color(0xFF764ba2)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
    } else {
        modifier
            .height(deviceInfo.optimalSpacing)
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )

    }

    Button(
        onClick = onClick,
        modifier = buttonModifier,
        enabled = enabled,
        colors = if (gradient) {
            ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentColor = contentColor
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor
            )
        },
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        androidx.compose.material3.Text(text = text, fontSize = when {
            screenWidthDp >= 1200 -> 18.sp
            screenWidthDp >= 800 -> 16.sp
            else -> 12.sp},
            color = textColor,fontWeight = FontWeight.Bold)
    }
}