package com.therishideveloper.dailyexpense.util

import android.content.Context
import androidx.core.content.edit

object ThemeHelper {
    private const val PREFS_NAME = "Settings"
    private const val KEY_DARK_MODE = "dark_mode"

    fun isDarkMode(context: Context, systemIsDark: Boolean): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.contains(KEY_DARK_MODE)) {
            prefs.getBoolean(KEY_DARK_MODE, systemIsDark)
        } else {
            systemIsDark
        }
    }

    fun setDarkMode(context: Context, isDark: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit(commit = true) { putBoolean(KEY_DARK_MODE, isDark) }
    }
}
