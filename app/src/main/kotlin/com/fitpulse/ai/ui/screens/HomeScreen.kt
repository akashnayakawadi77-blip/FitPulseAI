package com.fitpulse.ai.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitpulse.ai.data.FitnessRepository
import com.fitpulse.ai.data.ImageStorageHelper
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
            .background(GymDarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Editorial Greeting Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeonGreen.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "${userProfile.fitnessGoal.displayName.uppercase()} MODE",
                                color = NeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Hey, ${userProfile.name}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    val profileBitmap = remember(userProfile.profilePhotoPath) {
                        ImageStorageHelper.loadImageBitmap(userProfile.profilePhotoPath)
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(GymDarkSurfaceElevated)
                            .border(1.5.dp, NeonGreen.copy(alpha = 0.75f), RoundedCornerShape(16.dp))
                            .clickable { onOpenProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileBitmap != null) {
                            Image(
                                bitmap = profileBitmap,
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile & Settings",
                                tint = NeonGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Hero Readiness & Fitness Score Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = NeonGreen,
                    borderColor = NeonGreen.copy(alpha = 0.28f)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "READINESS SCORE",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${dailyScore.score}",
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary,
                                        lineHeight = 48.sp
                                    )
                                    Text(
                                        text = " / 100",
                                        fontSize = 15.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${dailyScore.status} • ${dailyScore.readiness}",
                                    color = NeonGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            AnimatedCircularScoreRing(
                                progress = dailyScore.score / 100f,
                                scoreText = "${dailyScore.score}%",
                                subText = "READY",
                                size = 96.dp,
                                strokeWidth = 10.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ScorePill("Recovery", "${dailyScore.recoveryContribution}%", NeonGreen, Modifier.weight(1f))
                            ScorePill("Consistency", "${dailyScore.consistencyContribution}%", ElectricCyan, Modifier.weight(1f))
                        }
                    }
                }
            }

            // Quick Actions Strip
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(Icons.Default.FitnessCenter, "Train", NeonGreen, onStartWorkout, Modifier.weight(1f))
                    QuickActionTile(Icons.Default.Restaurant, "Fuel", EnergeticAmber, onOpenNutrition, Modifier.weight(1f))
                    QuickActionTile(Icons.Default.AutoAwesome, "Coach", ElectricCyan, onConsultCoach, Modifier.weight(1f))
                    QuickActionTile(Icons.Default.Person, "Profile", ElectricViolet, onOpenProfile, Modifier.weight(1f))
                }
            }

            // Today's Workout Hero Banner
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = ElectricCyan,
                    borderColor = GlassBorderHighlight
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
                                        .size(46.dp)
                                        .background(NeonGreen, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color(0xFF0A0D00), modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("NEXT WORKOUT", color = ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
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
                                shape = RoundedCornerShape(12.dp),
                                color = NeonGreen.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = "${currentDay?.exercises?.size ?: 5} ex",
                                    color = NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${currentDay?.focusArea ?: "Hypertrophy"} • ~40 min session",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        GradientPrimaryButton(
                            text = "Start Workout Session",
                            onClick = onStartWorkout,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF0A0D00), modifier = Modifier.size(20.dp))
                            }
                        )
                    }
                }
            }

            // Section Title: Daily Pulse
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Daily Metrics", fontSize = 17.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                    Text("Tap Water / Steps to add", fontSize = 11.sp, color = TextSecondary)
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

            // AI Coach Insight Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = ElectricViolet,
                    borderColor = ElectricViolet.copy(alpha = 0.25f)
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
                                        .background(ElectricCyan.copy(alpha = 0.16f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("AI Coach Brief", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                    Text("Adaptive daily guidance", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                            TextButton(onClick = onConsultCoach) {
                                Text("Open Coach →", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "“Recovery is at ${dailyScore.recoveryContribution}%. Aim for clean progressive overload today and hit ${dailyTargets.targetProteinGrams}g protein.”",
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
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(GymDarkSurface)
            .border(1.dp, GymDarkBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(accent.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ScorePill(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(GymDarkCard)
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
        borderColor = GymDarkBorder
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
                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(current, fontWeight = FontWeight.Black, color = TextPrimary, fontSize = 21.sp)
            Text(target, color = TextSecondary, fontSize = 11.sp)

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = GymDarkCard
            )
        }
    }
}
