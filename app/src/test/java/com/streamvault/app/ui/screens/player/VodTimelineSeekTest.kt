package com.streamvault.app.ui.screens.player

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class VodTimelineSeekTest {

    @Test
    fun timelineMovesTenSecondsAndStaysInsideTheMovie() {
        assertThat(
            nextVodTimelinePositionMs(positionMs = 30_000L, durationMs = 90_000L, forward = true)
        ).isEqualTo(40_000L)
        assertThat(
            nextVodTimelinePositionMs(positionMs = 30_000L, durationMs = 90_000L, forward = false)
        ).isEqualTo(20_000L)
        assertThat(
            nextVodTimelinePositionMs(positionMs = 85_000L, durationMs = 90_000L, forward = true)
        ).isEqualTo(90_000L)
        assertThat(
            nextVodTimelinePositionMs(positionMs = 4_000L, durationMs = 90_000L, forward = false)
        ).isEqualTo(0L)
    }

    @Test
    fun holdingTheRemoteStepsAgainOnlyAfterTheInterval() {
        assertThat(shouldStepVodTimeline(nowMs = 1_000L, lastStepAtMs = 0L, repeatCount = 0)).isTrue()
        assertThat(shouldStepVodTimeline(nowMs = 1_050L, lastStepAtMs = 1_000L, repeatCount = 4)).isFalse()
        assertThat(shouldStepVodTimeline(nowMs = 1_140L, lastStepAtMs = 1_000L, repeatCount = 6)).isTrue()
    }
}