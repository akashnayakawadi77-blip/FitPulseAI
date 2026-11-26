package com.fitpulse.ai.data

import com.fitpulse.ai.data.model.MealScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object OpenFoodFactsService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun lookupBarcode(barcode: String): MealScanResult? = withContext(Dispatchers.IO) {
        val cleanCode = barcode.trim()
        if (cleanCode.isBlank()) return@withContext null

        val url = "https://world.openfoodfacts.org/api/v2/product/$cleanCode.json"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "FitPulseAI-Android/1.4.0")
            .build()

        return@withContext try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val root = json.parseToJsonElement(body).jsonObject
                val status = root["status"]?.jsonPrimitive?.intOrNull ?: 0
                if (status != 1) return@withContext null

                val product = root["product"]?.jsonObject ?: return@withContext null
                val name = product["product_name"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                    ?: product["generic_name"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                    ?: "Packaged Food ($cleanCode)"
                val nutriments = product["nutriments"]?.jsonObject

                val kcal = nutriments?.get("energy-kcal_100g")?.jsonPrimitive?.doubleOrNull?.roundToInt()
                    ?: nutriments?.get("energy-kcal")?.jsonPrimitive?.doubleOrNull?.roundToInt()
                    ?: 200
                val protein = nutriments?.get("proteins_100g")?.jsonPrimitive?.doubleOrNull?.roundToInt() ?: 8
                val carbs = nutriments?.get("carbohydrates_100g")?.jsonPrimitive?.doubleOrNull?.roundToInt() ?: 25
                val fat = nutriments?.get("fat_100g")?.jsonPrimitive?.doubleOrNull?.roundToInt() ?: 7
                val servingSize = product["serving_size"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() } ?: "100g"

                MealScanResult(
                    name = name,
                    calories = kcal,
                    proteinGrams = protein,
                    carbsGrams = carbs,
                    fatGrams = fat,
                    servingDesc = "Per $servingSize (Barcode: $cleanCode)",
                    note = "Fetched from OpenFoodFacts",
                    usedGeminiVision = false
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
