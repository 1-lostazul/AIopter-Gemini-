package com.example.model

enum class ActionRiskLevel(val title: String, val level: Int) {
    READ_ONLY("Read Only", 0),
    LOW_REVERSIBLE("Low / Reversible", 1),
    MODERATE_EXTERNAL("Moderate / External", 2),
    CRITICAL_CONFIRMATION_REQUIRED("Critical / Sensitive", 3)
}

enum class RiskLevel(val level: Int, val displayName: String, val confirmationRule: String) {
    LEVEL_0_READ_LOCAL(0, "Level 0 - Read/Local", "No extra confirmation beyond user request"),
    LEVEL_1_NAVIGATION(1, "Level 1 - Navigation", "User taps the suggested action to launch standard intent"),
    LEVEL_2_EXTERNAL_CHANGE(2, "Level 2 - External Change", "Preview + explicit user confirmation required"),
    LEVEL_3_SENSITIVE(3, "Level 3 - Sensitive Access", "Two-step approval / 'Allow This Time' gate"),
    BLOCKED(4, "Blocked", "Bypass attempt or disallowed automation - execution prohibited")
}

enum class ActionType {
    OPEN_MAPS,
    OPEN_URL,
    CREATE_CALENDAR,
    COMPOSE_MESSAGE,
    ALLOW_SCREEN_ONCE,
    SHARE_TEXT,
    COPY_TEXT
}

enum class ActionStatus {
    PROPOSED,
    AWAITING_CONFIRMATION,
    AWAITING_SECOND_CONFIRMATION,
    CONFIRMED,
    EXECUTED,
    CANCELLED,
    REJECTED
}

data class WebCitation(
    val title: String,
    val url: String,
    val snippet: String? = null,
    val domain: String = ""
)

data class ManagedApp(
    val packageName: String,
    val appName: String,
    val isSensitiveDefault: Boolean,
    val accessLevel: AccessLevel,
    val category: String
)

data class SafeAction(
    val id: String,
    val title: String,
    val description: String,
    val riskLevel: RiskLevel,
    val actionType: ActionType,
    val payload: Map<String, String> = emptyMap(),
    val status: ActionStatus = ActionStatus.PROPOSED,
    val approvalToken: String? = null
) {
    val explanation: String get() = description
    val parameters: Map<String, String> get() = payload

    val actionRiskLevel: ActionRiskLevel
        get() = when (riskLevel) {
            RiskLevel.LEVEL_0_READ_LOCAL -> ActionRiskLevel.READ_ONLY
            RiskLevel.LEVEL_1_NAVIGATION -> ActionRiskLevel.LOW_REVERSIBLE
            RiskLevel.LEVEL_2_EXTERNAL_CHANGE -> ActionRiskLevel.MODERATE_EXTERNAL
            RiskLevel.LEVEL_3_SENSITIVE, RiskLevel.BLOCKED -> ActionRiskLevel.CRITICAL_CONFIRMATION_REQUIRED
        }
}
