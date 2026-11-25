package com.fitpulse.ai.ui.screens

import android.content.Intent
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitpulse.ai.data.FitnessRepository
import com.fitpulse.ai.data.model.WeightLogEntry
import com.fitpulse.ai.ui.components.MuscleRecoveryTracker
import com.fitpulse.ai.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    repository: FitnessRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val muscleRecoveries by repository.muscleRecoveries.collectAsState()
    val weightLogs by repository.weightLogs.collectAsState()
    val workoutSessions by repository.workoutSessions.collectAsState()
    val userProfile by repository.userProfile.collectAsState()

    var showLogWeightDialog by remember { mutableStateOf(false) }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    val heightM = userProfile.heightCm / 100f
    val bmi = if (heightM > 0) userProfile.weightKg / (heightM * heightM) else 0f
    val bmiFormatted = String.format("%.1f", bmi)

    val sessionsThisWeek = remember(workoutSessions) { repository.countSessionsThisWeek() }
    val weeklyActive = remember(workoutSessions) { repository.getWeeklyActiveDays() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GymDarkBackground)
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Progress & Performance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Recovery heatmap, consistency & body trends",
                style = MaterialTheme.typography.bodySmall,
                color = ElectricCyan
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 1: Muscle Recovery Radar
            item {
                MuscleRecoveryTracker(
                    recoveryMap = muscleRecoveries,
                    initialExpanded = true
                )
            }

            // Section 2: Weekly Performance & Consistency (Real session data)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Weekly Training Consistency", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("Target: ${userProfile.daysPerWeek} days/week", color = Color.Gray, fontSize = 11.sp)
                            }
                            Badge(containerColor = NeonGreen.copy(alpha = 0.2f), contentColor = NeonGreen) {
                                Text("$sessionsThisWeek / ${userProfile.daysPerWeek} Completed", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            days.forEachIndexed { i, d ->
                                val isAct = weeklyActive.getOrElse(i) { false }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(if (isAct) NeonGreen else GymDarkCard, CircleShape)
                                            .border(1.dp, if (isAct) NeonGreen else GymDarkBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isAct) "✓" else "·",
                                            color = if (isAct) Color.Black else Color.Gray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(d, color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Weight Journey & Real Trend Chart
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Weight Journey", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Current: ${userProfile.weightKg} kg • BMI: $bmiFormatted", color = Color.Gray, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showLogWeightDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Weight", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            item {
                WeightTrendChartCard(logs = weightLogs)
            }

            items(weightLogs.take(4), key = { it.timestamp }) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(GymDarkCard, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(log.dateDisplay, color = Color.LightGray, fontSize = 13.sp)
                        }

                        Text(
                            text = "${log.weightKg} kg",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Section 4: Completed Workout History
            if (workoutSessions.isNotEmpty()) {
                item {
                    Text("Recent Workout Sessions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                items(workoutSessions.take(5), key = { it.id }) { session ->
                    val dateStr = remember(session.startTimeMillis) {
                        SimpleDateFormat("MMM dd • hh:mm a", Locale.getDefault()).format(Date(session.startTimeMillis))
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(session.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = "$dateStr • ${session.completedExercisesCount}/${session.totalExercisesCount} exercises",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                            Badge(containerColor = ElectricCyan.copy(alpha = 0.15f), contentColor = ElectricCyan) {
                                Text("${session.totalVolumeKg.toInt()} kg vol", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section 5: Data Ownership & Real CSV Export
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Data Ownership & Export", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Share or save all your workout sessions, weight logs, and food history in CSV format.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                val csv = repository.exportProgressCsv()
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_SUBJECT, "FitPulseAI Progress Export")
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export FitPulseAI CSV"))
                                exportSuccessMessage = "CSV ready to share/save!"
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GymDarkBorder)
                        ) {
                            Text("Export & Share CSV", color = ElectricCyan, fontSize = 12.sp)
                        }
                        exportSuccessMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(msg, color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

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
}

@Composable
private fun WeightTrendChartCard(logs: List<WeightLogEntry>) {
    val chronological = remember(logs) { logs.take(10).reversed() }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Weight Trend", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                if (chronological.size >= 2) {
                    val delta = chronological.last().weightKg - chronological.first().weightKg
                    val sign = if (delta >= 0) "+" else ""
                    Text(
                        text = "$sign${String.format("%.1f", delta)} kg",
                        color = if (delta <= 0) NeonGreen else EnergeticAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (chronological.isEmpty()) {
                Text("Log your weight to see your trend chart.", color = Color.Gray, fontSize = 12.sp)
            } else {
                val minW = (chronological.minOf { it.weightKg } - 1.5f).coerceAtLeast(20f)
                val maxW = chronological.maxOf { it.weightKg } + 1.5f
                val span = (maxW - minW).coerceAtLeast(1f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val padX = 16f
                    val padY = 16f
                    val usableW = (w - 2 * padX).coerceAtLeast(1f)
                    val usableH = (h - 2 * padY).coerceAtLeast(1f)

                    val points = chronological.mapIndexed { idx, entry ->
                        val x = if (chronological.size == 1) w / 2f else padX + (idx.toFloat() / (chronological.size - 1)) * usableW
                        val normY = (entry.weightKg - minW) / span
                        val y = padY + (1f - normY) * usableH
                        Offset(x, y)
                    }

                    if (points.size >= 2) {
                        val fillPath = Path().apply {
                            moveTo(points.first().x, h)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(points.last().x, h)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(NeonGreen.copy(alpha = 0.30f), Color.Transparent)
                            )
                        )

                        val linePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        drawPath(
                            path = linePath,
                            color = NeonGreen,
                            style = Stroke(width = 5f, cap = StrokeCap.Round)
                        )
                    }

                    points.forEach { pt ->
                        drawCircle(color = GymDarkBackground, radius = 9f, center = pt)
                        drawCircle(color = ElectricCyan, radius = 6f, center = pt)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(chronological.first().dateDisplay, color = Color.Gray, fontSize = 10.sp)
                    Text(
                        text = "${chronological.minOf { it.weightKg }}–${chronological.maxOf { it.weightKg }} kg",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    Text(chronological.last().dateDisplay, color = Color.Gray, fontSize = 10.sp)
                }
            }
        }
    }
}
