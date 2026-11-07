package com.example

import com.example.subtitles.SubtitleParser
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun subtitleParser_parsesCuesCorrectly() {
        val srt = """
            1
            00:00:01,000 --> 00:00:04,500
            Hello world subtitle

            2
            00:00:05,000 --> 00:00:09,000
            Second synchronized cue
        """.trimIndent()

        val cues = SubtitleParser.parse(srt)
        assertEquals(2, cues.size)
        assertEquals(1000L, cues[0].startTimeMs)
        assertEquals(4500L, cues[0].endTimeMs)
        assertEquals("Hello world subtitle", cues[0].text)
        assertEquals(5000L, cues[1].startTimeMs)
        assertEquals("Second synchronized cue", cues[1].text)
    }
}
