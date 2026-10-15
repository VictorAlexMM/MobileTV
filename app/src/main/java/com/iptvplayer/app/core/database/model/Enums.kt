package com.iptvplayer.app.core.database.model

enum class PlaylistType {
    M3U_URL,
    M3U_FILE,
    XTREAM,
    STALKER
}

enum class ContentType {
    LIVE,
    MOVIE,
    EPISODE
}

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

enum class RecordingStatus {
    SCHEDULED,
    RECORDING,
    DONE,
    FAILED
}
