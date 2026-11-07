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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import java.util.Locale

@Composable
fun SpeedControlDialog(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val presetSpeeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f)
    var sliderValue by remember { mutableFloatStateOf(currentSpeed) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onSpeedSelected(sliderValue)
                    onDismiss()
                },
                modifier = Modifier.testTag("apply_speed_button")
            ) {
                Text("Apply", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF9CA3AF))
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Playback Speed",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Playback Speed",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                // Current speed display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1B2436))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format(Locale.US, "%.2fx", sliderValue),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Slider
                Text(
                    text = "Fine Tune:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
                )
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0.25f..3.0f,
                    steps = 55, // 0.05 step
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00E5FF),
                        activeTrackColor = Color(0xFF00E5FF),
                        inactiveTrackColor = Color(0xFF374151)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("speed_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Presets:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Preset Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.height(110.dp)
                ) {
                    items(presetSpeeds) { speed ->
                        val isSelected = kotlin.math.abs(speed - sliderValue) < 0.03f
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF00E5FF) else Color(0xFF1F2937))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.White else Color(0xFF374151),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    sliderValue = speed
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${speed}x",
                                color = if (isSelected) Color(0xFF001524) else Color.White,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF111827),
        shape = RoundedCornerShape(20.dp)
    )
}
