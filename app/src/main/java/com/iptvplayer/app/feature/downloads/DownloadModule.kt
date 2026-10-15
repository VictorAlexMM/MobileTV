package com.iptvplayer.app.feature.downloads

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.offline.DownloadManager
import com.iptvplayer.app.core.network.NetworkClient
import com.iptvplayer.app.core.network.NetworkGate
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.Executors

@OptIn(UnstableApi::class)
object DownloadModule {

    @Volatile
    private var databaseProvider: DatabaseProvider? = null

    @Volatile
    private var downloadCache: Cache? = null

    @Volatile
    private var downloadManager: DownloadManager? = null

    @Synchronized
    fun getDatabaseProvider(context: Context): DatabaseProvider {
        return databaseProvider ?: StandaloneDatabaseProvider(context.applicationContext).also {
            databaseProvider = it
        }
    }

    @Synchronized
    fun getDownloadCache(context: Context): Cache {
        return downloadCache ?: run {
            val downloadDir = File(context.applicationContext.getExternalFilesDir(null), "downloads")
            if (!downloadDir.exists()) downloadDir.mkdirs()
            val db = getDatabaseProvider(context)
            SimpleCache(downloadDir, NoOpCacheEvictor(), db).also {
                downloadCache = it
            }
        }
    }

    fun getHttpDataSourceFactory(okHttpClient: OkHttpClient = NetworkClient.createOkHttpClient()): DataSource.Factory {
        return OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent("IPTVSmartPlayer/1.0 (Linux; Android) ExoPlayerLib/Media3")
    }

    @Synchronized
    fun getDownloadManager(
        context: Context,
        maxParallelDownloads: Int = 3
    ): DownloadManager {
        return downloadManager ?: run {
            val ctx = context.applicationContext
            val db = getDatabaseProvider(ctx)
            val cache = getDownloadCache(ctx)
            val httpFactory = getHttpDataSourceFactory()
            val executor = Executors.newFixedThreadPool(maxParallelDownloads)

            DownloadManager(ctx, db, cache, httpFactory, executor).apply {
                this.maxParallelDownloads = maxParallelDownloads
                this.requirements = androidx.media3.exoplayer.scheduler.Requirements(
                    androidx.media3.exoplayer.scheduler.Requirements.NETWORK
                )
                this.resumeDownloads()
                downloadManager = this
            }
        }
    }

    /**
     * Constrói o CacheDataSource.Factory para reprodução de mídia.
     * Quando onlineAllowed = true: toca do cache se existir; senão, busca na rede.
     * Quando onlineAllowed = false: lê SOMENTE do cache local, com ZERO tráfego de rede.
     */
    fun buildPlaybackDataSourceFactory(
        context: Context,
        networkGate: NetworkGate,
        okHttpClient: OkHttpClient = NetworkClient.createOkHttpClient()
    ): DataSource.Factory {
        val cache = getDownloadCache(context)
        val isAllowed = networkGate.canAccessNetwork()

        val upstreamFactory = if (isAllowed) {
            DefaultDataSource.Factory(context, OkHttpDataSource.Factory(okHttpClient))
        } else {
            null // Sem upstream -> leitura 100% offline
        }

        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(null) // somente leitura na reprodução
            .setFlags(if (isAllowed) 0 else CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR.inv())
    }
}
