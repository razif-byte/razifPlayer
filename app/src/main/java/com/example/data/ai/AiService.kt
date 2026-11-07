package com.example.data.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "AiService"

data class GeneratedTrack(
    val title: String,
    val artist: String,
    val description: String,
    val mediaType: String = "AUDIO"
)

data class MusicIdea(
    val title: String,
    val genre: String,
    val tempoBpm: Int,
    val musicalKey: String,
    val instruments: List<String>,
    val mood: String,
    val lyricsOrNotes: String
)

class AiService {

    // General purpose Gemini 3.8 Flash model
    private val generalModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    // Transcription model (gemini-3.5-transcribe or gemini-3.8-flash fallback)
    private val transcribeModel by lazy {
        Firebase.ai.generativeModel("gemini-3.5-transcribe")
    }

    // Structured JSON model for smart playlist generation
    private val playlistModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "playlistName" to Schema.string(description = "Catchy playlist title"),
                "description" to Schema.string(description = "Playlist summary"),
                "tracks" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "title" to Schema.string(description = "Track title"),
                            "artist" to Schema.string(description = "Artist name"),
                            "description" to Schema.string(description = "Why this track fits"),
                            "mediaType" to Schema.string(description = "AUDIO or VIDEO")
                        ),
                        optionalProperties = listOf("mediaType")
                    ),
                    description = "List of recommended tracks"
                )
            )
        )

        val config = generationConfig {
            responseMimeType = "application/json"
            responseSchema = schema
        }

        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = config
        )
    }

    suspend fun getMediaInfoAndLyrics(title: String, artist: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are Razif Player's AI Media Assistant.
                Find and provide verified, up-to-date information, background story, interesting trivia, and lyrics/synopsis for:
                Title: "$title"
                Artist/Creator: "$artist"
                
                Format with clean headings:
                • Story & Background
                • Genre & Musical Style
                • Lyrics / Synopsis
                • Fun Trivia
            """.trimIndent()

            val response = withTimeout(45_000L) {
                generalModel.generateContent(prompt)
            }
            val text = response.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(IllegalStateException("No info received from Gemini"))
            }
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("Media info request timed out. Please check connection."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get media info", e)
            Result.failure(e)
        }
    }

    suspend fun generateSmartPlaylist(prompt: String): Result<Pair<String, List<GeneratedTrack>>> = withContext(Dispatchers.IO) {
        try {
            val response = withTimeout(45_000L) {
                playlistModel.generateContent("Create a playlist for: $prompt. Include 5 to 7 creative and real tracks.")
            }
            val jsonStr = response.text ?: throw IllegalStateException("Empty response")
            val json = JSONObject(jsonStr)
            val name = json.optString("playlistName", "AI Generated Mix")
            val tracksArray = json.optJSONArray("tracks") ?: JSONArray()
            val tracks = mutableListOf<GeneratedTrack>()
            for (i in 0 until tracksArray.length()) {
                val t = tracksArray.getJSONObject(i)
                tracks.add(
                    GeneratedTrack(
                        title = t.optString("title", "Untitled Track"),
                        artist = t.optString("artist", "Various Artists"),
                        description = t.optString("description", ""),
                        mediaType = t.optString("mediaType", "AUDIO").uppercase()
                    )
                )
            }
            Result.success(Pair(name, tracks))
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("Playlist generation timed out."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate playlist", e)
            Result.failure(e)
        }
    }

    suspend fun transcribeSpeechOrLyrics(spokenTextPrompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are Razif Player's Gemini Audio Transcription engine.
                The user has dictated audio or wants audio transcript formatting.
                Input: $spokenTextPrompt
                
                Please transcribe and refine the spoken audio clearly, fixing any phonetic ambiguities, adding punctuation, and providing clean formatted text or song lyrics.
            """.trimIndent()

            val response = withTimeout(45_000L) {
                try {
                    transcribeModel.generateContent(prompt)
                } catch (e: Exception) {
                    // Fallback to gemini-3.8-flash if model name is restricted
                    generalModel.generateContent(prompt)
                }
            }
            val text = response.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(IllegalStateException("No transcription generated"))
            }
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("Transcription timed out."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to transcribe", e)
            Result.failure(e)
        }
    }

    suspend fun generateMusicIdea(vibe: String, genre: String): Result<MusicIdea> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Act as an AI Music Generator (Lyria audio model synthesizer assistant).
                Generate a 30s audio clip blueprint and track structure for:
                Genre: $genre
                Vibe/Mood: $vibe
                
                Return JSON with fields:
                - title (string)
                - genre (string)
                - tempoBpm (integer)
                - musicalKey (string, e.g. "F# Minor")
                - instruments (array of strings)
                - mood (string)
                - lyricsOrNotes (string with beat structure, chords, and lyrics or melody description)
            """.trimIndent()

            val schema = Schema.obj(
                properties = mapOf(
                    "title" to Schema.string(),
                    "genre" to Schema.string(),
                    "tempoBpm" to Schema.integer(),
                    "musicalKey" to Schema.string(),
                    "instruments" to Schema.array(Schema.string()),
                    "mood" to Schema.string(),
                    "lyricsOrNotes" to Schema.string()
                )
            )

            val model = Firebase.ai.generativeModel(
                modelName = "gemini-3.8-flash",
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                    responseSchema = schema
                }
            )

            val response = withTimeout(45_000L) {
                model.generateContent(prompt)
            }
            val json = JSONObject(response.text ?: "{}")
            val instrumentsArray = json.optJSONArray("instruments") ?: JSONArray()
            val instruments = mutableListOf<String>()
            for (i in 0 until instrumentsArray.length()) {
                instruments.add(instrumentsArray.getString(i))
            }

            Result.success(
                MusicIdea(
                    title = json.optString("title", "Neon Synth Horizon"),
                    genre = json.optString("genre", genre),
                    tempoBpm = json.optInt("tempoBpm", 120),
                    musicalKey = json.optString("musicalKey", "C Major"),
                    instruments = instruments,
                    mood = json.optString("mood", vibe),
                    lyricsOrNotes = json.optString("lyricsOrNotes", "")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("Music generation timed out."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate music idea", e)
            Result.failure(e)
        }
    }
}
