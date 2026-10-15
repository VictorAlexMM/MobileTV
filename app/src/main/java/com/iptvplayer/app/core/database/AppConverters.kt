package com.iptvplayer.app.core.database

import androidx.room.TypeConverter
import com.iptvplayer.app.core.database.model.ContentType
import com.iptvplayer.app.core.database.model.DownloadStatus
import com.iptvplayer.app.core.database.model.PlaylistType
import com.iptvplayer.app.core.database.model.RecordingStatus

class AppConverters {
    @TypeConverter
    fun fromPlaylistType(value: PlaylistType?): String? = value?.name

    @TypeConverter
    fun toPlaylistType(value: String?): PlaylistType? = value?.let {
        try { PlaylistType.valueOf(it) } catch (_: Exception) { PlaylistType.M3U_URL }
    }

    @TypeConverter
    fun fromContentType(value: ContentType?): String? = value?.name

    @TypeConverter
    fun toContentType(value: String?): ContentType? = value?.let {
        try { ContentType.valueOf(it) } catch (_: Exception) { ContentType.LIVE }
    }

    @TypeConverter
    fun fromDownloadStatus(value: DownloadStatus?): String? = value?.name

    @TypeConverter
    fun toDownloadStatus(value: String?): DownloadStatus? = value?.let {
        try { DownloadStatus.valueOf(it) } catch (_: Exception) { DownloadStatus.QUEUED }
    }

    @TypeConverter
    fun fromRecordingStatus(value: RecordingStatus?): String? = value?.name

    @TypeConverter
    fun toRecordingStatus(value: String?): RecordingStatus? = value?.let {
        try { RecordingStatus.valueOf(it) } catch (_: Exception) { RecordingStatus.SCHEDULED }
    }
}
