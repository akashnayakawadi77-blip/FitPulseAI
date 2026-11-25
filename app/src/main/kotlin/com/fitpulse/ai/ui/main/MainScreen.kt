package com.fitpulse.ai.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
    PROGRESS("Progress", Icons.AutoMirrored.Filled.TrendingUp)
}

@Composable
fun MainScreen(
    repository: FitnessRepository = remember { FitnessRepository() },
    onOpenProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GymDarkBackground,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(28.dp), ambientColor = NeonGreen.copy(alpha = 0.15f))
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    GymDarkSurfaceElevated.copy(alpha = 0.96f),
                                    GymDarkSurface.copy(alpha = 0.98f)
                                )
                            )
                        )
                        .border(1.dp, GlassBorderHighlight, RoundedCornerShape(28.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.verticalGradient(
                                            listOf(
                                                NeonGreen.copy(alpha = 0.22f),
                                                ElectricCyan.copy(alpha = 0.08f)
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                                    }
                                )
                                .clickable { selectedTab = tab }
                                .padding(vertical = 9.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) NeonGreen else TextSecondary,
                                modifier = Modifier.size(21.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = tab.title,
                                color = if (isSelected) NeonGreen else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .statusBarsPadding()
        ) {
            when (selectedTab) {
                BottomTab.HOME -> HomeScreen(
                    repository = repository,
                    onStartWorkout = { selectedTab = BottomTab.WORKOUT },
                    onConsultCoach = { selectedTab = BottomTab.COACH },
                    onOpenProfile = onOpenProfile,
                    onOpenNutrition = { selectedTab = BottomTab.NUTRITION }
                )
                BottomTab.WORKOUT -> WorkoutScreen(repository = repository)
                BottomTab.NUTRITION -> NutritionScreen(repository = repository)
                BottomTab.COACH -> AiCoachScreen(repository = repository)
                BottomTab.PROGRESS -> ProgressScreen(repository = repository)
            }
        }
    }
}
