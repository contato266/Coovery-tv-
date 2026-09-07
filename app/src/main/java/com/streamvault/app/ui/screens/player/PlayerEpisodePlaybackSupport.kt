package com.streamvault.app.ui.screens.player

import com.streamvault.domain.model.Episode
import com.streamvault.domain.model.Series

internal fun resolveEpisode(
    series: Series,
    episodeId: Long,
    seasonNumber: Int?,
    episodeNumber: Int?
): Episode? {
    val episodes = series.seasons
        .sanitizedForPlayer()
        .sortedBy { it.seasonNumber }
        .flatMap { season -> season.episodes.sortedBy { it.episodeNumber } }
    return episodes.firstOrNull {
        it.id == episodeId || it.matchesPlaybackEpisode(episodeId, seasonNumber, episodeNumber)
    } ?: episodes.firstOrNull {
        it.seasonNumber == seasonNumber && it.episodeNumber == episodeNumber
    }
}

internal fun findNextEpisode(series: Series, episode: Episode): Episode? {
    val orderedEpisodes = series.seasons
        .sanitizedForPlayer()
        .sortedBy { it.seasonNumber }
        .flatMap { season -> season.episodes.sortedBy { it.episodeNumber } }
    val currentIndex = orderedEpisodes.indexOfFirst {
        it.id == episode.id ||
            it.playbackEpisodeIdentity() == episode.playbackEpisodeIdentity() ||
            (it.seasonNumber == episode.seasonNumber && it.episodeNumber == episode.episodeNumber)
    }
    if (currentIndex < 0) {
        return findNextEpisodeByNumbers(
            series = series,
            seasonNumber = episode.seasonNumber,
            episodeNumber = episode.episodeNumber
        )
    }
    return orderedEpisodes.getOrNull(currentIndex + 1)
}

internal fun findNextEpisodeByNumbers(
    series: Series,
    seasonNumber: Int?,
    episodeNumber: Int?
): Episode? {
    if (seasonNumber == null || episodeNumber == null) return null
    val orderedEpisodes = series.seasons
        .sanitizedForPlayer()
        .sortedBy { it.seasonNumber }
        .flatMap { season -> season.episodes.sortedBy { it.episodeNumber } }
    val currentIndex = orderedEpisodes.indexOfFirst {
        it.seasonNumber == seasonNumber && it.episodeNumber == episodeNumber
    }
    if (currentIndex < 0) return null
    return orderedEpisodes.getOrNull(currentIndex + 1)
}

internal fun canConfirmSeriesEpisodeIsLast(
    series: Series?,
    currentEpisode: Episode?,
    seasonNumber: Int?,
    episodeNumber: Int?
): Boolean {
    val resolvedSeries = series ?: return false
    val hasEpisodeCatalog = resolvedSeries.seasons.any { it.episodes.isNotEmpty() }
    if (!hasEpisodeCatalog) return false
    val resolvedCurrentEpisode = currentEpisode ?: resolveEpisode(
        series = resolvedSeries,
        episodeId = -1L,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber
    ) ?: return false
    return findNextEpisode(resolvedSeries, resolvedCurrentEpisode) == null
}

internal fun Episode.playbackEpisodeIdentity(): Long =
    episodeId.takeIf { it > 0L } ?: id

internal fun Episode.matchesPlaybackEpisode(
    requestedEpisodeId: Long,
    requestedSeasonNumber: Int?,
    requestedEpisodeNumber: Int?
): Boolean {
    val identity = playbackEpisodeIdentity()
    return (requestedEpisodeId > 0L && identity == requestedEpisodeId) ||
        (requestedSeasonNumber != null &&
            requestedEpisodeNumber != null &&
            seasonNumber == requestedSeasonNumber &&
            episodeNumber == requestedEpisodeNumber)
}

internal fun buildEpisodePlaybackTitle(episode: Episode): String =
    "${episode.title} - S${episode.seasonNumber}E${episode.episodeNumber}"