package com.iptvplayer.app.feature.recordings

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.iptvplayer.app.core.database.AppDatabase
import com.iptvplayer.app.core.database.model.RecordingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class LiveStreamRecordingWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_RECORDING_ID = "key_recording_id"
        const val KEY_STREAM_URL = "key_stream_url"
        const val KEY_DURATION_MINUTES = "key_duration_minutes"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val recordingId = inputData.getLong(KEY_RECORDING_ID, -1L)
        val streamUrl = inputData.getString(KEY_STREAM_URL)
        val durationMinutes = inputData.getInt(KEY_DURATION_MINUTES, 30)

        if (recordingId == -1L || streamUrl.isNullOrBlank()) {
            return@withContext Result.failure()
        }

        val database = AppDatabase.getInstance(context)
        val recordingDao = database.recordingDao()

        val recording = recordingDao.getRecordingById(recordingId) ?: return@withContext Result.failure()

        // 1. Atualiza status para RECORDING
        recordingDao.update(recording.copy(status = RecordingStatus.RECORDING))

        // 2. Cria diretório de gravações e arquivo de saída (.ts)
        val recordingsDir = File(context.getExternalFilesDir(null), "recordings").apply { mkdirs() }
        val outputFile = File(recordingsDir, "rec_${recordingId}_${System.currentTimeMillis()}.ts")

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(streamUrl)
            .header("User-Agent", "Mozilla/5.0 (Android; IPTVPlayer)")
            .build()

        val endTimestamp = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    recordingDao.update(recording.copy(status = RecordingStatus.FAILED))
                    return@withContext Result.failure()
                }

                val body = response.body ?: run {
                    recordingDao.update(recording.copy(status = RecordingStatus.FAILED))
                    return@withContext Result.failure()
                }

                body.byteStream().use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        val buffer = ByteArray(16 * 1024)
                        var bytesRead: Int

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            if (isStopped || System.currentTimeMillis() >= endTimestamp) {
                                break
                            }
                            outputStream.write(buffer, 0, bytesRead)
                        }
                        outputStream.flush()
                    }
                }
            }

            // 3. Sucesso se arquivo tiver dados capturados (>100KB)
            if (outputFile.exists() && outputFile.length() > 100 * 1024) {
                recordingDao.update(
                    recording.copy(
                        status = RecordingStatus.DONE,
                        filePath = outputFile.absolutePath
                    )
                )
                Result.success()
            } else {
                recordingDao.update(recording.copy(status = RecordingStatus.FAILED))
                Result.failure()
            }
        } catch (e: Exception) {
            if (outputFile.exists() && outputFile.length() > 200 * 1024) {
                // Preserva o que foi gravado até o erro/cancelamento
                recordingDao.update(
                    recording.copy(
                        status = RecordingStatus.DONE,
                        filePath = outputFile.absolutePath
                    )
                )
                Result.success()
            } else {
                recordingDao.update(recording.copy(status = RecordingStatus.FAILED))
                Result.failure()
            }
        }
    }
}
