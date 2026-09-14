package com.example.ui.screens

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import com.example.model.AccessLevel
import com.example.model.AppCategory
import com.example.model.AppRule
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

@Composable
fun AppsAccessScreen(
    repository: AIopterRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allRules by repository.appRules.collectAsState(initial = emptyList())

    var selectedTabIndex by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Allowed", "Ask Every Time", "Never Allow")

    val filteredRules = allRules.filter { rule ->
        val matchesTab = when (selectedTabIndex) {
            0 -> rule.accessLevel == AccessLevel.ALLOWED
            1 -> rule.accessLevel == AccessLevel.ASK_EVERY_TIME
            else -> rule.accessLevel == AccessLevel.NEVER_ALLOW
        }
        val matchesSearch = rule.appName.contains(searchQuery, ignoreCase = true) ||
                rule.packageName.contains(searchQuery, ignoreCase = true)
        matchesTab && (searchQuery.isBlank() || matchesSearch)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AIopterNavyDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Screen Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Apps & Access",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Granular privacy control per installed application",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(AIopterCyan, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add App Rule", tint = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name or package...", fontSize = 12.sp, color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AIopterCyan,
                    unfocusedBorderColor = AIopterBorderDark,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Access Level Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = AIopterSurfaceDark,
                contentColor = AIopterCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = when (selectedTabIndex) {
                            0 -> AIopterSuccessGreen
                            1 -> AIopterWarningAmber
                            else -> AIopterKillRed
                        },
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) Color.White else Color.Gray
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section Explanation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AIopterSurfaceVariantDark),
                border = BorderStroke(1.dp, AIopterBorderDark)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val infoIcon = when (selectedTabIndex) {
                        0 -> Icons.Default.CheckCircle
                        1 -> Icons.Default.Warning
                        else -> Icons.Default.Block
                    }
                    val infoColor = when (selectedTabIndex) {
                        0 -> AIopterSuccessGreen
                        1 -> AIopterWarningAmber
                        else -> AIopterKillRed
                    }
                    Icon(
                        imageVector = infoIcon,
                        contentDescription = null,
                        tint = infoColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (selectedTabIndex) {
                            0 -> "Allowed: Screen context permitted whenever global Screen Sharing switch is ON."
                            1 -> "Ask Every Time: Screen context requires explicit 'Allow This Time' authorization."
                            else -> "Never Allow: Screen context is strictly suppressed. AIopter cannot read these screens."
                        },
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rules List
            if (filteredRules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Apps, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No apps in this category", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredRules) { rule ->
                        AppRuleItemCard(
                            rule = rule,
                            onChangeAccess = { newLevel ->
                                scope.launch {
                                    repository.setRule(rule.copy(accessLevel = newLevel))
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    repository.removeRule(rule.packageName)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Add App Dialog
        if (showAddDialog) {
            AddAppRuleDialog(
                context = context,
                onDismiss = { showAddDialog = false },
                onAddRule = { newRule ->
                    scope.launch {
                        repository.setRule(newRule)
                        showAddDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun AppRuleItemCard(
    rule: AppRule,
    onChangeAccess: (AccessLevel) -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(AIopterBorderDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rule.appName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = AIopterCyan,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = rule.appName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Text(
                        text = "${rule.category.title} • ${rule.packageName.take(24)}...",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Box {
                OutlinedButton(
                    onClick = { showMenu = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = when (rule.accessLevel) {
                            AccessLevel.ALLOWED -> AIopterSuccessGreen
                            AccessLevel.ASK_EVERY_TIME -> AIopterWarningAmber
                            AccessLevel.NEVER_ALLOW -> AIopterKillRed
                        }
                    )
                ) {
                    Text(rule.accessLevel.title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(AIopterSurfaceDark)
                ) {
                    AccessLevel.values().forEach { level ->
                        DropdownMenuItem(
                            text = { Text(level.title, color = Color.White, fontSize = 12.sp) },
                            onClick = {
                                onChangeAccess(level)
                                showMenu = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Remove Rule", color = AIopterKillRed, fontSize = 12.sp) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddAppRuleDialog(
    context: Context,
    onDismiss: () -> Unit,
    onAddRule: (AppRule) -> Unit
) {
    var appName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AppCategory.BANKING_FINANCE) }
    var selectedLevel by remember { mutableStateOf(AccessLevel.ASK_EVERY_TIME) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AIopterSurfaceDark,
        title = { Text("Add App Protection Rule", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("App Name (e.g. Schwab Mobile)", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package Name (e.g. com.schwab)", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Text("Category:", fontSize = 12.sp, color = Color.LightGray)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AppCategory.values().take(3).forEach { cat ->
                        Surface(
                            color = if (selectedCategory == cat) AIopterCyan.copy(alpha = 0.2f) else AIopterSurfaceVariantDark,
                            border = BorderStroke(1.dp, if (selectedCategory == cat) AIopterCyan else AIopterBorderDark),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat.title.split(" ").first(),
                                fontSize = 10.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Text("Access Policy:", fontSize = 12.sp, color = Color.LightGray)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AccessLevel.values().forEach { level ->
                        Surface(
                            color = if (selectedLevel == level) AIopterCyan.copy(alpha = 0.2f) else AIopterSurfaceVariantDark,
                            border = BorderStroke(1.dp, if (selectedLevel == level) AIopterCyan else AIopterBorderDark),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { selectedLevel = level }
                        ) {
                            Text(
                                text = level.title,
                                fontSize = 10.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (appName.isNotBlank() && packageName.isNotBlank()) {
                        onAddRule(
                            AppRule(
                                packageName = packageName.trim(),
                                appName = appName.trim(),
                                category = selectedCategory,
                                accessLevel = selectedLevel
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AIopterCyan)
            ) {
                Text("Add Rule", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
