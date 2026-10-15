package com.iptvplayer.app.core.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StorageUtils {

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return String.format(Locale.US, "%.1f %s", value, units[digitGroups])
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0L) return "-"
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getAvailableStorageBytes(context: Context): Long {
        val path = context.getExternalFilesDir(null) ?: Environment.getDataDirectory()
        val stat = StatFs(path.path)
        return stat.availableBlocksLong * stat.blockSizeLong
    }

    fun getTotalStorageBytes(context: Context): Long {
        val path = context.getExternalFilesDir(null) ?: Environment.getDataDirectory()
        val stat = StatFs(path.path)
        return stat.blockCountLong * stat.blockSizeLong
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Caminho de Downloads") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Caminho copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Tenta abrir a pasta de downloads no gerenciador de arquivos nativo do aparelho.
     * Suporta Android DocumentsUI, Samsung My Files e fallback com FileProvider.
     */
    fun openFolderInFileManager(context: Context, folder: File): Boolean {
        // Tentativa 1: Intent Samsung My Files
        try {
            val samsungIntent = Intent("samsung.myfiles.intent.action.LAUNCH_MY_FILES").apply {
                putExtra("samsung.myfiles.intent.extra.START_PATH", folder.absolutePath)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(samsungIntent)
            return true
        } catch (_: Exception) {}

        // Tentativa 2: Intent FileProvider ACTION_VIEW com tipo MIME de diretório
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                folder
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "resource/folder")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return true
        } catch (_: Exception) {}

        // Tentativa 3: Intent genérica para gerenciador de arquivos
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                folder
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Abrir pasta de downloads com").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            return true
        } catch (_: Exception) {}

        return false
    }
}
