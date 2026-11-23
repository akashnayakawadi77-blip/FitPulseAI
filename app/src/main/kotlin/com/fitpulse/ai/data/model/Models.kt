package com.fitpulse.ai.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class FitnessGoal(val displayName: String, val description: String) {
    MUSCLE_BUILD("Build Muscle", "Hypertrophy focus, progressive overload, high protein"),
    FAT_LOSS("Fat Loss", "High calorie burn, moderate weights, caloric deficit"),
    STRENGTH("Pure Strength", "Heavy compound lifts (1-5 reps), longer rest periods"),
    ENDURANCE("Endurance & Tone", "Circuit training, higher reps (15-20), short rest")
}

@Serializable
enum class FitnessLevel(val displayName: String) {
    BEGINNER("Beginner (0-1 year)"),
    INTERMEDIATE("Intermediate (1-3 years)"),
    ADVANCED("Advanced (3+ years)")
}

@Serializable
enum class WorkoutEnvironment(val displayName: String) {
    FULL_GYM("Full Commercial Gym"),
    DUMBBELLS_HOME("Home (Dumbbells Only)"),
    BODYWEIGHT("Home (Bodyweight & Calisthenics)")
}

@Serializable
data class UserProfile(
    val name: String = "Fitness Warrior",
    val age: Int = 25,
    val weightKg: Float = 75.0f,
    val heightCm: Float = 178.0f,
    val fitnessGoal: FitnessGoal = FitnessGoal.MUSCLE_BUILD,
    val fitnessLevel: FitnessLevel = FitnessLevel.INTERMEDIATE,
    val workoutEnvironment: WorkoutEnvironment = WorkoutEnvironment.FULL_GYM,
    val daysPerWeek: Int = 4,
    val geminiApiKey: String = ""
)

@Serializable
data class Exercise(
    val id: String,
    val name: String,
    val targetMuscle: String,
    val sets: Int,
    val reps: String,
    val restSeconds: Int = 60,
    val weightKg: Float = 20.0f,
    val coachingTip: String,
    val isCompleted: Boolean = false
)

@Serializable
data class WorkoutDay(
    val id: String,
    val dayTitle: String,
    val focusArea: String,
    val exercises: List<Exercise>
)

@Serializable
data class MealPlanItem(
    val title: String,
    val mealType: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val suggestion: String
)

@Serializable
data class NutritionPlan(
    val dailyCalories: Int,
    val proteinTargetGrams: Int,
    val carbsTargetGrams: Int,
    val fatTargetGrams: Int,
    val waterTargetMl: Int = 3000,
    val currentWaterMl: Int = 1250,
    val meals: List<MealPlanItem>
)

@Serializable
data class WeightLogEntry(
    val timestamp: Long,
    val dateDisplay: String,
    val weightKg: Float
)

@Serializable
data class DailyFitnessScore(
    val score: Int = 78,
    val maxScore: Int = 100,
    val status: String = "Good Progress",
    val readiness: String = "Optimal Readiness",
    val recoveryContribution: Int = 84,
    val consistencyContribution: Int = 75
)

@Serializable
data class DailyTargets(
    val targetCalories: Int = 2200,
    val loggedCalories: Int = 1850,
    val targetProteinGrams: Int = 140,
    val loggedProteinGrams: Int = 95,
    val targetWaterLiters: Float = 2.5f,
    val loggedWaterLiters: Float = 1.8f,
    val targetSteps: Int = 10000,
    val loggedSteps: Int = 6200
)

@Serializable
data class WorkoutSession(
    val id: String,
    val planId: String,
    val title: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long = 0,
    val totalVolumeKg: Float = 0f,
    val completedExercisesCount: Int = 0,
    val totalExercisesCount: Int = 0,
    val perceivedExertionRpe: Int = 7, // 1-10
    val userNotes: String = ""
)

@Serializable
data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class MuscleGroup(val displayName: String, val category: String) {
    CHEST("Chest", "Upper"),
    BACK("Back & Lats", "Upper"),
    SHOULDERS("Shoulders", "Upper"),
    BICEPS("Biceps", "Arms"),
    TRICEPS("Triceps", "Arms"),
    QUADS("Quads", "Legs"),
    HAMSTRINGS("Hamstrings", "Legs"),
    GLUTES("Glutes", "Legs"),
    CALVES("Calves", "Legs"),
    CORE("Abs & Core", "Core");

    companion object {
        fun fromTargetMuscle(target: String): MuscleGroup {
            val t = target.lowercase()
            return when {
                t.contains("chest") -> CHEST
                t.contains("back") || t.contains("lat") -> BACK
                t.contains("shoulder") || t.contains("delt") -> SHOULDERS
                t.contains("bicep") -> BICEPS
                t.contains("tricep") -> TRICEPS
                t.contains("quad") || t.contains("squat") -> QUADS
                t.contains("hamstring") || t.contains("rdl") -> HAMSTRINGS
                t.contains("glute") -> GLUTES
                t.contains("calf") || t.contains("calves") -> CALVES
                t.contains("core") || t.contains("ab") || t.contains("oblique") -> CORE
                else -> CHEST
            }
        }
    }
}

@Serializable
data class MuscleRecoveryState(
    val muscle: MuscleGroup,
    val freshnessPercent: Int, // 0 to 100
    val hoursSinceTrained: Int,
    val statusLabel: String = when {
        freshnessPercent >= 80 -> "Fresh (Peak)"
        freshnessPercent >= 50 -> "Recovering"
        else -> "Fatigued"
    }
)
