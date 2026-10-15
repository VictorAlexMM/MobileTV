package com.iptvplayer.app.feature.playlists

import com.iptvplayer.app.core.common.AppException
import com.iptvplayer.app.core.database.AppDatabase
import com.iptvplayer.app.core.database.entity.ChannelEntity
import com.iptvplayer.app.core.database.entity.PlaylistEntity
import com.iptvplayer.app.core.database.model.ContentType
import com.iptvplayer.app.core.database.model.PlaylistType
import com.iptvplayer.app.core.network.NetworkClient
import com.iptvplayer.app.core.network.NetworkGate
import com.iptvplayer.app.parser.m3u.M3UParser
import com.iptvplayer.app.parser.xtream.XtreamApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.InputStream

data class PlaylistImportResult(
    val playlistId: Long,
    val name: String,
    val liveCount: Int = 0,
    val vodCount: Int = 0,
    val seriesCount: Int = 0
) {
    val totalCount: Int get() = liveCount + vodCount + seriesCount
}

class PlaylistRepository(
    private val database: AppDatabase,
    private val networkGate: NetworkGate
) {
    private val playlistDao = database.playlistDao()
    private val channelDao = database.channelDao()
    private val okHttpClient = NetworkClient.createOkHttpClient()

    fun getAllPlaylists(): Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    suspend fun getPlaylistById(id: Long): PlaylistEntity? = playlistDao.getPlaylistById(id)

    suspend fun importM3UFromUrl(name: String, url: String): PlaylistImportResult = withContext(Dispatchers.IO) {
        if (!networkGate.canAccessNetwork()) {
            throw AppException.OfflineModeException()
        }

        val playlist = PlaylistEntity(
            name = name.trim(),
            type = PlaylistType.M3U_URL,
            url = url.trim(),
            username = null,
            password = null,
            epgUrl = null,
            lastSyncAt = System.currentTimeMillis()
        )
        val playlistId = playlistDao.insert(playlist)

        val request = Request.Builder().url(url.trim()).build()
        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            playlistDao.deleteById(playlistId)
            throw AppException.NetworkUnavailableException("HTTP ${response.code}: Não foi possível carregar a playlist.")
        }

        var totalParsed = 0
        response.body?.byteStream()?.use { stream ->
            totalParsed = M3UParser.parseStreaming(
                inputStream = stream,
                playlistId = playlistId,
                batchSize = 500
            ) { batch ->
                channelDao.insertChannels(batch)
            }
        } ?: throw AppException.InvalidPlaylistException("Corpo de resposta vazio.")

        PlaylistImportResult(
            playlistId = playlistId,
            name = name.trim(),
            liveCount = totalParsed
        )
    }

    suspend fun importM3UFromFile(name: String, inputStream: InputStream): PlaylistImportResult = withContext(Dispatchers.IO) {
        val playlist = PlaylistEntity(
            name = name.trim(),
            type = PlaylistType.M3U_FILE,
            url = null,
            username = null,
            password = null,
            epgUrl = null,
            lastSyncAt = System.currentTimeMillis()
        )
        val playlistId = playlistDao.insert(playlist)

        var totalParsed = 0
        inputStream.use { stream ->
            totalParsed = M3UParser.parseStreaming(
                inputStream = stream,
                playlistId = playlistId,
                batchSize = 500
            ) { batch ->
                channelDao.insertChannels(batch)
            }
        }

        PlaylistImportResult(
            playlistId = playlistId,
            name = name.trim(),
            liveCount = totalParsed
        )
    }

    suspend fun importXtreamAccount(
        serverUrl: String,
        username: String,
        password: String
    ): PlaylistImportResult = withContext(Dispatchers.IO) {
        if (!networkGate.canAccessNetwork()) {
            throw AppException.OfflineModeException()
        }

        val cleanServer = if (serverUrl.endsWith("/")) serverUrl.trim() else "${serverUrl.trim()}/"
        val retrofit = NetworkClient.createRetrofit(cleanServer, okHttpClient)
        val api = retrofit.create(XtreamApiService::class.java)

        val auth = api.authenticate(username.trim(), password.trim())
        if (auth.userInfo?.status != "Active") {
            throw AppException.NetworkUnavailableException("Autenticação Xtream falhou ou conta inativa.")
        }

        val playlistName = "Xtream: ${username.trim()}"
        val playlist = PlaylistEntity(
            name = playlistName,
            type = PlaylistType.XTREAM,
            url = cleanServer,
            username = username.trim(),
            password = password.trim(),
            epgUrl = "${cleanServer}xmltv.php?username=${username.trim()}&password=${password.trim()}",
            lastSyncAt = System.currentTimeMillis()
        )
        val playlistId = playlistDao.insert(playlist)

        var liveCount = 0
        var vodCount = 0
        var seriesCount = 0

        // 1. Categorias e Canais Ao Vivo
        try {
            val liveCats = try {
                api.getLiveCategories(username.trim(), password.trim()).associate { it.categoryId to it.categoryName.trim() }
            } catch (_: Exception) {
                emptyMap()
            }

            val liveStreams = api.getLiveStreams(username.trim(), password.trim())
            val channels = liveStreams.map { item ->
                val catName = item.categoryId?.let { liveCats[it] }?.ifBlank { null } ?: "Canais Gerais"
                ChannelEntity(
                    id = "xtream_${playlistId}_live_${item.streamId}",
                    playlistId = playlistId,
                    name = item.name,
                    logoUrl = item.streamIcon,
                    groupTitle = catName,
                    streamUrl = "${cleanServer}live/${username.trim()}/${password.trim()}/${item.streamId}.m3u8",
                    tvgId = item.epgChannelId,
                    contentType = ContentType.LIVE,
                    isFavorite = false
                )
            }
            channelDao.insertChannels(channels)
            liveCount = channels.size
        } catch (_: Exception) {}

        // 2. Categorias e Filmes (VOD)
        try {
            val vodCats = try {
                api.getVodCategories(username.trim(), password.trim()).associate { it.categoryId to it.categoryName.trim() }
            } catch (_: Exception) {
                emptyMap()
            }

            val vodStreams = api.getVodStreams(username.trim(), password.trim())
            val vodChannels = vodStreams.map { item ->
                val ext = item.containerExtension?.ifBlank { "mp4" } ?: "mp4"
                val catName = item.categoryId?.let { vodCats[it] }?.ifBlank { null } ?: "Filmes Gerais"
                ChannelEntity(
                    id = "xtream_${playlistId}_vod_${item.streamId}",
                    playlistId = playlistId,
                    name = item.name,
                    logoUrl = item.streamIcon,
                    groupTitle = catName,
                    streamUrl = "${cleanServer}movie/${username.trim()}/${password.trim()}/${item.streamId}.$ext",
                    tvgId = null,
                    contentType = ContentType.MOVIE,
                    isFavorite = false
                )
            }
            channelDao.insertChannels(vodChannels)
            vodCount = vodChannels.size
        } catch (_: Exception) {}

        // 3. Categorias e Séries
        try {
            val seriesCats = try {
                api.getSeriesCategories(username.trim(), password.trim()).associate { it.categoryId to it.categoryName.trim() }
            } catch (_: Exception) {
                emptyMap()
            }

            val seriesStreams = api.getSeries(username.trim(), password.trim())
            val seriesChannels = seriesStreams.map { item ->
                val catName = item.categoryId?.let { seriesCats[it] }?.ifBlank { null } ?: "Séries Gerais"
                ChannelEntity(
                    id = "xtream_${playlistId}_series_${item.seriesId}",
                    playlistId = playlistId,
                    name = item.name,
                    logoUrl = item.cover,
                    groupTitle = catName,
                    streamUrl = "${cleanServer}series/${username.trim()}/${password.trim()}/${item.seriesId}",
                    tvgId = null,
                    contentType = ContentType.SERIES,
                    isFavorite = false
                )
            }
            channelDao.insertChannels(seriesChannels)
            seriesCount = seriesChannels.size
        } catch (_: Exception) {}

        PlaylistImportResult(
            playlistId = playlistId,
            name = playlistName,
            liveCount = liveCount,
            vodCount = vodCount,
            seriesCount = seriesCount
        )
    }

    suspend fun deletePlaylist(id: Long) = withContext(Dispatchers.IO) {
        channelDao.deleteChannelsByPlaylist(id)
        playlistDao.deleteById(id)
    }
}
