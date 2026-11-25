package com.fitpulse.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fitpulse.ai.data.FitnessRepository
import com.fitpulse.ai.data.FoodCatalog
import com.fitpulse.ai.data.model.FoodCatalogItem
import com.fitpulse.ai.data.model.LoggedFoodItem
import com.fitpulse.ai.data.model.MealPlanItem
import com.fitpulse.ai.ui.components.AnimatedCircularScoreRing
import com.fitpulse.ai.ui.components.GlassCard
import com.fitpulse.ai.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun NutritionScreen(
    repository: FitnessRepository,
    modifier: Modifier = Modifier
) {
    val nutritionPlan by repository.nutritionPlan.collectAsState()
    val loggedFoods by repository.loggedFoods.collectAsState()
    var showLogFoodDialog by remember { mutableStateOf(false) }

    val loggedCalories = loggedFoods.sumOf { it.calories }
    val loggedProtein = loggedFoods.sumOf { it.proteinGrams }
    val loggedCarbs = loggedFoods.sumOf { it.carbsGrams }
    val loggedFat = loggedFoods.sumOf { it.fatGrams }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GymDarkBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Diet & Fuel",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Search Indian & fitness foods or log meals",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Button(
                onClick = { showLogFoodDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log Food", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        if (nutritionPlan == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonGreen)
            }
        } else {
            val plan = nutritionPlan!!
            val calProgress = if (plan.dailyCalories > 0) (loggedCalories.toFloat() / plan.dailyCalories).coerceIn(0f, 1f) else 0f

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Calorie & Macronutrient Summary Card
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        accentGlow = EnergeticAmber,
                        borderColor = EnergeticAmber.copy(alpha = 0.28f)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = EnergeticAmber.copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = "DAILY FUEL",
                                            color = EnergeticAmber,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "$loggedCalories",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 32.sp
                                        )
                                        Text(
                                            text = " / ${plan.dailyCalories} kcal",
                                            color = TextSecondary,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(bottom = 5.dp)
                                        )
                                    }
                                }
                                AnimatedCircularScoreRing(
                                    progress = calProgress,
                                    scoreText = "${(calProgress * 100).toInt()}%",
                                    subText = "KCAL",
                                    size = 76.dp,
                                    strokeWidth = 7.dp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MacroItem(
                                    label = "Protein",
                                    amount = "${loggedProtein}g",
                                    target = "/ ${plan.proteinTargetGrams}g",
                                    color = NeonGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                MacroItem(
                                    label = "Carbs",
                                    amount = "${loggedCarbs}g",
                                    target = "/ ${plan.carbsTargetGrams}g",
                                    color = ElectricCyan,
                                    modifier = Modifier.weight(1f)
                                )
                                MacroItem(
                                    label = "Fats",
                                    amount = "${loggedFat}g",
                                    target = "/ ${plan.fatTargetGrams}g",
                                    color = EnergeticAmber,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Today's Food Log Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Food Log (${loggedFoods.size})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        TextButton(onClick = { showLogFoodDialog = true }) {
                            Text("+ Add Item", color = ElectricCyan, fontSize = 13.sp)
                        }
                    }
                }

                if (loggedFoods.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GymDarkBorder))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showLogFoodDialog = true }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = Color.Gray)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "No foods logged today yet. Tap here to search Indian & fitness foods!",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    items(loggedFoods, key = { it.id }) { food ->
                        LoggedFoodRow(
                            item = food,
                            onDelete = { repository.deleteLoggedFood(food.id) }
                        )
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
                    MealCard(
                        meal = meal,
                        onQuickLog = {
                            repository.logFood(
                                LoggedFoodItem(
                                    id = "meal_${System.currentTimeMillis()}_${meal.title.hashCode()}",
                                    name = meal.title,
                                    mealType = meal.mealType,
                                    calories = meal.calories,
                                    proteinGrams = meal.proteinGrams,
                                    carbsGrams = meal.carbsGrams,
                                    fatGrams = meal.fatGrams,
                                    servingDesc = "AI Meal Plan"
                                )
                            )
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    if (showLogFoodDialog) {
        FoodSearchAndLogDialog(
            onDismiss = { showLogFoodDialog = false },
            onLogFood = { item ->
                repository.logFood(item)
                showLogFoodDialog = false
            }
        )
    }
}

@Composable
fun MacroItem(
    label: String,
    amount: String,
    target: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(GymDarkCard, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = amount, color = color, fontWeight = FontWeight.Black, fontSize = 16.sp)
        Text(text = target, color = Color.Gray, fontSize = 11.sp)
    }
}

@Composable
fun LoggedFoodRow(
    item: LoggedFoodItem,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GymDarkBorder, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(containerColor = GymDarkCard, contentColor = ElectricCyan) {
                        Text(item.mealType, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.servingDesc} • ${item.calories} kcal • P:${item.proteinGrams}g C:${item.carbsGrams}g F:${item.fatGrams}g",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun MealCard(
    meal: MealPlanItem,
    onQuickLog: () -> Unit
) {
    var loggedJustNow by remember { mutableStateOf(false) }

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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("P: ${meal.proteinGrams}g", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("C: ${meal.carbsGrams}g", color = ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("F: ${meal.fatGrams}g", color = EnergeticAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        onQuickLog()
                        loggedJustNow = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (loggedJustNow) NeonGreen else ElectricCyan)
                ) {
                    Icon(
                        if (loggedJustNow) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (loggedJustNow) NeonGreen else ElectricCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (loggedJustNow) "Logged" else "Log Meal",
                        color = if (loggedJustNow) NeonGreen else ElectricCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FoodSearchAndLogDialog(
    onDismiss: () -> Unit,
    onLogFood: (LoggedFoodItem) -> Unit
) {
    var tabIndex by remember { mutableIntStateOf(0) } // 0 = Search, 1 = Custom
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedMealType by remember { mutableStateOf("Lunch") }
    var selectedMultiplier by remember { mutableFloatStateOf(1.0f) }

    // Custom entry fields
    var customName by remember { mutableStateOf("") }
    var customCal by remember { mutableStateOf("") }
    var customPro by remember { mutableStateOf("") }
    var customCarb by remember { mutableStateOf("") }
    var customFat by remember { mutableStateOf("") }

    val filteredFoods = remember(searchQuery, selectedCategory) {
        FoodCatalog.items.filter { item ->
            val matchesCat = selectedCategory == "All" || item.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .border(1.dp, GymDarkBorder, RoundedCornerShape(22.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Log Food", fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // Mode Switch: Search vs Custom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GymDarkCard, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    listOf("Food Search", "Custom Entry").forEachIndexed { idx, label ->
                        val sel = tabIndex == idx
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { tabIndex = idx },
                            shape = RoundedCornerShape(10.dp),
                            color = if (sel) NeonGreen else Color.Transparent
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(label, color = if (sel) Color.Black else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Meal Type Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("Breakfast", "Lunch", "Snack", "Dinner")) { mt ->
                        val sel = selectedMealType == mt
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (sel) ElectricCyan.copy(alpha = 0.2f) else GymDarkCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (sel) ElectricCyan else GymDarkBorder),
                            modifier = Modifier.clickable { selectedMealType = mt }
                        ) {
                            Text(
                                text = mt,
                                color = if (sel) ElectricCyan else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (tabIndex == 0) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search Paneer, Dal, Oats, Chicken...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonGreen) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = GymDarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category filter
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(FoodCatalog.categories) { cat ->
                            val sel = selectedCategory == cat
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (sel) NeonGreen else GymDarkCard,
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    color = if (sel) Color.Black else Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Serving multiplier
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Serving:", color = Color.Gray, fontSize = 12.sp)
                        listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { mult ->
                            val sel = selectedMultiplier == mult
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (sel) EnergeticAmber.copy(alpha = 0.25f) else GymDarkCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (sel) EnergeticAmber else GymDarkBorder),
                                modifier = Modifier.clickable { selectedMultiplier = mult }
                            ) {
                                Text(
                                    text = "${mult}x",
                                    color = if (sel) EnergeticAmber else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredFoods, key = { it.id }) { food ->
                            FoodCatalogRow(
                                item = food,
                                multiplier = selectedMultiplier,
                                onAdd = {
                                    onLogFood(
                                        LoggedFoodItem(
                                            id = "food_${System.currentTimeMillis()}_${food.id}",
                                            name = food.name,
                                            mealType = selectedMealType,
                                            calories = (food.calories * selectedMultiplier).roundToInt(),
                                            proteinGrams = (food.proteinGrams * selectedMultiplier).roundToInt(),
                                            carbsGrams = (food.carbsGrams * selectedMultiplier).roundToInt(),
                                            fatGrams = (food.fatGrams * selectedMultiplier).roundToInt(),
                                            servingDesc = "${selectedMultiplier}x ${food.servingDesc}"
                                        )
                                    )
                                }
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Food Name") },
                            placeholder = { Text("e.g., Homemade Chicken Biryani") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = GymDarkBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = customCal,
                            onValueChange = { customCal = it.filter { c -> c.isDigit() } },
                            label = { Text("Calories (kcal)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = GymDarkBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customPro,
                                onValueChange = { customPro = it.filter { c -> c.isDigit() } },
                                label = { Text("Protein (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGreen,
                                    unfocusedBorderColor = GymDarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = customCarb,
                                onValueChange = { customCarb = it.filter { c -> c.isDigit() } },
                                label = { Text("Carbs (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGreen,
                                    unfocusedBorderColor = GymDarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = customFat,
                                onValueChange = { customFat = it.filter { c -> c.isDigit() } },
                                label = { Text("Fat (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGreen,
                                    unfocusedBorderColor = GymDarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                if (customName.isNotBlank()) {
                                    onLogFood(
                                        LoggedFoodItem(
                                            id = "custom_${System.currentTimeMillis()}",
                                            name = customName.trim(),
                                            mealType = selectedMealType,
                                            calories = customCal.toIntOrNull() ?: 250,
                                            proteinGrams = customPro.toIntOrNull() ?: 10,
                                            carbsGrams = customCarb.toIntOrNull() ?: 30,
                                            fatGrams = customFat.toIntOrNull() ?: 8,
                                            servingDesc = "Custom"
                                        )
                                    )
                                }
                            },
                            enabled = customName.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                        ) {
                            Text("Save Custom Food", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodCatalogRow(
    item: FoodCatalogItem,
    multiplier: Float,
    onAdd: () -> Unit
) {
    val cals = (item.calories * multiplier).roundToInt()
    val pro = (item.proteinGrams * multiplier).roundToInt()

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = GymDarkCard,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAdd() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    text = "${item.servingDesc} • $cals kcal • ${pro}g Protein",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(NeonGreen.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = NeonGreen, modifier = Modifier.size(18.dp))
            }
        }
    }
}
