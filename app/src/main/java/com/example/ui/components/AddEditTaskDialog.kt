package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.db.TaskEntity
import com.example.ui.screens.getCategoryPastelColor
import com.example.ui.theme.*
import com.example.util.CategoryHelper
import com.example.util.RoutineTimeEngine

private val DAYS_OF_WEEK_ORDER = listOf(
    "MONDAY" to "Mon",
    "TUESDAY" to "Tue",
    "WEDNESDAY" to "Wed",
    "THURSDAY" to "Thu",
    "FRIDAY" to "Fri",
    "SATURDAY" to "Sat",
    "SUNDAY" to "Sun"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditTaskDialog(
    taskToEdit: TaskEntity? = null,
    defaultDay: String = "MONDAY",
    existingTasks: List<TaskEntity> = emptyList(),
    customCategories: List<String> = emptyList(),
    onAddCustomCategory: ((name: String, onResult: (Boolean, String?) -> Unit) -> Unit)? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        category: String,
        startTime: String,
        endTime: String,
        daysOfWeek: Set<String>,
        reminderEnabled: Boolean
    ) -> Unit,
    onDelete: ((TaskEntity) -> Unit)? = null
) {
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    val initialCategory = remember(taskToEdit) {
        if (taskToEdit != null) {
            CategoryHelper.mapLegacyCategory(taskToEdit.category)
        } else {
            CategoryHelper.DEFAULT_CATEGORIES.first()
        }
    }
    var selectedCategory by remember(taskToEdit) { mutableStateOf(initialCategory) }
    var localCustomCategories by remember(customCategories) { mutableStateOf(customCategories) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }
    var categoryDialogError by remember { mutableStateOf<String?>(null) }
    var startTime by remember { mutableStateOf(taskToEdit?.startTime ?: "09:00") }
    var endTime by remember { mutableStateOf(taskToEdit?.endTime ?: "10:00") }
    var selectedDays by remember {
        mutableStateOf(
            if (taskToEdit != null) {
                taskToEdit.daysOfWeek.split(",").map { it.trim().uppercase() }.filter { it.isNotEmpty() }.toSet()
            } else {
                setOf(defaultDay.uppercase())
            }
        )
    }
    var reminderEnabled by remember { mutableStateOf(taskToEdit?.reminderEnabled ?: true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Check for overlap against existing active tasks on any of the selected days
    val hasOverlap by remember(startTime, endTime, selectedDays, existingTasks) {
        derivedStateOf {
            val startMin = RoutineTimeEngine.toLogicalMinutes(startTime)
            val endMin = RoutineTimeEngine.toLogicalMinutes(endTime)
            if (startMin >= endMin) false
            else {
                existingTasks.any { other ->
                    if (other.id == taskToEdit?.id || !other.isEnabled) false
                    else {
                        val sharesDay = selectedDays.any { other.repeatsOn(it) }
                        if (!sharesDay) false
                        else {
                            val otherStart = RoutineTimeEngine.toLogicalMinutes(other.startTime)
                            val otherEnd = RoutineTimeEngine.toLogicalMinutes(other.endTime)
                            // Interval overlap: max(start1, start2) < min(end1, end2)
                            maxOf(startMin, otherStart) < minOf(endMin, otherEnd)
                        }
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .testTag("add_edit_task_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(2.dp, ToDodoYellow)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (taskToEdit == null) "Create Task ✏️" else "Edit Task 📝",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (taskToEdit != null && onDelete != null) {
                        IconButton(
                            onClick = {
                                onDelete(taskToEdit)
                                onDismiss()
                            },
                            modifier = Modifier.testTag("delete_task_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete task",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Task Name
                Text(
                    text = "Task Name",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    placeholder = { Text("e.g. DSA Practice, Guitar, Study...") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ToDodoYellow,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                val allDisplayCategories = remember(localCustomCategories, selectedCategory) {
                    val base = CategoryHelper.DEFAULT_CATEGORIES + localCustomCategories
                    if (base.none { it.equals(selectedCategory, ignoreCase = true) }) {
                        base + selectedCategory
                    } else {
                        base
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    allDisplayCategories.forEach { category ->
                        val isSelected = selectedCategory.equals(category, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) ToDodoYellow else getCategoryPastelColor(category),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) ToDodoTextDark else Color.Transparent
                            ),
                            modifier = Modifier
                                .clickable { selectedCategory = category }
                                .testTag("cat_chip_$category")
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = ToDodoTextDark,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // + Add Category Chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable {
                                newCategoryInput = ""
                                categoryDialogError = null
                                showAddCategoryDialog = true
                            }
                            .testTag("btn_add_category")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Category",
                                modifier = Modifier.size(16.dp),
                                tint = ToDodoTextDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add Category",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = ToDodoTextDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Start & End Times
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Start Time",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = {
                                startTime = it
                                errorMessage = null
                            },
                            placeholder = { Text("09:00") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("task_start_time_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ToDodoYellow,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "End Time",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = {
                                endTime = it
                                errorMessage = null
                            },
                            placeholder = { Text("10:00") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("task_end_time_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ToDodoYellow,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Days of Week Selection
                Text(
                    text = "Repeat on Days",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DAYS_OF_WEEK_ORDER.forEach { (dayFullName, dayShort) ->
                        val isDaySelected = selectedDays.contains(dayFullName)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isDaySelected) ToDodoYellow else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    selectedDays = if (isDaySelected) {
                                        if (selectedDays.size > 1) selectedDays - dayFullName else selectedDays
                                    } else {
                                        selectedDays + dayFullName
                                    }
                                }
                                .testTag("day_chip_$dayShort"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayShort,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isDaySelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDaySelected) ToDodoTextDark else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reminder Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Reminder Notifications",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Notify when task starts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it },
                        modifier = Modifier.testTag("reminder_switch")
                    )
                }

                // Overlap Non-Blocking Warning
                if (hasOverlap) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFFFF9DB),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFF59F00).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Overlap warning",
                                tint = Color(0xFFE67700),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚠️ This overlaps with another scheduled task.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD9480F),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Error validation message
                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "Please enter a task name"
                                return@Button
                            }
                            if (selectedDays.isEmpty()) {
                                errorMessage = "Please select at least one day"
                                return@Button
                            }
                            onSave(
                                title.trim(),
                                selectedCategory,
                                startTime.trim(),
                                endTime.trim(),
                                selectedDays,
                                reminderEnabled
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_task_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ToDodoYellow)
                    ) {
                        Text(
                            text = "Save ♡",
                            fontWeight = FontWeight.Bold,
                            color = ToDodoTextDark
                        )
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = {
                Text(
                    text = "Add Category",
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCategoryInput,
                        onValueChange = {
                            newCategoryInput = it
                            categoryDialogError = null
                        },
                        label = { Text("Category name") },
                        placeholder = { Text("e.g. Photography, Cooking...") },
                        singleLine = true,
                        isError = categoryDialogError != null,
                        supportingText = {
                            categoryDialogError?.let {
                                Text(text = it, color = MaterialTheme.colorScheme.error)
                            } ?: Text("${newCategoryInput.length}/30")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_category_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val validationError = CategoryHelper.validateCategoryName(
                            newCategoryInput,
                            localCustomCategories
                        )
                        if (validationError != null) {
                            categoryDialogError = validationError
                            return@Button
                        }
                        val trimmed = newCategoryInput.trim()
                        if (onAddCustomCategory != null) {
                            onAddCustomCategory(trimmed) { success, err ->
                                if (success) {
                                    if (!localCustomCategories.any { it.equals(trimmed, ignoreCase = true) }) {
                                        localCustomCategories = localCustomCategories + trimmed
                                    }
                                    selectedCategory = trimmed
                                    showAddCategoryDialog = false
                                } else {
                                    categoryDialogError = err ?: "Category already exists"
                                }
                            }
                        } else {
                            if (!localCustomCategories.any { it.equals(trimmed, ignoreCase = true) }) {
                                localCustomCategories = localCustomCategories + trimmed
                            }
                            selectedCategory = trimmed
                            showAddCategoryDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_category_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = ToDodoYellow)
                ) {
                    Text("Add", color = ToDodoTextDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddCategoryDialog = false },
                    modifier = Modifier.testTag("cancel_add_category_btn")
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
