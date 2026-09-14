package com.example.net

import com.example.model.ActionStatus
import com.example.model.ActionType
import com.example.model.RiskLevel
import com.example.model.SafeAction
import com.example.model.WebCitation
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ChatMessageDto(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class ChatRequestDto(
    @Json(name = "prompt") val prompt: String,
    @Json(name = "screen_summary") val screenSummary: String? = null,
    @Json(name = "screen_image_base64") val screenImageBase64: String? = null,
    @Json(name = "detail_level") val detailLevel: String = "balanced",
    @Json(name = "history") val history: List<ChatMessageDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class WebCitationDto(
    @Json(name = "title") val title: String,
    @Json(name = "url") val url: String,
    @Json(name = "snippet") val snippet: String? = null,
    @Json(name = "domain") val domain: String? = null
)

@JsonClass(generateAdapter = true)
data class ActionDto(
    @Json(name = "id") val id: String?,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "risk_level") val riskLevel: String, // "READ_ONLY", "LOW", "MODERATE", "CRITICAL"
    @Json(name = "action_type") val actionType: String,
    @Json(name = "payload") val payload: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class ChatResponseDto(
    @Json(name = "text") val text: String,
    @Json(name = "citations") val citations: List<WebCitationDto>? = null,
    @Json(name = "proposed_action") val proposedAction: ActionDto? = null
)

interface AIopterBackendApi {
    @POST
    suspend fun sendChatRequest(
        @Url endpointUrl: String,
        @Body request: ChatRequestDto
    ): ChatResponseDto
}

object AIopterApiClient {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC // Never log prompt bodies or private screen contexts
            }
        )
        .build()

    val api: AIopterBackendApi = Retrofit.Builder()
        .baseUrl("https://ai.aiopter.companion/") // Dynamic full @Url used at runtime
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(AIopterBackendApi::class.java)

    fun mapToDomainAction(dto: ActionDto): SafeAction {
        val mappedRisk = when (dto.riskLevel.uppercase()) {
            "READ_ONLY", "LEVEL_0" -> RiskLevel.LEVEL_0_READ_LOCAL
            "LOW", "LEVEL_1" -> RiskLevel.LEVEL_1_NAVIGATION
            "MODERATE", "LEVEL_2" -> RiskLevel.LEVEL_2_EXTERNAL_CHANGE
            else -> RiskLevel.LEVEL_3_SENSITIVE
        }
        val mappedType = runCatching { ActionType.valueOf(dto.actionType.uppercase()) }
            .getOrDefault(ActionType.OPEN_URL)

        return SafeAction(
            id = dto.id ?: java.util.UUID.randomUUID().toString(),
            title = dto.title,
            description = dto.description,
            riskLevel = mappedRisk,
            actionType = mappedType,
            payload = dto.payload ?: emptyMap(),
            status = ActionStatus.PROPOSED
        )
    }

    fun mapToDomainCitation(dto: WebCitationDto): WebCitation {
        return WebCitation(
            title = dto.title,
            url = dto.url,
            snippet = dto.snippet,
            domain = dto.domain ?: ""
        )
    }
}
