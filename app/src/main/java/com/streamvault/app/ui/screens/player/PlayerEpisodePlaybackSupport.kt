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

internal fun findNextEpisodeFromOrderedList(
    episodes: List<Episode>,
    currentEpisode: Episode?,
    seasonNumber: Int?,
    episodeNumber: Int?,
    contentId: Long,
    stableEpisodeId: Long?
): Episode? {
    if (episodes.isEmpty()) return null
    val orderedEpisodes = episodes.sortedWith(compareBy({ it.seasonNumber }, { it.episodeNumber }))
    val currentIndex = when {
        currentEpisode != null -> orderedEpisodes.indexOfFirst { candidate ->
            candidate.id == currentEpisode.id ||
                candidate.playbackEpisodeIdentity() == currentEpisode.playbackEpisodeIdentity() ||
                (candidate.seasonNumber == currentEpisode.seasonNumber &&
                    candidate.episodeNumber == currentEpisode.episodeNumber)
        }
        seasonNumber != null && episodeNumber != null -> orderedEpisodes.indexOfFirst {
            it.seasonNumber == seasonNumber && it.episodeNumber == episodeNumber
        }
        contentId > 0L -> orderedEpisodes.indexOfFirst { candidate ->
            candidate.id == contentId ||
                candidate.playbackEpisodeIdentity() == contentId ||
                (stableEpisodeId != null && candidate.playbackEpisodeIdentity() == stableEpisodeId)
        }
        else -> -1
    }
    if (currentIndex < 0) return null
    return orderedEpisodes.getOrNull(currentIndex + 1)
}

internal fun canConfirmSeriesEpisodeIsLast(
    series: Series?,
    currentEpisode: Episode?,
    seasonNumber: Int?,
    episodeNumber: Int?,
    persistedEpisodes: List<Episode> = emptyList(),
    contentId: Long = -1L,
    stableEpisodeId: Long? = null
): Boolean {
    if (persistedEpisodes.isEmpty()) return false
    val catalogEpisodes = persistedEpisodes.sortedWith(compareBy({ it.seasonNumber }, { it.episodeNumber }))
    val currentIndex = findCurrentEpisodeIndex(
        episodes = catalogEpisodes,
        currentEpisode = currentEpisode,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        contentId = contentId,
        stableEpisodeId = stableEpisodeId
    )
    if (currentIndex < 0) return false
    return currentIndex == catalogEpisodes.lastIndex
}

private fun findCurrentEpisodeIndex(
    episodes: List<Episode>,
    currentEpisode: Episode?,
    seasonNumber: Int?,
    episodeNumber: Int?,
    contentId: Long,
    stableEpisodeId: Long?
): Int {
    if (episodes.isEmpty()) return -1
    val orderedEpisodes = episodes.sortedWith(compareBy({ it.seasonNumber }, { it.episodeNumber }))
    return when {
        currentEpisode != null -> orderedEpisodes.indexOfFirst { candidate ->
            candidate.id == currentEpisode.id ||
                candidate.playbackEpisodeIdentity() == currentEpisode.playbackEpisodeIdentity() ||
                (candidate.seasonNumber == currentEpisode.seasonNumber &&
                    candidate.episodeNumber == currentEpisode.episodeNumber)
        }
        seasonNumber != null && episodeNumber != null -> orderedEpisodes.indexOfFirst {
            it.seasonNumber == seasonNumber && it.episodeNumber == episodeNumber
        }
        contentId > 0L -> orderedEpisodes.indexOfFirst { candidate ->
            candidate.id == contentId ||
                candidate.playbackEpisodeIdentity() == contentId ||
                (stableEpisodeId != null && candidate.playbackEpisodeIdentity() == stableEpisodeId)
        }
        else -> -1
    }
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