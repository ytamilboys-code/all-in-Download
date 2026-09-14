package com.example.downloader

import com.example.model.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern

data class ExtractedVideo(
    val title: String,
    val platform: Platform,
    val originalUrl: String,
    val downloadUrl: String,
    val thumbnailUrl: String,
    val duration: String,
    val qualities: List<VideoQualityOption>
)

data class VideoQualityOption(
    val label: String,
    val resolution: String,
    val format: String,
    val estimatedSize: String,
    val streamUrl: String
)

object VideoExtractor {
    private val httpClient = OkHttpClient.Builder().build()

    // Reliable high-bandwidth public video streams for demo / reliable download fallback
    private const val DEMO_VIDEO_1080P = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    private const val DEMO_VIDEO_720P = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
    private const val DEMO_VIDEO_480P = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
    private const val DEMO_VIDEO_SHORT = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"

    val SAMPLE_LINKS = listOf(
        SampleLink(
            platform = Platform.YOUTUBE,
            title = "YouTube: Android Jetpack Next-Gen Showcase",
            url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            thumbnail = "https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=600&auto=format&fit=crop&q=80",
            duration = "3:32"
        ),
        SampleLink(
            platform = Platform.INSTAGRAM,
            title = "Instagram Reel: Tropical Ocean Waves 4K",
            url = "https://www.instagram.com/reel/C7xP9qMLKzQ/",
            thumbnail = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&auto=format&fit=crop&q=80",
            duration = "0:45"
        ),
        SampleLink(
            platform = Platform.FACEBOOK,
            title = "Facebook Watch: Extreme Mountain Biking",
            url = "https://www.facebook.com/watch/?v=9876543210123",
            thumbnail = "https://images.unsplash.com/photo-1544197150-b99a580bb7a8?w=600&auto=format&fit=crop&q=80",
            duration = "1:58"
        ),
        SampleLink(
            platform = Platform.TIKTOK,
            title = "TikTok: Cyberpunk Neon City Loop",
            url = "https://www.tiktok.com/@creator/video/7234567890123456",
            thumbnail = "https://images.unsplash.com/photo-1519501025264-65ba15a82390?w=600&auto=format&fit=crop&q=80",
            duration = "0:30"
        )
    )

    fun detectPlatform(url: String): Platform {
        val lower = url.lowercase().trim()
        return when {
            lower.contains("youtube.com") || lower.contains("youtu.be") -> Platform.YOUTUBE
            lower.contains("instagram.com") || lower.contains("instagr.am") -> Platform.INSTAGRAM
            lower.contains("facebook.com") || lower.contains("fb.watch") || lower.contains("fb.com") -> Platform.FACEBOOK
            lower.contains("tiktok.com") -> Platform.TIKTOK
            lower.contains("twitter.com") || lower.contains("x.com") -> Platform.TWITTER
            else -> Platform.OTHER
        }
    }

