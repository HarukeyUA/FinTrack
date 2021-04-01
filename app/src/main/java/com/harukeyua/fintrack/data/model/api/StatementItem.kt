package com.harukeyua.fintrack.data.model.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StatementItem(
    val id: String,
    val time: Long,
    val description: String,
    val mcc: Int,
    val hold: Boolean,
    val amount: Long,
    val operationAmount: Long,
    val currencyCode: Int,
    val commissionRate: Long,
    val cashbackAmount: Long,
    val balance: Long
)