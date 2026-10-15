package com.iptvplayer.app.feature.vod

import com.iptvplayer.app.core.database.entity.ChannelEntity

data class MovieDetails(
    val cleanTitle: String,
    val releaseYear: String,
    val genre: String,
    val plot: String,
    val posterUrl: String?,
    val rating: String = "8.4"
)

object MovieMetadataHelper {

    private val YEAR_REGEX = Regex("""\b(19\d{2}|20\d{2})\b""")
    private val TAGS_REGEX = Regex("""\[.*?\]|\(.*?\)|4K|FHD|HD|SD|DUBLADO|LEG|LEGENDADO|H264|H265|HEVC""", RegexOption.IGNORE_CASE)

    fun extract(movie: ChannelEntity): MovieDetails {
        val rawName = movie.name
        val group = movie.groupTitle ?: ""

        // Extrai o ano
        val yearMatch = YEAR_REGEX.find(rawName)
        val releaseYear = yearMatch?.value ?: "2024"

        // Extrai o gênero / estilo
        val genre = detectGenre(rawName, group)

        // Limpa o título
        val cleanTitle = rawName
            .replace(YEAR_REGEX, "")
            .replace(TAGS_REGEX, "")
            .replace(Regex("""^\s*[-–—:|]+\s*"""), "")
            .replace(Regex("""\s*[-–—:|]+\s*$"""), "")
            .trim()
            .ifBlank { rawName }

        // Sinopse / Biografia detalhada
        val plot = buildSynopsis(cleanTitle, releaseYear, genre)

        return MovieDetails(
            cleanTitle = cleanTitle,
            releaseYear = releaseYear,
            genre = genre,
            plot = plot,
            posterUrl = movie.logoUrl
        )
    }

    private fun detectGenre(name: String, group: String): String {
        val combined = "$name $group".lowercase()
        return when {
            combined.contains("terror") || combined.contains("horror") -> "Terror & Suspense"
            combined.contains("ação") || combined.contains("acao") || combined.contains("action") -> "Ação & Aventura"
            combined.contains("comédia") || combined.contains("comedia") || combined.contains("comedy") -> "Comédia"
            combined.contains("ficção") || combined.contains("ficcao") || combined.contains("sci-fi") || combined.contains("scifi") -> "Ficção Científica"
            combined.contains("animação") || combined.contains("animacao") || combined.contains("desenho") || combined.contains("kids") || combined.contains("infantil") -> "Animação & Família"
            combined.contains("suspense") || combined.contains("thriller") -> "Suspense & Mistério"
            combined.contains("drama") -> "Drama"
            combined.contains("romance") -> "Romance"
            combined.contains("documentário") || combined.contains("documentario") || combined.contains("doc") -> "Documentário"
            combined.contains("policial") || combined.contains("crime") -> "Policial & Crime"
            combined.contains("fantasia") || combined.contains("fantasy") -> "Fantasia & Magia"
            combined.contains("guerra") || combined.contains("war") -> "Guerra & História"
            group.isNotBlank() && !group.contains("geral", ignoreCase = true) -> group.replace(Regex("""^(?:FILMES|VOD)\s*\|\s*""", RegexOption.IGNORE_CASE), "").trim()
            else -> "Cinema & Entretenimento"
        }
    }

    private fun buildSynopsis(title: String, year: String, genre: String): String {
        return "Em '$title' ($year), prepare-se para uma experiência cinematográfica imperdível no estilo $genre. O enredo combina ritmo eletrizante, personagens memoráveis e reviravoltas surpreendentes que cativam do início ao fim."
    }
}
