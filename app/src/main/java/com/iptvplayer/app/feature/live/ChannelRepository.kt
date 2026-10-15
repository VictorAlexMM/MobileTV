package com.iptvplayer.app.feature.live

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.iptvplayer.app.core.database.AppDatabase
import com.iptvplayer.app.core.database.entity.ChannelEntity
import com.iptvplayer.app.core.database.entity.DownloadEntity
import com.iptvplayer.app.core.database.model.ContentType
import com.iptvplayer.app.core.database.model.PlaylistType
import com.iptvplayer.app.core.network.NetworkClient
import com.iptvplayer.app.core.network.NetworkGate
import com.iptvplayer.app.feature.series.SeriesDetails
import com.iptvplayer.app.feature.series.SeriesMetadataHelper
import com.iptvplayer.app.parser.xtream.XtreamApiService
import kotlinx.coroutines.flow.Flow

class ChannelRepository(
    private val database: AppDatabase,
    private val networkGate: NetworkGate? = null
) {
    private val channelDao = database.channelDao()
    private val downloadDao = database.downloadDao()
    private val playlistDao = database.playlistDao()

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

    suspend fun getChannelsByGroup(playlistId: Long?, contentType: ContentType, groupTitle: String): List<ChannelEntity> {
        return channelDao.getChannelsByGroup(playlistId, contentType, groupTitle)
    }

    fun observeWatchProgress(contentId: String): Flow<com.iptvplayer.app.core.database.entity.WatchProgressEntity?> {
        return database.watchProgressDao().getProgress(contentId)
    }

    fun getAllWatchProgress(): Flow<List<com.iptvplayer.app.core.database.entity.WatchProgressEntity>> {
        return database.watchProgressDao().getAllProgress()
    }

    /**
     * Carrega os detalhes completos e todos os episódios estritamente pertencentes
     * à série selecionada, isolando-a de canais ou episódios de outras séries.
     * Suporta tanto playlists M3U (Room DB) quanto Xtream Codes (API get_series_info).
     */
    suspend fun loadSeriesDetails(item: ChannelEntity): SeriesDetails {
        val playlist = playlistDao.getPlaylistById(item.playlistId)

        // 1. Resolução via API Xtream Codes
        if (playlist != null && playlist.type == PlaylistType.XTREAM) {
            val seriesId = when {
                item.id.contains("_series_") -> item.id.substringAfterLast("_series_")
                item.streamUrl.contains("/series/") -> item.streamUrl.substringAfterLast("/").trim()
                else -> ""
            }

            val rawUrl = playlist.url
            if (seriesId.isNotBlank() && !rawUrl.isNullOrBlank() && (networkGate == null || networkGate.canAccessNetwork())) {
                try {
                    val cleanServer = if (rawUrl.endsWith("/")) rawUrl.trim() else "${rawUrl.trim()}/"
                    val retrofit = NetworkClient.createRetrofit(cleanServer, NetworkClient.createOkHttpClient())
                    val api = retrofit.create(XtreamApiService::class.java)
                    val response = api.getSeriesInfo(
                        user = playlist.username.orEmpty(),
                        pass = playlist.password.orEmpty(),
                        seriesId = seriesId
                    )
                    val jsonStr = response.string()
                    return SeriesMetadataHelper.parseXtreamSeriesInfo(
                        jsonString = jsonStr,
                        seriesItem = item,
                        cleanServer = cleanServer,
                        username = playlist.username.orEmpty(),
                        password = playlist.password.orEmpty()
                    )
                } catch (_: Exception) {
                    // Se falhar a conexão, faz o fallback gracioso para os dados locais
                }
            }
        }

        // 2. Resolução para playlists M3U locais
        val pureTitle = SeriesMetadataHelper.extractPureSeriesTitle(item.name)
        val candidates = channelDao.getSeriesEpisodes(item.playlistId, pureTitle)

        val matched = candidates.filter { candidate ->
            SeriesMetadataHelper.isMatchingSeries(candidate.name, pureTitle)
        }

        val finalEpisodes = if (matched.isNotEmpty()) matched else listOf(item)
        return SeriesMetadataHelper.groupEpisodes(finalEpisodes, fallbackSeriesTitle = pureTitle)
    }
}

