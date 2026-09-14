package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DownloadStatus
import com.example.model.Platform
import com.example.model.VideoItem
import com.example.ui.components.VideoCard
import com.example.viewmodel.VideoViewModel

enum class LibraryFilter {
    ALL,
    COMPLETED,
    DOWNLOADING,
    FAVORITES
}

@Composable
fun LibraryScreen(
    viewModel: VideoViewModel,
    modifier: Modifier = Modifier
) {
    val allVideos by viewModel.allVideos.collectAsState()
    val isTamil by viewModel.isTamil.collectAsState()
    val selectedPlatform by viewModel.selectedPlatformFilter.collectAsState()

    var statusFilter by remember { mutableStateOf(LibraryFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredVideos = remember(allVideos, statusFilter, selectedPlatform, searchQuery) {
        allVideos.filter { video ->
            val matchStatus = when (statusFilter) {
                LibraryFilter.ALL -> true
                LibraryFilter.COMPLETED -> video.status == DownloadStatus.COMPLETED
                LibraryFilter.DOWNLOADING -> video.status == DownloadStatus.DOWNLOADING
                LibraryFilter.FAVORITES -> video.isFavorite
            }
            val matchPlatform = selectedPlatform == null || video.platform == selectedPlatform
            val matchSearch = searchQuery.isBlank() || video.title.contains(searchQuery, ignoreCase = true)

            matchStatus && matchPlatform && matchSearch
        }
    }

    val completedCount = allVideos.count { it.status == DownloadStatus.COMPLETED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Header Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isTamil) "என் வீடியோக்கள்" else "My Video Library",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isTamil) "$completedCount வீடியோக்கள் கேலரியில் சேமிக்கப்பட்டுள்ளன" else "$completedCount videos saved in Gallery",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${allVideos.size} Total",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (isTamil) "வீடியோக்களைத் தேடுங்கள்..." else "Search saved videos...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_search_field")
            )
        }

        // Status Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = statusFilter == LibraryFilter.ALL,
                        onClick = { statusFilter = LibraryFilter.ALL },
                        label = { Text(if (isTamil) "அனைத்தும்" else "All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == LibraryFilter.COMPLETED,
                        onClick = { statusFilter = LibraryFilter.COMPLETED },
                        label = { Text(if (isTamil) "முடிந்தது" else "Completed") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == LibraryFilter.DOWNLOADING,
                        onClick = { statusFilter = LibraryFilter.DOWNLOADING },
                        label = { Text(if (isTamil) "பதிவிறக்கம்" else "Downloading") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == LibraryFilter.FAVORITES,
                        onClick = { statusFilter = LibraryFilter.FAVORITES },
                        label = { Text(if (isTamil) "பிடித்தவை" else "Favorites") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Platform Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedPlatform == null,
                        onClick = { viewModel.setPlatformFilter(null) },
                        label = { Text("All Platforms") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
                items(Platform.values()) { p ->
                    FilterChip(
                        selected = selectedPlatform == p,
                        onClick = { viewModel.setPlatformFilter(if (selectedPlatform == p) null else p) },
                        label = { Text(p.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(p.brandColor),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Video list or empty state
        if (filteredVideos.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isTamil) "வீடியோக்கள் எதுவும் இல்லை" else "No Videos Found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isTamil) "முகப்பு பக்கத்தில் சென்று 1-கிளிக் மூலம் வீடியோக்களை டவுன்லோட் செய்யுங்கள்!" else "Go to Downloader tab and paste any YouTube, Instagram or Facebook link to download in 1-click!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredVideos, key = { it.id }) { video ->
                VideoCard(
                    video = video,
                    onPlay = { viewModel.setPlayingVideo(it) },
                    onDelete = { viewModel.deleteVideo(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onCancel = { viewModel.cancelActiveDownload() },
                    onRetry = { viewModel.startOneClickDownload() }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
