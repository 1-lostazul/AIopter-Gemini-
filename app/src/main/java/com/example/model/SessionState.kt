package com.example.model

/**
 * Explicit state machine for AIopter to prevent "looks on but is actually off" failures.
 * Each subsystem tracks its independent state strictly.
 */
enum class OverlayState {
    DISABLED,
    PERMISSION_MISSING,
    READY,
    BUBBLE,
    PANEL
}

enum class ScreenCaptureState {
    OFF,
    REQUESTING_PERMISSION,
    STARTING,
    SHARING,
    BLOCKED_FOR_APP,
    STOPPING,
    ERROR
}

enum class MicrophoneState {
    IDLE,
    REQUESTING_PERMISSION,
    LISTENING,
    TRANSCRIBING,
    ERROR
}

enum class AiRequestState {
    IDLE,
    SENDING,
    STREAMING,
    TOOL_PROPOSED,
    AWAITING_CONFIRMATION,
    EXECUTING,
    COMPLETE,
    CANCELLED,
    ERROR
}

enum class NetworkState {
    ONLINE,
    DEGRADED,
    OFFLINE
}

enum class AccountState {
    SIGNED_OUT,
    AUTHENTICATING,
    READY,
    TOKEN_REFRESH,
    ERROR
}

data class AIopterSessionState(
    val overlayState: OverlayState = OverlayState.DISABLED,
    val screenCaptureState: ScreenCaptureState = ScreenCaptureState.OFF,
    val microphoneState: MicrophoneState = MicrophoneState.IDLE,
    val aiRequestState: AiRequestState = AiRequestState.IDLE,
    val networkState: NetworkState = NetworkState.ONLINE,
    val accountState: AccountState = AccountState.READY,
    val currentPackageName: String? = null,
    val currentAppName: String? = null,
    val activeStreamingText: String = "",
    val activeError: String? = null,
    val hasUnsavedCaptureBuffers: Boolean = false
)
