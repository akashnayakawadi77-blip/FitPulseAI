package com.fitpulse.ai.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.fitpulse.ai.data.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalDatabase(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    companion object {
        private const val DB_NAME = "fitpulse_ai.db"
        private const val DB_VERSION = 1

        private const val TABLE_KV = "kv_store"
        private const val COL_KEY = "k"
        private const val COL_VAL = "v"

        private const val TABLE_WEIGHT = "weight_logs"
        private const val TABLE_FOOD_LOG = "logged_foods"
        private const val TABLE_SESSIONS = "workout_sessions"

        fun todayDateKey(): String =
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_KV (
                $COL_KEY TEXT PRIMARY KEY,
                $COL_VAL TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_WEIGHT (
                timestamp INTEGER PRIMARY KEY,
                dateDisplay TEXT NOT NULL,
                weightKg REAL NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_FOOD_LOG (
                id TEXT PRIMARY KEY,
                dateKey TEXT NOT NULL,
                payload TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_SESSIONS (
                id TEXT PRIMARY KEY,
                startTimeMillis INTEGER NOT NULL,
                payload TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }

    private fun putString(key: String, value: String) {
        val cv = ContentValues().apply {
            put(COL_KEY, key)
            put(COL_VAL, value)
        }
        writableDatabase.insertWithOnConflict(TABLE_KV, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    private fun getString(key: String): String? {
        readableDatabase.query(TABLE_KV, arrayOf(COL_VAL), "$COL_KEY = ?", arrayOf(key), null, null, null).use { c ->
            return if (c.moveToFirst()) c.getString(0) else null
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        putString("user_profile", json.encodeToString(profile))
    }

    fun loadUserProfile(): UserProfile? {
        val raw = getString("user_profile") ?: return null
        return runCatching { json.decodeFromString<UserProfile>(raw) }.getOrNull()
    }

    fun saveWorkoutDays(days: List<WorkoutDay>) {
        putString("workout_days", json.encodeToString(days))
    }

    fun loadWorkoutDays(): List<WorkoutDay>? {
        val raw = getString("workout_days") ?: return null
        return runCatching { json.decodeFromString<List<WorkoutDay>>(raw) }.getOrNull()
    }

    fun saveSelectedDayIndex(index: Int) {
        putString("selected_day_index", index.toString())
    }

    fun loadSelectedDayIndex(): Int {
        return getString("selected_day_index")?.toIntOrNull() ?: 0
    }

    fun saveNutritionPlan(plan: NutritionPlan) {
        putString("nutrition_plan", json.encodeToString(plan))
    }

    fun loadNutritionPlan(): NutritionPlan? {
        val raw = getString("nutrition_plan") ?: return null
        return runCatching { json.decodeFromString<NutritionPlan>(raw) }.getOrNull()
    }

    fun saveMuscleRecoveries(map: Map<MuscleGroup, MuscleRecoveryState>) {
        putString("muscle_recoveries", json.encodeToString(map))
    }

    fun loadMuscleRecoveries(): Map<MuscleGroup, MuscleRecoveryState>? {
        val raw = getString("muscle_recoveries") ?: return null
        return runCatching { json.decodeFromString<Map<MuscleGroup, MuscleRecoveryState>>(raw) }.getOrNull()
    }

    fun saveChatMessages(messages: List<AiChatMessage>) {
        putString("chat_messages", json.encodeToString(messages.takeLast(60)))
    }

    fun loadChatMessages(): List<AiChatMessage>? {
        val raw = getString("chat_messages") ?: return null
        return runCatching { json.decodeFromString<List<AiChatMessage>>(raw) }.getOrNull()
    }

    fun saveReminderSettings(settings: ReminderSettings) {
        putString("reminder_settings", json.encodeToString(settings))
    }

    fun loadReminderSettings(): ReminderSettings {
        val raw = getString("reminder_settings") ?: return ReminderSettings()
        return runCatching { json.decodeFromString<ReminderSettings>(raw) }.getOrDefault(ReminderSettings())
    }

    fun saveTransformationPhotos(photos: TransformationPhotos) {
        putString("transformation_photos", json.encodeToString(photos))
    }

    fun loadTransformationPhotos(): TransformationPhotos {
        val raw = getString("transformation_photos") ?: return TransformationPhotos()
        return runCatching { json.decodeFromString<TransformationPhotos>(raw) }.getOrDefault(TransformationPhotos())
    }

    fun saveDailySteps(dateKey: String, steps: Int) {
        putString("steps_$dateKey", steps.toString())
    }

    fun loadDailySteps(dateKey: String): Int {
        return getString("steps_$dateKey")?.toIntOrNull() ?: 0
    }

    fun saveLastActiveDate(dateKey: String) {
        putString("last_active_date", dateKey)
    }

    fun loadLastActiveDate(): String? = getString("last_active_date")

    fun insertWeightLog(entry: WeightLogEntry) {
        val cv = ContentValues().apply {
            put("timestamp", entry.timestamp)
            put("dateDisplay", entry.dateDisplay)
            put("weightKg", entry.weightKg)
        }
        writableDatabase.insertWithOnConflict(TABLE_WEIGHT, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun loadWeightLogs(): List<WeightLogEntry> {
        val list = mutableListOf<WeightLogEntry>()
        readableDatabase.query(
            TABLE_WEIGHT,
            arrayOf("timestamp", "dateDisplay", "weightKg"),
            null, null, null, null,
            "timestamp DESC"
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    WeightLogEntry(
                        timestamp = c.getLong(0),
                        dateDisplay = c.getString(1),
                        weightKg = c.getFloat(2)
                    )
                )
            }
        }
        return list
    }

    fun insertLoggedFood(item: LoggedFoodItem) {
        val dateKey = item.dateKey.ifBlank { todayDateKey() }
        val normalized = item.copy(dateKey = dateKey)
        val cv = ContentValues().apply {
            put("id", normalized.id)
            put("dateKey", dateKey)
            put("payload", json.encodeToString(normalized))
            put("timestamp", normalized.timestamp)
        }
        writableDatabase.insertWithOnConflict(TABLE_FOOD_LOG, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteLoggedFood(id: String) {
        writableDatabase.delete(TABLE_FOOD_LOG, "id = ?", arrayOf(id))
    }

    fun loadLoggedFoodsForDate(dateKey: String = todayDateKey()): List<LoggedFoodItem> {
        val list = mutableListOf<LoggedFoodItem>()
        readableDatabase.query(
            TABLE_FOOD_LOG,
            arrayOf("payload"),
            "dateKey = ?",
            arrayOf(dateKey),
            null, null,
            "timestamp DESC"
        ).use { c ->
            while (c.moveToNext()) {
                runCatching { json.decodeFromString<LoggedFoodItem>(c.getString(0)) }
                    .getOrNull()?.let { list.add(it) }
            }
        }
        return list
    }

    fun insertWorkoutSession(session: WorkoutSession) {
        val cv = ContentValues().apply {
            put("id", session.id)
            put("startTimeMillis", session.startTimeMillis)
            put("payload", json.encodeToString(session))
        }
        writableDatabase.insertWithOnConflict(TABLE_SESSIONS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun loadWorkoutSessions(): List<WorkoutSession> {
        val list = mutableListOf<WorkoutSession>()
        readableDatabase.query(
            TABLE_SESSIONS,
            arrayOf("payload"),
            null, null, null, null,
            "startTimeMillis DESC"
        ).use { c ->
            while (c.moveToNext()) {
                runCatching { json.decodeFromString<WorkoutSession>(c.getString(0)) }
                    .getOrNull()?.let { list.add(it) }
            }
        }
        return list
    }

    fun clearAllData() {
        writableDatabase.delete(TABLE_KV, null, null)
        writableDatabase.delete(TABLE_WEIGHT, null, null)
        writableDatabase.delete(TABLE_FOOD_LOG, null, null)
        writableDatabase.delete(TABLE_SESSIONS, null, null)
    }
}
