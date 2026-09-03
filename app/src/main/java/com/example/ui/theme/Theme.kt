package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.model.AppTheme

val LocalAppTheme = staticCompositionLocalOf { AppTheme.ORIGINAL }

data class AppThemeColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val cardGradientTop: Color,
    val cardGradientBottom: Color,
    val isLight: Boolean
)

val LocalThemeColors = staticCompositionLocalOf {
    AppThemeColors(
        background = PurpleNightBg,
        surface = PurpleNightSurface,
        surfaceElevated = PurpleNightSurfaceElevated,
        border = PurpleNightBorder,
        textPrimary = Color.White,
        textSecondary = TextSecondary,
        cardGradientTop = PurpleNightSurface.copy(alpha = 0.9f),
        cardGradientBottom = Color.Black.copy(alpha = 0.95f),
        isLight = false
    )
}

private val OriginalColorScheme = darkColorScheme(
    primary = VioletPrimary,
    onPrimary = TextPrimary,
    primaryContainer = PurpleNightSurfaceElevated,
    onPrimaryContainer = TextPrimary,
    background = PurpleNightBg,
    onBackground = TextPrimary,
    surface = PurpleNightSurface,
    onSurface = TextPrimary,
    surfaceVariant = PurpleNightSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = PurpleNightBorder
)

private val BlackColorScheme = darkColorScheme(
    primary = VioletPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF141414),
    onPrimaryContainer = Color.White,
    background = Color(0xFF000000),
    onBackground = Color.White,
    surface = Color(0xFF0C0C0C),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF181818),
    onSurfaceVariant = Color(0xFFA0A0A0),
    outline = Color(0xFF2E2E2E)
)

private val WhiteColorScheme = lightColorScheme(
    primary = VioletPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF0EBF8),
    onPrimaryContainer = Color(0xFF2D1E4E),
    background = Color(0xFFF4F6FB),
    onBackground = Color(0xFF15181E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF15181E),
    surfaceVariant = Color(0xFFE8ECF4),
    onSurfaceVariant = Color(0xFF5A6275),
    outline = Color(0xFFD0D5E2)
)

@Composable
fun MyApplicationTheme(
    appTheme: AppTheme = AppTheme.ORIGINAL,
    content: @Composable () -> Unit
) {
    val isLight = appTheme == AppTheme.WHITE
    val colorScheme = when (appTheme) {
        AppTheme.ORIGINAL -> OriginalColorScheme
        AppTheme.BLACK -> BlackColorScheme
        AppTheme.WHITE -> WhiteColorScheme
    }

    val themeColors = when (appTheme) {
        AppTheme.ORIGINAL -> AppThemeColors(
            background = PurpleNightBg,
            surface = PurpleNightSurface,
            surfaceElevated = PurpleNightSurfaceElevated,
            border = PurpleNightBorder,
            textPrimary = Color.White,
            textSecondary = TextSecondary,
            cardGradientTop = PurpleNightSurface.copy(alpha = 0.9f),
            cardGradientBottom = Color.Black.copy(alpha = 0.95f),
            isLight = false
        )
        AppTheme.BLACK -> AppThemeColors(
            background = Color.Black,
            surface = Color(0xFF0D0D0D),
            surfaceElevated = Color(0xFF181818),
            border = Color(0xFF2A2A2A),
            textPrimary = Color.White,
            textSecondary = Color(0xFFAAAAAA),
            cardGradientTop = Color(0xFF121212),
            cardGradientBottom = Color(0xFF040404),
            isLight = false
        )
        AppTheme.WHITE -> AppThemeColors(
            background = Color(0xFFF3F5FA),
            surface = Color(0xFFFFFFFF),
            surfaceElevated = Color(0xFFF8F9FD),
            border = Color(0xFFDFE3EC),
            textPrimary = Color(0xFF12141A),
            textSecondary = Color(0xFF555B6E),
            cardGradientTop = Color(0xFFFFFFFF),
            cardGradientBottom = Color(0xFFF0F3F9),
            isLight = true
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = isLight
                isAppearanceLightNavigationBars = isLight
            }
        }
    }

    CompositionLocalProvider(
        LocalAppTheme provides appTheme,
        LocalThemeColors provides themeColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
