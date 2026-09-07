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
    val seriesContextLoaded: Boolean
)

internal suspend fun PlayerViewModel.resolveSeriesEpisodeContinuation(): SeriesEpisodeContinuation {
    val initial = resolveNextEpisodeForAutoPlay(
        nextEpisode = nextEpisode.value,
        currentSeries = currentSeries.value,
        currentEpisode = currentEpisode.value,
        currentSeasonNumber = currentSeasonNumber,
        currentEpisodeNumber = currentEpisodeNumber
    )
    if (initial != null) {
        return SeriesEpisodeContinuation(nextEpisode = initial, seriesContextLoaded = true)
    }

    val providerId = currentProviderId
    val seriesId = currentSeriesId
    if (providerId <= 0 || seriesId == null) {
        return SeriesEpisodeContinuation(nextEpisode = null, seriesContextLoaded = false)
    }

    loadSeriesEpisodeContext(
        requestVersion = prepareRequestVersion,
        providerId = providerId,
        seriesId = seriesId,
        episodeId = currentStableEpisodeId?.takeIf { it > 0 } ?: currentContentId,
        seasonNumber = currentSeasonNumber,
        episodeNumber = currentEpisodeNumber
    )

    val seriesContextLoaded = currentSeries.value != null
    val resolvedNext = resolveNextEpisodeForAutoPlay(
        nextEpisode = nextEpisode.value,
        currentSeries = currentSeries.value,
        currentEpisode = currentEpisode.value,
        currentSeasonNumber = currentSeasonNumber,
        currentEpisodeNumber = currentEpisodeNumber
    )
    return SeriesEpisodeContinuation(
        nextEpisode = resolvedNext,
        seriesContextLoaded = seriesContextLoaded
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
                playEpisode(continuation.nextEpisode, showResumePrompt = false)
            }
            continuation.nextEpisode == null && continuation.seriesContextLoaded -> {
                requestSeriesPlaybackExit()
            }
        }
    }
}

internal fun PlayerViewModel.requestSeriesPlaybackExit() {
    _seriesPlaybackExitEvent.value += 1
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
