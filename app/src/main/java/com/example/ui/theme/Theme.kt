package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    secondary = ElectricBlue,
    onSecondary = Color.White,
    tertiary = AmberWarning,
    background = Color(0xFF000000), // Pure OLED Black background
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),     // All panels are black panels
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF141419),
    onSurfaceVariant = Color(0xFF9E9E9E),
    error = AlertCoral,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    secondary = ElectricBlue,
    onSecondary = Color.White,
    tertiary = AmberWarning,
    background = Color(0xFF000000), // Pure OLED Black background always
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),     // All panels are black panels
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF141419),
    onSurfaceVariant = Color(0xFF9E9E9E),
    error = AlertCoral,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    // Keep dynamic color support if Android 12+ or use standard slate colors for consistent look
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
    
    val darkTheme = when (themePreferenceGlobal) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemDark
    }

    androidx.compose.runtime.SideEffect {
        isDarkThemeGlobal = darkTheme
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
