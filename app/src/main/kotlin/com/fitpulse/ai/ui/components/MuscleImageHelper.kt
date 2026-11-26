package com.fitpulse.ai.ui.components

import androidx.annotation.DrawableRes
import com.fitpulse.ai.R
import com.fitpulse.ai.data.model.MuscleGroup

object MuscleImageHelper {
    @DrawableRes
    fun drawableForMuscle(group: MuscleGroup): Int {
        return when (group) {
            MuscleGroup.CHEST -> R.drawable.muscle_chest
            MuscleGroup.BACK -> R.drawable.muscle_back
            MuscleGroup.SHOULDERS -> R.drawable.muscle_shoulders
            MuscleGroup.BICEPS -> R.drawable.muscle_biceps
            MuscleGroup.TRICEPS -> R.drawable.muscle_triceps
            MuscleGroup.QUADS -> R.drawable.muscle_quads
            MuscleGroup.HAMSTRINGS -> R.drawable.muscle_hamstrings
            MuscleGroup.GLUTES -> R.drawable.muscle_glutes
            MuscleGroup.CALVES -> R.drawable.muscle_calves
            MuscleGroup.CORE -> R.drawable.muscle_core
        }
    }

    @DrawableRes
    fun drawableForTarget(targetMuscle: String): Int {
        return drawableForMuscle(MuscleGroup.fromTargetMuscle(targetMuscle))
    }
}
