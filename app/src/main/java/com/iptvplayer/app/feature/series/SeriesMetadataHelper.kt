package com.iptvplayer.app.feature.series

import com.iptvplayer.app.core.database.entity.ChannelEntity
import com.iptvplayer.app.core.database.model.ContentType
import com.iptvplayer.app.core.network.NetworkClient
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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

    // S01E01, Temporada 1 Episodio 01, Temp 1 Ep 1, T1 EP 1, etc.
    private val SEASON_EP_REGEX = Regex(
        """^(.*?)\s*[-–—:]*\s*(?:S|T|Temp|Temporada|Season)\s*0*(\d+)[\s._-]*(?:E|EP|Episodio|Episode|Cap|Capitulo|Capítulo)\s*0*(\d+)\s*[-–—:]*\s*(.*)$""",
        RegexOption.IGNORE_CASE
    )

    // S01E01, T01E01, S1EP1, T1EP1 (compact)
    private val S_E_COMPACT_REGEX = Regex(
        """^(.*?)\s*[-–—:]*\s*(?:S|T)0*(\d+)(?:E|EP)0*(\d+)\s*[-–—:]*\s*(.*)$""",
        RegexOption.IGNORE_CASE
    )

    // 1x01, 01x05
    private val SEASON_X_EP_REGEX = Regex(
        """^(.*?)\s*[-–—:]*\s*0*(\d+)\s*[xX]\s*0*(\d+)\s*[-–—:]*\s*(.*)$""",
        RegexOption.IGNORE_CASE
    )

    // E01, EP 01, Episodio 1
    private val EP_ONLY_REGEX = Regex(
        """^(.*?)\s*[-–—:]*\s*(?:E|EP|Episodio|Episode|Cap|Capitulo|Capítulo)\s*0*(\d+)\s*[-–—:]*\s*(.*)$""",
        RegexOption.IGNORE_CASE
    )

    fun parseEpisode(channel: ChannelEntity): ParsedEpisode {
        return parseEpisode(channel.name, channel.groupTitle, channel)
    }

    fun parseEpisode(
        rawName: String,
        groupTitle: String? = null,
        channel: ChannelEntity? = null
    ): ParsedEpisode {
        val targetChannel = channel ?: ChannelEntity(
            id = "",
            playlistId = 0,
            name = rawName,
            logoUrl = null,
            groupTitle = groupTitle,
            streamUrl = "",
            tvgId = null,
            contentType = ContentType.SERIES,
            isFavorite = false
        )

        val cleanName = cleanPrefixesAndTags(rawName)

        SEASON_EP_REGEX.find(cleanName)?.let { match ->
            val seriesRaw = match.groupValues[1].trim()
            val season = match.groupValues[2].toIntOrNull() ?: 1
            val episode = match.groupValues[3].toIntOrNull() ?: 1
            val epName = match.groupValues[4].trim().ifBlank { "Episódio $episode" }
            val seriesTitle = resolveSeriesTitle(seriesRaw, groupTitle)
            return ParsedEpisode(targetChannel, seriesTitle, season, episode, epName)
        }

        S_E_COMPACT_REGEX.find(cleanName)?.let { match ->
            val seriesRaw = match.groupValues[1].trim()
            val season = match.groupValues[2].toIntOrNull() ?: 1
            val episode = match.groupValues[3].toIntOrNull() ?: 1
            val epName = match.groupValues[4].trim().ifBlank { "Episódio $episode" }
            val seriesTitle = resolveSeriesTitle(seriesRaw, groupTitle)
            return ParsedEpisode(targetChannel, seriesTitle, season, episode, epName)
        }

        SEASON_X_EP_REGEX.find(cleanName)?.let { match ->
            val seriesRaw = match.groupValues[1].trim()
            val season = match.groupValues[2].toIntOrNull() ?: 1
            val episode = match.groupValues[3].toIntOrNull() ?: 1
            val epName = match.groupValues[4].trim().ifBlank { "Episódio $episode" }
            val seriesTitle = resolveSeriesTitle(seriesRaw, groupTitle)
            return ParsedEpisode(targetChannel, seriesTitle, season, episode, epName)
        }

        EP_ONLY_REGEX.find(cleanName)?.let { match ->
            val seriesRaw = match.groupValues[1].trim()
            val episode = match.groupValues[2].toIntOrNull() ?: 1
            val epName = match.groupValues[3].trim().ifBlank { "Episódio $episode" }
            val season = extractSeasonFromText(groupTitle) ?: extractSeasonFromText(cleanName) ?: 1
            val seriesTitle = resolveSeriesTitle(seriesRaw, groupTitle)
            return ParsedEpisode(targetChannel, seriesTitle, season, episode, epName)
        }

        val season = extractSeasonFromText(groupTitle) ?: extractSeasonFromText(cleanName) ?: 1
        val seriesTitle = resolveSeriesTitle(cleanName, groupTitle)
        return ParsedEpisode(targetChannel, seriesTitle, season, 1, cleanName)
    }

    /**
     * Remove tags iniciais como [BR], (DUB), [4K], bem como prefixos de categoria (SÉRIES |, BR |).
     */
    fun cleanPrefixesAndTags(text: String): String {
        return text
            .replace(Regex("""^\[.*?\]\s*"""), "")
            .replace(Regex("""^\((?:BR|DUB|LEG|FHD|4K|HD|PT|EN|DUBLADO|LEGENDADO)\)\s*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""^(?:SÉRIES|SERIES|SÉRIE|SERIE|BR|FHD|4K|HD|VOD)\s*[-–—:|]\s*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*[-–—:|]+\s*$"""), "")
            .replace(Regex("""^\s*[-–—:|]+\s*"""), "")
            .trim()
    }

    /**
     * Limpa o título da série extraindo o nome puro (sem números de episódio, temporada ou sufixos).
     */
    fun cleanSeriesTitle(text: String): String {
        var result = cleanPrefixesAndTags(text)
        result = result
            .replace(Regex("""\s*[-–—:]*\s*\b(?:Temp|Temporada|Season)\s*0*\d+.*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*[-–—:]*\s*\b[ST]0*\d+.*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*[-–—:]*\s*\b\d+\s*[xX]\s*0*\d+.*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*[-–—:]*\s*\b(?:E|EP|Episodio|Episode|Cap|Capitulo|Capítulo)\s*0*\d+.*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*[-–—:|]+\s*$"""), "")
            .replace(Regex("""^\s*[-–—:|]+\s*"""), "")
            .trim()
        return if (result.isNotBlank()) result else text.trim()
    }

    /**
     * Extrai o nome limpo e canônico de uma série a partir do nome de qualquer episódio.
     */
    fun extractPureSeriesTitle(rawName: String): String {
        val parsed = parseEpisode(rawName)
        val fromParsed = cleanSeriesTitle(parsed.seriesTitle)
        if (fromParsed.isNotBlank() && fromParsed.length >= 2 && !fromParsed.equals("Série", ignoreCase = true)) {
            return fromParsed
        }
        val clean = cleanSeriesTitle(rawName)
        return if (clean.isNotBlank()) clean else rawName.trim()
    }

    /**
     * Verifica com rigor se um candidato pertence à mesma série que o target,
     * evitando que episódios de séries aleatórias se misturem na mesma visualização.
     */
    fun isMatchingSeries(candidateName: String, targetSeriesTitle: String): Boolean {
        val cleanTarget = cleanSeriesTitle(targetSeriesTitle).lowercase().trim()
        if (cleanTarget.isBlank()) return false

        val cleanCandidate = cleanSeriesTitle(candidateName).lowercase().trim()
        if (cleanCandidate == cleanTarget) return true

        val normTarget = cleanTarget.replace(Regex("""[^a-z0-9]"""), "")
        val normCandidate = cleanCandidate.replace(Regex("""[^a-z0-9]"""), "")
        if (normTarget.isNotEmpty() && normCandidate == normTarget) return true

        val parsedCandidate = cleanSeriesTitle(parseEpisode(candidateName).seriesTitle).lowercase().trim()
        if (parsedCandidate == cleanTarget) return true
        val normParsed = parsedCandidate.replace(Regex("""[^a-z0-9]"""), "")
        if (normTarget.isNotEmpty() && normParsed == normTarget) return true

        if (normTarget.length >= 4) {
            if (normCandidate.startsWith(normTarget) || normTarget.startsWith(normCandidate)) return true
            if (normParsed.startsWith(normTarget) || normTarget.startsWith(normParsed)) return true
        }

        return false
    }

    fun groupEpisodes(episodes: List<ChannelEntity>, fallbackSeriesTitle: String? = null): SeriesDetails {
        val parsedList = episodes.map { parseEpisode(it) }
        val targetTitle = fallbackSeriesTitle?.let { cleanSeriesTitle(it) }
            ?: parsedList.firstOrNull()?.seriesTitle
            ?: "Série"

        // Filtragem estrita: garante que apenas episódios pertencentes a esta série sejam incluídos
        val filteredList = parsedList.filter { ep ->
            isMatchingSeries(ep.seriesTitle, targetTitle) ||
            isMatchingSeries(ep.channel.name, targetTitle)
        }.ifEmpty {
            parsedList
        }

        val seasonsMap = filteredList
            .groupBy { it.seasonNumber }
            .toSortedMap()
            .map { (seasonNum, eps) ->
                SeriesSeasonGroup(
                    seasonNumber = seasonNum,
                    seasonName = "Temporada $seasonNum",
                    episodes = eps.sortedBy { it.episodeNumber }
                )
            }

        val sample = filteredList.firstOrNull()?.channel ?: episodes.firstOrNull()
        val genre = detectGenre(targetTitle, sample?.groupTitle ?: "")
        val plot = buildSynopsis(targetTitle, genre, seasonsMap.size, filteredList.size)

        return SeriesDetails(
            seriesTitle = targetTitle,
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

    private fun extractSeasonFromText(text: String?): Int? {
        if (text.isNullOrBlank()) return null
        val match = Regex("""(?:Temporada|Season|Temp|T|S)\s*0*(\d+)""", RegexOption.IGNORE_CASE).find(text)
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

    /**
     * Faz o parsing completo da resposta get_series_info da API Xtream Codes,
     * montando os episódios com URLs de streaming reproduzíveis e metadados detalhados.
     */
    fun parseXtreamSeriesInfo(
        jsonString: String,
        seriesItem: ChannelEntity,
        cleanServer: String,
        username: String,
        password: String
    ): SeriesDetails {
        val root = try {
            NetworkClient.json.parseToJsonElement(jsonString).jsonObject
        } catch (_: Exception) {
            JsonObject(emptyMap())
        }

        val infoObj = root["info"] as? JsonObject
        val title = infoObj?.get("name")?.jsonPrimitive?.contentOrNull?.ifBlank { null }
            ?: cleanSeriesTitle(seriesItem.name)
        val plot = infoObj?.get("plot")?.jsonPrimitive?.contentOrNull?.ifBlank { null }
            ?: infoObj?.get("description")?.jsonPrimitive?.contentOrNull?.ifBlank { null }
            ?: "Acompanhe todos os episódios e temporadas de '$title'."
        val genre = infoObj?.get("genre")?.jsonPrimitive?.contentOrNull?.ifBlank { null }
            ?: detectGenre(title, seriesItem.groupTitle ?: "")
        val poster = infoObj?.get("cover")?.jsonPrimitive?.contentOrNull?.ifBlank { null }
            ?: seriesItem.logoUrl

        val seasonsMap = mutableMapOf<Int, MutableList<ParsedEpisode>>()
        val episodesElem = root["episodes"]

        if (episodesElem is JsonObject) {
            episodesElem.forEach { (seasonKey, seasonEpisodes) ->
                val seasonNum = seasonKey.toIntOrNull() ?: 1
                val epList = seasonsMap.getOrPut(seasonNum) { mutableListOf() }
                if (seasonEpisodes is JsonArray) {
                    seasonEpisodes.forEachIndexed { index, epElem ->
                        if (epElem is JsonObject) {
                            val epId = epElem["id"]?.jsonPrimitive?.contentOrNull?.ifBlank { null } ?: return@forEachIndexed
                            val epNum = epElem["episode_num"]?.jsonPrimitive?.intOrNull ?: (index + 1)
                            val epTitle = epElem["title"]?.jsonPrimitive?.contentOrNull?.ifBlank { "Episódio $epNum" } ?: "Episódio $epNum"
                            val ext = epElem["container_extension"]?.jsonPrimitive?.contentOrNull?.ifBlank { "mp4" } ?: "mp4"
                            val epInfo = epElem["info"] as? JsonObject
                            val epImage = epInfo?.get("movie_image")?.jsonPrimitive?.contentOrNull?.ifBlank { null } ?: poster

                            val epStreamUrl = "${cleanServer}series/$username/$password/$epId.$ext"
                            val epChannel = ChannelEntity(
                                id = "xtream_${seriesItem.playlistId}_ep_${epId}",
                                playlistId = seriesItem.playlistId,
                                name = "$title S${seasonNum.toString().padStart(2, '0')}E${epNum.toString().padStart(2, '0')} - $epTitle",
                                logoUrl = epImage,
                                groupTitle = title,
                                streamUrl = epStreamUrl,
                                tvgId = null,
                                contentType = ContentType.SERIES,
                                isFavorite = false
                            )
                            epList.add(
                                ParsedEpisode(
                                    channel = epChannel,
                                    seriesTitle = title,
                                    seasonNumber = seasonNum,
                                    episodeNumber = epNum,
                                    episodeTitle = epTitle
                                )
                            )
                        }
                    }
                }
            }
        } else if (episodesElem is JsonArray) {
            episodesElem.forEachIndexed { index, epElem ->
                if (epElem is JsonObject) {
                    val epId = epElem["id"]?.jsonPrimitive?.contentOrNull?.ifBlank { null } ?: return@forEachIndexed
                    val seasonNum = epElem["season"]?.jsonPrimitive?.intOrNull ?: 1
                    val epNum = epElem["episode_num"]?.jsonPrimitive?.intOrNull ?: (index + 1)
                    val epTitle = epElem["title"]?.jsonPrimitive?.contentOrNull?.ifBlank { "Episódio $epNum" } ?: "Episódio $epNum"
                    val ext = epElem["container_extension"]?.jsonPrimitive?.contentOrNull?.ifBlank { "mp4" } ?: "mp4"
                    val epInfo = epElem["info"] as? JsonObject
                    val epImage = epInfo?.get("movie_image")?.jsonPrimitive?.contentOrNull?.ifBlank { null } ?: poster

                    val epStreamUrl = "${cleanServer}series/$username/$password/$epId.$ext"
                    val epChannel = ChannelEntity(
                        id = "xtream_${seriesItem.playlistId}_ep_${epId}",
                        playlistId = seriesItem.playlistId,
                        name = "$title S${seasonNum.toString().padStart(2, '0')}E${epNum.toString().padStart(2, '0')} - $epTitle",
                        logoUrl = epImage,
                        groupTitle = title,
                        streamUrl = epStreamUrl,
                        tvgId = null,
                        contentType = ContentType.SERIES,
                        isFavorite = false
                    )
                    seasonsMap.getOrPut(seasonNum) { mutableListOf() }.add(
                        ParsedEpisode(
                            channel = epChannel,
                            seriesTitle = title,
                            seasonNumber = seasonNum,
                            episodeNumber = epNum,
                            episodeTitle = epTitle
                        )
                    )
                }
            }
        }

        val seasonGroups = seasonsMap.toSortedMap().map { (seasonNum, eps) ->
            SeriesSeasonGroup(
                seasonNumber = seasonNum,
                seasonName = "Temporada $seasonNum",
                episodes = eps.sortedBy { it.episodeNumber }
            )
        }

        return SeriesDetails(
            seriesTitle = title,
            genre = genre,
            plot = plot,
            posterUrl = poster,
            seasons = seasonGroups.ifEmpty {
                listOf(
                    SeriesSeasonGroup(
                        seasonNumber = 1,
                        seasonName = "Temporada 1",
                        episodes = listOf(
                            ParsedEpisode(
                                channel = seriesItem,
                                seriesTitle = title,
                                seasonNumber = 1,
                                episodeNumber = 1,
                                episodeTitle = seriesItem.name
                            )
                        )
                    )
                )
            }
        )
    }
}
