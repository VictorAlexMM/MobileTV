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
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        val player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setPreferredAudioLanguage("por")
            .setPreferredTextLanguage("por")
            .build()

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

        val mediaItemBuilder = MediaItem.Builder()
            .setMediaId(contentId)
            .setUri(Uri.parse(streamUrl))

        if (isHls || streamUrl.contains(".m3u8", ignoreCase = true)) {
            mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
        }

        val mediaItem = mediaItemBuilder.build()

        _uiState.value = _uiState.value.copy(
            contentId = contentId,
            streamUrl = streamUrl,
            contentTitle = title,
            isOfflinePlayback = isDownloaded || !networkGate.canAccessNetwork(),
            errorMessage = null
        )

        player.setMediaItem(mediaItem)
        if (startPositionMs > 0) {
            player.seekTo(startPositionMs)
        }
        player.prepare()
        player.playWhenReady = true
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
