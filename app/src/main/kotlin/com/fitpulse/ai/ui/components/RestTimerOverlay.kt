package com.fitpulse.ai.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitpulse.ai.ui.theme.ElectricCyan
import com.fitpulse.ai.ui.theme.GymDarkCard
import com.fitpulse.ai.ui.theme.GymDarkSurface
import com.fitpulse.ai.ui.theme.NeonGreen
import kotlinx.coroutines.delay

@Composable
fun RestTimerOverlay(
    totalSeconds: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var secondsLeft by remember(totalSeconds) { mutableIntStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isRunning, secondsLeft) {
        if (isRunning && secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }
    }

    val progress = if (totalSeconds > 0) secondsLeft.toFloat() / totalSeconds.toFloat() else 0f
    val minutes = secondsLeft / 60
    val secs = secondsLeft % 60
    val timeFormatted = String.format("%02d:%02d", minutes, secs)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = NeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rest Timer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time countdown display
            Text(
                text = timeFormatted,
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = if (secondsLeft <= 5) Color(0xFFFF5252) else NeonGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (secondsLeft <= 5) Color(0xFFFF5252) else ElectricCyan,
                trackColor = GymDarkCard
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { secondsLeft = (secondsLeft + 30).coerceAtMost(300) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+30s", color = NeonGreen)
                }

                FilledIconButton(
                    onClick = { isRunning = !isRunning },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = NeonGreen),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Play",
                        tint = Color.Black
                    )
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GymDarkCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Skip", color = Color.White)
                }
            }
        }
    }
}
