package com.iptvplayer.app.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.iptvplayer.app.core.database.entity.ChannelEntity
import com.iptvplayer.app.core.database.model.ContentType
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId AND contentType = :contentType ORDER BY name ASC")
    fun getChannelsPaged(playlistId: Long, contentType: ContentType): PagingSource<Int, ChannelEntity>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId AND contentType = :contentType AND groupTitle = :category ORDER BY name ASC")
    fun getChannelsByCategoryPaged(
        playlistId: Long,
        contentType: ContentType,
        category: String
    ): PagingSource<Int, ChannelEntity>

    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchChannelsPaged(query: String): PagingSource<Int, ChannelEntity>

    @Query("SELECT DISTINCT groupTitle FROM channels WHERE playlistId = :playlistId AND contentType = :contentType AND groupTitle IS NOT NULL ORDER BY groupTitle ASC")
    fun getCategories(playlistId: Long, contentType: ContentType): Flow<List<String>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    suspend fun getChannelById(id: String): ChannelEntity?

    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteChannelsByPlaylist(playlistId: Long)

    @Query("SELECT COUNT(*) FROM channels WHERE playlistId = :playlistId")
    suspend fun getCountByPlaylist(playlistId: Long): Int

    @Transaction
    suspend fun replacePlaylistChannels(playlistId: Long, channels: List<ChannelEntity>) {
        deleteChannelsByPlaylist(playlistId)
        insertChannels(channels)
    }
}
