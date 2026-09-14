package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.preferences.AIopterPreferences
import com.example.ui.components.AIopterRotorView
import com.example.ui.theme.AIopterBorderDark
import com.example.ui.theme.AIopterCyan
import com.example.ui.theme.AIopterCyanLight
import com.example.ui.theme.AIopterKillRed
import com.example.ui.theme.AIopterNavyDark
import com.example.ui.theme.AIopterSuccessGreen
import com.example.ui.theme.AIopterSurfaceDark
import com.example.ui.theme.AIopterSurfaceVariantDark
import com.example.ui.theme.AIopterWarningAmber

@Composable
fun OnboardingScreen(
    preferences: AIopterPreferences,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 7

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AIopterNavyDark)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with Step Indicator
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Setup AIopter",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AIopterCyanLight
                    )
                    Text(
                        text = "Step $currentStep of $totalSteps",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = currentStep / totalSteps.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AIopterCyan,
                    trackColor = AIopterBorderDark
                )
            }

            // Step Content Area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AIopterSurfaceDark),
                border = BorderStroke(1.dp, AIopterBorderDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (currentStep) {
                        1 -> OnboardingStepWelcome()
                        2 -> OnboardingStepOverlay(context)
                        3 -> OnboardingStepNotifications()
                        4 -> OnboardingStepMicrophone()
                        5 -> OnboardingStepScreenSharing()
                        6 -> OnboardingStepPrivacy()
                        7 -> OnboardingStepChecklist(context)
                    }
                }
            }

            // Navigation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Back", color = Color.White)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (currentStep < totalSteps) {
                            currentStep++
                        } else {
                            preferences.isOnboardingCompleted = true
                            onComplete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AIopterCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (currentStep == totalSteps) "Get Started with AIopter" else "Continue",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingStepWelcome() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AIopterRotorView(size = 80.dp, isSpinning = true)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Welcome to AIopter",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "A native Android AI assistant designed to remain available while you work in other apps.",
            fontSize = 13.sp,
            color = Color(0xFFCBD5E1),
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            color = AIopterSurfaceVariantDark,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, AIopterBorderDark)
        ) {
            Text(
                text = "Product Principle: Suggest -> Explain -> Approve -> Execute",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AIopterCyanLight,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
fun OnboardingStepOverlay(context: Context) {
    val hasOverlay = Settings.canDrawOverlays(context)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Layers, contentDescription = null, tint = AIopterCyan, modifier = Modifier.size(54.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Display Over Other Apps",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "This allows the official blue/cyan AIopter rotor bubble to float quietly above your other apps, ready when you tap it.",
            fontSize = 12.sp,
            color = Color(0xFFCBD5E1),
            lineHeight = 17.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        if (!hasOverlay) {
            Button(
                onClick = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AIopterCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Open Settings to Grant Permission", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AIopterSuccessGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Permission Granted", color = AIopterSuccessGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OnboardingStepNotifications() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Notifications, contentDescription = null, tint = AIopterCyan, modifier = Modifier.size(54.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Notification Transparency",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "AIopter displays a persistent notification whenever the floating assistant is active so you are never surprised. It includes an Emergency STOP / KILL button right in the shade.",
            fontSize = 12.sp,
            color = Color(0xFFCBD5E1),
            lineHeight = 17.sp
        )
    }
}

@Composable
fun OnboardingStepMicrophone() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Mic, contentDescription = null, tint = AIopterCyan, modifier = Modifier.size(54.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tap-to-Talk Microphone",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "AIopter does NOT perform background hotword listening. Your microphone is only activated on-demand when you explicitly tap the mic button.",
            fontSize = 12.sp,
            color = Color(0xFFCBD5E1),
            lineHeight = 17.sp
        )
    }
}

@Composable
fun OnboardingStepScreenSharing() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.ScreenShare, contentDescription = null, tint = AIopterCyan, modifier = Modifier.size(54.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Screen Context & Protection",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Screen sharing is OFF by default. When toggled ON, capture buffers are ephemeral. Sensitive apps (banking, passwords, medical) are protected with explicit 'Allow This Time' or 'Never Allow' gates.",
            fontSize = 12.sp,
            color = Color(0xFFCBD5E1),
            lineHeight = 17.sp
        )
    }
}

@Composable
fun OnboardingStepPrivacy() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Security, contentDescription = null, tint = AIopterCyan, modifier = Modifier.size(54.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your Data, Your Control",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "You can wipe all conversation history, preferences, and cached rules at any time with a single tap in the Privacy & Permissions menu.",
            fontSize = 12.sp,
            color = Color(0xFFCBD5E1),
            lineHeight = 17.sp
        )
    }
}

@Composable
fun OnboardingStepChecklist(context: Context) {
    val hasOverlay = Settings.canDrawOverlays(context)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AIopterSuccessGreen, modifier = Modifier.size(54.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "AIopter is Ready",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChecklistRow("Overlay Display", hasOverlay)
            ChecklistRow("Microphone Protection", true)
            ChecklistRow("Sensitive App Shield", true)
            ChecklistRow("Emergency KILL Switch", true)
        }
    }
}

@Composable
fun ChecklistRow(title: String, ready: Boolean) {
    Surface(
        color = AIopterSurfaceVariantDark,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AIopterBorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 12.sp, color = Color.White)
            Icon(
                imageVector = if (ready) Icons.Default.Check else Icons.Default.Security,
                contentDescription = null,
                tint = if (ready) AIopterSuccessGreen else AIopterWarningAmber,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
