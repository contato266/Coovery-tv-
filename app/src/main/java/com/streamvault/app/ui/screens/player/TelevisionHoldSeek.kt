package com.streamvault.app.ui.screens.player

import android.view.KeyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal const val TELEVISION_HOLD_SEEK_STEP_MS = 10_000L
internal const val TELEVISION_HOLD_SEEK_INTERVAL_MS = 200L

/**
 * Netflix-style hold speed. A short hold nudges a few seconds. Keeping the
 * remote button down speeds up until a movie can reach the middle or the end
 * without waiting through fixed 20-second jumps.
 */
internal fun acceleratedSeekStepMs(heldMs: Long, durationMs: Long): Long {
    val timed = when {
        heldMs < 700L -> 10_000L
        heldMs < 1_400L -> 30_000L
        heldMs < 2_200L -> 60_000L
        heldMs < 3_200L -> 180_000L
        else -> 0L
    }
    if (timed > 0L) return timed
    if (durationMs <= 0L) return 600_000L
    return (durationMs / 8L).coerceAtLeast(180_000L).coerceAtMost(durationMs)
}

internal enum class TelevisionHoldSeekDirection {
    BACKWARD,
    FORWARD
}

internal fun supportsTelevisionHoldSeek(contentType: String): Boolean =
    contentType == "MOVIE" ||
        contentType == "VOD" ||
        contentType == "SERIES" ||
        contentType == "SERIES_EPISODE"

internal fun televisionHoldSeekDirection(
    keyCode: Int,
    isRtl: Boolean
): TelevisionHoldSeekDirection? {
    val seeksForward = when (keyCode) {
        KeyEvent.KEYCODE_DPAD_RIGHT -> !isRtl
        KeyEvent.KEYCODE_DPAD_LEFT -> isRtl
        else -> return null
    }
    return if (seeksForward) {
        TelevisionHoldSeekDirection.FORWARD
    } else {
        TelevisionHoldSeekDirection.BACKWARD
    }
}

internal fun nextTelevisionHoldSeekPosition(
    anchorMs: Long?,
    currentPositionMs: Long,
    durationMs: Long,
    forward: Boolean,
    stepMs: Long = TELEVISION_HOLD_SEEK_STEP_MS
): Long {
    val base = (anchorMs ?: currentPositionMs).coerceAtLeast(0L)
    val shifted = if (forward) base + stepMs else base - stepMs
    val atLeastZero = shifted.coerceAtLeast(0L)
    return if (durationMs > 0L) atLeastZero.coerceAtMost(durationMs) else atLeastZero
}

/**
 * While the remote's left or right key is held during a movie or episode, move
 * through the video. The longer the button stays down, the bigger each jump,
 * until the viewer releases it and playback continues from that position.
 */
internal class TelevisionHoldSeekController(
    private val scope: CoroutineScope,
    private val longPressTimeoutMs: Long,
    private val intervalMs: Long = TELEVISION_HOLD_SEEK_INTERVAL_MS,
    private val onStep: (TelevisionHoldSeekDirection, Long) -> Unit,
    private val onReleased: () -> Unit
) {
    private var pressedKey: Int? = null
    private var seeking = false
    private var job: Job? = null

    fun onKey(
        keyCode: Int,
        action: Int,
        repeatCount: Int,
        isRtl: Boolean,
        enabled: Boolean
    ): Boolean {
        val direction = televisionHoldSeekDirection(keyCode, isRtl)
        if (!enabled || direction == null) {
            if (pressedKey == keyCode && (action == KeyEvent.ACTION_UP || direction != null)) {
                return release()
            }
            return false
        }
        if (action == KeyEvent.ACTION_UP) {
            return release()
        }
        if (action != KeyEvent.ACTION_DOWN) return false
        if (repeatCount > 0) {
            if (pressedKey != keyCode) {
                arm(keyCode, direction, startImmediately = true)
            }
            return true
        }
        arm(keyCode, direction, startImmediately = false)
        return false
    }

    fun isTracking(keyCode: Int): Boolean = pressedKey == keyCode

    fun cancel() {
        release()
    }

    private fun arm(
        keyCode: Int,
        direction: TelevisionHoldSeekDirection,
        startImmediately: Boolean
    ) {
        pressedKey = keyCode
        seeking = false
        job?.cancel()
        job = scope.launch {
            if (!startImmediately) {
                delay(longPressTimeoutMs)
            }
            if (!isActive || pressedKey != keyCode) return@launch
            seeking = true
            val startedAtMs = android.os.SystemClock.uptimeMillis()
            while (isActive && pressedKey == keyCode) {
                onStep(direction, (android.os.SystemClock.uptimeMillis() - startedAtMs).coerceAtLeast(0L))
                delay(intervalMs)
            }
        }
    }

    private fun release(): Boolean {
        val consumed = seeking
        pressedKey = null
        seeking = false
        job?.cancel()
        job = null
        onReleased()
        return consumed
    }
}
