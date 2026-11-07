package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PlayerTheme
import com.example.ui.theme.ThemeManager
import com.example.ui.theme.ThemePresets

@Composable
fun ThemeSelectorDialog(
    currentTheme: PlayerTheme,
    onDismiss: () -> Unit
) {
    var selectedPreset by remember { mutableStateOf(currentTheme) }
    var customPrimary by remember { mutableStateOf(currentTheme.primary) }
    var customAccent by remember { mutableStateOf(currentTheme.accent) }
    var isCustomMode by remember { mutableStateOf(currentTheme.id == "custom") }

    val paletteOptions = listOf(
        Color(0xFF00E5FF), // Cyan
        Color(0xFF3DDC84), // Emerald
        Color(0xFFFF9E00), // Amber
        Color(0xFFFF0054), // Hot Pink
        Color(0xFFC77DFF), // Purple
        Color(0xFF00F5D4), // Mint
        Color(0xFF38BDF8), // Sky Blue
        Color(0xFFFF5722)  // Deep Orange
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (isCustomMode) {
                        ThemeManager.applyCustomColors(customPrimary, customAccent)
                    } else {
                        ThemeManager.selectPreset(selectedPreset)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isCustomMode) customPrimary else selectedPreset.primary),
                modifier = Modifier.testTag("apply_theme_button")
            ) {
                Text("Apply Theme", color = Color(0xFF001524), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF9CA3AF))
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Themes",
                        tint = if (isCustomMode) customPrimary else selectedPreset.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Player Themes",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Predefined Color Schemes:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF9CA3AF))
                )
                Spacer(modifier = Modifier.height(8.dp))

                ThemePresets.allPresets.forEach { preset ->
                    val isSelected = !isCustomMode && selectedPreset.id == preset.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                isCustomMode = false
                                selectedPreset = preset
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF111827)
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, preset.primary) else null,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(preset.primary)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(preset.accent)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Theme Creator
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCustomMode = true },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCustomMode) Color(0xFF1E293B) else Color(0xFF111827)
                    ),
                    border = if (isCustomMode) androidx.compose.foundation.BorderStroke(1.5.dp, customPrimary) else null,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ColorLens,
                                    contentDescription = null,
                                    tint = customPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Create Custom Theme",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            if (isCustomMode) {
                                Text(
                                    text = "Active",
                                    color = customPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (isCustomMode) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Choose Primary Color:",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF9CA3AF))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                paletteOptions.forEach { c ->
                                    val isPicked = customPrimary == c
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                            .border(
                                                width = if (isPicked) 2.5.dp else 0.dp,
                                                color = Color.White,
                                                shape = CircleShape
                                            )
                                            .clickable { customPrimary = c }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Choose Accent Color:",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF9CA3AF))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                paletteOptions.reversed().forEach { c ->
                                    val isPicked = customAccent == c
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                            .border(
                                                width = if (isPicked) 2.5.dp else 0.dp,
                                                color = Color.White,
                                                shape = CircleShape
                                            )
                                            .clickable { customAccent = c }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(20.dp)
    )
}
