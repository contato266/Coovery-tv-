package com.streamvault.app.device

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
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

@SuppressLint("SourceLockedOrientationActivity")
fun ComponentActivity.applyPlatformScreenOrientation() {
    applyResolvedOrientation(
        startupScreenOrientation(
            isTelevisionDevice = isTelevisionDevice(),
            isFireTvDevice = isFireTvDevice(),
            supportsPortrait = packageManager.hasSystemFeature(PackageManager.FEATURE_SCREEN_PORTRAIT)
        )
    )
}

/** Portrait for app browsing on phones. Televisions stay in landscape so the activity can open. */
@SuppressLint("SourceLockedOrientationActivity")
fun ComponentActivity.applyHandheldBrowseOrientation() {
    if (!canLockPortrait()) return
    applyResolvedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
}

/** Landscape while watching movies, series, or live channels on phones; no-op on TV. */
@SuppressLint("SourceLockedOrientationActivity")
fun ComponentActivity.applyHandheldPlayerOrientation() {
    if (isTelevisionDevice() || isFireTvDevice()) return
    applyResolvedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
}

internal fun startupScreenOrientation(
    isTelevisionDevice: Boolean,
    isFireTvDevice: Boolean,
    supportsPortrait: Boolean
): Int = when {
    isTelevisionDevice || isFireTvDevice || !supportsPortrait ->
        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
}

private fun ComponentActivity.canLockPortrait(): Boolean =
    startupScreenOrientation(
        isTelevisionDevice = isTelevisionDevice(),
        isFireTvDevice = isFireTvDevice(),
        supportsPortrait = packageManager.hasSystemFeature(PackageManager.FEATURE_SCREEN_PORTRAIT)
    ) == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

private fun ComponentActivity.applyResolvedOrientation(orientation: Int) {
    runCatching { requestedOrientation = orientation }
}
