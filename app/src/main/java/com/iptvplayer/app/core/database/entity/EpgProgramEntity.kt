package com.iptvplayer.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "epg_programs",
    indices = [
        Index("channelTvgId"),
        Index("start"),
        Index("end"),
        Index(value = ["channelTvgId", "start", "end"])
    ]
)
data class EpgProgramEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val channelTvgId: String,
    val title: String,
    val description: String? = null,
    val start: Long,
    val end: Long
)
