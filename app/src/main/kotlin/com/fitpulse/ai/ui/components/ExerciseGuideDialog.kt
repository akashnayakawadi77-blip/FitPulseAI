package com.fitpulse.ai.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fitpulse.ai.data.model.Exercise
import com.fitpulse.ai.data.model.MuscleGroup
import com.fitpulse.ai.ui.theme.*

@Composable
fun ExerciseGuideDialog(
    exercise: Exercise,
    onDismiss: () -> Unit
) {
    val muscleGroup = MuscleGroup.fromTargetMuscle(exercise.targetMuscle)

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f),
            borderColor = ElectricCyan.copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("FORM & MUSCLE GUIDE", color = ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text(exercise.name, color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Target Muscle Diagram + Key Stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GymDarkCard, RoundedCornerShape(16.dp))
                        .border(1.dp, GymDarkBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = MuscleImageHelper.drawableForMuscle(muscleGroup)),
                        contentDescription = muscleGroup.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Primary Target", color = TextSecondary, fontSize = 11.sp)
                        Surface(shape = RoundedCornerShape(8.dp), color = NeonGreen.copy(alpha = 0.16f)) {
                            Text(
                                text = muscleGroup.displayName,
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text("Volume: ${exercise.sets} sets × ${exercise.reps}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Rest: ${exercise.restSeconds}s • Tempo: 2-1-2", color = ElectricCyan, fontSize = 12.sp)
                    }
                }

                GuideStepCard(
                    stepNumber = "1",
                    title = "Setup & Posture",
                    body = "Brace your core, keep your spine neutral, and position joints smoothly over your base of support before lifting.",
                    accent = ElectricCyan
                )
                GuideStepCard(
                    stepNumber = "2",
                    title = "Execution & Tempo",
                    body = "Lower the weight under control for 2 seconds, pause briefly at peak stretch, then drive smoothly without swinging.",
                    accent = NeonGreen
                )
                GuideStepCard(
                    stepNumber = "3",
                    title = "Breathing & Pro Cue",
                    body = "${exercise.coachingTip} Exhale on exertion (push/pull) and inhale during the lowering phase.",
                    accent = EnergeticAmber
                )

                GradientPrimaryButton(
                    text = "Got It — Back to Workout",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun GuideStepCard(
    stepNumber: String,
    title: String,
    body: String,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GymDarkCard.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(accent.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(stepNumber, color = accent, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(3.dp))
            Text(body, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}
