package com.example.ui.overlay

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.StopScreenShare
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiRequestState
import com.example.model.MicrophoneState
import com.example.model.ScreenCaptureState
import com.example.service.AIopterSessionManager
import com.example.ui.components.AIopterRotorView
import com.example.ui.components.CitationsList
import com.example.ui.components.SafeActionCard
import com.example.ui.components.SystemStatusRow
import com.example.ui.theme.AIopterBorderDark
import com.example.ui.theme.AIopterCyan
import com.example.ui.theme.AIopterCyanLight
import com.example.ui.theme.AIopterKillRed
import com.example.ui.theme.AIopterNavyDark
import com.example.ui.theme.AIopterSurfaceDark
import com.example.ui.theme.AIopterSurfaceVariantDark
import com.example.ui.theme.AIopterWarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayPanelContent(
    sessionManager: AIopterSessionManager,
    onCollapse: () -> Unit,
    onOpenMainApp: () -> Unit
) {
    val context = LocalContext.current
    val sessionState by sessionManager.sessionState.collectAsState()
    val citations by sessionManager.activeCitations.collectAsState()
    val proposedAction by sessionManager.activeProposedAction.collectAsState()
    val history by sessionManager.panelHistory.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll when new streaming chunks or messages appear
    LaunchedEffect(sessionState.activeStreamingText, history.size) {
        if (history.isNotEmpty() || sessionState.activeStreamingText.isNotEmpty()) {
            listState.animateScrollToItem((history.size + 1).coerceAtLeast(0))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AIopterSurfaceDark),
        border = BorderStroke(1.5.dp, AIopterCyan.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // 1. Top Control Bar
            val isPanelProcessing = sessionState.aiRequestState == AiRequestState.SENDING ||
                    sessionState.aiRequestState == AiRequestState.STREAMING ||
                    sessionState.aiRequestState == AiRequestState.EXECUTING ||
                    sessionState.microphoneState == MicrophoneState.TRANSCRIBING

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AIopterRotorView(
                        size = 32.dp,
                        isSpinning = isPanelProcessing
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "AIopter",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Assistant Overlay",
                            fontSize = 10.sp,
                            color = AIopterCyanLight
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Open full main app
                    IconButton(
                        onClick = onOpenMainApp,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Open AIopter Home",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Minimize / Collapse back to rotor bubble
                    IconButton(
                        onClick = onCollapse,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Minimize to bubble",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Emergency STOP / KILL switch
                    Surface(
                        color = AIopterKillRed,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { sessionManager.triggerKillSwitch() }
                                .padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.PowerSettingsNew,
                                contentDescription = "Emergency Kill Switch",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "KILL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Real-time Subsystem Status Row
            SystemStatusRow(
                screenCaptureState = sessionState.screenCaptureState,
                microphoneState = sessionState.microphoneState,
                networkState = sessionState.networkState
            )

            // 3. Sensitive Screen Warning / Block Banner
            if (sessionState.screenCaptureState == ScreenCaptureState.BLOCKED_FOR_APP) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = AIopterKillRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AIopterKillRed)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = AIopterKillRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Screen Context Protected",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                            Text(
                                text = sessionState.activeError ?: "Sensitive app detected. Transmission blocked.",
                                fontSize = 10.sp,
                                color = Color(0xFFFFB3B8)
                            )
                        }

                        // Allow This Time button for Ask Every Time
                        val currentPkg = sessionState.currentPackageName
                        if (currentPkg != null) {
                            Button(
                                onClick = { sessionManager.allowSensitiveAppThisTime(currentPkg) },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AIopterWarningAmber),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Allow Once", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Conversation & Streaming Response Viewport
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 280.dp)
                    .background(AIopterNavyDark.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (history.isEmpty() && sessionState.activeStreamingText.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "How can AIopter help you right now?",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ask about your screen, search, plan routes, or tap mic to speak.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Render history
                items(history) { (role, message) ->
                    if (role == "user") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                color = AIopterSurfaceVariantDark,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, AIopterBorderDark)
                            ) {
                                Text(
                                    text = message,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = AIopterSurfaceDark,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, AIopterCyan.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = message,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // Render current in-flight stream
                if (sessionState.activeStreamingText.isNotEmpty()) {
                    item {
                        Surface(
                            color = AIopterSurfaceDark,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, AIopterCyan)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (sessionState.aiRequestState == AiRequestState.STREAMING) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(12.dp),
                                                strokeWidth = 1.5.dp,
                                                color = AIopterCyan
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = if (sessionState.aiRequestState == AiRequestState.STREAMING) "AIopter is responding..." else "AIopter Response",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AIopterCyan
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("AIopter Response", sessionState.activeStreamingText)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Copied response", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Copy response",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = sessionState.activeStreamingText,
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }

                // Render citations
                if (citations.isNotEmpty()) {
                    item {
                        CitationsList(citations = citations)
                    }
                }

                // Render proposed action card (Suggest -> Explain -> Approve -> Execute)
                if (proposedAction != null) {
                    item {
                        SafeActionCard(
                            action = proposedAction!!,
                            onConfirm = { sessionManager.confirmProposedAction(it) },
                            onReject = { sessionManager.rejectProposedAction(it) }
                        )
                    }
                }
            }

            // Progress bar during stream
            if (sessionState.aiRequestState == AiRequestState.STREAMING || sessionState.aiRequestState == AiRequestState.SENDING) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = AIopterCyan,
                    trackColor = AIopterBorderDark
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Bottom Interactive Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Screen share toggle button
                val isScreenOn = sessionState.screenCaptureState == ScreenCaptureState.SHARING
                IconButton(
                    onClick = {
                        sessionManager.toggleScreenSharing(!isScreenOn)
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (isScreenOn) AIopterCyan.copy(alpha = 0.2f) else AIopterBorderDark,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isScreenOn) Icons.Default.ScreenShare else Icons.Default.StopScreenShare,
                        contentDescription = if (isScreenOn) "Stop Screen Context" else "Enable Screen Context",
                        tint = if (isScreenOn) AIopterCyan else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Multi-line prompt input
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Ask AIopter...", fontSize = 12.sp, color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp, max = 80.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AIopterCyan,
                        unfocusedBorderColor = AIopterBorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Microphone Tap-to-Talk button
                val isListening = sessionState.microphoneState == MicrophoneState.LISTENING
                IconButton(
                    onClick = {
                        if (isListening) sessionManager.stopListening() else sessionManager.startListening()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (isListening) AIopterWarningAmber else AIopterBorderDark,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice input",
                        tint = if (isListening) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send or Cancel Generation
                val isStreaming = sessionState.aiRequestState == AiRequestState.STREAMING || sessionState.aiRequestState == AiRequestState.SENDING
                IconButton(
                    onClick = {
                        if (isStreaming) {
                            sessionManager.cancelCurrentAiRequest()
                        } else if (textInput.isNotBlank()) {
                            sessionManager.submitUserPrompt(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (isStreaming) AIopterKillRed else AIopterCyan,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isStreaming) Icons.Default.Stop else Icons.Default.Send,
                        contentDescription = if (isStreaming) "Stop generation" else "Send prompt",
                        tint = if (isStreaming) Color.White else Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
