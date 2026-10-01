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

private val ToDodoLightColorScheme = lightColorScheme(
    primary = ToDodoYellow,
    onPrimary = ToDodoTextDark,
    primaryContainer = ToDodoYellowLight,
    onPrimaryContainer = ToDodoTextDark,
    secondary = ToDodoOrange,
    onSecondary = Color.White,
    secondaryContainer = ToDodoYellowPastel,
    onSecondaryContainer = ToDodoTextDark,
    tertiary = ToDodoPink,
    onTertiary = Color.White,
    background = ToDodoCream,
    onBackground = ToDodoTextDark,
    surface = ToDodoSurface,
    onSurface = ToDodoTextDark,
    surfaceVariant = ToDodoSurfaceVariant,
    onSurfaceVariant = ToDodoTextMuted,
    outline = ToDodoBorder
)

private val ToDodoDarkColorScheme = darkColorScheme(
    primary = ToDodoDarkYellow,
    onPrimary = ToDodoDarkBg,
    primaryContainer = ToDodoDarkSurfaceVariant,
    onPrimaryContainer = ToDodoDarkText,
    secondary = ToDodoOrange,
    onSecondary = Color.White,
    secondaryContainer = ToDodoDarkSurface,
    onSecondaryContainer = ToDodoDarkText,
    tertiary = ToDodoPink,
    onTertiary = Color.White,
    background = ToDodoDarkBg,
    onBackground = ToDodoDarkText,
    surface = ToDodoDarkSurface,
    onSurface = ToDodoDarkText,
    surfaceVariant = ToDodoDarkSurfaceVariant,
    onSurfaceVariant = ToDodoDarkTextMuted,
    outline = ToDodoDarkSurfaceVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) ToDodoDarkColorScheme else ToDodoLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
