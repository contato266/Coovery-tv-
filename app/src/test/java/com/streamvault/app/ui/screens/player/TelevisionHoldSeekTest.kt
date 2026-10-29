package com.streamvault.app.ui.screens.player

import android.view.KeyEvent
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test

class TelevisionHoldSeekTest {

    @Test
    fun moviesAndEpisodesSupportHoldSeek() {
        assertThat(supportsTelevisionHoldSeek("MOVIE")).isTrue()
        assertThat(supportsTelevisionHoldSeek("VOD")).isTrue()
        assertThat(supportsTelevisionHoldSeek("SERIES")).isTrue()
        assertThat(supportsTelevisionHoldSeek("SERIES_EPISODE")).isTrue()
        assertThat(supportsTelevisionHoldSeek("LIVE")).isFalse()
    }

    @Test
    fun rightKeySeeksForwardAndLeftKeySeeksBackward() {
        assertThat(televisionHoldSeekDirection(KeyEvent.KEYCODE_DPAD_RIGHT, isRtl = false))
            .isEqualTo(TelevisionHoldSeekDirection.FORWARD)
        assertThat(televisionHoldSeekDirection(KeyEvent.KEYCODE_DPAD_LEFT, isRtl = false))
            .isEqualTo(TelevisionHoldSeekDirection.BACKWARD)
    }

    @Test
    fun rtlSwapsTheNavigationDirection() {
        assertThat(televisionHoldSeekDirection(KeyEvent.KEYCODE_DPAD_RIGHT, isRtl = true))
            .isEqualTo(TelevisionHoldSeekDirection.BACKWARD)
        assertThat(televisionHoldSeekDirection(KeyEvent.KEYCODE_DPAD_LEFT, isRtl = true))
            .isEqualTo(TelevisionHoldSeekDirection.FORWARD)
    }

    @Test
    fun holdStepsAccumulateTwentySecondsAndStayInsideTheMovie() {
        val first = nextTelevisionHoldSeekPosition(
            anchorMs = null,
            currentPositionMs = 10_000L,
            durationMs = 90_000L,
            forward = true
        )
        val second = nextTelevisionHoldSeekPosition(
            anchorMs = first,
            currentPositionMs = 10_000L,
            durationMs = 90_000L,
            forward = true
        )
        val third = nextTelevisionHoldSeekPosition(
            anchorMs = second,
            currentPositionMs = 10_000L,
            durationMs = 90_000L,
            forward = true
        )
        val back = nextTelevisionHoldSeekPosition(
            anchorMs = 15_000L,
            currentPositionMs = 15_000L,
            durationMs = 90_000L,
            forward = false
        )

        assertThat(first).isEqualTo(30_000L)
        assertThat(second).isEqualTo(50_000L)
        assertThat(third).isEqualTo(70_000L)
        assertThat(back).isEqualTo(0L)
        assertThat(
            nextTelevisionHoldSeekPosition(
                anchorMs = 80_000L,
                currentPositionMs = 80_000L,
                durationMs = 90_000L,
                forward = true
            )
        ).isEqualTo(90_000L)
    }

    @Test
    fun holdingRightStepsUntilTheKeyIsReleased() = runBlocking {
        val steps = mutableListOf<TelevisionHoldSeekDirection>()
        var released = 0
        val controller = TelevisionHoldSeekController(
            scope = this,
            longPressTimeoutMs = 40L,
            intervalMs = 40L,
            onStep = { steps.add(it) },
            onReleased = { released += 1 }
        )

        assertThat(
            controller.onKey(
                keyCode = KeyEvent.KEYCODE_DPAD_RIGHT,
                action = KeyEvent.ACTION_DOWN,
                repeatCount = 0,
                isRtl = false,
                enabled = true
            )
        ).isFalse()
        delay(20)
        assertThat(steps).isEmpty()
        delay(90)
        assertThat(steps.size).isAtLeast(2)
        assertThat(steps).containsExactlyElementsIn(
            List(steps.size) { TelevisionHoldSeekDirection.FORWARD }
        )

        assertThat(
            controller.onKey(
                keyCode = KeyEvent.KEYCODE_DPAD_RIGHT,
                action = KeyEvent.ACTION_UP,
                repeatCount = 0,
                isRtl = false,
                enabled = true
            )
        ).isTrue()
        assertThat(released).isEqualTo(1)
        val countAtRelease = steps.size
        delay(80)
        assertThat(steps).hasSize(countAtRelease)
        controller.cancel()
    }

    @Test
    fun quickPressDoesNotRepeatTheSeek() = runBlocking {
        val steps = mutableListOf<TelevisionHoldSeekDirection>()
        val controller = TelevisionHoldSeekController(
            scope = this,
            longPressTimeoutMs = 80L,
            intervalMs = 40L,
            onStep = { steps.add(it) },
            onReleased = {}
        )

        controller.onKey(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.ACTION_DOWN, 0, false, true)
        delay(30)
        assertThat(
            controller.onKey(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.ACTION_UP, 0, false, true)
        ).isFalse()
        delay(100)
        assertThat(steps).isEmpty()
        controller.cancel()
    }

    @Test
    fun phoneAndLivePlaybackIgnoreTheHold() = runBlocking {
        val steps = mutableListOf<TelevisionHoldSeekDirection>()
        val controller = TelevisionHoldSeekController(
            scope = this,
            longPressTimeoutMs = 20L,
            intervalMs = 20L,
            onStep = { steps.add(it) },
            onReleased = {}
        )

        controller.onKey(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.ACTION_DOWN, 0, false, enabled = false)
        delay(60)
        assertThat(steps).isEmpty()
        controller.cancel()
    }
}
