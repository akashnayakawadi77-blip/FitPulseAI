package com.fitpulse.ai.ui.components

import android.view.KeyEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fitpulse.ai.data.WorkoutMusicPlayer
import com.fitpulse.ai.ui.theme.*

@Composable
fun WorkoutMusicBar(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        WorkoutMusicPlayer.init(context)
    }

    val tracks by WorkoutMusicPlayer.tracks.collectAsState()
    val currentIndex by WorkoutMusicPlayer.currentIndex.collectAsState()
    val isPlaying by WorkoutMusicPlayer.isPlaying.collectAsState()
    val volume by WorkoutMusicPlayer.volume.collectAsState()
    val repeatOne by WorkoutMusicPlayer.repeatOne.collectAsState()
    val externalMode by WorkoutMusicPlayer.externalMode.collectAsState()

    var showPlaylistDialog by remember { mutableStateOf(false) }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            WorkoutMusicPlayer.setExternalMode(false)
            WorkoutMusicPlayer.addUserTracks(context, uris)
        }
    }

    val currentTrack = tracks.getOrNull(currentIndex)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        accentGlow = if (externalMode) ElectricCyan else if (isPlaying) NeonGreen else ElectricCyan,
        borderColor = if (externalMode) ElectricCyan.copy(alpha = 0.45f) else if (isPlaying) NeonGreen.copy(alpha = 0.45f) else GymDarkBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPlaylistDialog = true }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        if (externalMode) ElectricCyan.copy(alpha = 0.18f)
                        else if (isPlaying) NeonGreen.copy(alpha = 0.2f)
                        else GymDarkCard,
                        RoundedCornerShape(11.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (externalMode) Icons.Default.Headphones else Icons.Default.MusicNote,
                    contentDescription = "Workout Music",
                    tint = if (externalMode) ElectricCyan else if (isPlaying) NeonGreen else ElectricCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (externalMode) "Amazon / Phone Media" else (currentTrack?.title ?: "Workout Beats"),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (externalMode) "Controlling background music app" else (currentTrack?.subtitle ?: "Tap for Beats, MP3s or Amazon Music"),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = {
                    if (externalMode) {
                        WorkoutMusicPlayer.sendSystemMediaKey(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                    } else {
                        WorkoutMusicPlayer.previousTrack()
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }

            IconButton(
                onClick = {
                    if (externalMode) {
                        WorkoutMusicPlayer.sendSystemMediaKey(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                    } else {
                        WorkoutMusicPlayer.togglePlayPause()
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(if (externalMode) ElectricCyan else NeonGreen, CircleShape)
            ) {
                Icon(
                    imageVector = if (!externalMode && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play or Pause",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = {
                    if (externalMode) {
                        WorkoutMusicPlayer.sendSystemMediaKey(context, KeyEvent.KEYCODE_MEDIA_NEXT)
                    } else {
                        WorkoutMusicPlayer.nextTrack()
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }

            IconButton(
                onClick = { showPlaylistDialog = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "Music Options", tint = ElectricCyan, modifier = Modifier.size(19.dp))
            }
        }
    }

    if (showPlaylistDialog) {
        Dialog(onDismissRequest = { showPlaylistDialog = false }) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.84f),
                borderColor = NeonGreen.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("WORKOUT SOUNDTRACK", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Text("Music & Streaming", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                        IconButton(onClick = { showPlaylistDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // External Music Apps Quick-Launch (Amazon Music, Spotify, YT Music)
                    Text("Stream From Your Music App", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                WorkoutMusicPlayer.openExternalMusicApp(
                                    context,
                                    "com.amazon.mp3",
                                    "https://music.amazon.in"
                                )
                            },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Headphones, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Amazon Music", fontWeight = FontWeight.Black, fontSize = 12.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                WorkoutMusicPlayer.openExternalMusicApp(
                                    context,
                                    "com.spotify.music",
                                    "https://open.spotify.com"
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GymDarkBorder),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Text("Spotify", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mode Toggle Pill: FitPanda Player vs External App Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GymDarkCard, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        listOf("FitPanda Beats / MP3" to false, "External App Control" to true).forEach { (label, isExt) ->
                            val sel = externalMode == isExt
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { WorkoutMusicPlayer.setExternalMode(isExt) },
                                shape = RoundedCornerShape(9.dp),
                                color = if (sel) (if (isExt) ElectricCyan else NeonGreen) else Color.Transparent
                            ) {
                                Box(modifier = Modifier.padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        color = if (sel) Color.Black else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Add Phone MP3s + Loop Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { audioPicker.launch(arrayOf("audio/*")) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Default.LibraryMusic, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Phone MP3s", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { WorkoutMusicPlayer.toggleRepeatOne() },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan)
                        ) {
                            Icon(
                                imageVector = if (repeatOne) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                contentDescription = "Repeat Mode",
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (repeatOne) "Loop 1" else "Loop All", color = ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Volume Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = volume,
                            onValueChange = { WorkoutMusicPlayer.setVolume(it) },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(tracks, key = { _, t -> t.id }) { index, track ->
                            val isCurrent = !externalMode && index == currentIndex
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isCurrent) NeonGreen.copy(alpha = 0.14f) else GymDarkCard)
                                    .border(
                                        1.dp,
                                        if (isCurrent) NeonGreen else GymDarkBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        WorkoutMusicPlayer.setExternalMode(false)
                                        WorkoutMusicPlayer.playTrack(index)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isCurrent && isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = if (isCurrent) NeonGreen else ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        color = if (isCurrent) NeonGreen else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(track.subtitle, color = TextSecondary, fontSize = 11.sp)
                                }
                                if (!track.isBuiltIn) {
                                    IconButton(
                                        onClick = { WorkoutMusicPlayer.removeUserTrack(context, track.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(17.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
