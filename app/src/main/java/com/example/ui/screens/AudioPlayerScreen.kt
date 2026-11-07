package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.example.data.model.MediaItem
import com.example.player.PlayerController
import com.example.ui.components.Watermark3D
import com.example.util.formatTimeMs
import java.util.Locale

@Composable
fun AudioPlayerScreen(
    playerController: PlayerController,
    onOpenSpeedDialog: () -> Unit,
    onAskAiInfo: (MediaItem) -> Unit,
    audioTracks: List<MediaItem>,
    modifier: Modifier = Modifier
) {
    val state by playerController.state.collectAsState()
    val isPlaying = state.isPlaying

    // Infinite rotation for vinyl disc when playing
    val infiniteTransition = rememberInfiniteTransition(label = "disc_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_angle"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row with Title & 3D Watermark
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Audio Player",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "High Fidelity Playback",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF00E5FF))
                )
            }

            // 3D 50% Transparent Watermark
            Watermark3D(text = "RazifApps®")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Animated Vinyl Disc / Album Cover
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF020617))
                    )
                )
                .border(2.dp, Color(0xFF00E5FF), CircleShape)
                .rotate(if (isPlaying) rotationAngle else 0f),
            contentAlignment = Alignment.Center
        ) {
            // Grooves
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0x3300E5FF), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
            )
            // Center Centerpiece
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E5FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFF001524),
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Track Info
        Text(
            text = state.currentItem?.title ?: "Select a song from queue",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = state.currentItem?.artist ?: "Razif Player",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF9CA3AF)
            ),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Playback Speed Pill & AI Info Action Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenSpeedDialog() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("audio_speed_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Speed: " + String.format(Locale.US, "%.2fx", state.playbackSpeed),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            state.currentItem?.let { current ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x3300E5FF),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onAskAiInfo(current) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("ask_ai_info_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Info",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Story & Lyrics",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Seeker Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTimeMs(state.positionMs),
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF9CA3AF))
            )
            Slider(
                value = state.positionMs.toFloat().coerceAtMost(state.durationMs.toFloat()),
                onValueChange = { newPos ->
                    playerController.seekTo(newPos.toLong())
                },
                valueRange = 0f..state.durationMs.coerceAtLeast(1L).toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00E5FF),
                    activeTrackColor = Color(0xFF00E5FF),
                    inactiveTrackColor = Color(0xFF334155)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .testTag("audio_progress_slider")
            )
            Text(
                text = formatTimeMs(state.durationMs),
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF9CA3AF))
            )
        }

        // Media Controls (Shuffle, Prev, Rewind 10, Play/Pause, Forward 10, Next, Repeat)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { playerController.toggleShuffle() }) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (state.isShuffle) Color(0xFF00E5FF) else Color(0xFF64748B)
                )
            }

            IconButton(onClick = { playerController.playPrevious() }) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White
                )
            }

            IconButton(onClick = { playerController.seekBack(10000L) }) {
                Icon(
                    imageVector = Icons.Default.Replay10,
                    contentDescription = "Rewind 10s",
                    tint = Color(0xFF94A3B8)
                )
            }

            // Big Play/Pause Button
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E5FF))
                    .clickable { playerController.togglePlayPause() }
                    .testTag("audio_play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color(0xFF001524),
                    modifier = Modifier.size(38.dp)
                )
            }

            IconButton(onClick = { playerController.seekForward(10000L) }) {
                Icon(
                    imageVector = Icons.Default.Forward10,
                    contentDescription = "Forward 10s",
                    tint = Color(0xFF94A3B8)
                )
            }

            IconButton(onClick = { playerController.playNext() }) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White
                )
            }

            IconButton(onClick = { playerController.toggleRepeatMode() }) {
                val icon = when (state.repeatMode) {
                    Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                    else -> Icons.Default.Repeat
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Repeat",
                    tint = if (state.repeatMode != Player.REPEAT_MODE_OFF) Color(0xFF00E5FF) else Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Queue Header & List
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Track Queue (${audioTracks.size})",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(audioTracks) { index, track ->
                val isCurrent = track.mediaUri == state.currentItem?.mediaUri
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            playerController.playQueue(audioTracks, index)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) Color(0xFF1E293B) else Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isCurrent) Color(0xFF00E5FF) else Color(0xFF64748B),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.width(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) Color(0xFF00E5FF) else Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.artist,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8)
                                ),
                                maxLines = 1
                            )
                        }
                        if (isCurrent && isPlaying) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Playing",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
