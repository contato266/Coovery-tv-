package com.streamvault.app.ui.screens.player

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.Episode
import com.streamvault.domain.model.Season
import com.streamvault.domain.model.Series
import org.junit.Test

class PlayerAutoPlayActionsTest {

    private val series = Series(
        id = 1L,
        providerId = 10L,
        name = "Test Series",
        posterUrl = null,
        backdropUrl = null,
        seasons = listOf(
            Season(
                seasonNumber = 1,
                episodes = listOf(
                    episode(id = 101L, season = 1, number = 1, title = "Pilot"),
                    episode(id = 102L, season = 1, number = 2, title = "Second"),
                    episode(id = 201L, season = 2, number = 1, title = "Season 2 Premiere")
                )
            )
        )
    )

    @Test
    fun `resolve next episode prefers cached next episode`() {
        val cachedNext = episode(id = 999L, season = 9, number = 9, title = "Cached")
        val current = episode(id = 101L, season = 1, number = 1, title = "Pilot")

        val resolved = resolveNextEpisodeForAutoPlay(
            nextEpisode = cachedNext,
            currentSeries = series,
            currentEpisode = current
        )

        assertThat(resolved).isEqualTo(cachedNext)
    }

    @Test
    fun `resolve next episode falls back to series ordering`() {
        val current = episode(id = 102L, season = 1, number = 2, title = "Second")

        val resolved = resolveNextEpisodeForAutoPlay(
            nextEpisode = null,
            currentSeries = series,
            currentEpisode = current
        )

        assertThat(resolved?.id).isEqualTo(201L)
        assertThat(resolved?.seasonNumber).isEqualTo(2)
        assertThat(resolved?.episodeNumber).isEqualTo(1)
    }

    @Test
    fun `resolve next episode returns null for final episode`() {
        val current = episode(id = 201L, season = 2, number = 1, title = "Season 2 Premiere")

        val resolved = resolveNextEpisodeForAutoPlay(
            nextEpisode = null,
            currentSeries = series,
            currentEpisode = current
        )

        assertThat(resolved).isNull()
    }

    @Test
    fun `resolve next episode falls back to season and episode numbers`() {
        val resolved = resolveNextEpisodeForAutoPlay(
            nextEpisode = null,
            currentSeries = series,
            currentEpisode = null,
            currentSeasonNumber = 1,
            currentEpisodeNumber = 1
        )

        assertThat(resolved?.id).isEqualTo(102L)
        assertThat(resolved?.seasonNumber).isEqualTo(1)
        assertThat(resolved?.episodeNumber).isEqualTo(2)
    }

    @Test
    fun `series episode ended handling requires meaningful watch progress`() {
        assertThat(shouldHandleSeriesEpisodeEnded(positionMs = 6_000L, durationMs = 0L)).isTrue()
        assertThat(shouldHandleSeriesEpisodeEnded(positionMs = 0L, durationMs = 3_600_000L)).isTrue()
        assertThat(shouldHandleSeriesEpisodeEnded(positionMs = 0L, durationMs = 0L)).isFalse()
    }

    private fun episode(id: Long, season: Int, number: Int, title: String): Episode = Episode(
        id = id,
        episodeId = id,
        seriesId = 1L,
        providerId = 10L,
        title = title,
        streamUrl = "http://example.com/$id.mp4",
        coverUrl = null,
        seasonNumber = season,
        episodeNumber = number
    )
}
