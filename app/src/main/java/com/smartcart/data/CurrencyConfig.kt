package com.smartcart.data

import java.util.Locale

object CurrencyConfig {
    const val SYMBOL = "₸"
    const val CODE = "KZT"
    const val POSITION: String = "after" // "before" or "after"

    fun format(amount: Double): String {
        val tenge = (amount).toInt()
        return formatInt(tenge)
    }

    fun formatInt(amount: Int): String {
        val formatted = String.format(Locale("ru", "KZ"), "%,d", amount).replace(",", " ")
        return if (POSITION == "before") "$SYMBOL $formatted" else "$formatted $SYMBOL"
    }
}
