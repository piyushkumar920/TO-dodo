package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ToDodoMascot
import com.example.ui.theme.*
import com.example.viewmodel.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: RoutineViewModel,
    onOpenSettings: () -> Unit,
    onOpenRoutine: () -> Unit = {}
) {
    val userName by viewModel.userName.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val creditScore by viewModel.creditScore.collectAsState()
    val todayProgress by viewModel.todayProgress.collectAsState()
    val weekDays by viewModel.currentWeekDays.collectAsState()

    val totalWeeklyCompleted = weekDays.sumOf { it.completed }
    val totalWeeklyTasks = weekDays.sumOf { it.total }
    val weeklyPercentage = if (totalWeeklyTasks > 0) ((totalWeeklyCompleted.toFloat() / totalWeeklyTasks) * 100).toInt() else 0

    var showEditNameDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf(userName) }
    var showGoalsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = PatrickHandFontFamily),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Card with Avatar & Name
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(ToDodoYellowLight),
                            contentAlignment = Alignment.Center
                        ) {
                            ToDodoMascot(size = 75.dp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                newNameInput = userName
                                showEditNameDialog = true
                            }
                        ) {
                            Text(
                                text = "$userName ♡",
                                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = PatrickHandFontFamily),
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Name",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Better than yesterday",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ProfileStatItem(count = todayProgress.completed.toString(), label = "Tasks")
                            ProfileStatItem(count = "$currentStreak", label = "Streak")
                            ProfileStatItem(count = "⭐ $creditScore", label = "Credit")
                            ProfileStatItem(count = "$weeklyPercentage%", label = "Week")
                        }
                    }
                }
            }

            // Menu Items
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ProfileMenuRow(
                            icon = Icons.Default.CalendarMonth,
                            title = "My Routine",
                            onClick = onOpenRoutine
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ProfileMenuRow(
                            icon = Icons.Default.EmojiEvents,
                            title = "My Goals",
                            onClick = { showGoalsDialog = true }
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ProfileMenuRow(
                            icon = Icons.Default.Settings,
                            title = "Settings",
                            onClick = onOpenSettings
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ProfileMenuRow(
                            icon = Icons.Default.Info,
                            title = "About to-dodo",
                            onClick = { showAboutDialog = true }
                        )
                    }
                }
            }

            // Inspiring quote footer card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ToDodoYellowLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "♡", fontSize = 22.sp, color = ToDodoOrange)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "A little progress every day adds up to big results ♡",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ToDodoTextDark
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = {
                Text(
                    text = "What should we call you?",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = newNameInput,
                    onValueChange = { newNameInput = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNameInput.isNotBlank()) {
                            viewModel.setUserName(newNameInput.trim())
                        }
                        showEditNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ToDodoYellow)
                ) {
                    Text("Save ♡", color = ToDodoTextDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // My Goals Dialog
    if (showGoalsDialog) {
        AlertDialog(
            onDismissRequest = { showGoalsDialog = false },
            title = {
                Text(
                    text = "My Routine Goals",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "🎯 Maintain 70%+ daily completion for streak", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "📚 Deep Study sessions every weekday", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "🎸 Guitar & Flute daily practice", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "🏃 Consistent exercise Monday–Saturday", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "📖 30 mins nightly reading before bed", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { showGoalsDialog = false }) {
                    Text("Got it! ♡")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(
                    text = "to-dodo ♡",
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = PatrickHandFontFamily),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "A warm, cute, handwritten routine companion.", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Designed and developed by Piyush Kumar\nDepartment of CSE (AI/ML)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text(text = "🔒 100% offline. Stored locally on this device.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ProfileStatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = PatrickHandFontFamily),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ToDodoYellowLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = ToDodoTextDark, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Icon(Icons.Default.ChevronRight, contentDescription = "Navigate", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
