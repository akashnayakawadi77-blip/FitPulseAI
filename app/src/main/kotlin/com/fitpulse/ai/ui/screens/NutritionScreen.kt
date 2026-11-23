package com.fitpulse.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitpulse.ai.data.FitnessRepository
import com.fitpulse.ai.data.model.MealPlanItem
import com.fitpulse.ai.ui.theme.*

@Composable
fun NutritionScreen(
    repository: FitnessRepository,
    modifier: Modifier = Modifier
) {
    val nutritionPlan by repository.nutritionPlan.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GymDarkBackground)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Diet & Fuel",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Calculated based on your TDEE & fitness goal",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        if (nutritionPlan == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonGreen)
            }
        } else {
            val plan = nutritionPlan!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Calorie & Macronutrient Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Daily Calorie Target", color = Color.Gray, fontSize = 13.sp)
                                    Text(
                                        text = "${plan.dailyCalories} kcal",
                                        color = NeonGreen,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 28.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(NeonGreen.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = NeonGreen)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 3 Macro Pillars
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MacroItem(
                                    label = "Protein",
                                    amount = "${plan.proteinTargetGrams}g",
                                    color = NeonGreen,
                                    percent = "30%"
                                )
                                MacroItem(
                                    label = "Carbs",
                                    amount = "${plan.carbsTargetGrams}g",
                                    color = ElectricCyan,
                                    percent = "45%"
                                )
                                MacroItem(
                                    label = "Fats",
                                    amount = "${plan.fatTargetGrams}g",
                                    color = EnergeticAmber,
                                    percent = "25%"
                                )
                            }
                        }
                    }
                }

                // Hydration & Water Tracker Card
                item {
                    val waterPercent = (plan.currentWaterMl.toFloat() / plan.waterTargetMl.toFloat()).coerceIn(0f, 1f)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(ElectricCyan.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Water Intake Tracker", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                        Text("Stay hydrated for peak recovery", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }

                                Text(
                                    text = "${plan.currentWaterMl} / ${plan.waterTargetMl} ml",
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LinearProgressIndicator(
                                progress = { waterPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = ElectricCyan,
                                trackColor = GymDarkCard
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { repository.addWaterIntake(250) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GymDarkCard),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("+250 ml", color = ElectricCyan, fontSize = 13.sp)
                                }
                                Button(
                                    onClick = { repository.addWaterIntake(500) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GymDarkCard),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("+500 ml", color = ElectricCyan, fontSize = 13.sp)
                                }
                                IconButton(
                                    onClick = { repository.resetWaterIntake() },
                                    modifier = Modifier.background(GymDarkCard, CircleShape)
                                ) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = "Reset", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                // AI Meal Suggestions Section
                item {
                    Text(
                        text = "AI Recommended Meal Plan",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                items(plan.meals) { meal ->
                    MealCard(meal = meal)
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun MacroItem(
    label: String,
    amount: String,
    color: Color,
    percent: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(GymDarkCard, RoundedCornerShape(14.dp))
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = amount, color = color, fontWeight = FontWeight.Black, fontSize = 16.sp)
        Text(text = percent, color = Color.Gray, fontSize = 11.sp)
    }
}

@Composable
fun MealCard(meal: MealPlanItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GymDarkBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(containerColor = GymDarkCard, contentColor = NeonGreen) {
                        Text(meal.mealType, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(meal.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                }
                Text("${meal.calories} kcal", fontWeight = FontWeight.Bold, color = EnergeticAmber, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = meal.suggestion,
                color = Color.LightGray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("P: ${meal.proteinGrams}g", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("C: ${meal.carbsGrams}g", color = ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("F: ${meal.fatGrams}g", color = EnergeticAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
