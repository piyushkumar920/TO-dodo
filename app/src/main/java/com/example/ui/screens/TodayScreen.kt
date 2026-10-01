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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.db.TaskEntity
import com.example.ui.components.CircularProgressRing
import com.example.ui.components.FullDayChickenCelebrationOverlay
import com.example.ui.components.SleepingCat
import com.example.ui.components.SunDoodle
import com.example.ui.components.TaskConfettiOverlay
import com.example.ui.components.TaskConfettiPopper
import com.example.ui.components.ToDodoMascot
import com.example.ui.theme.*
import com.example.util.CelebrationSoundHelper
import com.example.util.RoutineTimeEngine
import com.example.viewmodel.RoutineViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: RoutineViewModel,
    onOpenProfile: () -> Unit = {},
    onOpenRoutine: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshTime()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val userName by viewModel.userName.collectAsState()
    val actualDate by viewModel.actualDate.collectAsState()
    val currentGreeting by viewModel.currentGreeting.collectAsState()
    val routineStatus by viewModel.routineStatus.collectAsState()

    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val tasks by viewModel.tasksForSelectedDate.collectAsState()
    val completions by viewModel.completionsForSelectedDate.collectAsState()
    val progress by viewModel.todayProgress.collectAsState()
    val creditScore by viewModel.creditScore.collectAsState()
    val partyPopperTaskId by viewModel.partyPopperTaskId.collectAsState()
    val fullDayCelebrationEvent by viewModel.fullDayCelebrationEvent.collectAsState()

    var activeBurstOrigin by remember { mutableStateOf<Offset?>(null) }

    val dateFormatted = RoutineTimeEngine.formatActualDate(actualDate)
    val motivationalQuote by viewModel.currentMotivationalQuote.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
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
                // Dynamic Header Greeting & Actual Device Date
                item {
                    Column {
                        Text(
                            text = "${currentGreeting.greeting},\n$userName ${currentGreeting.emoji}",
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

                // Motivational Quote Card (Phase 4)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("motivational_card"),
                        colors = CardDefaults.cardColors(containerColor = ToDodoYellowLight),
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.dp, ToDodoYellow.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SunDoodle(size = 38.dp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                AnimatedContent(
                                    targetState = motivationalQuote,
                                    transitionSpec = {
                                        (fadeIn(animationSpec = tween(250)) + slideInVertically(animationSpec = tween(250)) { 10 })
                                            .togetherWith(fadeOut(animationSpec = tween(200)))
                                    },
                                    label = "quoteContentAnimation"
                                ) { quote ->
                                    Column {
                                        Text(
                                            text = "“${quote.text}”",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = PatrickHandFontFamily),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 17.sp,
                                            color = ToDodoTextDark,
                                            lineHeight = 22.sp
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "— ${quote.author}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ToDodoTextDark.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                            IconButton(
                                onClick = { viewModel.refreshMotivationalQuote(forceNew = true) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("refresh_quote_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh quote",
                                    tint = ToDodoTextDark.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Live Routine Status Card (Currently / Up Next / Done for day)
                item {
                    RoutineStatusCard(routineStatus = routineStatus)
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
                            if (progress.total == 0) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🌿 Free Day",
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Nothing scheduled today. Enjoy your restful day ♡",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
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
                                    if (progress.percentage >= 100) {
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
                                    } else if (progress.percentage >= 70) {
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
                    val isCurrent = routineStatus.currentTask?.id == task.id
                    val isPopping = partyPopperTaskId == task.id

                    ToDodoTaskItem(
                        task = task,
                        isCompleted = isCompleted,
                        isCurrentTask = isCurrent,
                        isPopping = isPopping,
                        onPopperFinished = { viewModel.clearPartyPopper() },
                        onToggle = { checked, itemOrigin ->
                            if (checked) {
                                CelebrationSoundHelper.playConfettiPop()
                                activeBurstOrigin = itemOrigin
                            }
                            viewModel.toggleTaskCompletion(task.id, checked, selectedDateStr)
                        }
                    )
                }

                // Empty state if no tasks
                if (tasks.isEmpty()) {
                    item {
                        if (allTasks.isEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .testTag("empty_routine_card"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    ToDodoMascot(size = 90.dp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Your routine is empty 🌿",
                                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Create your first task\nand start building your day.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Button(
                                        onClick = onOpenRoutine,
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ToDodoYellow),
                                        modifier = Modifier.testTag("add_first_task_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add Task", tint = ToDodoTextDark)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "+ Add Task",
                                            fontWeight = FontWeight.Bold,
                                            color = ToDodoTextDark
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                SleepingCat(size = 90.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Free Day 🌿",
                                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No scheduled tasks today. Rest & enjoy!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Dedicated Root-Level Task Confetti Overlay (Unclipped, Top Z-Index)
        TaskConfettiOverlay(
            burstOrigin = activeBurstOrigin,
            onBurstFinished = { activeBurstOrigin = null }
        )

        // Full-day Chicken Celebration Overlay
        fullDayCelebrationEvent?.let { celebrationData ->
            FullDayChickenCelebrationOverlay(
                creditScore = celebrationData.newCreditScore,
                milestoneMessage = celebrationData.milestoneMessage,
                onDismiss = { viewModel.dismissFullDayCelebration() }
            )
        }
    }
}

/**
 * Routine Status Card displaying Live Routine State (CURRENT_TASK, UP NEXT, etc.)
 */
@Composable
fun RoutineStatusCard(
    routineStatus: RoutineTimeEngine.RoutineStatusInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("routine_status_card"),
        colors = CardDefaults.cardColors(
            containerColor = when (routineStatus.state) {
                RoutineTimeEngine.RoutineStatusType.CURRENT_TASK -> ToDodoYellowLight
                RoutineTimeEngine.RoutineStatusType.AFTER_LAST_TASK -> ToDodoYellowLight.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            1.dp,
            if (routineStatus.state == RoutineTimeEngine.RoutineStatusType.CURRENT_TASK) ToDodoYellow else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            when (routineStatus.state) {
                RoutineTimeEngine.RoutineStatusType.CURRENT_TASK -> {
                    val current = routineStatus.currentTask
                    val next = routineStatus.nextTask

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = ToDodoYellow,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "CURRENTLY ⚡",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = ToDodoTextDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (current != null) {
                            Text(
                                text = if (current.endTime.isNotEmpty()) "${current.startTime} – ${current.endTime}" else current.startTime,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ToDodoTextDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = current?.title ?: "",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = ToDodoTextDark
                    )

                    if (next != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(
                            color = ToDodoYellow.copy(alpha = 0.6f),
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ToDodoCream,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "UP NEXT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ToDodoTextDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${next.title}  •  ${if (next.endTime.isNotEmpty()) "${next.startTime} – ${next.endTime}" else next.startTime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ToDodoTextDark,
                                maxLines = 1
                            )
                        }
                    }
                }
                RoutineTimeEngine.RoutineStatusType.BEFORE_FIRST_TASK,
                RoutineTimeEngine.RoutineStatusType.BETWEEN_TASKS -> {
                    val next = routineStatus.nextTask
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (routineStatus.state == RoutineTimeEngine.RoutineStatusType.BEFORE_FIRST_TASK) "🌅" else "⏳",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = routineStatus.headline,
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (next != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ToDodoYellowLight,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "UP NEXT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ToDodoTextDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${next.title} at ${next.startTime}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                RoutineTimeEngine.RoutineStatusType.AFTER_LAST_TASK -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎉", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = routineStatus.headline,
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = ToDodoTextDark
                            )
                            Text(
                                text = routineStatus.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = ToDodoTextDark.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                RoutineTimeEngine.RoutineStatusType.NO_TASKS_TODAY -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🌿", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = routineStatus.headline,
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = routineStatus.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToDodoTaskItem(
    task: TaskEntity,
    isCompleted: Boolean,
    isCurrentTask: Boolean,
    onToggle: (Boolean) -> Unit
) {
    ToDodoTaskItem(
        task = task,
        isCompleted = isCompleted,
        isCurrentTask = isCurrentTask,
        isPopping = false,
        onPopperFinished = {},
        onToggle = { checked, _ -> onToggle(checked) }
    )
}

@Composable
fun ToDodoTaskItem(
    task: TaskEntity,
    isCompleted: Boolean,
    isCurrentTask: Boolean,
    isPopping: Boolean = false,
    onPopperFinished: () -> Unit = {},
    onToggle: (Boolean, Offset) -> Unit
) {
    val categoryBg = getCategoryPastelColor(task.category)
    val checkScale by animateFloatAsState(
        targetValue = if (isCompleted) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "checkScale"
    )

    var checkboxOrigin by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isPopping) 15f else 1f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("task_item_${task.id}")
                .clickable { onToggle(!isCompleted, checkboxOrigin) },
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
                        .onGloballyPositioned { coordinates ->
                            val pos = coordinates.positionInRoot()
                            checkboxOrigin = Offset(
                                pos.x + coordinates.size.width / 2f,
                                pos.y + coordinates.size.height / 2f
                            )
                        }
                        .clickable { onToggle(!isCompleted, checkboxOrigin) }
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

        // Inline Confetti Burst
        if (isPopping) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-40).dp, y = (-40).dp)
                    .zIndex(30f)
            ) {
                TaskConfettiPopper(onFinished = onPopperFinished)
            }
        }
    }
}

fun getCategoryPastelColor(category: String): Color {
    val clean = category.trim().lowercase()
    return when (clean) {
        "study & learning", "study", "college" -> Color(0xFFFFF3B0)
        "work" -> Color(0xFFE0F2FE)
        "health & fitness", "exercise" -> Color(0xFFE8F5E9)
        "personal" -> Color(0xFFFFF0F5)
        "family" -> Color(0xFFFFE0B2)
        "projects", "project / internship" -> Color(0xFFE3FAFC)
        "hobbies", "guitar", "flute" -> Color(0xFFF3E5F5)
        "self growth" -> Color(0xFFF1F8E9)
        "errands" -> Color(0xFFFFF8E1)
        "rest" -> Color(0xFFEDE7F6)
        "reading" -> Color(0xFFFFE0B2)
        "other" -> Color(0xFFF1F3F5)
        else -> {
            val palette = listOf(
                Color(0xFFFFF3B0),
                Color(0xFFE0F2FE),
                Color(0xFFE8F5E9),
                Color(0xFFFFF0F5),
                Color(0xFFFFE0B2),
                Color(0xFFE3FAFC),
                Color(0xFFF3E5F5),
                Color(0xFFF1F8E9),
                Color(0xFFFFF8E1),
                Color(0xFFEDE7F6)
            )
            val index = kotlin.math.abs(clean.hashCode() % palette.size)
            palette[index]
        }
    }
}
