package com.harukeyua.fintrack

import android.content.Context
import android.content.res.ColorStateList
import android.text.InputFilter
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources

@ColorInt
fun Context.getThemedColor(@AttrRes attribute: Int) =
    TypedValue().let {
        theme.resolveAttribute(attribute, it, true)
        it.data
    }

val currencyInputFilter =
    InputFilter { source, _, _, dest, _, _ ->
        val decimalIndex = dest.indexOfFirst { it == ',' || it == '.' }
        if (dest.length - decimalIndex > 2 && decimalIndex != -1)
            ""
        else if (source.contains("[.,]".toRegex()) && dest.isEmpty())
            ""
        else
            null
    }

fun getConvertedBalance(balance: Long): String {
    val balanceString = balance.toString()
    return if (balance == 0L)
        "0.00"
    else
        StringBuilder(balanceString).insert(balanceString.length - 2, ".").toString()
}