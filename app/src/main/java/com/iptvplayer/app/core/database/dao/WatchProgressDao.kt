package com.iptvplayer.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iptvplayer.app.core.database.entity.WatchProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchProgressDao {
    @Query("SELECT * FROM watch_progress WHERE contentId = :contentId LIMIT 1")
    fun getProgress(contentId: String): Flow<WatchProgressEntity?>

    @Query("SELECT * FROM watch_progress WHERE contentId = :contentId LIMIT 1")
    suspend fun getProgressSync(contentId: String): WatchProgressEntity?

    @Query("SELECT * FROM watch_progress ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentlyWatched(limit: Int = 20): Flow<List<WatchProgressEntity>>

    @Query("SELECT * FROM watch_progress")
    fun getAllProgress(): Flow<List<WatchProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: WatchProgressEntity)

    @Query("DELETE FROM watch_progress WHERE contentId = :contentId")
    suspend fun deleteProgress(contentId: String)
}
