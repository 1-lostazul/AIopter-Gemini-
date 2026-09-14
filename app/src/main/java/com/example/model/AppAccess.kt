package com.example.model

enum class AccessLevel(val title: String, val description: String) {
    ALLOWED("Allowed", "Screen context permitted when global Screen Sharing switch is ON"),
    ASK_EVERY_TIME("Ask Every Time", "Requires explicit 'Allow This Time' prompt before context transmission"),
    NEVER_ALLOW("Never Allow", "Screen context strictly blocked; transmission suppressed")
}

enum class AppCategory(val title: String, val defaultAccess: AccessLevel) {
    BANKING_FINANCE("Banking & Finance", AccessLevel.ASK_EVERY_TIME),
    PASSWORD_MANAGER("Password Managers", AccessLevel.NEVER_ALLOW),
    HEALTH_MEDICAL("Health & Medical", AccessLevel.ASK_EVERY_TIME),
    IDENTITY_GOV("Identity & IDs", AccessLevel.ASK_EVERY_TIME),
    GENERAL("General", AccessLevel.ALLOWED)
}

data class AppRule(
    val packageName: String,
    val appName: String,
    val category: AppCategory,
    val accessLevel: AccessLevel,
    val lastAccessedTimestamp: Long = System.currentTimeMillis()
)
