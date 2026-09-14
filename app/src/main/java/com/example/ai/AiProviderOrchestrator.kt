package com.example.ai

import com.example.data.preferences.AIopterPreferences
import kotlinx.coroutines.flow.Flow

class AiProviderOrchestrator(private val preferences: AIopterPreferences) : AiProvider {

    override val providerId: String = "aiopter-orchestrator"
    override val displayName: String = "AIopter AI Service Router"

    private val backendProvider = AIopterBackendAiProvider(preferences)
    private val debugFakeProvider = DebugFakeAiProvider()

    override fun streamResponse(
        prompt: String,
        screenSummary: String?,
        detailLevel: String,
        conversationHistory: List<Pair<String, String>>
    ): Flow<AiStreamChunk> {
        val selectedProvider: AiProvider = if (preferences.isDebugFakeProviderEnabled) {
            debugFakeProvider
        } else {
            backendProvider
        }

        return selectedProvider.streamResponse(
            prompt = prompt,
            screenSummary = screenSummary,
            detailLevel = detailLevel,
            conversationHistory = conversationHistory
        )
    }
}
