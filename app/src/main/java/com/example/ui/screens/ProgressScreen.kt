package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CelebratingCat
import com.example.ui.components.CircularProgressRing
import com.example.ui.components.SleepingCat
import com.example.ui.theme.*
import com.example.viewmodel.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(viewModel: RoutineViewModel) {
    val currentStreak by viewModel.currentStreak.collectAsState()
    val creditScore by viewModel.creditScore.collectAsState()
    val sessionStats by viewModel.sessionStats.collectAsState()
    val categoryList by viewModel.categoryProgressList.collectAsState()
    val weekDays by viewModel.currentWeekDays.collectAsState()

    val totalWeeklyCompleted = weekDays.sumOf { it.completed }
    val totalWeeklyTasks = weekDays.sumOf { it.total }
    val weeklyPercentage = if (totalWeeklyTasks > 0) ((totalWeeklyCompleted.toFloat() / totalWeeklyTasks) * 100).toInt() else 0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Progress looks good on you ♡",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                        fontSize = 24.sp,
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
            // This Week Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weekly_progress_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
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
                                text = "This Week",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$totalWeeklyCompleted of $totalWeeklyTasks tasks\ncompleted",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        CircularProgressRing(percentage = weeklyPercentage, size = 88.dp, strokeWidth = 10.dp)
                    }
                }
            }

            // Credit Score Card (100% full days completed)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("credit_score_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ToDodoYellowLight),
                    border = BorderStroke(1.5.dp, ToDodoYellow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🐥", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Credit Score",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = ToDodoTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$creditScore",
                            style = MaterialTheme.typography.displayMedium.copy(fontFamily = PatrickHandFontFamily),
                            fontWeight = FontWeight.Bold,
                            fontSize = 38.sp,
                            color = ToDodoTextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Full days completed (100%) ♡",
                            style = MaterialTheme.typography.bodySmall,
                            color = ToDodoTextDark.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Streak Card with Celebrating Cat
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("streak_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ToDodoYellowLight),
                    border = BorderStroke(1.dp, ToDodoYellow.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🔥", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Streak",
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ToDodoTextDark
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$currentStreak days",
                                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = PatrickHandFontFamily),
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = ToDodoTextDark
                            )
                            Text(
                                text = if (currentStreak > 0) "Keep going! ♡" else "Start today with 1 task ♡",
                                style = MaterialTheme.typography.bodySmall,
                                color = ToDodoTextDark.copy(alpha = 0.8f)
                            )
                        }
                        CelebratingCat(size = 72.dp)
                    }
                }
            }

            // Weekly Overview Bar Chart
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
                            text = "Weekly Overview",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            weekDays.forEach { day ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val heightFraction = (day.percentage / 100f).coerceIn(0.08f, 1f)
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .fillMaxHeight(heightFraction)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(if (day.isToday) ToDodoYellow else ToDodoYellowLight)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = day.dayName.take(3),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (day.isToday) ToDodoTextDark else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Session Trackers Grid
            item {
                Text(
                    text = "Habit Sessions",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SessionBox(modifier = Modifier.weight(1f), emoji = "📚", title = "Study", count = sessionStats.studySessions.toString())
                        SessionBox(modifier = Modifier.weight(1f), emoji = "💻", title = "Projects", count = sessionStats.projectSessions.toString())
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SessionBox(modifier = Modifier.weight(1f), emoji = "🏃", title = "Exercise", count = sessionStats.exerciseSessions.toString())
                        SessionBox(modifier = Modifier.weight(1f), emoji = "🎸", title = "Guitar", count = sessionStats.guitarSessions.toString())
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SessionBox(modifier = Modifier.weight(1f), emoji = "🎵", title = "Flute", count = sessionStats.fluteSessions.toString())
                        SessionBox(modifier = Modifier.weight(1f), emoji = "📖", title = "Reading", count = sessionStats.readingSessions.toString())
                    }
                }
            }

            // Category Progress Cards
            item {
                Text(
                    text = "Category Progress",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            items(categoryList) { cat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cat.category,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${cat.completedCount} completed (${cat.percentage}%)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { cat.percentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = ToDodoYellow,
                            trackColor = ToDodoYellowLight
                        )
                    }
                }
            }

            // Daily Identity (Atomic Habits)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ToDodoYellowLight.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, ToDodoYellow.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Daily Identity ♡",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ToDodoTextDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "«\"I am becoming someone who does what I planned, even when I don't feel like it.\"»",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = ToDodoTextDark
                        )
                    }
                }
            }

            // Minimum Day Fallback
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
                            text = "Minimum Day (Fallback System)",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Even on difficult days, keep the habit alive ♡",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "✓ 30 min study   ✓ 20 min project   ✓ 5 min guitar", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "✓ 5 min flute   ✓ 5 pages reading   ✓ 5 min cleaning", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SessionBox(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    count: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = count,
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = PatrickHandFontFamily),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
