package com.fitpulse.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitpulse.ai.data.FitnessRepository
import com.fitpulse.ai.ui.components.AnimatedCircularScoreRing
import com.fitpulse.ai.ui.components.GlassCard
import com.fitpulse.ai.ui.components.GradientPrimaryButton
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A141E),
                        GymDarkBackground,
                        GymDarkBackground
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FitPulse",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "AI",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen,
                                letterSpacing = (-0.5).sp
                            )
                        }
                        Text(
                            text = "Welcome back, ${userProfile.name}",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(NeonGreen.copy(alpha = 0.22f), ElectricCyan.copy(alpha = 0.14f))
                                )
                            )
                            .border(1.dp, NeonGreen.copy(alpha = 0.6f), CircleShape)
                            .clickable { onOpenProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile & Settings",
                            tint = NeonGreen,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
            }

            // Hero Card: Readiness & Fitness Score
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = NeonGreen,
                    borderColor = NeonGreen.copy(alpha = 0.25f)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NeonGreen.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "TODAY'S READINESS",
                                        color = NeonGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${dailyScore.score}",
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary,
                                        lineHeight = 44.sp
                                    )
                                    Text(
                                        text = " / ${dailyScore.maxScore}",
                                        fontSize = 15.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(bottom = 7.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${dailyScore.status} • ${dailyScore.readiness}",
                                    color = ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            AnimatedCircularScoreRing(
                                progress = dailyScore.score / 100f,
                                scoreText = "${dailyScore.score}%",
                                subText = "SCORE",
                                size = 94.dp,
                                strokeWidth = 9.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ScorePill("Muscle Recovery", "${dailyScore.recoveryContribution}%", NeonGreen, Modifier.weight(1f))
                            ScorePill("Consistency", "${dailyScore.consistencyContribution}%", ElectricCyan, Modifier.weight(1f))
                        }
                    }
                }
            }

            // Section Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Pulse",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Tap Water or Steps to quick-add",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            // 2x2 Bento Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TargetCard(
                            icon = Icons.Default.LocalFireDepartment,
                            label = "Calories",
                            current = "${dailyTargets.loggedCalories}",
                            target = "/ ${dailyTargets.targetCalories} kcal",
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
                            target = "/ ${dailyTargets.targetProteinGrams}g goal",
                            color = NeonGreen,
                            progress = dailyTargets.loggedProteinGrams.toFloat() / dailyTargets.targetProteinGrams.coerceAtLeast(1).toFloat(),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenNutrition() }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TargetCard(
                            icon = Icons.Default.WaterDrop,
                            label = "Water (+250ml)",
                            current = "${String.format("%.1f", dailyTargets.loggedWaterLiters)}L",
                            target = "/ ${String.format("%.1f", dailyTargets.targetWaterLiters)}L goal",
                            color = ElectricCyan,
                            progress = dailyTargets.loggedWaterLiters / dailyTargets.targetWaterLiters.coerceAtLeast(0.1f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { repository.addWaterIntake(250) }
                        )
                        TargetCard(
                            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                            label = "Steps (+500)",
                            current = "${dailyTargets.loggedSteps}",
                            target = "/ ${dailyTargets.targetSteps} goal",
                            color = ElectricViolet,
                            progress = dailyTargets.loggedSteps.toFloat() / dailyTargets.targetSteps.coerceAtLeast(1).toFloat(),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { repository.addSteps(500) }
                        )
                    }
                }
            }

            // Today's Workout Featured Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = ElectricCyan,
                    borderColor = NeonGreen.copy(alpha = 0.35f)
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
                                        .size(44.dp)
                                        .background(NeonGreen.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
                                        .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("TODAY'S SESSION", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                                    Text(
                                        text = currentDay?.dayTitle ?: "Upper Body Training",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        maxLines = 2
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = NeonGreen.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = "Ready",
                                    color = NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${currentDay?.exercises?.size ?: 5} exercises • ~40 min • ${currentDay?.focusArea ?: "Hypertrophy"}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        GradientPrimaryButton(
                            text = "Start Workout Session",
                            onClick = onStartWorkout,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF041009), modifier = Modifier.size(20.dp))
                            }
                        )
                    }
                }
            }

            // AI Coach Insight Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = ElectricViolet,
                    borderColor = ElectricCyan.copy(alpha = 0.25f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(ElectricCyan.copy(alpha = 0.16f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("AI Coach Insight", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                    Text("Adaptive daily guidance", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                            TextButton(onClick = onConsultCoach) {
                                Text("Chat →", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "“Readiness is at ${dailyScore.recoveryContribution}%. Focus on controlled progressive overload today and hit ${dailyTargets.targetProteinGrams}g protein for optimal repair.”",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun ScorePill(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(GymDarkCard.copy(alpha = 0.85f))
            .border(1.dp, GymDarkBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
        Text(text = value, color = color, fontWeight = FontWeight.Black, fontSize = 13.sp)
    }
}

@Composable
fun TargetCard(
    icon: ImageVector,
    label: String,
    current: String,
    target: String,
    color: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        borderColor = color.copy(alpha = 0.22f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(color.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(current, fontWeight = FontWeight.Black, color = TextPrimary, fontSize = 20.sp)
            Text(target, color = TextSecondary, fontSize = 11.sp)

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = GymDarkCard
            )
        }
    }
}
