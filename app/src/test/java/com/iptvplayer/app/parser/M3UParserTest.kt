package com.iptvplayer.app.parser

import com.iptvplayer.app.core.database.entity.ChannelEntity
import com.iptvplayer.app.core.database.model.ContentType
import com.iptvplayer.app.parser.m3u.M3UParser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.ByteArrayInputStream

class M3UParserTest {

    @Test
    fun testParseStreaming_extractsChannelsAndInfersTypesCorrectly() = runBlocking {
        val sampleM3u = """
            #EXTM3U
            #EXTINF:-1 tvg-id="globo.br" tvg-name="Globo" tvg-logo="https://cdn.test/globo.png" group-title="Aberto", TV Globo HD
            http://server.test:8080/live/user/pass/101.m3u8
            #EXTINF:-1 tvg-id="" tvg-name="Inception" tvg-logo="https://cdn.test/inception.jpg" group-title="Filmes Ação", A Origem (2010)
            http://server.test:8080/movie/user/pass/502.mp4
            #EXTINF:-1 tvg-id="" tvg-name="Breaking Bad S01E01" group-title="Séries Drama", Breaking Bad S01E01
            http://server.test:8080/series/user/pass/9001.mkv
        """.trimIndent()

        val parsedBatches = mutableListOf<List<ChannelEntity>>()
        val total = M3UParser.parseStreaming(
            inputStream = ByteArrayInputStream(sampleM3u.toByteArray(Charsets.UTF_8)),
            playlistId = 1L,
            batchSize = 2
        ) { batch ->
            parsedBatches.add(batch)
        }

        assertEquals(3, total)
        val allChannels = parsedBatches.flatten()
        assertEquals(3, allChannels.size)

        // Canal 1: Live
        val ch1 = allChannels[0]
        assertEquals("TV Globo HD", ch1.name)
        assertEquals("globo.br", ch1.tvgId)
        assertEquals("https://cdn.test/globo.png", ch1.logoUrl)
        assertEquals("Aberto", ch1.groupTitle)
        assertEquals(ContentType.LIVE, ch1.contentType)
        assertNotNull(ch1.id)

        // Canal 2: Movie
        val ch2 = allChannels[1]
        assertEquals("A Origem (2010)", ch2.name)
        assertEquals("https://cdn.test/inception.jpg", ch2.logoUrl)
        assertEquals("Filmes Ação", ch2.groupTitle)
        assertEquals(ContentType.MOVIE, ch2.contentType)

        // Canal 3: Series Episode
        val ch3 = allChannels[2]
        assertEquals("Breaking Bad S01E01", ch3.name)
        assertEquals("Séries Drama", ch3.groupTitle)
        assertEquals(ContentType.EPISODE, ch3.contentType)
    }
}
