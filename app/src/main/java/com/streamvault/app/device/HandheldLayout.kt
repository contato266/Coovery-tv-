package com.streamvault.app.device

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

/**
 * True on phones/tablets in portrait or on narrow widths. Android TV always returns false so TV
 * layouts stay unchanged.
 */
@Composable
fun rememberUseHandheldStackedLayout(): Boolean {
    val isTelevisionDevice = rememberIsTelevisionDevice()
    if (isTelevisionDevice) return false
    val configuration = LocalConfiguration.current
    return remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        configuration.screenWidthDp < 900 ||
            configuration.screenHeightDp > configuration.screenWidthDp
    }
}

@Composable
fun rememberIsHandheldDevice(): Boolean = !rememberIsTelevisionDevice()

fun ComponentActivity.applyPlatformScreenOrientation() {
    if (isTelevisionDevice()) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    } else {
        applyHandheldBrowseOrientation()
    }
}

/** Portrait for app browsing on phones; no-op on TV. */
fun ComponentActivity.applyHandheldBrowseOrientation() {
    if (!isTelevisionDevice()) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }
}

/** Landscape while watching movies, series, or live channels on phones; no-op on TV. */
fun ComponentActivity.applyHandheldPlayerOrientation() {
    if (!isTelevisionDevice()) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }
}
