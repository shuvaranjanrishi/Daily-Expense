package com.therishideveloper.dailyexpense.util

import android.content.Context
import com.therishideveloper.dailyexpense.R
import java.text.NumberFormat
import java.util.Locale

object NumberUtils {

    private val BN_DIGITS = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    private val HI_DIGITS = charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')
    private val EN_IN_LOCALE = Locale.forLanguageTag("en-IN")

    fun formatByLocale(context: Context, input: String): String {
        val language = LocaleHelper.getCurrentLocale(context).language
        return convertDigits(input, language)
    }

    fun formatAmountByLocale(context: Context, input: String): String {
        val cleanInput = input.replace(",", "")
        val amount = cleanInput.toDoubleOrNull() ?: return input
        val language = LocaleHelper.getCurrentLocale(context).language

        val commaFormatter = NumberFormat.getInstance(EN_IN_LOCALE).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = if (amount == 0.0) 1 else 0
        }

        val formattedNumber = commaFormatter.format(amount)
        val finalNumber = convertDigits(formattedNumber, language)
        val symbol = context.getString(R.string.currency_symbol)

        return "$symbol $finalNumber"
    }

    private fun convertDigits(input: String, language: String): String {
        val digits = when (language) {
            "bn" -> BN_DIGITS
            "hi" -> HI_DIGITS
            else -> return input
        }

        return buildString(input.length) {
            for (ch in input) {
                append(if (ch in '0'..'9') digits[ch - '0'] else ch)
            }
        }
    }
}