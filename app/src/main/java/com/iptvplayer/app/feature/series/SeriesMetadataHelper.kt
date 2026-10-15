package com.iptvplayer.app.feature.series

import com.iptvplayer.app.core.database.entity.ChannelEntity

data class ParsedEpisode(
    val channel: ChannelEntity,
    val seriesTitle: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val episodeTitle: String
)

data class SeriesSeasonGroup(
    val seasonNumber: Int,
    val seasonName: String,
    val episodes: List<ParsedEpisode>
)

data class SeriesDetails(
    val seriesTitle: String,
    val genre: String,
    val plot: String,
    val posterUrl: String?,
    val seasons: List<SeriesSeasonGroup>
)

object SeriesMetadataHelper {

    private val SEASON_EP_REGEX = Regex(
        """^(.*?)\s*[-–—:]*\s*(?:S|T|Temporada|Season)\s*0*(\d+)[\s._-]*(?:E|EP|Episodio|Episode)\s*0*(\d+)\s*[-–—:]*\s*(.*)$""",
        RegexOption.IGNORE_CASE
    )
    private val S_E_COMPACT_REGEX = Regex(
        """^(.*?)\s*[-–—:]*\s*S0*(\d+)E0*(\d+)\s*[-–—:]*\s*(.*)$""",
        RegexOption.IGNORE_CASE
    )
    private val EP_ONLY_REGEX = Regex(
        """^(.*?)\s*[-–—:]*\s*(?:E|EP|Episodio|Episode)\s*0*(\d+)\s*[-–—:]*\s*(.*)$""",
        RegexOption.IGNORE_CASE
    )

    fun parseEpisode(channel: ChannelEntity): ParsedEpisode {
        val rawName = channel.name
        val group = channel.groupTitle

        SEASON_EP_REGEX.find(rawName)?.let { match ->
            val seriesRaw = match.groupValues[1].trim()
            val season = match.groupValues[2].toIntOrNull() ?: 1
            val episode = match.groupValues[3].toIntOrNull() ?: 1
            val epName = match.groupValues[4].trim().ifBlank { "Episódio $episode" }
            val seriesTitle = resolveSeriesTitle(seriesRaw, group)
            return ParsedEpisode(channel, seriesTitle, season, episode, epName)
        }

        S_E_COMPACT_REGEX.find(rawName)?.let { match ->
            val seriesRaw = match.groupValues[1].trim()
            val season = match.groupValues[2].toIntOrNull() ?: 1
            val episode = match.groupValues[3].toIntOrNull() ?: 1
            val epName = match.groupValues[4].trim().ifBlank { "Episódio $episode" }
            val seriesTitle = resolveSeriesTitle(seriesRaw, group)
            return ParsedEpisode(channel, seriesTitle, season, episode, epName)
        }

        EP_ONLY_REGEX.find(rawName)?.let { match ->
            val seriesRaw = match.groupValues[1].trim()
            val episode = match.groupValues[2].toIntOrNull() ?: 1
            val epName = match.groupValues[3].trim().ifBlank { "Episódio $episode" }
            val season = extractSeasonFromText(group) ?: 1
            val seriesTitle = resolveSeriesTitle(seriesRaw, group)
            return ParsedEpisode(channel, seriesTitle, season, episode, epName)
        }

        val season = extractSeasonFromText(group) ?: extractSeasonFromText(rawName) ?: 1
        val seriesTitle = resolveSeriesTitle(rawName, group)
        return ParsedEpisode(channel, seriesTitle, season, 1, rawName)
    }

    fun groupEpisodes(episodes: List<ChannelEntity>, fallbackSeriesTitle: String? = null): SeriesDetails {
        val parsedList = episodes.map { parseEpisode(it) }
        val seriesTitle = fallbackSeriesTitle?.let { cleanSeriesTitle(it) }
            ?: parsedList.firstOrNull()?.seriesTitle
            ?: "Série"

        val seasonsMap = parsedList
            .groupBy { it.seasonNumber }
            .toSortedMap()
            .map { (seasonNum, eps) ->
                SeriesSeasonGroup(
                    seasonNumber = seasonNum,
                    seasonName = "Temporada $seasonNum",
                    episodes = eps.sortedBy { it.episodeNumber }
                )
            }

        val sample = episodes.firstOrNull()
        val genre = detectGenre(seriesTitle, sample?.groupTitle ?: "")
        val plot = buildSynopsis(seriesTitle, genre, seasonsMap.size, parsedList.size)

        return SeriesDetails(
            seriesTitle = seriesTitle,
            genre = genre,
            plot = plot,
            posterUrl = sample?.logoUrl,
            seasons = seasonsMap
        )
    }

    fun resolveSeriesTitle(extracted: String, groupTitle: String?): String {
        val clean = cleanSeriesTitle(extracted)
        if (clean.isNotBlank() && clean.length > 2) return clean
        if (!groupTitle.isNullOrBlank()) {
            return cleanSeriesTitle(groupTitle)
        }
        return "Série"
    }

    fun cleanSeriesTitle(text: String): String {
        return text
            .replace(Regex("""^\[.*?\]\s*"""), "")
            .replace(Regex("""^(?:SÉRIES|SERIES|SÉRIE|SERIE)\s*\|\s*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*[-–—:]*\s*(?:Temporada|Season)\s*\d+.*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*[-–—:]*\s*S\d+.*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*[-–—:|]+\s*$"""), "")
            .replace(Regex("""^\s*[-–—:|]+\s*"""), "")
            .trim()
    }

    private fun extractSeasonFromText(text: String?): Int? {
        if (text.isNullOrBlank()) return null
        val match = Regex("""(?:Temporada|Season|T|S)\s*0*(\d+)""", RegexOption.IGNORE_CASE).find(text)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun detectGenre(name: String, group: String): String {
        val combined = "$name $group".lowercase()
        return when {
            combined.contains("drama") -> "Drama & Suspense"
            combined.contains("ação") || combined.contains("acao") || combined.contains("action") -> "Ação & Aventura"
            combined.contains("comédia") || combined.contains("comedia") -> "Comédia"
            combined.contains("ficção") || combined.contains("sci-fi") -> "Ficção Científica"
            combined.contains("terror") || combined.contains("horror") -> "Terror & Mistério"
            combined.contains("animação") || combined.contains("anime") -> "Animação"
            combined.contains("crime") || combined.contains("policial") -> "Crime & Investigação"
            else -> "Série & Drama"
        }
    }

    private fun buildSynopsis(title: String, genre: String, totalSeasons: Int, totalEpisodes: Int): String {
        return "Acompanhe as aventuras e reviravoltas de '$title', uma série aclamada no gênero $genre. A produção conta com $totalSeasons temporada(s) e $totalEpisodes episódio(s) repletos de mistério, drama e ação envolvente."
    }
}
