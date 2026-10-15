package com.iptvplayer.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.iptvplayer.app.core.database.entity.DownloadEntity
import com.iptvplayer.app.core.database.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY createdAt DESC")
    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: String): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE channelId = :channelId LIMIT 1")
    fun observeDownloadByChannelId(channelId: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads WHERE channelId = :channelId AND status = 'COMPLETED' LIMIT 1")
    suspend fun getCompletedDownloadByChannelId(channelId: String): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(download: DownloadEntity)

    @Update
    suspend fun update(download: DownloadEntity)

    @Query("""
        UPDATE downloads 
        SET bytesDownloaded = :bytesDownloaded, 
            totalBytes = :totalBytes, 
            percent = :percent, 
            status = :status,
            completedAt = CASE WHEN :status = 'COMPLETED' THEN :now ELSE completedAt END,
            errorMessage = :errorMessage
        WHERE id = :id
    """)
    suspend fun updateProgress(
        id: String,
        bytesDownloaded: Long,
        totalBytes: Long,
        percent: Float,
        status: DownloadStatus,
        errorMessage: String? = null,
        now: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM downloads")
    suspend fun deleteAll()
}
