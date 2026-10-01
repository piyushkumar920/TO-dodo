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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FullDayChickenCelebrationOverlay
import com.example.ui.components.SleepingCat
import com.example.ui.theme.*
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
            // Calendar Card
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
                            .padding(16.dp)
                    ) {
                        // Month Selector Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                            }
                            Text(
                                text = "$monthName $year",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day of week labels
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                                Text(
                                    text = day,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Days Grid
                        val firstDayOfWeek = currentYearMonth.atDay(1).dayOfWeek.value % 7 // Sunday = 0
                        val daysInMonth = currentYearMonth.lengthOfMonth()
                        val totalCells = firstDayOfWeek + daysInMonth
                        val rows = (totalCells + 6) / 7

                        for (row in 0 until rows) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                for (col in 0 until 7) {
                                    val cellIndex = row * 7 + col
                                    val dayNumber = cellIndex - firstDayOfWeek + 1

                                    if (dayNumber in 1..daysInMonth) {
                                        val cellDate = currentYearMonth.atDay(dayNumber)
                                        val cellDateStr = cellDate.toString()
                                        val isSelected = cellDateStr == selectedDateStr
                                        val isToday = cellDateStr == LocalDate.now().toString()
                                        val hasCompletions = completedDatesSet.contains(cellDateStr)

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        isSelected -> ToDodoYellow
                                                        isToday -> ToDodoYellowLight
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .clickable {
                                                    viewModel.setSelectedDate(cellDateStr)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = dayNumber.toString(),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) ToDodoTextDark else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (hasCompletions) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) ToDodoTextDark else ToDodoOrange)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Date Routine Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val formattedSelected = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM"))
                    Text(
                        text = formattedSelected,
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                    onToggle = { checked ->
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

    fullDayCelebrationEvent?.let { celebrationData ->
        FullDayChickenCelebrationOverlay(
            creditScore = celebrationData.newCreditScore,
            milestoneMessage = celebrationData.milestoneMessage,
            onDismiss = { viewModel.dismissFullDayCelebration() }
        )
    }
}
