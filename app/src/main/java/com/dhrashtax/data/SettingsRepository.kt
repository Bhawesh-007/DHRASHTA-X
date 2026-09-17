package com.dhrashtax.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val onboardingComplete: Boolean = false,
    val voiceAlerts: Boolean = true,
    val trustedPackages: Set<String> = emptySet(),
)

class SettingsRepository(context: Context) {
    private val preferences = context.getSharedPreferences("dhrashta_settings", Context.MODE_PRIVATE)
    private val mutableSettings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = mutableSettings.asStateFlow()

    fun completeOnboarding() = update {
        preferences.edit().putBoolean(KEY_ONBOARDING, true).apply()
        copy(onboardingComplete = true)
    }

    fun setVoiceAlerts(enabled: Boolean) = update {
        preferences.edit().putBoolean(KEY_VOICE, enabled).apply()
        copy(voiceAlerts = enabled)
    }

    fun trustPackage(packageName: String) {
        val normalized = packageName.trim()
        if (!PACKAGE_PATTERN.matches(normalized)) return
        update {
            val updated = trustedPackages + normalized
            preferences.edit().putStringSet(KEY_TRUSTED, updated).apply()
            copy(trustedPackages = updated)
        }
    }

    fun removeTrustedPackage(packageName: String) = update {
        val updated = trustedPackages - packageName
        preferences.edit().putStringSet(KEY_TRUSTED, updated).apply()
        copy(trustedPackages = updated)
    }

    private fun load() = AppSettings(
        onboardingComplete = preferences.getBoolean(KEY_ONBOARDING, false),
        voiceAlerts = preferences.getBoolean(KEY_VOICE, true),
        trustedPackages = preferences.getStringSet(KEY_TRUSTED, emptySet()).orEmpty().toSet(),
    )

    private inline fun update(block: AppSettings.() -> AppSettings) {
        mutableSettings.value = mutableSettings.value.block()
    }

    private companion object {
        const val KEY_ONBOARDING = "onboarding_complete"
        const val KEY_VOICE = "voice_alerts"
        const val KEY_TRUSTED = "trusted_packages"
        val PACKAGE_PATTERN = Regex("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+$")
    }
}

