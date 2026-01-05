package com.warriortech.resb.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warriortech.resb.ui.theme.PrimaryGreen
import com.warriortech.resb.util.getDeviceInfo
import com.warriortech.resb.util.getScreenSizeInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import com.google.accompanist.permissions.rememberPermissionState

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

@Composable
fun BarcodeInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onBarcodeScanned: (String) -> Unit,
    onCameraClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Barcode") },
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = onCameraClick) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Scan")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onPreviewKeyEvent {
                if (it.type == KeyEventType.KeyUp && it.key == Key.Enter) {
                    if (value.isNotBlank()) onBarcodeScanned(value.trim())
                    true
                } else false
            },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.None
        )
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermission(
    onGranted: @Composable () -> Unit
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    if (permissionState.status.isGranted) {
        onGranted()
    }
}


@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CameraBarcodeScanner(
    onResult: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    CameraPermission {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val scanner = BarcodeScanning.getClient(
                        BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                            .build()
                    )

                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    barcodes.firstOrNull()?.rawValue?.let {
                                        onResult(it)
                                        onClose()
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else imageProxy.close()
                    }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                    )
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
