package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.VideoRepository
import com.example.downloader.DownloadEngine
import com.example.downloader.DownloadEvent
import com.example.downloader.ExtractedVideo
import com.example.downloader.SampleLink
import com.example.downloader.VideoExtractor
import com.example.downloader.VideoQualityOption
import com.example.model.DownloadStatus
import com.example.model.Platform
import com.example.model.VideoItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiNotification(
    val message: String,
    val isError: Boolean = false
)

class VideoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = VideoRepository(AppDatabase.getDatabase(application).videoDao())
    private val downloadEngine = DownloadEngine(application)

    val allVideos: StateFlow<List<VideoItem>> = repository.allVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _extractedVideo = MutableStateFlow<ExtractedVideo?>(null)
    val extractedVideo: StateFlow<ExtractedVideo?> = _extractedVideo.asStateFlow()

    private val _activeDownload = MutableStateFlow<VideoItem?>(null)
    val activeDownload: StateFlow<VideoItem?> = _activeDownload.asStateFlow()

    private val _selectedPlatformFilter = MutableStateFlow<Platform?>(null)
    val selectedPlatformFilter: StateFlow<Platform?> = _selectedPlatformFilter.asStateFlow()

    private val _playingVideo = MutableStateFlow<VideoItem?>(null)
    val playingVideo: StateFlow<VideoItem?> = _playingVideo.asStateFlow()

    private val _notification = MutableStateFlow<UiNotification?>(null)
    val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

    private val _isTamil = MutableStateFlow(false)
    val isTamil: StateFlow<Boolean> = _isTamil.asStateFlow()

    private var currentDownloadJob: Job? = null

    fun toggleLanguage() {
        _isTamil.value = !_isTamil.value
    }

    fun setUrlInput(text: String) {
        _urlInput.value = text
    }

    fun clearUrlInput() {
        _urlInput.value = ""
        _extractedVideo.value = null
    }

    fun setPlatformFilter(platform: Platform?) {
        _selectedPlatformFilter.value = platform
    }

    fun setPlayingVideo(video: VideoItem?) {
        _playingVideo.value = video
    }

    fun previewExtractedVideo(extracted: ExtractedVideo, customQuality: VideoQualityOption? = null) {
        val selectedQ = customQuality ?: extracted.qualities.firstOrNull()
        val previewItem = VideoItem(
            id = -1L,
            title = extracted.title,
            originalUrl = extracted.originalUrl,
            downloadUrl = selectedQ?.streamUrl ?: extracted.downloadUrl,
            thumbnailUrl = extracted.thumbnailUrl,
            platform = extracted.platform,
            duration = extracted.duration,
            quality = selectedQ?.label ?: "1080p Full HD",
            fileSizeFormatted = selectedQ?.estimatedSize ?: "Live Stream",
            localFilePath = null,
            status = DownloadStatus.IDLE
        )
        _playingVideo.value = previewItem
    }

    fun clearNotification() {
        _notification.value = null
    }

    fun showNotification(message: String, isError: Boolean = false) {
        _notification.value = UiNotification(message, isError)
    }

    fun pasteAndFetch(clipboardText: String) {
        val clean = clipboardText.trim()
        if (clean.isBlank()) {
            _notification.value = UiNotification(
                if (_isTamil.value) "கிளிப்போர்டு காலியாக உள்ளது" else "Clipboard is empty",
                isError = true
            )
            return
        }
        _urlInput.value = clean
        analyzeUrl(clean)
    }

    fun selectSample(sample: SampleLink) {
        _urlInput.value = sample.url
        analyzeUrl(sample.url)
    }

    fun analyzeUrl(rawUrl: String = _urlInput.value) {
        val clean = rawUrl.trim()
        if (clean.isBlank()) {
            _notification.value = UiNotification(
                if (_isTamil.value) "தயவுசெய்து ஒரு வீடியோ இணைப்பை உள்ளிடவும்" else "Please enter a valid video link",
                isError = true
            )
            return
        }

        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val extracted = VideoExtractor.extract(clean)
                _extractedVideo.value = extracted
            } catch (e: Exception) {
                _notification.value = UiNotification(
                    e.localizedMessage ?: "Failed to inspect video link",
                    isError = true
                )
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun startOneClickDownload(
        customQuality: VideoQualityOption? = null,
        urlToDownload: String = _urlInput.value
    ) {
        val targetUrl = urlToDownload.trim()
        if (targetUrl.isBlank()) {
            _notification.value = UiNotification(
                if (_isTamil.value) "வீடியோ இணைப்பு காலியாக உள்ளது" else "Video link is empty",
                isError = true
            )
            return
        }

        viewModelScope.launch {
            // If not yet analyzed, analyze first
            val extracted = _extractedVideo.value ?: run {
                _isAnalyzing.value = true
                try {
                    val res = VideoExtractor.extract(targetUrl)
                    _extractedVideo.value = res
                    res
                } catch (e: Exception) {
                    _isAnalyzing.value = false
                    _notification.value = UiNotification(
                        if (_isTamil.value) "இணைப்பை ஆய்வு செய்ய முடியவில்லை" else "Could not analyze link",
                        isError = true
                    )
                    return@launch
                } finally {
                    _isAnalyzing.value = false
                }
            }

            val chosenQuality = customQuality ?: extracted.qualities.firstOrNull()
            val streamUrl = chosenQuality?.streamUrl ?: extracted.downloadUrl
            val qualityLabel = chosenQuality?.label ?: "1080p HD"
            val estSize = chosenQuality?.estimatedSize ?: "Auto"

            val newItem = VideoItem(
                title = extracted.title,
                originalUrl = extracted.originalUrl,
                downloadUrl = streamUrl,
                thumbnailUrl = extracted.thumbnailUrl,
                platform = extracted.platform,
                duration = extracted.duration,
                quality = qualityLabel,
                fileSizeFormatted = estSize,
                status = DownloadStatus.DOWNLOADING,
                progress = 0
            )

            val insertedId = repository.insert(newItem)
            val activeItem = newItem.copy(id = insertedId)
            _activeDownload.value = activeItem

            _notification.value = UiNotification(
                if (_isTamil.value) "டவுன்லோடு தொடங்கப்பட்டது: ${activeItem.title}"
                else "Download started: ${activeItem.title}"
            )

            executeDownload(activeItem)
        }
    }

    private fun executeDownload(videoItem: VideoItem) {
        currentDownloadJob?.cancel()
        currentDownloadJob = viewModelScope.launch {
            downloadEngine.downloadVideo(videoItem) { event ->
                when (event) {
                    is DownloadEvent.Progress -> {
                        val updated = videoItem.copy(
                            progress = event.percent,
                            downloadedBytes = event.downloadedBytes,
                            totalBytes = event.totalBytes,
                            speed = event.speed,
                            status = DownloadStatus.DOWNLOADING
                        )
                        _activeDownload.value = updated
                        repository.update(updated)
                    }
                    is DownloadEvent.Success -> {
                        val completed = videoItem.copy(
                            localFilePath = event.localFilePath,
                            fileSizeFormatted = event.formattedSize,
                            progress = 100,
                            status = DownloadStatus.COMPLETED
                        )
                        _activeDownload.value = null
                        repository.update(completed)
                        _notification.value = UiNotification(
                            if (_isTamil.value) "வீடியோ வெற்றிகரமாக பதிவிறக்கப்பட்டது!"
                            else "Video successfully downloaded to Gallery!"
                        )
                    }
                    is DownloadEvent.Error -> {
                        val failed = videoItem.copy(
                            status = DownloadStatus.FAILED
                        )
                        _activeDownload.value = null
                        repository.update(failed)
                        _notification.value = UiNotification(
                            event.message,
                            isError = true
                        )
                    }
                }
            }
        }
    }

    fun cancelActiveDownload() {
        currentDownloadJob?.cancel()
        val current = _activeDownload.value
        if (current != null) {
            viewModelScope.launch {
                val cancelled = current.copy(status = DownloadStatus.CANCELLED)
                repository.update(cancelled)
                _activeDownload.value = null
                _notification.value = UiNotification(
                    if (_isTamil.value) "டவுன்லோடு ரத்து செய்யப்பட்டது" else "Download cancelled"
                )
            }
        }
    }

    fun deleteVideo(video: VideoItem) {
        viewModelScope.launch {
            repository.delete(video)
            // If local file exists, remove it
            video.localFilePath?.let { path ->
                try { java.io.File(path).delete() } catch (_: Exception) {}
            }
            _notification.value = UiNotification(
                if (_isTamil.value) "வீடியோ நீக்கப்பட்டது" else "Video deleted"
            )
        }
    }

    fun toggleFavorite(video: VideoItem) {
        viewModelScope.launch {
            val updated = video.copy(isFavorite = !video.isFavorite)
            repository.update(updated)
        }
    }

    fun handleSharedText(sharedText: String) {
        _urlInput.value = sharedText
        analyzeUrl(sharedText)
    }
}
