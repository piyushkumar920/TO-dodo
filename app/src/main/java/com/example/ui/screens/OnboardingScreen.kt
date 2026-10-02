package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CelebratingCat
import com.example.ui.components.ToDodoMascot
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    initialName: String = "Piyush",
    onFinish: (name: String, useStarterRoutine: Boolean) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var nameInput by remember { mutableStateOf(initialName) }
    var useStarterRoutine by remember { mutableStateOf(true) }

    // Intercept back button to navigate between onboarding steps rather than exiting
    BackHandler(enabled = step > 1) {
        step--
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ToDodoYellowPastel
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation & Indicator Dots (3 steps)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 1) {
                    IconButton(
                        onClick = { step-- },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("onboarding_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ToDodoTextDark
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(36.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (step == i) 22.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (step == i) ToDodoTextDark else ToDodoTextDark.copy(alpha = 0.2f))
                        )
                    }
                }

                Spacer(modifier = Modifier.size(36.dp))
            }

            // Animated Center Content (3 screens)
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "onboardingSlide",
                modifier = Modifier.weight(1f)
            ) { currentStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (currentStep) {
                        1 -> {
                            // SCREEN 1: Welcome
                            ToDodoMascot(size = 150.dp)
                            Spacer(modifier = Modifier.height(28.dp))
                            Text(
                                text = "🐣 to-dodo",
                                style = MaterialTheme.typography.displaySmall.copy(fontFamily = PatrickHandFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = ToDodoTextDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Your routine,\nyour way.",
                                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = PatrickHandFontFamily),
                                fontWeight = FontWeight.SemiBold,
                                color = ToDodoTextDark.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center,
                                lineHeight = 32.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Small steps. Big dreams. ♡",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ToDodoTextDark.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                        2 -> {
                            // SCREEN 2: Name Input
                            CelebratingCat(size = 130.dp)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "What's your name?",
                                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = PatrickHandFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = ToDodoTextDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "What should to-dodo call you? ♡",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ToDodoTextDark.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                singleLine = true,
                                label = { Text("Your name") },
                                placeholder = { Text("Piyush") },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .testTag("name_input_field")
                            )
                        }
                        3 -> {
                            // SCREEN 3: Routine Choice
                            ToDodoMascot(size = 90.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "How would you like\nto start?",
                                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = PatrickHandFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = ToDodoTextDark,
                                textAlign = TextAlign.Center,
                                lineHeight = 30.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            // Option A: Use Starter Routine
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { useStarterRoutine = true }
                                    .semantics {
                                        role = Role.RadioButton
                                        selected = useStarterRoutine
                                    }
                                    .testTag("use_starter_routine_card"),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (useStarterRoutine) ToDodoYellow else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    2.dp,
                                    if (useStarterRoutine) ToDodoTextDark else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = useStarterRoutine,
                                        onClick = { useStarterRoutine = true },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = ToDodoTextDark,
                                            unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Use Starter Routine ⭐",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (useStarterRoutine) ToDodoTextDark else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Start with a simple pre-made routine and customize it anytime",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (useStarterRoutine) ToDodoTextDark.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Option B: Create My Routine
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { useStarterRoutine = false }
                                    .semantics {
                                        role = Role.RadioButton
                                        selected = !useStarterRoutine
                                    }
                                    .testTag("create_own_routine_card"),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!useStarterRoutine) ToDodoYellow else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    2.dp,
                                    if (!useStarterRoutine) ToDodoTextDark else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = !useStarterRoutine,
                                        onClick = { useStarterRoutine = false },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = ToDodoTextDark,
                                            unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Create My Routine ✏️",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!useStarterRoutine) ToDodoTextDark else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Start fresh with an empty routine and add your own tasks",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (!useStarterRoutine) ToDodoTextDark.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                when (step) {
                    1 -> {
                        Button(
                            onClick = { step = 2 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("get_started_btn"),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ToDodoTextDark)
                        ) {
                            Text(
                                text = "Get Started →",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = ToDodoYellowPastel
                            )
                        }
                    }
                    2 -> {
                        Button(
                            onClick = { step = 3 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("next_onboarding_btn"),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ToDodoTextDark)
                        ) {
                            Text(
                                text = "Continue →",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = ToDodoYellowPastel
                            )
                        }
                    }
                    3 -> {
                        Button(
                            onClick = {
                                val finalName = if (nameInput.isNotBlank()) nameInput.trim() else "Piyush"
                                onFinish(finalName, useStarterRoutine)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("start_routine_button"),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ToDodoYellow)
                        ) {
                            Text(
                                text = "Let's get started ♡",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = PatrickHandFontFamily),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = ToDodoTextDark
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
