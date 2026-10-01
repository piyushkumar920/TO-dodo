package com.example.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.NotificationHelper
import com.example.viewmodel.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: RoutineViewModel,
    onBack: () -> Unit = {},
    onOpenRoutine: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentTheme by viewModel.theme.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val taskRemindersEnabled by viewModel.taskRemindersEnabled.collectAsState()
    val upcomingTaskEnabled by viewModel.upcomingTaskEnabled.collectAsState()
    val morningSummaryEnabled by viewModel.morningSummaryEnabled.collectAsState()
    val reminderTimingMinutes by viewModel.reminderTimingMinutes.collectAsState()
    val morningSummaryTime by viewModel.morningSummaryTime.collectAsState()

    var showClearConfirm by remember { mutableStateOf(false) }
    var showResetTodayConfirm by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showReminderTimingDialog by remember { mutableStateOf(false) }
    var showMorningSummaryTimeDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(userName) }

    // Permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setNotificationsEnabled(isGranted)
        if (isGranted) {
            NotificationHelper.showReminderNotification(
                context = context,
                title = "to-dodo ♡",
                message = "Local daily notifications are active. Stay consistent!"
            )
        }
    }

    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            try {
                val json = viewModel.exportDataJson()
                context.contentResolver.openOutputStream(it)?.use { output ->
                    output.write(json.toByteArray())
                }
                Toast.makeText(context, "Data exported successfully! ♡", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonStr = inputStream?.bufferedReader().use { it?.readText() } ?: ""
                val success = viewModel.importDataJson(jsonStr)
                if (success) {
                    Toast.makeText(context, "Data imported successfully! ♡", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Invalid backup format. Existing progress preserved.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = PatrickHandFontFamily),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Preferences
            item {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // My Routine Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenRoutine() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "My Routine", tint = ToDodoOrange)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "My Routine", style = MaterialTheme.typography.titleMedium)
                                    Text(text = "Customize weekly tasks & schedule", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Open Routine", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        // Edit Name Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    nameInput = userName
                                    showEditNameDialog = true
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = "Name", tint = ToDodoOrange)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Your Name", style = MaterialTheme.typography.titleMedium)
                                    Text(text = "$userName ♡", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        // Theme Mode Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showThemeDialog = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Palette, contentDescription = "Theme", tint = ToDodoYellow)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Appearance", style = MaterialTheme.typography.titleMedium)
                                    Text(text = currentTheme.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Select Theme", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                    }
                }
            }

            // Section: Notifications
            item {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Notifications Master Switch Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = ToDodoOrange)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Notifications", style = MaterialTheme.typography.titleMedium)
                                    Text(text = "Local routine alerts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { enable ->
                                    if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.setNotificationsEnabled(enable)
                                    }
                                },
                                modifier = Modifier.testTag("notifications_switch")
                            )
                        }

                        if (notificationsEnabled) {
                            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            // Task Reminders Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Task reminders", style = MaterialTheme.typography.titleMedium)
                                    Text(text = "Remind before scheduled tasks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = taskRemindersEnabled,
                                    onCheckedChange = { viewModel.setTaskRemindersEnabled(it) },
                                    modifier = Modifier.testTag("task_reminders_switch")
                                )
                            }

                            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            // Upcoming Task Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Upcoming task", style = MaterialTheme.typography.titleMedium)
                                    Text(text = "Alert for next scheduled task", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = upcomingTaskEnabled,
                                    onCheckedChange = { viewModel.setUpcomingTaskEnabled(it) },
                                    modifier = Modifier.testTag("upcoming_task_switch")
                                )
                            }

                            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            // Morning Summary Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Morning summary", style = MaterialTheme.typography.titleMedium)
                                    Text(text = "Daily morning overview of planned tasks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = morningSummaryEnabled,
                                    onCheckedChange = { viewModel.setMorningSummaryEnabled(it) },
                                    modifier = Modifier.testTag("morning_summary_switch")
                                )
                            }

                            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            // Reminder Timing Selector Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showReminderTimingDialog = true }
                                    .padding(16.dp)
                                    .testTag("reminder_timing_row"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Reminder timing", style = MaterialTheme.typography.titleMedium)
                                    val timingText = if (reminderTimingMinutes <= 0) "At task start" else "$reminderTimingMinutes minutes before"
                                    Text(text = timingText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = "Select Reminder Timing", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            // Morning Summary Time Selector Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showMorningSummaryTimeDialog = true }
                                    .padding(16.dp)
                                    .testTag("morning_summary_time_row"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Morning summary time", style = MaterialTheme.typography.titleMedium)
                                    val summaryTimeFormatted = when (morningSummaryTime) {
                                        "07:00" -> "7:00 AM"
                                        "07:30" -> "7:30 AM"
                                        "08:00" -> "8:00 AM"
                                        "08:30" -> "8:30 AM"
                                        "09:00" -> "9:00 AM"
                                        else -> morningSummaryTime
                                    }
                                    Text(text = summaryTimeFormatted, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = "Select Summary Time", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Section: Data & Backup
            item {
                Text(
                    text = "Data & Backup",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Export
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { exportLauncher.launch("to_dodo_backup_${System.currentTimeMillis()}.json") }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Download, contentDescription = "Export", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Export Data", style = MaterialTheme.typography.titleMedium)
                                    Text(text = "Save local backup file (.json)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Export", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        // Import
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { importLauncher.launch(arrayOf("application/json")) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Upload, contentDescription = "Import", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Import Data", style = MaterialTheme.typography.titleMedium)
                                    Text(text = "Restore progress from backup file", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Import", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        // Reset Today
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showResetTodayConfirm = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset Today", tint = ToDodoOrange)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Reset Today's Tasks", style = MaterialTheme.typography.titleMedium)
                                    Text(text = "Uncheck tasks for today only", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Reset", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        // Clear All Progress
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showClearConfirm = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DeleteForever, contentDescription = "Clear All", tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Clear All Progress", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                                    Text(text = "Erase all completion records permanently", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // About Card
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
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "to-dodo ♡",
                            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = PatrickHandFontFamily),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Small steps. Big dreams. ♡",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Designed and developed by Piyush Kumar",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Department of CSE (AI/ML)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "🔒 100% offline. Zero tracking, zero cloud dependencies.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Name Edit Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Your Name", style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Enter name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            viewModel.setUserName(nameInput.trim())
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

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Appearance") },
            text = {
                Column {
                    listOf("system", "light", "dark").forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setTheme(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == mode,
                                onClick = {
                                    viewModel.setTheme(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = mode.replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Today Confirmation
    if (showResetTodayConfirm) {
        AlertDialog(
            onDismissRequest = { showResetTodayConfirm = false },
            title = { Text("Reset Today's Tasks?") },
            text = { Text("This will uncheck today's tasks only. All other days and history will remain untouched.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetTodayTasks()
                        showResetTodayConfirm = false
                        Toast.makeText(context, "Today's tasks reset.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Reset Today")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetTodayConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Progress Confirmation
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Progress?") },
            text = { Text("This action will permanently delete all your completion records and reset streaks. Your schedule definitions will stay safe. This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllProgress()
                        showClearConfirm = false
                        Toast.makeText(context, "All progress cleared.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reminder Timing Dialog
    if (showReminderTimingDialog) {
        val timingOptions = listOf(
            0 to "At task start",
            5 to "5 minutes before",
            10 to "10 minutes before",
            15 to "15 minutes before",
            30 to "30 minutes before"
        )
        AlertDialog(
            onDismissRequest = { showReminderTimingDialog = false },
            title = { Text("Reminder Timing", style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    timingOptions.forEach { (minutes, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setReminderTimingMinutes(minutes)
                                    showReminderTimingDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (reminderTimingMinutes == minutes),
                                onClick = {
                                    viewModel.setReminderTimingMinutes(minutes)
                                    showReminderTimingDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReminderTimingDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Morning Summary Time Dialog
    if (showMorningSummaryTimeDialog) {
        val timeOptions = listOf(
            "07:00" to "7:00 AM",
            "07:30" to "7:30 AM",
            "08:00" to "8:00 AM",
            "08:30" to "8:30 AM",
            "09:00" to "9:00 AM"
        )
        AlertDialog(
            onDismissRequest = { showMorningSummaryTimeDialog = false },
            title = { Text("Morning Summary Time", style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    timeOptions.forEach { (timeStr, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setMorningSummaryTime(timeStr)
                                    showMorningSummaryTimeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (morningSummaryTime == timeStr),
                                onClick = {
                                    viewModel.setMorningSummaryTime(timeStr)
                                    showMorningSummaryTimeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMorningSummaryTimeDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}
