package com.iptvplayer.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iptvplayer.app.core.database.model.PlaylistType

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: PlaylistType,
    val url: String?,
    val username: String?,
    val password: String?,
    val epgUrl: String?,
    val lastSyncAt: Long? = null
)
