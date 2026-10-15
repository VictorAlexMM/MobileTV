package com.iptvplayer.app.feature.series

import com.iptvplayer.app.core.database.entity.ChannelEntity
import com.iptvplayer.app.core.database.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeriesMetadataHelperTest {

    @Test
    fun testCleanSeriesTitle_stripsTagsAndEpisodeNumbers() {
        assertEquals("Breaking Bad", SeriesMetadataHelper.cleanSeriesTitle("[BR] Breaking Bad S01 E01 Pilot"))
        assertEquals("Stranger Things", SeriesMetadataHelper.cleanSeriesTitle("Stranger Things 1x01 Chapter One"))
        assertEquals("The Boys", SeriesMetadataHelper.cleanSeriesTitle("SÉRIES | The Boys - T02E05"))
        assertEquals("Dark", SeriesMetadataHelper.cleanSeriesTitle("Dark Temporada 1 Episodio 03"))
        assertEquals("Vikings", SeriesMetadataHelper.cleanSeriesTitle("FHD | Vikings S06E10"))
        assertEquals("Chaves", SeriesMetadataHelper.cleanSeriesTitle("Chaves - Ep 150 - O Parque de Diversoes"))
    }

    @Test
    fun testExtractPureSeriesTitle() {
        val title1 = SeriesMetadataHelper.extractPureSeriesTitle("[BR] Breaking Bad S02 E08")
        assertEquals("Breaking Bad", title1)

        val title2 = SeriesMetadataHelper.extractPureSeriesTitle("Stranger Things 02x04")
        assertEquals("Stranger Things", title2)
    }

    @Test
    fun testIsMatchingSeries_differentiatesSameFromDifferentSeries() {
        // Mesma série em formatos variados
        assertTrue(SeriesMetadataHelper.isMatchingSeries("Breaking Bad S01 E01", "Breaking Bad"))
        assertTrue(SeriesMetadataHelper.isMatchingSeries("[BR] Breaking Bad S02E05", "Breaking Bad"))
        assertTrue(SeriesMetadataHelper.isMatchingSeries("Breaking Bad - 1x03", "Breaking Bad"))

        // Séries diferentes - NUNCA devem bater
        assertFalse(SeriesMetadataHelper.isMatchingSeries("Better Call Saul S01E01", "Breaking Bad"))
        assertFalse(SeriesMetadataHelper.isMatchingSeries("Stranger Things S01E01", "Breaking Bad"))
        assertFalse(SeriesMetadataHelper.isMatchingSeries("Vikings S01E01", "Breaking Bad"))
        assertFalse(SeriesMetadataHelper.isMatchingSeries("The Boys S01E01", "The Office"))
    }

    @Test
    fun testGroupEpisodes_filtersStrictlyForTargetSeries() {
        val mixedEpisodes = listOf(
            createChannel("1", "Breaking Bad S01 E01 Pilot"),
            createChannel("2", "Breaking Bad S01 E02 Cat's in the Bag"),
            createChannel("3", "Better Call Saul S01 E01 Uno"), // Outra série misturada na categoria
            createChannel("4", "Stranger Things 1x01 Chapter One"), // Outra série misturada na categoria
            createChannel("5", "Breaking Bad S02 E01 Seven Thirty-Seven")
        )

        val details = SeriesMetadataHelper.groupEpisodes(mixedEpisodes, fallbackSeriesTitle = "Breaking Bad")

        assertEquals("Breaking Bad", details.seriesTitle)
        // Deve ter 2 temporadas de Breaking Bad
        assertEquals(2, details.seasons.size)

        // Temporada 1 deve ter 2 episódios (Pilot e Cat's in the Bag)
        val s1 = details.seasons.first { it.seasonNumber == 1 }
        assertEquals(2, s1.episodes.size)
        assertEquals(1, s1.episodes[0].episodeNumber)
        assertEquals(2, s1.episodes[1].episodeNumber)
        assertEquals("Pilot", s1.episodes[0].episodeTitle)

        // Temporada 2 deve ter 1 episódio
        val s2 = details.seasons.first { it.seasonNumber == 2 }
        assertEquals(1, s2.episodes.size)
        assertEquals(1, s2.episodes[0].episodeNumber)

        // Total de episódios deve ser 3 (Better Call Saul e Stranger Things foram descartados!)
        val totalEps = details.seasons.sumOf { it.episodes.size }
        assertEquals(3, totalEps)
    }

    @Test
    fun testParseXtreamSeriesInfo_correctlyBuildsPlayableEpisodes() {
        val sampleJson = """
            {
                "info": {
                    "name": "The Last of Us",
                    "plot": "Vinte anos após uma pandemia devastar a civilização...",
                    "genre": "Ação, Drama, Pós-Apocalíptico",
                    "cover": "https://img.test/tlou.jpg"
                },
                "episodes": {
                    "1": [
                        {
                            "id": "5001",
                            "episode_num": 1,
                            "title": "When You're Lost in the Darkness",
                            "container_extension": "mkv",
                            "info": {
                                "movie_image": "https://img.test/ep1.jpg"
                            }
                        },
                        {
                            "id": "5002",
                            "episode_num": 2,
                            "title": "Infected",
                            "container_extension": "mp4",
                            "info": {
                                "movie_image": "https://img.test/ep2.jpg"
                            }
                        }
                    ]
                }
            }
        """.trimIndent()

        val seriesChannel = createChannel("xtream_1_series_99", "The Last of Us")
        val details = SeriesMetadataHelper.parseXtreamSeriesInfo(
            jsonString = sampleJson,
            seriesItem = seriesChannel,
            cleanServer = "http://iptv.server:8080/",
            username = "myuser",
            password = "mypassword"
        )

        assertEquals("The Last of Us", details.seriesTitle)
        assertEquals("Ação, Drama, Pós-Apocalíptico", details.genre)
        assertTrue(details.plot.contains("pandemia"))
        assertEquals(1, details.seasons.size)

        val s1 = details.seasons[0]
        assertEquals(1, s1.seasonNumber)
        assertEquals(2, s1.episodes.size)

        val ep1 = s1.episodes[0]
        assertEquals(1, ep1.episodeNumber)
        assertEquals("When You're Lost in the Darkness", ep1.episodeTitle)
        assertEquals("http://iptv.server:8080/series/myuser/mypassword/5001.mkv", ep1.channel.streamUrl)

        val ep2 = s1.episodes[1]
        assertEquals(2, ep2.episodeNumber)
        assertEquals("Infected", ep2.episodeTitle)
        assertEquals("http://iptv.server:8080/series/myuser/mypassword/5002.mp4", ep2.channel.streamUrl)
    }

    private fun createChannel(id: String, name: String): ChannelEntity {
        return ChannelEntity(
            id = id,
            playlistId = 1L,
            name = name,
            logoUrl = "https://img.test/poster.jpg",
            groupTitle = "SÉRIES GERAIS",
            streamUrl = "http://iptv.server/stream/$id.mkv",
            tvgId = null,
            contentType = ContentType.SERIES,
            isFavorite = false
        )
    }
}
