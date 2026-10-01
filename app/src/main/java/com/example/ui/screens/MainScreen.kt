package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.theme.*
import com.example.viewmodel.RoutineViewModel
import java.time.LocalDate

sealed class Screen(val route: String, val title: String, val iconActive: ImageVector, val iconInactive: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Calendar : Screen("calendar", "Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    object Stats : Screen("stats", "Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun MainScreen(viewModel: RoutineViewModel) {
    val navController = rememberNavController()
    val screens = listOf(Screen.Home, Screen.Calendar, Screen.Stats, Screen.Profile)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                screens.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(if (selected) 40.dp else 28.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) ToDodoYellowLight else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selected) screen.iconActive else screen.iconInactive,
                                    contentDescription = screen.title,
                                    tint = if (selected) ToDodoTextDark else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) ToDodoTextDark else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        selected = selected,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        ),
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                TodayScreen(
                    viewModel = viewModel,
                    onOpenProfile = { navController.navigate(Screen.Profile.route) },
                    onOpenRoutine = { navController.navigate("my_routine") }
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(viewModel = viewModel)
            }
            composable(Screen.Stats.route) {
                ProgressScreen(viewModel = viewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onOpenSettings = { navController.navigate("settings_sub") },
                    onOpenRoutine = { navController.navigate("my_routine") }
                )
            }
            composable("settings_sub") {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenRoutine = { navController.navigate("my_routine") }
                )
            }
            composable("my_routine") {
                RoutineManagementScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "day_detail/{dateStr}",
                arguments = listOf(navArgument("dateStr") { type = NavType.StringType })
            ) { backStackEntry ->
                val dateStr = backStackEntry.arguments?.getString("dateStr") ?: LocalDate.now().toString()
                DayDetailScreen(
                    viewModel = viewModel,
                    dateStr = dateStr,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
