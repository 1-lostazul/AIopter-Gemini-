package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.AIopterPreferences
import com.example.ui.theme.AIopterBorderDark
import com.example.ui.theme.AIopterCyan
import com.example.ui.theme.AIopterCyanLight
import com.example.ui.theme.AIopterNavyDark
import com.example.ui.theme.AIopterSurfaceDark
import com.example.ui.theme.AIopterSurfaceVariantDark

@Composable
fun AiSettingsScreen(
    preferences: AIopterPreferences
) {
    var responseDetail by remember { mutableStateOf(preferences.responseDetail) }
    var voiceEnabled by remember { mutableStateOf(preferences.isVoiceInputEnabled) }
    var ttsEnabled by remember { mutableStateOf(preferences.isSpokenRepliesEnabled) }
    var proactiveEnabled by remember { mutableStateOf(preferences.isProactiveSuggestionsEnabled) }
    var aiProvider by remember { mutableStateOf(preferences.preferredAiProvider) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AIopterNavyDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "AI Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Tune intelligence, response styles, and speech options",
                    fontSize = 12.sp,
                    color = AIopterCyanLight
                )
            }
        }

        // 1. Response Detail Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AIopterSurfaceDark),
                border = BorderStroke(1.dp, AIopterBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = AIopterCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Response Detail",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Choose how expansive assistant answers should be.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "concise" to "Concise",
                            "balanced" to "Balanced",
                            "detailed" to "Detailed"
                        ).forEach { (key, label) ->
                            val selected = responseDetail == key
                            Surface(
                                color = if (selected) AIopterCyan.copy(alpha = 0.2f) else AIopterSurfaceVariantDark,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (selected) AIopterCyan else AIopterBorderDark),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        responseDetail = key
                                        preferences.responseDetail = key
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Color.White else Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Voice & Speech Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AIopterSurfaceDark),
                border = BorderStroke(1.dp, AIopterBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Voice & Audio",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingToggleRow(
                        title = "Tap-to-Talk Voice Input",
                        description = "Enables microphone button for instant speech recognition",
                        checked = voiceEnabled,
                        onCheckedChange = {
                            voiceEnabled = it
                            preferences.isVoiceInputEnabled = it
                        },
                        icon = Icons.Default.Mic
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Spoken Audio Replies",
                        description = "Reads assistant responses aloud through Text-to-Speech (TTS)",
                        checked = ttsEnabled,
                        onCheckedChange = {
                            ttsEnabled = it
                            preferences.isSpokenRepliesEnabled = it
                        },
                        icon = Icons.Default.RecordVoiceOver
                    )
                }
            }
        }

        // 3. Proactive Suggestions (Default OFF per spec)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AIopterSurfaceDark),
                border = BorderStroke(1.dp, AIopterBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Proactive Suggestions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Per AIopter specification, proactive interruptions are OFF by default to preserve battery and respect your attention.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingToggleRow(
                        title = "Allow Ambient Suggestions",
                        description = "Suggest contextual actions without an explicit prompt when safe to do so",
                        checked = proactiveEnabled,
                        onCheckedChange = {
                            proactiveEnabled = it
                            preferences.isProactiveSuggestionsEnabled = it
                        },
                        icon = Icons.Default.AutoAwesome
                    )
                }
            }
        }

        // 4. Intelligence Routing
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AIopterSurfaceDark),
                border = BorderStroke(1.dp, AIopterBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI Model Engine",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    listOf(
                        "backend-hybrid" to "AIopter Hybrid Orchestrator (Multimodal + Local Safety)",
                        "gemini-flash" to "Gemini 2.5 Flash (Ultra-low latency streaming)",
                        "claude" to "Claude 3.5 Sonnet (Advanced reasoning route)"
                    ).forEach { (providerKey, label) ->
                        val isSelected = aiProvider == providerKey
                        Surface(
                            color = if (isSelected) AIopterCyan.copy(alpha = 0.15f) else AIopterSurfaceVariantDark,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isSelected) AIopterCyan else AIopterBorderDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    aiProvider = providerKey
                                    preferences.preferredAiProvider = providerKey
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(if (isSelected) AIopterCyan else Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Color.Black, CircleShape)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = Color.White
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
fun SettingToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
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
                    .size(36.dp)
                    .background(AIopterSurfaceVariantDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AIopterCyan, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                Text(text = description, fontSize = 11.sp, color = Color.Gray, lineHeight = 14.sp)
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AIopterCyan,
                checkedTrackColor = AIopterCyan.copy(alpha = 0.3f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = AIopterBorderDark
            )
        )
    }
}
