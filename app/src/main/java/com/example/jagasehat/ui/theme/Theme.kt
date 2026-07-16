package com.example.jagasehat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Green500,
    onPrimary = SurfaceWhite,
    primaryContainer = Green100,
    onPrimaryContainer = Green700,
    secondary = Blue500,
    background = SurfaceWhite,
    surface = SurfaceWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = Red500
)

@Composable
fun JagaSehatTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
