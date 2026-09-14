package com.example.data

import com.example.model.VideoItem
import kotlinx.coroutines.flow.Flow

class VideoRepository(private val videoDao: VideoDao) {
    val allVideos: Flow<List<VideoItem>> = videoDao.getAllVideos()
    val completedVideos: Flow<List<VideoItem>> = videoDao.getCompletedVideos()
    val favoriteVideos: Flow<List<VideoItem>> = videoDao.getFavoriteVideos()

    suspend fun insert(video: VideoItem): Long = videoDao.insertVideo(video)

    suspend fun update(video: VideoItem) = videoDao.updateVideo(video)

    suspend fun delete(video: VideoItem) = videoDao.deleteVideo(video)

    suspend fun deleteById(id: Long) = videoDao.deleteVideoById(id)

    suspend fun getById(id: Long): VideoItem? = videoDao.getVideoById(id)

    suspend fun getByUrl(url: String): VideoItem? = videoDao.getVideoByUrl(url)

    suspend fun clearAll() = videoDao.clearAll()
}
