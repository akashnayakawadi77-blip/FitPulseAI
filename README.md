# 💪 FitPulseAI — Your Personal AI Fitness Companion

<p align="center">
  <img src="screenshots/home.png" width="250" alt="Home Screen"/>
</p>

## 🚀 About

**FitPulseAI** is an AI-powered Android fitness application built with **Kotlin + Jetpack Compose + Material 3**. It provides personalized workout plans, nutrition tracking, muscle recovery monitoring, and an AI fitness coach powered by Google Gemini.

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🏠 **Home Dashboard** | Fitness score, daily targets (calories, protein, water, steps), today's workout |
| 🏋️ **Workout Planner** | 6-day push/pull/legs split with weight adjusters and rest timer |
| 🥗 **Nutrition Tracker** | Calorie & macro tracking, water intake, AI-generated Indian meal plans |
| 🤖 **AI Coach** | Gemini-powered fitness coach with workout/diet plan generation |
| 📊 **Progress** | Muscle recovery radar, weekly training consistency, weight journey |
| 💪 **Muscle Recovery** | Fitbod-style freshness tracker per muscle group |

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Theme:** Dark gym aesthetic (NeonGreen, ElectricCyan, EnergeticAmber)
- **AI:** Google Gemini API (gemini-2.0-flash) with smart offline fallback
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 36

## 📱 Screenshots

The app features a 5-tab navigation: **Home** | **Workout** | **Nutrition** | **Coach** | **Progress**

## 🏗️ Project Structure

```
app/src/main/kotlin/com/fitpulse/ai/
├── MainActivity.kt
├── Navigation.kt
├── data/
│   ├── AiCoachService.kt        # Gemini API integration
│   ├── FitnessRepository.kt     # Central data repository
│   └── model/Models.kt          # Data models
├── ui/
│   ├── components/
│   │   ├── MuscleRecoveryTracker.kt
│   │   └── RestTimerOverlay.kt
│   ├── main/
│   │   └── MainScreen.kt        # 5-tab navigation
│   ├── screens/
│   │   ├── HomeScreen.kt
│   │   ├── WorkoutScreen.kt
│   │   ├── NutritionScreen.kt
│   │   ├── AiCoachScreen.kt
│   │   ├── ProgressScreen.kt
│   │   └── ProfileScreen.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
```

## 🚀 Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/akashnayakawadi77-blip/FitPulseAI.git
   ```

2. **Open in Android Studio**

3. **Add Gemini API Key** (optional — app works offline too)
   - Get a key from [Google AI Studio](https://aistudio.google.com/)
   - Enter it in the AI Coach tab settings

4. **Build & Run**
   ```bash
   ./gradlew assembleDebug
   ```

## 📦 Download APK

Check the [Releases](https://github.com/akashnayakawadi77-blip/FitPulseAI/releases) page for the latest APK.

## 🗺️ Roadmap

- [ ] Room database for persistent storage
- [ ] Onboarding flow with goal selection
- [ ] Food search & barcode scanning
- [ ] Exercise instruction videos
- [ ] Weekly/monthly trend charts
- [ ] Subscription model (Free vs Premium)
- [ ] Google Play Store release

## 📄 License

This project is for personal and educational use.

---

**Built with ❤️ using Kotlin, Jetpack Compose & Google Gemini AI**
