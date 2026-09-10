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
    requestedOrientation = if (isTelevisionDevice()) {
        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    } else {
        ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }
}
