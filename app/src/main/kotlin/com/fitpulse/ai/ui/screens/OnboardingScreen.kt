package com.fitpulse.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.fitpulse.ai.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    repository: FitnessRepository,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val existingProfile by repository.userProfile.collectAsState()
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf(if (existingProfile.name == "Fitness Warrior") "" else existingProfile.name) }
    var ageText by remember { mutableStateOf(existingProfile.age.toString()) }
    var weightText by remember { mutableStateOf(existingProfile.weightKg.toString()) }
    var heightText by remember { mutableStateOf(existingProfile.heightCm.toString()) }
    var selectedGoal by remember { mutableStateOf(existingProfile.fitnessGoal) }
    var selectedLevel by remember { mutableStateOf(existingProfile.fitnessLevel) }
    var selectedEnv by remember { mutableStateOf(existingProfile.workoutEnvironment) }
    var daysPerWeek by remember { mutableIntStateOf(existingProfile.daysPerWeek) }
    var isSaving by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val totalSteps = 4

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GymDarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        // Top Progress Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 0) {
                IconButton(
                    onClick = { step-- },
                    modifier = Modifier
                        .size(40.dp)
                        .background(GymDarkSurface, CircleShape)
                        .border(1.dp, GymDarkBorder, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            } else {
                Spacer(modifier = Modifier.size(40.dp))
            }

            Text(
                text = "Step ${step + 1} of $totalSteps",
                color = ElectricCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            TextButton(
                onClick = {
                    if (!isSaving) {
                        isSaving = true
                        scope.launch {
                            repository.completeOnboarding(
                                existingProfile.copy(
                                    name = name.trim().ifBlank { "Athlete" },
                                    onboardingCompleted = true
                                )
                            )
                            isSaving = false
                            onCompleted()
                        }
                    }
                }
            ) {
                Text("Skip", color = Color.Gray, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { (step + 1) / totalSteps.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = NeonGreen,
            trackColor = GymDarkSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (step) {
                0 -> {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(NeonGreen.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, NeonGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(30.dp))
                    }

                    Text(
                        text = "Welcome to FitPanda",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Let's personalize your training, nutrition, and recovery engine.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Your Name") },
                        placeholder = { Text("e.g., Akash") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = GymDarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = NeonGreen,
                            unfocusedLabelColor = Color.Gray
                        )
                    )

                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { if (it.length <= 2) ageText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Age (years)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = GymDarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = NeonGreen,
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                }

                1 -> {
                    Text(
                        text = "Body Metrics",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Used to calculate your exact TDEE, daily calories, and protein targets.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Current Weight (kg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = GymDarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = NeonGreen,
                            unfocusedLabelColor = Color.Gray
                        )
                    )

                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Height (cm)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = GymDarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = NeonGreen,
                            unfocusedLabelColor = Color.Gray
                        )
                    )

                    val w = weightText.toFloatOrNull() ?: 75f
                    val h = (heightText.toFloatOrNull() ?: 178f) / 100f
                    val bmi = if (h > 0f) w / (h * h) else 0f
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Estimated BMI", color = Color.Gray, fontSize = 12.sp)
                                Text(String.format("%.1f", bmi), color = ElectricCyan, fontWeight = FontWeight.Black, fontSize = 22.sp)
                            }
                            Text(
                                text = when {
                                    bmi < 18.5f -> "Lean / Underweight"
                                    bmi < 25f -> "Healthy Range"
                                    bmi < 30f -> "Overweight"
                                    else -> "High BMI"
                                },
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                2 -> {
                    Text(
                        text = "What's your primary goal?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "We tailor your workout split, rep ranges, and macro ratios around this.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    FitnessGoal.entries.forEach { goal ->
                        val isSelected = selectedGoal == goal
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedGoal = goal },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) NeonGreen.copy(alpha = 0.12f) else GymDarkSurface
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) NeonGreen else GymDarkBorder)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(goal.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(goal.description, color = Color.Gray, fontSize = 12.sp)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen)
                                }
                            }
                        }
                    }
                }

                3 -> {
                    Text(
                        text = "Training Setup",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text("Where and how often will you train?", fontSize = 14.sp, color = Color.Gray)

                    Text("Experience Level", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    FitnessLevel.entries.forEach { lvl ->
                        val sel = selectedLevel == lvl
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLevel = lvl },
                            shape = RoundedCornerShape(12.dp),
                            color = if (sel) NeonGreen.copy(alpha = 0.12f) else GymDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (sel) NeonGreen else GymDarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(lvl.displayName, color = Color.White, fontSize = 14.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                                if (sel) Icon(Icons.Default.Check, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Equipment", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    WorkoutEnvironment.entries.forEach { env ->
                        val sel = selectedEnv == env
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedEnv = env },
                            shape = RoundedCornerShape(12.dp),
                            color = if (sel) NeonGreen.copy(alpha = 0.12f) else GymDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (sel) NeonGreen else GymDarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(env.displayName, color = Color.White, fontSize = 14.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                                if (sel) Icon(Icons.Default.Check, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Workout Days / Week: $daysPerWeek days", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(3, 4, 5, 6).forEach { d ->
                            val sel = daysPerWeek == d
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { daysPerWeek = d },
                                shape = RoundedCornerShape(12.dp),
                                color = if (sel) NeonGreen else GymDarkSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (sel) NeonGreen else GymDarkBorder)
                            ) {
                                Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                    Text("$d", color = if (sel) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (step < totalSteps - 1) {
                    step++
                } else if (!isSaving) {
                    isSaving = true
                    val finalProfile = UserProfile(
                        name = name.trim().ifBlank { "Athlete" },
                        age = ageText.toIntOrNull()?.coerceIn(13, 95) ?: 25,
                        weightKg = weightText.toFloatOrNull()?.coerceIn(30f, 250f) ?: 75f,
                        heightCm = heightText.toFloatOrNull()?.coerceIn(120f, 230f) ?: 178f,
                        fitnessGoal = selectedGoal,
                        fitnessLevel = selectedLevel,
                        workoutEnvironment = selectedEnv,
                        daysPerWeek = daysPerWeek,
                        geminiApiKey = existingProfile.geminiApiKey,
                        onboardingCompleted = true
                    )
                    scope.launch {
                        repository.completeOnboarding(finalProfile)
                        isSaving = false
                        onCompleted()
                    }
                }
            },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.Black, strokeWidth = 2.5.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Building Your Plan...", fontWeight = FontWeight.Black)
            } else {
                Text(
                    text = if (step < totalSteps - 1) "Continue" else "Build My Fitness Plan",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
