package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun HelpSafetyScreen() {
    val context = LocalContext.current

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
                    text = "Help & Safety",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Architecture guidelines, transparency, and safety doctrine",
                    fontSize = 12.sp,
                    color = AIopterCyanLight
                )
            }
        }

        // Product Doctrine: Suggest -> Explain -> Approve -> Execute
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AIopterSurfaceDark),
                border = BorderStroke(1.5.dp, AIopterCyan)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "The AIopter Safety Principle",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Suggest -> Explain -> Approve -> Execute",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = AIopterCyan
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PrincipleStep(
                        step = "1. Suggest",
                        description = "The assistant identifies relevant actions or answers without executing automatically."
                    )
                    PrincipleStep(
                        step = "2. Explain",
                        description = "Every proposed action details its exact destination, payload, and risk level."
                    )
                    PrincipleStep(
                        step = "3. Approve",
                        description = "The user explicitly reviews and taps to authorize or decline."
                    )
                    PrincipleStep(
                        step = "4. Execute",
                        description = "The OS handles execution via standard Android intents with full sandbox isolation."
                    )
                }
            }
        }

        // What AIopter Can and Cannot See
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AIopterSurfaceDark),
                border = BorderStroke(1.dp, AIopterBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = AIopterCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("What AIopter Can & Cannot See", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Visible when authorized:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AIopterSuccessGreen)
                    Text("• On-demand screen capture when Screen Sharing switch is explicitly toggled ON\n• Text layout and visual elements in non-sensitive apps\n• Audio spoken while mic button is active", fontSize = 11.sp, color = Color(0xFFCBD5E1), lineHeight = 15.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Strictly inaccessible (Suppressed):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AIopterKillRed)
                    Text("• Any app on your 'Never Allow' list\n• System FLAG_SECURE screens (banking PINs, password entries)\n• Background audio or continuous listening when mic is idle\n• Files or personal storage outside the authorized app context", fontSize = 11.sp, color = Color(0xFFCBD5E1), lineHeight = 15.sp)
                }
            }
        }

        // Emergency KILL Switch Mechanics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AIopterSurfaceDark),
                border = BorderStroke(1.dp, AIopterKillRed.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = AIopterKillRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("The Emergency KILL Switch", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The KILL button is prominently located in the panel header, notification shade, and Home screen. When pressed, it instantly:\n\n" +
                                "1. Shuts down active MediaProjection screen capture\n" +
                                "2. Cancels all in-flight AI streaming requests immediately\n" +
                                "3. Destroys active microphone speech audio sessions\n" +
                                "4. Discards all in-memory capture buffers\n" +
                                "5. Closes and unbinds the floating overlay",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Diagnostics & Report a Problem
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AIopterSurfaceDark),
                border = BorderStroke(1.dp, AIopterBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Diagnostics & Feedback", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Submit diagnostic reports or report unexpected behavior.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "System diagnostics clean • Version 1.0 Production Baseline", Toast.LENGTH_LONG).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Run Subsystem Self-Diagnostics")
                    }
                }
            }
        }
    }
}

@Composable
fun PrincipleStep(step: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(step, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AIopterCyanLight)
        Text(description, fontSize = 11.sp, color = Color(0xFFCBD5E1), modifier = Modifier.padding(start = 8.dp))
    }
}
