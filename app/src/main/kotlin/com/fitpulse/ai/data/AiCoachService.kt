package com.fitpulse.ai.data

import com.fitpulse.ai.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class AiCoachService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun generateWorkoutAndNutrition(profile: UserProfile): Pair<List<WorkoutDay>, NutritionPlan> =
        withContext(Dispatchers.IO) {
            if (profile.geminiApiKey.isNotBlank()) {
                try {
                    val prompt = buildPlanPrompt(profile)
                    val responseJson = callGeminiApi(profile.geminiApiKey, prompt)
                    val parsed = parseGeminiResponse(responseJson)
                    if (parsed != null) return@withContext parsed
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Fall back to built-in smart AI generator if network or API key fails
                }
            }
            // Intelligent scientific fallback tailored to user profile
            generateSmartFallbackPlan(profile)
        }

    suspend fun askCoach(question: String, profile: UserProfile): String = withContext(Dispatchers.IO) {
        if (profile.geminiApiKey.isNotBlank()) {
            try {
                val prompt = """
                    You are an elite, encouraging certified Personal Fitness Coach and Sports Nutritionist named FitPulse AI.
                    The user's profile:
                    - Age: ${profile.age}, Weight: ${profile.weightKg}kg, Height: ${profile.heightCm}cm
                    - Primary Goal: ${profile.fitnessGoal.displayName}
                    - Fitness Level: ${profile.fitnessLevel.displayName}
                    - Equipment: ${profile.workoutEnvironment.displayName}
                    
                    User question: "$question"
                    
                    Provide a concise, practical, motivating answer in 2-4 punchy paragraphs with bullet points for key tips.
                """.trimIndent()

                val responseJson = callGeminiApi(profile.geminiApiKey, prompt)
                val text = extractTextFromGeminiResponse(responseJson)
                if (!text.isNullOrBlank()) return@withContext text
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Offline smart response
        generateSmartAdvice(question, profile)
    }

    private fun callGeminiApi(apiKey: String, promptText: String): String {
        // Modern supported endpoint: Gemini 2.0 Flash with automatic fallback
        val modelName = "gemini-2.0-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
        val requestBodyJson = buildJsonObject {
            putJsonArray("contents") {
                addJsonObject {
                    putJsonArray("parts") {
                        addJsonObject {
                            put("text", promptText)
                        }
                    }
                }
            }
        }.toString()

        val request = Request.Builder()
            .url(url)
            .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                // Fallback attempt to gemini-1.5-flash-latest
                val fallbackUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=$apiKey"
                val fbRequest = Request.Builder()
                    .url(fallbackUrl)
                    .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
                    .build()
                httpClient.newCall(fbRequest).execute().use { fbResponse ->
                    if (!fbResponse.isSuccessful) {
                        throw IllegalStateException("Gemini API call failed with code: ${response.code}")
                    }
                    return fbResponse.body?.string() ?: ""
                }
            }
            return response.body?.string() ?: ""
        }
    }

    private fun extractTextFromGeminiResponse(jsonString: String): String? {
        return try {
            val element = json.parseToJsonElement(jsonString)
            element.jsonObject["candidates"]?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("content")?.jsonObject
                ?.get("parts")?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("text")?.jsonPrimitive?.content
        } catch (e: Exception) {
            null
        }
    }

    private fun parseGeminiResponse(jsonString: String): Pair<List<WorkoutDay>, NutritionPlan>? {
        val text = extractTextFromGeminiResponse(jsonString) ?: return null
        return try {
            // Find JSON substring
            val cleanJson = text.substringAfter("```json")
                .substringBeforeLast("```")
                .trim()
                .ifEmpty { text.trim() }

            val root = json.parseToJsonElement(cleanJson).jsonObject
            val daysJson = root["workoutDays"]?.jsonArray ?: return null
            val nutritionJson = root["nutritionPlan"]?.jsonObject ?: return null

            val workoutDays = daysJson.mapIndexed { index, dayEl ->
                val dayObj = dayEl.jsonObject
                val exercises = (dayObj["exercises"]?.jsonArray ?: JsonArray(emptyList())).mapIndexed { exIdx, exEl ->
                    val exObj = exEl.jsonObject
                    Exercise(
                        id = "gemini_ex_${index}_$exIdx",
                        name = exObj["name"]?.jsonPrimitive?.content ?: "Exercise",
                        targetMuscle = exObj["targetMuscle"]?.jsonPrimitive?.content ?: "Full Body",
                        sets = exObj["sets"]?.jsonPrimitive?.intOrNull ?: 3,
                        reps = exObj["reps"]?.jsonPrimitive?.content ?: "10",
                        restSeconds = exObj["restSeconds"]?.jsonPrimitive?.intOrNull ?: 60,
                        weightKg = exObj["weightKg"]?.jsonPrimitive?.floatOrNull ?: 20f,
                        coachingTip = exObj["coachingTip"]?.jsonPrimitive?.content ?: "Maintain strict form."
                    )
                }
                WorkoutDay(
                    id = "gemini_day_$index",
                    dayTitle = dayObj["dayTitle"]?.jsonPrimitive?.content ?: "Day ${index + 1}",
                    focusArea = dayObj["focusArea"]?.jsonPrimitive?.content ?: "Strength",
                    exercises = exercises
                )
            }

            val meals = (nutritionJson["meals"]?.jsonArray ?: JsonArray(emptyList())).map { mealEl ->
                val mObj = mealEl.jsonObject
                MealPlanItem(
                    title = mObj["title"]?.jsonPrimitive?.content ?: "Balanced Meal",
                    mealType = mObj["mealType"]?.jsonPrimitive?.content ?: "General",
                    calories = mObj["calories"]?.jsonPrimitive?.intOrNull ?: 500,
                    proteinGrams = mObj["proteinGrams"]?.jsonPrimitive?.intOrNull ?: 30,
                    carbsGrams = mObj["carbsGrams"]?.jsonPrimitive?.intOrNull ?: 50,
                    fatGrams = mObj["fatGrams"]?.jsonPrimitive?.intOrNull ?: 15,
                    suggestion = mObj["suggestion"]?.jsonPrimitive?.content ?: "Whole foods, lean protein."
                )
            }

            val plan = NutritionPlan(
                dailyCalories = nutritionJson["dailyCalories"]?.jsonPrimitive?.intOrNull ?: 2200,
                proteinTargetGrams = nutritionJson["proteinTargetGrams"]?.jsonPrimitive?.intOrNull ?: 150,
                carbsTargetGrams = nutritionJson["carbsTargetGrams"]?.jsonPrimitive?.intOrNull ?: 220,
                fatTargetGrams = nutritionJson["fatTargetGrams"]?.jsonPrimitive?.intOrNull ?: 65,
                waterTargetMl = nutritionJson["waterTargetMl"]?.jsonPrimitive?.intOrNull ?: 3000,
                currentWaterMl = 0,
                meals = meals
            )

            Pair(workoutDays, plan)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun buildPlanPrompt(profile: UserProfile): String {
        return """
            You are a World-Class AI Fitness Trainer and Sports Nutritionist. Generate a structured workout routine and daily nutrition plan in pure JSON format (no markdown explanations).
            User Stats:
            - Age: ${profile.age}
            - Weight: ${profile.weightKg} kg, Height: ${profile.heightCm} cm
            - Goal: ${profile.fitnessGoal.displayName} (${profile.fitnessGoal.description})
            - Fitness Level: ${profile.fitnessLevel.displayName}
            - Equipment: ${profile.workoutEnvironment.displayName}
            - Days/Week: ${profile.daysPerWeek}

            Return JSON matching this schema:
            {
              "workoutDays": [
                {
                  "dayTitle": "Day 1: Chest & Triceps (Push)",
                  "focusArea": "Chest, Front Delts, Triceps",
                  "exercises": [
                    {
                      "name": "Barbell Bench Press",
                      "targetMuscle": "Chest",
                      "sets": 4,
                      "reps": "8-10",
                      "restSeconds": 90,
                      "weightKg": 40.0,
                      "coachingTip": "Retract scapula, lower bar to mid-chest, drive up explosively."
                    }
                  ]
                }
              ],
              "nutritionPlan": {
                "dailyCalories": 2400,
                "proteinTargetGrams": 160,
                "carbsTargetGrams": 260,
                "fatTargetGrams": 65,
                "waterTargetMl": 3200,
                "meals": [
                  {
                    "title": "Power Oatmeal & Eggs",
                    "mealType": "Breakfast",
                    "calories": 600,
                    "proteinGrams": 40,
                    "carbsGrams": 70,
                    "fatGrams": 18,
                    "suggestion": "3 whole eggs, 80g oats with berries and whey."
                  }
                ]
              }
            }
        """.trimIndent()
    }

    fun generateSmartFallbackPlan(profile: UserProfile): Pair<List<WorkoutDay>, NutritionPlan> {
        // Calculate BMR & TDEE
        // Mifflin-St Jeor: 10 * weight(kg) + 6.25 * height(cm) - 5 * age + 5 (male baseline)
        val bmr = (10 * profile.weightKg + 6.25 * profile.heightCm - 5 * profile.age + 5).toInt()
        val activityMultiplier = when (profile.daysPerWeek) {
            3 -> 1.375f
            4 -> 1.45f
            5 -> 1.55f
            else -> 1.65f
        }
        val tdee = (bmr * activityMultiplier).toInt()

        val dailyCalories = when (profile.fitnessGoal) {
            FitnessGoal.FAT_LOSS -> (tdee * 0.82f).toInt() // 18% deficit
            FitnessGoal.MUSCLE_BUILD -> (tdee * 1.12f).toInt() // 12% surplus
            FitnessGoal.STRENGTH -> (tdee * 1.05f).toInt()
            FitnessGoal.ENDURANCE -> tdee
        }

        // Protein: 2.0g per kg for muscle/cut, 1.6g for endurance
        val proteinTarget = (profile.weightKg * if (profile.fitnessGoal == FitnessGoal.MUSCLE_BUILD || profile.fitnessGoal == FitnessGoal.FAT_LOSS) 2.2f else 1.8f).toInt()
        val fatTarget = ((dailyCalories * 0.25f) / 9).toInt()
        val carbsTarget = ((dailyCalories - (proteinTarget * 4) - (fatTarget * 9)) / 4).coerceAtLeast(100)

        val days = when (profile.workoutEnvironment) {
            WorkoutEnvironment.FULL_GYM -> getGymSplit(profile)
            WorkoutEnvironment.DUMBBELLS_HOME -> getDumbbellSplit(profile)
            WorkoutEnvironment.BODYWEIGHT -> getBodyweightSplit(profile)
        }

        val meals = listOf(
            MealPlanItem(
                title = "High-Protein Power Breakfast",
                mealType = "Breakfast",
                calories = (dailyCalories * 0.28).toInt(),
                proteinGrams = (proteinTarget * 0.28).toInt(),
                carbsGrams = (carbsTarget * 0.30).toInt(),
                fatGrams = (fatTarget * 0.25).toInt(),
                suggestion = "Paneer Bhurji / 3 Eggs with 2 Multigrain Rotis, warm oats with chia seeds and almonds."
            ),
            MealPlanItem(
                title = "Balanced Muscle Fuel Lunch",
                mealType = "Lunch",
                calories = (dailyCalories * 0.35).toInt(),
                proteinGrams = (proteinTarget * 0.35).toInt(),
                carbsGrams = (carbsTarget * 0.35).toInt(),
                fatGrams = (fatTarget * 0.35).toInt(),
                suggestion = "Grilled Chicken Breast or Soya Chunks Curry with Brown Rice, Dal Tadka, and fresh cucumber salad."
            ),
            MealPlanItem(
                title = "Pre/Post Workout Recharge",
                mealType = "Snack",
                calories = (dailyCalories * 0.15).toInt(),
                proteinGrams = (proteinTarget * 0.15).toInt(),
                carbsGrams = (carbsTarget * 0.20).toInt(),
                fatGrams = (fatTarget * 0.10).toInt(),
                suggestion = "Sprouted Moong Chaat or Roasted Makhana with whey protein / Greek yogurt."
            ),
            MealPlanItem(
                title = "Recovery & Repair Dinner",
                mealType = "Dinner",
                calories = (dailyCalories * 0.22).toInt(),
                proteinGrams = (proteinTarget * 0.22).toInt(),
                carbsGrams = (carbsTarget * 0.15).toInt(),
                fatGrams = (fatTarget * 0.30).toInt(),
                suggestion = "Tofu / Fish Curry with steamed vegetables and 1 Multigrain Roti with a touch of ghee."
            )
        )

        val nutritionPlan = NutritionPlan(
            dailyCalories = dailyCalories,
            proteinTargetGrams = proteinTarget,
            carbsTargetGrams = carbsTarget,
            fatTargetGrams = fatTarget,
            waterTargetMl = (profile.weightKg * 40).toInt(), // 40ml per kg
            currentWaterMl = 1250,
            meals = meals
        )

        return Pair(days, nutritionPlan)
    }

    private fun getGymSplit(profile: UserProfile): List<WorkoutDay> {
        val (repRange, restSec) = when (profile.fitnessGoal) {
            FitnessGoal.STRENGTH -> "4-6" to 120
            FitnessGoal.MUSCLE_BUILD -> "8-12" to 75
            FitnessGoal.FAT_LOSS -> "10-15" to 45
            FitnessGoal.ENDURANCE -> "15-20" to 30
        }

        return listOf(
            WorkoutDay(
                id = "day_push",
                dayTitle = "Day 1: Push (Chest, Shoulders, Triceps)",
                focusArea = "Upper Body Pushing & Hypertrophy",
                exercises = listOf(
                    Exercise("ex_bench", "Barbell Bench Press", "Chest", 4, repRange, restSec, 50f, "Keep shoulder blades retracted and elbows at 45 degrees."),
                    Exercise("ex_incline_db", "Incline Dumbbell Press", "Upper Chest", 3, repRange, restSec, 18f, "Angle bench to 30 degrees, feel the stretch at bottom."),
                    Exercise("ex_ohp", "Standing Overhead Press", "Shoulders", 3, repRange, restSec, 30f, "Engage glutes and core to protect your lower back."),
                    Exercise("ex_lat_raise", "Dumbbell Lateral Raises", "Side Delts", 4, "12-15", 45, 8f, "Lead with elbows, pause 1 second at shoulder height."),
                    Exercise("ex_tricep_push", "Cable Tricep Pushdown", "Triceps", 3, "10-12", 45, 20f, "Keep elbows pinned to your ribs throughout.")
                )
            ),
            WorkoutDay(
                id = "day_pull",
                dayTitle = "Day 2: Pull (Back, Rear Delts, Biceps)",
                focusArea = "Back Width, Density & Biceps",
                exercises = listOf(
                    Exercise("ex_deadlift", "Conventional Deadlift", "Back & Posterior Chain", 3, "5", 120, 70f, "Hinge at hips, keep spine neutral, push floor away."),
                    Exercise("ex_lat_pulldown", "Lat Pulldown (Wide Grip)", "Lats", 4, repRange, restSec, 45f, "Pull bar down to upper chest, drive elbows down."),
                    Exercise("ex_seated_row", "Seated Cable Row", "Mid Back", 3, repRange, restSec, 40f, "Squeeze your shoulder blades together at full contraction."),
                    Exercise("ex_face_pull", "Rope Face Pulls", "Rear Delts / Posture", 4, "15", 45, 15f, "Pull rope towards eye level, rotate hands outward."),
                    Exercise("ex_bicep_curl", "Incline Dumbbell Bicep Curls", "Biceps", 3, "10-12", 45, 10f, "Allow full elbow extension at bottom for maximum stretch.")
                )
            ),
            WorkoutDay(
                id = "day_legs",
                dayTitle = "Day 3: Legs & Core Power",
                focusArea = "Quads, Hamstrings, Glutes & Abs",
                exercises = listOf(
                    Exercise("ex_squat", "Barbell Back Squat", "Quads & Glutes", 4, repRange, restSec, 60f, "Hit parallel depth, drive knees outward on way up."),
                    Exercise("ex_rdl", "Romanian Deadlift (RDL)", "Hamstrings", 3, repRange, restSec, 45f, "Feel deep hamstring stretch, push hips far back."),
                    Exercise("ex_leg_press", "Leg Press", "Legs", 3, "10-12", restSec, 100f, "Keep feet shoulder-width, avoid locking knees at top."),
                    Exercise("ex_calf_raise", "Standing Calf Raises", "Calves", 4, "15", 30, 40f, "2-second pause at peak stretch and peak contraction."),
                    Exercise("ex_hanging_leg", "Hanging Leg Raises", "Core", 3, "12-15", 45, 0f, "Curl pelvis up towards sternum to isolate lower abs.")
                )
            ),
            WorkoutDay(
                id = "day_upper",
                dayTitle = "Day 4: Upper Body Sculpt & Arms",
                focusArea = "Arms, Delts & Upper Symmetry",
                exercises = listOf(
                    Exercise("ex_db_shoulder", "Dumbbell Shoulder Press", "Shoulders", 3, repRange, restSec, 16f, "Press overhead with controlled tempo."),
                    Exercise("ex_chest_fly", "Cable Chest Flys", "Chest", 3, "12-15", 45, 12f, "Imagine hugging a tree, squeeze inner chest."),
                    Exercise("ex_hammer_curl", "Dumbbell Hammer Curls", "Brachialis & Forearms", 3, "10-12", 45, 12f, "Palms facing each other, control the eccentric descent."),
                    Exercise("ex_skull_crush", "EZ Bar Skullcrushers", "Triceps", 3, "10-12", 45, 18f, "Lower bar to forehead level with upper arms steady."),
                    Exercise("ex_plank", "Weighted Core Plank", "Abs", 3, "45s", 45, 10f, "Maintain flat spine and tense abs as if bracing for a punch.")
                )
            )
        )
    }

    private fun getDumbbellSplit(profile: UserProfile): List<WorkoutDay> {
        return listOf(
            WorkoutDay(
                id = "db_day1",
                dayTitle = "Day 1: Dumbbell Upper Body Blast",
                focusArea = "Chest, Shoulders & Back",
                exercises = listOf(
                    Exercise("db_floor_press", "Dumbbell Floor / Flat Press", "Chest", 4, "10-12", 60, 16f, "Touch dumbbells lightly at top, keep control."),
                    Exercise("db_row", "Bent-Over Single Arm DB Row", "Back", 4, "10-12", 60, 14f, "Pull elbow towards hip pocket, pause 1 second."),
                    Exercise("db_seated_press", "Seated Dumbbell Shoulder Press", "Shoulders", 3, "10-12", 60, 12f, "Press smooth and lockout without shrugging."),
                    Exercise("db_curls", "Standing Dumbbell Bicep Curls", "Biceps", 3, "12", 45, 10f, "Supinate wrist on the way up.")
                )
            ),
            WorkoutDay(
                id = "db_day2",
                dayTitle = "Day 2: Dumbbell Lower Body & Core",
                focusArea = "Quads, Glutes & Abs",
                exercises = listOf(
                    Exercise("db_goblet_squat", "Dumbbell Goblet Squat", "Quads & Glutes", 4, "12-15", 60, 20f, "Hold dumbbell tight to chest, sit between hips."),
                    Exercise("db_lunges", "Dumbbell Walking Lunges", "Quads & Glutes", 3, "12 per leg", 60, 10f, "Keep chest tall, 90 degree angle on front knee."),
                    Exercise("db_rdl", "Dumbbell Romanian Deadlift", "Hamstrings", 4, "10-12", 60, 16f, "Slight knee bend, hinge at hips."),
                    Exercise("db_russian_twist", "Dumbbell Russian Twists", "Obliques", 3, "20 total", 45, 6f, "Rotate torso fully from side to side.")
                )
            )
        )
    }

    private fun getBodyweightSplit(profile: UserProfile): List<WorkoutDay> {
        return listOf(
            WorkoutDay(
                id = "bw_day1",
                dayTitle = "Day 1: Calisthenics Push & Core",
                focusArea = "Chest, Triceps & Core Strength",
                exercises = listOf(
                    Exercise("bw_pushup", "Standard / Diamond Push-Ups", "Chest & Triceps", 4, "15-20", 45, 0f, "Full range of motion: chest to ground."),
                    Exercise("bw_pike_pushup", "Pike Push-Ups (Shoulder Focus)", "Delts", 3, "10-12", 60, 0f, "Hips high in the air, lower crown of head forward."),
                    Exercise("bw_chair_dips", "Bench / Chair Tricep Dips", "Triceps", 3, "15", 45, 0f, "Lower until arms form 90 degrees, press up."),
                    Exercise("bw_hollow_body", "Hollow Body Core Hold", "Abs", 4, "30-45s", 45, 0f, "Press lower back flat into floor, toes pointed.")
                )
            ),
            WorkoutDay(
                id = "bw_day2",
                dayTitle = "Day 2: Calisthenics Legs & Explosive Cardio",
                focusArea = "Leg Endurance & Fat Burn",
                exercises = listOf(
                    Exercise("bw_squats", "Tempo Air Squats", "Quads", 4, "20-25", 45, 0f, "3 seconds down, explosive drive up."),
                    Exercise("bw_jump_lunges", "Plyometric Jump Lunges", "Leg Power & Cardio", 3, "12 per leg", 60, 0f, "Soft landing on the balls of your feet."),
                    Exercise("bw_glute_bridge", "Single-Leg Glute Bridges", "Glutes", 3, "15 per leg", 45, 0f, "Drive through heel, hold at peak 2 seconds."),
                    Exercise("bw_mountain_climbers", "Fast Mountain Climbers", "Core & Cardio", 4, "40s", 30, 0f, "Keep hips down, drive knees towards chest.")
                )
            )
        )
    }

    private fun generateSmartAdvice(question: String, profile: UserProfile): String {
        val q = question.lowercase()
        return when {
            q.contains("protein") || q.contains("diet") || q.contains("food") -> {
                "💡 **Nutrition Coach Tip:** Aim for approximately **${(profile.weightKg * 2.0).toInt()}g of daily protein** spread across 3-4 meals to optimize muscle protein synthesis. Combine lean meats, eggs, fish, Greek yogurt, or plant proteins with complex carbs around your workout window."
            }
            q.contains("bench") || q.contains("chest") -> {
                "🏋️ **Bench Press Mastery:**\n1. Plant feet firmly on the floor and retract your shoulder blades.\n2. Keep elbows tucked at roughly 45-75 degrees (avoid flaring to 90).\n3. Lower with control (2-second negative) and drive up explosively pushing through your heels."
            }
            q.contains("fat loss") || q.contains("cut") || q.contains("lose weight") -> {
                "🔥 **Fat Loss Protocol:**\n- Maintain a modest 300-500 calorie deficit.\n- Keep protein high (${(profile.weightKg * 2.2).toInt()}g) to preserve muscle mass.\n- Walk 8,000-10,000 steps daily as low-fatigue NEAT (Non-Exercise Activity Thermogenesis).\n- Lift heavy compound weights to signal your body to retain muscle."
            }
            q.contains("sore") || q.contains("recovery") || q.contains("rest") -> {
                "💤 **Muscle Recovery Essentials:**\n- Prioritize 7-8.5 hours of quality sleep—that's when human growth hormone peaks.\n- Drink at least ${((profile.weightKg * 35) / 1000).toInt()}L of water daily.\n- Light active recovery (e.g. 20-minute walk or gentle stretching) boosts blood circulation and reduces DOMS (delayed onset muscle soreness)."
            }
            else -> {
                "💪 **FitPulse Coach Advice:** For your goal of **${profile.fitnessGoal.displayName}**, consistency and progressive overload are your best friends. Track every set, add 1 rep or 1-2kg each week, and ensure your sleep and protein targets are met. You have got this!"
            }
        }
    }
}
