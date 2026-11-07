package com.example.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.data.model.MediaItem
import com.example.player.PlayerController
import com.example.ui.components.SubtitleSettingsDialog
import com.example.ui.components.Watermark3D
import com.example.util.formatTimeMs
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    playerController: PlayerController,
    onOpenSpeedDialog: () -> Unit,
    onSelectMedia: (MediaItem) -> Unit,
    availableVideos: List<MediaItem>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by playerController.state.collectAsState()
    var controlsVisible by remember { mutableStateOf(true) }
    var showSubtitleDialog by remember { mutableStateOf(false) }

    // Auto-hide controls timer
    LaunchedEffect(controlsVisible, state.isPlaying) {
        if (controlsVisible && state.isPlaying) {
            delay(4000L)
            controlsVisible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        controlsVisible = !controlsVisible
                    },
                    onDoubleTap = { offset ->
                        // Double tap left = -10s, right = +10s
                        if (offset.x < size.width / 2) {
                            playerController.seekBack(10000L)
                        } else {
                            playerController.seekForward(10000L)
                        }
                        controlsVisible = true
                    }
                )
            }
    ) {
        // ExoPlayer Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = playerController.exoPlayer
                    useController = false // Custom Compose controls overlay
                    resizeMode = state.scaleMode.resizeMode
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { view ->
                view.player = playerController.exoPlayer
                view.resizeMode = state.scaleMode.resizeMode
            },
            modifier = Modifier.fillMaxSize()
        )

        // ALWAYS-VISIBLE: 3D 50% Transparent Watermark "RazifApps®"
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
        ) {
            Watermark3D(text = "RazifApps®")
        }

        // Live Synchronized Subtitle Display
        if (state.subtitleStyle.isEnabled && !state.currentSubtitleText.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (controlsVisible) 110.dp else 40.dp, start = 24.dp, end = 24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(state.subtitleStyle.backgroundColor)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("subtitle_display_box")
            ) {
                Text(
                    text = state.currentSubtitleText ?: "",
                    color = state.subtitleStyle.textColor,
                    fontSize = state.subtitleStyle.fontSizeSp.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = (state.subtitleStyle.fontSizeSp * 1.3f).sp
                )
            }
        }

        // Buffering indicator
        if (state.isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }

        // Animated Controls Overlay
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xCC000000),
                                Color(0x40000000),
                                Color(0xE6000000)
                            )
                        )
                    )
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 28.dp)
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.currentItem?.title ?: "No video playing",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = state.currentItem?.artist ?: "Razif Player",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.primary
                            ),
                            maxLines = 1
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Subtitle Settings Button
                        IconButton(
                            onClick = { showSubtitleDialog = true },
                            modifier = Modifier.testTag("subtitles_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subtitles,
                                contentDescription = "Subtitles",
                                tint = if (state.subtitleStyle.isEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }

                        // Background Playback Indicator/Toggle Button
                        IconButton(
                            onClick = {
                                playerController.toggleBackgroundPlayback(!state.isBackgroundPlaybackEnabled)
                            },
                            modifier = Modifier.testTag("background_playback_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = "Background Audio",
                                tint = if (state.isBackgroundPlaybackEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }

                        // Playback Speed Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onOpenSpeedDialog() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("speed_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Speed",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format(Locale.US, "%.2fx", state.playbackSpeed),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Scale mode button (Fit / Crop / Stretch)
                        IconButton(
                            onClick = { playerController.cycleScaleMode() },
                            modifier = Modifier.testTag("scale_mode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = "Scale Mode",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Center Play / Pause & Seek 10s buttons
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    IconButton(
                        onClick = { playerController.seekBack(10000L) },
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("seek_back_10_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind 10s",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { playerController.togglePlayPause() }
                            .testTag("video_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                            tint = Color(0xFF001524),
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    IconButton(
                        onClick = { playerController.seekForward(10000L) },
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("seek_forward_10_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10s",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Bottom Timeline & Playlist Quick Switcher
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    // Quick Video Switcher row
                    if (availableVideos.isNotEmpty()) {
                        Text(
                            text = "Videos in Playlist:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF9CA3AF),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            items(availableVideos) { video ->
                                val isCurrent = video.mediaUri == state.currentItem?.mediaUri
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else Color(0xFF1E293B),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onSelectMedia(video) }
                                ) {
                                    Text(
                                        text = video.title,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isCurrent) Color(0xFF001524) else Color.White,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Seekbar with timestamps
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTimeMs(state.positionMs),
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                        )
                        Slider(
                            value = state.positionMs.toFloat().coerceAtMost(state.durationMs.toFloat()),
                            onValueChange = { newPos ->
                                playerController.seekTo(newPos.toLong())
                            },
                            valueRange = 0f..state.durationMs.coerceAtLeast(1L).toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color(0x66FFFFFF)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                                .testTag("video_progress_slider")
                        )
                        Text(
                            text = formatTimeMs(state.durationMs),
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                        )
                    }

                    // Bottom Navigation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { playerController.playPrevious() }) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Video",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "Mode: ${state.scaleMode.label} • BG Audio: ${if (state.isBackgroundPlaybackEnabled) "ON" else "OFF"}",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF9CA3AF))
                        )

                        IconButton(onClick = { playerController.playNext() }) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Video",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Subtitle Settings Dialog
    if (showSubtitleDialog) {
        SubtitleSettingsDialog(
            currentStyle = state.subtitleStyle,
            onStyleChanged = { newStyle ->
                playerController.updateSubtitleStyle(newStyle)
            },
            onLoadExternalSubtitles = { srtContent ->
                playerController.loadSubtitles(srtContent)
            },
            onDismiss = { showSubtitleDialog = false }
        )
    }
}
