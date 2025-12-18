package com.warriortech.resb.util

import android.content.Context
import android.content.res.Configuration
import androidx.activity.ComponentActivity

abstract class LocaleAwareActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase ?: this))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.onAttach(this)
        recreate()
    }

    protected fun changeLanguage(language: String) {
        LocaleHelper.setLocale(this, language)
        recreate()
    }

    protected fun getCurrentLanguage(): String {
        return LocaleHelper.getLanguage(this)
    }

    protected fun isRTL(): Boolean {
        return LocaleHelper.isRTL(this)
    }
}
