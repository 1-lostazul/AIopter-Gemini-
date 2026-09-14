package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.StopScreenShare
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionRiskLevel
import com.example.model.ActionStatus
import com.example.model.ActionType
import com.example.model.MicrophoneState
import com.example.model.NetworkState
import com.example.model.RiskLevel
import com.example.model.SafeAction
import com.example.model.ScreenCaptureState
import com.example.model.WebCitation
import com.example.security.DataSanitizer
import com.example.ui.theme.AIopterBorderDark
import com.example.ui.theme.AIopterCyan
import com.example.ui.theme.AIopterCyanLight
import com.example.ui.theme.AIopterKillRed
import com.example.ui.theme.AIopterSuccessGreen
import com.example.ui.theme.AIopterSurfaceVariantDark
import com.example.ui.theme.AIopterWarningAmber

@Composable
fun SystemStatusRow(
    screenCaptureState: ScreenCaptureState,
    microphoneState: MicrophoneState,
    networkState: NetworkState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Screen status
        StatusChip(
            label = when (screenCaptureState) {
                ScreenCaptureState.SHARING -> "Screen: ON"
                ScreenCaptureState.BLOCKED_FOR_APP -> "Screen: BLOCKED"
                ScreenCaptureState.REQUESTING_PERMISSION -> "Screen: AUTH"
                else -> "Screen: OFF"
            },
            icon = if (screenCaptureState == ScreenCaptureState.SHARING) Icons.Default.ScreenShare else Icons.Default.StopScreenShare,
            active = screenCaptureState == ScreenCaptureState.SHARING,
            isAlert = screenCaptureState == ScreenCaptureState.BLOCKED_FOR_APP
        )

        // Mic status
        StatusChip(
            label = when (microphoneState) {
                MicrophoneState.LISTENING -> "Mic: LISTENING"
                MicrophoneState.TRANSCRIBING -> "Mic: PROCESSING"
                MicrophoneState.REQUESTING_PERMISSION -> "Mic: AUTH"
                MicrophoneState.ERROR -> "Mic: ERROR"
                else -> "Mic: IDLE"
            },
            icon = if (microphoneState == MicrophoneState.LISTENING) Icons.Default.Mic else Icons.Default.MicOff,
            active = microphoneState == MicrophoneState.LISTENING || microphoneState == MicrophoneState.TRANSCRIBING,
            isAlert = microphoneState == MicrophoneState.ERROR
        )

        // Network status
        StatusChip(
            label = if (networkState == NetworkState.ONLINE) "Online" else "Offline",
            icon = if (networkState == NetworkState.ONLINE) Icons.Default.Wifi else Icons.Default.WifiOff,
            active = networkState == NetworkState.ONLINE,
            isAlert = networkState == NetworkState.OFFLINE
        )
    }
}

@Composable
fun StatusChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    isAlert: Boolean = false
) {
    val chipBg = when {
        isAlert -> AIopterKillRed.copy(alpha = 0.2f)
        active -> AIopterCyan.copy(alpha = 0.2f)
        else -> Color.Black.copy(alpha = 0.3f)
    }
    val chipColor = when {
        isAlert -> AIopterKillRed
        active -> AIopterCyan
        else -> Color.Gray
    }

    Surface(
        color = chipBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, chipColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = chipColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = chipColor
            )
        }
    }
}

@Composable
fun SafeActionCard(
    action: SafeAction,
    onConfirm: (SafeAction) -> Unit,
    onReject: (SafeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCritical = action.riskLevel == RiskLevel.LEVEL_2_EXTERNAL_CHANGE ||
            action.riskLevel == RiskLevel.LEVEL_3_SENSITIVE ||
            action.actionRiskLevel == ActionRiskLevel.CRITICAL_CONFIRMATION_REQUIRED

    val borderColor = when (action.riskLevel) {
        RiskLevel.LEVEL_0_READ_LOCAL -> AIopterCyan
        RiskLevel.LEVEL_1_NAVIGATION -> AIopterCyan
        RiskLevel.LEVEL_2_EXTERNAL_CHANGE -> AIopterWarningAmber
        RiskLevel.LEVEL_3_SENSITIVE -> AIopterKillRed
        RiskLevel.BLOCKED -> Color.Red
    }

    val isSecondStep = action.status == ActionStatus.AWAITING_SECOND_CONFIRMATION

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = AIopterSurfaceVariantDark
        ),
        border = BorderStroke(1.5.dp, if (isSecondStep) AIopterKillRed else borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header with risk badge and warning icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when {
                        isCritical -> Icons.Default.Warning
                        action.actionType == ActionType.OPEN_MAPS -> Icons.Default.Directions
                        action.actionType == ActionType.CREATE_CALENDAR -> Icons.Default.Event
                        action.actionType == ActionType.COMPOSE_MESSAGE -> Icons.Default.Send
                        action.actionType == ActionType.OPEN_URL -> Icons.Default.Link
                        else -> Icons.Default.Info
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = borderColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = action.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Surface(
                    color = borderColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isSecondStep) "Step 2: Final Auth" else action.riskLevel.displayName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = borderColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = action.description,
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1)
            )

            // Parameters with sensitive data masked
            if (action.payload.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Action Parameters:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8)
                    )
                    action.payload.forEach { (key, value) ->
                        val masked = DataSanitizer.maskParameterValue(key, value)
                        Text(
                            text = "• $key: $masked",
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isSecondStep) {
                    "Critical Step: A temporary 60-second authorization token was issued. Confirm to allow this single execution."
                } else {
                    "Rule: ${action.riskLevel.confirmationRule}"
                },
                fontSize = 11.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = if (isSecondStep) AIopterWarningAmber else Color(0xFF94A3B8)
            )

            if (action.status == ActionStatus.PROPOSED ||
                action.status == ActionStatus.AWAITING_CONFIRMATION ||
                action.status == ActionStatus.AWAITING_SECOND_CONFIRMATION
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onReject(action) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Decline action", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Decline", fontSize = 12.sp)
                    }

                    val buttonLabel = when {
                        isSecondStep -> "Allow This Time"
                        isCritical -> "Review & Confirm"
                        action.riskLevel == RiskLevel.LEVEL_1_NAVIGATION -> "Launch App"
                        else -> "Approve & Run"
                    }

                    Button(
                        onClick = { onConfirm(action) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSecondStep) AIopterKillRed else borderColor,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = buttonLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (action.status == ActionStatus.EXECUTED) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = AIopterSuccessGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Action executed successfully", fontSize = 11.sp, color = AIopterSuccessGreen)
                }
            }
        }
    }
}

@Composable
fun CitationsList(
    citations: List<WebCitation>,
    modifier: Modifier = Modifier
) {
    if (citations.isEmpty()) return
    val uriHandler = LocalUriHandler.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Verified Sources:",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            citations.take(3).forEach { citation ->
                Surface(
                    color = AIopterBorderDark,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable {
                        runCatching { uriHandler.openUri(citation.url) }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Link,
                            contentDescription = null,
                            tint = AIopterCyanLight,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = citation.domain.ifEmpty { citation.title.take(15) },
                            fontSize = 10.sp,
                            color = AIopterCyanLight,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
