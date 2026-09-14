package com.example.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messageCount: Int = 0,
    val lastSnippet: String = ""
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val hasScreenContext: Boolean = false,
    val hasAudioContext: Boolean = false,
    val citationsJson: String? = null,
    val actionsJson: String? = null,
    val status: String = "COMPLETE" // "COMPLETE", "ERROR", "CANCELLED"
)

@Entity(tableName = "app_access_rules")
data class AppAccessEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val category: String, // BANKING_FINANCE, PASSWORD_MANAGER, HEALTH_MEDICAL, IDENTITY_GOV, GENERAL
    val accessLevel: String, // ALLOWED, ASK_EVERY_TIME, NEVER_ALLOW
    val lastAccessed: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String, // SCREEN_CAPTURE_REQUEST, AUDIO_START, KILL_TRIGGERED, RULE_BLOCKED, ACTION_EXECUTED
    val details: String,
    val riskLevel: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
