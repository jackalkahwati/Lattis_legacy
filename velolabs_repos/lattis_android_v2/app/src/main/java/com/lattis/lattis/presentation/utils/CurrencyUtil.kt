package com.lattis.lattis.presentation.utils

import android.text.TextUtils
import androidx.core.text.isDigitsOnly
import java.text.NumberFormat
import java.util.*

object CurrencyUtil {
    private const val defaultCurrencySymbol = "$"
    private const val defaultCost = "0"
    private const val defaultCurrencyCost = defaultCurrencySymbol + defaultCost
    private const val defaultCurrencyCode = "USD"

    fun getCurrencySymbolByCode(code: String?,cost:String?): String {
        if(TextUtils.isEmpty(cost) && TextUtils.isEmpty(code)) {
            return defaultCurrencyCost
        }else if(TextUtils.isEmpty(cost) && !TextUtils.isEmpty(code)){
            for (currency in Currency.getAvailableCurrencies()) {
                if (currency.currencyCode == code) {
                    return currency.getSymbol(Locale.US) + defaultCost
                }
            }
            return defaultCurrencyCost
        }else{
            return getNumberFormat(if(TextUtils.isEmpty(code)) defaultCurrencyCode else code!!)?.format(if(cost?.isDigitsOnly()!!) cost.toInt() else cost.toFloat())!!
        }
    }

    private fun getNumberFormat(currencyCode: String): NumberFormat? {
        val currency = Currency.getInstance(currencyCode)
        val locales = NumberFormat.getAvailableLocales()
        for (locale in locales) {
            val numberFormat = NumberFormat.getCurrencyInstance(locale)
            if (numberFormat.currency == currency) return numberFormat
        }
        return null
    }
}