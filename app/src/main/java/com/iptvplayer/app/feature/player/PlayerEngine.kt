package com.iptvplayer.app.feature.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.iptvplayer.app.core.network.NetworkGate
import com.iptvplayer.app.feature.downloads.DownloadModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val contentId: String = "",
    val streamUrl: String = "",
    val contentTitle: String = "",
    val isLive: Boolean = false,
    val isOfflinePlayback: Boolean = false,
    val isMuted: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(UnstableApi::class)
class PlayerEngine(
    private val context: Context,
    private val networkGate: NetworkGate,
    private val coroutineScope: CoroutineScope,
    private val onProgressUpdate: (suspend (contentId: String, posMs: Long, durMs: Long) -> Unit)? = null
) {
    private var exoPlayer: ExoPlayer? = null
    private var progressJob: Job? = null
    private var currentContentId: String? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun getPlayer(): ExoPlayer {
        return exoPlayer ?: createPlayer().also { exoPlayer = it }
    }

    private fun createPlayer(): ExoPlayer {
        val dataSourceFactory = DownloadModule.buildPlaybackDataSourceFactory(context, networkGate)
        val mediaSourceFactory = DefaultMediaSourceFactory(context.applicationContext)
            .setDataSourceFactory(dataSourceFactory)

        val player = ExoPlayer.Builder(context.applicationContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setPreferredAudioLanguage("por")
            .setPreferredTextLanguage("por")
            .build()

        player.volume = if (_uiState.value.isMuted) 0f else 1f

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                if (isPlaying) startProgressLoop() else stopProgressLoop()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val isBuffering = playbackState == Player.STATE_BUFFERING
                val duration = if (player.duration > 0) player.duration else 0L
                val isLive = player.isCurrentMediaItemLive

                _uiState.value = _uiState.value.copy(
                    isBuffering = isBuffering,
                    durationMs = duration,
                    isLive = isLive
                )
            }

            override fun onPlayerError(error: PlaybackException) {
                android.util.Log.e("PlayerEngine", "Falha na reprodução de $currentContentId: [${error.errorCodeName}] ${error.message}", error)
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.localizedMessage ?: "Erro na reprodução"
                )
            }
        })

        return player
    }

    fun playContent(
        contentId: String,
        title: String,
        streamUrl: String,
        isHls: Boolean = true,
        startPositionMs: Long = 0L,
        isDownloaded: Boolean = false
    ) {
        currentContentId = contentId
        val player = getPlayer()

        // 1. Tenta obter o MediaItem oficial a partir do DownloadManager se estiver concluído no disco
        val downloadManager = DownloadModule.getDownloadManager(context)
        val completedDownload = try {
            val d = downloadManager.downloadIndex.getDownload(contentId)
            if (d != null && d.state == androidx.media3.exoplayer.offline.Download.STATE_COMPLETED && d.bytesDownloaded > 0L) {
                d
            } else null
        } catch (_: Exception) {
            null
        }

        val mediaItem = if (completedDownload != null) {
            completedDownload.request.toMediaItem().buildUpon()
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(title)
                        .build()
                )
                .build()
        } else {
            val mediaItemBuilder = MediaItem.Builder()
                .setMediaId(contentId)
                .setUri(Uri.parse(streamUrl))
                .setCustomCacheKey(streamUrl)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(title)
                        .build()
                )

            val mimeType = when {
                isHls || streamUrl.contains(".m3u8", ignoreCase = true) -> MimeTypes.APPLICATION_M3U8
                streamUrl.contains(".mpd", ignoreCase = true) -> MimeTypes.APPLICATION_MPD
                streamUrl.contains(".mp4", ignoreCase = true) -> MimeTypes.VIDEO_MP4
                streamUrl.contains(".mkv", ignoreCase = true) -> MimeTypes.VIDEO_MATROSKA
                streamUrl.contains(".ts", ignoreCase = true) -> MimeTypes.VIDEO_MP2T
                else -> null
            }
            if (mimeType != null) {
                mediaItemBuilder.setMimeType(mimeType)
            }
            mediaItemBuilder.build()
        }

        _uiState.value = _uiState.value.copy(
            contentId = contentId,
            streamUrl = streamUrl,
            contentTitle = title,
            isOfflinePlayback = isDownloaded || (completedDownload != null) || !networkGate.canAccessNetwork(),
            errorMessage = null
        )

        player.volume = if (_uiState.value.isMuted) 0f else 1f
        player.setMediaItem(mediaItem)
        if (startPositionMs > 0) {
            player.seekTo(startPositionMs)
        }
        player.prepare()
        player.playWhenReady = true
    }

    fun toggleMute() {
        val newMute = !_uiState.value.isMuted
        exoPlayer?.volume = if (newMute) 0f else 1f
        _uiState.value = _uiState.value.copy(isMuted = newMute)
    }

    fun setMuted(muted: Boolean) {
        exoPlayer?.volume = if (muted) 0f else 1f
        _uiState.value = _uiState.value.copy(isMuted = muted)
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun play() {
        exoPlayer?.play()
    }

    private fun startProgressLoop() {
        stopProgressLoop()
        progressJob = coroutineScope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    val pos = player.currentPosition
                    val dur = if (player.duration > 0) player.duration else 0L
                    _uiState.value = _uiState.value.copy(
                        currentPositionMs = pos,
                        durationMs = dur
                    )
                    currentContentId?.let { id ->
                        onProgressUpdate?.invoke(id, pos, dur)
                    }
                }
                delay(10000) // Salva o progresso a cada 10 segundos
            }
        }
    }

    private fun stopProgressLoop() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressLoop()
        exoPlayer?.release()
        exoPlayer = null
    }
}
