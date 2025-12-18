package com.warriortech.resb.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.warriortech.resb.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController

val MontserratFontFamily = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = Color.White,
    secondary = AccentOrange,
    onSecondary = Color.White,
    secondaryContainer = AccentOrangeLight,
    onSecondaryContainer = Color.White,
    tertiary = GradientStart,
    onTertiary = Color.White,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color.White,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMedium,
    onSurfaceVariant = TextSecondary,
    outline = TextHint,
    outlineVariant = SurfaceDark,
    scrim = Color.Black.copy(alpha = 0.32f)
)

// Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = Color.Black,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = Color.White,
    secondary = AccentOrangeLight,
    onSecondary = Color.Black,
    secondaryContainer = AccentOrange,
    onSecondaryContainer = Color.White,
    tertiary = GradientEnd,
    onTertiary = Color.White,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFE0E0E0),
    outline = Color(0xFF9E9E9E),
    outlineVariant = Color(0xFF424242),
    scrim = Color.Black.copy(alpha = 0.32f)
)

// Custom Typography
val ResbTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp
    )
)

@Composable
fun ResbTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // 👈 disable dynamic colors
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    val colorScheme = if (darkTheme) {
        systemUiController.setStatusBarColor(DarkColorScheme.surface, darkIcons = false)
        DarkColorScheme
    } else {
        systemUiController.setStatusBarColor(LightColorScheme.surface, darkIcons = true)
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
//    val systemUiController = rememberSystemUiController()
//    val colorScheme = if (darkTheme) {
//        DarkColorScheme
//    } else {
//        LightColorScheme
//    }
//
//    // Configure system bars for edge-to-edge display (crucial for Redmi/MIUI devices)
//    SideEffect {
//        systemUiController.setSystemBarsColor(
//            color = Color.Transparent,
//            darkIcons = !darkTheme,
//            isNavigationBarContrastEnforced = false
//        )
//
//        // Specifically handle navigation bar for Redmi devices
//        systemUiController.setNavigationBarColor(
//            color = Color.Transparent,
//            darkIcons = !darkTheme,
//            navigationBarContrastEnforced = false
//        )
//
//        // Set status bar with proper contrast for MIUI
//        systemUiController.setStatusBarColor(
//            color = Color.Transparent,
//            darkIcons = !darkTheme
//        )
//    }
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = ResbTypography,
//        content = content
//    )
}

//@Composable
//fun ResbTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    dynamicColor: Boolean = true,
//    content: @Composable () -> Unit
//) {
//    val systemUiController = rememberSystemUiController()
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) {
//                systemUiController.setStatusBarColor(
//                    color = DarkColorScheme.surface,
//                    darkIcons = false
//                )
//                dynamicDarkColorScheme(context)
//            } else {
//                systemUiController.setStatusBarColor(
//                    color = LightColorScheme.surface,
//                    darkIcons = true
//                )
//                dynamicLightColorScheme(context)
//            }
//        }
//        darkTheme -> {
//            systemUiController.setStatusBarColor(color = DarkColorScheme.surface, darkIcons = false)
//            DarkColorScheme
//        }
//        else -> {
//            systemUiController.setStatusBarColor(
//                color = LightColorScheme.surface,
//                darkIcons = true
//            )
//            LightColorScheme
//        }
//    }
//    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = Typography,
//        content = content
//    )
//}
