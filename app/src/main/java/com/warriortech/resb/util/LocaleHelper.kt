package com.warriortech.resb.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*
import androidx.core.content.edit

object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    private const val LANGUAGE_ENGLISH = "en"
    private const val LANGUAGE_TAMIL = "ta"

    fun onAttach(context: Context): Context {
        val lang = getPersistedData(context, Locale.getDefault().language)
        return setLocale(context, lang)
    }

    fun onAttach(context: Context, defaultLanguage: String): Context {
        val lang = getPersistedData(context, defaultLanguage)
        return setLocale(context, lang)
    }

    fun getLanguage(context: Context): String {
        return getPersistedData(context, Locale.getDefault().language)
    }

    @SuppressLint("ObsoleteSdkInt")
    fun setLocale(context: Context, language: String): Context {
        persist(context, language)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, language)
        } else {
            updateResourcesLegacy(context, language)
        }
    }

    private fun getPersistedData(context: Context, defaultLanguage: String): String {
        val preferences = context.getSharedPreferences("locale_preferences", Context.MODE_PRIVATE)
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage) ?: defaultLanguage
    }

    private fun persist(context: Context, language: String) {
        val preferences = context.getSharedPreferences("locale_preferences", Context.MODE_PRIVATE)
        preferences.edit {
            putString(SELECTED_LANGUAGE, language)
            apply()
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    @SuppressLint("ObsoleteSdkInt", "Deprecation")
    private fun updateResourcesLegacy(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale)
        }

        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }

    fun getCurrentLocale(context: Context): Locale {
        val language = getPersistedData(context, Locale.getDefault().language)
        return Locale(language)
    }

    fun isRTL(context: Context): Boolean {
        val locale = getCurrentLocale(context)
        return locale.language == "ar" || locale.language == "fa" || locale.language == "he"
    }

    fun getAvailableLanguages(): List<Pair<String, String>> {
        return listOf(
            LANGUAGE_ENGLISH to "English",
            LANGUAGE_TAMIL to "தமிழ்"
        )
    }

    suspend fun applyLocale(context: Context): Context {
        val lang = getLanguage(context)
        return setLocale(context, lang)
    }

    fun isEnglish(context: Context): Boolean {
        return getLanguage(context) == LANGUAGE_ENGLISH
    }

    fun isTamil(context: Context): Boolean {
        return getLanguage(context) == LANGUAGE_TAMIL
    }
}
