package com.streamvault.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.streamvault.app.device.rememberIsTelevisionDevice
import com.streamvault.app.ui.accessibility.rememberReducedMotionEnabled
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun rememberCrossfadeImageModel(data: Any?): Any? {
    val context = LocalContext.current
    val reducedMotionEnabled = rememberReducedMotionEnabled()
    val isTelevisionDevice = rememberIsTelevisionDevice()
    return remember(context, data, reducedMotionEnabled, isTelevisionDevice) {
        data?.let {
            ImageRequest.Builder(context)
                .data(it)
                .crossfade(!reducedMotionEnabled && !isTelevisionDevice)
                .build()
        }
    }
}