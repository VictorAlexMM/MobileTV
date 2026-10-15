package com.iptvplayer.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iptvplayer.app.core.database.dao.ChannelDao
import com.iptvplayer.app.core.database.dao.DownloadDao
import com.iptvplayer.app.core.database.dao.EpgDao
import com.iptvplayer.app.core.database.dao.PlaylistDao
import com.iptvplayer.app.core.database.dao.RecordingDao
import com.iptvplayer.app.core.database.dao.WatchProgressDao
import com.iptvplayer.app.core.database.entity.ChannelEntity
import com.iptvplayer.app.core.database.entity.DownloadEntity
import com.iptvplayer.app.core.database.entity.EpgProgramEntity
import com.iptvplayer.app.core.database.entity.PlaylistEntity
import com.iptvplayer.app.core.database.entity.RecordingEntity
import com.iptvplayer.app.core.database.entity.WatchProgressEntity

@Database(
    entities = [
        PlaylistEntity::class,
        ChannelEntity::class,
        DownloadEntity::class,
        RecordingEntity::class,
        EpgProgramEntity::class,
        WatchProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(AppConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun channelDao(): ChannelDao
    abstract fun downloadDao(): DownloadDao
    abstract fun recordingDao(): RecordingDao
    abstract fun epgDao(): EpgDao
    abstract fun watchProgressDao(): WatchProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "iptv_offline_player.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
