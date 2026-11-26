package com.fitpulse.ai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitpulse.ai.data.model.MuscleGroup
import com.fitpulse.ai.data.model.MuscleRecoveryState
import com.fitpulse.ai.ui.theme.*

@Composable
fun MuscleRecoveryTracker(
    recoveryMap: Map<MuscleGroup, MuscleRecoveryState>,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(initialExpanded) }
    var selectedCategory by remember { mutableStateOf("All") }

    val allStates = recoveryMap.values.toList()
    val averageFreshness = if (allStates.isNotEmpty()) allStates.map { it.freshnessPercent }.average().toInt() else 100

    val readinessColor = when {
        averageFreshness >= 80 -> NeonGreen
        averageFreshness >= 55 -> EnergeticAmber
        else -> FireOrange
    }

    val readinessLabel = when {
        averageFreshness >= 80 -> "Optimal Readiness (Prime to Train)"
        averageFreshness >= 55 -> "Moderate Recovery (Focus on Fresh Muscles)"
        else -> "High Fatigue (Active Recovery Suggested)"
    }

    val categories = listOf("All", "Upper", "Legs", "Arms", "Core")

    val filteredList = when (selectedCategory) {
        "Upper" -> allStates.filter { it.muscle.category == "Upper" }
        "Legs" -> allStates.filter { it.muscle.category == "Legs" }
        "Arms" -> allStates.filter { it.muscle.category == "Arms" }
        "Core" -> allStates.filter { it.muscle.category == "Core" }
        else -> allStates
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(readinessColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessibilityNew,
                            contentDescription = "Recovery",
                            tint = readinessColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Muscle Recovery",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(containerColor = readinessColor.copy(alpha = 0.2f), contentColor = readinessColor) {
                                Text(
                                    text = "$averageFreshness% Fresh",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                        Text(
                            text = readinessLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle",
                        tint = Color.Gray
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) {
                    // Category filter chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            val isSel = cat == selectedCategory
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSel) NeonGreen else GymDarkCard,
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSel) Color.Black else Color.LightGray,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Muscle cards horizontal scroll
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredList) { recoveryState ->
                            MuscleStatusCard(recoveryState = recoveryState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MuscleStatusCard(recoveryState: MuscleRecoveryState) {
    val pct = recoveryState.freshnessPercent
    val barColor = when {
        pct >= 80 -> NeonGreen
        pct >= 50 -> ElectricCyan
        else -> FireOrange
    }

    val statusBadgeText = when {
        pct >= 80 -> "Ready"
        pct >= 50 -> "Rebuilding"
        else -> "Fatigued"
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GymDarkCard),
        modifier = Modifier
            .width(135.dp)
            .border(1.dp, barColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = MuscleImageHelper.drawableForMuscle(recoveryState.muscle)),
                    contentDescription = recoveryState.muscle.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Text(
                    text = recoveryState.muscle.displayName,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$pct%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = barColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = statusBadgeText,
                    fontSize = 11.sp,
                    color = barColor.copy(alpha = 0.9f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { pct.toFloat() / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = barColor,
                trackColor = GymDarkSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (recoveryState.hoursSinceTrained == 0) "Trained today" else "${recoveryState.hoursSinceTrained}h ago",
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}
