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
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.subtitles.SubtitleParser
import com.example.subtitles.SubtitleStyle
import java.util.Locale

@Composable
fun SubtitleSettingsDialog(
    currentStyle: SubtitleStyle,
    onStyleChanged: (SubtitleStyle) -> Unit,
    onLoadExternalSubtitles: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(currentStyle.isEnabled) }
    var fontSize by remember { mutableFloatStateOf(currentStyle.fontSizeSp) }
    var selectedTextColor by remember { mutableStateOf(currentStyle.textColor) }
    var syncOffsetSeconds by remember { mutableFloatStateOf(currentStyle.syncOffsetMs / 1000f) }

    var customSrtInput by remember { mutableStateOf("") }
    var showPasteField by remember { mutableStateOf(false) }

    val colors = listOf(
        Pair("White", Color.White),
        Pair("Yellow", Color(0xFFFFEB3B)),
        Pair("Cyan", Color(0xFF00E5FF)),
        Pair("Green", Color(0xFF4ADE80))
    )

    val fontSizes = listOf(
        Pair("Small", 14f),
        Pair("Normal", 18f),
        Pair("Large", 24f),
        Pair("Extra", 30f)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onStyleChanged(
                        SubtitleStyle(
                            isEnabled = isEnabled,
                            fontSizeSp = fontSize,
                            textColor = selectedTextColor,
                            syncOffsetMs = (syncOffsetSeconds * 1000).toLong()
                        )
                    )
                    if (customSrtInput.isNotBlank()) {
                        onLoadExternalSubtitles(customSrtInput)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                modifier = Modifier.testTag("apply_subtitles_button")
            ) {
                Text("Save Settings", color = Color(0xFF001524), fontWeight = FontWeight.Bold)
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
                        imageVector = Icons.Default.Subtitles,
                        contentDescription = "Subtitles",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Subtitle Controls",
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
                // Enable switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Show Subtitles",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00E5FF),
                            checkedTrackColor = Color(0xFF005662)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Preview Box
                Text(
                    text = "Live Preview:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF9CA3AF))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isEnabled) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x99000000))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Sample subtitle preview text",
                                color = selectedTextColor,
                                fontSize = fontSize.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Text(
                            text = "(Subtitles disabled)",
                            color = Color(0xFF64748B),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Font Size Selector
                Text(
                    text = "Font Size:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF9CA3AF))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    fontSizes.forEach { (label, size) ->
                        val isSelected = fontSize == size
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E293B))
                                .clickable { fontSize = size }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color(0xFF001524) else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Color Selector
                Text(
                    text = "Text Color:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF9CA3AF))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colors.forEach { (label, c) ->
                        val isSelected = selectedTextColor == c
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF00E5FF) else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { selectedTextColor = c }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sync Offset Slider (-5s to +5s)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Subtitle Sync Offset:",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF9CA3AF))
                    )
                    Text(
                        text = String.format(Locale.US, "%+.1f s", syncOffsetSeconds),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Slider(
                    value = syncOffsetSeconds,
                    onValueChange = { syncOffsetSeconds = it },
                    valueRange = -5f..5f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00E5FF),
                        activeTrackColor = Color(0xFF00E5FF),
                        inactiveTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // External Subtitle Load options
                Button(
                    onClick = { showPasteField = !showPasteField },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showPasteField) "Hide Subtitle Input" else "Load Custom SRT / VTT Text",
                        color = Color(0xFF00E5FF)
                    )
                }

                if (showPasteField) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customSrtInput,
                        onValueChange = { customSrtInput = it },
                        placeholder = { Text("Paste .srt or .vtt content here...", color = Color(0xFF64748B)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        Button(
                            onClick = {
                                customSrtInput = SubtitleParser.sampleSubtitlesSrt
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005662)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Insert Sample SRT", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF111827),
        shape = RoundedCornerShape(20.dp)
    )
}
