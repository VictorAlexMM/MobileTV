package com.iptvplayer.app.parser.epg

import android.util.Xml
import com.iptvplayer.app.core.database.entity.EpgProgramEntity
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object XmltvParser {

    private val xmltvDateFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val xmltvFallbackFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Faz o parsing de XMLTV utilizando XmlPullParser de streaming,
     * emitindo blocos de programas sem acumular o XML inteiro na memória RAM.
     */
    suspend fun parseStreaming(
        inputStream: InputStream,
        batchSize: Int = 500,
        onBatchParsed: suspend (List<EpgProgramEntity>) -> Unit
    ): Int {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, "UTF-8")

        val currentBatch = ArrayList<EpgProgramEntity>(batchSize)
        var totalParsed = 0

        var eventType = parser.eventType
        var currentChannelId: String? = null
        var currentStartTime: Long = 0L
        var currentEndTime: Long = 0L
        var currentTitle: String? = null
        var currentDesc: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "programme" -> {
                            currentChannelId = parser.getAttributeValue(null, "channel")
                            val startAttr = parser.getAttributeValue(null, "start")
                            val stopAttr = parser.getAttributeValue(null, "stop")

                            currentStartTime = parseTimestamp(startAttr)
                            currentEndTime = parseTimestamp(stopAttr)
                            currentTitle = null
                            currentDesc = null
                        }
                        "title" -> {
                            currentTitle = parser.nextText()
                        }
                        "desc" -> {
                            currentDesc = parser.nextText()
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "programme") {
                        if (!currentChannelId.isNullOrBlank() && !currentTitle.isNullOrBlank()) {
                            val entity = EpgProgramEntity(
                                channelTvgId = currentChannelId,
                                title = currentTitle,
                                description = currentDesc,
                                start = currentStartTime,
                                end = currentEndTime
                            )
                            currentBatch.add(entity)
                            totalParsed++

                            if (currentBatch.size >= batchSize) {
                                onBatchParsed(ArrayList(currentBatch))
                                currentBatch.clear()
                            }
                        }
                        currentChannelId = null
                        currentTitle = null
                        currentDesc = null
                    }
                }
            }
            eventType = parser.next()
        }

        if (currentBatch.isNotEmpty()) {
            onBatchParsed(ArrayList(currentBatch))
            currentBatch.clear()
        }

        return totalParsed
    }

    private fun parseTimestamp(raw: String?): Long {
        if (raw.isNullOrBlank()) return 0L
        val clean = raw.trim()
        return try {
            xmltvDateFormat.parse(clean)?.time ?: 0L
        } catch (_: Exception) {
            try {
                xmltvFallbackFormat.parse(clean.take(14))?.time ?: 0L
            } catch (_: Exception) {
                0L
            }
        }
    }
}
