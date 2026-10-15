package com.iptvplayer.app.feature.downloads

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import androidx.annotation.OptIn
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.iptvplayer.app.core.common.AppException
import com.iptvplayer.app.core.database.AppDatabase
import com.iptvplayer.app.core.database.entity.DownloadEntity
import com.iptvplayer.app.core.database.model.DownloadStatus
import com.iptvplayer.app.core.datastore.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class DownloadController(
    private val context: Context,
    private val database: AppDatabase,
    private val settings: SettingsRepository,
    private val scope: CoroutineScope
) {
    private val downloadManager = DownloadModule.getDownloadManager(context)

    init {
        // Escuta os eventos do Media3 DownloadManager para manter o Room 100% sincronizado
        downloadManager.addListener(object : DownloadManager.Listener {
            override fun onDownloadChanged(
                manager: DownloadManager,
                download: Download,
                finalException: Exception?
            ) {
                scope.launch {
                    val status = mapMedia3Status(download.state)
                    val bytes = download.bytesDownloaded
                    val total = download.contentLength
                    val percent = if (download.percentDownloaded != -1f) download.percentDownloaded else 0f
                    val errorMsg = finalException?.localizedMessage

                    database.downloadDao().updateProgress(
                        id = download.request.id,
                        bytesDownloaded = bytes,
                        totalBytes = total,
                        percent = percent,
                        status = status,
                        errorMessage = errorMsg
                    )
                }
            }

            override fun onDownloadRemoved(manager: DownloadManager, download: Download) {
                scope.launch {
                    database.downloadDao().deleteById(download.request.id)
                }
            }
        })
    }

    suspend fun startDownload(
        contentId: String,
        title: String,
        sourceUrl: String,
        posterUrl: String? = null
    ) {
        // 1. Verificação de espaço livre em disco (mínimo 200MB de folga)
        val freeBytes = getAvailableStorageBytes()
        if (freeBytes < 200 * 1024 * 1024) {
            throw AppException.InsufficientStorageException()
        }

        // 2. Registro no Room
        val entity = DownloadEntity(
            id = contentId,
            channelId = contentId,
            title = title,
            posterUrl = posterUrl,
            sourceUrl = sourceUrl,
            status = DownloadStatus.QUEUED
        )
        database.downloadDao().insertOrUpdate(entity)

        // 3. Montagem da requisição Media3
        val uri = Uri.parse(sourceUrl)
        val requestBuilder = DownloadRequest.Builder(contentId, uri)

        if (sourceUrl.contains(".m3u8", ignoreCase = true)) {
            requestBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
        }

        val request = requestBuilder.build()

        // 4. Disparo do serviço em primeiro plano
        DownloadService.sendAddDownload(
            context,
            AppDownloadService::class.java,
            request,
            false
        )
    }

    fun pauseDownload(contentId: String) {
        DownloadService.sendSetStopReason(
            context,
            AppDownloadService::class.java,
            contentId,
            Download.STOP_REASON_NONE,
            false
        )
    }

    fun resumeDownload(contentId: String) {
        DownloadService.sendSetStopReason(
            context,
            AppDownloadService::class.java,
            contentId,
            0,
            false
        )
    }

    fun removeDownload(contentId: String) {
        DownloadService.sendRemoveDownload(
            context,
            AppDownloadService::class.java,
            contentId,
            false
        )
        scope.launch {
            database.downloadDao().deleteById(contentId)
        }
    }

    fun pauseAllDownloads() {
        DownloadService.sendPauseDownloads(
            context,
            AppDownloadService::class.java,
            false
        )
    }

    fun resumeAllDownloads() {
        DownloadService.sendResumeDownloads(
            context,
            AppDownloadService::class.java,
            false
        )
    }

    private fun getAvailableStorageBytes(): Long {
        val path = context.getExternalFilesDir(null) ?: Environment.getDataDirectory()
        val stat = StatFs(path.path)
        return stat.availableBlocksLong * stat.blockSizeLong
    }

    private fun mapMedia3Status(state: Int): DownloadStatus = when (state) {
        Download.STATE_QUEUED -> DownloadStatus.QUEUED
        Download.STATE_DOWNLOADING -> DownloadStatus.DOWNLOADING
        Download.STATE_STOPPED -> DownloadStatus.PAUSED
        Download.STATE_COMPLETED -> DownloadStatus.COMPLETED
        Download.STATE_FAILED -> DownloadStatus.FAILED
        Download.STATE_REMOVING -> DownloadStatus.PAUSED
        Download.STATE_RESTARTING -> DownloadStatus.DOWNLOADING
        else -> DownloadStatus.QUEUED
    }
}
