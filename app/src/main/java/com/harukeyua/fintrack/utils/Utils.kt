package com.harukeyua.fintrack

import android.content.Context
import android.text.InputFilter
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt

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
    else if (balance < 100 && balance > -100) {
        if (balance > 0)
            "0.$balance"
        else
            "-0.${balance * -1}"
    } else
        StringBuilder(balanceString).insert(balanceString.length - 2, ".").toString()
}

fun convertToPenny(item: String): Long {
    val dotPosition = item.indexOfFirst { it == ',' || it == '.' }
    return if (item.contains("[.,]".toRegex()) && item.length - dotPosition == 3)
        (item.replace("[.,]".toRegex(), "")).toLong()
    else if (item.contains("[.,]".toRegex()) && item.length - dotPosition == 2)
        (item.replace("[.,]".toRegex(), "0")).toLong()
    else if (item.isNotEmpty())
        item.plus("00").toLong()
    else
        0L
}