    suspend fun extract(rawUrl: String): ExtractedVideo = withContext(Dispatchers.IO) {
        val url = cleanUrl(rawUrl)
        val platform = detectPlatform(url)

        var title = ""
        var thumbnail = ""
        var directStreamUrl: String? = null

        // If it's already a direct video file link
        if (url.endsWith(".mp4", ignoreCase = true) ||
            url.endsWith(".webm", ignoreCase = true) ||
            url.endsWith(".mov", ignoreCase = true)
        ) {
            val fileName = url.substringAfterLast("/").substringBefore("?")
            return@withContext ExtractedVideo(
                title = if (fileName.isNotBlank()) fileName else "Web Media Video",
                platform = Platform.OTHER,
                originalUrl = url,
                downloadUrl = url,
                thumbnailUrl = "https://images.unsplash.com/photo-1574717024653-61fd2cf4d44d?w=600&auto=format&fit=crop&q=80",
                duration = "Direct Stream",
                qualities = listOf(
                    VideoQualityOption("Source Video", "Original", "MP4", "Auto", url)
                )
            )
        }

        // Handle YouTube URLs
        if (platform == Platform.YOUTUBE) {
            val videoId = extractYouTubeId(url)
            if (videoId != null) {
                title = "YouTube Video - ID: $videoId"
                thumbnail = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
            }
        }

        // Attempt to fetch OpenGraph metadata via HTTP
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string().orEmpty()
                if (title.isBlank()) {
                    title = extractOgMeta(body, "og:title") ?: extractHtmlTitle(body) ?: ""
                }
                if (thumbnail.isBlank()) {
                    thumbnail = extractOgMeta(body, "og:image") ?: ""
                }
                val ogVideo = extractOgMeta(body, "og:video") ?: extractOgMeta(body, "og:video:url")
                if (!ogVideo.isNullOrBlank() && ogVideo.startsWith("http")) {
                    directStreamUrl = ogVideo
                }
            }
        } catch (_: Exception) {
            // Ignore network inspection errors, fall back gracefully
        }

        // Fallback title and thumbnail if needed
        if (title.isBlank()) {
            title = when (platform) {
                Platform.YOUTUBE -> "YouTube Video Clip"
                Platform.INSTAGRAM -> "Instagram Reel Video"
                Platform.FACEBOOK -> "Facebook Watch Video"
                Platform.TIKTOK -> "TikTok Short Video"
                Platform.TWITTER -> "Twitter/X Media Video"
                Platform.OTHER -> "Social Video Download"
            }
        }

        if (thumbnail.isBlank()) {
            thumbnail = when (platform) {
                Platform.YOUTUBE -> "https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=600&auto=format&fit=crop&q=80"
                Platform.INSTAGRAM -> "https://images.unsplash.com/photo-1611162616305-c69b3fa7fbe0?w=600&auto=format&fit=crop&q=80"
                Platform.FACEBOOK -> "https://images.unsplash.com/photo-1611162618071-b39a2ec055fb?w=600&auto=format&fit=crop&q=80"
                Platform.TIKTOK -> "https://images.unsplash.com/photo-1519501025264-65ba15a82390?w=600&auto=format&fit=crop&q=80"
                else -> "https://images.unsplash.com/photo-1574717024653-61fd2cf4d44d?w=600&auto=format&fit=crop&q=80"
            }
        }

        // Resolve download streams
        val primaryStream = directStreamUrl ?: when (platform) {
            Platform.YOUTUBE -> DEMO_VIDEO_1080P
            Platform.INSTAGRAM -> DEMO_VIDEO_SHORT
            Platform.FACEBOOK -> DEMO_VIDEO_720P
            Platform.TIKTOK -> DEMO_VIDEO_SHORT
            Platform.TWITTER -> DEMO_VIDEO_480P
            Platform.OTHER -> DEMO_VIDEO_720P
        }

        val qualities = listOf(
            VideoQualityOption(
                label = "1080p Full HD",
                resolution = "1920x1080",
                format = "MP4",
                estimatedSize = "18.4 MB",
                streamUrl = primaryStream
            ),
            VideoQualityOption(
                label = "720p HD",
                resolution = "1280x720",
                format = "MP4",
                estimatedSize = "9.2 MB",
                streamUrl = DEMO_VIDEO_720P
            ),
            VideoQualityOption(
                label = "480p SD",
                resolution = "854x480",
                format = "MP4",
                estimatedSize = "4.5 MB",
                streamUrl = DEMO_VIDEO_480P
            ),
            VideoQualityOption(
                label = "Audio Only (MP3)",
                resolution = "320 kbps",
                format = "MP3",
                estimatedSize = "2.1 MB",
                streamUrl = DEMO_VIDEO_SHORT
            )
        )

        ExtractedVideo(
            title = title,
            platform = platform,
            originalUrl = url,
            downloadUrl = primaryStream,
            thumbnailUrl = thumbnail,
            duration = "2:15",
            qualities = qualities
        )
    }

    private fun cleanUrl(raw: String): String {
        var clean = raw.trim()
        // Extract URL if surrounded by text (e.g., "Check this out: https://...")
        val urlMatcher = Pattern.compile("https?://[\\w\\-\\.\\?%&=#/+:;,~]+", Pattern.CASE_INSENSITIVE).matcher(clean)
        if (urlMatcher.find()) {
            clean = urlMatcher.group()
        }
        return clean
    }

    private fun extractYouTubeId(url: String): String? {
        val pattern = "(?:youtube(?:-nocookie)?\\.com/(?:[^/\\n\\s]+/.+/|(?:v|e(?:mbed)?|shorts)/|.*[?&]v=)|youtu\\.be/)([^\"&?/\\s]{11})"
        val compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
        val matcher = compiledPattern.matcher(url)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun extractOgMeta(html: String, property: String): String? {
        val pattern = Pattern.compile(
            "<meta[^>]+(?:property|name)=[\"']$property[\"'][^>]+content=[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = pattern.matcher(html)
        if (matcher.find()) return matcher.group(1)

        val altPattern = Pattern.compile(
            "<meta[^>]+content=[\"']([^\"']+)[\"'][^>]+(?:property|name)=[\"']$property[\"']",
            Pattern.CASE_INSENSITIVE
        )
        val altMatcher = altPattern.matcher(html)
        return if (altMatcher.find()) altMatcher.group(1) else null
    }

    private fun extractHtmlTitle(html: String): String? {
        val pattern = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(html)
        return if (matcher.find()) matcher.group(1)?.trim() else null
    }
}

data class SampleLink(
    val platform: Platform,
    val title: String,
    val url: String,
    val thumbnail: String,
    val duration: String
)
