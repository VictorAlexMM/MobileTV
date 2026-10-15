package com.iptvplayer.app

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.iptvplayer.app.core.database.AppDatabase
import com.iptvplayer.app.core.database.entity.WatchProgressEntity
import com.iptvplayer.app.core.datastore.SettingsRepository
import com.iptvplayer.app.core.designsystem.theme.IPTVPlayerTheme
import com.iptvplayer.app.core.network.AndroidConnectivityObserver
import com.iptvplayer.app.core.network.NetworkGate
import com.iptvplayer.app.feature.downloads.DownloadController
import com.iptvplayer.app.feature.live.ChannelRepository
import com.iptvplayer.app.feature.player.PlayerEngine
import com.iptvplayer.app.feature.playlists.PlaylistRepository
import com.iptvplayer.app.ui.MainApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {

    private lateinit var playerEngine: PlayerEngine
    private val _isInPipMode = MutableStateFlow(false)
    val isInPipMode = _isInPipMode.asStateFlow()

    fun enterPipMode(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            return enterPictureInPictureMode(params)
        }
        return false
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        _isInPipMode.value = isInPictureInPictureMode
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (::playerEngine.isInitialized && playerEngine.uiState.value.isPlaying) {
            enterPipMode()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(this)
        val settingsRepository = SettingsRepository(this)
        val connectivityObserver = AndroidConnectivityObserver(this)
        val networkGate = NetworkGate(connectivityObserver, settingsRepository, lifecycleScope)

        val playlistRepository = PlaylistRepository(database, networkGate)
        val channelRepository = ChannelRepository(database, networkGate)
        val downloadController = DownloadController(this, database, settingsRepository, lifecycleScope)

        playerEngine = PlayerEngine(this, networkGate, lifecycleScope) { contentId, pos, dur ->
            database.watchProgressDao().saveProgress(
                WatchProgressEntity(
                    contentId = contentId,
                    positionMs = pos,
                    durationMs = dur
                )
            )
        }

        setContent {
            val pipMode by isInPipMode.collectAsState()
            IPTVPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp(
                        database = database,
                        settingsRepository = settingsRepository,
                        networkGate = networkGate,
                        playlistRepository = playlistRepository,
                        channelRepository = channelRepository,
                        downloadController = downloadController,
                        playerEngine = playerEngine,
                        isInPipMode = pipMode,
                        onEnterPip = { enterPipMode() }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerEngine.release()
    }
}
