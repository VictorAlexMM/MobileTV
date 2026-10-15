package com.iptvplayer.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.iptvplayer.app.core.database.model.ContentType

@Entity(
    tableName = "channels",
    indices = [
        Index("playlistId"),
        Index("contentType"),
        Index("groupTitle"),
        Index("isFavorite"),
        Index(value = ["playlistId", "contentType", "groupTitle"])
    ]
)
data class ChannelEntity(
    @PrimaryKey
    val id: String, // hash(playlistId + streamUrl)
    val playlistId: Long,
    val name: String,
    val logoUrl: String? = null,
    val groupTitle: String? = null,
    val streamUrl: String,
    val tvgId: String? = null,
    val contentType: ContentType = ContentType.LIVE,
    val isFavorite: Boolean = false
)
