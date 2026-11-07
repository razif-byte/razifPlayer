package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.MediaItem
import com.example.data.model.Playlist
import com.example.ui.components.Watermark3D

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    selectedPlaylist: Playlist?,
    currentItems: List<MediaItem>,
    onSelectPlaylist: (Playlist) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onAddMediaClick: () -> Unit,
    onPlayItem: (MediaItem) -> Unit,
    onPlayAll: (List<MediaItem>) -> Unit,
    onRemoveItem: (MediaItem) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = remember(currentItems, searchQuery) {
        if (searchQuery.isBlank()) currentItems
        else currentItems.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artist.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Playlists & Media",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Synced with Cloud Firestore",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF00E5FF))
                )
            }
            Watermark3D(text = "RazifApps®")
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Playlists horizontal selector + New button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(playlists) { pl ->
                    val isSelected = pl.id == selectedPlaylist?.id
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E293B),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectPlaylist(pl) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF001524) else Color(0xFF00E5FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = pl.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF001524) else Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Create Playlist Button
            IconButton(
                onClick = onCreatePlaylistClick,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B))
                    .testTag("create_playlist_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Playlist",
                    tint = Color(0xFF00E5FF)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Playlist Action Banner
        selectedPlaylist?.let { pl ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pl.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "${currentItems.size} items • ${pl.description.ifEmpty { "User Playlist" }}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                        )
                    }

                    Row {
                        Button(
                            onClick = { onPlayAll(currentItems) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(16.dp),
                            enabled = currentItems.isNotEmpty(),
                            modifier = Modifier.testTag("play_all_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF001524),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Play",
                                color = Color(0xFF001524),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onAddMediaClick,
                            modifier = Modifier.testTag("add_media_to_playlist_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlaylistAdd,
                                contentDescription = "Add Track",
                                tint = Color(0xFF00E5FF)
                            )
                        }

                        IconButton(onClick = { onDeletePlaylist(pl) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Playlist",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter / Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter items in playlist...", color = Color(0xFF64748B)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF1E293B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Media Items List
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = null,
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No media items in this playlist",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onAddMediaClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Text("Add Audio or Video", color = Color(0xFF00E5FF))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlayItem(item) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (item.mediaType == "VIDEO") Color(0x3300E5FF) else Color(0x339D4EDD)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.mediaType == "VIDEO") Icons.Default.Videocam else Icons.Default.Audiotrack,
                                    contentDescription = null,
                                    tint = if (item.mediaType == "VIDEO") Color(0xFF00E5FF) else Color(0xFFC77DFF),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${item.artist} • ${item.mediaType}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF)),
                                    maxLines = 1
                                )
                            }

                            IconButton(onClick = { onRemoveItem(item) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
