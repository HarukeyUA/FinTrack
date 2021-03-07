package com.harukeyua.fintrack

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources

fun Context.getThemeColor(@AttrRes attribute: Int): ColorStateList = TypedValue().let {
    theme.resolveAttribute(
        attribute,
        it,
        true
    )
    AppCompatResources.getColorStateList(this, it.resourceId)
}

@ColorInt
fun Context.getThemedColor(@AttrRes attribute: Int) =
    TypedValue().let {
        theme.resolveAttribute(attribute, it, true)
        it.data
    }