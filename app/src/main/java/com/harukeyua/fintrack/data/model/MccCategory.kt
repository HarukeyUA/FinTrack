package com.harukeyua.fintrack.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MccCategory(
    val mcc: Int,
    val shortDescription: String,
    val fullDescription: String
)
