package com.fintracker.data.prefs

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fintrack_prefs", Context.MODE_PRIVATE)

    // Флаг онбординга для конкретного пользователя
    fun isOnboardingShown(userId: String): Boolean =
        prefs.getBoolean("onboarding_shown_$userId", false)

    fun setOnboardingShown(userId: String) =
        prefs.edit().putBoolean("onboarding_shown_$userId", true).apply()

    // Можно оставить старые методы для обратной совместимости, но лучше их удалить
    // или переименовать, чтобы не путаться.
    // Но для простоты оставим, но используем новые.
}