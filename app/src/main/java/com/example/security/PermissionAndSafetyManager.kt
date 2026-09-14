package com.example.security

import com.example.data.repository.AIopterRepository
import com.example.model.AccessLevel
import com.example.model.ActionRiskLevel
import com.example.model.RiskLevel
import com.example.model.SafeAction
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

sealed class ScreenAccessCheckResult {
    data class Allowed(val packageName: String?, val isOneTimeGrant: Boolean) : ScreenAccessCheckResult()
    data class Blocked(val packageName: String?, val reason: String) : ScreenAccessCheckResult()
    data class NeedsOneTimeConfirmation(val packageName: String, val appName: String) : ScreenAccessCheckResult()
    data class UndeterminedForegroundApp(val message: String) : ScreenAccessCheckResult()
}

data class ActionApprovalToken(
    val token: String,
    val actionId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 60_000L // 60 seconds validity
) {
    fun isValid(forActionId: String): Boolean {
        return actionId == forActionId && System.currentTimeMillis() <= expiresAt
    }
}

class PermissionAndSafetyManager(private val repository: AIopterRepository) {

    private val sessionOneTimeAppGrants = ConcurrentHashMap.newKeySet<String>()
    private val pendingCriticalApprovalTokens = ConcurrentHashMap<String, ActionApprovalToken>()

    /**
     * Evaluates whether screen context capture may proceed for the given foreground package.
     * Enforces strict "fail-closed" policy: if app cannot be determined, explicit confirmation is required.
     */
    suspend fun evaluateScreenAccess(
        isScreenSharingGloballyEnabled: Boolean,
        detectedPackage: String?
    ): ScreenAccessCheckResult {
        if (!isScreenSharingGloballyEnabled) {
            return ScreenAccessCheckResult.Blocked(detectedPackage, "Global Screen Sharing switch is OFF")
        }

        if (detectedPackage == null) {
            // Fail closed: cannot verify foreground app safety
            return ScreenAccessCheckResult.UndeterminedForegroundApp(
                "Unable to identify active foreground app. Confirm access before sharing screen."
            )
        }

        // Rule lookup
        val rule = repository.getRuleForPackage(detectedPackage)

        return when (rule) {
            AccessLevel.NEVER_ALLOW -> {
                repository.logAudit(
                    "RULE_ENFORCED_NEVER_ALLOW",
                    "Screen capture blocked for protected app: $detectedPackage",
                    riskLevel = 2
                )
                ScreenAccessCheckResult.Blocked(detectedPackage, "Blocked: App is restricted by Never Allow security policy")
            }

            AccessLevel.ASK_EVERY_TIME -> {
                if (sessionOneTimeAppGrants.contains(detectedPackage)) {
                    ScreenAccessCheckResult.Allowed(detectedPackage, isOneTimeGrant = true)
                } else {
                    ScreenAccessCheckResult.NeedsOneTimeConfirmation(
                        packageName = detectedPackage,
                        appName = detectedPackage.substringAfterLast('.')
                    )
                }
            }

            AccessLevel.ALLOWED -> {
                ScreenAccessCheckResult.Allowed(detectedPackage, isOneTimeGrant = false)
            }
        }
    }

    fun grantOneTimeAppAccess(packageName: String) {
        sessionOneTimeAppGrants.add(packageName)
    }

    fun clearOneTimeGrants() {
        sessionOneTimeAppGrants.clear()
    }

    /**
     * Two-Step Authorization flow for Safe Actions:
     * - READ_ONLY or LOW_REVERSIBLE: 1 step approval.
     * - MODERATE_EXTERNAL or CRITICAL: Generates a short-lived token on Step 1 (Review & Confirm),
     *   which must be provided on Step 2 (Allow This Time).
     */
    fun isTwoStepRequired(action: SafeAction): Boolean {
        return action.riskLevel == RiskLevel.LEVEL_2_EXTERNAL_CHANGE ||
                action.riskLevel == RiskLevel.LEVEL_3_SENSITIVE ||
                action.actionRiskLevel == ActionRiskLevel.MODERATE_EXTERNAL ||
                action.actionRiskLevel == ActionRiskLevel.CRITICAL_CONFIRMATION_REQUIRED
    }

    fun issueApprovalToken(actionId: String): ActionApprovalToken {
        val token = ActionApprovalToken(
            token = UUID.randomUUID().toString(),
            actionId = actionId
        )
        pendingCriticalApprovalTokens[actionId] = token
        return token
    }

    fun verifyAndConsumeApprovalToken(actionId: String, providedToken: String): Boolean {
        val existing = pendingCriticalApprovalTokens[actionId] ?: return false
        if (existing.token == providedToken && existing.isValid(actionId)) {
            pendingCriticalApprovalTokens.remove(actionId)
            return true
        }
        return false
    }

    fun invalidateActionTokens(actionId: String? = null) {
        if (actionId != null) {
            pendingCriticalApprovalTokens.remove(actionId)
        } else {
            pendingCriticalApprovalTokens.clear()
        }
    }

    /**
     * Emergency Kill Switch cleanup
     */
    fun resetOnEmergencyKill() {
        sessionOneTimeAppGrants.clear()
        pendingCriticalApprovalTokens.clear()
    }

    fun sanitizeActionParameters(params: Map<String, String>): Map<String, String> {
        return params.mapValues { (key, value) ->
            DataSanitizer.maskParameterValue(key, value)
        }
    }
}
