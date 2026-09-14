package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AIopterRepository
import com.example.ui.theme.AIopterBorderDark
import com.example.ui.theme.AIopterCyan
import com.example.ui.theme.AIopterCyanLight
import com.example.ui.theme.AIopterKillRed
import com.example.ui.theme.AIopterNavyDark
import com.example.ui.theme.AIopterSuccessGreen
import com.example.ui.theme.AIopterSurfaceDark
import com.example.ui.theme.AIopterSurfaceVariantDark
import com.example.ui.theme.AIopterWarningAmber
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PrivacyPermissionsScreen(
    repository: AIopterRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auditLogs by repository.recentAuditLogs.collectAsState(initial = emptyList())
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val hasOverlayPermission = Settings.canDrawOverlays(context)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AIopterNavyDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "Privacy & Permissions",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "AIopter should feel trustworthy before it feels powerful",
                    fontSize = 12.sp,
                    color = AIopterCyanLight
                )
            }
        }

        // 2. Progressive Permissions Status
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AIopterSurfaceDark),
                border = BorderStroke(1.dp, AIopterBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "System Permissions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    PermissionItem(
                        title = "Display over other apps",
                        description = "Enables the floating rotor bubble to remain available above other apps",
                        granted = hasOverlayPermission,
                        icon = Icons.Default.Layers,
                        onFix = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PermissionItem(
                        title = "Microphone (Tap-to-Talk)",
                        description = "Used solely on-demand when you tap the mic button. No background hotword listening.",
                        granted = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED,
                        icon = Icons.Default.Mic,
                        onFix = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PermissionItem(
                        title = "Notifications & Transparency",
                        description = "Provides persistent foreground service status and immediate KILL switch in notification shade.",
                        granted = true,
                        icon = Icons.Default.Notifications,
                        onFix = {}
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PermissionItem(
                        title = "MediaProjection (Screen Sharing)",
                        description = "Granted on-demand with explicit prompt. Ephemeral frames; never stored permanently.",
                        granted = repository.preferences.isScreenSharingEnabled,
                        icon = Icons.Default.ScreenShare,
                        onFix = {}
                    )
                }
            }
        }

        // 3. Plain-English Disclosures
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AIopterSurfaceDark),
                border = BorderStroke(1.dp, AIopterBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = AIopterCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Privacy Disclosures",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    DisclosureBullet("Ephemeral Screen Context", "When screen sharing is active, screenshots are analyzed in-memory and discarded immediately after processing. AIopter never creates permanent local video recordings.")
                    DisclosureBullet("Zero Hotword Listening", "The microphone is inactive until you tap the mic icon or select voice input. Speech buffers are streamed exclusively for speech-to-text.")
                    DisclosureBullet("Suggest -> Explain -> Approve -> Execute", "AIopter never performs external actions, calendar events, messages, or sensitive navigations silently. You always see the preview and tap to approve.")
                    DisclosureBullet("FLAG_SECURE & Banking Gate", "Apps that prohibit capture (e.g. banking logins, password vaults) are automatically blocked and gated behind explicit user permission.")
                }
            }
        }

        // 4. Data Retention & 1-Tap Wipe
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AIopterSurfaceDark),
                border = BorderStroke(1.dp, AIopterKillRed.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data Retention & Control",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You maintain complete ownership of all local history, assistant logs, and preferences. You can wipe all data at any moment.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showClearConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AIopterKillRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All Local Data Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // 5. Recent Audit Logs
        if (auditLogs.isNotEmpty()) {
            item {
                Text(
                    text = "Security Audit Trail",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            items(auditLogs.take(6)) { log ->
                val dateStr = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = AIopterSurfaceVariantDark),
                    border = BorderStroke(1.dp, AIopterBorderDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.eventType,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = when (log.riskLevel) {
                                    3 -> AIopterKillRed
                                    2 -> AIopterWarningAmber
                                    else -> AIopterCyan
                                }
                            )
                            Text(
                                text = log.details,
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                        Text(
                            text = dateStr,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = AIopterSurfaceDark,
            title = { Text("Wipe All Local Data?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will delete all conversation histories, audit logs, cached rules, and reset preferences. This action cannot be undone.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.clearAllUserData()
                            showClearConfirmDialog = false
                            Toast.makeText(context, "All local data has been erased", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AIopterKillRed)
                ) {
                    Text("Erase Everything", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    granted: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onFix: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        if (granted) AIopterSuccessGreen.copy(alpha = 0.15f) else AIopterWarningAmber.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (granted) AIopterSuccessGreen else AIopterWarningAmber,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp
                )
            }
        }

        if (!granted) {
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedButton(
                onClick = onFix,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Grant", fontSize = 11.sp)
            }
        } else {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Granted",
                tint = AIopterSuccessGreen,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun DisclosureBullet(title: String, body: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "• $title",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = AIopterCyanLight
        )
        Text(
            text = body,
            fontSize = 11.sp,
            color = Color(0xFFCBD5E1),
            lineHeight = 15.sp,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}
