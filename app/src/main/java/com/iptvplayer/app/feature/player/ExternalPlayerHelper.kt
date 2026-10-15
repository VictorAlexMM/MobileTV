package com.iptvplayer.app.feature.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object ExternalPlayerHelper {

    /**
     * Tenta abrir o stream em um reprodutor de vídeo externo instalado (VLC, MX Player, Just Player, etc.)
     * Retorna true se um player foi encontrado e a Intent iniciada com sucesso.
     */
    fun launchExternalPlayer(context: Context, streamUrl: String, title: String? = null): Boolean {
        return try {
            val uri = Uri.parse(streamUrl)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "video/*")
                title?.let { putExtra("title", it) }
                putExtra("android.intent.extra.TITLE", title)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Reproduzir com reprodutor externo...")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Nenhum player externo encontrado ou erro ao abrir: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
