package com.streamvault.app.device

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Matches [MovieCard] / [CategoryRow] poster size on handheld Filmes browse. */
val HandheldVodPosterWidth: Dp = 136.dp
val HandheldVodPosterHeight: Dp = 204.dp

const val HANDHELD_SERIES_BROWSE_RESET_KEY = "handheld_series_browse_reset_token"

/**
 * Portrait phone VOD shelves (Filmes tab reference layout). Android TV and wide layouts return false.
 */
@Composable
fun rememberHandheldVodPortraitBrowse(): Boolean {
    val isTelevisionDevice = rememberIsTelevisionDevice()
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    return remember(isTelevisionDevice, screenWidthDp) {
        !isTelevisionDevice && screenWidthDp < 700
    }
}
