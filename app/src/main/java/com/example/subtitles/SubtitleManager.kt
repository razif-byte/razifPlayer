package com.example.subtitles

import androidx.compose.ui.graphics.Color
import java.util.regex.Pattern

data class SubtitleCue(
    val index: Int,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String
)

data class SubtitleStyle(
    val isEnabled: Boolean = true,
    val fontSizeSp: Float = 18f,
    val textColor: Color = Color.White,
    val backgroundColor: Color = Color(0x99000000),
    val syncOffsetMs: Long = 0L // Negative = advance, Positive = delay
)

object SubtitleParser {

    /**
     * Parses standard SRT or WebVTT content into timed cues.
     */
    fun parse(content: String): List<SubtitleCue> {
        val cues = mutableListOf<SubtitleCue>()
        if (content.isBlank()) return cues

        val cleaned = content.replace("\r\n", "\n").replace("\r", "\n")
        val blocks = cleaned.split("\n\n")

        val timePattern = Pattern.compile("(\\d{1,2}):(\\d{2}):(\\d{2})[,.](\\d{3})\\s*-->\\s*(\\d{1,2}):(\\d{2}):(\\d{2})[,.](\\d{3})")

        var autoIndex = 1
        for (block in blocks) {
            val lines = block.trim().lines()
            if (lines.isEmpty()) continue

            var timeLineIndex = -1
            var matcher = timePattern.matcher("")

            for (i in lines.indices) {
                matcher = timePattern.matcher(lines[i])
                if (matcher.find()) {
                    timeLineIndex = i
                    break
                }
            }

            if (timeLineIndex != -1) {
                try {
                    val startH = matcher.group(1)?.toLong() ?: 0L
                    val startM = matcher.group(2)?.toLong() ?: 0L
                    val startS = matcher.group(3)?.toLong() ?: 0L
                    val startMs = matcher.group(4)?.toLong() ?: 0L

                    val endH = matcher.group(5)?.toLong() ?: 0L
                    val endM = matcher.group(6)?.toLong() ?: 0L
                    val endS = matcher.group(7)?.toLong() ?: 0L
                    val endMs = matcher.group(8)?.toLong() ?: 0L

                    val startTime = (startH * 3600 + startM * 60 + startS) * 1000 + startMs
                    val endTime = (endH * 3600 + endM * 60 + endS) * 1000 + endMs

                    val textLines = lines.subList(timeLineIndex + 1, lines.size)
                    val text = textLines.joinToString("\n").replace(Regex("<[^>]*>"), "").trim()

                    if (text.isNotEmpty()) {
                        cues.add(SubtitleCue(autoIndex++, startTime, endTime, text))
                    }
                } catch (_: Exception) {}
            }
        }
        return cues
    }

    /**
     * Bundled sample subtitles for Big Buck Bunny and default videos
     */
    val sampleSubtitlesSrt = """
1
00:00:01,000 --> 00:00:05,000
[Razif Player] Welcome to High-Definition Video Playback

2
00:00:06,000 --> 00:00:10,000
A tranquil morning begins in the magical animated forest.

3
00:00:11,500 --> 00:00:16,000
Big Buck Bunny wakes up to enjoy the fresh butterflies.

4
00:00:17,000 --> 00:00:23,000
Subtitles synchronized seamlessly in real-time with playback speed controls!

5
00:00:24,000 --> 00:00:30,000
Watermarked with 3D 50% transparent RazifApps® brand overlay.
    """.trimIndent()
}
