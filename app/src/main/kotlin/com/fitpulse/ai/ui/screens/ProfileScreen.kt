package com.fitpulse.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitpulse.ai.data.FitnessRepository
import com.fitpulse.ai.data.model.FitnessGoal
import com.fitpulse.ai.data.model.FitnessLevel
import com.fitpulse.ai.data.model.UserProfile
import com.fitpulse.ai.data.model.WorkoutEnvironment
import com.fitpulse.ai.ui.components.MuscleRecoveryTracker
import com.fitpulse.ai.ui.theme.*

@Composable
fun ProfileScreen(
    repository: FitnessRepository,
    modifier: Modifier = Modifier
) {
    val userProfile by repository.userProfile.collectAsState()
    val weightLogs by repository.weightLogs.collectAsState()
    val muscleRecoveries by repository.muscleRecoveries.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogWeightDialog by remember { mutableStateOf(false) }

    val heightM = userProfile.heightCm / 100f
    val bmi = if (heightM > 0) userProfile.weightKg / (heightM * heightM) else 0f
    val bmiFormatted = String.format("%.1f", bmi)
    val bmiStatus = when {
        bmi < 18.5f -> "Underweight"
        bmi < 24.9f -> "Healthy Weight"
        bmi < 29.9f -> "Overweight"
        else -> "Obese"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GymDarkBackground)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Athlete Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            IconButton(
                onClick = { showEditProfileDialog = true },
                modifier = Modifier
                    .background(GymDarkSurface, CircleShape)
                    .border(1.dp, GymDarkBorder, CircleShape)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = NeonGreen)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Summary Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(NeonGreen.copy(alpha = 0.2f), CircleShape)
                                .border(2.dp, NeonGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(40.dp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = userProfile.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.White
                        )

                        Text(
                            text = "${userProfile.fitnessLevel.displayName} • ${userProfile.age} yrs old",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Badge(containerColor = GymDarkCard, contentColor = ElectricCyan) {
                            Text("Goal: ${userProfile.fitnessGoal.displayName}", fontSize = 12.sp, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }

            // Body Metrics Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(label = "Current Weight", value = "${userProfile.weightKg} kg", color = NeonGreen, modifier = Modifier.weight(1f))
                    MetricCard(label = "Height", value = "${userProfile.heightCm.toInt()} cm", color = ElectricCyan, modifier = Modifier.weight(1f))
                    MetricCard(label = "BMI", value = "$bmiFormatted ($bmiStatus)", color = EnergeticAmber, modifier = Modifier.weight(1.2f))
                }
            }

            // Muscle Recovery Status
            item {
                MuscleRecoveryTracker(recoveryMap = muscleRecoveries, initialExpanded = false)
            }

            // Weight Log & Progress Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Weight Journey", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Track your body composition progress", color = Color.Gray, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showLogWeightDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Weight", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            items(weightLogs, key = { it.timestamp }) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(GymDarkCard, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(log.dateDisplay, color = Color.LightGray, fontSize = 14.sp)
                        }

                        Text(
                            text = "${log.weightKg} kg",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Log Weight Dialog
    if (showLogWeightDialog) {
        var inputWeight by remember { mutableStateOf(userProfile.weightKg.toString()) }
        AlertDialog(
            onDismissRequest = { showLogWeightDialog = false },
            title = { Text("Log Today's Weight", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputWeight,
                    onValueChange = { inputWeight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonGreen
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = inputWeight.toFloatOrNull()
                        if (parsed != null && parsed > 20f) {
                            repository.logCurrentWeight(parsed)
                        }
                        showLogWeightDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text("Save", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogWeightDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = GymDarkSurface
        )
    }

    // Edit Full Profile Dialog
    if (showEditProfileDialog) {
        var name by remember { mutableStateOf(userProfile.name) }
        var age by remember { mutableStateOf(userProfile.age.toString()) }
        var height by remember { mutableStateOf(userProfile.heightCm.toInt().toString()) }
        var weight by remember { mutableStateOf(userProfile.weightKg.toString()) }
        var selectedGoal by remember { mutableStateOf(userProfile.fitnessGoal) }
        var selectedEnv by remember { mutableStateOf(userProfile.workoutEnvironment) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Update Fitness Profile", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Age") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("Height (cm)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                    Text("Fitness Goal:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    FitnessGoal.values().forEach { goal ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedGoal == goal,
                                onClick = { selectedGoal = goal },
                                colors = RadioButtonDefaults.colors(selectedColor = NeonGreen)
                            )
                            Text(goal.displayName, color = Color.LightGray, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = userProfile.copy(
                            name = name.ifBlank { "Warrior" },
                            age = age.toIntOrNull() ?: userProfile.age,
                            heightCm = height.toFloatOrNull() ?: userProfile.heightCm,
                            weightKg = weight.toFloatOrNull() ?: userProfile.weightKg,
                            fitnessGoal = selectedGoal,
                            workoutEnvironment = selectedEnv
                        )
                        repository.updateProfile(updated)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text("Save & Apply", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = GymDarkSurface
        )
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, color = Color.Gray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
