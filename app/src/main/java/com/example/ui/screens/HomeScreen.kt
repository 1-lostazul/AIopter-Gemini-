package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.OverlayState
import com.example.service.AIopterOverlayService
import com.example.service.AIopterSessionManager
import com.example.ui.components.AIopterRotorView
import com.example.ui.components.SystemStatusRow
import com.example.ui.theme.AIopterBlue
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    sessionManager: AIopterSessionManager,
    onNavigateToAppsAccess: () -> Unit,
    onNavigateToPrivacy: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionState by sessionManager.sessionState.collectAsState()
    val conversations by sessionManager.repository.conversations.collectAsState(initial = emptyList())

    val isOverlayActive = sessionState.overlayState == OverlayState.BUBBLE || sessionState.overlayState == OverlayState.PANEL

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AIopterNavyDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Brand Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AIopterSurfaceDark),
                border = BorderStroke(1.dp, AIopterBorderDark)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Generated Hero Banner Illustration
                    Image(
                        painter = painterResource(id = R.drawable.aiopter_hero_banner_1789341298625),
                        contentDescription = "AIopter Assistant Banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient overlay for smooth readability
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, AIopterSurfaceDark)
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .padding(top = 70.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AIopterRotorView(size = 36.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AIopter",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Native Ambient Android Assistant",
                                    fontSize = 12.sp,
                                    color = AIopterCyanLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "A floating, permissioned AI companion that remains accessible while you work in other apps. Suggest -> Explain -> Approve -> Execute.",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // 2. Master Floating Control Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOverlayActive) AIopterSurfaceVariantDark else AIopterSurfaceDark
                ),
                border = BorderStroke(
                    1.5.dp,
                    if (isOverlayActive) AIopterCyan else AIopterBorderDark
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (isOverlayActive) AIopterCyan.copy(alpha = 0.2f) else AIopterBorderDark,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOverlayActive) Icons.Default.Layers else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isOverlayActive) AIopterCyan else Color.LightGray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Floating Assistant",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isOverlayActive) "Active above other apps" else "Inactive • Tap to start",
                                    fontSize = 12.sp,
                                    color = if (isOverlayActive) AIopterSuccessGreen else Color.Gray
                                )
                            }
                        }

                        Switch(
                            checked = isOverlayActive,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    if (!Settings.canDrawOverlays(context)) {
                                        Toast.makeText(context, "Grant 'Display over other apps' to launch the floating rotor", Toast.LENGTH_LONG).show()
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    } else {
                                        AIopterOverlayService.start(context)
                                        sessionManager.repository.preferences.isOverlayEnabled = true
                                    }
                                } else {
                                    AIopterOverlayService.stop(context)
                                    sessionManager.repository.preferences.isOverlayEnabled = false
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AIopterCyan,
                                checkedTrackColor = AIopterCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = AIopterBorderDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Real-time Subsystem Status indicators
                    SystemStatusRow(
                        screenCaptureState = sessionState.screenCaptureState,
                        microphoneState = sessionState.microphoneState,
                        networkState = sessionState.networkState
                    )

                    if (isOverlayActive) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { sessionManager.triggerKillSwitch() },
                                colors = ButtonDefaults.buttonColors(containerColor = AIopterKillRed),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Emergency STOP (KILL)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Prompt Launchpad
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AIopterSurfaceDark),
                border = BorderStroke(1.dp, AIopterBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Quick Launchpad",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Core Capabilities",
                            fontSize = 11.sp,
                            color = AIopterCyanLight
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionChip("Explain my screen", Icons.Default.ScreenShare) {
                            sessionManager.submitUserPrompt("Explain what is visible on my screen in simple terms")
                            ensureOverlayStarted(context)
                        }
                        QuickActionChip("Scam & Threat Check", Icons.Default.Security) {
                            sessionManager.submitUserPrompt("Analyze this message for scam red flags and phishing indicators")
                            ensureOverlayStarted(context)
                        }
                        QuickActionChip("Simple English Homework", Icons.Default.School) {
                            sessionManager.submitUserPrompt("Explain this concept in simple English step-by-step")
                            ensureOverlayStarted(context)
                        }
                        QuickActionChip("Healthy Recipe & Why", Icons.Default.Restaurant) {
                            sessionManager.submitUserPrompt("Give me a quick healthy recipe with nutrient breakdown")
                            ensureOverlayStarted(context)
                        }
                        QuickActionChip("Search with Sources", Icons.Default.Search) {
                            sessionManager.submitUserPrompt("Find authoritative verified sources on current news")
                            ensureOverlayStarted(context)
                        }
                        QuickActionChip("Nearby Coffee Navigation", Icons.Default.Directions) {
                            sessionManager.submitUserPrompt("Navigate to the best rated coffee shop nearby")
                            ensureOverlayStarted(context)
                        }
                    }
                }
            }
        }

        // 4. Protection & Access Quick Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AIopterSurfaceDark),
                border = BorderStroke(1.dp, AIopterBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Privacy & App Protection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sensitive applications (banking, password managers, medical portals) are protected with explicit approval gates.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToAppsAccess,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Apps & Access", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onNavigateToPrivacy,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Privacy Controls", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 5. Recent Conversations
        if (conversations.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Sessions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            items(conversations.take(4)) { conv ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AIopterSurfaceDark),
                    border = BorderStroke(1.dp, AIopterBorderDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = AIopterCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = conv.title,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = conv.lastSnippet.ifEmpty { "${conv.messageCount} messages" },
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        color = AIopterSurfaceVariantDark,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, AIopterBorderDark),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AIopterCyanLight,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

private fun ensureOverlayStarted(context: Context) {
    if (Settings.canDrawOverlays(context)) {
        AIopterOverlayService.start(context)
    } else {
        Toast.makeText(context, "Grant 'Display over other apps' to view the floating assistant", Toast.LENGTH_LONG).show()
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent)
    }
}
