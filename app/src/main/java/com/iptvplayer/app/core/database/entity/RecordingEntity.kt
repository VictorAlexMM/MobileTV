package com.iptvplayer.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.iptvplayer.app.core.database.model.RecordingStatus

@Entity(
    tableName = "recordings",
    indices = [
        Index("channelId"),
        Index("status")
    ]
)
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val channelId: String,
    val title: String,
    val scheduledStart: Long,
    val durationMinutes: Int,
    val status: RecordingStatus = RecordingStatus.SCHEDULED,
    val filePath: String? = null
)
