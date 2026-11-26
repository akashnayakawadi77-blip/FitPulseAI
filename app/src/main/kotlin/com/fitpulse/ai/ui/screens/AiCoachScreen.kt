package com.fitpulse.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitpulse.ai.data.FitnessRepository
import com.fitpulse.ai.ui.components.GlassCard
import com.fitpulse.ai.ui.components.GradientPrimaryButton
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
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    var userQuestion by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var planGeneratedBanner by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    val quickQuestions = listOf(
        "Upper chest hypertrophy exercises?",
        "Daily protein target for muscle gain?",
        "How to avoid lower back pain in squats?",
        "Effective 30-min fat loss workout?"
    )

    // Auto-scroll to newest message or typing indicator
    LaunchedEffect(chatMessages.size, isSending) {
        val totalItems = 3 + chatMessages.size + (if (isSending) 1 else 0)
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    fun sendQuestion(text: String) {
        val q = text.trim()
        if (q.isBlank() || isSending) return
        userQuestion = ""
        keyboardController?.hide()
        isSending = true
        coroutineScope.launch {
            try {
                repository.sendChatMessage(q)
            } finally {
                isSending = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GymDarkBackground)
            .imePadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI Fitness Coach",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = if (userProfile.geminiApiKey.isNotBlank()) "Powered by Gemini 2.0 Flash" else "Powered by Smart AI Engine",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (userProfile.geminiApiKey.isNotBlank()) ElectricCyan else NeonGreen
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { repository.clearChatHistory() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(GymDarkSurface, CircleShape)
                        .border(1.dp, GymDarkBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear Chat",
                        tint = TextSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = { showApiKeyDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(GymDarkSurface, CircleShape)
                        .border(1.dp, GymDarkBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "API Key",
                        tint = if (userProfile.geminiApiKey.isNotBlank()) NeonGreen else TextSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            // AI Plan Generator Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = NeonGreen,
                    borderColor = NeonGreen.copy(alpha = 0.25f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(NeonGreen.copy(alpha = 0.16f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonGreen)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Personalized Routine Generator", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                                Text("Tailored to ${userProfile.fitnessGoal.displayName} • ${userProfile.workoutEnvironment.displayName}", color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        GradientPrimaryButton(
                            text = if (isGenerating) "Designing Custom Routine..." else "Generate Fresh AI Plan",
                            onClick = {
                                planGeneratedBanner = false
                                coroutineScope.launch {
                                    repository.generateNewPlan()
                                    planGeneratedBanner = true
                                }
                            },
                            enabled = !isGenerating,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                if (isGenerating) {
                                    CircularProgressIndicator(color = Color(0xFF041009), modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF041009), modifier = Modifier.size(18.dp))
                                }
                            }
                        )

                        if (planGeneratedBanner) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✅ Fresh workout & diet plan ready! Check Workout & Nutrition tabs.",
                                color = NeonGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Quick Question Chips
            item {
                Text("Quick Inquiries", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickQuestions) { question ->
                        OutlinedButton(
                            onClick = { sendQuestion(question) },
                            enabled = !isSending,
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GymDarkBorder),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = GymDarkSurfaceElevated)
                        ) {
                            Text(question, color = ElectricCyan, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Chat Messages Header
            item {
                Text("Coach Consultation", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                                color = if (isUser) NeonGreen.copy(alpha = 0.92f) else GymDarkSurfaceElevated,
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
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
                            color = if (isUser) Color(0xFF041009) else TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            if (isSending) {
                item(key = "typing_indicator") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Row(
                            modifier = Modifier
                                .background(GymDarkSurfaceElevated, RoundedCornerShape(16.dp))
                                .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = ElectricCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Coach is thinking...", color = ElectricCyan, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Bottom Chat Input Bar
        Surface(
            color = GymDarkSurface,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GymDarkBorder)
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
                    placeholder = { Text("Ask your coach anything...", color = TextSecondary, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendQuestion(userQuestion) }),
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
                    onClick = { sendQuestion(userQuestion) },
                    enabled = userQuestion.isNotBlank() && !isSending,
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            if (userQuestion.isNotBlank() && !isSending) NeonGreen else GymDarkCard,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (userQuestion.isNotBlank() && !isSending) Color.Black else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
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
                        "Paste your Google Gemini API key to activate live Gemini 2.0 Flash AI generation and meal photo scanning. If left empty, the app uses its built-in smart AI engine.",
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
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
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
