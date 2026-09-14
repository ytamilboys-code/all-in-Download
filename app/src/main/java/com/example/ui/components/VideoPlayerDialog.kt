package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.model.VideoItem
import java.io.File

/**
 * Built-in Video Player Dialog powered by AndroidX Media3 ExoPlayer.
 * Used for both streaming preview before download and playing offline saved videos after download.
 */
@Composable
fun VideoPlayerDialog(
    video: VideoItem,
    onDismiss: () -> Unit,
    onDownloadClick: ((VideoItem) -> Unit)? = null
) {
    val context = LocalContext.current

    val isOfflineFile = remember(video) {
        !video.localFilePath.isNullOrBlank() && File(video.localFilePath).exists()
    }

    val videoUri = remember(video, isOfflineFile) {
        if (isOfflineFile) {
            val file = File(video.localFilePath!!)
            try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (e: Exception) {
                Uri.fromFile(file)
            }
        } else {
            Uri.parse(video.downloadUrl)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 16.dp)
                .testTag("video_player_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // ExoPlayer UI Component
                ExoVideoPlayer(
                    mediaUri = videoUri,
                    title = video.title,
                    platform = video.platform,
                    quality = video.quality,
                    isOffline = isOfflineFile,
                    onClose = onDismiss,
                    onDownloadClick = if (!isOfflineFile && onDownloadClick != null) {
                        {
                            onDownloadClick(video)
                            onDismiss()
                        }
                    } else null,
                    onShareClick = if (isOfflineFile) {
                        { shareDownloadedVideo(context, video) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Footer Metadata Details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isOfflineFile) "Offline Local Storage (Gallery)" else "Live Remote Stream Preview",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Format: MP4 • ${video.quality}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = video.fileSizeFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun shareDownloadedVideo(context: Context, video: VideoItem) {
    if (video.localFilePath.isNullOrBlank()) return
    val file = File(video.localFilePath)
    if (!file.exists()) return

    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, video.title)
            putExtra(Intent.EXTRA_TEXT, "Check out this video: ${video.title}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share video via"))
    } catch (e: Exception) {
        // Fallback or ignore
    }
}
