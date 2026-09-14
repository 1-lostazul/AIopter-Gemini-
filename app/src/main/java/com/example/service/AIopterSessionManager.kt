package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import com.example.actions.ActionExecutor
import com.example.ai.AiProviderOrchestrator
import com.example.data.repository.AIopterRepository
import com.example.model.AIopterSessionState
import com.example.model.ActionRiskLevel
import com.example.model.ActionStatus
import com.example.model.AiRequestState
import com.example.model.MicrophoneState
import com.example.model.OverlayState
import com.example.model.SafeAction
import com.example.model.ScreenCaptureState
import com.example.model.WebCitation
import com.example.security.ForegroundAppDetector
import com.example.security.PermissionAndSafetyManager
import com.example.security.ScreenAccessCheckResult
import com.example.voice.VoiceInteractionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class AIopterSessionManager private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    val repository = AIopterRepository(appContext)
    val safetyManager = PermissionAndSafetyManager(repository)
    val voiceManager = VoiceInteractionManager(appContext)
    private val aiOrchestrator = AiProviderOrchestrator(repository.preferences)

    private val _sessionState = MutableStateFlow(AIopterSessionState())
    val sessionState: StateFlow<AIopterSessionState> = _sessionState.asStateFlow()

    private val _activeCitations = MutableStateFlow<List<WebCitation>>(emptyList())
    val activeCitations: StateFlow<List<WebCitation>> = _activeCitations.asStateFlow()

    private val _activeProposedAction = MutableStateFlow<SafeAction?>(null)
    val activeProposedAction: StateFlow<SafeAction?> = _activeProposedAction.asStateFlow()

    private val _panelHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val panelHistory: StateFlow<List<Pair<String, String>>> = _panelHistory.asStateFlow()

    private var activeStreamingJob: Job? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        initTts()
        voiceManager.onTranscriptionComplete = { text ->
            if (text.equals("stop", ignoreCase = true) || text.equals("stop aiopter", ignoreCase = true)) {
                triggerKillSwitch()
            } else {
                submitUserPrompt(text)
            }
        }

        // Mirror voiceManager state into sessionState
        scope.launch {
            voiceManager.micState.collect { state ->
                _sessionState.update { it.copy(microphoneState = state) }
            }
        }
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                    isTtsReady = true
                }
            }
        } catch (e: Exception) {
            Log.e("AIopterSessionManager", "TTS init error: ${e.message}")
        }
    }

    fun updateOverlayState(state: OverlayState) {
        _sessionState.update { it.copy(overlayState = state) }
    }

    fun updateScreenCaptureState(state: ScreenCaptureState) {
        _sessionState.update { it.copy(screenCaptureState = state) }
        scope.launch {
            repository.logAudit("SCREEN_STATE_CHANGE", "Screen state updated to ${state.name}", 1)
        }
    }

    fun updateCurrentForegroundApp(packageName: String, appName: String) {
        _sessionState.update {
            it.copy(
                currentPackageName = packageName,
                currentAppName = appName
            )
        }
    }

    fun allowSensitiveAppThisTime(packageName: String) {
        safetyManager.grantOneTimeAppAccess(packageName)
        _sessionState.update {
            it.copy(
                screenCaptureState = if (repository.preferences.isScreenSharingEnabled) ScreenCaptureState.SHARING else ScreenCaptureState.OFF,
                activeError = null
            )
        }
        scope.launch {
            repository.logAudit("SENSITIVE_APP_ALLOWED_ONCE", "User granted 'Allow This Time' for $packageName", 2)
        }
    }

    fun toggleScreenSharing(requestedOn: Boolean) {
        if (!requestedOn) {
            AIopterMediaProjectionService.stop(appContext)
            ScreenContextProcessor.clearTemporaryBuffers()
            _sessionState.update { it.copy(screenCaptureState = ScreenCaptureState.OFF, activeError = null) }
            repository.preferences.isScreenSharingEnabled = false
            scope.launch {
                repository.logAudit("SCREEN_SHARING_OFF", "Screen capture deactivated by user", 0)
            }
        } else {
            // Must be accompanied by system consent flow from Activity
            _sessionState.update { it.copy(screenCaptureState = ScreenCaptureState.REQUESTING_PERMISSION) }
            repository.preferences.isScreenSharingEnabled = true
        }
    }

    fun submitUserPrompt(promptText: String) {
        val trimmed = promptText.trim()
        if (trimmed.isEmpty()) return

        // Prevent duplicate conflicts by cancelling any existing stream
        cancelCurrentAiRequest()

        val historyList = _panelHistory.value.toMutableList()
        historyList.add("user" to trimmed)
        _panelHistory.value = historyList

        _sessionState.update {
            it.copy(
                aiRequestState = AiRequestState.SENDING,
                activeStreamingText = "",
                activeError = null
            )
        }
        _activeCitations.value = emptyList()
        _activeProposedAction.value = null

        val isSharing = _sessionState.value.screenCaptureState == ScreenCaptureState.SHARING

        scope.launch {
            var screenContextText: String? = null

            if (isSharing) {
                // Detect active foreground app
                val detectedPackage = ForegroundAppDetector.getForegroundPackageName(appContext)
                    ?: _sessionState.value.currentPackageName

                val accessResult = safetyManager.evaluateScreenAccess(
                    isScreenSharingGloballyEnabled = repository.preferences.isScreenSharingEnabled,
                    detectedPackage = detectedPackage
                )

                when (accessResult) {
                    is ScreenAccessCheckResult.Blocked -> {
                        _sessionState.update {
                            it.copy(
                                aiRequestState = AiRequestState.ERROR,
                                activeError = accessResult.reason
                            )
                        }
                        return@launch
                    }
                    is ScreenAccessCheckResult.NeedsOneTimeConfirmation -> {
                        _sessionState.update {
                            it.copy(
                                aiRequestState = AiRequestState.AWAITING_CONFIRMATION,
                                activeError = "Sensitive app detected (${accessResult.appName}). Tap 'Allow This Time' to continue."
                            )
                        }
                        _activeProposedAction.value = SafeAction(
                            id = java.util.UUID.randomUUID().toString(),
                            title = "Allow Screen for ${accessResult.appName}",
                            description = "Grant one-time screen inspection for ${accessResult.packageName}",
                            riskLevel = com.example.model.RiskLevel.LEVEL_3_SENSITIVE,
                            actionType = com.example.model.ActionType.ALLOW_SCREEN_ONCE,
                            payload = mapOf("package_name" to accessResult.packageName),
                            status = ActionStatus.PROPOSED
                        )
                        return@launch
                    }
                    is ScreenAccessCheckResult.UndeterminedForegroundApp -> {
                        _sessionState.update {
                            it.copy(
                                aiRequestState = AiRequestState.ERROR,
                                activeError = accessResult.message
                            )
                        }
                        return@launch
                    }
                    is ScreenAccessCheckResult.Allowed -> {
                        val processed = ScreenContextProcessor.extractContextForRequest(includeImage = false)
                        screenContextText = processed.summaryText
                    }
                }
            }

            activeStreamingJob = launch {
                _sessionState.update { it.copy(aiRequestState = AiRequestState.STREAMING) }

                repository.logAudit(
                    eventType = "AI_QUERY_DISPATCHED",
                    details = "User query dispatched | HasScreenContext: ${screenContextText != null}",
                    riskLevel = 0
                )

                try {
                    aiOrchestrator.streamResponse(
                        prompt = trimmed,
                        screenSummary = screenContextText,
                        detailLevel = repository.preferences.responseDetail,
                        conversationHistory = historyList
                    ).collect { chunk ->
                        _sessionState.update {
                            it.copy(
                                activeStreamingText = chunk.fullText,
                                aiRequestState = if (chunk.isComplete) {
                                    if (chunk.proposedAction != null) AiRequestState.TOOL_PROPOSED else AiRequestState.COMPLETE
                                } else AiRequestState.STREAMING,
                                activeError = chunk.errorMessage
                            )
                        }

                        if (chunk.citations.isNotEmpty()) {
                            _activeCitations.value = chunk.citations
                        }

                        if (chunk.proposedAction != null) {
                            _activeProposedAction.value = chunk.proposedAction
                        }

                        if (chunk.isComplete) {
                            val updated = _panelHistory.value.toMutableList()
                            updated.add("assistant" to chunk.fullText)
                            _panelHistory.value = updated

                            if (repository.preferences.isSpokenRepliesEnabled && isTtsReady) {
                                speakText(chunk.fullText.take(200))
                            }

                            // Optional conversation persistence
                            if (repository.preferences.isConversationHistoryEnabled) {
                                launch(Dispatchers.IO) {
                                    val convId = repository.createConversation("Conversation")
                                    repository.saveMessage(convId, "user", trimmed, hasScreen = screenContextText != null)
                                    repository.saveMessage(convId, "assistant", chunk.fullText)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    _sessionState.update {
                        it.copy(
                            aiRequestState = AiRequestState.ERROR,
                            activeError = e.localizedMessage ?: "Failed to receive response"
                        )
                    }
                }
            }
        }
    }

    fun startListening() {
        voiceManager.startListening()
    }

    fun stopListening() {
        voiceManager.stopListening()
    }

    fun cancelCurrentAiRequest() {
        activeStreamingJob?.cancel()
        activeStreamingJob = null
        if (_sessionState.value.aiRequestState == AiRequestState.STREAMING ||
            _sessionState.value.aiRequestState == AiRequestState.SENDING
        ) {
            _sessionState.update { it.copy(aiRequestState = AiRequestState.CANCELLED) }
        }
    }

    /**
     * Emergency Kill Switch:
     * Real, immediate, local stop of all processes, network jobs, sensors, buffers, and overlay windows.
     */
    fun triggerKillSwitch() {
        // 1. Cancel active AI coroutine jobs
        cancelCurrentAiRequest()

        // 2. Stop Voice Recognition
        voiceManager.resetOnEmergencyKill()

        // 3. Stop TTS
        try {
            tts?.stop()
        } catch (_: Exception) {}

        // 4. Invalidate tokens and security grants
        safetyManager.resetOnEmergencyKill()

        // 5. Stop MediaProjection Service & release buffers
        AIopterMediaProjectionService.stop(appContext)
        ScreenContextProcessor.clearTemporaryBuffers()

        // 6. Reset preference state & session state
        repository.preferences.isScreenSharingEnabled = false
        _sessionState.update {
            it.copy(
                overlayState = OverlayState.DISABLED,
                screenCaptureState = ScreenCaptureState.OFF,
                microphoneState = MicrophoneState.IDLE,
                aiRequestState = AiRequestState.CANCELLED,
                activeStreamingText = "",
                activeError = null,
                hasUnsavedCaptureBuffers = false
            )
        }
        _activeProposedAction.value = null
        _activeCitations.value = emptyList()

        // 7. Stop Overlay Service
        AIopterOverlayService.stop(appContext)

        // 8. Log audit
        scope.launch {
            repository.logAudit("KILL_TRIGGERED", "Emergency stop executed: all streams, mic, screen, and overlay terminated", 3)
        }

        Handler(Looper.getMainLooper()).post {
            Toast.makeText(appContext, "AIopter Emergency Stop (KILL) activated", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Two-Step Safe Action Confirmation Engine:
     * - For Read-Only or Low-Risk: single confirm runs action.
     * - For Moderate / Critical: Step 1 issues a short-lived token and moves state to AWAITING_SECOND_CONFIRMATION.
     *   Step 2 verifies the token with PermissionAndSafetyManager before running.
     */
    fun confirmProposedAction(action: SafeAction) {
        if (safetyManager.isTwoStepRequired(action) && action.status != ActionStatus.AWAITING_SECOND_CONFIRMATION) {
            // STEP 1: Review & Confirm -> generate short-lived token
            val token = safetyManager.issueApprovalToken(action.id)
            _activeProposedAction.value = action.copy(
                status = ActionStatus.AWAITING_SECOND_CONFIRMATION,
                approvalToken = token.token
            )
            _sessionState.update { it.copy(aiRequestState = AiRequestState.AWAITING_CONFIRMATION) }
            scope.launch {
                repository.logAudit("ACTION_REVIEWED_STEP_1", "User reviewed action ${action.title}. Awaiting final authorization.", action.riskLevel.level)
            }
            return
        }

        // STEP 2 (or single-step low risk): Execute
        _activeProposedAction.value = action.copy(status = ActionStatus.CONFIRMED)
        _sessionState.update { it.copy(aiRequestState = AiRequestState.EXECUTING) }

        ActionExecutor.execute(appContext, action, safetyManager, repository) { success, message ->
            _activeProposedAction.value = action.copy(status = if (success) ActionStatus.EXECUTED else ActionStatus.REJECTED)
            _sessionState.update {
                it.copy(
                    aiRequestState = AiRequestState.COMPLETE,
                    activeError = if (!success) message else null
                )
            }
            scope.launch {
                repository.logAudit(
                    "ACTION_EXECUTED",
                    "Action ${action.title} (${action.actionType.name}) result: $success ($message)",
                    action.riskLevel.level
                )
            }
        }
    }

    fun rejectProposedAction(action: SafeAction) {
        safetyManager.invalidateActionTokens(action.id)
        _activeProposedAction.value = action.copy(status = ActionStatus.REJECTED)
        _sessionState.update { it.copy(aiRequestState = AiRequestState.IDLE) }
        scope.launch {
            repository.logAudit("ACTION_REJECTED", "User rejected action ${action.title}", action.riskLevel.level)
        }
    }

    private fun speakText(text: String) {
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AIopterReply")
        } catch (_: Exception) {}
    }

    fun resetSession() {
        cancelCurrentAiRequest()
        _sessionState.value = AIopterSessionState()
        _activeProposedAction.value = null
        _activeCitations.value = emptyList()
    }

    companion object {
        @Volatile
        private var INSTANCE: AIopterSessionManager? = null

        fun getInstance(context: Context): AIopterSessionManager {
            return INSTANCE ?: synchronized(this) {
                val instance = AIopterSessionManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
