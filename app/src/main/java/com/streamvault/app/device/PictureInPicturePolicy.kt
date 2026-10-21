package com.streamvault.app.device

import android.os.Build

/**
 * Fire TV and other television devices treat picture-in-picture as a shrunk
 * background window. Leaving it enabled makes playback drop out of fullscreen
 * after the system backgrounds the activity (screensaver, Home, or idle).
 * PiP stays available on phones and tablets.
 */
fun shouldOfferPictureInPicture(
    isTelevisionDevice: Boolean,
    sdkInt: Int,
    hasPictureInPictureFeature: Boolean
): Boolean {
    if (isTelevisionDevice) return false
    if (sdkInt < Build.VERSION_CODES.O) return false
    return hasPictureInPictureFeature
}
