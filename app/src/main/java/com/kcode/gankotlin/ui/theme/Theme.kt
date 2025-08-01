package com.kcode.gankotlin.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Instagram-inspired light color scheme
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF833AB4), // Instagram purple
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE1306C), // Instagram pink
    onPrimaryContainer = Color.White,
    
    secondary = Color(0xFFF58529), // Instagram orange
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = Color(0xFF8A4000),
    
    tertiary = Color(0xFFDD2A7B), // Instagram pink
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD6E7),
    onTertiaryContainer = Color(0xFF5D1049),
    
    error = Color(0xFFFF4444),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    background = InstagramColors.BackgroundLight,
    onBackground = InstagramColors.TextPrimary,
    
    surface = InstagramColors.SurfaceLight,
    onSurface = InstagramColors.TextPrimary,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = InstagramColors.TextSecondary,
    
    outline = InstagramColors.BorderLight,
    outlineVariant = Color(0xFFF0F0F0),
    
    scrim = Color(0x80000000),
    
    inverseSurface = Color(0xFF2D2D2D),
    inverseOnSurface = Color(0xFFF2F2F2),
    inversePrimary = Color(0xFFB794F6)
)

// Instagram-inspired dark color scheme
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB794F6), // Lighter purple for dark mode
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFF7C3AED),
    onPrimaryContainer = Color.White,
    
    secondary = Color(0xFFFFB366), // Lighter orange for dark mode
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFFFF8A50),
    onSecondaryContainer = Color.White,
    
    tertiary = Color(0xFFFF6B9D), // Lighter pink for dark mode
    onTertiary = Color(0xFF1A1A1A),
    tertiaryContainer = Color(0xFFE91E63),
    onTertiaryContainer = Color.White,
    
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF1A1A1A),
    errorContainer = Color(0xFFFF5252),
    onErrorContainer = Color.White,
    
    background = InstagramColors.BackgroundDark,
    onBackground = InstagramColors.TextPrimaryDark,
    
    surface = InstagramColors.SurfaceDark,
    onSurface = InstagramColors.TextPrimaryDark,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = InstagramColors.TextSecondaryDark,
    
    outline = InstagramColors.BorderDark,
    outlineVariant = Color(0xFF404040),
    
    scrim = Color(0x80000000),
    
    inverseSurface = Color(0xFFF2F2F2),
    inverseOnSurface = Color(0xFF2D2D2D),
    inversePrimary = Color(0xFF833AB4)
)

@Composable
fun CryptoTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to maintain Instagram aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = InstagramTypography,
        content = content
    )
}