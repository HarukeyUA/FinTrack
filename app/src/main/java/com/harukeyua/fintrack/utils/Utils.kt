/*
 * Copyright  2021 Nazar Rusnak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.harukeyua.fintrack.utils

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

fun getFloatBalance(balance: Long): Float? {
    return if (balance >= 100 || balance <= -100)
        balance.toString().dropLast(2).toFloatOrNull()
    else
        0f
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