package com.streamvault.data.util

import com.streamvault.domain.model.Movie

private const val ULTRA_HD_MOVIE_QUALITY_THRESHOLD = 2160

private val ULTRA_HD_MOVIE_CATEGORY_REGEX = Regex(
    """\b(8k|4320p?|uhd|ultra\s*hd|4k|2160p?)\b""",
    RegexOption.IGNORE_CASE
)

fun isUltraHighDefinitionMovieCategoryName(categoryName: String): Boolean =
    ULTRA_HD_MOVIE_CATEGORY_REGEX.containsMatchIn(categoryName)

fun isUltraHighDefinitionMovieSignal(
    name: String,
    categoryName: String? = null,
    streamUrl: String? = null,
    containerExtension: String? = null
): Boolean {
    if (categoryName?.let(::isUltraHighDefinitionMovieCategoryName) == true) {
        return true
    }
    val searchable = listOfNotNull(name, categoryName, streamUrl, containerExtension)
        .filter { it.isNotBlank() }
        .joinToString(" ")
    return movieQualityScore(searchable) >= ULTRA_HD_MOVIE_QUALITY_THRESHOLD
}

fun isUltraHighDefinitionMovie(movie: Movie): Boolean = isUltraHighDefinitionMovieSignal(
    name = movie.name,
    categoryName = movie.categoryName,
    streamUrl = movie.streamUrl,
    containerExtension = movie.containerExtension
)

fun filterPresentableMovies(movies: List<Movie>): List<Movie> =
    movies.filterNot(::isUltraHighDefinitionMovie)
