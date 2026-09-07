package com.streamvault.app.ui.screens.player

import androidx.lifecycle.viewModelScope
import com.streamvault.domain.model.ContentType
import com.streamvault.domain.model.Episode
import com.streamvault.domain.model.Series
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val AUTO_PLAY_MIN_WATCHED_MS = 5_000L
private const val AUTO_PLAY_COUNTDOWN_SECONDS = 10

internal suspend fun PlayerViewModel.persistPlaybackCompletion() {
    val durationMs = playerEngine.duration.value
    val completedHistory = buildPlaybackHistorySnapshot(
        positionMs = durationMs.coerceAtLeast(playerEngine.currentPosition.value),
        durationMs = durationMs
    ) ?: return
    val result = playbackHistoryCoordinator.markAsWatched(completedHistory)
    logRepositoryFailure(
        operation = "Mark playback watched",
        result = result
    )
    if (result.isSuccess) {
        playbackHistoryCoordinator.refreshPlaybackSurfaces()
    }
}

internal fun resolveNextEpisodeForAutoPlay(
    nextEpisode: Episode?,
    currentSeries: Series?,
    currentEpisode: Episode?,
    currentSeasonNumber: Int? = null,
    currentEpisodeNumber: Int? = null
): Episode? {
    nextEpisode?.let { return it }
    val series = currentSeries ?: return null
    val episode = currentEpisode ?: resolveEpisode(
        series = series,
        episodeId = -1L,
        seasonNumber = currentSeasonNumber,
        episodeNumber = currentEpisodeNumber
    ) ?: return null
    return findNextEpisode(series, episode)
}

internal data class SeriesEpisodeContinuation(
    val nextEpisode: Episode?,
    val shouldReturnToSeriesScreen: Boolean
)

internal suspend fun PlayerViewModel.resolveSeriesEpisodeContinuation(): SeriesEpisodeContinuation {
    fun resolveFromLoadedCatalog(): Episode? = resolveNextEpisodeForAutoPlay(
        nextEpisode = nextEpisode.value,
        currentSeries = currentSeries.value,
        currentEpisode = currentEpisode.value,
        currentSeasonNumber = currentSeasonNumber,
        currentEpisodeNumber = currentEpisodeNumber
    )

    val initial = resolveFromLoadedCatalog()
    if (initial != null) {
        return SeriesEpisodeContinuation(
            nextEpisode = initial,
            shouldReturnToSeriesScreen = false
        )
    }

    val providerId = currentProviderId
    val seriesId = currentSeriesId
    if (providerId > 0 && seriesId != null) {
        refreshSeriesEpisodeContext(
            providerId = providerId,
            seriesId = seriesId,
            episodeId = currentStableEpisodeId?.takeIf { it > 0 } ?: currentContentId,
            seasonNumber = currentSeasonNumber,
            episodeNumber = currentEpisodeNumber
        )
    }

    val afterRefresh = resolveFromLoadedCatalog()
    if (afterRefresh != null) {
        return SeriesEpisodeContinuation(
            nextEpisode = afterRefresh,
            shouldReturnToSeriesScreen = false
        )
    }

    val persistedEpisodes = seriesId
        ?.takeIf { it > 0L }
        ?.let { playerContentResolver.getEpisodesForSeries(it) }
        .orEmpty()
    val fromDatabase = findNextEpisodeFromOrderedList(
        episodes = persistedEpisodes,
        currentEpisode = currentEpisode.value,
        seasonNumber = currentSeasonNumber,
        episodeNumber = currentEpisodeNumber,
        contentId = currentContentId,
        stableEpisodeId = currentStableEpisodeId
    )
    if (fromDatabase != null) {
        return SeriesEpisodeContinuation(
            nextEpisode = fromDatabase,
            shouldReturnToSeriesScreen = false
        )
    }

    return SeriesEpisodeContinuation(
        nextEpisode = null,
        shouldReturnToSeriesScreen = canConfirmSeriesEpisodeIsLast(
            series = currentSeries.value,
            currentEpisode = currentEpisode.value,
            seasonNumber = currentSeasonNumber,
            episodeNumber = currentEpisodeNumber,
            persistedEpisodes = persistedEpisodes,
            contentId = currentContentId,
            stableEpisodeId = currentStableEpisodeId
        )
    )
}

internal suspend fun PlayerViewModel.playNextSeriesEpisode(episode: Episode) {
    val providerId = episode.providerId.takeIf { it > 0 } ?: currentProviderId
    val seriesId = episode.seriesId.takeIf { it > 0 } ?: currentSeriesId
    if (providerId > 0 && seriesId != null) {
        refreshSeriesEpisodeContext(
            providerId = providerId,
            seriesId = seriesId,
            episodeId = episode.episodeId.takeIf { it > 0 } ?: episode.id,
            seasonNumber = episode.seasonNumber,
            episodeNumber = episode.episodeNumber
        )
    }

    primeSeriesEpisodePlayback(episode)

    playEpisode(
        episode = episode,
        showResumePrompt = false,
        showEntryOverlay = false
    )
}

internal fun shouldHandleSeriesEpisodeEnded(
    positionMs: Long,
    durationMs: Long
): Boolean = positionMs > AUTO_PLAY_MIN_WATCHED_MS || durationMs > 0L

internal fun PlayerViewModel.handlePlaybackEnded() {
    if (currentContentType == ContentType.LIVE) return
    viewModelScope.launch {
        persistPlaybackCompletion()
        if (currentContentType != ContentType.SERIES_EPISODE) return@launch
        if (!shouldHandleSeriesEpisodeEnded(
                positionMs = playerEngine.currentPosition.value,
                durationMs = playerEngine.duration.value
            )
        ) {
            return@launch
        }

        val continuation = resolveSeriesEpisodeContinuation()
        when {
            continuation.nextEpisode != null && autoPlayNextEpisodeEnabled -> {
                requestSeriesEpisodeContinuation(continuation.nextEpisode)
            }
            continuation.shouldReturnToSeriesScreen -> {
                requestSeriesPlaybackExit()
            }
        }
    }
}

internal fun PlayerViewModel.requestSeriesEpisodeContinuation(episode: Episode) {
    _seriesEpisodeContinueEvent.value = episode
}

internal fun PlayerViewModel.requestSeriesPlaybackExit() {
    _seriesPlaybackExitEvent.value += 1
}

fun PlayerViewModel.consumeSeriesEpisodeContinueEvent() {
    _seriesEpisodeContinueEvent.value = null
}

fun PlayerViewModel.consumeSeriesPlaybackExitEvent() {
    _seriesPlaybackExitEvent.value = 0
}

internal fun PlayerViewModel.startAutoPlayCountdown(episode: Episode) {
    autoPlayCountdownJob?.cancel()
    autoPlayCountdownJob = playbackSessionScope()?.launch {
        for (remaining in AUTO_PLAY_COUNTDOWN_SECONDS downTo 1) {
            _autoPlayCountdown.value = AutoPlayCountdownUiState(
                episode = episode,
                secondsRemaining = remaining
            )
            delay(1_000L)
        }
        _autoPlayCountdown.value = null
        playEpisode(episode, showResumePrompt = false)
    }
}

fun PlayerViewModel.cancelAutoPlay() {
    autoPlayCountdownJob?.cancel()
    autoPlayCountdownJob = null
    _autoPlayCountdown.value = null
}

fun PlayerViewModel.playNextEpisodeNow() {
    val episode = autoPlayCountdown.value?.episode ?: nextEpisode.value ?: return
    cancelAutoPlay()
    playEpisode(episode, showResumePrompt = false)
}
