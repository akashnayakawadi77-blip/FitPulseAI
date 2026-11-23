package com.fitpulse.ai.data

import android.content.Context
import com.fitpulse.ai.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FitnessRepository(private val context: Context? = null) {

    private val coachService = AiCoachService()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _workoutDays = MutableStateFlow<List<WorkoutDay>>(emptyList())
    val workoutDays: StateFlow<List<WorkoutDay>> = _workoutDays.asStateFlow()

    private val _selectedDayIndex = MutableStateFlow(0)
    val selectedDayIndex: StateFlow<Int> = _selectedDayIndex.asStateFlow()

    private val _nutritionPlan = MutableStateFlow<NutritionPlan?>(null)
    val nutritionPlan: StateFlow<NutritionPlan?> = _nutritionPlan.asStateFlow()

    private val _weightLogs = MutableStateFlow<List<WeightLogEntry>>(emptyList())
    val weightLogs: StateFlow<List<WeightLogEntry>> = _weightLogs.asStateFlow()

    private val _muscleRecoveries = MutableStateFlow<Map<MuscleGroup, MuscleRecoveryState>>(emptyMap())
    val muscleRecoveries: StateFlow<Map<MuscleGroup, MuscleRecoveryState>> = _muscleRecoveries.asStateFlow()

    private val _dailyScore = MutableStateFlow(DailyFitnessScore())
    val dailyScore: StateFlow<DailyFitnessScore> = _dailyScore.asStateFlow()

    private val _dailyTargets = MutableStateFlow(DailyTargets())
    val dailyTargets: StateFlow<DailyTargets> = _dailyTargets.asStateFlow()

    private val _workoutSessions = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val workoutSessions: StateFlow<List<WorkoutSession>> = _workoutSessions.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<AiChatMessage>> = _chatMessages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    init {
        // Initialize with default smart routine & initial weight log
        val initialPlan = coachService.generateSmartFallbackPlan(_userProfile.value)
        _workoutDays.value = initialPlan.first
        _nutritionPlan.value = initialPlan.second

        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val now = System.currentTimeMillis()
        val oneDay = 24L * 60 * 60 * 1000
        _weightLogs.value = listOf(
            WeightLogEntry(now - (3 * oneDay), dateFormat.format(Date(now - (3 * oneDay))), 76.2f),
            WeightLogEntry(now - (2 * oneDay), dateFormat.format(Date(now - (2 * oneDay))), 75.8f),
            WeightLogEntry(now - oneDay, dateFormat.format(Date(now - oneDay)), 75.4f),
            WeightLogEntry(now, dateFormat.format(Date(now)), 75.0f)
        )

        val defaultRecoveries = mutableMapOf<MuscleGroup, MuscleRecoveryState>()
        defaultRecoveries[MuscleGroup.CHEST] = MuscleRecoveryState(MuscleGroup.CHEST, 95, 48)
        defaultRecoveries[MuscleGroup.BACK] = MuscleRecoveryState(MuscleGroup.BACK, 85, 36)
        defaultRecoveries[MuscleGroup.SHOULDERS] = MuscleRecoveryState(MuscleGroup.SHOULDERS, 90, 48)
        defaultRecoveries[MuscleGroup.BICEPS] = MuscleRecoveryState(MuscleGroup.BICEPS, 100, 72)
        defaultRecoveries[MuscleGroup.TRICEPS] = MuscleRecoveryState(MuscleGroup.TRICEPS, 92, 48)
        defaultRecoveries[MuscleGroup.QUADS] = MuscleRecoveryState(MuscleGroup.QUADS, 70, 24)
        defaultRecoveries[MuscleGroup.HAMSTRINGS] = MuscleRecoveryState(MuscleGroup.HAMSTRINGS, 75, 24)
        defaultRecoveries[MuscleGroup.GLUTES] = MuscleRecoveryState(MuscleGroup.GLUTES, 80, 24)
        defaultRecoveries[MuscleGroup.CALVES] = MuscleRecoveryState(MuscleGroup.CALVES, 100, 72)
        defaultRecoveries[MuscleGroup.CORE] = MuscleRecoveryState(MuscleGroup.CORE, 90, 36)
        _muscleRecoveries.value = defaultRecoveries

        _chatMessages.value = listOf(
            AiChatMessage(
                id = "welcome_msg",
                isUser = false,
                message = "👋 Hello! I am your AI Fitness Coach. I can design personalized workout splits, track muscle recovery, and provide instant form tips. Let's crush your goals!"
            )
        )
    }

    fun selectWorkoutDay(index: Int) {
        if (index in _workoutDays.value.indices) {
            _selectedDayIndex.value = index
        }
    }

    fun toggleExerciseCompleted(dayIndex: Int, exerciseId: String) {
        var changedTargetMuscle: String? = null
        var nowCompleted = false
        _workoutDays.update { days ->
            days.mapIndexed { idx, day ->
                if (idx == dayIndex) {
                    val updatedExercises = day.exercises.map { ex ->
                        if (ex.id == exerciseId) {
                            val newCompleted = !ex.isCompleted
                            changedTargetMuscle = ex.targetMuscle
                            nowCompleted = newCompleted
                            ex.copy(isCompleted = newCompleted)
                        } else ex
                    }
                    day.copy(exercises = updatedExercises)
                } else {
                    day
                }
            }
        }

        changedTargetMuscle?.let { target ->
            val group = MuscleGroup.fromTargetMuscle(target)
            _muscleRecoveries.update { map ->
                val current = map[group] ?: MuscleRecoveryState(group, 100, 72)
                val newFreshness = if (nowCompleted) {
                    (current.freshnessPercent - 35).coerceAtLeast(15)
                } else {
                    (current.freshnessPercent + 35).coerceAtMost(100)
                }
                map + (group to current.copy(freshnessPercent = newFreshness, hoursSinceTrained = if (nowCompleted) 0 else current.hoursSinceTrained))
            }
        }
    }

    fun updateExerciseWeightAndReps(dayIndex: Int, exerciseId: String, newWeight: Float, newReps: String) {
        _workoutDays.update { days ->
            days.mapIndexed { idx, day ->
                if (idx == dayIndex) {
                    val updatedExercises = day.exercises.map { ex ->
                        if (ex.id == exerciseId) ex.copy(weightKg = newWeight, reps = newReps) else ex
                    }
                    day.copy(exercises = updatedExercises)
                } else {
                    day
                }
            }
        }
    }

    fun addWaterIntake(amountMl: Int) {
        _nutritionPlan.update { plan ->
            plan?.copy(
                currentWaterMl = (plan.currentWaterMl + amountMl).coerceAtLeast(0)
            )
        }
    }

    fun resetWaterIntake() {
        _nutritionPlan.update { plan ->
            plan?.copy(currentWaterMl = 0)
        }
    }

    fun logCurrentWeight(weight: Float) {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val entry = WeightLogEntry(
            timestamp = System.currentTimeMillis(),
            dateDisplay = dateFormat.format(Date()),
            weightKg = weight
        )
        _weightLogs.update { logs -> listOf(entry) + logs }
        _userProfile.update { it.copy(weightKg = weight) }
    }

    fun updateProfile(newProfile: UserProfile) {
        _userProfile.value = newProfile
    }

    suspend fun generateNewPlan() {
        _isGenerating.value = true
        try {
            val (days, nutrition) = coachService.generateWorkoutAndNutrition(_userProfile.value)
            _workoutDays.value = days
            _nutritionPlan.value = nutrition
            _selectedDayIndex.value = 0
        } finally {
            _isGenerating.value = false
        }
    }

    suspend fun sendChatMessage(userText: String) {
        val userMsg = AiChatMessage(
            id = "user_${System.currentTimeMillis()}",
            isUser = true,
            message = userText
        )
        _chatMessages.update { it + userMsg }

        val coachReply = coachService.askCoach(userText, _userProfile.value)
        val aiMsg = AiChatMessage(
            id = "ai_${System.currentTimeMillis()}",
            isUser = false,
            message = coachReply
        )
        _chatMessages.update { it + aiMsg }
    }
}
