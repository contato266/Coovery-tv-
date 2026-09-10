package com.streamvault.app.ui.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.streamvault.app.device.rememberIsTelevisionDevice

@Composable
fun rememberAppSpacing(): AppSpacing {
    val isTelevisionDevice = rememberIsTelevisionDevice()
    if (isTelevisionDevice) {
        return remember { AppSpacing() }
    }
    val configuration = LocalConfiguration.current
    val portrait = configuration.screenHeightDp > configuration.screenWidthDp
    return remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        if (portrait) {
            AppSpacing(
                screenGutter = 16.dp,
                railWidth = 124.dp,
                sectionGap = 20.dp,
                cardGap = 12.dp,
                chipGap = 8.dp,
                safeTop = 8.dp,
                safeBottom = 8.dp,
                safeHoriz = 16.dp
            )
        } else {
            AppSpacing(
                screenGutter = 24.dp,
                railWidth = 124.dp,
                sectionGap = 24.dp,
                cardGap = 14.dp,
                chipGap = 8.dp,
                safeTop = 12.dp,
                safeBottom = 12.dp,
                safeHoriz = 24.dp
            )
        }
    }
}
