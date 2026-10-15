package com.iptvplayer.app.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
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
import com.iptvplayer.app.feature.player.CompactPlayerView
import com.iptvplayer.app.feature.player.ExternalPlayerHelper
import com.iptvplayer.app.feature.player.PlayerEngine
import com.iptvplayer.app.feature.player.PlayerScreen
import com.iptvplayer.app.feature.playlists.PlaylistRepository
import com.iptvplayer.app.feature.playlists.PlaylistsScreen
import com.iptvplayer.app.feature.recordings.RecordingScheduler
import com.iptvplayer.app.feature.settings.SettingsScreen
import com.iptvplayer.app.feature.vod.VodScreen
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.platform.LocalContext
import com.iptvplayer.app.feature.home.HomeScreen
import com.iptvplayer.app.feature.series.SeriesDetails
import com.iptvplayer.app.feature.series.SeriesScreen
import kotlinx.coroutines.launch

enum class AppDestination(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Início", Icons.Default.Home),
    LIVE("Canais", Icons.Default.LiveTv),
    MOVIES("Filmes", Icons.Default.Movie),
    SERIES("Séries", Icons.Default.VideoLibrary),
    DOWNLOADS("Downloads", Icons.Default.Download),
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
    playerEngine: PlayerEngine,
    isInPipMode: Boolean = false,
    onEnterPip: () -> Unit = {}
) {
    val context = LocalContext.current
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    var isPlayingFullscreen by remember { mutableStateOf(false) }
    var isCompactPlayerVisible by remember { mutableStateOf(false) }
    var currentSeriesDetails by remember { mutableStateOf<SeriesDetails?>(null) }

    val activePlaylistId by settingsRepository.activePlaylistId.collectAsState(initial = null)
    val allPlaylists by playlistRepository.getAllPlaylists().collectAsState(initial = emptyList())
    val offlineModeEnabled by settingsRepository.offlineModeEnabled.collectAsState(initial = false)
    val externalPlayerEnabled by settingsRepository.externalPlayerEnabled.collectAsState(initial = false)
    val isOnlineAllowed by networkGate.isOnlineAllowed.collectAsState()
    val isAppOffline = !isOnlineAllowed

    // Sincronização automática da playlist ativa no primeiro carregamento
    androidx.compose.runtime.LaunchedEffect(allPlaylists, activePlaylistId) {
        if (activePlaylistId == null && allPlaylists.isNotEmpty()) {
            settingsRepository.setActivePlaylistId(allPlaylists.first().id)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var lastBackPressTime by remember { androidx.compose.runtime.mutableLongStateOf(0L) }
    val activity = context as? Activity

    // Se estiver em fullscreen, o botão de voltar sai do fullscreen para o player compacto
    BackHandler(enabled = isPlayingFullscreen) {
        isPlayingFullscreen = false
        isCompactPlayerVisible = true
    }

    // Se estiver em uma aba secundária e não estiver em detalhes de série, volta para Início
    BackHandler(enabled = !isPlayingFullscreen && currentDestination != AppDestination.HOME && currentSeriesDetails == null) {
        currentDestination = AppDestination.HOME
    }

    // Se estiver na tela inicial (Início), requer duplo clique para sair do aplicativo, evitando saída acidental
    BackHandler(enabled = !isPlayingFullscreen && currentDestination == AppDestination.HOME) {
        val now = System.currentTimeMillis()
        if (now - lastBackPressTime < 2000L) {
            activity?.finish()
        } else {
            lastBackPressTime = now
            Toast.makeText(context, "Pressione voltar novamente para sair", Toast.LENGTH_SHORT).show()
        }
    }

    val handlePlay: (String, String, String, Boolean, Long) -> Unit = { id, title, url, isDownloaded, startPositionMs ->
        if (externalPlayerEnabled && !isDownloaded) {
            val opened = ExternalPlayerHelper.launchExternalPlayer(context, url, title)
            if (!opened) {
                playerEngine.playContent(
                    contentId = id,
                    title = title,
                    streamUrl = url,
                    isHls = url.contains(".m3u8", ignoreCase = true),
                    startPositionMs = startPositionMs,
                    isDownloaded = false
                )
                isPlayingFullscreen = true
                isCompactPlayerVisible = false
            }
        } else {
            playerEngine.playContent(
                contentId = id,
                title = title,
                streamUrl = url,
                isHls = url.contains(".m3u8", ignoreCase = true),
                startPositionMs = startPositionMs,
                isDownloaded = isDownloaded
            )
            isPlayingFullscreen = true
            isCompactPlayerVisible = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = "MobileTV Offline",
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

                            // Botão de Ajustes / Configurações
                            IconButton(
                                onClick = { currentDestination = AppDestination.SETTINGS }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Ajustes",
                                    tint = if (currentDestination == AppDestination.SETTINGS) PrimaryIndigo else Color.White
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
                    AppDestination.entries.filter { it != AppDestination.SETTINGS }.forEach { dest ->
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isCompactPlayerVisible && !isPlayingFullscreen) {
                    CompactPlayerView(
                        playerEngine = playerEngine,
                        onExpandFullscreen = { isPlayingFullscreen = true },
                        onClosePlayer = {
                            playerEngine.stop()
                            isCompactPlayerVisible = false
                        },
                        onEnterPip = onEnterPip
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Crossfade(targetState = currentDestination, label = "ScreenTransition") { destination ->
                    when (destination) {
                        AppDestination.HOME -> {
                            HomeScreen(
                                database = database,
                                channelRepository = channelRepository,
                                downloadController = downloadController,
                                activePlaylistId = activePlaylistId,
                                isOfflineMode = isAppOffline,
                                onNavigateTo = { dest -> currentDestination = dest },
                                onPlayItem = { id, title, url, isDownloaded, startPos ->
                                    handlePlay(id, title, url, isDownloaded, startPos)
                                },
                                snackbarHostState = snackbarHostState
                            )
                        }
                        AppDestination.LIVE -> {
                            LiveScreen(
                                repository = channelRepository,
                                database = database,
                                snackbarHostState = snackbarHostState,
                                activePlaylistId = activePlaylistId,
                                isOfflineMode = isAppOffline,
                                onPlayChannel = { channel ->
                                    handlePlay(channel.id, channel.name, channel.streamUrl, false, 0L)
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
                                    handlePlay(movie.id, movie.name, movie.streamUrl, isDownloaded, 0L)
                                },
                                onEnableOnlineMode = {
                                    scope.launch { settingsRepository.setOfflineMode(false) }
                                },
                                snackbarHostState = snackbarHostState
                            )
                        }
                        AppDestination.SERIES -> {
                            SeriesScreen(
                                repository = channelRepository,
                                database = database,
                                downloadController = downloadController,
                                activePlaylistId = activePlaylistId,
                                isOfflineMode = isAppOffline,
                                selectedSeriesDetails = currentSeriesDetails,
                                onSelectSeriesDetails = { currentSeriesDetails = it },
                                onPlaySeries = { series, isDownloaded ->
                                    handlePlay(series.id, series.name, series.streamUrl, isDownloaded, 0L)
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
                                    handlePlay(download.id, download.title, download.sourceUrl, true, 0L)
                                },
                                onPlayRecording = { filePath, title ->
                                    playerEngine.playContent(
                                        contentId = filePath,
                                        title = title,
                                        streamUrl = filePath,
                                        isHls = false,
                                        startPositionMs = 0L,
                                        isDownloaded = true
                                    )
                                    isCompactPlayerVisible = true
                                    isPlayingFullscreen = false
                                }
                            )
                        }
                        AppDestination.SETTINGS -> {
                            SettingsScreen(
                                settingsRepository = settingsRepository,
                                downloadController = downloadController,
                                playlistRepository = playlistRepository,
                                activePlaylistId = activePlaylistId,
                                onSelectPlaylist = { id ->
                                    scope.launch {
                                        settingsRepository.setActivePlaylistId(id)
                                    }
                                },
                                snackbarHostState = snackbarHostState,
                                database = database,
                                onPlayOfflineItem = { download ->
                                    handlePlay(download.id, download.title, download.sourceUrl, true, 0L)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

        if (isPlayingFullscreen || isInPipMode) {
            PlayerScreen(
                playerEngine = playerEngine,
                onBackClick = {
                    isPlayingFullscreen = false
                    isCompactPlayerVisible = true
                },
                onClosePlayer = {
                    playerEngine.stop()
                    isPlayingFullscreen = false
                    isCompactPlayerVisible = false
                },
                isInPipMode = isInPipMode,
                onEnterPip = onEnterPip,
                onStartRecording = { channelId, title, streamUrl, duration ->
                    scope.launch {
                        try {
                            RecordingScheduler.startImmediateRecording(
                                context = context,
                                database = database,
                                channelId = channelId,
                                title = title,
                                streamUrl = streamUrl,
                                durationMinutes = duration
                            )
                            snackbarHostState.showSnackbar("Gravação iniciada: $title ($duration min)")
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Erro ao iniciar gravação: ${e.localizedMessage}")
                        }
                    }
                }
            )
        }
    }
}

