package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class MediaType {
    AUDIO,
    VIDEO
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val playbackSpeed: Double = 1.0,
    val repeatMode: String = "ALL", // OFF, ONE, ALL
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Playlist(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val itemCount: Long = 0L,
    val isFavorite: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MediaItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val playlistId: String = "",
    val title: String = "",
    val artist: String = "Unknown Artist",
    val mediaUri: String = "",
    val mediaType: String = "AUDIO", // AUDIO or VIDEO
    val durationMs: Long = 0L,
    val playbackPositionMs: Long = 0L,
    val thumbnailUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
