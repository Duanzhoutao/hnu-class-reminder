package com.hainanu.signinassistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B66C3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E8FF),
    onPrimaryContainer = Color(0xFF032B55),
    secondary = Color(0xFF2D7D79),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7F0EC),
    onSecondaryContainer = Color(0xFF0A3B38),
    tertiary = Color(0xFF7A5E1A),
    onTertiary = Color.White,
    background = Color(0xFFF5F8FC),
    onBackground = Color(0xFF152033),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF152033),
    surfaceVariant = Color(0xFFEEF3F9),
    onSurfaceVariant = Color(0xFF586475),
    outline = Color(0xFFD5DDE7),
    outlineVariant = Color(0xFFE5ECF4),
    error = Color(0xFFD04437),
    onError = Color.White,
    errorContainer = Color(0xFFFBE0DC),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF86BEFF),
    onPrimary = Color(0xFF00315F),
    primaryContainer = Color(0xFF0F3A67),
    onPrimaryContainer = Color(0xFFD6E8FF),
    secondary = Color(0xFF7AD2CB),
    onSecondary = Color(0xFF083532),
    secondaryContainer = Color(0xFF104643),
    onSecondaryContainer = Color(0xFFC7F0EC),
    tertiary = Color(0xFFE7C36C),
    onTertiary = Color(0xFF433100),
    background = Color(0xFF0E131B),
    onBackground = Color(0xFFF2F5F9),
    surface = Color(0xFF141B24),
    onSurface = Color(0xFFF2F5F9),
    surfaceVariant = Color(0xFF1B2430),
    onSurfaceVariant = Color(0xFFABB7C7),
    outline = Color(0xFF334153),
    outlineVariant = Color(0xFF273343),
    error = Color(0xFFFF8D80),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF8A170E),
)

@Composable
fun HainanuSignInAssistantTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
