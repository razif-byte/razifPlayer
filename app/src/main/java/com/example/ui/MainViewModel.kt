package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiService
import com.example.data.ai.GeneratedTrack
import com.example.data.ai.MusicIdea
import com.example.data.model.MediaItem
import com.example.data.model.Playlist
import com.example.data.repository.MediaRepository
import com.example.player.PlayerController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class InfoSuccess(val content: String) : AiUiState
    data class PlaylistSuccess(val name: String, val tracks: List<GeneratedTrack>) : AiUiState
    data class TranscribeSuccess(val transcript: String) : AiUiState
    data class MusicIdeaSuccess(val idea: MusicIdea) : AiUiState
    data class Error(val message: String) : AiUiState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)
    private val aiService = AiService()
    val playerController = PlayerController(application)

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _currentPlaylistItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val currentPlaylistItems: StateFlow<List<MediaItem>> = _currentPlaylistItems.asStateFlow()

    private val _history = MutableStateFlow<List<MediaItem>>(emptyList())
    val history: StateFlow<List<MediaItem>> = _history.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    private val _aiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val aiState: StateFlow<AiUiState> = _aiState.asStateFlow()

    private val _showSpeedDialog = MutableStateFlow(false)
    val showSpeedDialog: StateFlow<Boolean> = _showSpeedDialog.asStateFlow()

    private val _showAddMediaDialog = MutableStateFlow(false)
    val showAddMediaDialog: StateFlow<Boolean> = _showAddMediaDialog.asStateFlow()

    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val currentUserId: String?
        get() = Firebase.auth.currentUser?.uid

    init {
        currentUserId?.let { uid ->
            loadUserData(uid)
        }
    }

    fun loadUserData(userId: String) {
        viewModelScope.launch {
            try {
                repository.seedDefaultDataIfEmpty(userId)
            } catch (_: Exception) {}

            repository.observePlaylists(userId)
                .catch { emit(emptyList()) }
                .collect { list ->
                    _playlists.value = list
                    if (_selectedPlaylist.value == null && list.isNotEmpty()) {
                        selectPlaylist(list.first())
                    }
                }
        }

        viewModelScope.launch {
            repository.observeHistory(userId)
                .catch { emit(emptyList()) }
                .collect { list ->
                    _history.value = list
                }
        }
    }

    fun selectPlaylist(playlist: Playlist) {
        _selectedPlaylist.value = playlist
        currentUserId?.let { uid ->
            viewModelScope.launch {
                repository.observePlaylistItems(uid, playlist.id)
                    .catch { emit(emptyList()) }
                    .collect { items ->
                        _currentPlaylistItems.value = items
                    }
            }
        }
    }

    fun createNewPlaylist(name: String, description: String = "") {
        val uid = currentUserId ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                repository.createPlaylist(uid, name, description)
                _showCreatePlaylistDialog.value = false
                _userMessage.value = "Playlist \"$name\" created!"
            } catch (e: Exception) {
                _userMessage.value = "Failed to create playlist: ${e.message}"
            }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            try {
                repository.deletePlaylist(uid, playlist.id)
                if (_selectedPlaylist.value?.id == playlist.id) {
                    _selectedPlaylist.value = null
                    _currentPlaylistItems.value = emptyList()
                }
                _userMessage.value = "Playlist deleted"
            } catch (e: Exception) {
                _userMessage.value = "Failed to delete: ${e.message}"
            }
        }
    }

    fun addMediaToCurrentPlaylist(
        title: String,
        artist: String,
        uri: String,
        mediaType: String
    ) {
        val uid = currentUserId ?: return
        val currentPl = _selectedPlaylist.value
        val playlistId = currentPl?.id ?: return
        viewModelScope.launch {
            try {
                val item = MediaItem(
                    userId = uid,
                    playlistId = playlistId,
                    title = title,
                    artist = artist.ifEmpty { "Unknown Artist" },
                    mediaUri = uri,
                    mediaType = mediaType
                )
                repository.addPlaylistItem(uid, playlistId, item)
                _showAddMediaDialog.value = false
                _userMessage.value = "Added to ${currentPl.name}!"
            } catch (e: Exception) {
                _userMessage.value = "Failed to add item: ${e.message}"
            }
        }
    }

    fun removeItemFromPlaylist(item: MediaItem) {
        val uid = currentUserId ?: return
        val playlistId = _selectedPlaylist.value?.id ?: item.playlistId
        if (playlistId.isEmpty() || item.id.isEmpty()) return
        viewModelScope.launch {
            try {
                repository.removePlaylistItem(uid, playlistId, item.id)
                _userMessage.value = "Removed ${item.title}"
            } catch (e: Exception) {
                _userMessage.value = "Failed to remove: ${e.message}"
            }
        }
    }

    fun playItem(item: MediaItem, queue: List<MediaItem>? = null) {
        val activeQueue = queue ?: _currentPlaylistItems.value
        val idx = activeQueue.indexOfFirst { it.mediaUri == item.mediaUri }
        if (idx >= 0) {
            playerController.playQueue(activeQueue, idx)
        } else {
            playerController.playSingle(item)
        }

        currentUserId?.let { uid ->
            viewModelScope.launch {
                try {
                    repository.addToHistory(uid, item)
                } catch (_: Exception) {}
            }
        }
    }

    fun playQueue(items: List<MediaItem>, index: Int = 0) {
        if (items.isNotEmpty()) {
            playerController.playQueue(items, index)
            currentUserId?.let { uid ->
                viewModelScope.launch {
                    try {
                        repository.addToHistory(uid, items[index.coerceIn(0, items.size - 1)])
                    } catch (_: Exception) {}
                }
            }
        }
    }

    // AI & Search features
    fun fetchMediaInfo(item: MediaItem) {
        _aiState.value = AiUiState.Loading
        viewModelScope.launch {
            val result = aiService.getMediaInfoAndLyrics(item.title, item.artist)
            result.onSuccess { info ->
                _aiState.value = AiUiState.InfoSuccess(info)
            }.onFailure { err ->
                _aiState.value = AiUiState.Error(err.message ?: "Failed to get AI info")
            }
        }
    }

    fun generateSmartPlaylist(moodPrompt: String) {
        if (moodPrompt.isBlank()) return
        _aiState.value = AiUiState.Loading
        viewModelScope.launch {
            val result = aiService.generateSmartPlaylist(moodPrompt)
            result.onSuccess { (name, tracks) ->
                _aiState.value = AiUiState.PlaylistSuccess(name, tracks)
            }.onFailure { err ->
                _aiState.value = AiUiState.Error(err.message ?: "Failed to generate playlist")
            }
        }
    }

    fun transcribeAudioText(dictationOrLyrics: String) {
        if (dictationOrLyrics.isBlank()) return
        _aiState.value = AiUiState.Loading
        viewModelScope.launch {
            val result = aiService.transcribeSpeechOrLyrics(dictationOrLyrics)
            result.onSuccess { transcript ->
                _aiState.value = AiUiState.TranscribeSuccess(transcript)
            }.onFailure { err ->
                _aiState.value = AiUiState.Error(err.message ?: "Failed to transcribe audio")
            }
        }
    }

    fun generateMusicIdea(vibe: String, genre: String) {
        _aiState.value = AiUiState.Loading
        viewModelScope.launch {
            val result = aiService.generateMusicIdea(vibe, genre)
            result.onSuccess { idea ->
                _aiState.value = AiUiState.MusicIdeaSuccess(idea)
            }.onFailure { err ->
                _aiState.value = AiUiState.Error(err.message ?: "Failed to generate music idea")
            }
        }
    }

    fun saveGeneratedPlaylist(name: String, tracks: List<GeneratedTrack>) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            try {
                val plId = repository.createPlaylist(uid, name, "AI Curated Playlist")
                tracks.forEach { t ->
                    repository.addPlaylistItem(
                        uid,
                        plId,
                        MediaItem(
                            title = t.title,
                            artist = t.artist,
                            mediaUri = "https://ia800504.us.archive.org/3/items/MoonlightSonata_755/Beethoven-MoonlightSonata.mp3",
                            mediaType = t.mediaType
                        )
                    )
                }
                _userMessage.value = "Playlist \"$name\" saved to your library!"
            } catch (e: Exception) {
                _userMessage.value = "Failed to save: ${e.message}"
            }
        }
    }

    fun setSpeedDialogVisible(show: Boolean) {
        _showSpeedDialog.value = show
    }

    fun setAddMediaDialogVisible(show: Boolean) {
        _showAddMediaDialog.value = show
    }

    fun setCreatePlaylistDialogVisible(show: Boolean) {
        _showCreatePlaylistDialog.value = show
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun clearAiState() {
        _aiState.value = AiUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}
