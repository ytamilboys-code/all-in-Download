package com.example

import com.example.downloader.ExtractedVideo
import com.example.downloader.VideoQualityOption
import com.example.model.DownloadStatus
import com.example.model.Platform
import com.example.model.VideoItem
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testExtractedVideoPreviewProperties() {
        val extracted = ExtractedVideo(
            title = "Test Video",
            platform = Platform.YOUTUBE,
            originalUrl = "https://youtube.com/watch?v=123",
            downloadUrl = "https://example.com/video.mp4",
            thumbnailUrl = "https://example.com/thumb.jpg",
            duration = "03:15",
            qualities = listOf(
                VideoQualityOption("1080p HD", "1920x1080", "mp4", "45 MB", "https://example.com/1080.mp4"),
                VideoQualityOption("720p", "1280x720", "mp4", "22 MB", "https://example.com/720.mp4")
            )
        )

        assertEquals(Platform.YOUTUBE, extracted.platform)
        assertEquals(2, extracted.qualities.size)

        val previewItem = VideoItem(
            id = -1L,
            title = extracted.title,
            originalUrl = extracted.originalUrl,
            downloadUrl = extracted.qualities.first().streamUrl,
            thumbnailUrl = extracted.thumbnailUrl,
            platform = extracted.platform,
            duration = extracted.duration,
            quality = extracted.qualities.first().label,
            fileSizeFormatted = extracted.qualities.first().estimatedSize,
            localFilePath = null,
            status = DownloadStatus.IDLE
        )

        assertEquals(-1L, previewItem.id)
        assertNull(previewItem.localFilePath)
        assertEquals("https://example.com/1080.mp4", previewItem.downloadUrl)
    }

    @Test
    fun testClipboardUrlExtractionTrimming() {
        val rawClipboard = "   https://instagram.com/reel/12345/   \n"
        val cleanUrl = rawClipboard.trim()
        assertEquals("https://instagram.com/reel/12345/", cleanUrl)
        assertTrue(cleanUrl.startsWith("https://"))
    }
}
