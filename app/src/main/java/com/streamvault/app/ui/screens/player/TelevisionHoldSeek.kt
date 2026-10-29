package com.streamvault.app.ui.screens.player

import android.view.KeyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal const val TELEVISION_HOLD_SEEK_STEP_MS = 20_000L
internal const val TELEVISION_HOLD_SEEK_INTERVAL_MS = 500L

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
 * While the remote's left or right key is held during a movie or episode, step
 * the playhead by 20 seconds until the key is released. A short press still
 * follows the existing single-step seek.
 */
internal class TelevisionHoldSeekController(
    private val scope: CoroutineScope,
    private val longPressTimeoutMs: Long,
    private val intervalMs: Long = TELEVISION_HOLD_SEEK_INTERVAL_MS,
    private val onStep: (TelevisionHoldSeekDirection) -> Unit,
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
            while (isActive && pressedKey == keyCode) {
                onStep(direction)
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
