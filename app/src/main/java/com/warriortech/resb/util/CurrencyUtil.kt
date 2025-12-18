package com.warriortech.resb.util

import java.text.NumberFormat
import java.util.*

object CurrencySettings {
    var currencySymbol: String = "₹"
    var decimalPlaces: Int = 2

    fun update(symbol: String, decimals: Int) {
        currencySymbol = symbol
        decimalPlaces = decimals
    }

    fun format(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        formatter.minimumFractionDigits = decimalPlaces
        formatter.maximumFractionDigits = decimalPlaces

        val formatted = formatter.format(amount)
        return "$currencySymbol$formatted"
    }

    fun formatPlain(amount: Double): String {
        return String.format("%.${decimalPlaces}f", amount)
    }
}
