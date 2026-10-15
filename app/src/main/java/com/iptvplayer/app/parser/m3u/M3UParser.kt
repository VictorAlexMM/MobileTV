package com.iptvplayer.app.parser.m3u

import com.iptvplayer.app.core.database.entity.ChannelEntity
import com.iptvplayer.app.core.database.model.ContentType
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.security.MessageDigest

object M3UParser {

    private val TVG_ID_REGEX = """tvg-id="([^"]*)"""".toRegex()
    private val TVG_NAME_REGEX = """tvg-name="([^"]*)"""".toRegex()
    private val TVG_LOGO_REGEX = """tvg-logo="([^"]*)"""".toRegex()
    private val GROUP_TITLE_REGEX = """group-title="([^"]*)"""".toRegex()

    /**
     * Faz o parsing de um stream M3U em blocos (batching), permitindo processar
     * arquivos de 50.000+ canais sem estourar a memória heap do Android.
     */
    suspend fun parseStreaming(
        inputStream: InputStream,
        playlistId: Long,
        batchSize: Int = 500,
        onBatchParsed: suspend (List<ChannelEntity>) -> Unit
    ): Int {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val currentBatch = ArrayList<ChannelEntity>(batchSize)
        var totalParsed = 0

        var currentTvgId: String? = null
        var currentTvgName: String? = null
        var currentLogoUrl: String? = null
        var currentGroupTitle: String? = null
        var currentChannelName: String? = null

        reader.useLines { lines ->
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue

                if (trimmed.startsWith("#EXTINF:", ignoreCase = true)) {
                    currentTvgId = TVG_ID_REGEX.find(trimmed)?.groupValues?.get(1)?.ifBlank { null }
                    currentTvgName = TVG_NAME_REGEX.find(trimmed)?.groupValues?.get(1)?.ifBlank { null }
                    currentLogoUrl = TVG_LOGO_REGEX.find(trimmed)?.groupValues?.get(1)?.ifBlank { null }
                    currentGroupTitle = GROUP_TITLE_REGEX.find(trimmed)?.groupValues?.get(1)?.ifBlank { null }

                    // O nome do canal fica após a última vírgula da linha #EXTINF
                    val commaIndex = trimmed.lastIndexOf(',')
                    currentChannelName = if (commaIndex != -1 && commaIndex < trimmed.length - 1) {
                        trimmed.substring(commaIndex + 1).trim()
                    } else {
                        currentTvgName ?: "Canal Desconhecido"
                    }
                } else if (!trimmed.startsWith("#")) {
                    // É uma URL de stream
                    val streamUrl = trimmed
                    val name = currentChannelName ?: currentTvgName ?: "Canal Desconhecido"
                    val channelId = generateChannelId(playlistId, streamUrl)
                    val contentType = inferContentType(streamUrl, currentGroupTitle)

                    val channel = ChannelEntity(
                        id = channelId,
                        playlistId = playlistId,
                        name = name,
                        logoUrl = currentLogoUrl,
                        groupTitle = currentGroupTitle ?: "Geral",
                        streamUrl = streamUrl,
                        tvgId = currentTvgId,
                        contentType = contentType,
                        isFavorite = false
                    )

                    currentBatch.add(channel)
                    totalParsed++

                    if (currentBatch.size >= batchSize) {
                        onBatchParsed(ArrayList(currentBatch))
                        currentBatch.clear()
                    }

                    // Reset para o próximo canal
                    currentTvgId = null
                    currentTvgName = null
                    currentLogoUrl = null
                    currentGroupTitle = null
                    currentChannelName = null
                }
            }
        }

        if (currentBatch.isNotEmpty()) {
            onBatchParsed(ArrayList(currentBatch))
            currentBatch.clear()
        }

        return totalParsed
    }

    private fun generateChannelId(playlistId: Long, streamUrl: String): String {
        val input = "$playlistId:$streamUrl"
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun inferContentType(url: String, groupTitle: String?): ContentType {
        val lowerUrl = url.lowercase()
        val lowerGroup = groupTitle?.lowercase() ?: ""

        return when {
            lowerUrl.contains("/movie/") || lowerGroup.contains("filme") || lowerGroup.contains("movie") || lowerGroup.contains("vod") -> {
                ContentType.MOVIE
            }
            lowerUrl.contains("/series/") || lowerGroup.contains("serie") || lowerGroup.contains("série") || lowerGroup.contains("episod") -> {
                ContentType.EPISODE
            }
            else -> ContentType.LIVE
        }
    }
}
