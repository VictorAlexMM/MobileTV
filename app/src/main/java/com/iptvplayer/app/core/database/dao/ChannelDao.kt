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

data class CategoryCount(
    val category: String,
    val count: Int
)

@Dao
interface ChannelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    /**
     * Busca paginada unificada com isolamento estrito por ContentType (LIVE, MOVIE, SERIES).
     * Suporta filtro opcional por playlist, por categoria, busca textual (por nome E por categoria),
     * filtro de favoritos e eliminação de duplicatas via GROUP BY LOWER(TRIM(name)).
     */
    @Query("""
        SELECT * FROM channels 
        WHERE (:playlistId IS NULL OR playlistId = :playlistId)
          AND contentType = :contentType
          AND (:category IS NULL OR groupTitle = :category)
          AND (:query = '' OR name LIKE '%' || :query || '%' OR groupTitle LIKE '%' || :query || '%')
          AND (:onlyFavorites = 0 OR isFavorite = 1)
        GROUP BY LOWER(TRIM(name))
        ORDER BY name ASC, id ASC
    """)
    fun getChannelsFilteredPaged(
        playlistId: Long?,
        contentType: ContentType,
        category: String?,
        query: String,
        onlyFavorites: Boolean = false
    ): PagingSource<Int, ChannelEntity>

    @Query("""
        SELECT COUNT(DISTINCT LOWER(TRIM(name))) FROM channels 
        WHERE (:playlistId IS NULL OR playlistId = :playlistId) 
          AND contentType = :contentType
          AND isFavorite = 1
    """)
    fun getFavoriteCount(
        playlistId: Long?,
        contentType: ContentType
    ): Flow<Int>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId AND contentType = :contentType GROUP BY LOWER(TRIM(name)) ORDER BY name ASC, id ASC")
    fun getChannelsPaged(playlistId: Long, contentType: ContentType): PagingSource<Int, ChannelEntity>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId AND contentType = :contentType AND groupTitle = :category GROUP BY LOWER(TRIM(name)) ORDER BY name ASC, id ASC")
    fun getChannelsByCategoryPaged(
        playlistId: Long,
        contentType: ContentType,
        category: String
    ): PagingSource<Int, ChannelEntity>

    @Query("SELECT * FROM channels WHERE (name LIKE '%' || :query || '%' OR groupTitle LIKE '%' || :query || '%') GROUP BY LOWER(TRIM(name)) ORDER BY name ASC, id ASC")
    fun searchChannelsPaged(query: String): PagingSource<Int, ChannelEntity>

    /**
     * Categorias com contagem de itens únicos por ContentType, ordenadas pelas mais populares primeiro.
     */
    @Query("""
        SELECT groupTitle AS category, COUNT(DISTINCT LOWER(TRIM(name))) AS count
        FROM channels
        WHERE (:playlistId IS NULL OR playlistId = :playlistId)
          AND contentType = :contentType
          AND groupTitle IS NOT NULL
          AND TRIM(groupTitle) != ''
        GROUP BY groupTitle
        ORDER BY count DESC, groupTitle ASC
    """)
    fun getCategoriesWithCount(
        playlistId: Long?,
        contentType: ContentType
    ): Flow<List<CategoryCount>>

    @Query("""
        SELECT DISTINCT groupTitle FROM channels 
        WHERE (:playlistId IS NULL OR playlistId = :playlistId) 
          AND contentType = :contentType 
          AND groupTitle IS NOT NULL 
          AND TRIM(groupTitle) != ''
        ORDER BY groupTitle ASC
    """)
    fun getCategories(
        playlistId: Long?,
        contentType: ContentType
    ): Flow<List<String>>

    @Query("""
        SELECT COUNT(DISTINCT LOWER(TRIM(name))) FROM channels 
        WHERE (:playlistId IS NULL OR playlistId = :playlistId) 
          AND contentType = :contentType
    """)
    fun getTotalCountByType(
        playlistId: Long?,
        contentType: ContentType
    ): Flow<Int>

    @Query("""
        SELECT * FROM channels 
        WHERE (:playlistId IS NULL OR playlistId = :playlistId) 
          AND contentType = :contentType 
          AND groupTitle = :groupTitle 
        GROUP BY LOWER(TRIM(name))
        ORDER BY name ASC, id ASC
    """)
    suspend fun getChannelsByGroup(
        playlistId: Long?,
        contentType: ContentType,
        groupTitle: String
    ): List<ChannelEntity>

    /**
     * Retorna episódios de uma série específica dentro de uma playlist ou globalmente,
     * buscando por correspondência parcial no nome do episódio ou no grupo da série.
     */
    @Query("""
        SELECT * FROM channels 
        WHERE (:playlistId IS NULL OR playlistId = :playlistId) 
          AND contentType = 'SERIES' 
          AND (name LIKE '%' || :seriesQuery || '%' OR groupTitle LIKE '%' || :seriesQuery || '%')
        ORDER BY name ASC
    """)
    suspend fun getSeriesEpisodes(
        playlistId: Long?,
        seriesQuery: String
    ): List<ChannelEntity>

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
