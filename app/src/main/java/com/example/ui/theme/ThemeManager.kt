package com.example.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayerTheme(
    val id: String,
    val name: String,
    val primary: Color,
    val accent: Color,
    val background: Color,
    val surface: Color
)

object ThemePresets {
    val CyberCyan = PlayerTheme(
        id = "cyber_cyan",
        name = "Cyber Cyan (Default)",
        primary = Color(0xFF00E5FF),
        accent = Color(0xFF9D4EDD),
        background = Color(0xFF0A0F1D),
        surface = Color(0xFF131B2A)
    )

    val NeonEmerald = PlayerTheme(
        id = "neon_emerald",
        name = "MX Emerald (Classic)",
        primary = Color(0xFF3DDC84),
        accent = Color(0xFF00E5FF),
        background = Color(0xFF0A140F),
        surface = Color(0xFF112217)
    )

    val SunsetAmber = PlayerTheme(
        id = "sunset_amber",
        name = "Sunset Amber",
        primary = Color(0xFFFF9E00),
        accent = Color(0xFFFF0054),
        background = Color(0xFF140A0A),
        surface = Color(0xFF221212)
    )

    val ElectricPurple = PlayerTheme(
        id = "electric_purple",
        name = "Electric Purple",
        primary = Color(0xFFC77DFF),
        accent = Color(0xFF00F5D4),
        background = Color(0xFF120A1C),
        surface = Color(0xFF1F112E)
    )

    val OledBlack = PlayerTheme(
        id = "oled_black",
        name = "OLED Pitch Black",
        primary = Color(0xFF38BDF8),
        accent = Color(0xFF818CF8),
        background = Color(0xFF000000),
        surface = Color(0xFF121212)
    )

    val allPresets = listOf(CyberCyan, NeonEmerald, SunsetAmber, ElectricPurple, OledBlack)
}

object ThemeManager {
    private val _currentTheme = MutableStateFlow(ThemePresets.CyberCyan)
    val currentTheme: StateFlow<PlayerTheme> = _currentTheme.asStateFlow()

    fun selectPreset(theme: PlayerTheme) {
        _currentTheme.value = theme
    }

    fun applyCustomColors(primary: Color, accent: Color) {
        _currentTheme.value = PlayerTheme(
            id = "custom",
            name = "Custom Theme",
            primary = primary,
            accent = accent,
            background = Color(0xFF0A0F1D),
            surface = Color(0xFF131B2A)
        )
    }

    fun toColorScheme(theme: PlayerTheme) = darkColorScheme(
        primary = theme.primary,
        onPrimary = Color(0xFF001524),
        secondary = theme.accent,
        onSecondary = Color.White,
        background = theme.background,
        onBackground = Color.White,
        surface = theme.surface,
        onSurface = Color.White
    )
}
