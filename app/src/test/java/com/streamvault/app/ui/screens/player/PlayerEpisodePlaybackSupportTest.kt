package com.streamvault.app.ui.screens.player

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.Episode
import com.streamvault.domain.model.Season
import com.streamvault.domain.model.Series
import org.junit.Test

class PlayerEpisodePlaybackSupportTest {

    private val series = Series(
        id = 1L,
        providerId = 10L,
        name = "Test Series",
        seasons = listOf(
            Season(
                seasonNumber = 1,
                episodes = listOf(
                    episode(id = 101L, season = 1, number = 1),
                    episode(id = 102L, season = 1, number = 2)
                )
            ),
            Season(
                seasonNumber = 2,
                episodes = listOf(
                    episode(id = 201L, season = 2, number = 1)
                )
            )
        )
    )

    @Test
    fun `find next episode by numbers resolves cross season`() {
        val next = findNextEpisodeByNumbers(series, seasonNumber = 1, episodeNumber = 2)
        assertThat(next?.id).isEqualTo(201L)
    }

    @Test
    fun `can confirm last episode only when catalog and current episode are known`() {
        val middleEpisode = episode(id = 102L, season = 1, number = 2)
        assertThat(
            canConfirmSeriesEpisodeIsLast(
                series = series,
                currentEpisode = middleEpisode,
                seasonNumber = 1,
                episodeNumber = 2
            )
        ).isFalse()
        assertThat(
            canConfirmSeriesEpisodeIsLast(
                series = series,
                currentEpisode = episode(id = 201L, season = 2, number = 1),
                seasonNumber = 2,
                episodeNumber = 1
            )
        ).isTrue()
        assertThat(
            canConfirmSeriesEpisodeIsLast(
                series = null,
                currentEpisode = middleEpisode,
                seasonNumber = 1,
                episodeNumber = 2
            )
        ).isFalse()
    }

    @Test
    fun `find next episode from ordered list resolves by season and episode numbers`() {
        val episodes = listOf(
            episode(id = 101L, season = 1, number = 1),
            episode(id = 102L, season = 1, number = 2),
            episode(id = 201L, season = 2, number = 1)
        )

        val next = findNextEpisodeFromOrderedList(
            episodes = episodes,
            currentEpisode = null,
            seasonNumber = 1,
            episodeNumber = 1,
            contentId = -1L,
            stableEpisodeId = null
        )

        assertThat(next?.id).isEqualTo(102L)
    }

    @Test
    fun `find next episode from ordered list resolves by content id`() {
        val episodes = listOf(
            episode(id = 101L, season = 1, number = 1),
            episode(id = 102L, season = 1, number = 2)
        )

        val next = findNextEpisodeFromOrderedList(
            episodes = episodes,
            currentEpisode = null,
            seasonNumber = null,
            episodeNumber = null,
            contentId = 101L,
            stableEpisodeId = null
        )

        assertThat(next?.id).isEqualTo(102L)
    }

    @Test
    fun `can confirm last episode uses persisted episode catalog`() {
        val persistedEpisodes = listOf(
            episode(id = 101L, season = 1, number = 1),
            episode(id = 102L, season = 1, number = 2)
        )

        assertThat(
            canConfirmSeriesEpisodeIsLast(
                series = null,
                currentEpisode = episode(id = 101L, season = 1, number = 1),
                seasonNumber = 1,
                episodeNumber = 1,
                persistedEpisodes = persistedEpisodes,
                contentId = 101L
            )
        ).isFalse()
        assertThat(
            canConfirmSeriesEpisodeIsLast(
                series = null,
                currentEpisode = episode(id = 102L, season = 1, number = 2),
                seasonNumber = 1,
                episodeNumber = 2,
                persistedEpisodes = persistedEpisodes,
                contentId = 102L
            )
        ).isTrue()
    }

    private fun episode(id: Long, season: Int, number: Int): Episode = Episode(
        id = id,
        episodeId = id,
        seriesId = 1L,
        providerId = 10L,
        title = "Episode $number",
        streamUrl = "http://example.com/$id.mp4",
        seasonNumber = season,
        episodeNumber = number
    )
}
