package com.iptvplayer.app.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.iptvplayer.app.R
import com.iptvplayer.app.core.database.AppDatabase
import com.iptvplayer.app.core.datastore.SettingsRepository
import com.iptvplayer.app.core.designsystem.components.OfflineBanner
import com.iptvplayer.app.core.designsystem.theme.DarkBackground
import com.iptvplayer.app.core.designsystem.theme.DarkSurface
import com.iptvplayer.app.core.designsystem.theme.PrimaryIndigo
import com.iptvplayer.app.core.designsystem.theme.StatusWarningAmber
import com.iptvplayer.app.core.network.NetworkGate
import com.iptvplayer.app.feature.downloads.DownloadController
import com.iptvplayer.app.feature.downloads.DownloadsScreen
import com.iptvplayer.app.feature.live.ChannelRepository
import com.iptvplayer.app.feature.live.LiveScreen
import com.iptvplayer.app.feature.player.PlayerEngine
import com.iptvplayer.app.feature.player.PlayerScreen
import com.iptvplayer.app.feature.playlists.PlaylistRepository
import com.iptvplayer.app.feature.playlists.PlaylistsScreen
import com.iptvplayer.app.feature.settings.SettingsScreen
import com.iptvplayer.app.feature.vod.VodScreen
import kotlinx.coroutines.launch

enum class AppDestination(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    LIVE("Ao Vivo", Icons.Default.LiveTv),
    MOVIES("Filmes", Icons.Default.Movie),
    DOWNLOADS("Downloads", Icons.Default.Download),
    PLAYLISTS("Playlists", Icons.Default.PlaylistPlay),
    SETTINGS("Ajustes", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    database: AppDatabase,
    settingsRepository: SettingsRepository,
    networkGate: NetworkGate,
    playlistRepository: PlaylistRepository,
    channelRepository: ChannelRepository,
    downloadController: DownloadController,
    playerEngine: PlayerEngine
) {
    var currentDestination by remember { mutableStateOf(AppDestination.LIVE) }
    var isPlayingFullscreen by remember { mutableStateOf(false) }

    val activePlaylistId by settingsRepository.activePlaylistId.collectAsState(initial = null)
    val offlineModeEnabled by settingsRepository.offlineModeEnabled.collectAsState(initial = false)
    val isOnlineAllowed by networkGate.isOnlineAllowed.collectAsState()
    val isAppOffline = !isOnlineAllowed

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (isPlayingFullscreen) {
        PlayerScreen(
            playerEngine = playerEngine,
            onBackClick = { isPlayingFullscreen = false }
        )
    } else {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = "IPTV Player Offline",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = DarkSurface,
                            titleContentColor = Color.White
                        ),
                        actions = {
                            // Toggle rápido 'Ficar offline' na TopBar
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        settingsRepository.setOfflineMode(!offlineModeEnabled)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (offlineModeEnabled) Icons.Default.CloudOff else Icons.Default.Cloud,
                                    contentDescription = "Toggle Offline",
                                    tint = if (offlineModeEnabled) StatusWarningAmber else Color.White
                                )
                            }
                        }
                    )

                    // Banner fixo exibido quando offline (manual ou sem rede)
                    OfflineBanner(
                        isOffline = isAppOffline,
                        onGoOnlineClick = {
                            scope.launch {
                                settingsRepository.setOfflineMode(false)
                            }
                        }
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = Color.White
                ) {
                    AppDestination.entries.forEach { dest ->
                        NavigationBarItem(
                            selected = currentDestination == dest,
                            onClick = { currentDestination = dest },
                            icon = { Icon(dest.icon, contentDescription = dest.title) },
                            label = { Text(dest.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = PrimaryIndigo,
                                indicatorColor = PrimaryIndigo
                            )
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = DarkBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = currentDestination, label = "ScreenTransition") { destination ->
                    when (destination) {
                        AppDestination.LIVE -> {
                            LiveScreen(
                                repository = channelRepository,
                                activePlaylistId = activePlaylistId,
                                isOfflineMode = isAppOffline,
                                onPlayChannel = { channel ->
                                    playerEngine.playContent(
                                        contentId = channel.id,
                                        title = channel.name,
                                        streamUrl = channel.streamUrl,
                                        isHls = true,
                                        isDownloaded = false
                                    )
                                    isPlayingFullscreen = true
                                },
                                onEnableOnlineMode = {
                                    scope.launch { settingsRepository.setOfflineMode(false) }
                                }
                            )
                        }
                        AppDestination.MOVIES -> {
                            VodScreen(
                                repository = channelRepository,
                                downloadController = downloadController,
                                activePlaylistId = activePlaylistId,
                                isOfflineMode = isAppOffline,
                                onPlayMovie = { movie, isDownloaded ->
                                    playerEngine.playContent(
                                        contentId = movie.id,
                                        title = movie.name,
                                        streamUrl = movie.streamUrl,
                                        isHls = movie.streamUrl.contains(".m3u8", ignoreCase = true),
                                        isDownloaded = isDownloaded
                                    )
                                    isPlayingFullscreen = true
                                },
                                onEnableOnlineMode = {
                                    scope.launch { settingsRepository.setOfflineMode(false) }
                                },
                                snackbarHostState = snackbarHostState
                            )
                        }
                        AppDestination.DOWNLOADS -> {
                            DownloadsScreen(
                                database = database,
                                downloadController = downloadController,
                                onPlayOfflineItem = { download ->
                                    playerEngine.playContent(
                                        contentId = download.id,
                                        title = download.title,
                                        streamUrl = download.sourceUrl,
                                        isHls = download.sourceUrl.contains(".m3u8", ignoreCase = true),
                                        isDownloaded = true
                                    )
                                    isPlayingFullscreen = true
                                }
                            )
                        }
                        AppDestination.PLAYLISTS -> {
                            PlaylistsScreen(
                                repository = playlistRepository,
                                activePlaylistId = activePlaylistId,
                                onSelectPlaylist = { id ->
                                    scope.launch {
                                        settingsRepository.setActivePlaylistId(id)
                                    }
                                }
                            )
                        }
                        AppDestination.SETTINGS -> {
                            SettingsScreen(
                                settingsRepository = settingsRepository,
                                downloadController = downloadController
                            )
                        }
                    }
                }
            }
        }
    }
}
