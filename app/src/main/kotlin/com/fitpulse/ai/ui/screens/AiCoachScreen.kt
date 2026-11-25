package com.fitpulse.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitpulse.ai.data.FitnessRepository
import com.fitpulse.ai.data.model.FitnessGoal
import com.fitpulse.ai.data.model.WorkoutEnvironment
import com.fitpulse.ai.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AiCoachScreen(
    repository: FitnessRepository,
    modifier: Modifier = Modifier
) {
    val userProfile by repository.userProfile.collectAsState()
    val chatMessages by repository.chatMessages.collectAsState()
    val isGenerating by repository.isGenerating.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var userQuestion by remember { mutableStateOf("") }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    val quickQuestions = listOf(
        "Upper chest hypertrophy exercises?",
        "Daily protein target for muscle gain?",
        "How to avoid lower back pain in squats?",
        "Effective 30-min fat loss workout?"
    )

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
            Column {
                Text(
                    text = "AI Fitness Coach",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = if (userProfile.geminiApiKey.isNotBlank()) "Powered by Gemini 2.0 Flash" else "Powered by Smart AI Engine",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (userProfile.geminiApiKey.isNotBlank()) ElectricCyan else NeonGreen
                )
            }

            IconButton(
                onClick = { showApiKeyDialog = true },
                modifier = Modifier
                    .background(GymDarkSurface, CircleShape)
                    .border(1.dp, GymDarkBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "API Key",
                    tint = if (userProfile.geminiApiKey.isNotBlank()) NeonGreen else Color.Gray
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // AI Plan Generator Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonGreen)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Personalized Routine Generator", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Text("Tailored to ${userProfile.fitnessGoal.displayName} • ${userProfile.workoutEnvironment.displayName}", color = Color.Gray, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    repository.generateNewPlan()
                                }
                            },
                            enabled = !isGenerating,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Designing Custom Routine...", color = Color.Black, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Fresh AI Plan", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Quick Question Chips
            item {
                Text("Quick Inquiries", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickQuestions) { question ->
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    repository.sendChatMessage(question)
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = GymDarkSurface)
                        ) {
                            Text(question, color = ElectricCyan, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Chat Messages Header
            item {
                Text("Coach Consultation", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            // Chat Bubbles
            items(chatMessages, key = { it.id }) { msg ->
                val isUser = msg.isUser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .background(
                                color = if (isUser) NeonGreen.copy(alpha = 0.9f) else GymDarkSurface,
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 2.dp,
                                    bottomEnd = if (isUser) 2.dp else 16.dp
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = if (isUser) NeonGreen else GymDarkBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg.message,
                            color = if (isUser) Color.Black else Color.White,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Bottom Chat Input Bar
        Surface(
            color = GymDarkSurface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userQuestion,
                    onValueChange = { userQuestion = it },
                    placeholder = { Text("Ask your coach anything...", color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = GymDarkBorder,
                        focusedContainerColor = GymDarkCard,
                        unfocusedContainerColor = GymDarkCard
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val q = userQuestion.trim()
                        if (q.isNotBlank()) {
                            userQuestion = ""
                            coroutineScope.launch {
                                repository.sendChatMessage(q)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .background(NeonGreen, CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    // Gemini API Key Config Dialog
    if (showApiKeyDialog) {
        var tempKey by remember { mutableStateOf(userProfile.geminiApiKey) }
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("Gemini API Key Settings", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Paste your Google Gemini API key to activate live Gemini 1.5 Flash AI generation. If left empty, the app uses its built-in smart AI engine seamlessly.",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
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
                        repository.updateProfile(userProfile.copy(geminiApiKey = tempKey.trim()))
                        showApiKeyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text("Save", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = GymDarkSurface
        )
    }
}
