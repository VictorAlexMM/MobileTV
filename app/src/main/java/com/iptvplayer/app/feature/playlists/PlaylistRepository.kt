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

class PlaylistRepository(
    private val database: AppDatabase,
    private val networkGate: NetworkGate
) {
    private val playlistDao = database.playlistDao()
    private val channelDao = database.channelDao()
    private val okHttpClient = NetworkClient.createOkHttpClient()

    fun getAllPlaylists(): Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    suspend fun getPlaylistById(id: Long): PlaylistEntity? = playlistDao.getPlaylistById(id)

    suspend fun importM3UFromUrl(name: String, url: String): Long = withContext(Dispatchers.IO) {
        if (!networkGate.canAccessNetwork()) {
            throw AppException.OfflineModeException()
        }

        val playlist = PlaylistEntity(
            name = name,
            type = PlaylistType.M3U_URL,
            url = url,
            username = null,
            password = null,
            epgUrl = null,
            lastSyncAt = System.currentTimeMillis()
        )
        val playlistId = playlistDao.insert(playlist)

        val request = Request.Builder().url(url).build()
        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            playlistDao.deleteById(playlistId)
            throw AppException.NetworkUnavailableException("HTTP ${response.code}: Não foi possível carregar a playlist.")
        }

        response.body?.byteStream()?.use { stream ->
            M3UParser.parseStreaming(
                inputStream = stream,
                playlistId = playlistId,
                batchSize = 500
            ) { batch ->
                channelDao.insertChannels(batch)
            }
        } ?: throw AppException.InvalidPlaylistException("Corpo de resposta vazio.")

        playlistId
    }

    suspend fun importM3UFromFile(name: String, inputStream: InputStream): Long = withContext(Dispatchers.IO) {
        val playlist = PlaylistEntity(
            name = name,
            type = PlaylistType.M3U_FILE,
            url = null,
            username = null,
            password = null,
            epgUrl = null,
            lastSyncAt = System.currentTimeMillis()
        )
        val playlistId = playlistDao.insert(playlist)

        inputStream.use { stream ->
            M3UParser.parseStreaming(
                inputStream = stream,
                playlistId = playlistId,
                batchSize = 500
            ) { batch ->
                channelDao.insertChannels(batch)
            }
        }

        playlistId
    }

    suspend fun importXtreamAccount(
        serverUrl: String,
        username: String,
        password: String
    ): Long = withContext(Dispatchers.IO) {
        if (!networkGate.canAccessNetwork()) {
            throw AppException.OfflineModeException()
        }

        val cleanServer = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
        val retrofit = NetworkClient.createRetrofit(cleanServer, okHttpClient)
        val api = retrofit.create(XtreamApiService::class.java)

        val auth = api.authenticate(username, password)
        if (auth.userInfo?.status != "Active") {
            throw AppException.NetworkUnavailableException("Autenticação Xtream falhou ou conta inativa.")
        }

        val playlist = PlaylistEntity(
            name = "Xtream: $username",
            type = PlaylistType.XTREAM,
            url = cleanServer,
            username = username,
            password = password,
            epgUrl = "${cleanServer}xmltv.php?username=$username&password=$password",
            lastSyncAt = System.currentTimeMillis()
        )
        val playlistId = playlistDao.insert(playlist)

        // Sincroniza canais ao vivo do Xtream
        try {
            val liveStreams = api.getLiveStreams(username, password)
            val channels = liveStreams.map { item ->
                ChannelEntity(
                    id = "xtream_${playlistId}_live_${item.streamId}",
                    playlistId = playlistId,
                    name = item.name,
                    logoUrl = item.streamIcon,
                    groupTitle = "Ao Vivo",
                    streamUrl = "${cleanServer}live/$username/$password/${item.streamId}.m3u8",
                    tvgId = item.epgChannelId,
                    contentType = ContentType.LIVE,
                    isFavorite = false
                )
            }
            channelDao.insertChannels(channels)
        } catch (_: Exception) {}

        // Sincroniza filmes (VOD) do Xtream
        try {
            val vodStreams = api.getVodStreams(username, password)
            val vodChannels = vodStreams.map { item ->
                val ext = item.containerExtension ?: "mp4"
                ChannelEntity(
                    id = "xtream_${playlistId}_vod_${item.streamId}",
                    playlistId = playlistId,
                    name = item.name,
                    logoUrl = item.streamIcon,
                    groupTitle = "Filmes",
                    streamUrl = "${cleanServer}movie/$username/$password/${item.streamId}.$ext",
                    tvgId = null,
                    contentType = ContentType.MOVIE,
                    isFavorite = false
                )
            }
            channelDao.insertChannels(vodChannels)
        } catch (_: Exception) {}

        playlistId
    }

    suspend fun deletePlaylist(id: Long) = withContext(Dispatchers.IO) {
        channelDao.deleteChannelsByPlaylist(id)
        playlistDao.deleteById(id)
    }
}
