package com.example.ai

import com.example.model.ActionStatus
import com.example.model.ActionType
import com.example.model.RiskLevel
import com.example.model.SafeAction
import com.example.model.WebCitation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

/**
 * INTERNAL DEBUG ONLY:
 * Used exclusively for local UI preview and regression testing when explicitly enabled by the user.
 * Explicitly announces its simulated nature in output text.
 */
class DebugFakeAiProvider : AiProvider {

    override val providerId: String = "debug-fake-local"
    override val displayName: String = "Internal Debug Mock (Offline Demo Only)"

    override fun streamResponse(
        prompt: String,
        screenSummary: String?,
        detailLevel: String,
        conversationHistory: List<Pair<String, String>>
    ): Flow<AiStreamChunk> = flow {
        val disclaimer = "[DEBUG / OFFLINE DEMO MODE]\n"
        val lower = prompt.lowercase()

        val sampleText = when {
            screenSummary != null -> "$disclaimer Analyzed temporary screen context: $screenSummary. I suggest reviewing the details above."
            lower.contains("coffee") || lower.contains("map") -> "$disclaimer Here are nearby coffee shops. You can tap below to view them in Maps."
            lower.contains("calendar") || lower.contains("meeting") -> "$disclaimer Prepared calendar entry preview: 'Team Sync'. Tap below to review and confirm."
            else -> "$disclaimer Received: '$prompt'. This is a simulated offline debug response."
        }

        var sampleAction: SafeAction? = null
        if (lower.contains("coffee") || lower.contains("map")) {
            sampleAction = SafeAction(
                id = UUID.randomUUID().toString(),
                title = "Open Maps",
                description = "Launch Google Maps to view local coffee shops",
                riskLevel = RiskLevel.LEVEL_1_NAVIGATION,
                actionType = ActionType.OPEN_MAPS,
                payload = mapOf("query" to "coffee near me"),
                status = ActionStatus.PROPOSED
            )
        } else if (lower.contains("calendar") || lower.contains("meeting")) {
            sampleAction = SafeAction(
                id = UUID.randomUUID().toString(),
                title = "Schedule Meeting",
                description = "Add 'Team Sync' to your device calendar",
                riskLevel = RiskLevel.LEVEL_2_EXTERNAL_CHANGE,
                actionType = ActionType.CREATE_CALENDAR,
                payload = mapOf("title" to "Team Sync", "description" to "AIopter synchronized meeting"),
                status = ActionStatus.PROPOSED
            )
        }

        val words = sampleText.split(" ")
        val currentSb = StringBuilder()

        for (i in words.indices) {
            if (i > 0) currentSb.append(" ")
            currentSb.append(words[i])
            val isLast = i == words.size - 1

            emit(
                AiStreamChunk(
                    textDelta = words[i] + if (isLast) "" else " ",
                    fullText = currentSb.toString(),
                    citations = emptyList(),
                    proposedAction = if (isLast) sampleAction else null,
                    isComplete = isLast
                )
            )
            delay(25)
        }
    }
}
