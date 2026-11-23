package com.fitpulse.ai.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitpulse.ai.data.FitnessRepository
import com.fitpulse.ai.ui.screens.*
import com.fitpulse.ai.ui.theme.*

enum class BottomTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    WORKOUT("Workout", Icons.Default.FitnessCenter),
    NUTRITION("Nutrition", Icons.Default.Restaurant),
    COACH("Coach", Icons.Default.AutoAwesome),
    PROGRESS("Progress", Icons.Default.TrendingUp)
}

@Composable
fun MainScreen(
    repository: FitnessRepository = remember { FitnessRepository() },
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GymDarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = GymDarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = GymDarkBorder,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
            ) {
                BottomTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) NeonGreen else Color.Gray,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) NeonGreen else Color.Gray,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = NeonGreen.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                BottomTab.HOME -> HomeScreen(
                    repository = repository,
                    onStartWorkout = { selectedTab = BottomTab.WORKOUT },
                    onConsultCoach = { selectedTab = BottomTab.COACH }
                )
                BottomTab.WORKOUT -> WorkoutScreen(repository = repository)
                BottomTab.NUTRITION -> NutritionScreen(repository = repository)
                BottomTab.COACH -> AiCoachScreen(repository = repository)
                BottomTab.PROGRESS -> ProgressScreen(repository = repository)
            }
        }
    }
}
