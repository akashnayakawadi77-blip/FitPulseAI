package com.fitpulse.ai.data

import android.content.Context
import com.fitpulse.ai.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class FitnessRepository(private val context: Context? = null) {

    private val db: LocalDatabase? = context?.let { LocalDatabase(it.applicationContext) }
    private val coachService = AiCoachService()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _workoutDays = MutableStateFlow<List<WorkoutDay>>(emptyList())
    val workoutDays: StateFlow<List<WorkoutDay>> = _workoutDays.asStateFlow()

    private val _selectedDayIndex = MutableStateFlow(0)
    val selectedDayIndex: StateFlow<Int> = _selectedDayIndex.asStateFlow()

    private val _nutritionPlan = MutableStateFlow<NutritionPlan?>(null)
    val nutritionPlan: StateFlow<NutritionPlan?> = _nutritionPlan.asStateFlow()

    private val _loggedFoods = MutableStateFlow<List<LoggedFoodItem>>(emptyList())
    val loggedFoods: StateFlow<List<LoggedFoodItem>> = _loggedFoods.asStateFlow()

    private val _weightLogs = MutableStateFlow<List<WeightLogEntry>>(emptyList())
    val weightLogs: StateFlow<List<WeightLogEntry>> = _weightLogs.asStateFlow()

    private val _muscleRecoveries = MutableStateFlow<Map<MuscleGroup, MuscleRecoveryState>>(emptyMap())
    val muscleRecoveries: StateFlow<Map<MuscleGroup, MuscleRecoveryState>> = _muscleRecoveries.asStateFlow()

    private val _dailyScore = MutableStateFlow(DailyFitnessScore())
    val dailyScore: StateFlow<DailyFitnessScore> = _dailyScore.asStateFlow()

    private val _dailyTargets = MutableStateFlow(DailyTargets())
    val dailyTargets: StateFlow<DailyTargets> = _dailyTargets.asStateFlow()

    private val _dailySteps = MutableStateFlow(0)
    val dailySteps: StateFlow<Int> = _dailySteps.asStateFlow()

    private val _workoutSessions = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val workoutSessions: StateFlow<List<WorkoutSession>> = _workoutSessions.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<AiChatMessage>> = _chatMessages.asStateFlow()

    private val _reminderSettings = MutableStateFlow(ReminderSettings())
    val reminderSettings: StateFlow<ReminderSettings> = _reminderSettings.asStateFlow()

    private val _transformationPhotos = MutableStateFlow(TransformationPhotos())
    val transformationPhotos: StateFlow<TransformationPhotos> = _transformationPhotos.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    init {
        loadOrInitialize()
    }

    private fun loadOrInitialize() {
        val today = LocalDatabase.todayDateKey()
        val savedProfile = db?.loadUserProfile()
        val profile = savedProfile ?: UserProfile()
        _userProfile.value = profile

        val savedDays = db?.loadWorkoutDays()
        val savedNutrition = db?.loadNutritionPlan()
        if (savedDays != null && savedNutrition != null) {
            _workoutDays.value = savedDays
            _nutritionPlan.value = savedNutrition
        } else {
            val initialPlan = coachService.generateSmartFallbackPlan(profile)
            _workoutDays.value = initialPlan.first
            _nutritionPlan.value = initialPlan.second.copy(currentWaterMl = 0)
            db?.saveWorkoutDays(_workoutDays.value)
            _nutritionPlan.value?.let { db?.saveNutritionPlan(it) }
        }

        _selectedDayIndex.value = (db?.loadSelectedDayIndex() ?: 0)
            .coerceIn(0, (_workoutDays.value.size - 1).coerceAtLeast(0))

        val savedWeights = db?.loadWeightLogs().orEmpty()
        if (savedWeights.isNotEmpty()) {
            _weightLogs.value = savedWeights
        } else {
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val now = System.currentTimeMillis()
            val entry = WeightLogEntry(now, dateFormat.format(Date(now)), profile.weightKg)
            _weightLogs.value = listOf(entry)
            db?.insertWeightLog(entry)
        }

        val savedRecoveries = db?.loadMuscleRecoveries()
        if (savedRecoveries != null && savedRecoveries.isNotEmpty()) {
            _muscleRecoveries.value = savedRecoveries
        } else {
            val defaults = defaultRecoveries()
            _muscleRecoveries.value = defaults
            db?.saveMuscleRecoveries(defaults)
        }

        val savedMessages = db?.loadChatMessages()
        if (savedMessages != null && savedMessages.isNotEmpty()) {
            _chatMessages.value = savedMessages
        } else {
            val welcome = listOf(
                AiChatMessage(
                    id = "welcome_msg",
                    isUser = false,
                    message = "👋 Hello ${profile.name}! I am your AI Fitness Coach. I can design personalized workout splits, track muscle recovery, and provide instant form tips. Let's crush your goals!"
                )
            )
            _chatMessages.value = welcome
            db?.saveChatMessages(welcome)
        }

        _workoutSessions.value = db?.loadWorkoutSessions().orEmpty()
        _loggedFoods.value = db?.loadLoggedFoodsForDate(today).orEmpty()
        _reminderSettings.value = db?.loadReminderSettings() ?: ReminderSettings()
        _transformationPhotos.value = db?.loadTransformationPhotos() ?: TransformationPhotos()

        // Handle daily rollover for water & steps
        val lastDate = db?.loadLastActiveDate()
        if (lastDate != null && lastDate != today) {
            _nutritionPlan.update { it?.copy(currentWaterMl = 0) }
            _nutritionPlan.value?.let { db?.saveNutritionPlan(it) }
            _dailySteps.value = 0
            db?.saveDailySteps(today, 0)
        } else {
            _dailySteps.value = db?.loadDailySteps(today) ?: 0
        }
        db?.saveLastActiveDate(today)

        recalculateMetrics()
    }

    private fun defaultRecoveries(): Map<MuscleGroup, MuscleRecoveryState> = mapOf(
        MuscleGroup.CHEST to MuscleRecoveryState(MuscleGroup.CHEST, 95, 48),
        MuscleGroup.BACK to MuscleRecoveryState(MuscleGroup.BACK, 90, 48),
        MuscleGroup.SHOULDERS to MuscleRecoveryState(MuscleGroup.SHOULDERS, 92, 48),
        MuscleGroup.BICEPS to MuscleRecoveryState(MuscleGroup.BICEPS, 100, 72),
        MuscleGroup.TRICEPS to MuscleRecoveryState(MuscleGroup.TRICEPS, 95, 48),
        MuscleGroup.QUADS to MuscleRecoveryState(MuscleGroup.QUADS, 85, 36),
        MuscleGroup.HAMSTRINGS to MuscleRecoveryState(MuscleGroup.HAMSTRINGS, 88, 36),
        MuscleGroup.GLUTES to MuscleRecoveryState(MuscleGroup.GLUTES, 90, 36),
        MuscleGroup.CALVES to MuscleRecoveryState(MuscleGroup.CALVES, 100, 72),
        MuscleGroup.CORE to MuscleRecoveryState(MuscleGroup.CORE, 95, 36)
    )

    private fun recalculateMetrics() {
        val plan = _nutritionPlan.value
        val foods = _loggedFoods.value
        val totalCals = foods.sumOf { it.calories }
        val totalProtein = foods.sumOf { it.proteinGrams }
        val waterLiters = (plan?.currentWaterMl ?: 0) / 1000f
        val targetWaterLiters = (plan?.waterTargetMl ?: 3000) / 1000f
        val steps = _dailySteps.value
        val stepTarget = 10000

        _dailyTargets.value = DailyTargets(
            targetCalories = plan?.dailyCalories ?: 2200,
            loggedCalories = totalCals,
            targetProteinGrams = plan?.proteinTargetGrams ?: 140,
            loggedProteinGrams = totalProtein,
            targetWaterLiters = targetWaterLiters,
            loggedWaterLiters = waterLiters,
            targetSteps = stepTarget,
            loggedSteps = steps
        )

        val recoveries = _muscleRecoveries.value.values
        val avgRecovery = if (recoveries.isNotEmpty()) {
            recoveries.map { it.freshnessPercent }.average().roundToInt()
        } else 90

        val sessionsThisWeek = countSessionsThisWeek()
        val targetDays = _userProfile.value.daysPerWeek.coerceAtLeast(1)
        val currentDay = _workoutDays.value.getOrNull(_selectedDayIndex.value)
        val todayCompletionRatio = if (currentDay != null && currentDay.exercises.isNotEmpty()) {
            currentDay.exercises.count { it.isCompleted }.toFloat() / currentDay.exercises.size
        } else 0f

        val consistencyPct = (((sessionsThisWeek.toFloat() / targetDays) * 80f) + (todayCompletionRatio * 20f))
            .roundToInt()
            .coerceIn(20, 100)

        val waterScore = if (targetWaterLiters > 0f) (waterLiters / targetWaterLiters).coerceIn(0f, 1f) * 100f else 0f
        val calTarget = (plan?.dailyCalories ?: 2200).toFloat()
        val nutritionScore = if (totalCals > 0) (totalCals / calTarget).coerceIn(0f, 1f) * 100f else 40f

        val overall = ((avgRecovery * 0.35f) + (consistencyPct * 0.35f) + (waterScore * 0.15f) + (nutritionScore * 0.15f))
            .roundToInt()
            .coerceIn(10, 100)

        val status = when {
            overall >= 85 -> "Peak Condition"
            overall >= 70 -> "Good Progress"
            overall >= 50 -> "Building Momentum"
            else -> "Time to Recharge"
        }
        val readiness = when {
            avgRecovery >= 80 -> "Optimal Readiness"
            avgRecovery >= 55 -> "Moderate Readiness"
            else -> "Recovery Focus"
        }

        _dailyScore.value = DailyFitnessScore(
            score = overall,
            maxScore = 100,
            status = status,
            readiness = readiness,
            recoveryContribution = avgRecovery,
            consistencyContribution = consistencyPct
        )
    }

    fun countSessionsThisWeek(): Int {
        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val weekStart = cal.timeInMillis
        return _workoutSessions.value.count { it.startTimeMillis >= weekStart }
    }

    fun getWeeklyActiveDays(): List<Boolean> {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val sessions = _workoutSessions.value
        val result = MutableList(7) { false }
        for (i in 0 until 7) {
            val dayStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = cal.timeInMillis
            result[i] = sessions.any { it.startTimeMillis in dayStart until dayEnd }
        }
        return result
    }

    suspend fun completeOnboarding(profile: UserProfile) {
        val completed = profile.copy(onboardingCompleted = true)
        _userProfile.value = completed
        db?.saveUserProfile(completed)

        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val entry = WeightLogEntry(System.currentTimeMillis(), dateFormat.format(Date()), completed.weightKg)
        _weightLogs.value = listOf(entry)
        db?.insertWeightLog(entry)

        generateNewPlan()
        recalculateMetrics()
    }

    fun selectWorkoutDay(index: Int) {
        if (index in _workoutDays.value.indices) {
            _selectedDayIndex.value = index
            db?.saveSelectedDayIndex(index)
            recalculateMetrics()
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
        db?.saveWorkoutDays(_workoutDays.value)

        changedTargetMuscle?.let { target ->
            val group = MuscleGroup.fromTargetMuscle(target)
            _muscleRecoveries.update { map ->
                val current = map[group] ?: MuscleRecoveryState(group, 100, 72)
                val newFreshness = if (nowCompleted) {
                    (current.freshnessPercent - 25).coerceAtLeast(15)
                } else {
                    (current.freshnessPercent + 25).coerceAtMost(100)
                }
                map + (group to current.copy(
                    freshnessPercent = newFreshness,
                    hoursSinceTrained = if (nowCompleted) 0 else current.hoursSinceTrained
                ))
            }
            db?.saveMuscleRecoveries(_muscleRecoveries.value)
        }
        recalculateMetrics()
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
        db?.saveWorkoutDays(_workoutDays.value)
    }

    fun completeWorkoutSession(dayIndex: Int, rpe: Int = 7, notes: String = "") {
        val day = _workoutDays.value.getOrNull(dayIndex) ?: return
        val completedExercises = day.exercises.filter { it.isCompleted }.ifEmpty { day.exercises }
        val totalVolume = completedExercises.sumOf { ex ->
            val avgReps = ex.reps.filter { it.isDigit() || it == '-' }
                .split("-")
                .mapNotNull { it.toIntOrNull() }
                .let { if (it.isEmpty()) 10.0 else it.average() }
            (ex.sets * avgReps * ex.weightKg.coerceAtLeast(1f)).toDouble()
        }.toFloat()

        val now = System.currentTimeMillis()
        val session = WorkoutSession(
            id = "session_$now",
            planId = day.id,
            title = day.dayTitle,
            startTimeMillis = now,
            endTimeMillis = now,
            totalVolumeKg = totalVolume,
            completedExercisesCount = completedExercises.size,
            totalExercisesCount = day.exercises.size,
            perceivedExertionRpe = rpe,
            userNotes = notes
        )
        _workoutSessions.update { listOf(session) + it }
        db?.insertWorkoutSession(session)

        // Reset completed checkboxes for the next time this day is trained
        _workoutDays.update { days ->
            days.mapIndexed { idx, d ->
                if (idx == dayIndex) {
                    d.copy(exercises = d.exercises.map { it.copy(isCompleted = false) })
                } else d
            }
        }
        db?.saveWorkoutDays(_workoutDays.value)
        recalculateMetrics()
    }

    fun addWaterIntake(amountMl: Int) {
        _nutritionPlan.update { plan ->
            plan?.copy(currentWaterMl = (plan.currentWaterMl + amountMl).coerceAtLeast(0))
        }
        _nutritionPlan.value?.let { db?.saveNutritionPlan(it) }
        recalculateMetrics()
    }

    fun resetWaterIntake() {
        _nutritionPlan.update { plan -> plan?.copy(currentWaterMl = 0) }
        _nutritionPlan.value?.let { db?.saveNutritionPlan(it) }
        recalculateMetrics()
    }

    fun logFood(item: LoggedFoodItem) {
        val today = LocalDatabase.todayDateKey()
        val normalized = item.copy(dateKey = today)
        _loggedFoods.update { listOf(normalized) + it }
        db?.insertLoggedFood(normalized)
        recalculateMetrics()
    }

    fun deleteLoggedFood(id: String) {
        _loggedFoods.update { list -> list.filterNot { it.id == id } }
        db?.deleteLoggedFood(id)
        recalculateMetrics()
    }

    fun addSteps(delta: Int) {
        val today = LocalDatabase.todayDateKey()
        val updated = (_dailySteps.value + delta).coerceAtLeast(0)
        _dailySteps.value = updated
        db?.saveDailySteps(today, updated)
        recalculateMetrics()
    }

    fun updateDeviceSteps(steps: Int) {
        val today = LocalDatabase.todayDateKey()
        if (steps >= _dailySteps.value) {
            _dailySteps.value = steps
            db?.saveDailySteps(today, steps)
            recalculateMetrics()
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
        db?.insertWeightLog(entry)
        _userProfile.update { it.copy(weightKg = weight) }
        db?.saveUserProfile(_userProfile.value)
    }

    fun updateProfile(newProfile: UserProfile) {
        _userProfile.value = newProfile
        db?.saveUserProfile(newProfile)
        recalculateMetrics()
    }

    fun updateReminderSettings(settings: ReminderSettings) {
        _reminderSettings.value = settings
        db?.saveReminderSettings(settings)
    }

    fun updateProfilePhoto(path: String) {
        _userProfile.update { it.copy(profilePhotoPath = path) }
        db?.saveUserProfile(_userProfile.value)
    }

    fun updateBeforePhoto(path: String) {
        val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        val updated = _transformationPhotos.value.copy(
            beforePhotoPath = path,
            beforeDate = dateStr,
            beforeWeightKg = _userProfile.value.weightKg
        )
        _transformationPhotos.value = updated
        db?.saveTransformationPhotos(updated)
    }

    fun updateAfterPhoto(path: String) {
        val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        val updated = _transformationPhotos.value.copy(
            afterPhotoPath = path,
            afterDate = dateStr,
            afterWeightKg = _userProfile.value.weightKg
        )
        _transformationPhotos.value = updated
        db?.saveTransformationPhotos(updated)
    }

    suspend fun generateNewPlan() {
        _isGenerating.value = true
        try {
            val (days, nutrition) = coachService.generateWorkoutAndNutrition(_userProfile.value)
            val existingWater = _nutritionPlan.value?.currentWaterMl ?: 0
            val updatedNutrition = nutrition.copy(currentWaterMl = existingWater)
            _workoutDays.value = days
            _nutritionPlan.value = updatedNutrition
            _selectedDayIndex.value = 0
            db?.saveWorkoutDays(days)
            db?.saveNutritionPlan(updatedNutrition)
            db?.saveSelectedDayIndex(0)
            recalculateMetrics()
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
        db?.saveChatMessages(_chatMessages.value)

        val coachReply = coachService.askCoach(userText, _userProfile.value)
        val aiMsg = AiChatMessage(
            id = "ai_${System.currentTimeMillis()}",
            isUser = false,
            message = coachReply
        )
        _chatMessages.update { it + aiMsg }
        db?.saveChatMessages(_chatMessages.value)
    }

    fun resetAllData() {
        db?.clearAllData()
        _workoutSessions.value = emptyList()
        _loggedFoods.value = emptyList()
        _dailySteps.value = 0
        loadOrInitialize()
    }

    fun exportProgressCsv(): String {
        val sb = StringBuilder()
        sb.appendLine("Type,Date,Title/Metric,Value")
        _weightLogs.value.forEach { w ->
            sb.appendLine("Weight,${w.dateDisplay},Body Weight,${w.weightKg} kg")
        }
        val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        _workoutSessions.value.forEach { s ->
            sb.appendLine("Workout,${dateFmt.format(Date(s.startTimeMillis))},${s.title},${s.totalVolumeKg.toInt()} kg volume (${s.completedExercisesCount}/${s.totalExercisesCount} exercises)")
        }
        _loggedFoods.value.forEach { f ->
            sb.appendLine("Food,${f.dateKey},${f.name},${f.calories} kcal / ${f.proteinGrams}g P")
        }
        return sb.toString()
    }
}
