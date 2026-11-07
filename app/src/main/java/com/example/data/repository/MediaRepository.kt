package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.MediaItem
import com.example.data.model.Playlist
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class MediaRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    fun observePlaylists(userId: String): Flow<List<Playlist>> = flow {
        val path = "users/$userId/playlists"
        emitAll(
            db.collection("users").document(userId).collection("playlists")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Playlist::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createPlaylist(userId: String, name: String, description: String = "", isFavorite: Boolean = false): String {
        val docRef = db.collection("users").document(userId).collection("playlists").document()
        val payload = mapOf(
            "userId" to userId,
            "name" to name.trim(),
            "description" to description.trim(),
            "itemCount" to 0L,
            "isFavorite" to isFavorite,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deletePlaylist(userId: String, playlistId: String) {
        val docRef = db.collection("users").document(userId).collection("playlists").document(playlistId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    fun observePlaylistItems(userId: String, playlistId: String): Flow<List<MediaItem>> = flow {
        val path = "users/$userId/playlists/$playlistId/items"
        emitAll(
            db.collection("users").document(userId).collection("playlists").document(playlistId)
                .collection("items")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(MediaItem::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addPlaylistItem(userId: String, playlistId: String, item: MediaItem) {
        val itemsCol = db.collection("users").document(userId).collection("playlists").document(playlistId).collection("items")
        val docRef = itemsCol.document()
        val payload = mapOf(
            "userId" to userId,
            "playlistId" to playlistId,
            "title" to item.title.trim(),
            "artist" to item.artist.trim(),
            "mediaUri" to item.mediaUri.trim(),
            "mediaType" to item.mediaType,
            "durationMs" to item.durationMs,
            "playbackPositionMs" to 0L,
            "thumbnailUrl" to item.thumbnailUrl,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            // Increment playlist item count
            val playlistRef = db.collection("users").document(userId).collection("playlists").document(playlistId)
            playlistRef.update("itemCount", FieldValue.increment(1L), "updatedAt", FieldValue.serverTimestamp()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun removePlaylistItem(userId: String, playlistId: String, itemId: String) {
        val docRef = db.collection("users").document(userId).collection("playlists").document(playlistId).collection("items").document(itemId)
        try {
            docRef.delete().await()
            val playlistRef = db.collection("users").document(userId).collection("playlists").document(playlistId)
            playlistRef.update("itemCount", FieldValue.increment(-1L), "updatedAt", FieldValue.serverTimestamp()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    fun observeHistory(userId: String): Flow<List<MediaItem>> = flow {
        val path = "users/$userId/history"
        emitAll(
            db.collection("users").document(userId).collection("history")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(30)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(MediaItem::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addToHistory(userId: String, item: MediaItem) {
        val docRef = db.collection("users").document(userId).collection("history").document()
        val payload = mapOf(
            "userId" to userId,
            "playlistId" to "history",
            "title" to item.title.trim(),
            "artist" to item.artist.trim(),
            "mediaUri" to item.mediaUri.trim(),
            "mediaType" to item.mediaType,
            "durationMs" to item.durationMs,
            "playbackPositionMs" to item.playbackPositionMs,
            "thumbnailUrl" to item.thumbnailUrl,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun seedDefaultDataIfEmpty(userId: String) {
        val playlistsRef = db.collection("users").document(userId).collection("playlists")
        val snapshot = playlistsRef.limit(1).get().await()
        if (snapshot.isEmpty) {
            // Seed a "Popular Videos" playlist
            val videoPlaylistId = createPlaylist(userId, "Popular Videos", "HD Videos with speed controls", false)
            addPlaylistItem(
                userId,
                videoPlaylistId,
                MediaItem(
                    title = "Big Buck Bunny Animation",
                    artist = "Blender Foundation",
                    mediaUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    mediaType = "VIDEO",
                    thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg",
                    durationMs = 596000L
                )
            )
            addPlaylistItem(
                userId,
                videoPlaylistId,
                MediaItem(
                    title = "For Bigger Blazes (Nature)",
                    artist = "Google TV Showcase",
                    mediaUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    mediaType = "VIDEO",
                    thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerBlazes.jpg",
                    durationMs = 15000L
                )
            )
            addPlaylistItem(
                userId,
                videoPlaylistId,
                MediaItem(
                    title = "Elephants Dream Sci-Fi",
                    artist = "Orange Open Movie Project",
                    mediaUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    mediaType = "VIDEO",
                    thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg",
                    durationMs = 653000L
                )
            )

            // Seed a "Featured Audio" playlist
            val audioPlaylistId = createPlaylist(userId, "Classical Chillout", "Peaceful classical pieces for relaxation", true)
            addPlaylistItem(
                userId,
                audioPlaylistId,
                MediaItem(
                    title = "Moonlight Sonata (Adagio)",
                    artist = "Ludwig van Beethoven",
                    mediaUri = "https://ia800504.us.archive.org/3/items/MoonlightSonata_755/Beethoven-MoonlightSonata.mp3",
                    mediaType = "AUDIO",
                    durationMs = 315000L
                )
            )
            addPlaylistItem(
                userId,
                audioPlaylistId,
                MediaItem(
                    title = "Für Elise (Bagatelle in A Minor)",
                    artist = "Ludwig van Beethoven",
                    mediaUri = "https://ia800108.us.archive.org/11/items/FurElise_485/Beethoven-FurElise.mp3",
                    mediaType = "AUDIO",
                    durationMs = 178000L
                )
            )
            addPlaylistItem(
                userId,
                audioPlaylistId,
                MediaItem(
                    title = "The Four Seasons: Spring",
                    artist = "Antonio Vivaldi",
                    mediaUri = "https://ia800301.us.archive.org/15/items/Vivaldi-TheFourSeasons-Spring/01_Allegro.mp3",
                    mediaType = "AUDIO",
                    durationMs = 216000L
                )
            )
            addPlaylistItem(
                userId,
                audioPlaylistId,
                MediaItem(
                    title = "Canon in D Major",
                    artist = "Johann Pachelbel",
                    mediaUri = "https://ia800201.us.archive.org/12/items/Pachelbel-CanonInD/Pachelbel-CanonInD.mp3",
                    mediaType = "AUDIO",
                    durationMs = 360000L
                )
            )
        }
    }
}
