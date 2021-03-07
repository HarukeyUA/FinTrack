package com.harukeyua.fintrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moneyStores")
data class MoneyStore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: StoreType,
    val balance: Long,
    val monoCardType: String? = null
) {
    fun getConvertedBalance(): Float = (balance/100).toFloat()
}

enum class StoreType {
    CARD, CASH, MONO
}
