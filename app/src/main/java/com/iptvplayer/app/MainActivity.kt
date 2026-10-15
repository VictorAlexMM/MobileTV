package com.iptvplayer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

class MainActivity : ComponentActivity() {

    private lateinit var playerEngine: PlayerEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(this)
        val settingsRepository = SettingsRepository(this)
        val connectivityObserver = AndroidConnectivityObserver(this)
        val networkGate = NetworkGate(connectivityObserver, settingsRepository, lifecycleScope)

        val playlistRepository = PlaylistRepository(database, networkGate)
        val channelRepository = ChannelRepository(database)
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
                        playerEngine = playerEngine
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
