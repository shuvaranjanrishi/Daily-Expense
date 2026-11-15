package com.therishideveloper.dailyexpense.util

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.core.content.edit
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "Settings"
    private const val KEY_LANG = "app_lang"
    private const val DEFAULT_LANG = "bn"

    fun applyLocale(context: Context, langCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit(commit = true) { putString(KEY_LANG, langCode) }

        applyLocaleToResources(context, langCode)
        val appContext = context.applicationContext
        if (appContext != null && appContext !== context) {
            applyLocaleToResources(appContext, langCode)
        }
    }

    fun applyLocaleToResources(context: Context, langCode: String = getSavedLocale(context)) {
        val locale = Locale.forLanguageTag(langCode)
        Locale.setDefault(locale)
        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        config.setLocales(LocaleList(locale))
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun wrapContext(context: Context): Context {
        val langCode = getSavedLocale(context)
        val locale = Locale.forLanguageTag(langCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLocales(LocaleList(locale))
        return context.createConfigurationContext(config)
    }

    fun getSavedLocale(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANG, DEFAULT_LANG) ?: DEFAULT_LANG
    }

    fun getCurrentLocale(context: Context): Locale {
        return Locale.forLanguageTag(getSavedLocale(context))
    }
}
