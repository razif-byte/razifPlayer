package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CreatePlaylistDialog(
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onConfirm(name, description) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("confirm_create_playlist_button")
            ) {
                Text("Create", color = Color(0xFF001524), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF9CA3AF))
            }
        },
        title = {
            Text(
                "Create New Playlist",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist Name", color = Color(0xFF00E5FF)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF374151),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)", color = Color(0xFF00E5FF)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF374151),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        containerColor = Color(0xFF111827),
        shape = RoundedCornerShape(16.dp)
    )
}

data class SampleMediaPreset(
    val title: String,
    val artist: String,
    val uri: String,
    val type: String
)

@Composable
fun AddMediaDialog(
    onConfirm: (title: String, artist: String, uri: String, type: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var uri by remember { mutableStateOf("") }
    var mediaType by remember { mutableStateOf("VIDEO") }

    val presets = listOf(
        SampleMediaPreset(
            title = "Big Buck Bunny HD",
            artist = "Blender Animation",
            uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            type = "VIDEO"
        ),
        SampleMediaPreset(
            title = "Tears of Steel Sci-Fi",
            artist = "Blender VFX Project",
            uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            type = "VIDEO"
        ),
        SampleMediaPreset(
            title = "For Bigger Blazes",
            artist = "Nature Cinematic",
            uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            type = "VIDEO"
        ),
        SampleMediaPreset(
            title = "Beethoven Moonlight Sonata",
            artist = "Classical Masterpiece",
            uri = "https://ia800504.us.archive.org/3/items/MoonlightSonata_755/Beethoven-MoonlightSonata.mp3",
            type = "AUDIO"
        ),
        SampleMediaPreset(
            title = "Vivaldi Four Seasons: Spring",
            artist = "Antonio Vivaldi",
            uri = "https://ia800301.us.archive.org/15/items/Vivaldi-TheFourSeasons-Spring/01_Allegro.mp3",
            type = "AUDIO"
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onConfirm(title, artist, uri, mediaType) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                enabled = title.isNotBlank() && uri.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_media_button")
            ) {
                Text("Add Media", color = Color(0xFF001524), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF9CA3AF))
            }
        },
        title = {
            Text(
                "Add Audio or Video",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        },
        text = {
            Column {
                // Type selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { mediaType = "VIDEO" }
                    ) {
                        RadioButton(
                            selected = mediaType == "VIDEO",
                            onClick = { mediaType = "VIDEO" },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00E5FF))
                        )
                        Text("Video (MP4/MKV)", color = Color.White)
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { mediaType = "AUDIO" }
                    ) {
                        RadioButton(
                            selected = mediaType == "AUDIO",
                            onClick = { mediaType = "AUDIO" },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00E5FF))
                        )
                        Text("Audio (MP3/FLAC)", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title", color = Color(0xFF00E5FF)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF374151),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist / Channel", color = Color(0xFF00E5FF)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF374151),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uri,
                    onValueChange = { uri = it },
                    label = { Text("Media URL or File Path", color = Color(0xFF00E5FF)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF374151),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Or pick a sample preset:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF9CA3AF))
                )

                Spacer(modifier = Modifier.height(4.dp))

                presets.forEach { preset ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                title = preset.title
                                artist = preset.artist
                                uri = preset.uri
                                mediaType = preset.type
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = preset.type,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF00E5FF),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF111827),
        shape = RoundedCornerShape(16.dp)
    )
}
