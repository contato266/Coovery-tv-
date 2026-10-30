package com.streamvault.app.ui.screens.player

internal const val VOD_TIMELINE_STEP_MS = 10_000L
internal const val VOD_TIMELINE_STEP_INTERVAL_MS = 140L

internal fun nextVodTimelinePositionMs(
    positionMs: Long,
    durationMs: Long,
    forward: Boolean,
    stepMs: Long = VOD_TIMELINE_STEP_MS
): Long {
    if (durationMs <= 0L) return 0L
    val delta = if (forward) stepMs else -stepMs
    return (positionMs + delta).coerceIn(0L, durationMs)
}

internal fun shouldStepVodTimeline(
    nowMs: Long,
    lastStepAtMs: Long,
    repeatCount: Int,
    intervalMs: Long = VOD_TIMELINE_STEP_INTERVAL_MS
): Boolean {
    if (repeatCount <= 0) return true
    return nowMs - lastStepAtMs >= intervalMs
}
