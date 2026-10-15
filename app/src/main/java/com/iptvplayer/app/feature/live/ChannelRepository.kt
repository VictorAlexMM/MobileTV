package com.iptvplayer.app.feature.live

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.iptvplayer.app.core.database.AppDatabase
import com.iptvplayer.app.core.database.entity.ChannelEntity
import com.iptvplayer.app.core.database.entity.DownloadEntity
import com.iptvplayer.app.core.database.model.ContentType
import kotlinx.coroutines.flow.Flow

class ChannelRepository(private val database: AppDatabase) {
    private val channelDao = database.channelDao()
    private val downloadDao = database.downloadDao()

    fun getChannelsPaged(playlistId: Long, contentType: ContentType): Flow<PagingData<ChannelEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 40, enablePlaceholders = true),
            pagingSourceFactory = { channelDao.getChannelsPaged(playlistId, contentType) }
        ).flow
    }

    fun getChannelsByCategoryPaged(
        playlistId: Long,
        contentType: ContentType,
        category: String
    ): Flow<PagingData<ChannelEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 40, enablePlaceholders = true),
            pagingSourceFactory = { channelDao.getChannelsByCategoryPaged(playlistId, contentType, category) }
        ).flow
    }

    fun searchChannelsPaged(query: String): Flow<PagingData<ChannelEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 40, enablePlaceholders = true),
            pagingSourceFactory = { channelDao.searchChannelsPaged(query) }
        ).flow
    }

    fun getCategories(playlistId: Long, contentType: ContentType): Flow<List<String>> {
        return channelDao.getCategories(playlistId, contentType)
    }

    fun getFavoriteChannels(): Flow<List<ChannelEntity>> {
        return channelDao.getFavoriteChannels()
    }

    suspend fun getChannelById(id: String): ChannelEntity? {
        return channelDao.getChannelById(id)
    }

    suspend fun toggleFavorite(id: String, currentFavorite: Boolean) {
        channelDao.setFavorite(id, !currentFavorite)
    }

    suspend fun getCompletedDownload(channelId: String): DownloadEntity? {
        return downloadDao.getCompletedDownloadByChannelId(channelId)
    }

    fun observeDownload(channelId: String): Flow<DownloadEntity?> {
        return downloadDao.observeDownloadByChannelId(channelId)
    }
}
