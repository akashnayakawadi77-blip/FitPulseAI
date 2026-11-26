package com.fitpulse.ai.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitpulse.ai.data.FitnessRepository
import com.fitpulse.ai.data.ImageStorageHelper
import com.fitpulse.ai.data.ReminderScheduler
import com.fitpulse.ai.data.model.FitnessGoal
import com.fitpulse.ai.data.model.FitnessLevel
import com.fitpulse.ai.data.model.UserProfile
import com.fitpulse.ai.data.model.WorkoutEnvironment
import com.fitpulse.ai.ui.components.GlassCard
import com.fitpulse.ai.ui.components.MuscleRecoveryTracker
import com.fitpulse.ai.ui.theme.*

@Composable
fun ProfileScreen(
    repository: FitnessRepository,
    onBack: (() -> Unit)? = null,
    onRerunOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by repository.userProfile.collectAsState()
    val weightLogs by repository.weightLogs.collectAsState()
    val muscleRecoveries by repository.muscleRecoveries.collectAsState()
    val reminderSettings by repository.reminderSettings.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogWeightDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var backupStatusMsg by remember { mutableStateOf<String?>(null) }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val raw = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
            backupStatusMsg = if (raw != null && repository.importFullBackupJson(raw)) {
                "✅ Backup restored successfully!"
            } else {
                "⚠️ Invalid backup file."
            }
        }
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val profilePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.saveImageFromUri(context, uri, "profile")
            if (savedPath != null) {
                repository.updateProfilePhoto(savedPath)
            }
        }
    }

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
            .statusBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(GymDarkSurface, CircleShape)
                            .border(1.dp, GymDarkBorder, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = "Athlete Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

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
                val profileBitmap = remember(userProfile.profilePhotoPath) {
                    ImageStorageHelper.loadImageBitmap(userProfile.profilePhotoPath)
                }
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = NeonGreen,
                    borderColor = NeonGreen.copy(alpha = 0.25f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clickable {
                                    profilePhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen.copy(alpha = 0.18f))
                                    .border(2.dp, NeonGreen, CircleShape),
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
                                    Icon(Icons.Default.Person, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(44.dp))
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(28.dp)
                                    .background(ElectricCyan, CircleShape)
                                    .border(2.dp, GymDarkBackground, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Upload Photo", tint = Color.Black, modifier = Modifier.size(15.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (profileBitmap != null) "Tap photo to change" else "Tap to upload your photo",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = userProfile.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 21.sp,
                            color = TextPrimary
                        )

                        Text(
                            text = "${userProfile.fitnessLevel.displayName} • ${userProfile.age} yrs old",
                            fontSize = 13.sp,
                            color = TextSecondary
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

            // Reminders & Notifications Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Smart Reminders", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Daily Workout Reminder", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("Reminds you at 6:00 PM daily", color = Color.Gray, fontSize = 11.sp)
                            }
                            Switch(
                                checked = reminderSettings.workoutReminderEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    val updated = reminderSettings.copy(workoutReminderEnabled = enabled)
                                    repository.updateReminderSettings(updated)
                                    ReminderScheduler.applySettings(context, updated)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonGreen)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Hydration Reminder", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("Every 2 hours for peak recovery", color = Color.Gray, fontSize = 11.sp)
                            }
                            Switch(
                                checked = reminderSettings.waterReminderEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    val updated = reminderSettings.copy(waterReminderEnabled = enabled)
                                    repository.updateReminderSettings(updated)
                                    ReminderScheduler.applySettings(context, updated)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = ElectricCyan)
                            )
                        }
                    }
                }
            }

            // Account & Data Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Account & Data", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)

                        OutlinedButton(
                            onClick = {
                                val jsonBackup = repository.exportFullBackupJson()
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_SUBJECT, "FitPanda Backup")
                                    putExtra(Intent.EXTRA_TEXT, jsonBackup)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Save FitPanda Backup"))
                                backupStatusMsg = "Backup ready to save to Drive/Files!"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen)
                        ) {
                            Text("Export Full Backup (JSON)", color = NeonGreen, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { restoreBackupLauncher.launch("application/json") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan)
                        ) {
                            Text("Restore from Backup File", color = ElectricCyan, fontWeight = FontWeight.Bold)
                        }

                        backupStatusMsg?.let { msg ->
                            Text(msg, color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onRerunOnboarding,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan)
                        ) {
                            Text("Re-run Goal Setup Wizard", color = ElectricCyan, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showResetConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252))
                        ) {
                            Text("Reset All App Data", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset All Data?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will permanently clear your workout history, weight logs, food logs, and profile on this device.",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.resetAllData()
                        showResetConfirmDialog = false
                        onRerunOnboarding()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Reset", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = GymDarkSurface
        )
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
