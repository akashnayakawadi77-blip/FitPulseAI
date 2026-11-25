package com.fitpulse.ai.data

import com.fitpulse.ai.data.model.FoodCatalogItem

object FoodCatalog {
    val items: List<FoodCatalogItem> = listOf(
        // Indian Favorites
        FoodCatalogItem("in_paneer_bhurji", "Paneer Bhurji (150g)", "Indian", "150g bowl", 340, 22, 6, 25),
        FoodCatalogItem("in_paneer_raw", "Low-Fat Paneer (100g)", "Indian", "100g", 210, 20, 4, 12),
        FoodCatalogItem("in_dal_tadka", "Dal Tadka / Toor Dal", "Indian", "1 bowl (200ml)", 220, 11, 30, 6),
        FoodCatalogItem("in_moong_dal", "Moong Dal Chilla (2 pcs)", "Indian", "2 medium chillas", 250, 14, 34, 6),
        FoodCatalogItem("in_soya_chunks", "Soya Chunks Curry (50g dry)", "Indian", "1 bowl", 210, 26, 17, 3),
        FoodCatalogItem("in_roti", "Whole Wheat Roti / Chapati", "Indian", "1 medium roti", 105, 3, 20, 1),
        FoodCatalogItem("in_brown_rice", "Cooked Brown Rice", "Indian", "1 cup (180g)", 215, 5, 45, 2),
        FoodCatalogItem("in_white_rice", "Steamed White Rice", "Indian", "1 cup (180g)", 230, 4, 50, 1),
        FoodCatalogItem("in_chicken_curry", "Home-style Chicken Curry", "Indian", "1 bowl (200g)", 320, 32, 8, 17),
        FoodCatalogItem("in_tandoori_chicken", "Tandoori Chicken (Breast/Leg)", "Indian", "200g", 290, 42, 3, 11),
        FoodCatalogItem("in_rajma_chawal", "Rajma Chawal", "Indian", "1 plate (300g)", 410, 15, 72, 6),
        FoodCatalogItem("in_chole", "Chole / Chickpea Masala", "Indian", "1 bowl (200g)", 280, 12, 42, 7),
        FoodCatalogItem("in_idli", "Steamed Idli with Sambar", "Indian", "3 idlis + sambar", 280, 10, 52, 3),
        FoodCatalogItem("in_masala_dosa", "Plain / Light Masala Dosa", "Indian", "1 dosa", 290, 7, 46, 8),
        FoodCatalogItem("in_poha", "Kanda Poha with Peanuts", "Indian", "1 bowl (200g)", 260, 6, 44, 7),
        FoodCatalogItem("in_upma", "Vegetable Rava Upma", "Indian", "1 bowl (200g)", 250, 6, 42, 6),
        FoodCatalogItem("in_sprouts", "Sprouted Moong Chaat", "Indian", "1 bowl (150g)", 165, 12, 26, 1),
        FoodCatalogItem("in_makhana", "Roasted Makhana (Fox Nuts)", "Indian", "1 bowl (30g)", 110, 3, 22, 1),
        FoodCatalogItem("in_curd", "Homemade Curd / Dahi", "Indian", "1 cup (150g)", 115, 6, 8, 6),
        FoodCatalogItem("in_egg_bhurji", "Indian Egg Bhurji (3 Eggs)", "Indian", "3 eggs", 260, 19, 4, 18),
        FoodCatalogItem("in_besan_chilla", "Besan Chilla (2 pcs)", "Indian", "2 chillas", 240, 11, 32, 7),
        FoodCatalogItem("in_khichdi", "Moong Dal Khichdi", "Indian", "1 bowl (250g)", 295, 11, 48, 6),

        // High-Protein Staples
        FoodCatalogItem("pr_eggs", "Boiled Whole Eggs (2)", "Protein", "2 large eggs", 145, 13, 1, 10),
        FoodCatalogItem("pr_egg_whites", "Egg Whites (4)", "Protein", "4 whites", 70, 14, 1, 0),
        FoodCatalogItem("pr_chicken_breast", "Grilled Chicken Breast", "Protein", "150g", 248, 46, 0, 5),
        FoodCatalogItem("pr_whey", "Whey Protein Isolate Scoop", "Protein", "1 scoop (30g)", 120, 25, 2, 1),
        FoodCatalogItem("pr_greek_yogurt", "High-Protein Greek Yogurt", "Protein", "150g cup", 130, 15, 7, 4),
        FoodCatalogItem("pr_tofu", "Grilled Tofu", "Protein", "150g", 160, 17, 4, 8),
        FoodCatalogItem("pr_fish", "Grilled Fish / Rohu / Salmon", "Protein", "150g", 250, 32, 0, 12),

        // Carbs & Energy
        FoodCatalogItem("cb_oats", "Rolled Oats (Cooked)", "Carbs", "50g dry", 190, 7, 34, 3),
        FoodCatalogItem("cb_banana", "Medium Banana", "Carbs", "1 piece (115g)", 105, 1, 27, 0),
        FoodCatalogItem("cb_sweet_potato", "Boiled Sweet Potato", "Carbs", "150g", 130, 2, 30, 0),
        FoodCatalogItem("cb_bread", "Multigrain Bread (2 slices)", "Carbs", "2 slices", 150, 6, 26, 2),
        FoodCatalogItem("cb_apple", "Fresh Apple", "Carbs", "1 medium", 95, 0, 25, 0),

        // Healthy Fats & Snacks
        FoodCatalogItem("sn_peanut_butter", "Natural Peanut Butter", "Snacks", "1 tbsp (16g)", 95, 4, 3, 8),
        FoodCatalogItem("sn_almonds", "Soaked Almonds (15 pcs)", "Snacks", "18g", 105, 4, 4, 9),
        FoodCatalogItem("sn_walnuts", "Walnuts (5 halves)", "Snacks", "15g", 100, 2, 2, 10),
        FoodCatalogItem("sn_milk", "Toned Milk (1 Glass)", "Snacks", "250ml", 145, 8, 12, 7),
        FoodCatalogItem("sn_black_coffee", "Black Coffee / Green Tea", "Snacks", "1 cup", 5, 0, 1, 0),
        FoodCatalogItem("sn_salad", "Cucumber Tomato Salad", "Snacks", "1 bowl", 45, 2, 9, 0)
    )

    val categories: List<String> = listOf("All", "Indian", "Protein", "Carbs", "Snacks")
}
