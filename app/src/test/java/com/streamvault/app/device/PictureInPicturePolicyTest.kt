package com.streamvault.app.device

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PictureInPicturePolicyTest {

    @Test
    fun `television devices never enter picture in picture`() {
        assertThat(
            shouldOfferPictureInPicture(
                isTelevisionDevice = true,
                sdkInt = 30,
                hasPictureInPictureFeature = true
            )
        ).isFalse()
        assertThat(
            shouldOfferPictureInPicture(
                isTelevisionDevice = true,
                sdkInt = 34,
                hasPictureInPictureFeature = true
            )
        ).isFalse()
    }

    @Test
    fun `handheld devices keep picture in picture when the platform supports it`() {
        assertThat(
            shouldOfferPictureInPicture(
                isTelevisionDevice = false,
                sdkInt = 30,
                hasPictureInPictureFeature = true
            )
        ).isTrue()
    }

    @Test
    fun `handheld devices without the feature or below Oreo stay fullscreen`() {
        assertThat(
            shouldOfferPictureInPicture(
                isTelevisionDevice = false,
                sdkInt = 25,
                hasPictureInPictureFeature = true
            )
        ).isFalse()
        assertThat(
            shouldOfferPictureInPicture(
                isTelevisionDevice = false,
                sdkInt = 30,
                hasPictureInPictureFeature = false
            )
        ).isFalse()
    }
}
