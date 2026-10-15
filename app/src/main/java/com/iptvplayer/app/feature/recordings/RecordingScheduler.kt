package com.iptvplayer.app.feature.recordings

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.iptvplayer.app.core.database.AppDatabase
import com.iptvplayer.app.core.database.entity.RecordingEntity
import com.iptvplayer.app.core.database.model.RecordingStatus
import java.io.File
import java.util.concurrent.TimeUnit

object RecordingScheduler {

    /**
     * Inicia a gravação imediata de uma transmissão ao vivo com duração definida em minutos.
     */
    suspend fun startImmediateRecording(
        context: Context,
        database: AppDatabase,
        channelId: String,
        title: String,
        streamUrl: String,
        durationMinutes: Int
    ): Long {
        val entity = RecordingEntity(
            channelId = channelId,
            title = title,
            streamUrl = streamUrl,
            scheduledStart = System.currentTimeMillis(),
            durationMinutes = durationMinutes,
            status = RecordingStatus.SCHEDULED
        )
        val recordingId = database.recordingDao().insert(entity)

        val inputData = Data.Builder()
            .putLong(LiveStreamRecordingWorker.KEY_RECORDING_ID, recordingId)
            .putString(LiveStreamRecordingWorker.KEY_STREAM_URL, streamUrl)
            .putInt(LiveStreamRecordingWorker.KEY_DURATION_MINUTES, durationMinutes)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<LiveStreamRecordingWorker>()
            .setInputData(inputData)
            .addTag("recording_$recordingId")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "work_recording_$recordingId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        return recordingId
    }

    /**
     * Agenda a gravação futura de um programa do EPG com início no startEpochMs.
     */
    suspend fun scheduleEpgRecording(
        context: Context,
        database: AppDatabase,
        channelId: String,
        title: String,
        streamUrl: String,
        startEpochMs: Long,
        durationMinutes: Int
    ): Long {
        val entity = RecordingEntity(
            channelId = channelId,
            title = title,
            streamUrl = streamUrl,
            scheduledStart = startEpochMs,
            durationMinutes = durationMinutes,
            status = RecordingStatus.SCHEDULED
        )
        val recordingId = database.recordingDao().insert(entity)

        val delayMs = (startEpochMs - System.currentTimeMillis()).coerceAtLeast(0)

        val inputData = Data.Builder()
            .putLong(LiveStreamRecordingWorker.KEY_RECORDING_ID, recordingId)
            .putString(LiveStreamRecordingWorker.KEY_STREAM_URL, streamUrl)
            .putInt(LiveStreamRecordingWorker.KEY_DURATION_MINUTES, durationMinutes)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<LiveStreamRecordingWorker>()
            .setInputData(inputData)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag("recording_$recordingId")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "work_recording_$recordingId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        return recordingId
    }

    /**
     * Cancela o agendamento ou a gravação em andamento e atualiza o status.
     */
    suspend fun cancelRecording(
        context: Context,
        database: AppDatabase,
        recordingId: Long
    ) {
        WorkManager.getInstance(context).cancelUniqueWork("work_recording_$recordingId")
        val rec = database.recordingDao().getRecordingById(recordingId)
        if (rec != null && rec.status != RecordingStatus.DONE) {
            database.recordingDao().update(rec.copy(status = RecordingStatus.FAILED))
        }
    }

    /**
     * Remove o registro do Room e apaga o arquivo físico gravado do disco.
     */
    suspend fun deleteRecording(
        context: Context,
        database: AppDatabase,
        recordingId: Long,
        filePath: String?
    ) {
        WorkManager.getInstance(context).cancelUniqueWork("work_recording_$recordingId")
        if (!filePath.isNullOrBlank()) {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
        }
        database.recordingDao().deleteById(recordingId)
    }
}
