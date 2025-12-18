package com.warriortech.resb.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.util.LocaleHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LocaleViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _currentLanguage = MutableStateFlow(LocaleHelper.getLanguage(context))
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _currentLocale = MutableStateFlow(LocaleHelper.getCurrentLocale(context))
    val currentLocale: StateFlow<Locale> = _currentLocale.asStateFlow()

    private val _isRTL = MutableStateFlow(LocaleHelper.isRTL(context))
    val isRTL: StateFlow<Boolean> = _isRTL.asStateFlow()

    private val _availableLanguages = MutableStateFlow(LocaleHelper.getAvailableLanguages())
    val availableLanguages: StateFlow<List<Pair<String, String>>> =
        _availableLanguages.asStateFlow()

    fun changeLanguage(language: String) {
        viewModelScope.launch {
            LocaleHelper.setLocale(context, language)
            _currentLanguage.value = language
            _currentLocale.value = LocaleHelper.getCurrentLocale(context)
            _isRTL.value = LocaleHelper.isRTL(context)
        }
    }

    fun refreshLocaleState() {
        viewModelScope.launch {
            _currentLanguage.value = LocaleHelper.getLanguage(context)
            _currentLocale.value = LocaleHelper.getCurrentLocale(context)
            _isRTL.value = LocaleHelper.isRTL(context)
        }
    }
}
