package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Platform(val displayName: String, val brandColor: Long) {
    YOUTUBE("YouTube", 0xFFFF0000),
    INSTAGRAM("Instagram", 0xFFE1306C),
    FACEBOOK("Facebook", 0xFF1877F2),
    TIKTOK("TikTok", 0xFF000000),
    TWITTER("Twitter/X", 0xFF1DA1F2),
    OTHER("Web Video", 0xFF6366F1)
}

enum class DownloadStatus {
    IDLE,
    ANALYZING,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    CANCELLED
}

@Entity(tableName = "videos")
data class VideoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val originalUrl: String,
    val downloadUrl: String,
    val thumbnailUrl: String,
    val platform: Platform,
    val duration: String = "0:00",
    val quality: String = "1080p HD",
    val fileSizeFormatted: String = "0 MB",
    val localFilePath: String? = null,
    val status: DownloadStatus = DownloadStatus.IDLE,
    val progress: Int = 0,
    val speed: String = "",
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
