package com.example.actions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import com.example.data.repository.AIopterRepository
import com.example.model.ActionStatus
import com.example.model.ActionType
import com.example.model.SafeAction
import com.example.security.PermissionAndSafetyManager

object ActionExecutor {

    fun execute(
        context: Context,
        action: SafeAction,
        safetyManager: PermissionAndSafetyManager,
        repository: AIopterRepository,
        onResult: (Boolean, String) -> Unit
    ) {
        // Enforce safety authorization check for critical actions
        if (safetyManager.isTwoStepRequired(action)) {
            val token = action.approvalToken
            if (token == null || !safetyManager.verifyAndConsumeApprovalToken(action.id, token)) {
                val denialMessage = "Execution rejected: Two-step authorization token is missing or expired"
                onResult(false, denialMessage)
                return
            }
        }

        try {
            when (action.actionType) {
                ActionType.OPEN_MAPS -> {
                    val query = action.payload["query"] ?: "coffee"
                    val mapIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                        onResult(true, "Launched Maps navigation for $query")
                    } else {
                        // Fallback to web maps
                        val webMapIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(webMapIntent)
                        onResult(true, "Opened Maps search in browser for $query")
                    }
                }

                ActionType.OPEN_URL -> {
                    val url = action.payload["url"] ?: "https://www.google.com"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    onResult(true, "Opened URL: $url")
                }

                ActionType.CREATE_CALENDAR -> {
                    val title = action.payload["title"] ?: "AIopter Event"
                    val description = action.payload["description"] ?: ""
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        data = CalendarContract.Events.CONTENT_URI
                        putExtra(CalendarContract.Events.TITLE, title)
                        putExtra(CalendarContract.Events.DESCRIPTION, description)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    onResult(true, "Opened system calendar to review and save '$title'")
                }

                ActionType.COMPOSE_MESSAGE -> {
                    val body = action.payload["body"] ?: ""
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:")
                        putExtra("sms_body", body)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    onResult(true, "Prepared message draft in SMS application")
                }

                ActionType.SHARE_TEXT -> {
                    val text = action.payload["text"] ?: ""
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, text)
                        type = "text/plain"
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share with").apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(shareIntent)
                    onResult(true, "Opened system share sheet")
                }

                ActionType.COPY_TEXT -> {
                    val text = action.payload["text"] ?: ""
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("AIopter Response", text)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    onResult(true, "Copied text to clipboard")
                }

                ActionType.ALLOW_SCREEN_ONCE -> {
                    val pkg = action.payload["package_name"] ?: ""
                    if (pkg.isNotEmpty()) {
                        safetyManager.grantOneTimeAppAccess(pkg)
                    }
                    onResult(true, "Screen context granted for this session")
                }
            }
        } catch (e: Exception) {
            onResult(false, "Failed to execute action: ${e.message}")
        }
    }
}
