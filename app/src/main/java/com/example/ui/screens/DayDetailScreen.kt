package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FullDayChickenCelebrationOverlay
import com.example.ui.components.TaskConfettiOverlay
import com.example.ui.theme.*
import com.example.util.CelebrationSoundHelper
import com.example.viewmodel.RoutineViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(viewModel: RoutineViewModel, dateStr: String, onBack: () -> Unit) {
    LaunchedEffect(dateStr) {
        viewModel.setSelectedDate(dateStr)
    }

    val tasks by viewModel.tasksForSelectedDate.collectAsState()
    val completions by viewModel.completionsForSelectedDate.collectAsState()
    val partyPopperTaskId by viewModel.partyPopperTaskId.collectAsState()
    val fullDayCelebrationEvent by viewModel.fullDayCelebrationEvent.collectAsState()
    val progress = viewModel.getProgressForDate(dateStr)

    var activeBurstOrigin by remember { mutableStateOf<Offset?>(null) }

    val parsedDate = try {
        LocalDate.parse(dateStr)
    } catch (e: Exception) {
        LocalDate.now()
    }
    val dayName = parsedDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
    val formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "$dayName Routine",
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                            fontSize = 22.sp,
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
                item {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress.percentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = ToDodoYellow,
                        trackColor = ToDodoYellowLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${progress.completed} of ${progress.total} tasks completed (${progress.percentage}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(tasks, key = { it.id }) { task ->
                    val isCompleted = completions[task.id] == true
                    val isPopping = partyPopperTaskId == task.id
                    ToDodoTaskItem(
                        task = task,
                        isCompleted = isCompleted,
                        isCurrentTask = false,
                        isPopping = isPopping,
                        onPopperFinished = { viewModel.clearPartyPopper() },
                        onToggle = { checked, itemOrigin ->
                            if (checked) {
                                CelebrationSoundHelper.playConfettiPop()
                                activeBurstOrigin = itemOrigin
                            }
                            viewModel.toggleTaskCompletion(task.id, checked, dateStr)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Root Task Confetti Overlay
        TaskConfettiOverlay(
            burstOrigin = activeBurstOrigin,
            onBurstFinished = { activeBurstOrigin = null }
        )

        fullDayCelebrationEvent?.let { celebrationData ->
            FullDayChickenCelebrationOverlay(
                creditScore = celebrationData.newCreditScore,
                milestoneMessage = celebrationData.milestoneMessage,
                onDismiss = { viewModel.dismissFullDayCelebration() }
            )
        }
    }
}
