package com.dhrashtax.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color(0xFF00272D),
    secondary = Color(0xFF8BE9FD),
    background = Color(0xFF0B1220),
    surface = Color(0xFF141B2E),
    surfaceVariant = Color(0xFF1B253A),
    error = Color(0xFFEF4444),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
)

@Composable
fun DhrashtaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}

