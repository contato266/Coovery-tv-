package com.streamvault.data.util

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.Movie
import com.streamvault.domain.model.VodDuplicateHandlingMode
import com.streamvault.domain.model.VodVariantPreferenceMode
import org.junit.Test

class VodUltraHdMoviePolicyTest {

    @Test
    fun `detects ultra hd movie from title tokens`() {
        val movie = movie(name = "Galaxy Quest 4K HDR")
        assertThat(isUltraHighDefinitionMovie(movie)).isTrue()
    }

    @Test
    fun `detects ultra hd movie category shelves`() {
        assertThat(isUltraHighDefinitionMovieCategoryName("Filmes 4K VOD")).isTrue()
        assertThat(isUltraHighDefinitionMovieCategoryName("Action")).isFalse()
    }

    @Test
    fun `buildPresentedMovies drops ultra hd only entries`() {
        val only4k = movie(id = 1L, name = "Only Ultra 2160p")
        val hd = movie(id = 2L, name = "Only Standard 1080p")

        val presented = filterPresentableMovies(listOf(only4k, hd))

        assertThat(presented.map { it.id }).containsExactly(2L)
    }

    @Test
    fun `variant labels omit ultra hd quality tags`() {
        val grouped = buildPresentedMovies(
            movies = listOf(
                movie(id = 1L, name = "Star Voyage 1080p", year = "2024"),
                movie(id = 2L, name = "Star Voyage 720p", year = "2024")
            ),
            settings = MoviePresentationSettings(
                duplicateHandlingMode = VodDuplicateHandlingMode.GROUPED,
                preferenceMode = VodVariantPreferenceMode.BALANCED
            )
        )

        assertThat(grouped.single().variantLabel).doesNotContain("4K")
    }

    private fun movie(
        id: Long,
        name: String,
        year: String? = null
    ): Movie = Movie(
        id = id,
        streamId = id,
        name = name,
        year = year,
        streamUrl = "http://example.test/movie/$id.mp4",
        providerId = 7L,
        containerExtension = "mp4",
        addedAt = id
    )
}
