package com.streamvault.player.playback

import com.streamvault.player.PlaybackState

internal fun shouldRecoverReadyStalls(resolvedStreamType: ResolvedStreamType): Boolean =
    true

internal fun shouldRecoverPositionAdvancingReadyStalls(resolvedStreamType: ResolvedStreamType): Boolean =
    !resolvedStreamType.isLiveForStallRecovery

internal fun shouldRecoverFrameSilentReadyStalls(resolvedStreamType: ResolvedStreamType): Boolean =
    resolvedStreamType.isLiveForStallRecovery

internal fun shouldReconnectLiveStall(
    playbackState: PlaybackState,
    resolvedStreamType: ResolvedStreamType,
    recoveryAttempt: Int
): Boolean {
    if (recoveryAttempt != 1) return false
    return when (playbackState) {
        PlaybackState.BUFFERING ->
            resolvedStreamType.isLiveForStallRecovery ||
                resolvedStreamType == ResolvedStreamType.PROGRESSIVE
        PlaybackState.READY -> resolvedStreamType.isLiveForStallRecovery
        else -> false
    }
}

private val ResolvedStreamType.isLiveForStallRecovery: Boolean
    get() = this == ResolvedStreamType.HLS ||
        this == ResolvedStreamType.SMOOTH_STREAMING ||
        this == ResolvedStreamType.MPEG_TS_LIVE ||
        this == ResolvedStreamType.RTSP
