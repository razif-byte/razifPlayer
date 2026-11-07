package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.AddMediaDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.SpeedControlDialog

enum class AppTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    VIDEO("Video", Icons.Default.Videocam),
    AUDIO("Audio", Icons.Default.Audiotrack),
    PLAYLISTS("Playlists", Icons.Default.QueueMusic),
    AI_STUDIO("AI Studio", Icons.Default.AutoAwesome),
    ACCOUNT("Account", Icons.Default.Person)
}

@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = AppTab.values()
    val playerState by viewModel.playerController.state.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
    val currentItems by viewModel.currentPlaylistItems.collectAsState()
    val showSpeedDialog by viewModel.showSpeedDialog.collectAsState()
    val showAddMediaDialog by viewModel.showAddMediaDialog.collectAsState()
    val showCreatePlaylistDialog by viewModel.showCreatePlaylistDialog.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    val videoItems = remember(currentItems) {
        currentItems.filter { it.mediaType == "VIDEO" }
    }
    val audioItems = remember(currentItems) {
        currentItems.filter { it.mediaType == "AUDIO" }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                // Mini-player bar when playing and not on the active player tab
                val showMiniPlayer = playerState.currentItem != null &&
                        ((selectedTab != 0 && playerState.currentItem?.mediaType == "VIDEO") ||
                         (selectedTab != 1 && playerState.currentItem?.mediaType == "AUDIO"))

                AnimatedVisibility(
                    visible = showMiniPlayer,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clickable {
                                selectedTab = if (playerState.currentItem?.mediaType == "VIDEO") 0 else 1
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (playerState.currentItem?.mediaType == "VIDEO") Icons.Default.Videocam else Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playerState.currentItem?.title ?: "",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${playerState.playbackSpeed}x • ${playerState.currentItem?.artist}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8)),
                                    maxLines = 1
                                )
                            }
                            IconButton(onClick = { viewModel.playerController.togglePlayPause() }) {
                                Icon(
                                    imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color(0xFF00E5FF)
                                )
                            }
                            IconButton(onClick = { viewModel.playerController.playNext() }) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // Bottom Navigation
                NavigationBar(
                    containerColor = Color(0xFF0D1322),
                    tonalElevation = 8.dp
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF001524),
                                selectedTextColor = Color(0xFF00E5FF),
                                indicatorColor = Color(0xFF00E5FF),
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B)
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.title.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> VideoPlayerScreen(
                    playerController = viewModel.playerController,
                    onOpenSpeedDialog = { viewModel.setSpeedDialogVisible(true) },
                    onSelectMedia = { viewModel.playItem(it) },
                    availableVideos = if (videoItems.isNotEmpty()) videoItems else currentItems
                )
                1 -> AudioPlayerScreen(
                    playerController = viewModel.playerController,
                    onOpenSpeedDialog = { viewModel.setSpeedDialogVisible(true) },
                    onAskAiInfo = {
                        viewModel.fetchMediaInfo(it)
                        selectedTab = 3 // Jump to AI Studio tab
                    },
                    audioTracks = if (audioItems.isNotEmpty()) audioItems else currentItems
                )
                2 -> PlaylistsScreen(
                    playlists = playlists,
                    selectedPlaylist = selectedPlaylist,
                    currentItems = currentItems,
                    onSelectPlaylist = { viewModel.selectPlaylist(it) },
                    onCreatePlaylistClick = { viewModel.setCreatePlaylistDialogVisible(true) },
                    onAddMediaClick = { viewModel.setAddMediaDialogVisible(true) },
                    onPlayItem = { item ->
                        viewModel.playItem(item)
                        selectedTab = if (item.mediaType == "VIDEO") 0 else 1
                    },
                    onPlayAll = { items ->
                        viewModel.playQueue(items, 0)
                        val firstType = items.firstOrNull()?.mediaType ?: "AUDIO"
                        selectedTab = if (firstType == "VIDEO") 0 else 1
                    },
                    onRemoveItem = { viewModel.removeItemFromPlaylist(it) },
                    onDeletePlaylist = { viewModel.deletePlaylist(it) }
                )
                3 -> AiToolsScreen(
                    viewModel = viewModel
                )
                4 -> AccountScreen(
                    playerController = viewModel.playerController,
                    onSignOutComplete = onSignOut
                )
            }
        }
    }

    // Dialogs
    if (showSpeedDialog) {
        SpeedControlDialog(
            currentSpeed = playerState.playbackSpeed,
            onSpeedSelected = { newSpeed ->
                viewModel.playerController.setPlaybackSpeed(newSpeed)
            },
            onDismiss = { viewModel.setSpeedDialogVisible(false) }
        )
    }

    if (showAddMediaDialog) {
        AddMediaDialog(
            onConfirm = { title, artist, uri, type ->
                viewModel.addMediaToCurrentPlaylist(title, artist, uri, type)
            },
            onDismiss = { viewModel.setAddMediaDialogVisible(false) }
        )
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onConfirm = { name, desc ->
                viewModel.createNewPlaylist(name, desc)
            },
            onDismiss = { viewModel.setCreatePlaylistDialogVisible(false) }
        )
    }
}
