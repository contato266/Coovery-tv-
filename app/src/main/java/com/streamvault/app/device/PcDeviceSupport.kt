package com.streamvault.app.device

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.streamvault.app.BuildConfig

/** True only in the PC distribution APK ([BuildConfig.IS_PC_DISTRIBUTION]). */
fun Context.isPcDevice(): Boolean = BuildConfig.IS_PC_DISTRIBUTION

@Composable
fun rememberIsPcDevice(): Boolean {
    val context = LocalContext.current
    return remember(context) { context.isPcDevice() }
}

/**
 * Wide-screen shell shared by Android TV and the PC distribution build. Handheld phones/tablets
 * stay on the existing bottom-navigation layout.
 */
@Composable
fun rememberUseLivingRoomUi(): Boolean {
    val isTelevision = rememberIsTelevisionDevice()
    val isPc = rememberIsPcDevice()
    return remember(isTelevision, isPc) { isTelevision || isPc }
}

/** Handheld phone UX (portrait chrome, bottom bar, immersive welcome). Excludes TV and PC builds. */
fun Context.isHandheldPhoneExperience(): Boolean = !isTelevisionDevice() && !isPcDevice()
