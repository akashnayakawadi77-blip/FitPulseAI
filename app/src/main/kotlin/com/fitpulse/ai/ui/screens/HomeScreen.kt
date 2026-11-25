package com.fitpulse.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitpulse.ai.data.FitnessRepository
import com.fitpulse.ai.ui.theme.*

@Composable
fun HomeScreen(
    repository: FitnessRepository,
    onStartWorkout: () -> Unit,
    onConsultCoach: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onOpenNutrition: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userProfile by repository.userProfile.collectAsState()
    val dailyScore by repository.dailyScore.collectAsState()
    val dailyTargets by repository.dailyTargets.collectAsState()
    val workoutDays by repository.workoutDays.collectAsState()
    val selectedDayIndex by repository.selectedDayIndex.collectAsState()
    val currentDay = workoutDays.getOrNull(selectedDayIndex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GymDarkBackground)
    ) {
        // App Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FitPulseAI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Welcome back, ${userProfile.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElectricCyan
                    )
                }
                IconButton(
                    onClick = onOpenProfile,
                    modifier = Modifier
                        .size(40.dp)
                        .background(NeonGreen.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, NeonGreen, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile & Settings",
                        tint = NeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Card 1: Today's Fitness Score
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Today's Fitness Score", color = Color.Gray, fontSize = 13.sp)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${dailyScore.score}",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NeonGreen
                                    )
                                    Text(
                                        text = " / ${dailyScore.maxScore}",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                                Text(
                                    text = "${dailyScore.status} • ${dailyScore.readiness}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ElectricCyan
                                )
                            }

                            // Circular visual progress indicator
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .background(GymDarkCard, CircleShape)
                                    .border(3.dp, NeonGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${dailyScore.score}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Sub-metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ScorePill(label = "Recovery", value = "${dailyScore.recoveryContribution}%", color = NeonGreen, modifier = Modifier.weight(1f))
                            ScorePill(label = "Consistency", value = "${dailyScore.consistencyContribution}%", color = ElectricCyan, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Card 2: Your Daily Targets Grid
            item {
                Text(
                    text = "Your Daily Targets",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TargetCard(
                        icon = Icons.Default.LocalFireDepartment,
                        label = "Calories",
                        current = "${dailyTargets.loggedCalories}",
                        target = "${dailyTargets.targetCalories} kcal",
                        color = EnergeticAmber,
                        progress = dailyTargets.loggedCalories.toFloat() / dailyTargets.targetCalories.coerceAtLeast(1).toFloat(),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenNutrition() }
                    )
                    TargetCard(
                        icon = Icons.Default.FitnessCenter,
                        label = "Protein",
                        current = "${dailyTargets.loggedProteinGrams}g",
                        target = "${dailyTargets.targetProteinGrams}g target",
                        color = NeonGreen,
                        progress = dailyTargets.loggedProteinGrams.toFloat() / dailyTargets.targetProteinGrams.coerceAtLeast(1).toFloat(),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenNutrition() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TargetCard(
                        icon = Icons.Default.WaterDrop,
                        label = "Water (+250ml)",
                        current = "${String.format("%.1f", dailyTargets.loggedWaterLiters)} L",
                        target = "${String.format("%.1f", dailyTargets.targetWaterLiters)} L target",
                        color = ElectricCyan,
                        progress = dailyTargets.loggedWaterLiters / dailyTargets.targetWaterLiters.coerceAtLeast(0.1f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { repository.addWaterIntake(250) }
                    )
                    TargetCard(
                        icon = Icons.Default.DirectionsWalk,
                        label = "Steps (+500)",
                        current = "${dailyTargets.loggedSteps}",
                        target = "${dailyTargets.targetSteps} target",
                        color = Color(0xFFE040FB),
                        progress = dailyTargets.loggedSteps.toFloat() / dailyTargets.targetSteps.coerceAtLeast(1).toFloat(),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { repository.addSteps(500) }
                    )
                }
            }

            // Card 3: Today's Workout (Actionable in < 5 seconds)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NeonGreen.copy(alpha = 0.5f)))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(NeonGreen.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Today's Workout", color = Color.Gray, fontSize = 12.sp)
                                    Text(
                                        text = currentDay?.dayTitle ?: "Upper Body Training",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        maxLines = 2
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(containerColor = NeonGreen.copy(alpha = 0.2f), contentColor = NeonGreen) {
                                Text(
                                    text = "Ready",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${currentDay?.exercises?.size ?: 6} exercises • ~40 minutes • Focus: ${currentDay?.focusArea ?: "Hypertrophy"}",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onStartWorkout,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Workout Session", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Card 4: AI Coach Contextual Insight
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(ElectricCyan.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Your AI Coach", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "“Your last workout had heavy volume. Today's optimal training focus is upper body push with moderate intensity to prioritize recovery.”",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = onConsultCoach,
                            colors = ButtonDefaults.textButtonColors(contentColor = ElectricCyan)
                        ) {
                            Text("Ask Coach about recovery →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ScorePill(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(GymDarkCard, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 11.sp)
        Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun TargetCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    current: String,
    target: String,
    color: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, color = Color.Gray, fontSize = 11.sp)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(current, fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
            Text(target, color = Color.Gray, fontSize = 10.sp)

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = GymDarkCard
            )
        }
    }
}
