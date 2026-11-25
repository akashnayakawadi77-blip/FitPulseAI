package com.fitpulse.ai

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.fitpulse.ai.data.FitnessRepository
import com.fitpulse.ai.ui.main.MainScreen
import com.fitpulse.ai.ui.screens.OnboardingScreen
import com.fitpulse.ai.ui.screens.ProfileScreen

@Composable
fun MainNavigation(
    sharedRepository: FitnessRepository? = null
) {
    val context = LocalContext.current
    val repository = remember(sharedRepository) {
        sharedRepository ?: FitnessRepository(context.applicationContext)
    }
    val userProfile by repository.userProfile.collectAsState()

    var showProfile by remember { mutableStateOf(false) }
    var forceOnboarding by remember { mutableStateOf(false) }

    when {
        !userProfile.onboardingCompleted || forceOnboarding -> {
            OnboardingScreen(
                repository = repository,
                onCompleted = {
                    forceOnboarding = false
                }
            )
        }
        showProfile -> {
            ProfileScreen(
                repository = repository,
                onBack = { showProfile = false },
                onRerunOnboarding = {
                    showProfile = false
                    forceOnboarding = true
                }
            )
        }
        else -> {
            MainScreen(
                repository = repository,
                onOpenProfile = { showProfile = true }
            )
        }
    }
}
