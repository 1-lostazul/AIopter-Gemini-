package com.example.ai

import com.example.model.SafeAction
import com.example.model.WebCitation
import kotlinx.coroutines.flow.Flow

data class AiStreamChunk(
    val textDelta: String = "",
    val fullText: String = "",
    val citations: List<WebCitation> = emptyList(),
    val proposedAction: SafeAction? = null,
    val isComplete: Boolean = false,
    val errorMessage: String? = null
)

interface AiProvider {
    val providerId: String
    val displayName: String

    fun streamResponse(
        prompt: String,
        screenSummary: String? = null,
        detailLevel: String = "balanced",
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): Flow<AiStreamChunk>
}
