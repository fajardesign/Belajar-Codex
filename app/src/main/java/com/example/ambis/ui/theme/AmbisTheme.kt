package com.example.ambis.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0053A1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0D274D),
    onPrimaryContainer = Color(0xFFE0ECFF),
    secondary = Color(0xFF546070),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E3F7),
    onSecondaryContainer = Color(0xFF111C2B),
    tertiary = Color(0xFF386A20),
    onTertiary = Color.White,
    background = Color(0xFFF8F9FC),
    onBackground = Color(0xFF1B1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE0E2EC),
    onSurfaceVariant = Color(0xFF424753),
    outline = Color(0xFF737680),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFACC7FF),
    onPrimary = Color(0xFF002F65),
    primaryContainer = Color(0xFF0D274D),
    onPrimaryContainer = Color(0xFFE0ECFF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF243140),
    secondaryContainer = Color(0xFF3B4858),
    onSecondaryContainer = Color(0xFFD7E3F7),
    tertiary = Color(0xFFA0D48A),
    onTertiary = Color(0xFF063908),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF434750),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E909B),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val AmbisShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val AmbisTypography = Typography()

@Composable
fun AmbisTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AmbisTypography,
        shapes = AmbisShapes,
        content = content
    )
}
