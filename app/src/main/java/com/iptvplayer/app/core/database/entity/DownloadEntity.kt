package com.iptvplayer.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.iptvplayer.app.core.database.model.DownloadStatus

@Entity(
    tableName = "downloads",
    indices = [
        Index("channelId"),
        Index("status")
    ]
)
data class DownloadEntity(
    @PrimaryKey
    val id: String, // Mesmo id usado no Media3 DownloadRequest
    val channelId: String,
    val title: String,
    val posterUrl: String? = null,
    val sourceUrl: String,
    val localPath: String? = null,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = -1L,
    val percent: Float = 0f,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorMessage: String? = null
)
