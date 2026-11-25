package com.fitpulse.ai.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepCounterManager(
    context: Context,
    private val onStepsChanged: (Int) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val prefs = context.getSharedPreferences("fitpulse_steps", Context.MODE_PRIVATE)

    val isSensorAvailable: Boolean
        get() = stepSensor != null

    fun start() {
        stepSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_STEP_COUNTER) return
        val totalSinceBoot = event.values.firstOrNull()?.toInt() ?: return
        val today = LocalDatabase.todayDateKey()
        val savedDate = prefs.getString("baseline_date", null)
        var baseline = prefs.getInt("baseline_steps", -1)

        if (savedDate != today || baseline < 0 || totalSinceBoot < baseline) {
            baseline = totalSinceBoot
            prefs.edit()
                .putString("baseline_date", today)
                .putInt("baseline_steps", baseline)
                .apply()
        }

        val todaySteps = (totalSinceBoot - baseline).coerceAtLeast(0)
        onStepsChanged(todaySteps)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
