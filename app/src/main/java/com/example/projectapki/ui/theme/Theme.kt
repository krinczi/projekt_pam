package com.example.projectapki.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PinkScheme = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = PinkOnPrimary,

    secondary = PinkSecondary,
    tertiary = PinkTertiary,

    background = Bg,
    onBackground = TextStrong,

    surface = Surface,
    onSurface = TextStrong,

    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextMuted,

    outline = OutlineSoft,
    error = ErrorSoft,
    onError = Color.White
)

@Composable
fun ProjectapkiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PinkScheme,
        typography = Typography(),
        content = content
    )
}
