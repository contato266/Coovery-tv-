package com.streamvault.player.playback

import com.streamvault.player.PlaybackState

internal fun shouldRecoverReadyStalls(
    resolvedStreamType: ResolvedStreamType,
    televisionDevice: Boolean = false
): Boolean {
    if (televisionDevice && resolvedStreamType == ResolvedStreamType.PROGRESSIVE) {
        return false
    }
    return true
}

internal fun shouldRecoverPositionAdvancingReadyStalls(
    resolvedStreamType: ResolvedStreamType,
    televisionDevice: Boolean = false
): Boolean =
    !resolvedStreamType.isLiveForStallRecovery && !televisionDevice

internal fun shouldRecoverFrameSilentReadyStalls(resolvedStreamType: ResolvedStreamType): Boolean =
    resolvedStreamType.isLiveForStallRecovery

internal fun shouldReconnectLiveStall(
    playbackState: PlaybackState,
    resolvedStreamType: ResolvedStreamType,
    recoveryAttempt: Int,
    televisionDevice: Boolean = false
): Boolean {
    if (recoveryAttempt != 1) return false
    return when (playbackState) {
        PlaybackState.BUFFERING ->
            resolvedStreamType.isLiveForStallRecovery ||
                (resolvedStreamType == ResolvedStreamType.PROGRESSIVE && !televisionDevice)
        PlaybackState.READY -> resolvedStreamType.isLiveForStallRecovery
        else -> false
    }
}

internal fun shouldRecoverBufferingStalls(
    resolvedStreamType: ResolvedStreamType,
    liveStream: Boolean,
    playbackStarted: Boolean,
    televisionDevice: Boolean = false
): Boolean {
    if (!playbackStarted) return false
    if (liveStream) return true
    if (resolvedStreamType == ResolvedStreamType.PROGRESSIVE && televisionDevice) {
        return false
    }
    return resolvedStreamType == ResolvedStreamType.PROGRESSIVE
}

private val ResolvedStreamType.isLiveForStallRecovery: Boolean
    get() = this == ResolvedStreamType.HLS ||
        this == ResolvedStreamType.SMOOTH_STREAMING ||
        this == ResolvedStreamType.MPEG_TS_LIVE ||
        this == ResolvedStreamType.RTSP
