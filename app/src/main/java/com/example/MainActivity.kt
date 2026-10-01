package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.screens.MainScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.CelebrationSoundHelper
import com.example.viewmodel.RoutineViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: RoutineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CelebrationSoundHelper.initialize(this)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.theme.collectAsState()
            val userName by viewModel.userName.collectAsState()
            val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()

            val darkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                if (!onboardingCompleted) {
                    OnboardingScreen(
                        initialName = userName,
                        onFinish = { chosenName, useStarterRoutine ->
                            viewModel.setUserName(chosenName)
                            if (useStarterRoutine) {
                                viewModel.loadStarterRoutine()
                            } else {
                                viewModel.clearRoutineForCustom()
                            }
                            viewModel.setOnboardingCompleted(true)
                        }
                    )
                } else {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
