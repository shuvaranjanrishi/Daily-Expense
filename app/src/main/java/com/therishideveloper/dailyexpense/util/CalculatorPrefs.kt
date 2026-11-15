package com.therishideveloper.dailyexpense.util

import android.content.Context
import androidx.core.content.edit

object CalculatorPrefs {
    private const val PREFS_NAME = "Settings"
    private const val KEY_CALCULATOR_SCALE = "calculator_scale"

    const val MIN_SCALE = 0.65f
    const val DEFAULT_SCALE = 0.78f
    const val MAX_SCALE = 1.35f

    fun getSavedScale(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_CALCULATOR_SCALE)) return DEFAULT_SCALE
        return prefs.getFloat(KEY_CALCULATOR_SCALE, DEFAULT_SCALE)
            .coerceIn(MIN_SCALE, MAX_SCALE)
    }

    fun saveScale(context: Context, scale: Float) {
        val clamped = scale.coerceIn(MIN_SCALE, MAX_SCALE)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putFloat(KEY_CALCULATOR_SCALE, clamped) }
    }

    fun resetScale(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { remove(KEY_CALCULATOR_SCALE) }
    }
}
