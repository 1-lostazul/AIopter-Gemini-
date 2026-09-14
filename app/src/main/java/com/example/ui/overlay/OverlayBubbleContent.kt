package com.example.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.model.AiRequestState
import com.example.model.MicrophoneState
import com.example.model.ScreenCaptureState
import com.example.ui.components.AIopterRotorView
import com.example.ui.theme.AIopterCyan
import com.example.ui.theme.AIopterKillRed
import com.example.ui.theme.AIopterNavyDark
import com.example.ui.theme.AIopterWarningAmber

/**
 * The official AIopter blue/cyan three-part propeller floating overlay.
 * The propeller itself is the floating control (transparent background, subtle shadow/glow).
 * Communicates Idle vs Processing states directly via smooth rotation.
 * Features distinct badges for active Screen Sharing and Microphone states.
 */
@Composable
fun OverlayBubbleContent(
    screenCaptureState: ScreenCaptureState,
    microphoneState: MicrophoneState,
    aiRequestState: AiRequestState,
    onBubbleClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Propeller rotates while AIopter is thinking or processing requests
    val isProcessing = aiRequestState == AiRequestState.SENDING ||
            aiRequestState == AiRequestState.STREAMING ||
            aiRequestState == AiRequestState.EXECUTING ||
            microphoneState == MicrophoneState.TRANSCRIBING

    val isScreenSharing = screenCaptureState == ScreenCaptureState.SHARING
    val isListening = microphoneState == MicrophoneState.LISTENING
    val isBlocked = screenCaptureState == ScreenCaptureState.BLOCKED_FOR_APP

    Surface(
        shape = CircleShape,
        color = Color.Transparent,
        modifier = Modifier
            .size(64.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onBubbleClick
            )
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            // The official AIopter blue/cyan three-part propeller
            AIopterRotorView(
                size = 56.dp,
                isSpinning = isProcessing,
                isScreenSharingActive = isScreenSharing,
                isListening = isListening,
                isBlockedOrAlert = isBlocked,
                hasBackingDisk = false
            )

            // Visible Screen-Sharing indicator badge beside the propeller
            AnimatedVisibility(
                visible = isScreenSharing,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = (-2).dp, y = 2.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(AIopterNavyDark),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(AIopterCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenShare,
                            contentDescription = "Screen Context Active",
                            tint = Color.Black,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            // Visible Microphone/Listening indicator badge beside the propeller
            AnimatedVisibility(
                visible = isListening,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = (-2).dp, y = (-2).dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(AIopterNavyDark),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(AIopterWarningAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Microphone Active",
                            tint = Color.Black,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            // Visible Sensitive Screen Blocked / Privacy Shield indicator
            AnimatedVisibility(
                visible = isBlocked,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = 2.dp, y = 2.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(AIopterNavyDark),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(AIopterKillRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Sensitive App Shielded",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
}
