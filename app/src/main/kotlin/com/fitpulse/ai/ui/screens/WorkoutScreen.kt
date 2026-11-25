package com.fitpulse.ai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.fitpulse.ai.data.model.Exercise
import com.fitpulse.ai.ui.components.MuscleRecoveryTracker
import com.fitpulse.ai.ui.components.RestTimerOverlay
import com.fitpulse.ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    repository: FitnessRepository,
    modifier: Modifier = Modifier
) {
    val workoutDays by repository.workoutDays.collectAsState()
    val selectedDayIndex by repository.selectedDayIndex.collectAsState()
    val muscleRecoveries by repository.muscleRecoveries.collectAsState()
    var activeTimerSeconds by remember { mutableStateOf<Int?>(null) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var savedBanner by remember { mutableStateOf<String?>(null) }

    val currentDay = workoutDays.getOrNull(selectedDayIndex)

    Box(modifier = modifier.fillMaxSize().background(GymDarkBackground)) {
        if (currentDay == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No workout plan generated yet. Visit AI Coach tab!", color = Color.Gray)
            }
        } else {
            val totalExercises = currentDay.exercises.size
            val completedExercises = currentDay.exercises.count { it.isCompleted }
            val progressPercent = if (totalExercises > 0) completedExercises.toFloat() / totalExercises.toFloat() else 0f

            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Today's Training",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = currentDay.focusArea,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElectricCyan
                    )
                }

                // Day Selection Tabs
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(workoutDays) { idx, day ->
                        val isSelected = idx == selectedDayIndex
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) NeonGreen else GymDarkSurface,
                            modifier = Modifier.clickable { repository.selectWorkoutDay(idx) }
                        ) {
                            Text(
                                text = "Day ${idx + 1}",
                                color = if (isSelected) Color.Black else Color.LightGray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Banner Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentDay.dayTitle,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$completedExercises of $totalExercises Completed",
                                color = if (completedExercises == totalExercises && totalExercises > 0) NeonGreen else Color.Gray,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progressPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = NeonGreen,
                                trackColor = GymDarkCard
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(if (completedExercises == totalExercises && totalExercises > 0) NeonGreen.copy(alpha = 0.2f) else GymDarkCard, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (completedExercises == totalExercises && totalExercises > 0) Icons.Default.CheckCircle else Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = if (completedExercises == totalExercises && totalExercises > 0) NeonGreen else ElectricCyan
                            )
                        }
                    }
                }

                // Exercises List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        MuscleRecoveryTracker(
                            recoveryMap = muscleRecoveries,
                            initialExpanded = true
                        )
                    }

                    items(currentDay.exercises, key = { it.id }) { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            onToggleComplete = {
                                repository.toggleExerciseCompleted(selectedDayIndex, exercise.id)
                                if (!exercise.isCompleted) {
                                    activeTimerSeconds = exercise.restSeconds
                                }
                            },
                            onWeightChange = { delta ->
                                val newWeight = (exercise.weightKg + delta).coerceAtLeast(0f)
                                repository.updateExerciseWeightAndReps(selectedDayIndex, exercise.id, newWeight, exercise.reps)
                            },
                            onStartTimer = {
                                activeTimerSeconds = exercise.restSeconds
                            }
                        )
                    }
                    item {
                        savedBanner?.let { msg ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = NeonGreen.copy(alpha = 0.15f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen)
                            ) {
                                Text(
                                    text = msg,
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        Button(
                            onClick = { showFinishDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Finish & Save Workout Session", fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom bar & timer
                    }
                }
            }
        }

        if (showFinishDialog && currentDay != null) {
            var rpe by remember { mutableIntStateOf(7) }
            var notes by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showFinishDialog = false },
                title = { Text("Complete Workout?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Save ${currentDay.dayTitle} to your training history?",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                        Text("Effort / RPE: $rpe / 10", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Slider(
                            value = rpe.toFloat(),
                            onValueChange = { rpe = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen)
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Session Notes (optional)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonGreen
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            repository.completeWorkoutSession(selectedDayIndex, rpe, notes)
                            showFinishDialog = false
                            savedBanner = "✅ Workout session saved to Progress & History!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text("Save Session", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFinishDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = GymDarkSurface
            )
        }

        // Floating Rest Timer Modal
        activeTimerSeconds?.let { seconds ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                RestTimerOverlay(
                    totalSeconds = seconds,
                    onDismiss = { activeTimerSeconds = null },
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onToggleComplete: () -> Unit,
    onWeightChange: (Float) -> Unit,
    onStartTimer: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (exercise.isCompleted) GymDarkSurface.copy(alpha = 0.7f) else GymDarkSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (exercise.isCompleted) NeonGreen.copy(alpha = 0.4f) else GymDarkBorder,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (exercise.isCompleted) NeonGreen else GymDarkCard,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (exercise.isCompleted) Icons.Default.Check else Icons.Default.Circle,
                        contentDescription = "Done",
                        tint = if (exercise.isCompleted) Color.Black else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (exercise.isCompleted) Color.LightGray else Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Badge(containerColor = GymDarkCard, contentColor = ElectricCyan) {
                            Text(exercise.targetMuscle, fontSize = 11.sp)
                        }
                        Badge(containerColor = GymDarkCard, contentColor = EnergeticAmber) {
                            Text("${exercise.sets} sets × ${exercise.reps}", fontSize = 11.sp)
                        }
                    }
                }

                // Rest Timer button
                IconButton(onClick = onStartTimer) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Rest Timer",
                        tint = ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weight adjuster & Tips toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GymDarkCard, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Weight: ", color = Color.Gray, fontSize = 13.sp)
                    Text(
                        text = "${exercise.weightKg} kg",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onWeightChange(-2.5f) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { onWeightChange(2.5f) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = NeonGreen, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.Info,
                            contentDescription = "Coaching Tips",
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Coaching Tip expandable section
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(GymDarkCard.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = EnergeticAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI Coach Tip:", color = EnergeticAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = exercise.coachingTip, color = Color.LightGray, fontSize = 13.sp)
                }
            }
        }
    }
}
