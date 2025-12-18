package com.warriortech.resb.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun stringResource(@StringRes id: Int): String {
    val context = LocalContext.current
    return context.getString(id)
}

@Composable
fun stringResource(@StringRes id: Int, vararg formatArgs: Any): String {
    val context = LocalContext.current
    return context.getString(id, *formatArgs)
}

fun Context.getLocalizedString(@StringRes id: Int): String {
    val localizedContext = LocaleHelper.onAttach(this)
    return localizedContext.getString(id)
}

fun Context.getLocalizedString(@StringRes id: Int, vararg formatArgs: Any): String {
    val localizedContext = LocaleHelper.onAttach(this)
    return localizedContext.getString(id, *formatArgs)
}
