package com.example.ai

import com.example.data.preferences.AIopterPreferences
import com.example.net.AIopterApiClient
import com.example.net.ChatMessageDto
import com.example.net.ChatRequestDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AIopterBackendAiProvider(private val preferences: AIopterPreferences) : AiProvider {

    override val providerId: String = "aiopter-backend-secure"
    override val displayName: String = "AIopter Secure HTTPS Backend"

    override fun streamResponse(
        prompt: String,
        screenSummary: String?,
        detailLevel: String,
        conversationHistory: List<Pair<String, String>>
    ): Flow<AiStreamChunk> = flow {
        val endpoint = preferences.backendEndpointUrl.trim()

        if (endpoint.isEmpty() || (!endpoint.startsWith("https://") && !endpoint.startsWith("http://"))) {
            emit(
                AiStreamChunk(
                    textDelta = "AI service not configured.\n\nAIopter requires a secure HTTPS backend to process multimodal AI requests while protecting your privacy and preventing credentials from leaking in the client APK.\n\nPlease configure your backend endpoint URL in AI Settings.",
                    fullText = "AI service not configured.\n\nAIopter requires a secure HTTPS backend to process multimodal AI requests while protecting your privacy and preventing credentials from leaking in the client APK.\n\nPlease configure your backend endpoint URL in AI Settings.",
                    citations = emptyList(),
                    proposedAction = null,
                    isComplete = true,
                    errorMessage = "AI backend service not configured"
                )
            )
            return@flow
        }

        emit(
            AiStreamChunk(
                textDelta = "Connecting to AIopter secure service...",
                fullText = "Connecting to AIopter secure service...",
                isComplete = false
            )
        )

        try {
            val requestDto = ChatRequestDto(
                prompt = prompt,
                screenSummary = screenSummary,
                screenImageBase64 = null, // Ephemeral: attach only when explicitly authorized
                detailLevel = detailLevel,
                history = conversationHistory.map { (role, content) ->
                    ChatMessageDto(role = role, content = content)
                }
            )

            val response = AIopterApiClient.api.sendChatRequest(endpoint, requestDto)
            val fullText = response.text
            val citations = response.citations?.map { AIopterApiClient.mapToDomainCitation(it) } ?: emptyList()
            val action = response.proposedAction?.let { AIopterApiClient.mapToDomainAction(it) }

            // Stream response words for a natural reading cadence
            val words = fullText.split(" ")
            val currentSb = StringBuilder()

            for (i in words.indices) {
                if (i > 0) currentSb.append(" ")
                currentSb.append(words[i])

                val isLast = i == words.size - 1
                emit(
                    AiStreamChunk(
                        textDelta = words[i] + if (isLast) "" else " ",
                        fullText = currentSb.toString(),
                        citations = if (isLast) citations else emptyList(),
                        proposedAction = if (isLast) action else null,
                        isComplete = isLast
                    )
                )
                delay(20)
            }
        } catch (e: Exception) {
            val errorMsg = "Unable to connect to AI service: ${e.localizedMessage ?: "Network error"}"
            emit(
                AiStreamChunk(
                    textDelta = errorMsg,
                    fullText = errorMsg,
                    isComplete = true,
                    errorMessage = errorMsg
                )
            )
        }
    }
}
