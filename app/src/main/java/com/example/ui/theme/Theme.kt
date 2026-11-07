package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    val currentTheme by ThemeManager.currentTheme.collectAsState()
    val colorScheme = ThemeManager.toColorScheme(currentTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
