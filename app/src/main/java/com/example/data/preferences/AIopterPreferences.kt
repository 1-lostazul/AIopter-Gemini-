package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences

class AIopterPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("aiopter_prefs", Context.MODE_PRIVATE)

    var isOverlayEnabled: Boolean
        get() = prefs.getBoolean(KEY_OVERLAY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, value).apply()

    var isScreenSharingEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCREEN_SHARING_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SCREEN_SHARING_ENABLED, value).apply()

    var isVoiceInputEnabled: Boolean
        get() = prefs.getBoolean(KEY_VOICE_INPUT_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VOICE_INPUT_ENABLED, value).apply()

    var isSpokenRepliesEnabled: Boolean
        get() = prefs.getBoolean(KEY_SPOKEN_REPLIES_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SPOKEN_REPLIES_ENABLED, value).apply()

    var isProactiveSuggestionsEnabled: Boolean
        get() = prefs.getBoolean(KEY_PROACTIVE_SUGGESTIONS_ENABLED, false) // Default OFF
        set(value) = prefs.edit().putBoolean(KEY_PROACTIVE_SUGGESTIONS_ENABLED, value).apply()

    var isConversationHistoryEnabled: Boolean
        get() = prefs.getBoolean(KEY_CONVERSATION_HISTORY_ENABLED, false) // Default OFF per privacy doctrine
        set(value) = prefs.edit().putBoolean(KEY_CONVERSATION_HISTORY_ENABLED, value).apply()

    var responseDetail: String
        get() = prefs.getString(KEY_RESPONSE_DETAIL, "balanced") ?: "balanced"
        set(value) = prefs.edit().putString(KEY_RESPONSE_DETAIL, value).apply()

    var backendEndpointUrl: String
        get() = prefs.getString(KEY_BACKEND_ENDPOINT_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_BACKEND_ENDPOINT_URL, value).apply()

    var preferredAiProvider: String
        get() = prefs.getString(KEY_AI_PROVIDER, "backend-secure") ?: "backend-secure"
        set(value) = prefs.edit().putString(KEY_AI_PROVIDER, value).apply()

    var isDebugFakeProviderEnabled: Boolean
        get() = prefs.getBoolean(KEY_DEBUG_FAKE_PROVIDER, false)
        set(value) = prefs.edit().putBoolean(KEY_DEBUG_FAKE_PROVIDER, value).apply()

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var isAnalyticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_ANALYTICS_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_ANALYTICS_ENABLED, value).apply()

    var bubblePosX: Float
        get() = prefs.getFloat(KEY_BUBBLE_POS_X, 80f)
        set(value) = prefs.edit().putFloat(KEY_BUBBLE_POS_X, value).apply()

    var bubblePosY: Float
        get() = prefs.getFloat(KEY_BUBBLE_POS_Y, 200f)
        set(value) = prefs.edit().putFloat(KEY_BUBBLE_POS_Y, value).apply()

    var dockedCorner: String
        get() = prefs.getString(KEY_DOCKED_CORNER, "TOP_RIGHT") ?: "TOP_RIGHT"
        set(value) = prefs.edit().putString(KEY_DOCKED_CORNER, value).apply()

    fun clearAllPreferences() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_OVERLAY_ENABLED = "key_overlay_enabled"
        private const val KEY_SCREEN_SHARING_ENABLED = "key_screen_sharing_enabled"
        private const val KEY_VOICE_INPUT_ENABLED = "key_voice_input_enabled"
        private const val KEY_SPOKEN_REPLIES_ENABLED = "key_spoken_replies_enabled"
        private const val KEY_PROACTIVE_SUGGESTIONS_ENABLED = "key_proactive_suggestions_enabled"
        private const val KEY_CONVERSATION_HISTORY_ENABLED = "key_conversation_history_enabled"
        private const val KEY_RESPONSE_DETAIL = "key_response_detail"
        private const val KEY_BACKEND_ENDPOINT_URL = "key_backend_endpoint_url"
        private const val KEY_AI_PROVIDER = "key_ai_provider"
        private const val KEY_DEBUG_FAKE_PROVIDER = "key_debug_fake_provider"
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
        private const val KEY_ANALYTICS_ENABLED = "key_analytics_enabled"
        private const val KEY_BUBBLE_POS_X = "key_bubble_pos_x"
        private const val KEY_BUBBLE_POS_Y = "key_bubble_pos_y"
        private const val KEY_DOCKED_CORNER = "key_docked_corner"
    }
}
