package com.harukeyua.fintrack.data.model.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AccountInfo(
    val id: String,
    val balance: Long,
    val creditLimit: Long,
    val type: String,
    val currencyCode: Int,
    val cashbackType: String,
    val maskedPan: List<String>
)
