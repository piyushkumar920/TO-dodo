package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FullDayChickenCelebrationOverlay
import com.example.ui.components.SleepingCat
import com.example.ui.components.TaskConfettiOverlay
import com.example.ui.theme.*
import com.example.util.CelebrationSoundHelper
import com.example.viewmodel.RoutineViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: RoutineViewModel) {
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val allCompletions by viewModel.allCompletions.collectAsState()
    val tasks by viewModel.tasksForSelectedDate.collectAsState()
    val completions by viewModel.completionsForSelectedDate.collectAsState()
    val partyPopperTaskId by viewModel.partyPopperTaskId.collectAsState()
    val fullDayCelebrationEvent by viewModel.fullDayCelebrationEvent.collectAsState()

    var activeBurstOrigin by remember { mutableStateOf<Offset?>(null) }

    var currentYearMonth by remember {
        mutableStateOf(
            try {
                YearMonth.from(LocalDate.parse(selectedDateStr))
            } catch (e: Exception) {
                YearMonth.now()
            }
        )
    }

    val selectedDate = try {
        LocalDate.parse(selectedDateStr)
    } catch (e: Exception) {
        LocalDate.now()
    }

    val completedDatesSet = remember(allCompletions) {
        allCompletions.filter { it.completed }.map { it.date }.toSet()
    }

    val monthName = currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val year = currentYearMonth.year

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Calendar",
                            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = PatrickHandFontFamily),
                            fontWeight = FontWeight.Bold
                        )
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
                // Month Navigation Header
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calendar_month_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                                    modifier = Modifier.testTag("prev_month_button")
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                                }

                                Text(
                                    text = "$monthName $year",
                                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                IconButton(
                                    onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                                    modifier = Modifier.testTag("next_month_button")
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Day of week headers
                            Row(modifier = Modifier.fillMaxWidth()) {
                                val dayHeaders = listOf("M", "T", "W", "T", "F", "S", "S")
                                dayHeaders.forEach { d ->
                                    Text(
                                        text = d,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Days Grid
                            val daysInMonth = currentYearMonth.lengthOfMonth()
                            val firstDayOfWeek = currentYearMonth.atDay(1).dayOfWeek.value // 1 = Monday
                            val leadingEmptyDays = firstDayOfWeek - 1
                            val totalGridCells = leadingEmptyDays + daysInMonth

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(7),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                userScrollEnabled = false
                            ) {
                                items(totalGridCells) { index ->
                                    if (index >= leadingEmptyDays) {
                                        val dayNumber = index - leadingEmptyDays + 1
                                        val date = currentYearMonth.atDay(dayNumber)
                                        val dateIso = date.toString()
                                        val isSelected = dateIso == selectedDateStr
                                        val isCompletedDay = completedDatesSet.contains(dateIso)
                                        val isToday = date == LocalDate.now()

                                        Box(
                                            modifier = Modifier
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        isSelected -> ToDodoYellow
                                                        isCompletedDay -> ToDodoYellowLight
                                                        isToday -> MaterialTheme.colorScheme.surfaceVariant
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .clickable {
                                                    viewModel.setSelectedDate(dateIso)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "$dayNumber",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        isSelected -> ToDodoTextDark
                                                        isCompletedDay -> ToDodoTextDark
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                                if (isCompletedDay && !isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(ToDodoOrange)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.aspectRatio(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // Selected Date Header
                item {
                    val formattedSelected = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM"))
                    Text(
                        text = formattedSelected,
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Tasks for selected date
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
                            viewModel.toggleTaskCompletion(task.id, checked, selectedDateStr)
                        }
                    )
                }

                if (tasks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            SleepingCat(size = 80.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No routine scheduled for this date ♡",
                                style = MaterialTheme.typography.bodyMedium,
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

        // Dedicated Root-Level Task Confetti Overlay
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
