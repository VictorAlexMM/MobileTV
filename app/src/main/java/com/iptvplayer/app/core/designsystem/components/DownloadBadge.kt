package com.iptvplayer.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptvplayer.app.core.database.model.DownloadStatus
import com.iptvplayer.app.core.designsystem.theme.StatusDownloadedGreen
import com.iptvplayer.app.core.designsystem.theme.StatusDownloadingBlue
import com.iptvplayer.app.core.designsystem.theme.StatusErrorRed
import com.iptvplayer.app.core.designsystem.theme.StatusWarningAmber

@Composable
fun DownloadBadge(
    status: DownloadStatus?,
    percent: Float = 0f,
    modifier: Modifier = Modifier
) {
    if (status == null) return

    val (bgColor, contentColor, icon, text) = when (status) {
        DownloadStatus.COMPLETED -> Quadruple(
            StatusDownloadedGreen.copy(alpha = 0.15f),
            StatusDownloadedGreen,
            Icons.Default.CheckCircle,
            "Baixado"
        )
        DownloadStatus.DOWNLOADING -> Quadruple(
            StatusDownloadingBlue.copy(alpha = 0.15f),
            StatusDownloadingBlue,
            Icons.Default.Downloading,
            if (percent > 0) "${percent.toInt()}%" else "Baixando"
        )
        DownloadStatus.QUEUED -> Quadruple(
            StatusDownloadingBlue.copy(alpha = 0.10f),
            StatusDownloadingBlue,
            Icons.Default.Downloading,
            "Na fila"
        )
        DownloadStatus.PAUSED -> Quadruple(
            StatusWarningAmber.copy(alpha = 0.15f),
            StatusWarningAmber,
            Icons.Default.PauseCircle,
            "Pausado"
        )
        DownloadStatus.FAILED -> Quadruple(
            StatusErrorRed.copy(alpha = 0.15f),
            StatusErrorRed,
            Icons.Default.Error,
            "Falha"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = contentColor
                ),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
