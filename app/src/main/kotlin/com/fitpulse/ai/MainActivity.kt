package com.fitpulse.ai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.fitpulse.ai.data.FitnessRepository
import com.fitpulse.ai.data.StepCounterManager
import com.fitpulse.ai.data.WorkoutMusicPlayer
import com.fitpulse.ai.ui.theme.FitPulseAITheme

class MainActivity : ComponentActivity() {

    private lateinit var repository: FitnessRepository
    private var stepCounterManager: StepCounterManager? = null

    private val activityRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            stepCounterManager?.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = FitnessRepository(applicationContext)
        stepCounterManager = StepCounterManager(applicationContext) { steps ->
            repository.updateDeviceSteps(steps)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted && stepCounterManager?.isSensorAvailable == true) {
                activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        enableEdgeToEdge()
        setContent {
            FitPulseAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(sharedRepository = repository)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        ) {
            stepCounterManager?.start()
        }
    }

    override fun onPause() {
        stepCounterManager?.stop()
        super.onPause()
    }

    override fun onDestroy() {
        WorkoutMusicPlayer.release()
        super.onDestroy()
    }
}
