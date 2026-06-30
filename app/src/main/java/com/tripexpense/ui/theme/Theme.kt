package com.tripexpense.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = tint,
    onPrimary = bg,
    secondary = tintDim,
    onSecondary = bg,
    background = bg,
    onBackground = textPrimary,
    surface = bg,
    onSurface = textPrimary,
    surfaceVariant = bgSecondary,
    onSurfaceVariant = textSecondary,
    outline = separator,
    outlineVariant = separatorOpaque,
    error = red,
    onError = bg,
    tertiary = blue,
    onTertiary = bg,
)

@Composable
fun TripExpenseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content,
    )
}
