package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CelebratingCat
import com.example.ui.components.SleepingCat
import com.example.ui.components.ToDodoMascot
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    initialName: String = "Piyush",
    onFinish: (name: String) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var nameInput by remember { mutableStateOf(initialName) }

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
            // Top indicator dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 1..4) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (step == i) 20.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(if (step == i) ToDodoTextDark else ToDodoTextDark.copy(alpha = 0.2f))
                    )
                }
            }

            // Animated Center Content
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> -width } + fadeOut()
                    )
                },
                label = "onboardingSlide",
                modifier = Modifier.weight(1f)
            ) { currentStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (currentStep) {
                        1 -> {
                            ToDodoMascot(size = 150.dp)
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = "Welcome to to-dodo",
                                style = MaterialTheme.typography.displaySmall.copy(fontFamily = PatrickHandFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = ToDodoTextDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Small steps. Big dreams. ♡",
                                style = MaterialTheme.typography.titleMedium,
                                color = ToDodoTextDark.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                        2 -> {
                            CelebratingCat(size = 140.dp)
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = "One task at a time.",
                                style = MaterialTheme.typography.displaySmall.copy(fontFamily = PatrickHandFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = ToDodoTextDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Build healthy routines with daily consistency and cozy companions ♡",
                                style = MaterialTheme.typography.bodyLarge,
                                color = ToDodoTextDark.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        3 -> {
                            SleepingCat(size = 150.dp)
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = "Consistency beats intensity.",
                                style = MaterialTheme.typography.displaySmall.copy(fontFamily = PatrickHandFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = ToDodoTextDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Track streaks, celebrate milestones, and achieve your goals peacefully ♡",
                                style = MaterialTheme.typography.bodyLarge,
                                color = ToDodoTextDark.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        4 -> {
                            ToDodoMascot(size = 100.dp)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "What should we call you?",
                                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = PatrickHandFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = ToDodoTextDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                singleLine = true,
                                placeholder = { Text("Your name") },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = ToDodoTextDark,
                                    unfocusedBorderColor = ToDodoBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .testTag("name_input_field")
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                if (step < 4) {
                    Button(
                        onClick = { step++ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("next_onboarding_btn"),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ToDodoTextDark)
                    ) {
                        Text(
                            text = "Next →",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ToDodoYellowPastel
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            val finalName = if (nameInput.isNotBlank()) nameInput.trim() else "Piyush"
                            onFinish(finalName)
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
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
