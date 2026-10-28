package com.streamvault.app.device

import android.content.pm.ActivityInfo
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HandheldOrientationTest {

    @Test
    fun televisionOpensInLandscapeEvenWhenPortraitIsAdvertised() {
        assertThat(
            startupScreenOrientation(
                isTelevisionDevice = true,
                isFireTvDevice = false,
                supportsPortrait = true
            )
        ).isEqualTo(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
    }

    @Test
    fun fireTvOpensInLandscape() {
        assertThat(
            startupScreenOrientation(
                isTelevisionDevice = false,
                isFireTvDevice = true,
                supportsPortrait = true
            )
        ).isEqualTo(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
    }

    @Test
    fun deviceWithoutPortraitSupportOpensInLandscape() {
        assertThat(
            startupScreenOrientation(
                isTelevisionDevice = false,
                isFireTvDevice = false,
                supportsPortrait = false
            )
        ).isEqualTo(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
    }

    @Test
    fun phoneWithPortraitSupportUsesPortrait() {
        assertThat(
            startupScreenOrientation(
                isTelevisionDevice = false,
                isFireTvDevice = false,
                supportsPortrait = true
            )
        ).isEqualTo(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
    }
}
