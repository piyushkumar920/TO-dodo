package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TaskEntity
import com.example.ui.components.AddEditTaskDialog
import com.example.ui.components.SleepingCat
import com.example.ui.theme.*
import com.example.util.RoutineTimeEngine
import com.example.viewmodel.RoutineViewModel

private val DAYS = listOf(
    "MONDAY" to "Monday",
    "TUESDAY" to "Tuesday",
    "WEDNESDAY" to "Wednesday",
    "THURSDAY" to "Thursday",
    "FRIDAY" to "Friday",
    "SATURDAY" to "Saturday",
    "SUNDAY" to "Sunday"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineManagementScreen(
    viewModel: RoutineViewModel,
    onBack: () -> Unit
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val customCategories by viewModel.customCategories.collectAsState()
    var selectedDay by remember { mutableStateOf("MONDAY") }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Filter active tasks for the selected day, sorted chronologically by start time
    val dayTasks = remember(allTasks, selectedDay) {
        allTasks.filter { task ->
            task.effectiveUntilDate == null && task.repeatsOn(selectedDay)
        }.sortedWith(compareBy({ RoutineTimeEngine.toLogicalMinutes(it.startTime) }, { it.sortOrder }))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Routine ♡",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ToDodoYellow,
                contentColor = ToDodoTextDark,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Task",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Day Selector Pills Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(DAYS) { (dayKey, dayLabel) ->
                    val isSelected = selectedDay == dayKey
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) ToDodoYellow else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) ToDodoTextDark else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .clickable { selectedDay = dayKey }
                            .testTag("routine_day_tab_$dayKey")
                    ) {
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) ToDodoTextDark else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task list for selected day
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (dayTasks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            SleepingCat(size = 90.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "🌿 Free day!",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No tasks scheduled for this day. Tap + Add Task to create one ♡",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(dayTasks, key = { it.id }) { task ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { taskToEdit = task }
                                .testTag("routine_task_item_${task.id}"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (task.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (!task.isEnabled) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = Color(0xFFE9ECEF),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "DISABLED",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFF495057),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = if (task.endTime.isNotEmpty()) "${task.startTime} – ${task.endTime}" else task.startTime,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Surface(
                                            color = getCategoryPastelColor(task.category),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = task.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = ToDodoTextDark,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                // Enable/Disable Switch
                                Switch(
                                    checked = task.isEnabled,
                                    onCheckedChange = { enabled ->
                                        viewModel.toggleTaskEnabled(task.id, enabled)
                                    },
                                    modifier = Modifier.testTag("toggle_switch_${task.id}")
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(onClick = { taskToEdit = task }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit task",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddDialog) {
        AddEditTaskDialog(
            taskToEdit = null,
            defaultDay = selectedDay,
            existingTasks = dayTasks,
            customCategories = customCategories,
            onAddCustomCategory = { name, cb -> viewModel.addCustomCategory(name, cb) },
            onDismiss = { showAddDialog = false },
            onSave = { title, category, startTime, endTime, daysOfWeek, reminderEnabled ->
                viewModel.addCustomTask(
                    title = title,
                    category = category,
                    startTime = startTime,
                    endTime = endTime,
                    daysOfWeek = daysOfWeek,
                    reminderEnabled = reminderEnabled
                )
            }
        )
    }

    // Edit Task Dialog
    taskToEdit?.let { task ->
        AddEditTaskDialog(
            taskToEdit = task,
            defaultDay = selectedDay,
            existingTasks = dayTasks,
            customCategories = customCategories,
            onAddCustomCategory = { name, cb -> viewModel.addCustomCategory(name, cb) },
            onDismiss = { taskToEdit = null },
            onSave = { title, category, startTime, endTime, daysOfWeek, reminderEnabled ->
                viewModel.editCustomTask(
                    existingTask = task,
                    newTitle = title,
                    newCategory = category,
                    newStartTime = startTime,
                    newEndTime = endTime,
                    newDaysOfWeek = daysOfWeek,
                    newReminderEnabled = reminderEnabled
                )
            },
            onDelete = { taskToDelete ->
                viewModel.deleteCustomTask(taskToDelete)
            }
        )
    }
}
