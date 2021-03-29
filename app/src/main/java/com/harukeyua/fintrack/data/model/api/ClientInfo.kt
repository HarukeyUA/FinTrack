package com.harukeyua.fintrack.data.model.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClientInfo(
    val clientId: String,
    val name: String,
    val accounts: List<AccountInfo>
)
