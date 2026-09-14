package com.example.data.repository

import android.content.Context
import com.example.data.db.AIopterDatabase
import com.example.data.db.AppAccessEntity
import com.example.data.db.AuditLogEntity
import com.example.data.db.ChatMessageEntity
import com.example.data.db.ConversationEntity
import com.example.data.preferences.AIopterPreferences
import com.example.model.AccessLevel
import com.example.model.AppCategory
import com.example.model.AppRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class AIopterRepository(private val context: Context) {
    private val db = AIopterDatabase.getInstance(context)
    val preferences = AIopterPreferences(context)

    val conversations: Flow<List<ConversationEntity>> = db.conversationDao().getAllConversations()
    val appRules: Flow<List<AppRule>> = db.appAccessDao().getAllRules().map { entities ->
        entities.map { entity ->
            AppRule(
                packageName = entity.packageName,
                appName = entity.appName,
                category = runCatching { AppCategory.valueOf(entity.category) }.getOrDefault(AppCategory.GENERAL),
                accessLevel = runCatching { AccessLevel.valueOf(entity.accessLevel) }.getOrDefault(AccessLevel.ALLOWED),
                lastAccessedTimestamp = entity.lastAccessed
            )
        }
    }
    val recentAuditLogs: Flow<List<AuditLogEntity>> = db.auditLogDao().getRecentLogs()

    init {
        // Seed default sensitive application rules
        CoroutineScope(Dispatchers.IO).launch {
            seedDefaultRulesIfNeeded()
        }
    }

    suspend fun seedDefaultRulesIfNeeded() {
        val defaults = listOf(
            AppAccessEntity(
                packageName = "com.chase.sig.android",
                appName = "Chase Mobile",
                category = AppCategory.BANKING_FINANCE.name,
                accessLevel = AccessLevel.ASK_EVERY_TIME.name
            ),
            AppAccessEntity(
                packageName = "com.bankofamerica.activity",
                appName = "Bank of America",
                category = AppCategory.BANKING_FINANCE.name,
                accessLevel = AccessLevel.ASK_EVERY_TIME.name
            ),
            AppAccessEntity(
                packageName = "com.bitwarden.mobile",
                appName = "Bitwarden",
                category = AppCategory.PASSWORD_MANAGER.name,
                accessLevel = AccessLevel.NEVER_ALLOW.name
            ),
            AppAccessEntity(
                packageName = "com.onepassword.android",
                appName = "1Password",
                category = AppCategory.PASSWORD_MANAGER.name,
                accessLevel = AccessLevel.NEVER_ALLOW.name
            ),
            AppAccessEntity(
                packageName = "com.mychart.app",
                appName = "MyChart Health",
                category = AppCategory.HEALTH_MEDICAL.name,
                accessLevel = AccessLevel.ASK_EVERY_TIME.name
            ),
            AppAccessEntity(
                packageName = "gov.login.auth",
                appName = "Login.gov Authenticator",
                category = AppCategory.IDENTITY_GOV.name,
                accessLevel = AccessLevel.ASK_EVERY_TIME.name
            ),
            AppAccessEntity(
                packageName = "com.google.android.apps.docs",
                appName = "Google Docs",
                category = AppCategory.GENERAL.name,
                accessLevel = AccessLevel.ALLOWED.name
            ),
            AppAccessEntity(
                packageName = "com.android.chrome",
                appName = "Chrome Browser",
                category = AppCategory.GENERAL.name,
                accessLevel = AccessLevel.ALLOWED.name
            )
        )
        db.appAccessDao().insertRules(defaults)
    }

    suspend fun getRuleForPackage(packageName: String): AccessLevel {
        val entity = db.appAccessDao().getRuleForPackage(packageName)
        return if (entity != null) {
            runCatching { AccessLevel.valueOf(entity.accessLevel) }.getOrDefault(AccessLevel.ALLOWED)
        } else {
            // Default heuristics for unrecognized packages
            when {
                packageName.contains("bank", ignoreCase = true) ||
                packageName.contains("pay", ignoreCase = true) ||
                packageName.contains("wallet", ignoreCase = true) -> AccessLevel.ASK_EVERY_TIME
                packageName.contains("password", ignoreCase = true) ||
                packageName.contains("vault", ignoreCase = true) ||
                packageName.contains("keeper", ignoreCase = true) -> AccessLevel.NEVER_ALLOW
                else -> AccessLevel.ALLOWED
            }
        }
    }

    suspend fun setRule(rule: AppRule) {
        db.appAccessDao().insertOrUpdateRule(
            AppAccessEntity(
                packageName = rule.packageName,
                appName = rule.appName,
                category = rule.category.name,
                accessLevel = rule.accessLevel.name,
                lastAccessed = System.currentTimeMillis()
            )
        )
        logAudit("RULE_UPDATED", "Rule updated for ${rule.appName} (${rule.packageName}) to ${rule.accessLevel.title}", 1)
    }

    suspend fun removeRule(packageName: String) {
        db.appAccessDao().deleteRule(packageName)
        logAudit("RULE_REMOVED", "Removed rule for $packageName", 1)
    }

    fun getMessages(conversationId: String): Flow<List<ChatMessageEntity>> {
        return db.chatMessageDao().getMessagesForConversation(conversationId)
    }

    suspend fun createConversation(title: String): String {
        val id = UUID.randomUUID().toString()
        val conversation = ConversationEntity(
            id = id,
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            messageCount = 0,
            lastSnippet = ""
        )
        db.conversationDao().insertOrUpdate(conversation)
        return id
    }

    suspend fun saveMessage(
        conversationId: String,
        role: String,
        content: String,
        hasScreen: Boolean = false,
        hasAudio: Boolean = false,
        citationsJson: String? = null,
        actionsJson: String? = null,
        status: String = "COMPLETE"
    ) {
        val message = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = role,
            content = content,
            timestamp = System.currentTimeMillis(),
            hasScreenContext = hasScreen,
            hasAudioContext = hasAudio,
            citationsJson = citationsJson,
            actionsJson = actionsJson,
            status = status
        )
        db.chatMessageDao().insertMessage(message)

        // Update conversation summary
        val existing = db.conversationDao().getConversationById(conversationId)
        if (existing != null) {
            db.conversationDao().insertOrUpdate(
                existing.copy(
                    updatedAt = System.currentTimeMillis(),
                    messageCount = existing.messageCount + 1,
                    lastSnippet = content.take(60)
                )
            )
        }
    }

    suspend fun logAudit(eventType: String, details: String, riskLevel: Int = 0) {
        val sanitizedDetails = com.example.security.DataSanitizer.redact(details)
        db.auditLogDao().insertLog(
            AuditLogEntity(
                eventType = eventType,
                details = sanitizedDetails,
                riskLevel = riskLevel,
                timestamp = System.currentTimeMillis()
            )
        )
        // Trim audit retention periodically to keep database size bounded
        db.auditLogDao().trimRetentionLogs()
    }

    suspend fun clearConversationHistory() {
        db.chatMessageDao().clearAll()
        db.conversationDao().clearAll()
        logAudit("CONVERSATIONS_CLEARED", "User cleared all conversation history", 0)
    }

    suspend fun clearAuditLogs() {
        db.auditLogDao().clearAll()
    }

    suspend fun clearAllUserData() {
        db.chatMessageDao().clearAll()
        db.conversationDao().clearAll()
        db.auditLogDao().clearAll()
        preferences.clearAllPreferences()
        logAudit("DATA_WIPED", "All conversations, logs, and preferences cleared by user", 2)
    }
}
