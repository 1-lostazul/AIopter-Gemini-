package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.AIopterSessionManager
import com.example.ui.components.AIopterRotorView
import com.example.ui.screens.AccountPlanScreen
import com.example.ui.screens.AiSettingsScreen
import com.example.ui.screens.AppsAccessScreen
import com.example.ui.screens.HelpSafetyScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PrivacyPermissionsScreen
import com.example.ui.theme.AIopterBorderDark
import com.example.ui.theme.AIopterCyan
import com.example.ui.theme.AIopterCyanLight
import com.example.ui.theme.AIopterKillRed
import com.example.ui.theme.AIopterNavyDark
import com.example.ui.theme.AIopterSurfaceDark
import com.example.ui.theme.MyApplicationTheme

enum class MainNavDestination(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Home", Icons.Default.Home),
    APPS_ACCESS("Apps", Icons.Default.Apps),
    PRIVACY("Privacy", Icons.Default.Security),
    AI_SETTINGS("AI", Icons.Default.Tune),
    ACCOUNT("Account", Icons.Default.AccountCircle),
    HELP("Help", Icons.Default.HelpOutline)
}

class MainActivity : ComponentActivity() {

    private lateinit var sessionManager: AIopterSessionManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sessionManager = AIopterSessionManager.getInstance(this)
        val preferences = sessionManager.repository.preferences

        setContent {
            MyApplicationTheme(darkTheme = true) {
                var isOnboardingDone by remember { mutableStateOf(preferences.isOnboardingCompleted) }
                var currentDestination by remember { mutableStateOf(MainNavDestination.HOME) }

                if (!isOnboardingDone) {
                    OnboardingScreen(
                        preferences = preferences,
                        onComplete = { isOnboardingDone = true }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = AIopterNavyDark,
                        topBar = {
                            TopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = AIopterSurfaceDark,
                                    titleContentColor = Color.White
                                ),
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AIopterRotorView(size = 28.dp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "AIopter",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 17.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = currentDestination.title,
                                                fontSize = 10.sp,
                                                color = AIopterCyanLight
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    // Global Emergency STOP (KILL) button
                                    Surface(
                                        color = AIopterKillRed,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .padding(end = 12.dp)
                                            .height(30.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .testTag("emergency_kill_button")
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
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = AIopterSurfaceDark,
                                contentColor = AIopterCyan
                            ) {
                                MainNavDestination.values().forEach { dest ->
                                    val isSelected = currentDestination == dest
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentDestination = dest },
                                        icon = {
                                            Icon(
                                                dest.icon,
                                                contentDescription = dest.title,
                                                tint = if (isSelected) AIopterCyan else Color.Gray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = dest.title,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else Color.Gray
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = AIopterCyan.copy(alpha = 0.15f)
                                        )
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentDestination) {
                                MainNavDestination.HOME -> HomeScreen(
                                    sessionManager = sessionManager,
                                    onNavigateToAppsAccess = { currentDestination = MainNavDestination.APPS_ACCESS },
                                    onNavigateToPrivacy = { currentDestination = MainNavDestination.PRIVACY }
                                )
                                MainNavDestination.APPS_ACCESS -> AppsAccessScreen(
                                    repository = sessionManager.repository
                                )
                                MainNavDestination.PRIVACY -> PrivacyPermissionsScreen(
                                    repository = sessionManager.repository
                                )
                                MainNavDestination.AI_SETTINGS -> AiSettingsScreen(
                                    preferences = sessionManager.repository.preferences
                                )
                                MainNavDestination.ACCOUNT -> AccountPlanScreen()
                                MainNavDestination.HELP -> HelpSafetyScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

