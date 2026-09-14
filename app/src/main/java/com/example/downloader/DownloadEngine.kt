package com.example.downloader

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.model.VideoItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

sealed class DownloadEvent {
    data class Progress(val percent: Int, val downloadedBytes: Long, val totalBytes: Long, val speed: String) : DownloadEvent()
    data class Success(val localFilePath: String, val formattedSize: String) : DownloadEvent()
    data class Error(val message: String) : DownloadEvent()
}

class DownloadEngine(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun downloadVideo(
        video: VideoItem,
        onEvent: suspend (DownloadEvent) -> Unit
    ) = withContext(Dispatchers.IO) {
        val safeFileName = sanitizeFileName(video.title.ifBlank { "video_${System.currentTimeMillis()}" }) + ".mp4"
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
        val destinationFile = File(downloadDir, safeFileName)

        try {
            val request = Request.Builder()
                .url(video.downloadUrl)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                onEvent(DownloadEvent.Error("HTTP Error ${response.code}: Failed to download media"))
                return@withContext
            }

            val body = response.body
            if (body == null) {
                onEvent(DownloadEvent.Error("Server returned empty media response"))
                return@withContext
            }

            val contentLength = body.contentLength().let { if (it <= 0) 18_000_000L else it }
            var downloaded = 0L
            var lastUpdateTime = System.currentTimeMillis()
            var bytesSinceLastUpdate = 0L

            val buffer = ByteArray(8192)
            val inputStream: InputStream = body.byteStream()
            val outputStream = FileOutputStream(destinationFile)

            try {
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    if (!coroutineContext.isActive) {
                        throw CancellationException("Download cancelled")
                    }

                    outputStream.write(buffer, 0, bytesRead)
                    downloaded += bytesRead
                    bytesSinceLastUpdate += bytesRead

                    val now = System.currentTimeMillis()
                    val timeDiff = now - lastUpdateTime

                    // Emit progress every 200ms or on completion
                    if (timeDiff >= 200 || downloaded >= contentLength) {
                        val speedBytesPerSec = if (timeDiff > 0) (bytesSinceLastUpdate * 1000) / timeDiff else 0L
                        val speedString = formatSpeed(speedBytesPerSec)
                        val percent = ((downloaded.toDouble() / contentLength.toDouble()) * 100).toInt().coerceIn(0, 100)

                        onEvent(DownloadEvent.Progress(percent, downloaded, contentLength, speedString))
                        lastUpdateTime = now
                        bytesSinceLastUpdate = 0L
                    }
                }
                outputStream.flush()
            } finally {
                try { outputStream.close() } catch (_: Exception) {}
                try { inputStream.close() } catch (_: Exception) {}
            }

            // Save to Public MediaStore so it is accessible in device Gallery
            saveToMediaStore(context, destinationFile, safeFileName)

            val formattedSize = formatFileSize(destinationFile.length())
            onEvent(DownloadEvent.Success(destinationFile.absolutePath, formattedSize))

        } catch (e: CancellationException) {
            if (destinationFile.exists()) destinationFile.delete()
            throw e
        } catch (e: Exception) {
            onEvent(DownloadEvent.Error(e.localizedMessage ?: "Unknown download error"))
        }
    }

    private fun saveToMediaStore(context: Context, sourceFile: File, title: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, title)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/VideoDownloader")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri: Uri? = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        sourceFile.inputStream().use { inp ->
                            inp.copyTo(out)
                        }
                    }
                    values.clear()
                    values.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
            }
        } catch (_: Exception) {
            // Non-fatal if media store indexing fails on some emulators
        }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(50)
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec >= 1024 * 1024 -> DecimalFormat("#0.0").format(bytesPerSec / (1024.0 * 1024.0)) + " MB/s"
            bytesPerSec >= 1024 -> (bytesPerSec / 1024).toString() + " KB/s"
            else -> "$bytesPerSec B/s"
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> DecimalFormat("#0.00").format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB"
            bytes >= 1024 * 1024 -> DecimalFormat("#0.0").format(bytes / (1024.0 * 1024.0)) + " MB"
            bytes >= 1024 -> (bytes / 1024).toString() + " KB"
            else -> "$bytes B"
        }
    }
}
