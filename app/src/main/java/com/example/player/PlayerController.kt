package com.example.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.ui.graphics.Color
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import com.example.data.model.MediaItem
import com.example.subtitles.SubtitleCue
import com.example.subtitles.SubtitleParser
import com.example.subtitles.SubtitleStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class VideoScaleMode(val label: String, val resizeMode: Int) {
    FIT("Fit", AspectRatioFrameLayout.RESIZE_MODE_FIT),
    CROP("Crop (Fill)", AspectRatioFrameLayout.RESIZE_MODE_ZOOM),
    STRETCH("Stretch", AspectRatioFrameLayout.RESIZE_MODE_FILL)
}

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentItem: MediaItem? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val isShuffle: Boolean = false,
    val volume: Float = 1.0f,
    val brightness: Float = 0.8f,
    val scaleMode: VideoScaleMode = VideoScaleMode.FIT,
    val isBuffering: Boolean = false,
    val playlist: List<MediaItem> = emptyList(),
    val currentIndex: Int = -1,
    val isBackgroundPlaybackEnabled: Boolean = true,
    val subtitleStyle: SubtitleStyle = SubtitleStyle(),
    val currentSubtitleText: String? = null
)

@OptIn(UnstableApi::class)
class PlayerController(context: Context) {

    // Configure audio attributes for smooth background playback & audio focus
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .build()

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        repeatMode = Player.REPEAT_MODE_OFF
        playWhenReady = true
        setAudioAttributes(audioAttributes, true) // Background audio support
        setWakeMode(C.WAKE_MODE_LOCAL) // Keep CPU awake during audio/video playback
    }

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private var parsedSubtitles: List<SubtitleCue> = SubtitleParser.parse(SubtitleParser.sampleSubtitlesSrt)

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(
                    isPlaying = isPlaying,
                    durationMs = exoPlayer.duration.coerceAtLeast(0L)
                )
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    progressJob?.cancel()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val isBuffering = playbackState == Player.STATE_BUFFERING
                val duration = exoPlayer.duration.coerceAtLeast(0L)
                _state.value = _state.value.copy(
                    isBuffering = isBuffering,
                    durationMs = duration
                )
                if (playbackState == Player.STATE_ENDED) {
                    playNext()
                }
            }

            override fun onMediaItemTransition(mediaItem: ExoMediaItem?, reason: Int) {
                val curIndex = exoPlayer.currentMediaItemIndex
                val queue = _state.value.playlist
                if (curIndex in queue.indices) {
                    _state.value = _state.value.copy(
                        currentItem = queue[curIndex],
                        currentIndex = curIndex
                    )
                }
            }
        })
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
                    val dur = exoPlayer.duration.coerceAtLeast(0L)
                    val subText = calculateActiveSubtitle(pos)
                    _state.value = _state.value.copy(
                        positionMs = pos,
                        durationMs = dur,
                        currentSubtitleText = subText
                    )
                }
                delay(200L)
            }
        }
    }

    private fun calculateActiveSubtitle(currentPos: Long): String? {
        val style = _state.value.subtitleStyle
        if (!style.isEnabled) return null

        val adjustedPos = currentPos + style.syncOffsetMs
        val cue = parsedSubtitles.firstOrNull { adjustedPos in it.startTimeMs..it.endTimeMs }
        return cue?.text
    }

    fun loadSubtitles(srtOrVttText: String) {
        parsedSubtitles = SubtitleParser.parse(srtOrVttText)
        _state.value = _state.value.copy(
            subtitleStyle = _state.value.subtitleStyle.copy(isEnabled = true),
            currentSubtitleText = calculateActiveSubtitle(_state.value.positionMs)
        )
    }

    fun updateSubtitleStyle(style: SubtitleStyle) {
        _state.value = _state.value.copy(subtitleStyle = style)
    }

    fun toggleSubtitles(enabled: Boolean) {
        _state.value = _state.value.copy(
            subtitleStyle = _state.value.subtitleStyle.copy(isEnabled = enabled)
        )
    }

    fun setSubtitleSyncOffset(offsetMs: Long) {
        _state.value = _state.value.copy(
            subtitleStyle = _state.value.subtitleStyle.copy(syncOffsetMs = offsetMs)
        )
    }

    fun setSubtitleFontSize(fontSizeSp: Float) {
        _state.value = _state.value.copy(
            subtitleStyle = _state.value.subtitleStyle.copy(fontSizeSp = fontSizeSp)
        )
    }

    fun setSubtitleTextColor(color: Color) {
        _state.value = _state.value.copy(
            subtitleStyle = _state.value.subtitleStyle.copy(textColor = color)
        )
    }

    fun toggleBackgroundPlayback(enabled: Boolean) {
        _state.value = _state.value.copy(isBackgroundPlaybackEnabled = enabled)
    }

    fun playQueue(items: List<MediaItem>, startIndex: Int = 0) {
        if (items.isEmpty()) return
        val clampedIndex = startIndex.coerceIn(0, items.size - 1)
        _state.value = _state.value.copy(
            playlist = items,
            currentIndex = clampedIndex,
            currentItem = items[clampedIndex]
        )

        exoPlayer.clearMediaItems()
        items.forEach { item ->
            val exoItem = ExoMediaItem.Builder()
                .setUri(item.mediaUri)
                .setMediaId(item.id.ifEmpty { item.mediaUri })
                .build()
            exoPlayer.addMediaItem(exoItem)
        }
        exoPlayer.seekTo(clampedIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun playSingle(item: MediaItem) {
        val currentQueue = _state.value.playlist.toMutableList()
        val existingIndex = currentQueue.indexOfFirst { it.mediaUri == item.mediaUri }
        if (existingIndex >= 0) {
            playQueue(currentQueue, existingIndex)
        } else {
            currentQueue.add(0, item)
            playQueue(currentQueue, 0)
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _state.value = _state.value.copy(
            positionMs = positionMs,
            currentSubtitleText = calculateActiveSubtitle(positionMs)
        )
    }

    fun seekForward(ms: Long = 10000L) {
        val target = (exoPlayer.currentPosition + ms).coerceAtMost(exoPlayer.duration)
        seekTo(target)
    }

    fun seekBack(ms: Long = 10000L) {
        val target = (exoPlayer.currentPosition - ms).coerceAtLeast(0L)
        seekTo(target)
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.25f, 3.0f)
        exoPlayer.playbackParameters = PlaybackParameters(clamped)
        _state.value = _state.value.copy(playbackSpeed = clamped)
    }

    fun playNext() {
        val queue = _state.value.playlist
        if (queue.isEmpty()) return
        val nextIndex = if (_state.value.isShuffle) {
            queue.indices.random()
        } else {
            (_state.value.currentIndex + 1) % queue.size
        }
        _state.value = _state.value.copy(
            currentIndex = nextIndex,
            currentItem = queue[nextIndex]
        )
        exoPlayer.seekTo(nextIndex, 0L)
        exoPlayer.play()
    }

    fun playPrevious() {
        val queue = _state.value.playlist
        if (queue.isEmpty()) return
        if (exoPlayer.currentPosition > 3000L) {
            seekTo(0L)
            return
        }
        val prevIndex = if (_state.value.currentIndex - 1 >= 0) {
            _state.value.currentIndex - 1
        } else {
            queue.size - 1
        }
        _state.value = _state.value.copy(
            currentIndex = prevIndex,
            currentItem = queue[prevIndex]
        )
        exoPlayer.seekTo(prevIndex, 0L)
        exoPlayer.play()
    }

    fun toggleRepeatMode() {
        val newMode = when (exoPlayer.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        exoPlayer.repeatMode = newMode
        _state.value = _state.value.copy(repeatMode = newMode)
    }

    fun toggleShuffle() {
        val newShuffle = !_state.value.isShuffle
        exoPlayer.shuffleModeEnabled = newShuffle
        _state.value = _state.value.copy(isShuffle = newShuffle)
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        exoPlayer.volume = clamped
        _state.value = _state.value.copy(volume = clamped)
    }

    fun cycleScaleMode() {
        val modes = VideoScaleMode.values()
        val curIndex = modes.indexOf(_state.value.scaleMode)
        val nextMode = modes[(curIndex + 1) % modes.size]
        _state.value = _state.value.copy(scaleMode = nextMode)
    }

    fun release() {
        progressJob?.cancel()
        exoPlayer.release()
    }
}
