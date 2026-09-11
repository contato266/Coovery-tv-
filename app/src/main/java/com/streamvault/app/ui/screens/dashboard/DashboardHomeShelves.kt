package com.streamvault.app.ui.screens.dashboard

import com.streamvault.domain.model.AppHomeDashboardShelf

internal fun resolveVisibleDashboardShelves(
    uiState: DashboardUiState,
    handheldPortraitHome: Boolean = false
): List<AppHomeDashboardShelf> {
    val visible = AppHomeDashboardShelf
        .normalizeForStorage(uiState.homeDashboardShelves)
        .filter(uiState::hasContentFor)
    return if (handheldPortraitHome) {
        reorderHandheldRecentShelvesAboveChannels(visible)
    } else {
        visible
    }
}

/** Mobile home: recent movies & series shelves sit directly above recent channels. */
internal fun reorderHandheldRecentShelvesAboveChannels(
    shelves: List<AppHomeDashboardShelf>
): List<AppHomeDashboardShelf> {
    val movable = setOf(
        AppHomeDashboardShelf.RECENT_MOVIES,
        AppHomeDashboardShelf.RECENT_SERIES,
        AppHomeDashboardShelf.RECENT_CHANNELS
    )
    val movies = shelves.filter { it == AppHomeDashboardShelf.RECENT_MOVIES }
    val series = shelves.filter { it == AppHomeDashboardShelf.RECENT_SERIES }
    val channels = shelves.filter { it == AppHomeDashboardShelf.RECENT_CHANNELS }
    if (movies.isEmpty() && series.isEmpty()) return shelves
    val channelIndex = shelves.indexOfFirst { it == AppHomeDashboardShelf.RECENT_CHANNELS }
    if (channelIndex < 0) {
        val without = shelves.filter { it !in movable }
        return without + movies + series
    }
    val before = shelves.take(channelIndex).filter { it !in movable }
    val after = shelves.drop(channelIndex + 1).filter { it !in movable }
    return before + movies + series + channels + after
}

internal fun DashboardUiState.hasContentFor(shelf: AppHomeDashboardShelf): Boolean = when (shelf) {
    AppHomeDashboardShelf.FAVORITE_CHANNELS -> favoriteChannels.isNotEmpty()
    AppHomeDashboardShelf.RECENT_CHANNELS -> recentChannels.isNotEmpty()
    AppHomeDashboardShelf.LIVE_SHORTCUTS -> liveShortcuts.isNotEmpty()
    AppHomeDashboardShelf.CONTINUE_WATCHING -> continueWatching.isNotEmpty()
    AppHomeDashboardShelf.RECENT_MOVIES -> recentMovies.isNotEmpty()
    AppHomeDashboardShelf.RECENT_SERIES -> recentSeries.isNotEmpty()
    AppHomeDashboardShelf.FAVORITE_MOVIES -> favoriteMovies.isNotEmpty()
    AppHomeDashboardShelf.FAVORITE_SERIES -> favoriteSeries.isNotEmpty()
    AppHomeDashboardShelf.CONTINUE_WATCHING_MOVIES -> continueWatchingMovies.isNotEmpty()
    AppHomeDashboardShelf.CONTINUE_WATCHING_SERIES -> continueWatchingSeriesItems.isNotEmpty()
    AppHomeDashboardShelf.TOP_RATED_MOVIES -> topRatedMovies.isNotEmpty()
    AppHomeDashboardShelf.RECOMMENDED_MOVIES -> recommendedMovies.isNotEmpty()
}
