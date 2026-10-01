package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TaskEntity
import com.example.ui.components.CircularProgressRing
import com.example.ui.components.FullDayChickenCelebrationOverlay
import com.example.ui.components.PartyPopperEffect
import com.example.ui.components.SleepingCat
import com.example.ui.components.SunDoodle
import com.example.ui.theme.*
import com.example.viewmodel.RoutineViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: RoutineViewModel,
    onOpenProfile: () -> Unit = {}
) {
    val userName by viewModel.userName.collectAsState()
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val tasks by viewModel.tasksForSelectedDate.collectAsState()
    val completions by viewModel.completionsForSelectedDate.collectAsState()
    val progress by viewModel.todayProgress.collectAsState()
    val creditScore by viewModel.creditScore.collectAsState()
    val partyPopperTaskId by viewModel.partyPopperTaskId.collectAsState()
    val fullDayCelebrationEvent by viewModel.fullDayCelebrationEvent.collectAsState()

    val parsedDate = try {
        LocalDate.parse(selectedDateStr)
    } catch (e: Exception) {
        RoutineViewModel.getLogicalDate()
    }

    val dateFormatted = parsedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM"))

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingTime = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    val motivationalQuotes = remember {
        listOf(
            "You got this!" to "One task at a time ♡",
            "Small steps count." to "Consistency beats intensity ♡",
            "Keep showing up." to "A little progress adds up to big results ♡",
            "Discipline is self-love." to "Make today count ♡"
        )
    }
    val currentQuote = motivationalQuotes[Math.floorMod(parsedDate.dayOfYear, motivationalQuotes.size)]

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    // Credit Score Top Pill
                    Surface(
                        color = ToDodoYellowLight,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { onOpenProfile() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⭐", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$creditScore",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = ToDodoTextDark
                            )
                        }
                    }

                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ToDodoYellowLight)
                            .clickable { onOpenProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                            color = ToDodoTextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
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
            // Header Greeting
            item {
                Column {
                    Text(
                        text = "$greetingTime,\n$userName ♡",
                        style = MaterialTheme.typography.displaySmall.copy(fontFamily = PatrickHandFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 36.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Warm Motivational Banner Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("motivational_card"),
                    colors = CardDefaults.cardColors(containerColor = ToDodoYellowLight),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, ToDodoYellow.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SunDoodle(size = 46.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentQuote.first,
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = ToDodoTextDark
                            )
                            Text(
                                text = currentQuote.second,
                                style = MaterialTheme.typography.bodySmall,
                                color = ToDodoTextDark.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Daily Progress Summary Card with Ring
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_progress_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Today's Progress",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${progress.completed} of ${progress.total} tasks completed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (progress.percentage >= 100 && progress.total > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = ToDodoYellowLight,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "100% Day Complete! 🐥⭐",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ToDodoTextDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else if (progress.percentage >= 70 && progress.total > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = ToDodoYellowLight,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Streak Goal Reached! 🌟",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ToDodoTextDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        CircularProgressRing(percentage = progress.percentage, size = 80.dp, strokeWidth = 9.dp)
                    }
                }
            }

            // Today's Tasks Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Tasks",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        color = ToDodoYellowLight,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${progress.completed}/${progress.total}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ToDodoTextDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Task List
            items(tasks, key = { it.id }) { task ->
                val isCompleted = completions[task.id] == true
                val isCurrent = isTaskCurrentlyActive(task.startTime, task.endTime)
                val isPopping = partyPopperTaskId == task.id

                ToDodoTaskItem(
                    task = task,
                    isCompleted = isCompleted,
                    isCurrentTask = isCurrent,
                    isPopping = isPopping,
                    onPopperFinished = { viewModel.clearPartyPopper() },
                    onToggle = { checked ->
                        viewModel.toggleTaskCompletion(task.id, checked, selectedDateStr)
                    }
                )
            }

            // Empty state if no tasks
            if (tasks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SleepingCat(size = 90.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nothing here yet ♡",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                            fontSize = 20.sp,
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

    // Full-day Chicken Celebration Overlay
    fullDayCelebrationEvent?.let { celebrationData ->
        FullDayChickenCelebrationOverlay(
            creditScore = celebrationData.newCreditScore,
            milestoneMessage = celebrationData.milestoneMessage,
            onDismiss = { viewModel.dismissFullDayCelebration() }
        )
    }
}

@Composable
fun ToDodoTaskItem(
    task: TaskEntity,
    isCompleted: Boolean,
    isCurrentTask: Boolean,
    isPopping: Boolean = false,
    onPopperFinished: () -> Unit = {},
    onToggle: (Boolean) -> Unit
) {
    val categoryBg = getCategoryPastelColor(task.category)
    val checkScale by animateFloatAsState(
        targetValue = if (isCompleted) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "checkScale"
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("task_item_${task.id}")
                .clickable { onToggle(!isCompleted) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrentTask) ToDodoYellowLight.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                if (isCurrentTask) ToDodoYellow else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cute Rounded Circle Checkbox
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .scale(checkScale)
                        .clip(CircleShape)
                        .background(if (isCompleted) ToDodoYellow else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (isCompleted) ToDodoYellow else Color(0xFFB2BEC3),
                            shape = CircleShape
                        )
                        .clickable { onToggle(!isCompleted) }
                        .testTag("checkbox_${task.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = ToDodoTextDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Bold,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (task.endTime.isNotEmpty()) "${task.startTime} – ${task.endTime}" else task.startTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            color = categoryBg,
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

                        if (isCurrentTask) {
                            Surface(
                                color = ToDodoYellow,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "NOW ⚡",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ToDodoTextDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Party Popper Confetti Particle Animation
        if (isPopping) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 2.dp)
            ) {
                PartyPopperEffect(onFinished = onPopperFinished)
            }
        }
    }
}

fun getCategoryPastelColor(category: String): Color {
    return when (category.lowercase().trim()) {
        "study" -> Color(0xFFFFF3B0)
        "college" -> Color(0xFFE0F2FE)
        "project / internship" -> Color(0xFFE3FAFC)
        "exercise" -> Color(0xFFE8F5E9)
        "guitar" -> Color(0xFFF3E5F5)
        "flute" -> Color(0xFFF8F0FC)
        "reading" -> Color(0xFFFFE0B2)
        "personal" -> Color(0xFFFFF0F5)
        else -> Color(0xFFF1F3F5)
    }
}

private fun isTaskCurrentlyActive(startTime: String, endTime: String): Boolean {
    if (startTime.isEmpty()) return false
    try {
        val now = LocalTime.now()
        val startParts = startTime.split(":")
        val startHour = startParts[0].toInt()
        val startMin = if (startParts.size > 1) startParts[1].toInt() else 0
        val start = LocalTime.of(startHour, startMin)

        if (endTime.isEmpty()) {
            return now.hour == startHour
        }

        val endParts = endTime.split(":")
        val endHour = endParts[0].toInt()
        val endMin = if (endParts.size > 1) endParts[1].toInt() else 0
        val end = LocalTime.of(endHour, endMin)

        return if (end.isAfter(start)) {
            !now.isBefore(start) && now.isBefore(end)
        } else {
            !now.isBefore(start) || now.isBefore(end)
        }
    } catch (e: Exception) {
        return false
    }
}
