package com.fitpulse.ai.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.fitpulse.ai.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class WorkoutTrack(
    val id: String,
    val title: String,
    val subtitle: String,
    val uriString: String,
    val isBuiltIn: Boolean = false
)

object WorkoutMusicPlayer {

    private var player: ExoPlayer? = null
    private var appContext: Context? = null
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _tracks = MutableStateFlow<List<WorkoutTrack>>(emptyList())
    val tracks: StateFlow<List<WorkoutTrack>> = _tracks.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _volume = MutableStateFlow(0.85f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _repeatOne = MutableStateFlow(true)
    val repeatOne: StateFlow<Boolean> = _repeatOne.asStateFlow()

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
            loadTracks(appContext!!)
        }
        if (player == null) {
            player = ExoPlayer.Builder(appContext!!).build().apply {
                repeatMode = if (_repeatOne.value) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_ALL
                volume = _volume.value
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED && !_repeatOne.value) {
                            nextTrack()
                        }
                    }
                })
            }
        }
    }

    private fun builtInTracks(context: Context): List<WorkoutTrack> {
        val pkg = context.packageName
        return listOf(
            WorkoutTrack(
                id = "builtin_heavy_lift",
                title = "Heavy Lift Bass",
                subtitle = "Built-in • 105 BPM",
                uriString = "android.resource://$pkg/${R.raw.beat_heavy_lift}",
                isBuiltIn = true
            ),
            WorkoutTrack(
                id = "builtin_hiit_power",
                title = "HIIT Power Drive",
                subtitle = "Built-in • 138 BPM",
                uriString = "android.resource://$pkg/${R.raw.beat_hiit_power}",
                isBuiltIn = true
            ),
            WorkoutTrack(
                id = "builtin_cardio_flow",
                title = "Cardio Flow",
                subtitle = "Built-in • 124 BPM",
                uriString = "android.resource://$pkg/${R.raw.beat_cardio_flow}",
                isBuiltIn = true
            ),
            WorkoutTrack(
                id = "builtin_cooldown_zen",
                title = "Cool-Down & Stretch",
                subtitle = "Built-in • 78 BPM",
                uriString = "android.resource://$pkg/${R.raw.beat_cooldown_zen}",
                isBuiltIn = true
            )
        )
    }

    private fun loadTracks(context: Context) {
        val prefs = context.getSharedPreferences("fitpanda_music", Context.MODE_PRIVATE)
        val raw = prefs.getString("user_tracks", null)
        val userTracks = if (raw != null) {
            runCatching { json.decodeFromString<List<WorkoutTrack>>(raw) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }
        _tracks.value = builtInTracks(context) + userTracks
    }

    private fun saveUserTracks(context: Context) {
        val userOnly = _tracks.value.filterNot { it.isBuiltIn }
        context.getSharedPreferences("fitpanda_music", Context.MODE_PRIVATE)
            .edit()
            .putString("user_tracks", json.encodeToString(userOnly))
            .apply()
    }

    fun addUserTracks(context: Context, uris: List<Uri>) {
        if (uris.isEmpty()) return
        init(context)
        val newTracks = uris.mapNotNull { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val displayName = resolveFileName(context, uri)
            WorkoutTrack(
                id = "user_${uri.toString().hashCode()}_${System.currentTimeMillis()}",
                title = displayName,
                subtitle = "My Phone Audio",
                uriString = uri.toString(),
                isBuiltIn = false
            )
        }
        if (newTracks.isNotEmpty()) {
            val firstNewIdx = _tracks.value.size
            _tracks.value = _tracks.value + newTracks
            saveUserTracks(context)
            playTrack(firstNewIdx)
        }
    }

    fun removeUserTrack(context: Context, trackId: String) {
        val idx = _tracks.value.indexOfFirst { it.id == trackId && !it.isBuiltIn }
        if (idx < 0) return
        val wasCurrent = idx == _currentIndex.value
        _tracks.value = _tracks.value.filterNot { it.id == trackId }
        saveUserTracks(context)
        if (_currentIndex.value >= _tracks.value.size) {
            _currentIndex.value = (_tracks.value.size - 1).coerceAtLeast(0)
        }
        if (wasCurrent) {
            player?.stop()
            _isPlaying.value = false
        }
    }

    private fun resolveFileName(context: Context, uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val name = c.getString(0)
                if (!name.isNullOrBlank()) {
                    return name.substringBeforeLast(".")
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast("/")?.substringBeforeLast(".") ?: "Workout Track"
    }

    fun playTrack(index: Int) {
        val list = _tracks.value
        if (list.isEmpty() || index !in list.indices) return
        _currentIndex.value = index
        val track = list[index]
        val exo = player ?: return
        exo.setMediaItem(MediaItem.fromUri(Uri.parse(track.uriString)))
        exo.prepare()
        exo.play()
    }

    fun togglePlayPause() {
        val exo = player ?: return
        if (_isPlaying.value) {
            exo.pause()
        } else {
            if (exo.currentMediaItem == null) {
                playTrack(_currentIndex.value)
            } else {
                exo.play()
            }
        }
    }

    fun nextTrack() {
        val list = _tracks.value
        if (list.isEmpty()) return
        val next = (_currentIndex.value + 1) % list.size
        playTrack(next)
    }

    fun previousTrack() {
        val list = _tracks.value
        if (list.isEmpty()) return
        val prev = if (_currentIndex.value - 1 < 0) list.size - 1 else _currentIndex.value - 1
        playTrack(prev)
    }

    fun toggleRepeatOne() {
        val nextMode = !_repeatOne.value
        _repeatOne.value = nextMode
        player?.repeatMode = if (nextMode) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_ALL
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        player?.volume = clamped
    }

    fun release() {
        player?.release()
        player = null
        _isPlaying.value = false
    }
}
