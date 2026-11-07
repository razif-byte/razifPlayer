package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.GeneratedTrack
import com.example.ui.AiUiState
import com.example.ui.MainViewModel
import com.example.ui.components.Watermark3D

@Composable
fun AiToolsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val aiState by viewModel.aiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("AI Playlist", "Transcribe", "Music Studio", "Track Grounding")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Gemini AI Studio",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Powered by Firebase AI Logic",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF00E5FF))
                )
            }
            Watermark3D(text = "RazifApps®")
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF131B2A),
            contentColor = Color(0xFF00E5FF),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFF00E5FF)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        viewModel.clearAiState()
                    },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) Color(0xFF00E5FF) else Color(0xFF94A3B8)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTab) {
                0 -> AiPlaylistSection(viewModel, aiState)
                1 -> TranscribeSection(viewModel, aiState)
                2 -> MusicStudioSection(viewModel, aiState)
                3 -> TrackGroundingSection(viewModel, aiState)
            }
        }
    }
}

@Composable
private fun AiPlaylistSection(viewModel: MainViewModel, state: AiUiState) {
    var prompt by remember { mutableStateOf("Chill lofi beats for night driving") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Generate Themed Playlist",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Text(
            text = "Describe any mood, genre, tempo, or activity for Gemini to curate.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            placeholder = { Text("e.g. 140bpm upbeat workout videos", color = Color(0xFF64748B)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF1E293B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF111827),
                unfocusedContainerColor = Color(0xFF111827)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.generateSmartPlaylist(prompt) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("generate_smart_playlist_button"),
            enabled = state !is AiUiState.Loading
        ) {
            if (state is AiUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF001524))
            } else {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = null,
                    tint = Color(0xFF001524)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Curate Playlist with Gemini", color = Color(0xFF001524), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state) {
            is AiUiState.PlaylistSuccess -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF)
                                )
                            )
                            Button(
                                onClick = { viewModel.saveGeneratedPlaylist(state.name, state.tracks) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkAdd,
                                    contentDescription = null,
                                    tint = Color(0xFF001524),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", color = Color(0xFF001524), fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        state.tracks.forEachIndexed { i, t ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${i + 1}.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF00E5FF)),
                                    modifier = Modifier.width(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "${t.title} - ${t.artist}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    if (t.description.isNotEmpty()) {
                                        Text(
                                            text = t.description,
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is AiUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun TranscribeSection(viewModel: MainViewModel, state: AiUiState) {
    var dictation by remember {
        mutableStateOf("Play Beethoven Moonlight Sonata third movement presto agitato at one point five speed")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Speech & Audio Transcription",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Text(
            text = "Uses model gemini-3.5-transcribe to transcribe and refine lyrics or voice commands.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = dictation,
            onValueChange = { dictation = it },
            placeholder = { Text("Input speech or voice dictation...", color = Color(0xFF64748B)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF1E293B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF111827),
                unfocusedContainerColor = Color(0xFF111827)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.transcribeAudioText(dictation) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("transcribe_audio_button"),
            enabled = state !is AiUiState.Loading
        ) {
            if (state is AiUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF001524))
            } else {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color(0xFF001524)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Transcribe with Gemini 3.5", color = Color(0xFF001524), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state) {
            is AiUiState.TranscribeSuccess -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Transcription Result:",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.transcript,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                        )
                    }
                }
            }
            is AiUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun MusicStudioSection(viewModel: MainViewModel, state: AiUiState) {
    var genre by remember { mutableStateOf("Synthwave / Cyberpunk") }
    var vibe by remember { mutableStateOf("Fast energetic racing rhythm with heavy bass") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Generate Music & Lyrics (Lyria Preview)",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Text(
            text = "Compose 30s clips or full-length track structures with Gemini audio intelligence.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = genre,
            onValueChange = { genre = it },
            label = { Text("Genre", color = Color(0xFF00E5FF)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF1E293B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF111827),
                unfocusedContainerColor = Color(0xFF111827)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = vibe,
            onValueChange = { vibe = it },
            label = { Text("Vibe & Mood", color = Color(0xFF00E5FF)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF1E293B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF111827),
                unfocusedContainerColor = Color(0xFF111827)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.generateMusicIdea(vibe, genre) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("generate_music_idea_button"),
            enabled = state !is AiUiState.Loading
        ) {
            if (state is AiUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF001524))
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFF001524)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Music Blueprint", color = Color(0xFF001524), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state) {
            is AiUiState.MusicIdeaSuccess -> {
                val idea = state.idea
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = idea.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        )
                        Text(
                            text = "${idea.genre} • ${idea.tempoBpm} BPM • Key: ${idea.musicalKey}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Instruments: ${idea.instruments.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Composition & Lyrics:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        )
                        Text(
                            text = idea.lyricsOrNotes,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                        )
                    }
                }
            }
            is AiUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun TrackGroundingSection(viewModel: MainViewModel, state: AiUiState) {
    var searchSong by remember { mutableStateOf("Bohemian Rhapsody") }
    var searchArtist by remember { mutableStateOf("Queen") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Track Grounding & Search",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Text(
            text = "Use Gemini with Google Search data grounding for song origins, lyrics, and trivia.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = searchSong,
                onValueChange = { searchSong = it },
                label = { Text("Song Title", color = Color(0xFF00E5FF)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF1E293B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF111827),
                    unfocusedContainerColor = Color(0xFF111827)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = searchArtist,
                onValueChange = { searchArtist = it },
                label = { Text("Artist", color = Color(0xFF00E5FF)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF1E293B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF111827),
                    unfocusedContainerColor = Color(0xFF111827)
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                viewModel.fetchMediaInfo(
                    com.example.data.model.MediaItem(
                        title = searchSong,
                        artist = searchArtist,
                        mediaUri = ""
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("grounding_search_button"),
            enabled = state !is AiUiState.Loading
        ) {
            if (state is AiUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF001524))
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF001524)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search Grounded Info", color = Color(0xFF001524), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state) {
            is AiUiState.InfoSuccess -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Grounded Information & Lyrics:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.content,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                        )
                    }
                }
            }
            is AiUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            else -> {}
        }
    }
}
